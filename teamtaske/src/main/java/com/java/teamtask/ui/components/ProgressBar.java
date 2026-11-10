package com.java.teamtask.ui.components;

import com.java.teamtask.ui.theme.Painters;
import com.java.teamtask.ui.theme.Palette;
import com.java.teamtask.ui.theme.UiScale;

import javax.swing.JComponent;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

/** Thanh tiến độ bo tròn, giá trị từ 0 đến 100. */
public final class ProgressBar extends JComponent {
    private int value;

    public ProgressBar() {
        setOpaque(false);
        setPreferredSize(new Dimension(0, UiScale.px(10)));
        setMinimumSize(new Dimension(0, UiScale.px(10)));
        setToolTipText("Tiến độ dự án");
        if (getAccessibleContext() != null) {
            getAccessibleContext().setAccessibleName("Tiến độ dự án");
        }
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        int next = Math.max(0, Math.min(100, value));
        if (next == this.value) {
            return;
        }
        this.value = next;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Painters.antialias(g2);
        int width = getWidth();
        int height = getHeight();
        int radius = height / 2;
        Painters.fill(g2, 0, 0, width, height, radius, Palette.PRIMARY_SOFT);
        if (value > 0 && width > 0) {
            int fillWidth =
                    value == 100
                            ? width
                            : Math.min(width, Math.max(height, Math.round(width * value / 100f)));
            Painters.fill(g2, 0, 0, fillWidth, height, radius, Palette.PRIMARY);
        }
        g2.dispose();
    }
}
