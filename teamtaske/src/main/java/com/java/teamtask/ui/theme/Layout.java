package com.java.teamtask.ui.theme;

import java.awt.Insets;

/** Breakpoint và số đo layout. */
public final class Layout {
    public static final int SHELL_MAX_WIDTH = 1280;
    public static final int BP_MD = 1024;
    public static final int BP_SM = 768;

    public static final int SIDEBAR_WIDTH = 270;
    public static final int SIDEBAR_WIDTH_COMPACT = 220;
    public static final int SIDEBAR_WIDTH_STACKED = 200;

    private Layout() {
    }

    public static Insets pagePadding(int width) {
        if (width < BP_SM) {
            return new Insets(0, 0, 0, 0);
        }
        if (width <= BP_MD) {
            return new Insets(20, 20, 20, 20);
        }
        return new Insets(Space.X3L, Space.X3L, Space.X3L, Space.X3L);
    }

    public static Insets mainPadding(int width) {
        if (width < BP_SM) {
            return new Insets(Space.XXL, Space.MD, 40, Space.MD);
        }
        if (width <= BP_MD) {
            return new Insets(Space.X4L, Space.X3L, Space.X4L, Space.X3L);
        }
        return new Insets(Space.X6L, Space.X6L, Space.X7L, Space.X6L);
    }

    public static Insets sidebarPadding(int width) {
        if (width <= BP_MD) {
            return new Insets(Space.X3L, Space.LG, Space.X3L, Space.LG);
        }
        return new Insets(Space.X5L, Space.X3L, Space.X5L, Space.X3L);
    }

    public static int sidebarWidth(int width) {
        if (width < BP_SM) {
            return SIDEBAR_WIDTH_STACKED;
        }
        if (width <= BP_MD) {
            return SIDEBAR_WIDTH_COMPACT;
        }
        return SIDEBAR_WIDTH;
    }

    public static boolean isCompact(int width) {
        return width <= BP_MD;
    }

    public static boolean isStacked(int width) {
        return width < BP_SM;
    }
}
