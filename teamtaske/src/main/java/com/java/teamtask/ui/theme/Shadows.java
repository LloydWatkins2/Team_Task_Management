package com.java.teamtask.ui.theme;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/** Bóng đổ mô phỏng box-shadow bằng nhiều lớp alpha. */
public final class Shadows {
    private Shadows() {
    }

    public static void paint(Graphics2D g2, Rectangle bounds, int radius,
                             Color color, int blur, int offsetY) {
        if (blur <= 0 || color == null) {
            return;
        }
        int alpha = Math.max(1, color.getAlpha() / blur);
        for (int i = blur; i >= 1; i--) {
            g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
            int grow = i;
            int arcRadius = radius + grow / 2;
            g2.fill(Painters.roundRect(bounds.x - grow / 2, bounds.y - grow / 2 + offsetY,
                    bounds.width + grow, bounds.height + grow, arcRadius));
        }
    }

    public static void paintShell(Graphics2D g2, Rectangle bounds, int radius) {
        paint(g2, bounds, radius, Palette.SHADOW_SHELL, 60, 18);
    }

    public static void paintSm(Graphics2D g2, Rectangle bounds, int radius) {
        paint(g2, bounds, radius, Palette.SHADOW_SM, 8, 2);
    }

    public static void paintMd(Graphics2D g2, Rectangle bounds, int radius) {
        paint(g2, bounds, radius, Palette.SHADOW_MD, 24, 8);
    }

    public static void paintPrimary(Graphics2D g2, Rectangle bounds, int radius) {
        paint(g2, bounds, radius, Palette.SHADOW_PRIMARY, 20, 8);
    }

    public static void paintAccent(Graphics2D g2, Rectangle bounds, int radius) {
        paint(g2, bounds, radius, Palette.SHADOW_ACCENT, 18, 8);
    }
}
