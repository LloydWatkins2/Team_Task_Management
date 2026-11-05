package com.java.teamtask.ui.theme;

/** Quy đổi kích thước thiết kế (CSS px) sang Swing. */
public final class UiScale {
    /** Hệ số người dùng; tăng nếu muốn toàn bộ UI lớn hơn. */
    public static final float USER = 1.0f;
    public static final float FONT_BOOST_PX = 2f;

    private UiScale() {
    }

    /** Kích thước layout: giữ logical px, Java/FlatLaf tự xử lý HiDPI. */
    public static int px(float cssPx) {
        return Math.round(cssPx * USER);
    }

    /** Font: 1 CSS px = 0.75 pt ở 96 DPI, cộng +2 CSS px boost. */
    public static float font(float cssPx) {
        return (cssPx + FONT_BOOST_PX) * 0.75f * USER;
    }
}
