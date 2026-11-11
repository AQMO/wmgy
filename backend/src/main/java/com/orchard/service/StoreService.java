package com.orchard.service;

import com.orchard.common.BizException;
import com.orchard.dto.StoreApplyReq;
import com.orchard.entity.*;
import com.orchard.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class StoreService {

    private final StoreRepository storeRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ConfigRepository configRepository;
    private final SettlementFlowRepository flowRepository;
    private final OperationLogRepository opLogRepository;
    private final NotificationService notificationService;

    public StoreService(StoreRepository storeRepository, CategoryRepository categoryRepository,
                        ProductRepository productRepository, UserRepository userRepository,
                        ConfigRepository configRepository, SettlementFlowRepository flowRepository,
                        OperationLogRepository opLogRepository, NotificationService notificationService) {
        this.storeRepository = storeRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.configRepository = configRepository;
        this.flowRepository = flowRepository;
        this.opLogRepository = opLogRepository;
        this.notificationService = notificationService;
    }

    /** 商家提交入驻申请（消费者可升级为商家） */
    @Transactional
    public Store apply(Long uid, StoreApplyReq req) {
        if (storeRepository.findByOwnerId(uid).isPresent()) {
            throw new BizException("您已提交过入驻申请");
        }
        Store s = new Store();
        s.setOwnerId(uid);
        s.setName(req.getName());
        s.setAddress(req.getAddress());
        s.setIntro(req.getIntro());
        s.setLogo(req.getLogo());
        s.setCover(req.getCover());
        s.setStatus("PENDING");
        s = storeRepository.save(s);

        User u = userRepository.findById(uid).orElseThrow(() -> new com.orchard.common.BizException(40004, "用户不存在"));
        if ("USER".equals(u.getRole())) {
            u.setRole("MERCHANT");
            userRepository.save(u);
        }
        log(uid, "STORE_APPLY", "提交入驻申请：" + s.getName());
        for (User admin : userRepository.findByRole("ADMIN")) {
            notificationService.push(admin.getId(), "新商家入驻申请",
                    "果园「" + s.getName() + "」提交了入驻申请，请及时审核", "AUDIT", s.getId());
        }
        return s;
    }

    public Store myStore(Long uid) {
        return storeRepository.findByOwnerId(uid)
                .orElseThrow(() -> new BizException(40004, "尚未开通店铺，请先提交入驻申请"));
    }

    /** 店铺装修（PRD 4.3） */
    @Transactional
    public Store updateStore(Long uid, Map<String, Object> body) {
        Store s = myStore(uid);
        if (body.containsKey("name")) s.setName((String) body.get("name"));
        if (body.containsKey("intro")) s.setIntro((String) body.get("intro"));
        if (body.containsKey("notice")) s.setNotice((String) body.get("notice"));
        if (body.containsKey("logo")) s.setLogo((String) body.get("logo"));
        if (body.containsKey("cover")) s.setCover((String) body.get("cover"));
        if (body.containsKey("address")) s.setAddress((String) body.get("address"));
        if (body.containsKey("selfPickup")) s.setSelfPickup((Boolean) body.get("selfPickup"));
        return storeRepository.save(s);
    }

    /** C端店铺列表（仅营业中） */
    public Page<Store> listActive(String keyword, int page, int size) {
        PageRequest pr = PageRequest.of(page, size);
        if (keyword != null && !keyword.isEmpty()) {
            return storeRepository.findByNameContainingAndStatus(keyword, "ACTIVE", pr);
        }
        return storeRepository.findByStatus("ACTIVE", pr);
    }

    /** 店铺首页详情（PRD 3.2） */
    public Map<String, Object> detail(Long storeId) {
        Store s = storeRepository.findById(storeId)
                .orElseThrow(() -> new BizException(40004, "店铺不存在"));
        if (!"ACTIVE".equals(s.getStatus())) {
            throw new BizException(40004, "店铺暂不可访问");
        }
        List<Category> categories = categoryRepository.findByStoreIdOrderBySortAsc(storeId);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("store", s);
        map.put("categories", categories);
        return map;
    }

    // ---------- 分类管理（PRD 4.4） ----------

    @Transactional
    public Category addCategory(Long uid, Map<String, Object> body) {
        Store s = myStore(uid);
        String name = (String) body.get("name");
        if (name == null || name.trim().isEmpty()) {
            throw new BizException("分类名称不能为空");
        }
        Category c = new Category();
        c.setStoreId(s.getId());
        c.setName(name.trim());
        c.setSort(body.get("sort") == null ? 0 : (Integer) body.get("sort"));
        return categoryRepository.save(c);
    }

    @Transactional
    public void deleteCategory(Long uid, Long categoryId) {
        Store s = myStore(uid);
        Category c = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BizException(40004, "分类不存在"));
        if (!c.getStoreId().equals(s.getId())) {
            throw new BizException(BizException.FORBIDDEN, "无权操作他人分类");
        }
        categoryRepository.delete(c);
    }

    public List<Category> myCategories(Long uid) {
        return categoryRepository.findByStoreIdOrderBySortAsc(myStore(uid).getId());
    }

    /** 平台审核时设置保证金并记录流水 */
    @Transactional
    public void activateWithDeposit(Store s) {
        String v = configRepository.findById("DEFAULT_DEPOSIT").map(Config::getConfigValue).orElse("5000");
        s.setDeposit(new BigDecimal(v));
        s.setStatus("ACTIVE");
        storeRepository.save(s);
        SettlementFlow f = new SettlementFlow();
        f.setStoreId(s.getId());
        f.setType("DEPOSIT");
        f.setAmount(s.getDeposit());
        f.setRemark("店铺开通，缴纳保证金");
        flowRepository.save(f);
    }

    private void log(Long operator, String action, String detail) {
        OperationLog l = new OperationLog();
        l.setOperatorId(operator);
        l.setModule("STORE");
        l.setAction(action);
        l.setDetail(detail);
        opLogRepository.save(l);
    }
}
