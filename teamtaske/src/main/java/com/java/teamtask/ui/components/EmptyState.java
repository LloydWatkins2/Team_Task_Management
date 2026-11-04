package com.java.teamtask.ui.components;

import com.java.teamtask.ui.Card;
import com.java.teamtask.ui.theme.Palette;
import com.java.teamtask.ui.theme.Space;
import com.java.teamtask.ui.theme.Typography;
import com.java.teamtask.ui.theme.UiScale;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;

/** Trạng thái rỗng: icon tròn lớn, tiêu đề, mô tả và tối đa hai hành động. */
public final class EmptyState extends Card {
    public EmptyState(String title, String description, AppButton... actions) {
        super();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(Space.X3L, Space.X4L, Space.X4L, Space.X4L));
        setMaximumSize(new Dimension(UiScale.px(340), Integer.MAX_VALUE));

        CircleIconPanel icon = new CircleIconPanel(80, Palette.EMPTY_ICON, "tasks", Palette.WARNING);
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(Typography.h2());
        titleLabel.setForeground(Palette.TEXT);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel descriptionLabel = new JLabel("<html><div style='text-align:center;width:260px'>"
                + escape(description) + "</div></html>");
        descriptionLabel.setFont(Typography.body());
        descriptionLabel.setForeground(Palette.TEXT_MUTED_STRONG);
        descriptionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        add(icon);
        add(Box.createVerticalStrut(Space.XL));
        add(titleLabel);
        add(Box.createVerticalStrut(Space.XS));
        add(descriptionLabel);
        add(Box.createVerticalStrut(Space.XL));

        if (actions.length > 0) {
            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, Space.SM, 0));
            buttons.setOpaque(false);
            buttons.setAlignmentX(Component.CENTER_ALIGNMENT);
            for (AppButton action : actions) {
                if (action != null) {
                    buttons.add(action);
                }
            }
            add(buttons);
        }
    }

    private static String escape(String text) {
        return text == null ? "" : text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
