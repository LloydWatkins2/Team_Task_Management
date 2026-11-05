package com.java.teamtask.ui.components;

import com.java.teamtask.ui.theme.Palette;
import com.java.teamtask.ui.theme.Space;
import com.java.teamtask.ui.theme.Typography;
import com.java.teamtask.ui.theme.UiScale;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Component;

/** Tiêu đề trang: title 32/800, subtitle phụ và vùng hành động bên phải. */
public final class PageHeader extends JPanel {
    private final JLabel titleLabel = new JLabel();
    private final JLabel subtitleLabel = new JLabel();

    public PageHeader(String title, String subtitle, JComponent action) {
        setOpaque(false);
        setLayout(new BorderLayout(Space.XXL, 0));
        setBorder(new EmptyBorder(0, 0, Space.XXL, 0));

        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));

        titleLabel.setText(title);
        titleLabel.setFont(Typography.h1());
        titleLabel.setForeground(Palette.TEXT);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        subtitleLabel.setText(subtitle);
        subtitleLabel.setFont(Typography.body());
        subtitleLabel.setForeground(Palette.TEXT_MUTED_STRONG);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitleLabel.setBorder(new EmptyBorder(Space.XS, 0, 0, 0));

        texts.add(titleLabel);
        texts.add(subtitleLabel);
        add(texts, BorderLayout.CENTER);

        if (action != null) {
            JPanel wrapper = new JPanel(new BorderLayout());
            wrapper.setOpaque(false);
            wrapper.add(action, BorderLayout.EAST);
            wrapper.setBorder(new EmptyBorder(Space.XS, 0, 0, 0));
            add(wrapper, BorderLayout.EAST);
        }
    }

    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    public void setSubtitle(String subtitle) {
        subtitleLabel.setText(subtitle);
    }

    /** Giảm cỡ tiêu đề khi cửa sổ hẹp dưới 768px. */
    public void setCompact(boolean compact) {
        titleLabel.setFont(compact
                ? Typography.of(Typography.Weight.EXTRABOLD, UiScale.font(26))
                : Typography.h1());
        revalidate();
        repaint();
    }
}
