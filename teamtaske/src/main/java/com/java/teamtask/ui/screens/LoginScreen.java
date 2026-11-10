package com.java.teamtask.ui.screens;

import com.java.teamtask.elements.users;
import com.java.teamtask.ui.Card;
import com.java.teamtask.ui.GradientPanel;
import com.java.teamtask.ui.components.AppButton;
import com.java.teamtask.ui.components.CircleIconPanel;
import com.java.teamtask.ui.components.FormField;
import com.java.teamtask.ui.data.DataAccessException;
import com.java.teamtask.ui.data.SessionGateway;
import com.java.teamtask.ui.theme.Icons;
import com.java.teamtask.ui.theme.Palette;
import com.java.teamtask.ui.theme.Space;
import com.java.teamtask.ui.theme.Typography;
import com.java.teamtask.ui.theme.UiScale;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.function.Consumer;

/** Màn hình đăng nhập: xác thực qua system.authentication. */
public final class LoginScreen extends GradientPanel {
  private final FormField username = FormField.text("Tên đăng nhập", "Nhập tên đăng nhập", "user");
  private final FormField password = FormField.password("Mật khẩu", "Nhập mật khẩu", "lock");
  private final JLabel globalError = new JLabel();
  private final SessionGateway gateway;
  private final Consumer<users> onLogin;
  private final Runnable onOpenRegister;

