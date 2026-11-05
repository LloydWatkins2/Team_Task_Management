package com.java.teamtask.ui;

import com.java.teamtask.ui.components.AppButton;
import com.java.teamtask.ui.components.FormField;
import com.java.teamtask.ui.screens.DashboardScreen;
import com.java.teamtask.ui.screens.LoginScreen;
import com.java.teamtask.ui.theme.Theme;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.swing.JComponent;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Kiểm tra luồng đăng nhập: lỗi tại chỗ và chuyển sang app shell. */
class LoginFlowTest {
    @BeforeAll
    static void setUp() {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(), "Cần môi trường có màn hình");
        Theme.install();
    }

    @Test
    void wrongCredentialsKeepLoginAndShowInlineError() throws Exception {
        MainFrame frame = createFrame();
        try {
            LoginScreen login = find(frame.getContentPane(), LoginScreen.class);
            assertNotNull(login);
            List<FormField> fields = findAll(login, FormField.class);
            assertEquals(2, fields.size());
            AppButton submit = find(login, AppButton.class, button -> "Đăng nhập".equals(button.getText()));
            assertNotNull(submit);

            SwingUtilities.invokeAndWait(() -> {
                fields.get(0).setValue("admin");
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
        MainFrame frame = createFrame();
        try {
            LoginScreen login = find(frame.getContentPane(), LoginScreen.class);
            List<FormField> fields = findAll(login, FormField.class);
            AppButton submit = find(login, AppButton.class, button -> "Đăng nhập".equals(button.getText()));

            SwingUtilities.invokeAndWait(() -> {
                fields.get(0).setValue("admin");
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

    private static MainFrame createFrame() throws Exception {
        MainFrame[] holder = new MainFrame[1];
        SwingUtilities.invokeAndWait(() -> {
            MainFrame frame = new MainFrame();
            frame.addNotify();
            frame.setSize(1360, 900);
            frame.validate();
            holder[0] = frame;
        });
        return holder[0];
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
