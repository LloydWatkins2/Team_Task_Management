package com.java.teamtask.ui.screens;

import com.java.teamtask.elements.users;
import com.java.teamtask.ui.Card;
import com.java.teamtask.ui.GradientPanel;
import com.java.teamtask.ui.components.AppButton;
import com.java.teamtask.ui.components.Avatar;
import com.java.teamtask.ui.components.Badge;
import com.java.teamtask.ui.components.ProgressBar;
import com.java.teamtask.ui.data.MemberInfoView;
import com.java.teamtask.ui.data.MemberTaskView;
import com.java.teamtask.ui.data.UiMapper;
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
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.util.List;

/** Popup thông tin thành viên, tiến độ và danh sách công việc liên quan. */
public final class MemberInfoDialog extends JDialog {
  private final MemberInfoView info;

  public MemberInfoDialog(Window owner, MemberInfoView info) {
    super(owner, "Thông tin thành viên", Dialog.ModalityType.APPLICATION_MODAL);
    this.info = info;
    setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

    GradientPanel root = new GradientPanel();
    root.setLayout(new BorderLayout());
    root.setBorder(new EmptyBorder(Space.XXL, Space.XXL, Space.XXL, Space.XXL));
    setContentPane(root);

    Card card = new Card();
    card.setBorder(new EmptyBorder(Space.X3L, Space.X3L, Space.X3L, Space.X3L));
    root.add(card, BorderLayout.CENTER);

    JPanel body = new JPanel();
    body.setOpaque(false);
    body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
    body.setAlignmentX(Component.LEFT_ALIGNMENT);

    body.add(header());
    body.add(vgap(Space.XL));
    body.add(profile());
    body.add(vgap(Space.XL));
    body.add(progress());
    body.add(vgap(Space.XL));
    body.add(taskList());
    body.add(vgap(Space.XXL));
    body.add(actions());

    card.add(body, BorderLayout.CENTER);

    getRootPane()
        .registerKeyboardAction(
            e -> dispose(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW);

    DialogSizer.fit(this, 560, 700);
    setLocationRelativeTo(owner);
  }

  private JPanel header() {
    users user = info == null ? null : info.user();
    JPanel row = new JPanel(new BorderLayout(Space.MD, 0));
    row.setOpaque(false);
    row.setAlignmentX(Component.LEFT_ALIGNMENT);

    row.add(new Avatar(UiMapper.displayName(user), 48), BorderLayout.WEST);

    JPanel texts = new JPanel();
    texts.setOpaque(false);
    texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
    JLabel name = new JLabel(UiMapper.displayName(user));
    name.setFont(Typography.h3());
    name.setForeground(Palette.TEXT);
    name.setAlignmentX(Component.LEFT_ALIGNMENT);
    name.setToolTipText(name.getText());
    JLabel username =
        new JLabel(user == null || user.getUsername() == null ? "" : "@" + user.getUsername());
    username.setFont(Typography.body());
    username.setForeground(Palette.TEXT_MUTED_STRONG);
    username.setAlignmentX(Component.LEFT_ALIGNMENT);
    texts.add(name);
    texts.add(username);
    row.add(texts, BorderLayout.CENTER);

    JPanel role = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
    role.setOpaque(false);
    String roleValue = info == null ? null : info.role();
    role.add(
        new Badge(
            UiMapper.roleLabel(roleValue),
            "owner".equalsIgnoreCase(roleValue) ? Badge.Kind.PRIMARY : Badge.Kind.NEUTRAL));
    row.add(role, BorderLayout.EAST);
    return row;
  }

  private JPanel profile() {
    users user = info == null ? null : info.user();
    JPanel grid = new JPanel(new GridLayout(0, 2, Space.MD, Space.SM));
    grid.setOpaque(false);
    grid.setAlignmentX(Component.LEFT_ALIGNMENT);
    grid.add(infoRow("Gmail", user == null ? "" : user.getEmail()));
    grid.add(infoRow("Số điện thoại", user == null ? "" : user.getPhone()));
    grid.add(infoRow("Ngày sinh", user == null ? "" : user.getDob()));
    grid.add(
        infoRow(
            "Tham gia dự án",
            info == null ? "" : UiMapper.formatDate(info.assignedAt())));
    return grid;
  }

  private JPanel infoRow(String label, String value) {
    JPanel row = new JPanel();
    row.setOpaque(false);
    row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
    JLabel caption = new JLabel(label);
    caption.setFont(Typography.small());
    caption.setForeground(Palette.TEXT_MUTED_STRONG);
    caption.setAlignmentX(Component.LEFT_ALIGNMENT);
    JLabel content = new JLabel(value == null || value.isBlank() ? "Chưa cập nhật" : value);
    content.setFont(Typography.body());
    content.setForeground(Palette.TEXT);
    content.setAlignmentX(Component.LEFT_ALIGNMENT);
    content.setToolTipText(content.getText());
    row.add(caption);
    row.add(Box.createVerticalStrut(Space.XXS));
    row.add(content);
    return row;
  }

  private JPanel progress() {
    JPanel panel = new JPanel(new BorderLayout(0, Space.XS));
    panel.setOpaque(false);
    panel.setAlignmentX(Component.LEFT_ALIGNMENT);

    JPanel head = new JPanel(new BorderLayout(Space.MD, 0));
    head.setOpaque(false);
    JLabel title = new JLabel("Tiến độ công việc");
    title.setFont(Typography.h3());
    title.setForeground(Palette.TEXT);
    int percent =
        info == null
            ? 0
            : (int) Math.round(Math.max(0.0, Math.min(1.0, info.progress())) * 100);
    JLabel value = new JLabel(percent + "%");
    value.setFont(Typography.label());
    value.setForeground(Palette.PRIMARY_DARK);
    head.add(title, BorderLayout.WEST);
    head.add(value, BorderLayout.EAST);

    String memberName = UiMapper.displayName(info == null ? null : info.user());
    ProgressBar bar = new ProgressBar();
    bar.setName("member.progress");
    bar.setValue(percent);
    bar.setToolTipText("Tiến độ công việc của " + memberName);
    if (bar.getAccessibleContext() != null) {
      bar.getAccessibleContext().setAccessibleName("Tiến độ công việc của " + memberName);
    }

    int done = info == null ? 0 : info.doneTasks();
    int total = info == null ? 0 : info.totalTasks();
    JLabel detail =
        new JLabel(
            total == 0
                ? "Chưa được giao công việc nào"
                : "Đã hoàn thành " + done + "/" + total + " công việc");
    detail.setFont(Typography.small());
    detail.setForeground(Palette.TEXT_MUTED_STRONG);

    panel.add(head, BorderLayout.NORTH);
    panel.add(bar, BorderLayout.CENTER);
    panel.add(detail, BorderLayout.SOUTH);
    return panel;
  }

  private JPanel taskList() {
    JPanel panel = new JPanel(new BorderLayout(0, Space.XS));
    panel.setOpaque(false);
    panel.setAlignmentX(Component.LEFT_ALIGNMENT);

    JLabel title = new JLabel("Công việc liên quan");
    title.setFont(Typography.h3());
    title.setForeground(Palette.TEXT);
    panel.add(title, BorderLayout.NORTH);

    JPanel list = new JPanel();
    list.setOpaque(false);
    list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
    List<MemberTaskView> tasks = info == null ? null : info.tasks();
    if (tasks == null || tasks.isEmpty()) {
      JLabel empty = new JLabel("Chưa được giao công việc nào.");
      empty.setFont(Typography.body());
      empty.setForeground(Palette.TEXT_MUTED_STRONG);
      empty.setAlignmentX(Component.LEFT_ALIGNMENT);
      list.add(empty);
    } else {
      for (MemberTaskView task : tasks) {
        list.add(taskRow(task));
        list.add(Box.createVerticalStrut(Space.XS));
      }
    }

    JScrollPane scroll = new JScrollPane(list);
    scroll.setName("member.tasks");
    scroll.setBorder(BorderFactory.createEmptyBorder());
    scroll.setOpaque(false);
    scroll.getViewport().setOpaque(false);
    scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    scroll.getVerticalScrollBar().setUnitIncrement(UiScale.px(18));
    scroll.setPreferredSize(new Dimension(UiScale.px(440), UiScale.px(170)));
    panel.add(scroll, BorderLayout.CENTER);
    return panel;
  }

  private JPanel taskRow(MemberTaskView task) {
    JPanel row = new JPanel(new BorderLayout(Space.SM, 0));
    row.setOpaque(false);
    row.setAlignmentX(Component.LEFT_ALIGNMENT);

    String taskName = task == null ? "" : safeName(task.name());
    JLabel name = new JLabel(taskName);
    name.setFont(Typography.body());
    name.setForeground(Palette.TEXT);
    name.setToolTipText(taskName);
    row.add(name, BorderLayout.CENTER);

    if (task != null && task.status() != null) {
      row.add(
          new Badge(task.status().label, task.status().badge),
          BorderLayout.EAST);
    }
    return row;
  }

  private JPanel actions() {
    JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
    row.setOpaque(false);
    row.setAlignmentX(Component.LEFT_ALIGNMENT);
    AppButton close = new AppButton("Đóng", AppButton.Variant.OUTLINE);
    close.addActionListener(e -> dispose());
    row.add(close);
    return row;
  }

  private static String safeName(String name) {
    return name == null || name.isBlank() ? "Công việc chưa đặt tên" : name;
  }

  private static Component vgap(int height) {
    Component gap = Box.createVerticalStrut(height);
    ((JComponent) gap).setAlignmentX(Component.LEFT_ALIGNMENT);
    return gap;
  }
}
