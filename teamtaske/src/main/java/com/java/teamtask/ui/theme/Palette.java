package com.java.teamtask.ui.theme;

import java.awt.Color;

/** Bảng màu Taskia — nguồn duy nhất cho toàn bộ UI. */
public final class Palette {
    private Palette() {
    }

    // Thương hiệu
    public static final Color PRIMARY = new Color(0x7A5CFA);
    public static final Color PRIMARY_DARK = new Color(0x5B3FE0);
    public static final Color PRIMARY_SOFT = new Color(0xE9E4FF);
    public static final Color PRIMARY_GRADIENT_TOP = new Color(0x8B6DFF);
    public static final Color PRIMARY_GRADIENT_BOTTOM = new Color(0x6A4BF0);

    // Nền
    public static final Color PAGE_GRADIENT_START = new Color(0x7B5CFF);
    public static final Color PAGE_GRADIENT_END = new Color(0x4A2BD9);
    public static final Color SHELL = new Color(0xFFFFFF);
    public static final Color CONTENT = new Color(0xECEEF8);
    public static final Color CARD = new Color(0xFFFFFF);
    public static final Color NAV_HOVER = new Color(0xF8F7FF);
    public static final Color SELECT_HOVER = new Color(0xF5F3FF);

    // Chữ
    public static final Color TEXT = new Color(0x14122B);
    public static final Color TEXT_MUTED = new Color(0x8D90A6);
    public static final Color TEXT_MUTED_STRONG = new Color(0x6E7189);
    public static final Color TEXT_PLACEHOLDER = new Color(0xA9ABBD);
    public static final Color ON_PRIMARY = new Color(0xFFFFFF);

    // Viền
    public static final Color BORDER = new Color(0xE7E9F3);

    // Trạng thái
    public static final Color ACCENT = new Color(0xFFD66B);
    public static final Color ACCENT_SHADOW = new Color(255, 193, 59, 115);
    public static final Color DANGER = new Color(0xF04E4E);
    public static final Color DANGER_SOFT = new Color(0xFFF1F1);
    public static final Color SUCCESS = new Color(0x1FA85A);
    public static final Color SUCCESS_SOFT = new Color(0xE3F8EC);
    public static final Color WARNING = new Color(0xF5A524);
    public static final Color WARNING_SOFT = new Color(0xFFF1D1);
    public static final Color WARNING_TEXT = new Color(0xB87500);
    public static final Color NEUTRAL_SOFT = new Color(0xF1F2F8);
    public static final Color EMPTY_ICON = new Color(0xFFE9B8);

    // Icon task
    public static final Color TASK_BLUE = new Color(0xCDEEFF);
    public static final Color TASK_YELLOW = new Color(0xFFE3A3);
    public static final Color TASK_PURPLE = new Color(0xD9D1FF);

    // Focus & shadow
    public static final Color FOCUS_RING = new Color(122, 92, 250, 115);
    public static final Color PRESS_OVERLAY = new Color(0, 0, 0, 18);
    public static final Color SHADOW_SM = new Color(40, 30, 110, 16);
    public static final Color SHADOW_MD = new Color(40, 30, 110, 34);
    public static final Color SHADOW_PRIMARY = new Color(122, 92, 250, 90);
    public static final Color SHADOW_ACCENT = new Color(255, 193, 59, 115);
    public static final Color SHADOW_SHELL = new Color(20, 10, 90, 80);
}
