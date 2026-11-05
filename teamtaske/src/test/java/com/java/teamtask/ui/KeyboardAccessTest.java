package com.java.teamtask.ui;

import com.java.teamtask.ui.components.AppButton;
import com.java.teamtask.ui.components.IconButton;
import com.java.teamtask.ui.components.NavItem;
import com.java.teamtask.ui.components.Sidebar;
import com.java.teamtask.ui.theme.Theme;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Kiểm tra phím Enter kích hoạt nút/nav và nút đăng xuất đủ lớn, có tên trợ năng. */
class KeyboardAccessTest {
    @BeforeAll
    static void installTheme() {
        Theme.install();
    }

    @Test
    void appButtonActivatesWithEnter() throws Exception {
        AtomicInteger clicks = new AtomicInteger();
        AppButton button = onEdt(() -> {
            AppButton value = new AppButton("Lưu", AppButton.Variant.PRIMARY);
            value.addActionListener(e -> clicks.incrementAndGet());
            return value;
        });
        pressEnter(button);
        assertEquals(1, clicks.get());
    }

    @Test
    void navItemActivatesWithEnter() throws Exception {
        AtomicInteger clicks = new AtomicInteger();
        NavItem item = onEdt(() -> {
            NavItem value = new NavItem("Tổng quan", "dashboard");
            value.addActionListener(e -> clicks.incrementAndGet());
            return value;
        });
        pressEnter(item);
        assertEquals(1, clicks.get());
    }

    @Test
    void sidebarLogoutIsFocusableIconButtonWithAccessibleName() throws Exception {
        Sidebar sidebar = onEdt(() -> new Sidebar("Nguyễn An", key -> {
        }));
        IconButton logout = find(sidebar, IconButton.class);
        assertNotNull(logout, "Sidebar phải có nút đăng xuất dạng IconButton");
        assertEquals("Đăng xuất", logout.getToolTipText());
        assertNotNull(logout.getAccessibleContext().getAccessibleName());
        assertEquals(new Dimension(44, 44), logout.getPreferredSize());
        assertTrue(logout.isFocusable(), "Nút đăng xuất phải nhận focus bàn phím");
    }

    private static void pressEnter(JComponent component) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            Object binding = component.getInputMap(JComponent.WHEN_FOCUSED)
                    .get(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
            assertNotNull(binding, "Thiếu binding Enter");
            Action action = component.getActionMap().get(binding);
            assertNotNull(action, "Thiếu action cho binding Enter");
            assertTrue(action instanceof AbstractAction);
            action.actionPerformed(new ActionEvent(component, ActionEvent.ACTION_PERFORMED, "Enter"));
        });
    }

    private static <T extends Component> T find(Container root, Class<T> type) {
        if (type.isInstance(root)) {
            return type.cast(root);
        }
        for (Component child : root.getComponents()) {
            if (child instanceof Container container) {
                T found = find(container, type);
                if (found != null) {
                    return found;
                }
            } else if (type.isInstance(child)) {
                return type.cast(child);
            }
        }
        return null;
    }

    private static <T> T onEdt(Supplier<T> supplier) throws Exception {
        Object[] result = new Object[1];
        SwingUtilities.invokeAndWait(() -> result[0] = supplier.get());
        @SuppressWarnings("unchecked")
        T typed = (T) result[0];
        return typed;
    }
}
