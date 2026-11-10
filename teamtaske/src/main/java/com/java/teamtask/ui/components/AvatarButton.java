package com.java.teamtask.ui.components;

import com.java.teamtask.ui.theme.Painters;
import com.java.teamtask.ui.theme.Palette;
import com.java.teamtask.ui.theme.UiScale;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/** Avatar tròn có thể bấm, giữ vùng bấm tối thiểu 44px và focus ring bàn phím. */
public final class AvatarButton extends JButton {
    private static final int MIN_BOX = 44;

    private final Avatar renderer;
    private final int boxSize;

    public AvatarButton(String name, String tooltip, int size) {
        renderer = new Avatar(name, Math.max(MIN_BOX, size));
        boxSize = UiScale.px(Math.max(MIN_BOX, size));
        setToolTipText(tooltip);
        getAccessibleContext().setAccessibleName(tooltip);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setRolloverEnabled(true);
        setFocusable(true);
        setBorder(new EmptyBorder(0, 0, 0, 0));
        setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));

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
        return new Dimension(boxSize, boxSize);
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        renderer.setSize(getWidth(), getHeight());
        renderer.paint(g2);
        int edge = Math.min(getWidth(), getHeight());
        if (getModel().isRollover() && isEnabled()) {
            Painters.border(g2, 0, 0, edge, edge, edge / 2, Palette.PRIMARY, 1f);
        }
        if (isFocusOwner()) {
            Painters.focusRing(g2, edge, edge, edge / 2);
        }
        g2.dispose();
    }
}
