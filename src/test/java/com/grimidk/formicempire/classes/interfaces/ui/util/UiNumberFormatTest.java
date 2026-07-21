package com.grimidk.formicempire.classes.interfaces.ui.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UiNumberFormatTest {

    @Test
    void belowThresholdShowsPlainInteger() {
        assertEquals("999", UiNumberFormat.format(999));
        assertEquals("0", UiNumberFormat.format(0));
        assertEquals("-42", UiNumberFormat.format(-42));
    }

    @Test
    void atAndAboveThresholdUsesSuffix() {
        assertEquals("1k", UiNumberFormat.format(1000));
        assertEquals("1.5k", UiNumberFormat.format(1542));
        assertEquals("1m", UiNumberFormat.format(1_000_000));
        assertEquals("2.5m", UiNumberFormat.format(2_543_210));
    }

    @Test
    void formatSignedKeepsExplicitPlus() {
        assertEquals("+1.5k", UiNumberFormat.formatSigned(1542));
        assertEquals("-10", UiNumberFormat.formatSigned(-10));
    }

    @Test
    void formatRatioUsesCompactParts() {
        assertEquals("1.5k / 2k", UiNumberFormat.formatRatio(1542, 2000));
    }

    @Test
    void formatNumberAcceptsBoxedIntegerWithoutRecursion() {
        assertEquals("1.5k", UiNumberFormat.format(Integer.valueOf(1542)));
        assertEquals("1m", UiNumberFormat.format(Long.valueOf(1_000_000)));
    }
}
