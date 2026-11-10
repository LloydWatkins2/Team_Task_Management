package com.java.teamtask.ui.screens;

import com.java.teamtask.elements.users;
import com.java.teamtask.ui.Card;
import com.java.teamtask.ui.GradientPanel;
import com.java.teamtask.ui.components.AppButton;
import com.java.teamtask.ui.components.CircleIconPanel;
import com.java.teamtask.ui.components.FormField;
import com.java.teamtask.ui.data.DataAccessException;
import com.java.teamtask.ui.data.Registration;
import com.java.teamtask.ui.data.SessionGateway;
import com.java.teamtask.ui.data.Validators;
import com.java.teamtask.ui.theme.Icons;
import com.java.teamtask.ui.theme.Palette;
import com.java.teamtask.ui.theme.Space;
import com.java.teamtask.ui.theme.Typography;
import com.java.teamtask.ui.theme.UiScale;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.List;
import java.util.function.Consumer;

/** Màn hình đăng ký: gửi đủ 7 trường của system.register và tự đăng nhập khi thành công. */
public final class RegisterScreen extends GradientPanel {
  private final FormField lname = FormField.text("Họ", "Nhập họ", "user");
  private final FormField fname = FormField.text("Tên", "Nhập tên", "user");
  private final FormField username = FormField.text("Tên đăng nhập", "Nhập tên đăng nhập", "user");
  private final FormField password = FormField.password("Mật khẩu", "Tối thiểu 6 ký tự", "lock");
  private final FormField gmail = FormField.text("Gmail", "ban@example.com", "mail");
  private final FormField phone = FormField.text("Số điện thoại", "09xxxxxxxx", "phone");
  private final FormField dob = FormField.text("Ngày sinh", "dd/MM/yyyy", "calendar");

  private final List<FormField> fields =
      List.of(lname, fname, username, password, gmail, phone, dob);
  private final JLabel globalError = new JLabel();
  private final SessionGateway gateway;
  private final Consumer<users> onRegistered;
  private final Runnable onBack;

  public RegisterScreen(
      SessionGateway gateway, Consumer<users> onRegistered, Runnable onBack) {
    this.gateway = gateway;
    this.onRegistered = onRegistered == null ? user -> {} : onRegistered;
    this.onBack = onBack == null ? () -> {} : onBack;
    setLayout(new GridBagLayout());
    add(buildCard());
  }

  private Card buildCard() {
    Card card =
        new Card() {
          @Override
          public Dimension getPreferredSize() {
            Dimension size = super.getPreferredSize();
            return new Dimension(UiScale.px(560), size.height);
          }
        };
    card.setBorder(new EmptyBorder(Space.X3L, Space.X3L, Space.X3L, Space.X3L));

    JPanel body = new JPanel();
    body.setOpaque(false);
    body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
    body.setAlignmentX(Component.LEFT_ALIGNMENT);

    JPanel logo = new JPanel(new BorderLayout(Space.SM, 0));
    logo.setOpaque(false);
    logo.setAlignmentX(Component.LEFT_ALIGNMENT);
    logo.setMaximumSize(new Dimension(Integer.MAX_VALUE, UiScale.px(40)));
    logo.add(new CircleIconPanel(40, Palette.PRIMARY, "logo", Palette.SHELL), BorderLayout.WEST);
    JLabel word = new JLabel("Collabify");
    word.setFont(Typography.logo());
    word.setForeground(Palette.TEXT);
    logo.add(word, BorderLayout.CENTER);

    JLabel title = new JLabel("Tạo tài khoản mới");
    title.setFont(Typography.of(Typography.Weight.EXTRABOLD, UiScale.font(26)));
    title.setForeground(Palette.TEXT);
    title.setAlignmentX(Component.LEFT_ALIGNMENT);

    JLabel subtitle = new JLabel("Điền đầy đủ thông tin để bắt đầu làm việc cùng nhóm.");
    subtitle.setFont(Typography.body());
    subtitle.setForeground(Palette.TEXT_MUTED_STRONG);
    subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

    globalError.setFont(Typography.small());
    globalError.setForeground(Palette.DANGER);
    globalError.setIcon(Icons.danger("alert", 18));
    globalError.setIconTextGap(Space.XS);
    globalError.setAlignmentX(Component.LEFT_ALIGNMENT);
    globalError.setVisible(false);

    body.add(logo);
    body.add(vgap(Space.MD));
    body.add(title);
    body.add(vgap(Space.XXS));
    body.add(subtitle);
    body.add(vgap(Space.MD));
    body.add(globalError);
    body.add(vgap(Space.XS));
    body.add(twoColumns(lname, fname));
    body.add(vgap(Space.SM));
    body.add(twoColumns(username, password));
    body.add(vgap(Space.SM));
    body.add(twoColumns(gmail, phone));
    body.add(vgap(Space.SM));
    body.add(dob);
    body.add(vgap(Space.LG));
    body.add(actions());

    card.add(body, BorderLayout.CENTER);
    return card;
  }

