package com.java.teamtask.ui.screens;

import com.java.teamtask.ui.Card;
import com.java.teamtask.ui.GradientPanel;
import com.java.teamtask.ui.components.AppButton;
import com.java.teamtask.ui.components.Badge;
import com.java.teamtask.ui.components.CircleIconPanel;
import com.java.teamtask.ui.sample.CommentItem;
import com.java.teamtask.ui.sample.TaskItem;
import com.java.teamtask.ui.theme.Icons;
import com.java.teamtask.ui.theme.Palette;
import com.java.teamtask.ui.theme.Space;
import com.java.teamtask.ui.theme.Typography;
import com.java.teamtask.ui.theme.UiScale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;

/** Hộp thoại chi tiết công việc kèm danh sách bình luận. */
public final class TaskDetailDialog extends JDialog {
    public TaskDetailDialog(Window owner, TaskItem task, List<CommentItem> comments, String currentUser) {
        super(owner, "Chi tiết công việc", Dialog.ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        GradientPanel root = new GradientPanel();
        root.setLayout(new BorderLayout());
        root.setBorder(new EmptyBorder(Space.XXL, Space.XXL, Space.XXL, Space.XXL));
        setContentPane(root);

        Card card = new Card(new BorderLayout(0, Space.XL));
        card.setBorder(new EmptyBorder(Space.X3L, Space.X3L, Space.X3L, Space.X3L));
        root.add(card, BorderLayout.CENTER);

        card.add(details(task), BorderLayout.NORTH);
        card.add(new CommentsPanel(currentUser, comments), BorderLayout.CENTER);
        card.add(actions(), BorderLayout.SOUTH);

        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        setSize(new Dimension(UiScale.px(660), UiScale.px(720)));
        setLocationRelativeTo(owner);
    }

    private JPanel details(TaskItem task) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel head = new JPanel(new BorderLayout(Space.MD, 0));
        head.setOpaque(false);
        head.add(new CircleIconPanel(48, task.iconBackground(), task.iconName(), Palette.TEXT),
                BorderLayout.WEST);

        JPanel titleArea = new JPanel();
        titleArea.setOpaque(false);
        titleArea.setLayout(new BoxLayout(titleArea, BoxLayout.Y_AXIS));
        JLabel title = new JLabel(task.title());
        title.setFont(Typography.h3());
        title.setForeground(Palette.TEXT);
        JLabel subtitle = new JLabel(task.subtitle());
        subtitle.setFont(Typography.body());
        subtitle.setForeground(Palette.TEXT_MUTED_STRONG);
        titleArea.add(title);
        titleArea.add(subtitle);
        head.add(titleArea, BorderLayout.CENTER);

        JPanel badge = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        badge.setOpaque(false);
        badge.add(new Badge(task.status().label, task.status().badge));
        head.add(badge, BorderLayout.EAST);

        JPanel meta = new JPanel(new FlowLayout(FlowLayout.LEFT, Space.XL, 0));
        meta.setOpaque(false);
        meta.setBorder(new EmptyBorder(Space.SM, 0, 0, 0));
        meta.add(meta("clock", task.deadline(), Palette.TEXT_MUTED_STRONG));
        meta.add(meta("projects", task.project(), Palette.TEXT_MUTED_STRONG));
        meta.add(meta("user", task.assignee(), Palette.TEXT_MUTED_STRONG));

        JLabel description = new JLabel("<html><div style='width:520px'>"
                + escape(task.description()) + "</div></html>");
        description.setFont(Typography.body());
        description.setForeground(Palette.TEXT_MUTED_STRONG);
        description.setBorder(new EmptyBorder(Space.MD, 0, 0, 0));

        JLabel divider = new JLabel();
        divider.setOpaque(true);
        divider.setBackground(Palette.BORDER);
        divider.setPreferredSize(new Dimension(0, 1));
        divider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        divider.setBorder(BorderFactory.createEmptyBorder());
        divider.setAlignmentX(LEFT_ALIGNMENT);

        JLabel commentTitle = new JLabel("Bình luận");
        commentTitle.setFont(Typography.h3());
        commentTitle.setForeground(Palette.TEXT);
        commentTitle.setBorder(new EmptyBorder(Space.LG, 0, 0, 0));

        panel.add(head);
        panel.add(meta);
        panel.add(description);
        panel.add(Box.createVerticalStrut(Space.LG));
        panel.add(divider);
        panel.add(commentTitle);
        return panel;
    }

    private JLabel meta(String icon, String text, Color color) {
        JLabel label = new JLabel(text, Icons.icon(icon, 18, color), JLabel.LEFT);
        label.setFont(Typography.meta());
        label.setForeground(color);
        label.setIconTextGap(Space.XXS);
        return label;
    }

    private JPanel actions() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT, Space.SM, 0));
        row.setOpaque(false);
        AppButton close = new AppButton("Đóng", AppButton.Variant.OUTLINE);
        close.addActionListener((ActionEvent e) -> dispose());
        row.add(close);
        return row;
    }

    private static String escape(String text) {
        return text == null ? "" : text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
