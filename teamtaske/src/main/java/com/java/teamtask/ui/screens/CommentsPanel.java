package com.java.teamtask.ui.screens;

import com.formdev.flatlaf.FlatClientProperties;
import com.java.teamtask.ui.components.Avatar;
import com.java.teamtask.ui.components.IconButton;
import com.java.teamtask.ui.sample.CommentItem;
import com.java.teamtask.ui.theme.Palette;
import com.java.teamtask.ui.theme.Space;
import com.java.teamtask.ui.theme.Typography;
import com.java.teamtask.ui.theme.UiScale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Danh sách bình luận + ô nhập pill, tự cuộn xuống bình luận mới. */
public final class CommentsPanel extends JPanel {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    private final String currentUser;
    private final JPanel list = new JPanel();
    private final JScrollPane scroll = new JScrollPane(list);
    private final JTextField input = new JTextField();

    public CommentsPanel(String currentUser, List<CommentItem> comments) {
        this.currentUser = currentUser;
        setOpaque(false);
        setLayout(new BorderLayout(0, Space.MD));

        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBorder(new EmptyBorder(0, 0, Space.XS, Space.XS));
        for (CommentItem comment : comments) {
            list.add(commentRow(comment));
            list.add(Box.createVerticalStrut(Space.MD));
        }

        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(UiScale.px(18));

        add(scroll, BorderLayout.CENTER);
        add(inputRow(), BorderLayout.SOUTH);

        SwingUtilities.invokeLater(this::scrollToBottom);
    }

    private JPanel commentRow(CommentItem comment) {
        JPanel row = new JPanel(new BorderLayout(Space.SM, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        row.add(new Avatar(comment.author(), 32), BorderLayout.WEST);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        JLabel author = new JLabel(comment.author());
        author.setFont(Typography.label());
        author.setForeground(Palette.TEXT);
        JLabel time = new JLabel(comment.time());
        time.setFont(Typography.small());
        time.setForeground(Palette.TEXT_MUTED_STRONG);
        head.add(author, BorderLayout.WEST);
        head.add(time, BorderLayout.EAST);

        JLabel content = new JLabel("<html><div style='width:400px'>"
                + escape(comment.content()) + "</div></html>");
        content.setFont(Typography.body());
        content.setForeground(Palette.TEXT_MUTED_STRONG);
        content.setBorder(new EmptyBorder(Space.XXS, 0, 0, 0));

        body.add(head);
        body.add(content);
        row.add(body, BorderLayout.CENTER);
        return row;
    }

    private JPanel inputRow() {
        JPanel row = new JPanel(new BorderLayout(Space.XS, 0));
        row.setOpaque(false);

        input.setFont(Typography.body());
        input.setPreferredSize(new Dimension(0, UiScale.px(48)));
        input.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Viết bình luận...");
        input.putClientProperty(FlatClientProperties.STYLE, "arc: 999; focusWidth: 3");
        input.setMargin(new java.awt.Insets(0, 20, 0, 20));
        input.addActionListener(e -> send());

        IconButton send = new IconButton("send", "Gửi bình luận");
        send.addActionListener(e -> send());

        row.add(input, BorderLayout.CENTER);
        row.add(send, BorderLayout.EAST);
        return row;
    }

    private void send() {
        String text = input.getText().trim();
        if (text.isEmpty()) {
            return;
        }
        CommentItem comment = new CommentItem(currentUser,
                LocalTime.now().format(TIME), text);
        list.add(commentRow(comment));
        list.add(Box.createVerticalStrut(Space.MD));
        input.setText("");
        list.revalidate();
        list.repaint();
        SwingUtilities.invokeLater(this::scrollToBottom);
        input.requestFocusInWindow();
    }

    private void scrollToBottom() {
        scroll.getVerticalScrollBar().setValue(scroll.getVerticalScrollBar().getMaximum());
    }

    private static String escape(String text) {
        return text == null ? "" : text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
