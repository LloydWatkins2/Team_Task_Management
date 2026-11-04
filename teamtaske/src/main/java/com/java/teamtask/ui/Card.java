package com.java.teamtask.ui;

import com.java.teamtask.ui.theme.Painters;
import com.java.teamtask.ui.theme.Palette;
import com.java.teamtask.ui.theme.Radius;
import com.java.teamtask.ui.theme.Shadows;

import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** Panel trắng bo góc, tuỳ chọn bóng và hiệu ứng hover. */
public class Card extends JPanel {
    private int radius = Radius.MD;
    private boolean shadow;
    private boolean hoverable;
    private boolean hovering;
    private boolean hoverListenerAdded;

    public Card() {
        this(null);
    }

    public Card(LayoutManager layout) {
        super(layout == null ? new java.awt.BorderLayout() : layout);
        setOpaque(false);
    }

    public Card(LayoutManager layout, boolean shadow) {
        this(layout);
        this.shadow = shadow;
    }

    public void setRadius(int radius) {
        this.radius = radius;
        repaint();
    }

    public int getRadius() {
        return radius;
    }

    public void setShadow(boolean shadow) {
        this.shadow = shadow;
        repaint();
    }

    /** Bật bóng đổ nhẹ khi hover (dùng cho task card). */
    public void setHoverable(boolean hoverable) {
        this.hoverable = hoverable;
        if (hoverable && !hoverListenerAdded) {
            hoverListenerAdded = true;
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovering = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovering = false;
                    repaint();
                }
            });
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Painters.antialias(g2);
        Rectangle bounds = new Rectangle(0, 0, getWidth(), getHeight());
        if (hoverable && hovering) {
            Shadows.paintMd(g2, bounds, radius);
        } else if (shadow) {
            Shadows.paintSm(g2, bounds, radius);
        }
        Painters.fill(g2, 0, 0, getWidth(), getHeight(), radius, Palette.CARD);
        g2.dispose();
        super.paintComponent(g);
    }
}
