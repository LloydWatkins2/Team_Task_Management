package com.java.teamtask.ui;

import com.java.teamtask.ui.components.AppButton;
import com.java.teamtask.ui.components.Avatar;
import com.java.teamtask.ui.components.Badge;
import com.java.teamtask.ui.components.EmptyState;
import com.java.teamtask.ui.components.FormField;
import com.java.teamtask.ui.components.IconButton;
import com.java.teamtask.ui.components.NavItem;
import com.java.teamtask.ui.components.SearchField;
import com.java.teamtask.ui.components.SelectableCard;
import com.java.teamtask.ui.components.TaskCard;
import com.java.teamtask.ui.theme.Palette;
import com.java.teamtask.ui.theme.Theme;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Kiểm tra số đo và khả năng render của từng component cốt lõi. */
class ComponentRenderTest {
    @BeforeAll
    static void installTheme() {
        Theme.install();
    }

    @Test
    void componentsFollowSpecifiedMetrics() throws Exception {
        Dimension button = onEdt(() -> new AppButton("Lưu", AppButton.Variant.PRIMARY).getPreferredSize());
        Dimension nav = onEdt(() -> new NavItem("Tổng quan", "dashboard").getPreferredSize());
        Dimension search = onEdt(() -> new SearchField().getPreferredSize());
        Dimension iconButton = onEdt(() -> new IconButton("bell", "Thông báo").getPreferredSize());
        Dimension avatar = onEdt(() -> new Avatar("Nguyễn An").getPreferredSize());
        Dimension badge = onEdt(() -> new Badge("Đang làm", Badge.Kind.WARNING).getPreferredSize());

        assertEquals(48, button.height);
        assertEquals(48, nav.height);
        assertEquals(48, search.height);
        assertEquals(new Dimension(48, 48), iconButton);
        assertEquals(new Dimension(48, 48), avatar);
        assertEquals(28, badge.height);
    }

    @Test
    void selectedNavItemPaintsAccentBackground() throws Exception {
        NavItem item = onEdt(() -> {
            NavItem navItem = new NavItem("Tổng quan", "dashboard");
            navItem.setSelected(true);
            return navItem;
        });
        BufferedImage image = render(item, 240, 48);
        int rgb = image.getRGB(232, 24);
        assertEquals(Palette.ACCENT.getRGB(), rgb, "NavItem active phải tô nền ACCENT");
    }

    @Test
    void componentsRenderToNonEmptyImages() throws Exception {
        JComponent[] components = onEdt(() -> new JComponent[]{
                new AppButton("Tạo công việc", AppButton.Variant.PRIMARY).withIcon("plus"),
                new AppButton("Huỷ", AppButton.Variant.OUTLINE),
                new NavItem("Dự án", "projects"),
                new SearchField(),
                new IconButton("bell", "Thông báo"),
                new Avatar("Nguyễn An"),
                new Badge("Hoàn thành", Badge.Kind.SUCCESS),
                new TaskCard("Thiết kế trang Tổng quan", "Wireframe và bố cục dashboard",
                        TaskCard.Status.IN_PROGRESS, "Website Redesign", "Nguyễn An",
                        "Hôm nay, 17:00", Palette.TASK_BLUE, "dashboard"),
                new SelectableCard("Website Redesign", "6 thành viên", () -> {
                }),
                new EmptyState("Chưa có công việc nào",
                        "Bắt đầu bằng cách tạo công việc đầu tiên hoặc khám phá các dự án.",
                        new AppButton("Tạo công việc", AppButton.Variant.PRIMARY))
        });

        int[][] sizes = {
                {240, 80}, {200, 80}, {240, 48}, {400, 48}, {48, 48}, {48, 48}, {140, 28},
                {860, 120}, {400, 100}, {400, 360}
        };
        for (int i = 0; i < components.length; i++) {
            BufferedImage image = render(components[i], sizes[i][0], sizes[i][1]);
            assertTrue(distinctColors(image) > 3,
                    components[i].getClass().getSimpleName() + " render gần như trống");
        }
    }

    @Test
    void formFieldsExposeValueAndErrorState() throws Exception {
        FormField text = onEdt(() -> FormField.text("Tên", "Nhập tên", "user"));
        FormField area = onEdt(() -> FormField.textArea("Mô tả", "Nhập mô tả", 96));
        FormField combo = onEdt(() -> FormField.combo("Trạng thái",
                new String[]{"Cần làm", "Đang làm"}, "flag"));

        onEdt(() -> {
            text.setValue("Thiết kế");
            area.setValue("Mô tả công việc");
            combo.setSelected("Đang làm");
            text.setError("Vui lòng nhập tên.");
        });

        assertEquals("Thiết kế", text.getValue());
        assertEquals("Mô tả công việc", area.getValue());
        assertEquals("Đang làm", combo.getValue());
        assertTrue(text.hasError());
        assertTrue(distinctColors(render(text, 400, 100)) > 3);

        onEdt(text::clearError);
        assertFalse(text.hasError());
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

    private static BufferedImage render(JComponent component, int width, int height) throws Exception {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        SwingUtilities.invokeAndWait(() -> {
            component.setSize(width, height);
            component.doLayout();
            Graphics2D g = image.createGraphics();
            try {
                component.paint(g);
            } finally {
                g.dispose();
            }
        });
        return image;
    }

    private static int distinctColors(BufferedImage image) {
        Set<Integer> colors = new HashSet<>();
        for (int y = 0; y < image.getHeight(); y += 3) {
            for (int x = 0; x < image.getWidth(); x += 3) {
                colors.add(image.getRGB(x, y));
                if (colors.size() > 16) {
                    return colors.size();
                }
            }
        }
        return colors.size();
    }
}
