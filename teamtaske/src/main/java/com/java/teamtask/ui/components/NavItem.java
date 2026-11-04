package com.java.teamtask.ui.components;

import com.java.teamtask.ui.theme.Icons;
import com.java.teamtask.ui.theme.Painters;
import com.java.teamtask.ui.theme.Palette;
import com.java.teamtask.ui.theme.Shadows;
import com.java.teamtask.ui.theme.Space;
import com.java.teamtask.ui.theme.Typography;
import com.java.teamtask.ui.theme.UiScale;

import javax.swing.AbstractAction;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/** Mục menu dạng pill; mục active tô nền ACCENT kèm shadow accent. */
public final class NavItem extends JToggleButton {
    public NavItem(String text, String iconName) {
        super(text);
        setIcon(Icons.icon(iconName, 20, Palette.TEXT));
        setFont(Typography.nav());
        setForeground(Palette.TEXT);
        setHorizontalAlignment(SwingConstants.LEFT);
        setIconTextGap(Space.SM);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(new EmptyBorder(0, Space.LG, 0, Space.LG));

        getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "activate");
        getActionMap().put("activate", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doClick();
            }
        });
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(d.width, UiScale.px(48));
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, UiScale.px(48));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Painters.antialias(g2);
        int w = getWidth();
        int h = getHeight();
        int radius = h / 2;
        boolean active = isSelected();
        boolean hover = getModel().isRollover() && isEnabled();

        if (active) {
            Shadows.paintAccent(g2, new Rectangle(0, 0, w, h), radius);
            Painters.fill(g2, 0, 0, w, h, radius, Palette.ACCENT);
        } else {
            Painters.fill(g2, 0, 0, w, h, radius, hover ? Palette.NAV_HOVER : Palette.CARD);
            Painters.border(g2, 0, 0, w, h, radius, Palette.BORDER, 1f);
        }
        g2.dispose();

        super.paintComponent(g);

        if (isFocusOwner()) {
            Graphics2D focus = (Graphics2D) g.create();
            Painters.antialias(focus);
            Painters.focusRing(focus, w, h, radius);
            focus.dispose();
        }
    }
}
