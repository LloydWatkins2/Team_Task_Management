package com.java.teamtask.ui.components;

import com.java.teamtask.ui.theme.Icons;
import com.java.teamtask.ui.theme.Painters;
import com.java.teamtask.ui.theme.Palette;
import com.java.teamtask.ui.theme.Shadows;
import com.java.teamtask.ui.theme.Space;
import com.java.teamtask.ui.theme.Typography;
import com.java.teamtask.ui.theme.UiScale;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/** Nút Taskia: primary gradient, outline, danger-outline. */
public class AppButton extends JButton {
    public enum Variant { PRIMARY, OUTLINE, DANGER_OUTLINE }

    private static final int BODY_HEIGHT = UiScale.px(48);
    private static final int SHADOW_PAD = UiScale.px(18);

    private final Variant variant;
    private boolean block;

    public AppButton(String text, Variant variant) {
        super(text);
        this.variant = variant;
        setFont(Typography.button());
        setForeground(foregroundFor(variant));
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setRolloverEnabled(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setIconTextGap(Space.XS);
        setBorder(new EmptyBorder(0, Space.X3L, 0, Space.X3L));

        getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "activate");
        getActionMap().put("activate", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doClick();
            }
        });
    }

    public AppButton withIcon(String name) {
        Color color = switch (variant) {
            case PRIMARY -> Palette.ON_PRIMARY;
            case DANGER_OUTLINE -> Palette.DANGER;
            case OUTLINE -> Palette.TEXT;
        };
        setIcon(Icons.icon(name, 18, color));
        return this;
    }

    public void setBlock(boolean block) {
        this.block = block;
        revalidate();
    }

    private static Color foregroundFor(Variant variant) {
        return switch (variant) {
            case PRIMARY -> Palette.ON_PRIMARY;
            case DANGER_OUTLINE -> Palette.DANGER;
            case OUTLINE -> Palette.TEXT;
        };
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(d.width, fullHeight());
    }

    @Override
    public Dimension getMaximumSize() {
        if (block) {
            return new Dimension(Integer.MAX_VALUE, fullHeight());
        }
        return getPreferredSize();
    }

    @Override
    public Insets getInsets() {
        Insets insets = super.getInsets();
        int extra = Math.max(0, getHeight() - BODY_HEIGHT);
        return new Insets(insets.top, insets.left, insets.bottom + extra, insets.right);
    }

    private int fullHeight() {
        return BODY_HEIGHT + SHADOW_PAD;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Painters.antialias(g2);
        int w = getWidth();
        int bodyHeight = Math.min(BODY_HEIGHT, getHeight());
        int radius = bodyHeight / 2;
        Rectangle body = new Rectangle(0, 0, w, bodyHeight);
        boolean hover = getModel().isRollover() && isEnabled();

        if (variant == Variant.PRIMARY) {
            if (isEnabled()) {
                Shadows.paintPrimary(g2, body, radius);
                g2.setPaint(new GradientPaint(0, 0, Palette.PRIMARY_GRADIENT_TOP,
                        0, bodyHeight, Palette.PRIMARY_GRADIENT_BOTTOM));
                g2.fill(Painters.roundRect(0, 0, w, bodyHeight, radius));
                if (getModel().isArmed() && getModel().isPressed()) {
                    g2.setColor(Palette.PRESS_OVERLAY);
                    g2.fill(Painters.roundRect(0, 0, w, bodyHeight, radius));
                }
            } else {
                Painters.fill(g2, 0, 0, w, bodyHeight, radius, Palette.PRIMARY_SOFT);
            }
        } else {
            Color background = Palette.CARD;
            if (hover) {
                background = variant == Variant.DANGER_OUTLINE ? Palette.DANGER_SOFT : Palette.NAV_HOVER;
            }
            Painters.fill(g2, 0, 0, w, bodyHeight, radius, background);
            Painters.border(g2, 0, 0, w, bodyHeight, radius,
                    variant == Variant.DANGER_OUTLINE ? Palette.DANGER : Palette.TEXT, 1.5f);
        }
        g2.dispose();

        super.paintComponent(g);

        if (isFocusOwner()) {
            Graphics2D focus = (Graphics2D) g.create();
            Painters.antialias(focus);
            Painters.focusRing(focus, body, radius);
            focus.dispose();
        }
    }
}
