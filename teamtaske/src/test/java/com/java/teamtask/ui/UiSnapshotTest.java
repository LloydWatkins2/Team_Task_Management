package com.java.teamtask.ui;

import com.java.teamtask.ui.data.CommentView;
import com.java.teamtask.ui.data.UserSession;
import com.java.teamtask.ui.screens.CommentsPanel;
import com.java.teamtask.ui.screens.DashboardScreen;
import com.java.teamtask.ui.screens.LoginScreen;
import com.java.teamtask.ui.screens.ProfileScreen;
import com.java.teamtask.ui.screens.ProjectDetailScreen;
import com.java.teamtask.ui.screens.RegisterScreen;
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
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Render các màn hình DB-backed ra ảnh để kiểm tra trực quan và bắt lỗi layout/paint. */
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

        TestGateway gateway = new TestGateway();
        MainFrame[] frame = new MainFrame[1];
        SwingUtilities.invokeAndWait(() -> {
            MainFrame f = new MainFrame(gateway);
            f.addNotify();
            f.setSize(1360, 900);
            f.validate();
            frame[0] = f;
        });

        BufferedImage login = render(frame[0].getContentPane(), 1360, 900, "login-1360");
        assertDiverse(login);

        SwingUtilities.invokeAndWait(() -> {
            frame[0].signIn(gateway.an);
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

        SwingUtilities.invokeAndWait(frame[0]::dispose);
    }

    @Test
    void screensRender() throws Exception {
        TestGateway gateway = new TestGateway();
        UserSession session = onEdt(() -> {
            UserSession value = new UserSession();
            value.setUser(gateway.an);
            return value;
        });

        LoginScreen login = onEdt(() -> new LoginScreen(gateway, user -> {}, () -> {}));
        assertDiverse(render(login, 960, 820, "login-960"));

        RegisterScreen register = onEdt(() -> new RegisterScreen(gateway, user -> {}, () -> {}));
        assertDiverse(render(register, 960, 820, "register-960"));

        DashboardScreen dashboard =
                onEdt(() -> new DashboardScreen(gateway, session, assignment -> {}, () -> {}));
        assertDiverse(render(dashboard, 960, 820, "dashboard-960"));

        ProjectDetailScreen detail = onEdt(() -> {
            ProjectDetailScreen screen =
                    new ProjectDetailScreen(gateway, session, () -> {}, () -> {});
            screen.open(gateway.anOwner);
            return screen;
        });
        assertDiverse(render(detail, 960, 900, "project-detail-960"));

        ProfileScreen profile = onEdt(() -> new ProfileScreen(gateway, session, () -> {}));
        assertDiverse(render(profile, 960, 820, "profile-960"));

        CommentsPanel comments = onEdt(() ->
                new CommentsPanel(
                        List.of(
                                new CommentView("Nguyễn An", "Hôm nay", "Bình luận mẫu"),
                                new CommentView("Trần Minh", "Hôm qua", "Đã xem, sẽ phản hồi sớm.")),
                        text -> {}));
        assertDiverse(render(comments, 600, 420, "comments-600"));
    }

    private static <T> T onEdt(Supplier<T> supplier) throws Exception {
        Object[] result = new Object[1];
        SwingUtilities.invokeAndWait(() -> result[0] = supplier.get());
        @SuppressWarnings("unchecked")
        T typed = (T) result[0];
        return typed;
    }

    private static BufferedImage render(Component component, int width, int height, String name)
            throws Exception {
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
