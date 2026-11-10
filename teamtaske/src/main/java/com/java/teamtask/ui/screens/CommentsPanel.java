package com.java.teamtask.ui.screens;

import com.formdev.flatlaf.FlatClientProperties;
import com.java.teamtask.ui.components.AppButton;
import com.java.teamtask.ui.components.Avatar;
import com.java.teamtask.ui.data.CommentView;
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
import java.util.List;
import java.util.function.Consumer;

/** Danh sách bình luận + ô nhập pill; gửi qua callback để lưu DB rồi tải lại. */
public final class CommentsPanel extends JPanel {
  private final JPanel list = new JPanel();
  private final JScrollPane scroll = new JScrollPane(list);
  private final JTextField input = new JTextField();
  private final Consumer<String> onSend;

  public CommentsPanel(List<CommentView> comments, Consumer<String> onSend) {
    this.onSend = onSend == null ? text -> {} : onSend;
    setOpaque(false);
    setLayout(new BorderLayout(0, Space.MD));

    list.setOpaque(false);
    list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
    list.setBorder(new EmptyBorder(0, 0, Space.XS, Space.XS));

    scroll.setOpaque(false);
    scroll.getViewport().setOpaque(false);
    scroll.setBorder(BorderFactory.createEmptyBorder());
    scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    scroll.getVerticalScrollBar().setUnitIncrement(UiScale.px(18));

    add(scroll, BorderLayout.CENTER);
    add(inputRow(), BorderLayout.SOUTH);

    setComments(comments);
  }

  public void setComments(List<CommentView> comments) {
    list.removeAll();
    if (comments == null || comments.isEmpty()) {
      JLabel empty = new JLabel("Chưa có bình luận nào. Hãy bắt đầu cuộc trò chuyện.");
      empty.setFont(Typography.body());
      empty.setForeground(Palette.TEXT_MUTED_STRONG);
      empty.setAlignmentX(Component.LEFT_ALIGNMENT);
      list.add(empty);
    } else {
      for (CommentView comment : comments) {
        list.add(commentRow(comment));
        list.add(Box.createVerticalStrut(Space.MD));
      }
    }
    list.revalidate();
    list.repaint();
    SwingUtilities.invokeLater(this::scrollToBottom);
  }

  private JPanel commentRow(CommentView comment) {
    JPanel row = new JPanel(new BorderLayout(Space.SM, 0));
    row.setOpaque(false);
    row.setAlignmentX(Component.LEFT_ALIGNMENT);
    row.add(new Avatar(comment.author(), 32), BorderLayout.WEST);

    JPanel head = new JPanel(new BorderLayout());
    head.setOpaque(false);
    head.setAlignmentX(Component.LEFT_ALIGNMENT);
    JLabel author = new JLabel(comment.author());
    author.setFont(Typography.label());
    author.setForeground(Palette.TEXT);
    JLabel time = new JLabel(comment.time() == null ? "" : comment.time());
    time.setFont(Typography.small());
    time.setForeground(Palette.TEXT_MUTED_STRONG);
    head.add(author, BorderLayout.WEST);
    head.add(time, BorderLayout.EAST);
    head.setMaximumSize(new Dimension(Integer.MAX_VALUE, head.getPreferredSize().height));

    JLabel content =
        new JLabel(
            "<html><div style='width:400px'>" + escape(comment.content()) + "</div></html>");
    content.setFont(Typography.body());
    content.setForeground(Palette.TEXT_MUTED_STRONG);
    content.setAlignmentX(Component.LEFT_ALIGNMENT);

    // Tên người bình luận luôn cách nội dung đúng Space.XS, không phụ thuộc nội dung dài/ngắn.
    JPanel body = new JPanel();
    body.setOpaque(false);
    body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
    body.setAlignmentX(Component.LEFT_ALIGNMENT);
    body.add(head);
    body.add(Box.createVerticalStrut(Space.XS));
    body.add(content);

    row.add(body, BorderLayout.CENTER);
    row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
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

    JPanel inputHolder = new JPanel(new BorderLayout());
    inputHolder.setOpaque(false);
    inputHolder.add(input, BorderLayout.NORTH);

    AppButton send = new AppButton("Gửi", AppButton.Variant.PRIMARY).withIcon("send");
    send.addActionListener(e -> send());

    row.add(inputHolder, BorderLayout.CENTER);
    row.add(send, BorderLayout.EAST);
    return row;
  }

  private void send() {
    String text = input.getText().trim();
    if (text.isEmpty()) {
      return;
    }
    onSend.accept(text);
    input.setText("");
    input.requestFocusInWindow();
  }

  private void scrollToBottom() {
    scroll.getVerticalScrollBar().setValue(scroll.getVerticalScrollBar().getMaximum());
  }

  private static String escape(String text) {
    return text == null
        ? ""
        : text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
  }
}
