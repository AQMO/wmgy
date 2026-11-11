package com.orchard.service;

import com.orchard.common.BizException;
import com.orchard.entity.Coupon;
import com.orchard.entity.Store;
import com.orchard.entity.UserCoupon;
import com.orchard.repository.CouponRepository;
import com.orchard.repository.StoreRepository;
import com.orchard.repository.UserCouponRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 优惠券营销（二期）：商家发券 → 用户领取 → 下单抵扣 */
@Service
public class CouponService {

    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;
    private final StoreRepository storeRepository;

    public CouponService(CouponRepository couponRepository, UserCouponRepository userCouponRepository,
                         StoreRepository storeRepository) {
        this.couponRepository = couponRepository;
        this.userCouponRepository = userCouponRepository;
        this.storeRepository = storeRepository;
    }

    // ---------- 商家端 ----------

    @Transactional
    public Coupon create(Long merchantUid, Coupon c) {
        Store s = storeRepository.findByOwnerId(merchantUid)
                .orElseThrow(() -> new BizException(40004, "尚未开通店铺"));
        if (c.getName() == null || c.getName().isEmpty()) throw new BizException("请填写券名称");
        if (c.getAmount() == null || c.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException("优惠金额必须大于 0");
        }
        if (c.getThreshold() == null) c.setThreshold(BigDecimal.ZERO);
        if (c.getTotal() == null || c.getTotal() <= 0) throw new BizException("发放总量必须大于 0");
        c.setStoreId(s.getId());
        c.setRemain(c.getTotal());
        return couponRepository.save(c);
    }

    public List<Map<String, Object>> merchantList(Long merchantUid) {
        Store s = storeRepository.findByOwnerId(merchantUid)
                .orElseThrow(() -> new BizException(40004, "尚未开通店铺"));
        List<Map<String, Object>> list = new ArrayList<>();
        for (Coupon c : couponRepository.findByStoreIdOrderByIdDesc(s.getId())) {
            list.add(toMap(c, null));
        }
        return list;
    }

    /** 停用/启用 */
    @Transactional
    public Coupon toggle(Long merchantUid, Long id, Integer status) {
        Store s = storeRepository.findByOwnerId(merchantUid)
                .orElseThrow(() -> new BizException(40004, "尚未开通店铺"));
        Coupon c = couponRepository.findById(id).orElseThrow(() -> new BizException(40004, "优惠券不存在"));
        if (!c.getStoreId().equals(s.getId())) {
            throw new BizException(BizException.FORBIDDEN, "无权操作他人优惠券");
        }
        c.setStatus(status != null && status == 1 ? 1 : 0);
        return couponRepository.save(c);
    }

    // ---------- 消费者端 ----------

    /** 店铺可领券列表（生效中且有余量） */
    public List<Map<String, Object>> storeCoupons(Long storeId) {
        List<Map<String, Object>> list = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (Coupon c : couponRepository.findByStoreIdOrderByIdDesc(storeId)) {
            boolean effective = c.getStatus() != null && c.getStatus() == 1
                    && (c.getStartTime() == null || !now.isBefore(c.getStartTime()))
                    && (c.getEndTime() == null || !now.isAfter(c.getEndTime()))
                    && c.getRemain() != null && c.getRemain() > 0;
            if (effective) {
                list.add(toMap(c, null));
            }
        }
        return list;
    }

    @Transactional
    public UserCoupon claim(Long uid, Long couponId) {
        Coupon c = couponRepository.findById(couponId).orElseThrow(() -> new BizException(40004, "优惠券不存在"));
        if (c.getStatus() == null || c.getStatus() != 1) throw new BizException("该券已停用");
        LocalDateTime now = LocalDateTime.now();
        if (c.getStartTime() != null && now.isBefore(c.getStartTime())) throw new BizException("活动尚未开始");
        if (c.getEndTime() != null && now.isAfter(c.getEndTime())) throw new BizException("活动已结束");
        if (c.getRemain() == null || c.getRemain() <= 0) throw new BizException("已抢光");
        if (userCouponRepository.existsByCouponIdAndUserId(couponId, uid)) {
            throw new BizException("已领取过该券");
        }
        c.setRemain(c.getRemain() - 1);
        couponRepository.save(c);
        UserCoupon uc = new UserCoupon();
        uc.setCouponId(couponId);
        uc.setUserId(uid);
        return userCouponRepository.save(uc);
    }

