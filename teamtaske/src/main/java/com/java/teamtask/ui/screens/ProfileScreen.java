package com.java.teamtask.ui.screens;

import com.java.teamtask.elements.users;
import com.java.teamtask.ui.Card;
import com.java.teamtask.ui.components.AppButton;
import com.java.teamtask.ui.components.FormField;
import com.java.teamtask.ui.components.PageHeader;
import com.java.teamtask.ui.data.DataAccessException;
import com.java.teamtask.ui.data.ProfileChange;
import com.java.teamtask.ui.data.SessionGateway;
import com.java.teamtask.ui.data.UiMapper;
import com.java.teamtask.ui.data.UserSession;
import com.java.teamtask.ui.data.Validators;
import com.java.teamtask.ui.theme.Palette;
import com.java.teamtask.ui.theme.Space;
import com.java.teamtask.ui.theme.Typography;
import com.java.teamtask.ui.theme.UiScale;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

/** Hồ sơ cá nhân: bắt buộc xác thực trước khi cập nhật, chỉ cập nhật trường đã thay đổi. */
public final class ProfileScreen extends JPanel {
  private final SessionGateway gateway;
  private final UserSession session;
  private final Runnable onBack;

  private final FormField authPassword =
      FormField.password("Mật khẩu hiện tại *", "Nhập mật khẩu để xác thực", "lock");
  private final JLabel authMessage = new JLabel();
  private final JLabel globalMessage = new JLabel();
  private Card editCard;

  private final FormField lname = FormField.text("Họ", "Nhập họ mới", "user");
  private final FormField fname = FormField.text("Tên", "Nhập tên mới", "user");
  private final FormField gmail = FormField.text("Gmail", "ban@example.com", "mail");
  private final FormField phone = FormField.text("Số điện thoại", "09xxxxxxxx", "phone");
  private final FormField dob = FormField.text("Ngày sinh", "dd/MM/yyyy", "calendar");
  private final FormField newPassword =
      FormField.password("Mật khẩu mới", "Tối thiểu 6 ký tự", "lock");

  private final List<Hint> hints = new ArrayList<>();
  private final JLabel usernameValue = new JLabel();
  private final JLabel fullNameValue = new JLabel();

  private boolean authenticated;

  public ProfileScreen(SessionGateway gateway, UserSession session, Runnable onBack) {
    this.gateway = gateway;
    this.session = session;
    this.onBack = onBack == null ? () -> {} : onBack;

    setOpaque(false);
    setLayout(new BorderLayout());
    PageHeader header =
        new PageHeader(
            "Hồ sơ cá nhân",
            "Xác thực tài khoản trước khi thay đổi thông tin.",
            backButton());
    add(header, BorderLayout.NORTH);

    JPanel holder = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    holder.setOpaque(false);
    holder.add(body());
    add(holder, BorderLayout.CENTER);

    refreshHints();
    editCard.setVisible(false);
  }

  private AppButton backButton() {
    AppButton back = new AppButton("Quay lại", AppButton.Variant.OUTLINE).withIcon("dashboard");
    back.addActionListener(e -> onBack.run());
    return back;
  }

  private JPanel body() {
    JPanel body = new JPanel();
    body.setOpaque(false);
    body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
    body.setAlignmentX(Component.LEFT_ALIGNMENT);

    body.add(authCard());
    body.add(Box.createVerticalStrut(Space.XL));
    editCard = editCard();
    body.add(editCard);
    return body;
  }

  private Card authCard() {
    Card card = fixedCard();
    card.setBorder(new EmptyBorder(Space.XL, Space.X3L, Space.XL, Space.X3L));

    JPanel content = new JPanel();
    content.setOpaque(false);
    content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

    JLabel title = new JLabel("Xác thực tài khoản");
    title.setFont(Typography.h3());
    title.setForeground(Palette.TEXT);
    title.setAlignmentX(Component.LEFT_ALIGNMENT);

    JLabel subtitle = new JLabel("Nhập mật khẩu hiện tại để mở khoá phần chỉnh sửa hồ sơ.");
    subtitle.setFont(Typography.body());
    subtitle.setForeground(Palette.TEXT_MUTED_STRONG);
    subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

    authMessage.setFont(Typography.small());
    authMessage.setAlignmentX(Component.LEFT_ALIGNMENT);
    authMessage.setVisible(false);

    AppButton verify = new AppButton("Xác thực", AppButton.Variant.PRIMARY).withIcon("lock");
    verify.addActionListener(e -> authenticate());
    authPassword.getInputMap().clear();

    JPanel row = new JPanel(new BorderLayout(Space.MD, 0));
    row.setOpaque(false);
    row.setAlignmentX(Component.LEFT_ALIGNMENT);
    row.add(authPassword, BorderLayout.CENTER);
    JPanel buttonBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
    buttonBox.setOpaque(false);
    buttonBox.add(verify);
    row.add(buttonBox, BorderLayout.EAST);

    content.add(title);
    content.add(Box.createVerticalStrut(Space.XXS));
    content.add(subtitle);
    content.add(Box.createVerticalStrut(Space.LG));
    content.add(authMessage);
    content.add(Box.createVerticalStrut(Space.SM));
    content.add(row);
    card.add(content, BorderLayout.CENTER);
    return card;
  }

