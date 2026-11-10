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
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/** Danh sách bình luận + ô nhập pill; gửi qua callback để lưu DB. */
public final class CommentsPanel extends JPanel {
  private final JPanel list = new JPanel();
  private final JScrollPane scroll = new JScrollPane(list);
  private final JTextField input = new JTextField();
  private final Function<String, Boolean> onSend;
  private final ArrayList<CommentView> current = new ArrayList<>();
  private final List<JLabel> contentLabels = new ArrayList<>();

  public CommentsPanel(List<CommentView> comments, Function<String, Boolean> onSend) {
    this.onSend = onSend == null ? text -> true : onSend;
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
    scroll.getViewport().addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        updateContentWidths();
      }
    });

    add(scroll, BorderLayout.CENTER);
    add(inputRow(), BorderLayout.SOUTH);

    setComments(comments);
  }

  public void setComments(List<CommentView> comments) {
    current.clear();
    contentLabels.clear();
    list.removeAll();
    if (comments == null || comments.isEmpty()) {
      showEmptyState();
    } else {
      for (CommentView comment : comments) {
        current.add(comment);
        list.add(commentRow(comment));
        list.add(Box.createVerticalStrut(Space.MD));
      }
    }
    list.revalidate();
    list.repaint();
    SwingUtilities.invokeLater(this::scrollToBottom);
  }

  /** Thêm bình luận mới mà không dựng lại toàn bộ danh sách. */
  public void appendComments(List<CommentView> comments) {
    if (comments == null || comments.isEmpty()) {
      return;
    }
    ArrayList<CommentView> incoming = new ArrayList<>();
    for (CommentView comment : comments) {
      if (comment != null && !contains(comment)) {
        incoming.add(comment);
      }
    }
    if (incoming.isEmpty()) {
      return;
    }

    boolean nearBottom = isNearBottom();
    if (current.isEmpty()) {
      list.removeAll();
    }
    for (CommentView comment : incoming) {
      current.add(comment);
      list.add(commentRow(comment));
      list.add(Box.createVerticalStrut(Space.MD));
    }
    list.revalidate();
    list.repaint();
    if (nearBottom) {
      SwingUtilities.invokeLater(this::scrollToBottom);
    }
  }

  private void showEmptyState() {
    JLabel empty = new JLabel("Chưa có bình luận nào. Hãy bắt đầu cuộc trò chuyện.");
    empty.setFont(Typography.body());
    empty.setForeground(Palette.TEXT_MUTED_STRONG);
    empty.setAlignmentX(Component.LEFT_ALIGNMENT);
    list.add(empty);
  }

  private boolean contains(CommentView next) {
    if (next.id() > 0) {
      for (CommentView existing : current) {
        if (existing.id() == next.id()) {
          return true;
        }
      }
      return false;
    }
    return current.contains(next);
  }

  private boolean isNearBottom() {
    JScrollBar bar = scroll.getVerticalScrollBar();
    return bar.getMaximum() - (bar.getValue() + bar.getVisibleAmount()) <= UiScale.px(40);
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

    JLabel content = new JLabel();
    content.putClientProperty("comment", comment.content());
    content.setFont(Typography.body());
    content.setForeground(Palette.TEXT);
    content.setAlignmentX(Component.LEFT_ALIGNMENT);
    contentLabels.add(content);
    updateContentWidth(content);

    // Tên người bình luận luôn cách nội dung đúng Space.XS, không phụ thuộc nội dung dài/ngắn.
    JPanel body = new JPanel();
    body.setOpaque(false);
    body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
    body.setAlignmentX(Component.LEFT_ALIGNMENT);
    body.add(head);
    body.add(Box.createVerticalStrut(Space.XS));
    body.add(content);

    row.add(body, BorderLayout.CENTER);
    return row;
  }

  private void updateContentWidths() {
    for (JLabel label : contentLabels) {
      updateContentWidth(label);
    }
    list.revalidate();
    list.repaint();
  }

  private void updateContentWidth(JLabel label) {
    if (label == null) {
      return;
    }
    Object raw = label.getClientProperty("comment");
    String text = raw == null ? "" : escape(raw.toString());
    label.setText("<html><div style='width:" + commentWidth() + "px'>" + text + "</div></html>");
  }

  private int commentWidth() {
    int viewport = scroll.getViewport().getWidth();
    if (viewport <= 0) {
      viewport = UiScale.px(400);
    }
    int width = viewport - UiScale.px(32) - Space.SM - Space.XS;
    return Math.max(UiScale.px(180), width);
  }

  private JPanel inputRow() {
    JPanel row = new JPanel(new BorderLayout(Space.XS, 0));
    row.setOpaque(false);

    input.setFont(Typography.body());
    input.setPreferredSize(new Dimension(0, UiScale.px(48)));
    input.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Viết bình luận...");
    input.putClientProperty(FlatClientProperties.STYLE, "arc: 999; focusWidth: 3");
    input.setMargin(new Insets(0, Space.LG, 0, Space.LG));
    input.addActionListener(e -> send());

    JPanel inputHolder = new JPanel(new BorderLayout());
    inputHolder.setOpaque(false);
    inputHolder.add(input, BorderLayout.NORTH);

    AppButton send = new AppButton("Gửi", AppButton.Variant.PRIMARY).withIcon("send");
    send.addActionListener(e -> send());

    JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, Space.XS, 0));
    right.setOpaque(false);
    right.add(send);

    row.add(inputHolder, BorderLayout.CENTER);
    row.add(right, BorderLayout.EAST);
    return row;
  }

  private void send() {
    String text = input.getText().trim();
    if (text.isEmpty()) {
      return;
    }
    if (Boolean.TRUE.equals(onSend.apply(text))) {
      input.setText("");
    }
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