    /** 我的持券（含券详情） */
    public List<Map<String, Object>> mine(Long uid, String status) {
        String st = (status == null || status.isEmpty()) ? "UNUSED" : status;
        List<Map<String, Object>> list = new ArrayList<>();
        for (UserCoupon uc : userCouponRepository.findByUserIdAndStatusOrderByIdDesc(uid, st)) {
            Coupon c = couponRepository.findById(uc.getCouponId()).orElse(null);
            if (c != null) {
                Map<String, Object> m = toMap(c, null);
                m.put("userCouponId", uc.getId());
                m.put("userCouponStatus", uc.getStatus());
                list.add(m);
            }
        }
        return list;
    }

    /** 下单校验并锁定用券：返回抵扣金额；成功后将持券标记为已使用 */
    @Transactional
    public BigDecimal applyToOrder(Long uid, Long userCouponId, Long storeId, BigDecimal orderTotal, Long orderId) {
        UserCoupon uc = userCouponRepository.findById(userCouponId)
                .orElseThrow(() -> new BizException(40004, "优惠券不存在"));
        if (!uc.getUserId().equals(uid)) throw new BizException(BizException.FORBIDDEN, "无权使用该券");
        if (!"UNUSED".equals(uc.getStatus())) throw new BizException("优惠券不可用");
        Coupon c = couponRepository.findById(uc.getCouponId())
                .orElseThrow(() -> new BizException(40004, "优惠券不存在"));
        if (!c.getStoreId().equals(storeId)) throw new BizException("该券仅限对应店铺使用");
        LocalDateTime now = LocalDateTime.now();
        if (c.getEndTime() != null && now.isAfter(c.getEndTime())) throw new BizException("优惠券已过期");
        if (orderTotal.compareTo(c.getThreshold()) < 0) {
            throw new BizException("未达到用券门槛（满 " + c.getThreshold() + " 元可用）");
        }
        int updated = userCouponRepository.use(uc.getId(), uid, orderId);
        if (updated == 0) throw new BizException("优惠券已被使用");
        return c.getAmount().min(orderTotal);
    }

    /** 查询持券对应券的店铺 ID（下单定位用） */
    public Long storeOf(Long userCouponId) {
        UserCoupon uc = userCouponRepository.findById(userCouponId)
                .orElseThrow(() -> new BizException(40004, "优惠券不存在"));
        Coupon c = couponRepository.findById(uc.getCouponId())
                .orElseThrow(() -> new BizException(40004, "优惠券不存在"));
        return c.getStoreId();
    }

    /** 支付前订单快照（供商家端查看用券信息） */
    public Map<String, Object> couponInfo(Long userCouponId) {
        UserCoupon uc = userCouponRepository.findById(userCouponId).orElse(null);
        if (uc == null) return null;
        Coupon c = couponRepository.findById(uc.getCouponId()).orElse(null);
        if (c == null) return null;
        return toMap(c, uc);
    }

    private Map<String, Object> toMap(Coupon c, UserCoupon uc) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", c.getId());
        m.put("storeId", c.getStoreId());
        storeRepository.findById(c.getStoreId()).ifPresent(s -> m.put("storeName", s.getName()));
        m.put("name", c.getName());
        m.put("threshold", c.getThreshold());
        m.put("amount", c.getAmount());
        m.put("total", c.getTotal());
        m.put("remain", c.getRemain());
        m.put("startTime", c.getStartTime());
        m.put("endTime", c.getEndTime());
        m.put("status", c.getStatus());
        if (uc != null) {
            m.put("userCouponId", uc.getId());
            m.put("userCouponStatus", uc.getStatus());
        }
        return m;
    }
}