  public LoginScreen(
      SessionGateway gateway, Consumer<users> onLogin, Runnable onOpenRegister) {
    this.gateway = gateway;
    this.onLogin = onLogin == null ? user -> {} : onLogin;
    this.onOpenRegister = onOpenRegister == null ? () -> {} : onOpenRegister;
    setLayout(new GridBagLayout());
    add(buildCard());

    getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
        .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "submit");
    getActionMap()
        .put(
            "submit",
            new AbstractAction() {
              @Override
              public void actionPerformed(ActionEvent e) {
                submit();
              }
            });
  }

  private Card buildCard() {
    Card card =
        new Card() {
          @Override
          public Dimension getPreferredSize() {
            Dimension size = super.getPreferredSize();
            return new Dimension(UiScale.px(480), size.height);
          }
        };
    card.setBorder(new EmptyBorder(Space.X4L, Space.X4L, Space.X4L, Space.X4L));

    JPanel body = new JPanel();
    body.setOpaque(false);
    body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
    body.setAlignmentX(Component.LEFT_ALIGNMENT);

    JPanel logo = new JPanel(new BorderLayout(Space.SM, 0));
    logo.setOpaque(false);
    logo.setAlignmentX(Component.LEFT_ALIGNMENT);
    logo.setMaximumSize(new Dimension(Integer.MAX_VALUE, UiScale.px(44)));
    logo.add(new CircleIconPanel(44, Palette.PRIMARY, "logo", Palette.SHELL), BorderLayout.WEST);
    JLabel word = new JLabel("Uyên Phương");
    word.setFont(Typography.logo());
    word.setForeground(Palette.TEXT);
    logo.add(word, BorderLayout.CENTER);

    JLabel title = new JLabel("Chào mừng trở lại");
    title.setFont(Typography.of(Typography.Weight.EXTRABOLD, UiScale.font(28)));
    title.setForeground(Palette.TEXT);
    title.setAlignmentX(Component.LEFT_ALIGNMENT);

    JLabel subtitle = new JLabel("Đăng nhập để tiếp tục quản lý công việc nhóm.");
    subtitle.setFont(Typography.body());
    subtitle.setForeground(Palette.TEXT_MUTED_STRONG);
    subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

    globalError.setFont(Typography.small());
    globalError.setForeground(Palette.DANGER);
    globalError.setIcon(Icons.danger("alert", 18));
    globalError.setIconTextGap(Space.XS);
    globalError.setAlignmentX(Component.LEFT_ALIGNMENT);
    globalError.setVisible(false);

    AppButton login = new AppButton("Đăng nhập", AppButton.Variant.PRIMARY);
    login.setBlock(true);
    login.setAlignmentX(Component.LEFT_ALIGNMENT);
    login.addActionListener(e -> submit());

    body.add(logo);
    body.add(vgap(Space.LG));
    body.add(title);
    body.add(vgap(Space.XS));
    body.add(subtitle);
    body.add(vgap(Space.XL));
    body.add(globalError);
    body.add(vgap(Space.SM));
    body.add(username);
    body.add(vgap(Space.XL));
    body.add(password);
    body.add(vgap(Space.XS));
    body.add(forgotRow());
    body.add(vgap(Space.LG));
    body.add(login);
    body.add(vgap(Space.MD));
    body.add(registerRow());

    card.add(body, BorderLayout.CENTER);
    return card;
  }

  private JPanel forgotRow() {
    JPanel row = new JPanel(new BorderLayout());
    row.setOpaque(false);
    row.setAlignmentX(Component.LEFT_ALIGNMENT);
    row.setMaximumSize(new Dimension(Integer.MAX_VALUE, UiScale.px(28)));

    JButton forgot = new JButton("Quên mật khẩu?");
    forgot.setFont(Typography.meta());
    forgot.setForeground(Palette.PRIMARY_DARK);
    forgot.setContentAreaFilled(false);
    forgot.setBorderPainted(false);
    forgot.setFocusPainted(false);
    forgot.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    forgot.setHorizontalAlignment(SwingConstants.RIGHT);
    forgot.addActionListener(
        e ->
            JOptionPane.showMessageDialog(
                this,
                "Vui lòng liên hệ quản trị viên để đặt lại mật khẩu.",
                "Quên mật khẩu",
                JOptionPane.INFORMATION_MESSAGE));
    row.add(forgot, BorderLayout.EAST);
    return row;
  }

  private JPanel registerRow() {
    JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, Space.XS, 0));
    row.setOpaque(false);
    row.setAlignmentX(Component.LEFT_ALIGNMENT);
    row.setMaximumSize(new Dimension(Integer.MAX_VALUE, UiScale.px(32)));

    JLabel hint = new JLabel("Chưa có tài khoản?");
    hint.setFont(Typography.small());
    hint.setForeground(Palette.TEXT_MUTED_STRONG);

    JButton register = new JButton("Đăng ký ngay");
    register.setFont(Typography.meta());
    register.setForeground(Palette.PRIMARY_DARK);
    register.setContentAreaFilled(false);
    register.setBorderPainted(false);
    register.setFocusPainted(false);
    register.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    register.addActionListener(e -> this.onOpenRegister.run());

    row.add(hint);
    row.add(register);
    return row;
  }

  private static Component vgap(int height) {
    Component gap = Box.createVerticalStrut(height);
    ((JComponent) gap).setAlignmentX(Component.LEFT_ALIGNMENT);
    return gap;
  }

  public void reset() {
    username.setValue("");
    password.setValue("");
    username.clearError();
    password.clearError();
    globalError.setVisible(false);
  }

  private void submit() {
    username.clearError();
    password.clearError();
    globalError.setVisible(false);

    boolean valid = true;
    if (username.getValue().isEmpty()) {
      username.setError("Vui lòng nhập tên đăng nhập.");
      valid = false;
    }
    if (password.getValue().isEmpty()) {
      password.setError("Vui lòng nhập mật khẩu.");
      valid = false;
    }
    if (!valid) {
      if (username.getValue().isEmpty()) {
        username.focusInput();
      } else {
        password.focusInput();
      }
      return;
    }

    try {
      users user = gateway.authenticate(username.getValue(), password.getValue());
      if (user == null) {
        globalError.setText("Tên đăng nhập hoặc mật khẩu không đúng.");
        globalError.setVisible(true);
        password.setError("Kiểm tra lại thông tin đăng nhập.");
        password.focusInput();
        revalidate();
        repaint();
        return;
      }
      reset();
      onLogin.accept(user);
    } catch (DataAccessException ex) {
      globalError.setText(ex.getMessage());
      globalError.setVisible(true);
      revalidate();
      repaint();
    }
  }
}
