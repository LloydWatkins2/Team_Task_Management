package com.java.teamtask.ui.screens;

import com.java.teamtask.ui.Card;
import com.java.teamtask.ui.components.AppButton;
import com.java.teamtask.ui.components.FormField;
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
import java.util.function.Consumer;

/** Hộp thoại tạo/sửa dự án dùng chung cho Dashboard và Project Detail. */
public final class ProjectFormDialog extends JDialog {
  public record Values(String name, String description, LocalDateTime deadline) {}

  private final FormField name = FormField.text("Tên dự án *", "Ví dụ: Website Redesign", "projects");
  private final FormField description =
      FormField.textArea("Mô tả", "Mục tiêu và phạm vi dự án", 88);
  private final FormField deadline = FormField.text("Hạn chót *", "dd/MM/yyyy", "calendar");
  private final Consumer<Values> onSubmit;

  public ProjectFormDialog(
      Window owner,
      String title,
      String initialName,
      String initialDescription,
      LocalDateTime initialDeadline,
      Consumer<Values> onSubmit) {
    super(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
    this.onSubmit = onSubmit == null ? values -> {} : onSubmit;
    setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

    JPanel root = new JPanel(new BorderLayout());
    root.setBackground(Palette.CONTENT);
    root.setBorder(new EmptyBorder(Space.XXL, Space.XXL, Space.XXL, Space.XXL));

    Card card = new Card();
    card.setBorder(new EmptyBorder(Space.X3L, Space.X3L, Space.X3L, Space.X3L));
    root.add(card, BorderLayout.CENTER);

    JPanel body = new JPanel();
    body.setOpaque(false);
    body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

    JLabel heading = new JLabel(title);
    heading.setFont(Typography.h3());
    heading.setForeground(Palette.TEXT);
    heading.setAlignmentX(Component.LEFT_ALIGNMENT);

    name.setValue(initialName == null ? "" : initialName);
    description.setValue(initialDescription == null ? "" : initialDescription);
    deadline.setValue(initialDeadline == null ? "" : Validators.formatDate(initialDeadline.toLocalDate()));

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
    body.add(vgap(Space.XL));
    body.add(name);
    body.add(vgap(Space.XL));
    body.add(description);
    body.add(vgap(Space.XL));
    body.add(deadline);
    body.add(vgap(Space.XXL));
    body.add(actions);

    card.add(body, BorderLayout.CENTER);
    setContentPane(root);
    getRootPane()
        .registerKeyboardAction(
            e -> dispose(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW);
    DialogSizer.fit(this, 540, 600);
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
    } else if (parsed.isBefore(LocalDate.now())) {
      deadline.setError("Hạn chót không được trước ngày hôm nay.");
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

    String descriptionValue = description.getValue().isEmpty() ? "Chưa có mô tả" : description.getValue();
    onSubmit.accept(new Values(name.getValue(), descriptionValue, parsed.atTime(23, 59)));
    dispose();
  }
}