  private Card editCard() {
    Card card = fixedCard();
    card.setBorder(new EmptyBorder(Space.XL, Space.X3L, Space.XL, Space.X3L));

    JPanel content = new JPanel();
    content.setOpaque(false);
    content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

    JLabel title = new JLabel("Thông tin cá nhân");
    title.setFont(Typography.h3());
    title.setForeground(Palette.TEXT);
    title.setAlignmentX(Component.LEFT_ALIGNMENT);

    JLabel hint =
        new JLabel("Các trường để trống sẽ giữ nguyên giá trị hiện tại.");
    hint.setFont(Typography.body());
    hint.setForeground(Palette.TEXT_MUTED_STRONG);
    hint.setAlignmentX(Component.LEFT_ALIGNMENT);

    fullNameValue.setFont(Typography.label());
    fullNameValue.setForeground(Palette.TEXT);
    fullNameValue.setAlignmentX(Component.LEFT_ALIGNMENT);
    usernameValue.setFont(Typography.small());
    usernameValue.setForeground(Palette.TEXT_MUTED_STRONG);
    usernameValue.setAlignmentX(Component.LEFT_ALIGNMENT);

    globalMessage.setFont(Typography.small());
    globalMessage.setAlignmentX(Component.LEFT_ALIGNMENT);
    globalMessage.setVisible(false);

    AppButton update =
        new AppButton("Lưu thay đổi", AppButton.Variant.PRIMARY).withIcon("check");
    update.addActionListener(e -> updateProfile());

    JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, Space.SM, 0));
    actions.setOpaque(false);
    actions.setAlignmentX(Component.LEFT_ALIGNMENT);
    actions.add(update);

    content.add(title);
    content.add(Box.createVerticalStrut(Space.XXS));
    content.add(hint);
    content.add(Box.createVerticalStrut(Space.MD));
    content.add(fullNameValue);
    content.add(usernameValue);
    content.add(Box.createVerticalStrut(Space.XL));
    content.add(twoColumns(lname, fname, "Họ hiện tại", "Tên hiện tại"));
    content.add(Box.createVerticalStrut(Space.MD));
    content.add(twoColumns(gmail, phone, "Gmail hiện tại", "Số điện thoại hiện tại"));
    content.add(Box.createVerticalStrut(Space.MD));
    content.add(twoColumns(dob, newPassword, "Ngày sinh hiện tại", "Mật khẩu được mã hoá"));
    content.add(Box.createVerticalStrut(Space.SM));
    content.add(globalMessage);
    content.add(Box.createVerticalStrut(Space.LG));
    content.add(actions);

    card.add(content, BorderLayout.CENTER);
    return card;
  }

  private JPanel twoColumns(
      FormField left, FormField right, String leftHint, String rightHint) {
    JPanel row = new JPanel(new GridLayout(1, 2, Space.MD, 0));
    row.setOpaque(false);
    row.setAlignmentX(Component.LEFT_ALIGNMENT);
    row.add(withHint(left, () -> leftHint + ": " + currentLeft(left)));
    row.add(withHint(right, () -> rightHint + ": " + currentRight(right)));
    return row;
  }

  private JPanel withHint(FormField field, java.util.function.Supplier<String> text) {
    field.setAlignmentX(Component.LEFT_ALIGNMENT);
    JLabel hint = new JLabel(text.get());
    hint.setFont(Typography.small());
    hint.setForeground(Palette.TEXT_MUTED_STRONG);
    hint.setAlignmentX(Component.LEFT_ALIGNMENT);
    hint.setBorder(new EmptyBorder(Space.XXS, Space.SM, 0, 0));
    field.add(hint);
    hints.add(new Hint(hint, text));
    return field;
  }

  private String currentLeft(FormField field) {
    if (field == lname) {
      return value(session.getUser() == null ? null : session.getUser().getLname());
    }
    if (field == gmail) {
      return value(session.getUser() == null ? null : session.getUser().getGmail());
    }
    if (field == dob) {
      return value(session.getUser() == null ? null : session.getUser().getDob());
    }
    return "";
  }

  private String currentRight(FormField field) {
    if (field == fname) {
      return value(session.getUser() == null ? null : session.getUser().getFname());
    }
    if (field == phone) {
      return value(session.getUser() == null ? null : session.getUser().getPhone());
    }
    return "••••••";
  }

  private static String value(String value) {
    return value == null || value.isBlank() ? "(chưa có)" : value;
  }

  private Card fixedCard() {
    return new Card() {
      @Override
      public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        return new Dimension(UiScale.px(720), size.height);
      }
    };
  }

  private void refreshHints() {
    users user = session.getUser();
    fullNameValue.setText(UiMapper.displayName(user));
    usernameValue.setText(
        user == null || user.getUsername() == null
            ? ""
            : "Tên đăng nhập: @" + user.getUsername());
    for (Hint hint : hints) {
      hint.label().setText(hint.text().get());
    }
    revalidate();
    repaint();
  }

  private void authenticate() {
    authPassword.clearError();
    authMessage.setVisible(false);
    if (authPassword.getValue().isEmpty()) {
      authPassword.setError("Vui lòng nhập mật khẩu.");
      authPassword.focusInput();
      return;
    }
    if (session.getUser() == null) {
      showAuthMessage("Chưa có phiên đăng nhập.", Palette.DANGER);
      return;
    }
    try {
      if (gateway.authenticateForProfile(session.getUser(), authPassword.getValue())) {
        authenticated = true;
        editCard.setVisible(true);
        authPassword.setValue("");
        refreshHints();
        showAuthMessage("Xác thực thành công. Bạn có thể chỉnh sửa hồ sơ.", Palette.SUCCESS);
        revalidate();
        repaint();
      } else {
        authPassword.setError("Mật khẩu không đúng.");
        authPassword.focusInput();
      }
    } catch (DataAccessException ex) {
      showAuthMessage(ex.getMessage(), Palette.DANGER);
    }
  }

  private void updateProfile() {
    if (!authenticated || session.getUser() == null) {
      showAuthMessage("Vui lòng xác thực tài khoản trước.", Palette.DANGER);
      return;
    }
    clearErrors();
    globalMessage.setVisible(false);

    boolean valid = true;
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
    if (!newPassword.getValue().isEmpty() && !Validators.validPassword(newPassword.getValue())) {
      newPassword.setError("Mật khẩu phải có ít nhất 6 ký tự.");
      valid = false;
    }
    if (!valid) {
      for (FormField field : List.of(gmail, phone, dob, newPassword)) {
        if (field.hasError()) {
          field.focusInput();
          break;
        }
      }
      return;
    }

    ProfileChange change =
        new ProfileChange(
            blankToNull(lname.getValue()),
            blankToNull(fname.getValue()),
            blankToNull(gmail.getValue()),
            blankToNull(phone.getValue()),
            blankToNull(dob.getValue()),
            blankToNull(newPassword.getValue()));

    if (change.isEmpty()) {
      showGlobalMessage("Vui lòng thay đổi ít nhất một trường.", Palette.DANGER);
      return;
    }

    try {
      if (gateway.updateProfile(session.getUser(), change)) {
        clearValues();
        refreshHints();
        showGlobalMessage("Cập nhật hồ sơ thành công.", Palette.SUCCESS);
      } else {
        showGlobalMessage("Không thể cập nhật hồ sơ.", Palette.DANGER);
      }
    } catch (DataAccessException ex) {
      showGlobalMessage(ex.getMessage(), Palette.DANGER);
    }
  }

  private void clearErrors() {
    for (FormField field : List.of(lname, fname, gmail, phone, dob, newPassword)) {
      field.clearError();
    }
  }

  private void clearValues() {
    for (FormField field : List.of(lname, fname, gmail, phone, dob, newPassword)) {
      field.setValue("");
    }
  }

  private static String blankToNull(String value) {
    return value == null || value.isBlank() ? null : value.trim();
  }

  private void showAuthMessage(String text, java.awt.Color color) {
    authMessage.setText(text);
    authMessage.setForeground(color);
    authMessage.setVisible(true);
    revalidate();
    repaint();
  }

  private void showGlobalMessage(String text, java.awt.Color color) {
    globalMessage.setText(text);
    globalMessage.setForeground(color);
    globalMessage.setVisible(true);
    revalidate();
    repaint();
  }

  public void onShown() {
    authenticated = false;
    editCard.setVisible(false);
    authPassword.setValue("");
    authPassword.clearError();
    authMessage.setVisible(false);
    clearErrors();
    clearValues();
    refreshHints();
  }

  private record Hint(JLabel label, java.util.function.Supplier<String> text) {}
}
