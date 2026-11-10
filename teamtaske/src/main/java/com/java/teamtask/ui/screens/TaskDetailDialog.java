package com.java.teamtask.ui.screens;

import com.java.teamtask.elements.owners;
import com.java.teamtask.elements.projects;
import com.java.teamtask.elements.seeProjects;
import com.java.teamtask.elements.tasks;
import com.java.teamtask.elements.users;
import com.java.teamtask.ui.Card;
import com.java.teamtask.ui.GradientPanel;
import com.java.teamtask.ui.components.AppButton;
import com.java.teamtask.ui.components.Badge;
import com.java.teamtask.ui.components.CircleIconPanel;
import com.java.teamtask.ui.data.CommentView;
import com.java.teamtask.ui.data.DataAccessException;
import com.java.teamtask.ui.data.SessionGateway;
import com.java.teamtask.ui.data.TaskView;
import com.java.teamtask.ui.data.UiMapper;
import com.java.teamtask.ui.theme.Icons;
import com.java.teamtask.ui.theme.Palette;
import com.java.teamtask.ui.theme.Space;
import com.java.teamtask.ui.theme.Typography;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/** Hộp thoại chi tiết công việc: bình luận DB, đổi trạng thái theo quyền. */
public final class TaskDetailDialog extends JDialog {
  private static final String[] STATUS_LABELS = {"Cần làm", "Đang làm", "Hoàn thành", "Quá hạn"};
  private static final int COMMENT_POLL_MS = 1_000;

  private final SessionGateway gateway;
  private final seeProjects role;
  private final users currentUser;
  private final tasks task;
  private final projects project;
  private final Runnable onChanged;
  private CommentsPanel commentsPanel;
  private final Timer commentPollTimer;
  private final JPanel statusHolder = new JPanel(new FlowLayout(FlowLayout.LEFT, Space.SM, 0));
  private int lastCommentId;
  private boolean commentPollInFlight;
  private boolean disposed;

