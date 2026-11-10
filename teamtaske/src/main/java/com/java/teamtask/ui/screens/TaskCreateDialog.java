package com.java.teamtask.ui.screens;

import com.java.teamtask.elements.projectsAssignments;
import com.java.teamtask.elements.users;
import com.java.teamtask.ui.Card;
import com.java.teamtask.ui.components.AppButton;
import com.java.teamtask.ui.components.FormField;
import com.java.teamtask.ui.data.UiMapper;
import com.java.teamtask.ui.data.Validators;
import com.java.teamtask.ui.theme.Palette;
import com.java.teamtask.ui.theme.Space;
import com.java.teamtask.ui.theme.Typography;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/** Hộp thoại tạo công việc trong một dự án, giao việc cho thành viên đã chọn. */
public final class TaskCreateDialog extends JDialog {
  public record Values(
      String name, String description, String status, LocalDateTime deadline, users assignee) {}

  private static final Map<String, String> STATUS_TO_DB =
      Map.of(
          "Cần làm", "TODO",
          "Đang làm", "IN_PROGRESS",
          "Hoàn thành", "DONE",
          "Quá hạn", "OVERDUE");

  private final FormField name =
      FormField.text("Tên công việc *", "Ví dụ: Thiết kế trang Tổng quan", "tasks");
  private final FormField description =
      FormField.textArea("Mô tả", "Mô tả ngắn gọn nội dung công việc", 88);
  private final FormField deadline = FormField.text("Hạn chót *", "dd/MM/yyyy", "calendar");
  private final FormField status =
      FormField.combo("Trạng thái", new String[] {"Cần làm", "Đang làm", "Hoàn thành", "Quá hạn"}, "flag");
  private final FormField assignee;
  private final Map<String, users> assigneeByLabel = new LinkedHashMap<>();
  private final Consumer<Values> onSubmit;

  public TaskCreateDialog(
      Window owner, List<projectsAssignments> members, Consumer<Values> onSubmit) {
    super(owner, "Tạo công việc", Dialog.ModalityType.APPLICATION_MODAL);
    this.onSubmit = onSubmit == null ? values -> {} : onSubmit;
    setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

    String[] options = new String[members == null ? 0 : members.size()];
    if (members != null) {
      for (int i = 0; i < members.size(); i++) {
        users member = members.get(i).getUser();
        String label = label(member);
        options[i] = label;
        assigneeByLabel.put(label, member);
      }
    }
    assignee =
        FormField.combo(
            "Người phụ trách *",
            options.length == 0 ? new String[] {"Chưa có thành viên"} : options,
            "user");

    JPanel root = new JPanel(new BorderLayout());
    root.setBackground(Palette.CONTENT);
    root.setBorder(new EmptyBorder(Space.XXL, Space.XXL, Space.XXL, Space.XXL));

    Card card = new Card();
    card.setBorder(new EmptyBorder(Space.X3L, Space.X3L, Space.X3L, Space.X3L));
    root.add(card, BorderLayout.CENTER);

    JPanel body = new JPanel();
    body.setOpaque(false);
    body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

    JLabel heading = new JLabel("Thông tin công việc");
    heading.setFont(Typography.h3());
    heading.setForeground(Palette.TEXT);
    heading.setAlignmentX(Component.LEFT_ALIGNMENT);

    JLabel hint = new JLabel("Các trường có dấu * là bắt buộc.");
    hint.setFont(Typography.body());
    hint.setForeground(Palette.TEXT_MUTED_STRONG);
    hint.setAlignmentX(Component.LEFT_ALIGNMENT);

    AppButton cancel = new AppButton("Huỷ", AppButton.Variant.OUTLINE);
    cancel.addActionListener(e -> dispose());
    AppButton save = new AppButton("Lưu", AppButton.Variant.PRIMARY).withIcon("check");
    save.addActionListener(e -> submit());

    JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, Space.SM, 0));
    actions.setOpaque(false);
    actions.setAlignmentX(Component.LEFT_ALIGNMENT);
    actions.add(cancel);
    actions.add(save);

    body.add(heading);
    body.add(vgap(Space.XXS));
    body.add(hint);
    body.add(vgap(Space.XL));
    body.add(name);
    body.add(vgap(Space.XL));
    body.add(description);
    body.add(vgap(Space.XL));
    body.add(deadline);
    body.add(vgap(Space.XL));
    body.add(twoColumns(status, assignee));
    body.add(vgap(Space.XXL));
    body.add(actions);

    card.add(body, BorderLayout.CENTER);
    setContentPane(root);
    getRootPane()
        .registerKeyboardAction(
            e -> dispose(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW);
    DialogSizer.fit(this, 560, 660);
    setLocationRelativeTo(owner);
  }

  private JPanel twoColumns(FormField left, FormField right) {
    JPanel row = new JPanel(new java.awt.GridLayout(1, 2, Space.MD, 0));
    row.setOpaque(false);
    row.setAlignmentX(Component.LEFT_ALIGNMENT);
    row.add(left);
    row.add(right);
    return row;
  }

  private static Component vgap(int height) {
    Component gap = Box.createVerticalStrut(height);
    ((JComponent) gap).setAlignmentX(Component.LEFT_ALIGNMENT);
    return gap;
  }

  private static String label(users user) {
    if (user == null) {
      return "Chưa giao";
    }
    String full = UiMapper.displayName(user);
    String username = user.getUsername();
    return username == null || username.isBlank() ? full : full + " (" + username + ")";
  }

  private void submit() {
    name.clearError();
    deadline.clearError();
    assignee.clearError();

    boolean valid = true;
    if (name.getValue().isEmpty()) {
      name.setError(Validators.REQUIRED);
      valid = false;
    }
    LocalDate parsed = Validators.parseDate(deadline.getValue());
    if (deadline.getValue().isEmpty()) {
      deadline.setError(Validators.REQUIRED);
      valid = false;
    } else if (parsed == null) {
      deadline.setError("Hạn chót không hợp lệ (dd/MM/yyyy).");
      valid = false;
    } else if (parsed.isBefore(LocalDate.now())) {
      deadline.setError("Hạn chót không được trước ngày hôm nay.");
      valid = false;
    }

    users selected = assigneeByLabel.get(assignee.getValue());
    if (selected == null) {
      assignee.setError("Vui lòng chọn người phụ trách.");
      valid = false;
    }
    if (!valid) {
      if (name.hasError()) {
        name.focusInput();
      } else if (deadline.hasError()) {
        deadline.focusInput();
      } else {
        assignee.focusInput();
      }
      return;
    }

    String descriptionValue =
        description.getValue().isEmpty() ? "Chưa có mô tả" : description.getValue();
    String dbStatus = STATUS_TO_DB.getOrDefault(status.getValue(), "TODO");
    onSubmit.accept(
        new Values(name.getValue(), descriptionValue, dbStatus, parsed.atTime(23, 59), selected));
    dispose();
  }
}
