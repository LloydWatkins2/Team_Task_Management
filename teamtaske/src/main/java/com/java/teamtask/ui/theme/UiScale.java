package com.java.teamtask.ui.theme;

/** Quy đổi kích thước thiết kế (CSS px) sang Swing. */
public final class UiScale {
    /** Hệ số người dùng; tăng nếu muốn toàn bộ UI lớn hơn. */
    public static final float USER = 1.0f;

    private UiScale() {
    }

    /** Kích thước layout: giữ logical px, Java/FlatLaf tự xử lý HiDPI. */
    public static int px(float cssPx) {
        return Math.round(cssPx * USER);
    }

    /** Font: 1 CSS px = 0.75 pt ở 96 DPI. */
    public static float font(float cssPx) {
        return cssPx * 0.75f * USER;
    }
}
