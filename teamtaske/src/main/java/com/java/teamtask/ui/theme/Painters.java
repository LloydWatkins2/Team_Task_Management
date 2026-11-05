package com.java.teamtask.ui.theme;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;

/** Helper vẽ các hình cơ bản. */
public final class Painters {
    private Painters() {
    }

    public static void antialias(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    /** Nhận bán kính góc, tự nhân đôi cho Java2D. */
    public static Shape roundRect(int x, int y, int width, int height, int radius) {
        return new RoundRectangle2D.Float(x, y, width, height, radius * 2f, radius * 2f);
    }

    public static void fill(Graphics2D g2, int x, int y, int width, int height, int radius, Color color) {
        g2.setColor(color);
        g2.fill(roundRect(x, y, width, height, radius));
    }

    public static void border(Graphics2D g2, int x, int y, int width, int height,
                              int radius, Color color, float stroke) {
        g2.setColor(color);
        g2.setStroke(new BasicStroke(stroke));
        g2.draw(roundRect(Math.round(x + stroke / 2f), Math.round(y + stroke / 2f),
                Math.round(width - stroke), Math.round(height - stroke), radius));
    }

    public static void focusRing(Graphics2D g2, Rectangle bounds, int radius) {
        g2.setColor(Palette.FOCUS_RING);
        g2.setStroke(new BasicStroke(3f));
        g2.draw(roundRect(1, 1, bounds.width - 3, bounds.height - 3, radius));
    }

    public static void focusRing(Graphics2D g2, int width, int height, int radius) {
        focusRing(g2, new Rectangle(0, 0, width, height), radius);
    }
}
