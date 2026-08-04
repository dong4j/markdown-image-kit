package info.dong4j.idea.plugin.promotion;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** 验证 Starcat 推广的 macOS 主版本解析边界。 */
class StarcatPromotionTest {
    @Test
    void shouldParseSupportedMacosVersions() {
        assertThat(StarcatPromotion.parseMajorVersion("15.0")).isEqualTo(15);
        assertThat(StarcatPromotion.parseMajorVersion("16.4.1")).isEqualTo(16);
        assertThat(StarcatPromotion.isEligible(true, "15.0")).isTrue();
        assertThat(StarcatPromotion.isEligible(true, "14.7")).isFalse();
        assertThat(StarcatPromotion.isEligible(false, "15.0")).isFalse();
    }

    @Test
    void shouldRejectMissingOrMalformedVersions() {
        assertThat(StarcatPromotion.parseMajorVersion(null)).isZero();
        assertThat(StarcatPromotion.parseMajorVersion("unknown")).isZero();
    }
}
