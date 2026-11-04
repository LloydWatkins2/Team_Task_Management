package com.java.teamtask.ui;

import com.java.teamtask.ui.theme.Painters;
import com.java.teamtask.ui.theme.Palette;

import javax.swing.JPanel;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;

/** Nền gradient tím 135 độ của toàn cửa sổ. */
public class GradientPanel extends JPanel {
    public GradientPanel() {
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Painters.antialias(g2);
        int w = Math.max(1, getWidth());
        int h = Math.max(1, getHeight());
        g2.setPaint(new GradientPaint(0, 0, Palette.PAGE_GRADIENT_START,
                w, h, Palette.PAGE_GRADIENT_END));
        g2.fillRect(0, 0, w, h);
        g2.dispose();
    }
}