  public TaskDetailDialog(
      Window owner,
      SessionGateway gateway,
      seeProjects role,
      users currentUser,
      tasks task,
      projects project,
      Runnable onChanged) {
    super(owner, "Chi tiết công việc", Dialog.ModalityType.APPLICATION_MODAL);
    this.gateway = gateway;
    this.role = role;
    this.currentUser = currentUser;
    this.task = task;
    this.project = project;
    this.onChanged = onChanged == null ? () -> {} : onChanged;
    setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

    GradientPanel root = new GradientPanel();
    root.setLayout(new BorderLayout());
    root.setBorder(new EmptyBorder(Space.XXL, Space.XXL, Space.XXL, Space.XXL));
    setContentPane(root);

    Card card = new Card(new BorderLayout(0, Space.XL));
    card.setBorder(new EmptyBorder(Space.X3L, 0, Space.X3L, 0));
    root.add(card, BorderLayout.CENTER);

    card.add(headerSection(), BorderLayout.NORTH);
    ArrayList<CommentView> initialComments;
    try {
      initialComments = gateway.loadComments(task);
    } catch (DataAccessException ex) {
      initialComments = new ArrayList<>();
    }
    lastCommentId = maxCommentId(initialComments);
    commentsPanel = new CommentsPanel(initialComments, this::sendComment);
    commentPollTimer = new Timer(COMMENT_POLL_MS, e -> pollComments());
    commentPollTimer.setCoalesce(true);
    card.add(indented(commentsPanel), BorderLayout.CENTER);
    card.add(padded(actions()), BorderLayout.SOUTH);

    getRootPane()
        .registerKeyboardAction(
            e -> dispose(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW);

    DialogSizer.fit(this, 680, 740);
    setLocationRelativeTo(owner);
  }

  private JPanel headerSection() {
    JPanel section = new JPanel();
    section.setOpaque(false);
    section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
    section.setAlignmentX(Component.LEFT_ALIGNMENT);

    section.add(padded(details()));
    section.add(Box.createVerticalStrut(Space.LG));
    section.add(divider());

    JPanel titleRow = new JPanel(new BorderLayout());
    titleRow.setOpaque(false);
    titleRow.setAlignmentX(Component.LEFT_ALIGNMENT);
    titleRow.setBorder(new EmptyBorder(Space.LG, Space.X3L, 0, Space.X3L));
    JLabel commentTitle = new JLabel("Bình luận");
    commentTitle.setFont(Typography.h3());
    commentTitle.setForeground(Palette.TEXT);
    titleRow.add(commentTitle, BorderLayout.WEST);
    section.add(titleRow);
    return section;
  }

  private JPanel details() {
    TaskView view = UiMapper.taskView(task, project);
    JPanel panel = new JPanel();
    panel.setOpaque(false);
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setAlignmentX(Component.LEFT_ALIGNMENT);

    JPanel head = new JPanel(new BorderLayout(Space.MD, 0));
    head.setOpaque(false);
    head.setAlignmentX(Component.LEFT_ALIGNMENT);
    head.add(
        new CircleIconPanel(48, view.iconBackground(), view.iconName(), Palette.TEXT),
        BorderLayout.WEST);

    JPanel titleArea = new JPanel();
    titleArea.setOpaque(false);
    titleArea.setLayout(new BoxLayout(titleArea, BoxLayout.Y_AXIS));
    JLabel title = new JLabel(view.title());
    title.setFont(Typography.h3());
    title.setForeground(Palette.TEXT);
    JLabel subtitle = new JLabel(view.subtitle());
    subtitle.setFont(Typography.body());
    subtitle.setForeground(Palette.TEXT_MUTED_STRONG);
    titleArea.add(title);
    titleArea.add(subtitle);
    head.add(titleArea, BorderLayout.CENTER);

    statusHolder.setOpaque(false);
    head.add(statusHolder, BorderLayout.EAST);
    renderStatus();

    JPanel meta = new JPanel(new FlowLayout(FlowLayout.LEFT, Space.XL, 0));
    meta.setOpaque(false);
    meta.setAlignmentX(Component.LEFT_ALIGNMENT);
    meta.setBorder(new EmptyBorder(Space.SM, 0, 0, 0));
    meta.add(meta("clock", view.deadline(), Palette.TEXT_MUTED_STRONG));
    meta.add(meta("projects", view.projectName(), Palette.TEXT_MUTED_STRONG));
    meta.add(meta("user", view.assignee(), Palette.TEXT_MUTED_STRONG));

    JLabel description =
        new JLabel(
            "<html><div style='width:540px'>" + escape(view.description()) + "</div></html>");
    description.setFont(Typography.body());
    description.setForeground(Palette.TEXT_MUTED_STRONG);
    description.setAlignmentX(Component.LEFT_ALIGNMENT);
    description.setBorder(new EmptyBorder(Space.MD, 0, 0, 0));

    JPanel statusRow = new JPanel(new FlowLayout(FlowLayout.LEFT, Space.SM, 0));
    statusRow.setOpaque(false);
    statusRow.setAlignmentX(Component.LEFT_ALIGNMENT);
    statusRow.setBorder(new EmptyBorder(Space.MD, 0, 0, 0));
    JLabel statusLabel = new JLabel("Trạng thái");
    statusLabel.setFont(Typography.label());
    statusLabel.setForeground(Palette.TEXT);
    JComboBox<String> statusCombo = new JComboBox<>(STATUS_LABELS);
    statusCombo.setFont(Typography.meta());
    statusCombo.setSelectedItem(UiMapper.statusLabel(task.getTaskStatus()));
    statusCombo.setEnabled(canEditStatus());
    statusCombo.addActionListener(
        e -> {
          String selected = (String) statusCombo.getSelectedItem();
          if (selected == null) {
            return;
          }
          String next = toDbStatus(selected);
          if (next.equalsIgnoreCase(task.getTaskStatus())) {
            return;
          }
          try {
            if (gateway.updateTaskStatus(role, task, next)) {
              renderStatus();
              onChanged.run();
            }
          } catch (DataAccessException ex) {
            showError(ex.getMessage());
          }
        });
    statusRow.add(statusLabel);
    statusRow.add(statusCombo);

    panel.add(head);
    panel.add(meta);
    panel.add(description);
    panel.add(statusRow);
    return panel;
  }

  /** Đệm thêm 1 tab (32px) bên trái so với dòng tiêu đề. */
  private static JComponent indented(JComponent content) {
    JPanel wrap = new JPanel(new BorderLayout());
    wrap.setOpaque(false);
    wrap.setBorder(new EmptyBorder(0, Space.X3L + Space.X4L, 0, Space.X3L));
    wrap.add(content, BorderLayout.CENTER);
    return wrap;
  }

  /** Đệm lề ngang theo padding chuẩn của card. */
  private static JComponent padded(JComponent content) {
    JPanel wrap = new JPanel(new BorderLayout());
    wrap.setOpaque(false);
    wrap.setBorder(new EmptyBorder(0, Space.X3L, 0, Space.X3L));
    wrap.setAlignmentX(Component.LEFT_ALIGNMENT);
    wrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
    wrap.add(content, BorderLayout.CENTER);
    return wrap;
  }

  /** Đường kẻ ngang kéo hết chiều rộng khung. */
  private static JPanel divider() {
    JPanel divider = new JPanel();
    divider.setOpaque(true);
    divider.setBackground(Palette.BORDER);
    divider.setAlignmentX(Component.LEFT_ALIGNMENT);
    divider.setPreferredSize(new Dimension(Integer.MAX_VALUE, 1));
    divider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
    divider.setMinimumSize(new Dimension(0, 1));
    return divider;
  }

  private void renderStatus() {
    statusHolder.removeAll();
    TaskView view = UiMapper.taskView(task, project);
    statusHolder.add(new Badge(view.status().label, view.status().badge));
    statusHolder.revalidate();
    statusHolder.repaint();
  }

  private boolean canEditStatus() {
    if (role instanceof owners) {
      return true;
    }
    return currentUser != null
        && task.getAssginee() != null
        && task.getAssginee().userId == currentUser.userId;
  }

  private static String toDbStatus(String label) {
    return switch (label) {
      case "Đang làm" -> "IN_PROGRESS";
      case "Hoàn thành" -> "DONE";
      case "Quá hạn" -> "OVERDUE";
      default -> "TODO";
    };
  }

  private JLabel meta(String icon, String text, Color color) {
    JLabel label = new JLabel(text, Icons.icon(icon, 18, color), JLabel.LEFT);
    label.setFont(Typography.meta());
    label.setForeground(color);
    label.setIconTextGap(Space.XXS);
    return label;
  }

  private JPanel actions() {
    JPanel row = new JPanel(new BorderLayout());
    row.setOpaque(false);

    JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, Space.SM, 0));
    left.setOpaque(false);
    if (role instanceof owners) {
      AppButton delete =
          new AppButton("Xoá công việc", AppButton.Variant.DANGER_OUTLINE).withIcon("trash");
      delete.addActionListener(e -> deleteTask());
      left.add(delete);
    }

    JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, Space.SM, 0));
    right.setOpaque(false);
    AppButton close = new AppButton("Đóng", AppButton.Variant.OUTLINE);
    close.addActionListener(e -> dispose());
    right.add(close);

    row.add(left, BorderLayout.WEST);
    row.add(right, BorderLayout.EAST);
    return row;
  }

  private boolean sendComment(String text) {
    try {
      if (gateway.addComment(role, task, text)) {
        pollComments();
        onChanged.run();
        return true;
      }
    } catch (DataAccessException ex) {
      showError(ex.getMessage());
    }
    return false;
  }

  /** Gọi một vòng poll ngay; package-private để test không phải chờ timer. */
  void pollComments() {
    if (disposed || commentPollInFlight || !isShowing()) {
      return;
    }
    commentPollInFlight = true;
    new SwingWorker<ArrayList<CommentView>, Void>() {
      @Override
      protected ArrayList<CommentView> doInBackground() {
        return gateway.loadCommentsAfter(task, lastCommentId);
      }

      @Override
      protected void done() {
        try {
          ArrayList<CommentView> fresh = get();
          if (!disposed && commentsPanel != null) {
            commentsPanel.appendComments(fresh);
            lastCommentId = Math.max(lastCommentId, maxCommentId(fresh));
          }
        } catch (InterruptedException ex) {
          Thread.currentThread().interrupt();
        } catch (java.util.concurrent.ExecutionException ex) {
          // Auto refresh im lặng; lỗi gửi bình luận đã được xử lý riêng.
        } finally {
          commentPollInFlight = false;
        }
      }
    }.execute();
  }

  private static int maxCommentId(List<CommentView> comments) {
    int max = 0;
    if (comments != null) {
      for (CommentView comment : comments) {
        if (comment != null) {
          max = Math.max(max, comment.id());
        }
      }
    }
    return max;
  }

  @Override
  public void setVisible(boolean visible) {
    if (visible) {
      disposed = false;
      commentPollTimer.start();
      super.setVisible(true);
      if (isModal()) {
        // Modal setVisible chỉ trả về khi dialog đã đóng.
        commentPollTimer.stop();
      }
    } else {
      super.setVisible(false);
      commentPollTimer.stop();
    }
  }

  @Override
  public void dispose() {
    disposed = true;
    if (commentPollTimer != null) {
      commentPollTimer.stop();
    }
    super.dispose();
  }

  private void deleteTask() {
    int choice =
        JOptionPane.showConfirmDialog(
            this,
            "Bạn có chắc muốn xoá công việc này? Bình luận liên quan cũng sẽ bị xoá.",
            "Xoá công việc",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
    if (choice != JOptionPane.YES_OPTION) {
      return;
    }
    try {
      if (gateway.deleteTask(project, task)) {
        onChanged.run();
        dispose();
      } else {
        showError("Không thể xoá công việc.");
      }
    } catch (DataAccessException ex) {
      showError(ex.getMessage());
    }
  }

  private void showError(String message) {
    JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
  }

  private static String escape(String text) {
    return text == null
        ? ""
        : text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
  }
}
