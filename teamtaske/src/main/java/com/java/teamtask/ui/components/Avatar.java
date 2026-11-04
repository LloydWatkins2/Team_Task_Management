package com.java.teamtask.ui.components;

import com.java.teamtask.ui.theme.Painters;
import com.java.teamtask.ui.theme.Palette;
import com.java.teamtask.ui.theme.Typography;
import com.java.teamtask.ui.theme.UiScale;

import javax.swing.JLabel;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;

/** Avatar tròn với chữ cái đầu của tên. */
public final class Avatar extends JLabel {
    private final String initials;
    private final int size;

    public Avatar(String name) {
        this(name, 48);
    }

    public Avatar(String name, int size) {
        this.size = UiScale.px(size);
        this.initials = initialsOf(name);
        setOpaque(false);
        setHorizontalAlignment(CENTER);
        setVerticalAlignment(CENTER);
        setPreferredSize(new Dimension(this.size, this.size));
        setMinimumSize(new Dimension(this.size, this.size));
        setMaximumSize(new Dimension(this.size, this.size));
    }

    private static String initialsOf(String name) {
        if (name == null || name.isBlank()) {
            return "?";
        }
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, 1).toUpperCase();
        }
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Painters.antialias(g2);
        int d = Math.min(getWidth(), getHeight());
        int x = (getWidth() - d) / 2;
        int y = (getHeight() - d) / 2;
        g2.setColor(Palette.PRIMARY_SOFT);
        g2.fillOval(x, y, d, d);

        Font font = Typography.of(Typography.Weight.BOLD, UiScale.font(size * 0.32f));
        g2.setFont(font);
        g2.setColor(Palette.PRIMARY_DARK);
        FontMetrics metrics = g2.getFontMetrics();
        int tx = (getWidth() - metrics.stringWidth(initials)) / 2;
        int ty = (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
        g2.drawString(initials, tx, ty);
        g2.dispose();
    }
}
