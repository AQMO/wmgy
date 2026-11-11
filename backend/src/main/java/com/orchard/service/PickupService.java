package com.orchard.service;

import com.orchard.common.BizException;
import com.orchard.entity.PickupAppointment;
import com.orchard.entity.Product;
import com.orchard.entity.Store;
import com.orchard.repository.PickupAppointmentRepository;
import com.orchard.repository.ProductRepository;
import com.orchard.repository.StoreRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/** 采摘/到园自提预约（二期） */
@Service
public class PickupService {

    private final PickupAppointmentRepository appointmentRepository;
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final NotificationService notificationService;

    public PickupService(PickupAppointmentRepository appointmentRepository,
                         ProductRepository productRepository, StoreRepository storeRepository,
                         NotificationService notificationService) {
        this.appointmentRepository = appointmentRepository;
        this.productRepository = productRepository;
        this.storeRepository = storeRepository;
        this.notificationService = notificationService;
    }

    /** 消费者提交预约 */
    @Transactional
    public PickupAppointment create(Long uid, Long productId, LocalDate pickDate,
                                    String timeSlot, Integer quantity, String phone, String remark) {
        if (pickDate == null) throw new BizException("请选择到园日期");
        if (pickDate.isBefore(LocalDate.now())) throw new BizException("预约日期不能早于今天");
        if (timeSlot == null || timeSlot.isEmpty()) throw new BizException("请选择时间段");
        if (quantity == null || quantity <= 0) throw new BizException("预约人数/数量必须大于 0");
        Product p = productRepository.findById(productId)
                .orElseThrow(() -> new BizException(40004, "商品不存在"));
        Store s = storeRepository.findById(p.getStoreId())
                .orElseThrow(() -> new BizException(40004, "店铺不存在"));
        if (!"ACTIVE".equals(s.getStatus())) throw new BizException("店铺暂不可预约");

        PickupAppointment a = new PickupAppointment();
        a.setStoreId(s.getId());
        a.setProductId(p.getId());
        a.setProductName(p.getName());
        a.setUserId(uid);
        a.setPickDate(pickDate);
        a.setTimeSlot(timeSlot);
        a.setQuantity(quantity);
        a.setPhone(phone);
        a.setRemark(remark);
        PickupAppointment saved = appointmentRepository.save(a);
        notificationService.push(s.getOwnerId(), "新采摘预约",
                "买家预约了「" + p.getName() + "」，" + pickDate + " " + timeSlot + "，请及时确认", "PICKUP", saved.getId());
        return saved;
    }

    public Page<PickupAppointment> mine(Long uid, int page, int size) {
        return appointmentRepository.findByUserIdOrderByIdDesc(uid, PageRequest.of(page, size));
    }

    public Page<PickupAppointment> byStore(Long merchantUid, String status, int page, int size) {
        Store s = storeRepository.findByOwnerId(merchantUid)
                .orElseThrow(() -> new BizException(40004, "尚未开通店铺"));
        PageRequest pr = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        if (status != null && !status.isEmpty() && !"ALL".equals(status)) {
            return appointmentRepository.findByStoreIdAndStatusOrderByIdDesc(s.getId(), status, pr);
        }
        return appointmentRepository.findByStoreIdOrderByIdDesc(s.getId(), pr);
    }

    /** 商家确认/拒绝/完成 */
    @Transactional
    public PickupAppointment handle(Long merchantUid, Long id, String action, String storeReply) {
        Store s = storeRepository.findByOwnerId(merchantUid)
                .orElseThrow(() -> new BizException(40004, "尚未开通店铺"));
        PickupAppointment a = appointmentRepository.findById(id)
                .orElseThrow(() -> new BizException(40004, "预约不存在"));
        if (!a.getStoreId().equals(s.getId())) {
            throw new BizException(BizException.FORBIDDEN, "无权操作他人预约");
        }
        if (!"CONFIRM".equals(action) && !"REJECT".equals(action) && !"DONE".equals(action)) {
            throw new BizException("不支持的操作");
        }
        String target = "CONFIRM".equals(action) ? "CONFIRMED" : ("REJECT".equals(action) ? "REJECTED" : "DONE");
        if (!"PENDING".equals(a.getStatus()) && "DONE".equals(action) && !"CONFIRMED".equals(a.getStatus())) {
            throw new BizException("当前状态不支持该操作");
        }
        a.setStatus(target);
        a.setStoreReply(storeReply);
        PickupAppointment saved = appointmentRepository.save(a);
        String title = "CONFIRMED".equals(target) ? "采摘预约已确认" : ("REJECTED".equals(target) ? "采摘预约被拒绝" : "采摘预约已完成");
        notificationService.push(a.getUserId(), title,
                "您预约的「" + a.getProductName() + "」" + a.getPickDate() + " " + a.getTimeSlot()
                        + " 状态：" + target + (storeReply == null ? "" : "，商家留言：" + storeReply),
                "PICKUP", saved.getId());
        return saved;
    }

    /** 消费者取消 */
    @Transactional
    public PickupAppointment cancel(Long uid, Long id) {
        PickupAppointment a = appointmentRepository.findById(id)
                .orElseThrow(() -> new BizException(40004, "预约不存在"));
        if (!a.getUserId().equals(uid)) {
            throw new BizException(BizException.FORBIDDEN, "无权操作该预约");
        }
        if (!"PENDING".equals(a.getStatus()) && !"CONFIRMED".equals(a.getStatus())) {
            throw new BizException("当前状态不可取消");
        }
        a.setStatus("CANCELLED");
        return appointmentRepository.save(a);
    }

    public Map<String, Object> stats(Long merchantUid) {
        Store s = storeRepository.findByOwnerId(merchantUid)
                .orElseThrow(() -> new BizException(40004, "尚未开通店铺"));
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("pending", appointmentRepository.countByStoreIdAndStatus(s.getId(), "PENDING"));
        m.put("confirmed", appointmentRepository.countByStoreIdAndStatus(s.getId(), "CONFIRMED"));
        m.put("total", appointmentRepository.countByStoreId(s.getId()));
        return m;
    }
}
