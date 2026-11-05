package com.java.teamtask.ui.components;

import com.java.teamtask.ui.Card;
import com.java.teamtask.ui.theme.Icons;
import com.java.teamtask.ui.theme.Palette;
import com.java.teamtask.ui.theme.Space;
import com.java.teamtask.ui.theme.Typography;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;

/** Card công việc: icon tròn, tiêu đề, meta trạng thái, badge bên phải. */
public final class TaskCard extends Card {
    public enum Status {
        TODO("Cần làm", Badge.Kind.NEUTRAL, Palette.TEXT_MUTED_STRONG),
        IN_PROGRESS("Đang làm", Badge.Kind.WARNING, Palette.WARNING),
        DONE("Hoàn thành", Badge.Kind.SUCCESS, Palette.SUCCESS),
        OVERDUE("Quá hạn", Badge.Kind.DANGER, Palette.DANGER);

        public final String label;
        public final Badge.Kind badge;
        public final Color color;

        Status(String label, Badge.Kind badge, Color color) {
            this.label = label;
            this.badge = badge;
            this.color = color;
        }
    }

    private final String taskTitle;
    private Runnable onOpen = () -> {
    };

    public TaskCard(String title, String subtitle, Status status, String project,
                    String assignee, String deadline, Color iconBackground, String iconName) {
        super(new BorderLayout(Space.XXL, 0));
        this.taskTitle = title;
        setHoverable(true);
        setBorder(new EmptyBorder(Space.XL, Space.X3L, Space.XL, Space.X3L));

        JPanel main = new JPanel(new BorderLayout(Space.MD, 0));
        main.setOpaque(false);
        main.add(new CircleIconPanel(48, iconBackground, iconName, Palette.TEXT), BorderLayout.WEST);

        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(Typography.cardTitle());
        titleLabel.setForeground(Palette.TEXT);
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(Typography.body());
        subtitleLabel.setForeground(Palette.TEXT_MUTED_STRONG);
        subtitleLabel.setAlignmentX(LEFT_ALIGNMENT);
        subtitleLabel.setBorder(new EmptyBorder(2, 0, 0, 0));

        JPanel meta = new JPanel(new FlowLayout(FlowLayout.LEFT, Space.XL, 0));
        meta.setOpaque(false);
        meta.setAlignmentX(LEFT_ALIGNMENT);
        meta.setBorder(new EmptyBorder(Space.SM, 0, 0, 0));
        meta.add(metaLabel("clock", deadline, status == Status.OVERDUE ? Palette.DANGER : Palette.TEXT_MUTED_STRONG));
        meta.add(metaLabel("projects", project, Palette.TEXT_MUTED_STRONG));
        meta.add(metaLabel("user", assignee, Palette.TEXT_MUTED_STRONG));

        texts.add(titleLabel);
        texts.add(subtitleLabel);
        texts.add(meta);
        main.add(texts, BorderLayout.CENTER);
        add(main, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, Space.SM, 0));
        actions.setOpaque(false);
        actions.add(new Badge(status.label, status.badge));
        IconButton open = new IconButton("chevron-right", "Xem chi tiết");
        open.addActionListener(e -> onOpen.run());
        actions.add(open);
        add(actions, BorderLayout.EAST);
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public void setOnOpen(Runnable onOpen) {
        this.onOpen = onOpen == null ? () -> {
        } : onOpen;
    }

    private JLabel metaLabel(String icon, String text, Color color) {
        JLabel label = new JLabel(text, Icons.icon(icon, 18, color), SwingConstants.LEFT);
        label.setFont(Typography.meta());
        label.setForeground(color);
        label.setIconTextGap(6);
        return label;
    }
}