  private JPanel twoColumns(FormField left, FormField right) {
    JPanel row = new JPanel(new GridLayout(1, 2, Space.MD, 0));
    row.setOpaque(false);
    row.setAlignmentX(Component.LEFT_ALIGNMENT);
    row.add(left);
    row.add(right);
    return row;
  }

  private JPanel actions() {
    JPanel row = new JPanel(new BorderLayout(Space.MD, 0));
    row.setOpaque(false);
    row.setAlignmentX(Component.LEFT_ALIGNMENT);
    row.setMaximumSize(new Dimension(Integer.MAX_VALUE, UiScale.px(66)));

    JButton back = new JButton("Đã có tài khoản? Đăng nhập");
    back.setFont(Typography.meta());
    back.setForeground(Palette.PRIMARY_DARK);
    back.setContentAreaFilled(false);
    back.setBorderPainted(false);
    back.setFocusPainted(false);
    back.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    back.addActionListener(e -> this.onBack.run());

    AppButton register = new AppButton("Tạo tài khoản", AppButton.Variant.PRIMARY).withIcon("check");
    register.addActionListener(e -> submit());

    JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, Space.SM, 0));
    buttons.setOpaque(false);
    buttons.add(register);

    row.add(back, BorderLayout.WEST);
    row.add(buttons, BorderLayout.EAST);
    return row;
  }

  private static Component vgap(int height) {
    Component gap = Box.createVerticalStrut(height);
    ((JComponent) gap).setAlignmentX(Component.LEFT_ALIGNMENT);
    return gap;
  }

  public void reset() {
    fields.forEach(field -> {
      field.setValue("");
      field.clearError();
    });
    globalError.setVisible(false);
    revalidate();
    repaint();
  }

  private void submit() {
    fields.forEach(FormField::clearError);
    globalError.setVisible(false);

    boolean valid = true;
    for (FormField field : fields) {
      if (field.getValue().isEmpty()) {
        field.setError(Validators.REQUIRED);
        valid = false;
      }
    }

    if (!password.getValue().isEmpty() && !Validators.validPassword(password.getValue())) {
      password.setError("Mật khẩu phải có ít nhất 6 ký tự.");
      valid = false;
    }
    if (!gmail.getValue().isEmpty() && !Validators.validEmail(gmail.getValue())) {
      gmail.setError("Gmail không hợp lệ.");
      valid = false;
    }
    if (!phone.getValue().isEmpty() && !Validators.validPhone(phone.getValue())) {
      phone.setError("Số điện thoại không hợp lệ.");
      valid = false;
    }
    if (!dob.getValue().isEmpty() && !Validators.validDate(dob.getValue())) {
      dob.setError("Ngày sinh không hợp lệ (dd/MM/yyyy).");
      valid = false;
    }

    if (!valid) {
      for (FormField field : fields) {
        if (field.hasError()) {
          field.focusInput();
          break;
        }
      }
      return;
    }

    Registration registration =
        new Registration(
            lname.getValue(),
            fname.getValue(),
            username.getValue(),
            password.getValue(),
            gmail.getValue(),
            phone.getValue(),
            dob.getValue());

    try {
      if (!gateway.register(registration)) {
        globalError.setText("Tên đăng nhập hoặc Gmail đã tồn tại.");
        globalError.setVisible(true);
        revalidate();
        repaint();
        return;
      }
      users user = gateway.authenticate(username.getValue(), password.getValue());
      if (user == null) {
        globalError.setText("Tạo tài khoản thành công nhưng chưa thể đăng nhập. Vui lòng thử lại.");
        globalError.setVisible(true);
        revalidate();
        repaint();
        return;
      }
      reset();
      onRegistered.accept(user);
    } catch (DataAccessException ex) {
      globalError.setText(ex.getMessage());
      globalError.setVisible(true);
      revalidate();
      repaint();
    }
  }
}
