package com.java.teamtask.ui;

import com.java.teamtask.ui.components.AppButton;
import com.java.teamtask.ui.components.FormField;
import com.java.teamtask.ui.sample.TaskItem;
import com.java.teamtask.ui.screens.TaskFormScreen;
import com.java.teamtask.ui.theme.Theme;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Kiểm tra validate inline và tạo công việc của form. */
class FormValidationTest {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @BeforeAll
    static void installTheme() {
        Theme.install();
    }

    @Test
    void emptyFormBlocksSaveAndShowsErrors() throws Exception {
        TaskItem[] saved = new TaskItem[1];
        TaskFormScreen form = onEdt(() -> new TaskFormScreen(() -> {
        }, task -> saved[0] = task));
        List<FormField> fields = findAll(form, FormField.class);
        assertEquals(7, fields.size());
        AppButton save = find(form, AppButton.class, button -> "Lưu".equals(button.getText()));
        assertNotNull(save);

        SwingUtilities.invokeAndWait(save::doClick);

        assertNull(saved[0], "Form rỗng không được lưu");
        assertTrue(fields.get(0).hasError(), "Thiếu tên công việc phải báo lỗi");
        assertTrue(fields.get(2).hasError(), "Thiếu hạn chót phải báo lỗi");
    }

    @Test
    void invalidDeadlineIsRejected() throws Exception {
        TaskItem[] saved = new TaskItem[1];
        TaskFormScreen form = onEdt(() -> new TaskFormScreen(() -> {
        }, task -> saved[0] = task));
        List<FormField> fields = findAll(form, FormField.class);
        AppButton save = find(form, AppButton.class, button -> "Lưu".equals(button.getText()));

        SwingUtilities.invokeAndWait(() -> {
            fields.get(0).setValue("Việc mới");
            fields.get(2).setValue("31/02/2026");
            save.doClick();
        });

        assertNull(saved[0]);
        assertTrue(fields.get(2).hasError());
    }

    @Test
    void validFormCreatesTaskAndResets() throws Exception {
        TaskItem[] saved = new TaskItem[1];
        TaskFormScreen form = onEdt(() -> new TaskFormScreen(() -> {
        }, task -> saved[0] = task));
        List<FormField> fields = findAll(form, FormField.class);
        AppButton save = find(form, AppButton.class, button -> "Lưu".equals(button.getText()));
        String deadline = LocalDate.now().plusDays(2).format(DATE);

        SwingUtilities.invokeAndWait(() -> {
            fields.get(0).setValue("Việc mới");
            fields.get(1).setValue("Mô tả ngắn");
            fields.get(2).setValue(deadline);
            save.doClick();
        });

        assertNotNull(saved[0]);
        assertEquals("Việc mới", saved[0].title());
        assertEquals(deadline, saved[0].deadline());
        assertFalse(fields.get(0).hasError());
        assertEquals("", fields.get(0).getValue(), "Form phải được reset sau khi lưu");
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
