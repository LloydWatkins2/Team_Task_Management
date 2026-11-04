package com.java.teamtask.ui;

import com.java.teamtask.ui.theme.Painters;
import com.java.teamtask.ui.theme.Palette;
import com.java.teamtask.ui.theme.Radius;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;

/** Khung trắng bo 48px, cắt con theo góc bo. */
public final class ShellPanel extends JPanel {
    public ShellPanel() {
        super(new BorderLayout());
        setOpaque(false);
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Painters.antialias(g2);
        int w = getWidth();
        int h = getHeight();
        Painters.fill(g2, 0, 0, w, h, Radius.XL, Palette.SHELL);
        g2.setClip(new RoundRectangle2D.Float(0, 0, w, h, Radius.XL * 2f, Radius.XL * 2f));
        super.paint(g2);
        g2.dispose();
    }
}
