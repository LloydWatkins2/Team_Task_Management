package com.java.teamtask.ui;

import com.java.teamtask.elements.users;
import com.java.teamtask.ui.components.AppButton;
import com.java.teamtask.ui.components.FormField;
import com.java.teamtask.ui.components.ProjectCard;
import com.java.teamtask.ui.data.DataAccessException;
import com.java.teamtask.ui.data.Registration;
import com.java.teamtask.ui.data.Validators;
import com.java.teamtask.ui.screens.DashboardScreen;
import com.java.teamtask.ui.screens.LoginScreen;
import com.java.teamtask.ui.screens.RegisterScreen;
import com.java.teamtask.ui.theme.Theme;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Kiểm tra luồng đăng nhập/đăng ký và dữ liệu dự án trên dashboard. */
class LoginFlowTest {
    @BeforeAll
    static void setUp() {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(), "Cần môi trường có màn hình");
        Theme.install();
    }

    @Test
    void wrongCredentialsKeepLoginAndShowInlineError() throws Exception {
        TestGateway gateway = new TestGateway();
        MainFrame frame = createFrame(gateway);
        try {
            LoginScreen login = find(frame.getContentPane(), LoginScreen.class);
            assertNotNull(login);
            List<FormField> fields = findAll(login, FormField.class);
            assertEquals(2, fields.size());
            AppButton submit = find(login, AppButton.class, button -> "Đăng nhập".equals(button.getText()));
            assertNotNull(submit);

            SwingUtilities.invokeAndWait(() -> {
                fields.get(0).setValue("an");
                fields.get(1).setValue("sai-mat-khau");
                submit.doClick();
            });

            assertTrue(fields.get(1).hasError(), "Sai mật khẩu phải hiện lỗi tại field");
            AppRootPanel appRoot = find(frame.getContentPane(), AppRootPanel.class);
            assertNotNull(appRoot);
            assertFalse(appRoot.isVisible(), "Đăng nhập sai không được mở app shell");
        } finally {
            SwingUtilities.invokeAndWait(frame::dispose);
        }
    }

    @Test
    void validCredentialsOpenDashboard() throws Exception {
        TestGateway gateway = new TestGateway();
        MainFrame frame = createFrame(gateway);
        try {
            LoginScreen login = find(frame.getContentPane(), LoginScreen.class);
            List<FormField> fields = findAll(login, FormField.class);
            AppButton submit = find(login, AppButton.class, button -> "Đăng nhập".equals(button.getText()));

            SwingUtilities.invokeAndWait(() -> {
                fields.get(0).setValue("an");
                fields.get(1).setValue("123456");
                submit.doClick();
            });

            AppRootPanel appRoot = find(frame.getContentPane(), AppRootPanel.class);
            assertNotNull(appRoot);
            assertTrue(appRoot.isVisible(), "Đăng nhập đúng phải mở app shell");
            assertNotNull(find(appRoot, DashboardScreen.class));
        } finally {
            SwingUtilities.invokeAndWait(frame::dispose);
        }
    }

    @Test
    void dashboardShowsOnlyProjectsOfCurrentUserWithRole() throws Exception {
        TestGateway gateway = new TestGateway();
        MainFrame frame = createFrame(gateway);
        try {
            SwingUtilities.invokeAndWait(() -> frame.signIn(gateway.an));

            DashboardScreen dashboard = find(frame.getContentPane(), DashboardScreen.class);
            assertNotNull(dashboard);
            List<ProjectCard> cards = findAll(dashboard, ProjectCard.class);
            assertEquals(2, cards.size(), "Chỉ hiển thị dự án mà người dùng tham gia");

            String text = allLabelText(cards);
            assertTrue(text.contains("Website Redesign"), "Phải có dự án người dùng làm owner");
            assertTrue(text.contains("Mobile App"), "Phải có dự án người dùng làm member");
            assertTrue(text.contains("CHỦ DỰ ÁN"), "Role owner phải hiển thị cạnh dự án");
            assertTrue(text.contains("THÀNH VIÊN"), "Role member phải hiển thị cạnh dự án");
            assertFalse(text.contains("Dự án bí mật"), "Không được lộ dự án ngoài phạm vi");
        } finally {
            SwingUtilities.invokeAndWait(frame::dispose);
        }
    }

    @Test
    void registerRejectsMissingFieldsWithRequiredMessage() throws Exception {
        TestGateway gateway = new TestGateway();
        MainFrame frame = createFrame(gateway);
        try {
            RegisterScreen register = find(frame.getContentPane(), RegisterScreen.class);
            assertNotNull(register);
            List<FormField> fields = findAll(register, FormField.class);
            assertEquals(7, fields.size(), "Form đăng ký phải có đủ 7 trường của system.register");
            AppButton submit = find(register, AppButton.class,
                    button -> "Tạo tài khoản".equals(button.getText()));
            assertNotNull(submit);

            SwingUtilities.invokeAndWait(submit::doClick);

            for (FormField field : fields) {
                assertTrue(field.hasError(), "Mọi ô trống đều phải báo lỗi");
                assertEquals(Validators.REQUIRED, field.getErrorText(),
                        "Thiếu trường phải hiện đúng câu bắt buộc");
            }
            assertNull(gateway.lastRegistration, "Form thiếu trường không được gửi đăng ký");
        } finally {
            SwingUtilities.invokeAndWait(frame::dispose);
        }
    }

    @Test
    void registerPassesAllFieldsAndOpensDashboard() throws Exception {
        TestGateway gateway = new TestGateway();
        MainFrame frame = createFrame(gateway);
        try {
            RegisterScreen register = find(frame.getContentPane(), RegisterScreen.class);
            List<FormField> fields = findAll(register, FormField.class);
            AppButton submit = find(register, AppButton.class,
                    button -> "Tạo tài khoản".equals(button.getText()));

            SwingUtilities.invokeAndWait(() -> {
                fields.get(0).setValue("Lê");
                fields.get(1).setValue("Bình");
                fields.get(2).setValue("binh");
                fields.get(3).setValue("123456");
                fields.get(4).setValue("binh@example.com");
                fields.get(5).setValue("0912345678");
                fields.get(6).setValue("03/03/2001");
                submit.doClick();
            });

            Registration sent = gateway.lastRegistration;
            assertNotNull(sent, "Form hợp lệ phải gửi đăng ký");
            assertEquals("Lê", sent.lname());
            assertEquals("Bình", sent.fname());
            assertEquals("binh", sent.username());
            assertEquals("123456", sent.password());
            assertEquals("binh@example.com", sent.gmail());
            assertEquals("0912345678", sent.phone());
            assertEquals("03/03/2001", sent.dob());

            AppRootPanel appRoot = find(frame.getContentPane(), AppRootPanel.class);
            assertNotNull(appRoot);
            assertTrue(appRoot.isVisible(), "Đăng ký thành công phải vào thẳng Dashboard");
        } finally {
            SwingUtilities.invokeAndWait(frame::dispose);
        }
    }

    @Test
    void registerShowsDuplicateMessageForExistingAccount() throws Exception {
        TestGateway gateway = new TestGateway();
        MainFrame frame = createFrame(gateway);
        try {
            RegisterScreen register = find(frame.getContentPane(), RegisterScreen.class);
            List<FormField> fields = findAll(register, FormField.class);
            AppButton submit = find(register, AppButton.class,
                    button -> "Tạo tài khoản".equals(button.getText()));

            SwingUtilities.invokeAndWait(() -> {
                fill(fields, "Lê", "Bình", "an", "123456",
                        "binh@example.com", "0912345678", "03/03/2001");
                submit.doClick();
            });

            JLabel duplicate = findLabel(register, "Tên đăng nhập hoặc Gmail đã tồn tại.");
            assertNotNull(duplicate, "Trùng username/gmail phải hiện đúng thông báo");
        } finally {
            SwingUtilities.invokeAndWait(frame::dispose);
        }
    }

    @Test
    void registerShowsDatabaseFailureInsteadOfDuplicateMessage() throws Exception {
        TestGateway gateway = new TestGateway();
        gateway.registerFailure =
                new DataAccessException("Không thể tạo tài khoản lúc này. Vui lòng thử lại.");
        MainFrame frame = createFrame(gateway);
        try {
            RegisterScreen register = find(frame.getContentPane(), RegisterScreen.class);
            List<FormField> fields = findAll(register, FormField.class);
            AppButton submit = find(register, AppButton.class,
                    button -> "Tạo tài khoản".equals(button.getText()));

            SwingUtilities.invokeAndWait(() -> {
                fill(fields, "Lê", "Bình", "binh", "123456",
                        "binh@example.com", "0912345678", "03/03/2001");
                submit.doClick();
            });

            assertNotNull(findLabel(register, "Không thể tạo tài khoản lúc này. Vui lòng thử lại."),
                    "Lỗi SQL phải hiển thị đúng thông báo");
            assertNull(findLabel(register, "Tên đăng nhập hoặc Gmail đã tồn tại."),
                    "Lỗi SQL không được báo nhầm là trùng tài khoản");
        } finally {
            SwingUtilities.invokeAndWait(frame::dispose);
        }
    }

    private static void fill(List<FormField> fields, String... values) {
        for (int i = 0; i < values.length; i++) {
            fields.get(i).setValue(values[i]);
        }
    }

    private static JLabel findLabel(Container root, String text) {
        return find(root, JLabel.class, label -> text.equals(label.getText()) && label.isVisible());
    }

    private static MainFrame createFrame(TestGateway gateway) throws Exception {
        MainFrame[] holder = new MainFrame[1];
        SwingUtilities.invokeAndWait(() -> {
            MainFrame frame = new MainFrame(gateway);
            frame.addNotify();
            frame.setSize(1360, 900);
            frame.validate();
            holder[0] = frame;
        });
        return holder[0];
    }

    private static String allLabelText(List<? extends Container> roots) {
        StringBuilder text = new StringBuilder();
        for (Container root : roots) {
            for (JLabel label : findAll(root, JLabel.class)) {
                text.append(label.getText()).append('\n');
            }
        }
        return text.toString();
    }

    private static <T extends Component> T find(Container root, Class<T> type) {
        return find(root, type, value -> true);
    }

    private static <T extends Component> T find(Container root, Class<T> type, Predicate<T> filter) {
        if (type.isInstance(root) && filter.test(type.cast(root))) {
            return type.cast(root);
        }
        for (Component child : root.getComponents()) {
            if (child instanceof Container container) {
                T found = find(container, type, filter);
                if (found != null) {
                    return found;
                }
            } else if (type.isInstance(child) && filter.test(type.cast(child))) {
                return type.cast(child);
            }
        }
        return null;
    }

    private static <T extends Component> List<T> findAll(Container root, Class<T> type) {
        List<T> found = new ArrayList<>();
        if (type.isInstance(root)) {
            found.add(type.cast(root));
        }
        for (Component child : root.getComponents()) {
            if (child instanceof Container container) {
                found.addAll(findAll(container, type));
            } else if (type.isInstance(child)) {
                found.add(type.cast(child));
            }
        }
        return found;
    }
}
