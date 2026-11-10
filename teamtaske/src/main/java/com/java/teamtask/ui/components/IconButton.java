package com.java.teamtask.ui.components;

import com.java.teamtask.ui.theme.Icons;
import com.java.teamtask.ui.theme.Painters;
import com.java.teamtask.ui.theme.Palette;
import com.java.teamtask.ui.theme.Typography;
import com.java.teamtask.ui.theme.UiScale;

import javax.swing.JButton;
import javax.swing.border.EmptyBorder;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

/** Nút icon tròn 48px (hoặc ghost). */
public final class IconButton extends JButton {
    private final boolean ghost;
    private final int boxSize;

    public IconButton(String iconName, String tooltip) {
        this(iconName, tooltip, 48, false);
    }

    /**
     * @param size  cạnh vùng bấm (logical px), nên ≥ 44
     * @param ghost true = không nền/viền, chỉ hiện nền khi hover
     */
    public IconButton(String iconName, String tooltip, int size, boolean ghost) {
        super(Icons.icon(iconName, 22, ghost ? Palette.TEXT_MUTED_STRONG : Palette.TEXT));
        this.boxSize = UiScale.px(size);
        this.ghost = ghost;
        setToolTipText(tooltip);
        getAccessibleContext().setAccessibleName(tooltip);
        setFont(Typography.button());
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setRolloverEnabled(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(new EmptyBorder(0, 0, 0, 0));
        setHorizontalAlignment(CENTER);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(boxSize, boxSize);
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Painters.antialias(g2);
        int edge = Math.min(getWidth(), getHeight());
        int x = (getWidth() - edge) / 2;
        int y = (getHeight() - edge) / 2;
        boolean hover = getModel().isRollover() && isEnabled();
        if (ghost) {
            if (hover) {
                Painters.fill(g2, x, y, edge, edge, edge / 2, Palette.NAV_HOVER);
            }
        } else if (hover) {
            Painters.fill(g2, x, y, edge, edge, edge / 2, Palette.PRIMARY_SOFT);
        } else {
            Painters.fill(g2, x, y, edge, edge, edge / 2, Palette.CARD);
            Painters.border(g2, x, y, edge, edge, edge / 2, Palette.BORDER, 1f);
        }
        g2.dispose();

        super.paintComponent(g);

        if (isFocusOwner()) {
            Graphics2D focus = (Graphics2D) g.create();
            Painters.antialias(focus);
            Painters.focusRing(focus, edge, edge, edge / 2);
            focus.dispose();
        }
    }
}
