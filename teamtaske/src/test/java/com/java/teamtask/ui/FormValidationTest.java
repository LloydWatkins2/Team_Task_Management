package com.java.teamtask.ui;

import com.java.teamtask.ui.components.AppButton;
import com.java.teamtask.ui.components.FormField;
import com.java.teamtask.ui.data.Validators;
import com.java.teamtask.ui.screens.ProjectFormDialog;
import com.java.teamtask.ui.screens.TaskCreateDialog;
import com.java.teamtask.ui.theme.Theme;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Kiểm tra validate dùng chung và hai hộp thoại dự án/công việc. */
class FormValidationTest {
    @BeforeAll
    static void installTheme() {
        Theme.install();
    }

    @Test
    void requiredMessageMatchesSpecification() {
        assertEquals("Please fill up this box!", Validators.REQUIRED);
    }

    @Test
    void datesAreParsedStrictly() {
        assertNull(Validators.parseDate("31/02/2026"), "Ngày không tồn tại phải bị từ chối");
        assertNull(Validators.parseDate("2026-03-02"), "Sai định dạng phải bị từ chối");
        assertNotNull(Validators.parseDate("02/03/2026"));
        assertEquals("02/03/2026", Validators.formatDate(LocalDate.of(2026, 3, 2)));
    }

    @Test
    void emptyProjectFormBlocksSaveAndShowsRequiredErrors() throws Exception {
        ProjectFormDialog.Values[] saved = new ProjectFormDialog.Values[1];
        ProjectFormDialog dialog = onEdt(() ->
                new ProjectFormDialog(null, "Tạo dự án mới", "", "", null, values -> saved[0] = values));
        List<FormField> fields = allFields(dialog);
        assertEquals(3, fields.size());
        AppButton save = findButton(dialog, "Lưu");

        SwingUtilities.invokeAndWait(save::doClick);

        assertNull(saved[0]);
        assertEquals(Validators.REQUIRED, fields.get(0).getErrorText());
        assertEquals(Validators.REQUIRED, fields.get(2).getErrorText());
    }

    @Test
    void projectFormRejectsPastDeadline() throws Exception {
        ProjectFormDialog.Values[] saved = new ProjectFormDialog.Values[1];
        ProjectFormDialog dialog = onEdt(() ->
                new ProjectFormDialog(null, "Tạo dự án mới", "", "", null, values -> saved[0] = values));
        List<FormField> fields = allFields(dialog);

        SwingUtilities.invokeAndWait(() -> {
            fields.get(0).setValue("Website mới");
            fields.get(2).setValue(Validators.formatDate(LocalDate.now().minusDays(1)));
            findButton(dialog, "Lưu").doClick();
        });

        assertNull(saved[0]);
        assertTrue(fields.get(2).hasError(), "Hạn chót trước hôm nay phải bị chặn");
    }

    @Test
    void validProjectFormSubmitsValues() throws Exception {
        ProjectFormDialog.Values[] saved = new ProjectFormDialog.Values[1];
        ProjectFormDialog dialog = onEdt(() ->
                new ProjectFormDialog(null, "Tạo dự án mới", "", "", null, values -> saved[0] = values));
        List<FormField> fields = allFields(dialog);
        String deadline = Validators.formatDate(LocalDate.now().plusDays(3));

        SwingUtilities.invokeAndWait(() -> {
            fields.get(0).setValue("Website mới");
            fields.get(1).setValue("Mô tả dự án");
            fields.get(2).setValue(deadline);
            findButton(dialog, "Lưu").doClick();
        });

        assertNotNull(saved[0]);
        assertEquals("Website mới", saved[0].name());
        assertEquals("Mô tả dự án", saved[0].description());
        assertEquals(LocalDate.now().plusDays(3), saved[0].deadline().toLocalDate());
    }

    @Test
    void taskFormRequiresNameDeadlineAndAssignee() throws Exception {
        TaskCreateDialog.Values[] saved = new TaskCreateDialog.Values[1];
        TaskCreateDialog dialog = onEdt(() ->
                new TaskCreateDialog(null, List.of(), values -> saved[0] = values));
        List<FormField> fields = allFields(dialog);
        assertEquals(5, fields.size());
        AppButton save = findButton(dialog, "Lưu");

        SwingUtilities.invokeAndWait(save::doClick);

        assertNull(saved[0]);
        assertEquals(Validators.REQUIRED, fields.get(0).getErrorText());
        assertEquals(Validators.REQUIRED, fields.get(2).getErrorText());
        assertTrue(fields.get(4).hasError(), "Thiếu người phụ trách phải báo lỗi");
    }

    @Test
    void validTaskFormSubmitsDbStatusAndAssignee() throws Exception {
        TestGateway gateway = new TestGateway();
        TaskCreateDialog.Values[] saved = new TaskCreateDialog.Values[1];
        TaskCreateDialog dialog = onEdt(() ->
                new TaskCreateDialog(null, List.of(gateway.anOwner), values -> saved[0] = values));
        List<FormField> fields = allFields(dialog);
        String deadline = Validators.formatDate(LocalDate.now().plusDays(2));

        SwingUtilities.invokeAndWait(() -> {
            fields.get(0).setValue("Việc mới");
            fields.get(1).setValue("Mô tả ngắn");
            fields.get(2).setValue(deadline);
            fields.get(3).setSelected("Đang làm");
            findButton(dialog, "Lưu").doClick();
        });

        assertNotNull(saved[0]);
        assertEquals("Việc mới", saved[0].name());
        assertEquals("Mô tả ngắn", saved[0].description());
        assertEquals("IN_PROGRESS", saved[0].status(), "UI phải lưu status enum tiếng Anh");
        assertEquals(LocalDate.now().plusDays(2), saved[0].deadline().toLocalDate());
        assertEquals(gateway.an, saved[0].assignee());
        assertFalse(fields.get(0).hasError());
    }

    private static List<FormField> allFields(JDialog dialog) {
        return findAll(dialog.getContentPane(), FormField.class);
    }

    private static AppButton findButton(Container root, String text) {
        return find(root, AppButton.class, button -> text.equals(button.getText()));
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

    private static <T> T onEdt(java.util.function.Supplier<T> supplier) throws Exception {
        Object[] result = new Object[1];
        SwingUtilities.invokeAndWait(() -> result[0] = supplier.get());
        @SuppressWarnings("unchecked")
        T typed = (T) result[0];
        return typed;
    }
}
