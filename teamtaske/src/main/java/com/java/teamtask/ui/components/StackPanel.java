package com.java.teamtask.ui.components;

import com.java.teamtask.ui.theme.UiScale;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.Scrollable;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;

/** Cột dọc full width cho danh sách card, luôn bám mép trên. */
public final class StackPanel extends JPanel implements Scrollable {
    public StackPanel() {
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    public void addRow(Component component, int gapBottom) {
        int height = component.getPreferredSize().height;
        component.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        if (component instanceof JComponent jComponent) {
            jComponent.setAlignmentX(Component.LEFT_ALIGNMENT);
        }
        add(component);
        add(Box.createVerticalStrut(gapBottom));
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(
            Rectangle visibleRect, int orientation, int direction) {
        return UiScale.px(18);
    }

    @Override
    public int getScrollableBlockIncrement(
            Rectangle visibleRect, int orientation, int direction) {
        return Math.max(visibleRect.height - UiScale.px(40), UiScale.px(40));
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }
}
