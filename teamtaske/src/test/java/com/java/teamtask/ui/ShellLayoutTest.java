package com.java.teamtask.ui;

import com.java.teamtask.ui.components.AppButton;
import com.java.teamtask.ui.components.NavItem;
import com.java.teamtask.ui.components.Sidebar;
import com.java.teamtask.ui.screens.DashboardScreen;
import com.java.teamtask.ui.screens.LoginScreen;
import com.java.teamtask.ui.screens.TaskFormScreen;
import com.java.teamtask.ui.theme.Space;
import com.java.teamtask.ui.theme.Theme;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.awt.event.ComponentEvent;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Kiểm tra shell, breakpoint, sidebar và bề rộng form trong cửa sổ thật. */
class ShellLayoutTest {
    @BeforeAll
    static void setUp() {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(), "Cần môi trường có màn hình");
        Theme.install();
    }

    @Test
    void shellRespectsMaxWidthAndBreakpoints() throws Exception {
        MainFrame frame = createFrame(1360, 900, true);
        try {
            AppRootPanel appRoot = find(frame.getContentPane(), AppRootPanel.class);
            assertNotNull(appRoot);
            ShellPanel shell = appRoot.getShell();
            Sidebar sidebar = find(shell, Sidebar.class);
            JScrollPane scroll = find(shell, JScrollPane.class);
            DashboardScreen dashboard = find(shell, DashboardScreen.class);
            TaskFormScreen form = find(shell, TaskFormScreen.class);
            assertNotNull(sidebar);
            assertNotNull(scroll);
            assertNotNull(dashboard);
            assertNotNull(form);

            int contentWidth = frame.getContentPane().getWidth();
            assertEquals(Math.min(contentWidth - 56, 1280), shell.getWidth());
            assertEquals((contentWidth - shell.getWidth()) / 2, shell.getX(), 1);
            assertEquals(270, sidebar.getWidth());

            JComponent view = (JComponent) scroll.getViewport().getView();
            assertTrue(form.getPreferredSize().height > dashboard.getPreferredSize().height,
                    "Màn hình form phải cao hơn dashboard");
            assertEquals(dashboard.getPreferredSize().height, view.getPreferredSize().height,
                    "Vùng cuộn phải lấy chiều cao màn hình đang hiển thị");

            NavItem projectNav = findNav(sidebar, "Website Redesign");
            NavItem projectsNav = findNav(sidebar, "Dự án");
            assertNotNull(projectNav);
            assertNotNull(projectsNav);
            SwingUtilities.invokeAndWait(projectNav::doClick);
            assertTrue(projectNav.isSelected(), "Mục dự án vừa chọn phải giữ trạng thái active");
            assertFalse(projectsNav.isSelected());

            SwingUtilities.invokeAndWait(() -> {
                frame.setSize(1024, 700);
                frame.validate();
                frame.dispatchEvent(new ComponentEvent(frame, ComponentEvent.COMPONENT_RESIZED));
                frame.validate();
            });
            assertEquals(220, sidebar.getWidth(), "Dưới 1024px sidebar co còn 220px");
            contentWidth = frame.getContentPane().getWidth();
            assertEquals(Math.min(contentWidth - 40, 1280), shell.getWidth());
        } finally {
            SwingUtilities.invokeAndWait(frame::dispose);
        }
    }

    @Test
    void loginCardKeepsFixedWidthAndCenters() throws Exception {
        MainFrame frame = createFrame(1360, 900, false);
        try {
            LoginScreen login = find(frame.getContentPane(), LoginScreen.class);
            assertNotNull(login);
            Card card = find(login, Card.class);
            assertNotNull(card);
            assertEquals(480, card.getPreferredSize().width);
            assertTrue(card.getPreferredSize().height > 400);
            assertEquals(login.getWidth() / 2, card.getX() + card.getWidth() / 2, 2);

            AppButton submit = find(login, AppButton.class,
                    button -> "Đăng nhập".equals(button.getText()));
            assertNotNull(submit);
            int contentWidth = card.getWidth() - Space.X4L * 2;
            assertEquals(Space.X4L,
                    SwingUtilities.convertPoint(submit, 0, 0, card).x,
                    "Nội dung login phải căn trái tại card.x + 32");
            assertEquals(contentWidth, submit.getWidth(),
                    "Nút đăng nhập block phải phủ hết bề rộng nội dung 416px");
        } finally {
            SwingUtilities.invokeAndWait(frame::dispose);
        }
    }

    @Test
    void taskFormCardWidthIsCapped() throws Exception {
        TaskFormScreen form = onEdt(() -> new TaskFormScreen(() -> {
        }, task -> {
        }));
        Card card = find(form, Card.class);
        assertNotNull(card);
        assertEquals(640, card.getPreferredSize().width);

        onEdt(() -> form.setMaxWidth(420));
        assertEquals(420, card.getPreferredSize().width);

        onEdt(() -> form.setMaxWidth(2000));
        assertEquals(640, card.getPreferredSize().width);

        onEdt(() -> form.setMaxWidth(100));
        assertEquals(320, card.getPreferredSize().width);
    }

    private static MainFrame createFrame(int width, int height, boolean showApp) throws Exception {
        MainFrame[] holder = new MainFrame[1];
        SwingUtilities.invokeAndWait(() -> {
            MainFrame frame = new MainFrame();
            frame.addNotify();
            frame.setSize(width, height);
            frame.validate();
            if (showApp) {
                frame.showApp();
                frame.validate();
            }
            holder[0] = frame;
        });
        return holder[0];
    }

    private static NavItem findNav(Container root, String text) {
        return find(root, NavItem.class, nav -> text.equals(nav.getText()));
    }

    private static <T extends Component> T find(Container root, Class<T> type) {
        return find(root, type, value -> true);
    }

    private static <T extends Component> T find(Container root, Class<T> type,
                                               java.util.function.Predicate<T> filter) {
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

    private static <T> T onEdt(Supplier<T> supplier) throws Exception {
        Object[] result = new Object[1];
        SwingUtilities.invokeAndWait(() -> result[0] = supplier.get());
        @SuppressWarnings("unchecked")
        T typed = (T) result[0];
        return typed;
    }

    private static void onEdt(Runnable runnable) throws Exception {
        SwingUtilities.invokeAndWait(runnable);
    }
}
