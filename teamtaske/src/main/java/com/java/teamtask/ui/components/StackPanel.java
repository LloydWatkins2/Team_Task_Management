package com.java.teamtask.ui.components;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.Component;
import java.awt.Dimension;

/** Cột dọc full width cho danh sách card, luôn bám mép trên. */
public final class StackPanel extends JPanel {
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
}
