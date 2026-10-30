package com.example.BuiThiDiemTrinh_TH5;

import java.math.BigDecimal;
import com.example.BuiThiDiemTrinh_TH5.entity.Promotion;
import com.example.BuiThiDiemTrinh_TH5.repository.PromotionRepository;

/** Explicit persisted test data; production has no built-in discount codes. */
final class PromotionFixtures {
    static void seed(PromotionRepository repository) {
        save(repository, "GIAM10", "PERCENT", "10");
        save(repository, "GIAM20", "PERCENT", "20");
        save(repository, "VIP", "PERCENT", "15");
        save(repository, "CAFE50K", "AMOUNT", "50000");
    }
    static Promotion save(PromotionRepository repository, String code, String type, String value) {
        Promotion promotion = new Promotion();
        promotion.setCode(code); promotion.setName(code); promotion.setDiscountType(type);
        promotion.setDiscountValue(new BigDecimal(value)); promotion.setActive(true); promotion.setUsageCount(0);
        return repository.save(promotion);
    }
}
