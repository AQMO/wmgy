package com.orchard.config;

import com.orchard.entity.*;
import com.orchard.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** 种子数据：管理员、演示商家店铺、演示商品（仅首启时创建） */
@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CommissionRuleRepository ruleRepository;
    private final ConfigRepository configRepository;

    public DataInitializer(UserRepository userRepository, StoreRepository storeRepository,
                           CategoryRepository categoryRepository, ProductRepository productRepository,
                           CommissionRuleRepository ruleRepository, ConfigRepository configRepository) {
        this.userRepository = userRepository;
        this.storeRepository = storeRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.ruleRepository = ruleRepository;
        this.configRepository = configRepository;
    }

    @Override
    public void run(String... args) {
        BCryptPasswordEncoder enc = new BCryptPasswordEncoder();
        if (!userRepository.findByUsername("admin").isPresent()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(enc.encode("admin123"));
            admin.setNickname("平台管理员");
            admin.setRole("ADMIN");
            userRepository.save(admin);
            log.info("种子管理员已创建: admin / admin123");
        }
        if (!userRepository.findByUsername("guohr").isPresent()) {
            User u = new User();
            u.setUsername("guohr");
            u.setPassword(enc.encode("user123"));
            u.setNickname("小明");
            u.setRole("USER");
            userRepository.save(u);
            log.info("种子消费者已创建: guohr / user123");
        }
        // 默认抽成规则（比例 5%）
        if (!ruleRepository.findByBizTypeAndEffective("NORMAL", 1).isPresent()) {
            CommissionRule r = new CommissionRule();
            r.setMode("RATIO");
            r.setRatio(new BigDecimal("5"));
            r.setBizType("NORMAL");
            r.setIncludeFreight(false);
            r.setEffective(1);
            ruleRepository.save(r);
        }
        // 默认保证金 5000
        if (!configRepository.findById("DEFAULT_DEPOSIT").isPresent()) {
            Config c = new Config();
            c.setConfigKey("DEFAULT_DEPOSIT");
            c.setConfigValue("5000");
            configRepository.save(c);
        }
        // 演示果园店铺
        if (!userRepository.findByUsername("demo").isPresent()) {
            User m = new User();
            m.setUsername("demo");
            m.setPassword(enc.encode("merchant123"));
            m.setNickname("万亩一号果园");
            m.setRole("MERCHANT");
            m = userRepository.save(m);

            Store s = new Store();
            s.setOwnerId(m.getId());
            s.setName("万亩一号果园");
            s.setAddress("山东烟台市福山区果园路 1 号");
            s.setIntro("三十年老果园，红富士苹果、大樱桃产地直发，现摘现发新鲜到家。");
            s.setNotice("满 99 减 5，全场包邮，坏果包赔！");
            s.setStatus("ACTIVE");
            s.setDeposit(new BigDecimal("5000"));
            s = storeRepository.save(s);

            Category c1 = new Category();
            c1.setStoreId(s.getId());
            c1.setName("时令水果");
            c1.setSort(1);
            c1 = categoryRepository.save(c1);
            Category c2 = new Category();
            c2.setStoreId(s.getId());
            c2.setName("礼盒装");
            c2.setSort(2);
            c2 = categoryRepository.save(c2);

            seedProduct(s.getId(), c1.getId(), "烟台红富士苹果 5kg 装", new BigDecimal("39.90"), 500, "脆甜多汁，产地直发，坏果包赔。");
            seedProduct(s.getId(), c1.getId(), "大连美早大樱桃 1kg", new BigDecimal("89.00"), 200, "果肉厚实，甜度 20+，顺丰冷链。");
            seedProduct(s.getId(), c1.getId(), "海南贵妃芒 3kg", new BigDecimal("45.00"), 300, "树上熟贵妃芒，香甜浓郁。");
            seedProduct(s.getId(), c2.getId(), "精品果礼盒（混合装）", new BigDecimal("168.00"), 100, "高端礼盒，送礼有面。");
            log.info("演示果园店铺已创建: demo / merchant123");
        }
    }

    private void seedProduct(Long storeId, Long categoryId, String name, BigDecimal price, int stock, String detail) {
        Product p = new Product();
        p.setStoreId(storeId);
        p.setCategoryId(categoryId);
        p.setName(name);
        p.setPrice(price);
        p.setStock(stock);
        p.setDetail(detail);
        p.setStatus("ON_SHELF");
        productRepository.save(p);
    }
}
