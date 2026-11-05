package com.java.teamtask.ui;

import com.java.teamtask.ui.sample.SampleData;
import com.java.teamtask.ui.screens.CommentsPanel;
import com.java.teamtask.ui.screens.DashboardScreen;
import com.java.teamtask.ui.screens.ProjectsScreen;
import com.java.teamtask.ui.screens.TaskFormScreen;
import com.java.teamtask.ui.theme.Theme;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Render các màn hình ra ảnh để kiểm tra trực quan và bắt lỗi layout/paint. */
class UiSnapshotTest {
    private static final File OUT = new File("target/ui-screenshots");

    @BeforeAll
    static void setUp() {
        Theme.install();
        OUT.mkdirs();
    }

    @Test
    void loginAndAppShellRender() throws Exception {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(), "Cần môi trường có màn hình");

        MainFrame[] frame = new MainFrame[1];
        SwingUtilities.invokeAndWait(() -> {
            MainFrame f = new MainFrame();
            f.addNotify();
            f.setSize(1360, 900);
            f.validate();
            frame[0] = f;
        });

        BufferedImage login = render(frame[0].getContentPane(), 1360, 900, "login-1360");
        assertDiverse(login);

        SwingUtilities.invokeAndWait(() -> {
            frame[0].showApp();
            frame[0].validate();
        });
        BufferedImage app = render(frame[0].getContentPane(), 1360, 900, "app-1360");
        assertDiverse(app);

        SwingUtilities.invokeAndWait(() -> {
            frame[0].setSize(1024, 700);
            frame[0].validate();
        });
        BufferedImage compact = render(frame[0].getContentPane(), 1024, 700, "app-1024");
        assertDiverse(compact);

        SwingUtilities.invokeAndWait(() -> frame[0].dispose());
    }

    @Test
    void screensRender() throws Exception {
        TaskFormScreen form = onEdt(() -> new TaskFormScreen(() -> {
        }, task -> {
        }));
        assertDiverse(render(form, 960, 820, "task-form-960"));

        DashboardScreen dashboard = onEdt(() -> new DashboardScreen(SampleData.tasks(),
                () -> {
                }, () -> {
                }, task -> {
                }));
        assertDiverse(render(dashboard, 960, 820, "dashboard-960"));

        ProjectsScreen projects = onEdt(() -> new ProjectsScreen(() -> {
        }));
        assertDiverse(render(projects, 960, 820, "projects-960"));

        CommentsPanel comments = onEdt(() -> new CommentsPanel(SampleData.CURRENT_USER, SampleData.comments()));
        assertDiverse(render(comments, 600, 420, "comments-600"));
    }

    private static <T extends JComponent> T onEdt(java.util.function.Supplier<T> supplier) throws Exception {
        final JComponent[] result = new JComponent[1];
        SwingUtilities.invokeAndWait(() -> result[0] = supplier.get());
        @SuppressWarnings("unchecked")
        T typed = (T) result[0];
        return typed;
    }

    private static BufferedImage render(Component component, int width, int height, String name) throws Exception {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        SwingUtilities.invokeAndWait(() -> {
            component.setSize(width, height);
            layoutTree(component);
            Graphics2D g = image.createGraphics();
            try {
                component.paint(g);
            } finally {
                g.dispose();
            }
        });
        ImageIO.write(image, "png", new File(OUT, name + ".png"));
        return image;
    }

    private static void layoutTree(Component component) {
        if (component instanceof Container container) {
            container.doLayout();
            for (Component child : container.getComponents()) {
                layoutTree(child);
            }
        }
    }

    private static void assertDiverse(BufferedImage image) {
        Set<Integer> colors = new HashSet<>();
        for (int y = 0; y < image.getHeight(); y += 7) {
            for (int x = 0; x < image.getWidth(); x += 7) {
                colors.add(image.getRGB(x, y));
                if (colors.size() > 12) {
                    return;
                }
            }
        }
        assertTrue(colors.size() > 12, "Ảnh render gần như trống");
    }
}
