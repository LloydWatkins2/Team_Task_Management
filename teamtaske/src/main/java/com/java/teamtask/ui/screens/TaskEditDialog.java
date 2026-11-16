package com.java.teamtask.ui.screens;

import com.java.teamtask.elements.tasks;
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
import java.util.Map;
import java.util.function.Consumer;

/** Hộp thoại sửa thông tin công việc; người phụ trách được quản lý riêng bằng giao việc. */
public final class TaskEditDialog extends JDialog {
  public record Values(
      String name, String status, String description, LocalDateTime deadline) {}

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
  private final Consumer<Values> onSubmit;

  public TaskEditDialog(Window owner, tasks task, Consumer<Values> onSubmit) {
    super(owner, "Sửa công việc", Dialog.ModalityType.APPLICATION_MODAL);
    this.onSubmit = onSubmit == null ? values -> {} : onSubmit;
    setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

    if (task != null) {
      name.setValue(task.getTaskName() == null ? "" : task.getTaskName());
      description.setValue(
          task.getTaskDescription() == null ? "" : task.getTaskDescription());
      LocalDateTime currentDeadline = task.getTaskDeadline();
      deadline.setValue(currentDeadline == null ? "" : UiMapper.formatDate(currentDeadline));
      status.setSelected(UiMapper.statusLabel(task.getTaskStatus()));
    }

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
    body.add(status);
    body.add(vgap(Space.XXL));
    body.add(actions);

    card.add(body, BorderLayout.CENTER);
    setContentPane(root);
    getRootPane()
        .registerKeyboardAction(
            e -> dispose(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW);
    DialogSizer.fit(this, 560, 620);
    setLocationRelativeTo(owner);
  }

  private static Component vgap(int height) {
    Component gap = Box.createVerticalStrut(height);
    ((JComponent) gap).setAlignmentX(Component.LEFT_ALIGNMENT);
    return gap;
  }

  private void submit() {
    name.clearError();
    deadline.clearError();

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
    }
    if (!valid) {
      if (name.hasError()) {
        name.focusInput();
      } else {
        deadline.focusInput();
      }
      return;
    }

    String descriptionValue =
        description.getValue().isEmpty() ? "Chưa có mô tả" : description.getValue();
    String dbStatus = STATUS_TO_DB.getOrDefault(status.getValue(), "TODO");
    onSubmit.accept(
        new Values(name.getValue(), dbStatus, descriptionValue, parsed.atTime(23, 59)));
    dispose();
  }
}
