package com.java.teamtask.ui;

import com.java.teamtask.elements.members;
import com.java.teamtask.elements.owners;
import com.java.teamtask.ui.components.AppButton;
import com.java.teamtask.ui.components.FormField;
import com.java.teamtask.ui.data.UserSession;
import com.java.teamtask.ui.screens.ProfileScreen;
import com.java.teamtask.ui.screens.ProjectDetailScreen;
import com.java.teamtask.ui.theme.Theme;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Kiểm tra hành động theo role, vòng đời phiên dự án và cập nhật hồ sơ. */
class ProjectFlowTest {
    @BeforeAll
    static void installTheme() {
        Theme.install();
    }

    @Test
    void ownerSeesManagementActionsAndLeavingClearsActiveRole() throws Exception {
        TestGateway gateway = new TestGateway();
        UserSession session = new UserSession();
        session.setUser(gateway.an);
        int[] backCalls = new int[1];
        int[] changeCalls = new int[1];

        ProjectDetailScreen detail = onEdt(() -> {
            ProjectDetailScreen screen =
                    new ProjectDetailScreen(
                            gateway, session, () -> backCalls[0]++, () -> changeCalls[0]++);
            screen.open(gateway.anOwner);
            return screen;
        });

        assertInstanceOf(owners.class, session.getActiveRole(), "Owner phải dùng instance owners");
        assertTrue(session.hasActiveProject());
        assertNotNull(findButton(detail, "Sửa dự án"), "Owner được sửa dự án");
        assertNotNull(findButton(detail, "Thêm thành viên"), "Owner được thêm thành viên");
        assertNotNull(findButton(detail, "Xoá dự án"), "Owner được xoá dự án");
        assertNotNull(findButton(detail, "Tạo công việc"), "Owner được tạo công việc");

        AppButton back = findButton(detail, "Quay lại");
        assertNotNull(back);
        SwingUtilities.invokeAndWait(back::doClick);

        assertFalse(session.hasActiveProject(), "Rời dự án phải xoá ngay instance role");
        assertNull(session.getActiveRole());
        assertEquals(1, backCalls[0], "Rời dự án phải quay về Dashboard");
    }

    @Test
    void memberOnlySeesReadOnlyActions() throws Exception {
        TestGateway gateway = new TestGateway();
        UserSession session = new UserSession();
        session.setUser(gateway.an);

        ProjectDetailScreen detail = onEdt(() -> {
            ProjectDetailScreen screen =
                    new ProjectDetailScreen(gateway, session, () -> {}, () -> {});
            screen.open(gateway.anMember);
            return screen;
        });

        assertInstanceOf(members.class, session.getActiveRole(), "Member phải dùng instance members");
        assertNotNull(findButton(detail, "Quay lại"));
        assertNull(findButton(detail, "Sửa dự án"), "Member không được sửa dự án");
        assertNull(findButton(detail, "Thêm thành viên"), "Member không được thêm thành viên");
        assertNull(findButton(detail, "Xoá dự án"), "Member không được xoá dự án");
        assertNull(findButton(detail, "Tạo công việc"), "Member không được tạo công việc");
    }

    @Test
    void profileRequiresAuthenticationBeforeAnyChange() throws Exception {
        TestGateway gateway = new TestGateway();
        UserSession session = new UserSession();
        session.setUser(gateway.an);
        ProfileScreen profile = onEdt(() -> new ProfileScreen(gateway, session, () -> {}));

        List<FormField> fields = findAll(profile, FormField.class);
        assertEquals(7, fields.size(), "1 ô xác thực + 6 ô hồ sơ");
        AppButton update = findButton(profile, "Lưu thay đổi");
        assertNotNull(update);

        SwingUtilities.invokeAndWait(update::doClick);
        assertEquals(0, gateway.profileUpdateCalls, "Chưa xác thực thì không được cập nhật");
        assertTrue(labelText(profile).contains("Vui lòng xác thực tài khoản trước."));

        SwingUtilities.invokeAndWait(() -> {
            fields.get(0).setValue("sai-mat-khau");
            findButton(profile, "Xác thực").doClick();
        });
        assertTrue(fields.get(0).hasError(), "Sai mật khẩu xác thực phải báo lỗi");
        assertEquals(0, gateway.profileUpdateCalls);

        SwingUtilities.invokeAndWait(() -> {
            fields.get(0).setValue("123456");
            findButton(profile, "Xác thực").doClick();
        });
        assertFalse(fields.get(0).hasError());
        for (int i = 1; i < fields.size(); i++) {
            assertEquals("", fields.get(i).getValue(), "Form sửa hồ sơ phải bắt đầu rỗng");
        }

        SwingUtilities.invokeAndWait(update::doClick);
        assertEquals(0, gateway.profileUpdateCalls, "Không đổi trường nào thì không cập nhật");
        assertTrue(labelText(profile).contains("Vui lòng thay đổi ít nhất một trường."));
    }

    @Test
    void profileUpdatesOnlyChangedValidFields() throws Exception {
        TestGateway gateway = new TestGateway();
        UserSession session = new UserSession();
        session.setUser(gateway.an);
        ProfileScreen profile = onEdt(() -> new ProfileScreen(gateway, session, () -> {}));
        List<FormField> fields = findAll(profile, FormField.class);
        AppButton update = findButton(profile, "Lưu thay đổi");

        SwingUtilities.invokeAndWait(() -> {
            fields.get(0).setValue("123456");
            findButton(profile, "Xác thực").doClick();
        });

        SwingUtilities.invokeAndWait(() -> {
            fields.get(3).setValue("khong-phai-email");
            update.doClick();
        });
        assertTrue(fields.get(3).hasError(), "Gmail không hợp lệ phải bị chặn");
        assertEquals(0, gateway.profileUpdateCalls);

        SwingUtilities.invokeAndWait(() -> {
            fields.get(3).setValue("");
            fields.get(2).setValue("Bình");
            update.doClick();
        });

        assertEquals(1, gateway.profileUpdateCalls);
        assertNotNull(gateway.lastProfileChange);
        assertEquals("Bình", gateway.lastProfileChange.fname());
        assertNull(gateway.lastProfileChange.lname());
        assertNull(gateway.lastProfileChange.gmail());
        assertNull(gateway.lastProfileChange.phone());
        assertNull(gateway.lastProfileChange.dob());
        assertNull(gateway.lastProfileChange.newPassword());
        assertEquals("", fields.get(2).getValue(), "Sau khi lưu phải xoá trắng các ô");
        assertTrue(labelText(profile).contains("Cập nhật hồ sơ thành công."));
    }

    private static AppButton findButton(Container root, String text) {
        return find(root, AppButton.class, button -> text.equals(button.getText()));
    }

    private static String labelText(Container root) {
        StringBuilder text = new StringBuilder();
        for (JLabel label : findAll(root, JLabel.class)) {
            text.append(label.getText()).append('\n');
        }
        return text.toString();
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

    private static <T> T onEdt(Supplier<T> supplier) throws Exception {
        Object[] result = new Object[1];
        SwingUtilities.invokeAndWait(() -> result[0] = supplier.get());
        @SuppressWarnings("unchecked")
        T typed = (T) result[0];
        return typed;
    }
}
