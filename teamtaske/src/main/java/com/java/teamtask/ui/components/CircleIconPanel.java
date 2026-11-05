package com.java.teamtask.ui.components;

import com.java.teamtask.ui.theme.Icons;
import com.java.teamtask.ui.theme.Painters;
import com.java.teamtask.ui.theme.UiScale;

import javax.swing.Icon;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

/** Vòng tròn nền màu chứa icon (dùng cho task card / empty state). */
public final class CircleIconPanel extends JPanel {
    private final Icon icon;
    private final Color background;

    public CircleIconPanel(int size, Color background, String iconName, Color iconColor) {
        this.background = background;
        int diameter = UiScale.px(size);
        int iconSize = Math.max(16, Math.round(diameter * 0.46f));
        this.icon = Icons.icon(iconName, iconSize, iconColor);
        setOpaque(false);
        setPreferredSize(new Dimension(diameter, diameter));
        setMinimumSize(new Dimension(diameter, diameter));
        setMaximumSize(new Dimension(diameter, diameter));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Painters.antialias(g2);
        int d = Math.min(getWidth(), getHeight());
        int x = (getWidth() - d) / 2;
        int y = (getHeight() - d) / 2;
        g2.setColor(background);
        g2.fillOval(x, y, d, d);
        int ix = x + (d - icon.getIconWidth()) / 2;
        int iy = y + (d - icon.getIconHeight()) / 2;
        icon.paintIcon(this, g2, ix, iy);
        g2.dispose();
    }
}
