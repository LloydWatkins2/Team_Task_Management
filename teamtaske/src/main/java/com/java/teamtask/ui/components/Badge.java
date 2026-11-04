package com.java.teamtask.ui.components;

import com.java.teamtask.ui.theme.Painters;
import com.java.teamtask.ui.theme.Palette;
import com.java.teamtask.ui.theme.Space;
import com.java.teamtask.ui.theme.Typography;
import com.java.teamtask.ui.theme.UiScale;

import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

/** Nhãn trạng thái dạng pill. */
public final class Badge extends JLabel {
    public enum Kind { PRIMARY, SUCCESS, DANGER, WARNING, NEUTRAL }

    private final Kind kind;

    public Badge(String text, Kind kind) {
        super(text.toUpperCase(java.util.Locale.ROOT));
        this.kind = kind;
        setFont(Typography.small());
        setForeground(foregroundOf(kind));
        setOpaque(false);
        setBorder(new EmptyBorder(0, Space.SM, 0, Space.SM));
    }

    private static java.awt.Color foregroundOf(Kind kind) {
        return switch (kind) {
            case PRIMARY -> Palette.PRIMARY_DARK;
            case SUCCESS -> Palette.SUCCESS;
            case DANGER -> Palette.DANGER;
            case WARNING -> Palette.WARNING_TEXT;
            case NEUTRAL -> Palette.TEXT_MUTED_STRONG;
        };
    }

    private static java.awt.Color backgroundOf(Kind kind) {
        return switch (kind) {
            case PRIMARY -> Palette.PRIMARY_SOFT;
            case SUCCESS -> Palette.SUCCESS_SOFT;
            case DANGER -> Palette.DANGER_SOFT;
            case WARNING -> Palette.WARNING_SOFT;
            case NEUTRAL -> Palette.NEUTRAL_SOFT;
        };
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(d.width, UiScale.px(28));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Painters.antialias(g2);
        int h = getHeight();
        Painters.fill(g2, 0, 0, getWidth(), h, h / 2, backgroundOf(kind));
        g2.dispose();
        super.paintComponent(g);
    }
}
