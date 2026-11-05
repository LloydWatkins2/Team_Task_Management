package com.java.teamtask.ui;

import com.java.teamtask.ui.components.AppButton;
import com.java.teamtask.ui.components.EmptyState;
import com.java.teamtask.ui.components.PageHeader;
import com.java.teamtask.ui.components.Sidebar;
import com.java.teamtask.ui.components.TopBar;
import com.java.teamtask.ui.sample.SampleData;
import com.java.teamtask.ui.sample.TaskItem;
import com.java.teamtask.ui.screens.DashboardScreen;
import com.java.teamtask.ui.screens.LoginScreen;
import com.java.teamtask.ui.screens.ProjectsScreen;
import com.java.teamtask.ui.screens.TaskDetailDialog;
import com.java.teamtask.ui.screens.TaskFormScreen;
import com.java.teamtask.ui.theme.Layout;
import com.java.teamtask.ui.theme.Painters;
import com.java.teamtask.ui.theme.Palette;
import com.java.teamtask.ui.theme.Radius;
import com.java.teamtask.ui.theme.Space;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

/** Cửa sổ chính: CardLayout login ↔ app shell, sidebar + topbar + nội dung. */
public final class MainFrame extends JFrame {
  private final CardLayout rootLayout = new CardLayout();
  private final JPanel root = new JPanel(rootLayout);
  private final CardLayout contentLayout = new CardLayout();
  private final ScrollableContent content = new ScrollableContent(contentLayout);
  private final List<TaskItem> tasks = new ArrayList<>(SampleData.tasks());

  private Sidebar sidebar;
  private TopBar topBar;
  private MainSurface mainSurface;
  private JScrollPane scroll;
  private DashboardScreen dashboard;
  private ProjectsScreen projects;
  private TaskFormScreen taskForm;
  private JPanel placeholderScreen;
  private PageHeader placeholderHeader;
  private int lastWidth = -1;
  private boolean appBuilt;

  public MainFrame() {
    super("Team Task Managements");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(1360, 900);
    setMinimumSize(new Dimension(Layout.BP_MD, 700));
    setLocationRelativeTo(null);
    setContentPane(root);

    LoginScreen login = new LoginScreen(this::showApp);
    root.add(login, "login");

    buildApp();
    rootLayout.show(root, "login");

    addComponentListener(
        new ComponentAdapter() {
          @Override
          public void componentResized(ComponentEvent e) {
            applyResponsive(getContentPane().getWidth());
          }
        });
  }

  private void buildApp() {
    sidebar = new Sidebar(SampleData.CURRENT_USER, this::navigate);
    topBar = new TopBar(SampleData.CURRENT_USER);

    dashboard =
        new DashboardScreen(
            tasks,
            this::showForm,
            () -> navigate("projects"),
            task ->
                new TaskDetailDialog(this, task, SampleData.comments(), SampleData.CURRENT_USER)
                    .setVisible(true));
    projects = new ProjectsScreen(() -> navigate("dashboard"));
    taskForm = new TaskFormScreen(() -> navigate("dashboard"), this::saveTask);

    content.add(dashboard, "dashboard");
    content.add(projects, "projects");
    content.add(taskForm, "form");
    content.add(placeholder(), "placeholder");
    scroll = new JScrollPane(content);
    scroll.setBorder(null);
    scroll.setOpaque(false);
    scroll.getViewport().setOpaque(false);
    scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    scroll.getVerticalScrollBar().setUnitIncrement(18);

    mainSurface = new MainSurface(new BorderLayout());
    JPanel topWrapper = new JPanel(new BorderLayout());
    topWrapper.setOpaque(false);
    topWrapper.setBorder(new EmptyBorder(0, 0, Space.X5L, 0));
    topWrapper.add(topBar, BorderLayout.CENTER);
    mainSurface.add(topWrapper, BorderLayout.NORTH);
    mainSurface.add(scroll, BorderLayout.CENTER);

    ShellPanel shell = new ShellPanel();
    shell.add(sidebar, BorderLayout.WEST);
    shell.add(mainSurface, BorderLayout.CENTER);

    AppRootPanel appRoot = new AppRootPanel(shell);
    root.add(appRoot, "app");
    content.setCurrentScreen(dashboard);
    appBuilt = true;
    applyResponsive(1360);
  }

  private JPanel placeholder() {
    JPanel panel = new JPanel(new BorderLayout());
    placeholderScreen = panel;
    panel.setOpaque(false);
    placeholderHeader =
        new PageHeader("Tính năng", "Mô-đun này sẽ được hoàn thiện ở giai đoạn tiếp theo.", null);
    panel.add(placeholderHeader, BorderLayout.NORTH);

    JPanel body = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, Space.X6L + Space.XXL));
    body.setOpaque(false);
    AppButton back = new AppButton("Về tổng quan", AppButton.Variant.OUTLINE).withIcon("dashboard");
    back.addActionListener(e -> navigate("dashboard"));
    body.add(
        new EmptyState(
            "Sắp ra mắt",
            "Chúng tôi đang hoàn thiện màn hình này. Trong lúc chờ, bạn có thể quay lại tổng quan.",
            back));
    panel.add(body, BorderLayout.CENTER);
    return panel;
  }

  void showApp() {
    rootLayout.show(root, "app");
    sidebar.select("dashboard");
    contentLayout.show(content, "dashboard");
    content.setCurrentScreen(dashboard);
    scrollToTop();
    applyResponsive(getContentPane().getWidth());
  }

  private void navigate(String key) {
    if ("logout".equals(key)) {
      rootLayout.show(root, "login");
      return;
    }
    if ("dashboard".equals(key) || "tasks".equals(key)) {
      contentLayout.show(content, "dashboard");
      content.setCurrentScreen(dashboard);
      sidebar.select(key);
    } else if ("projects".equals(key) || key.startsWith("project:")) {
      contentLayout.show(content, "projects");
      content.setCurrentScreen(projects);
      sidebar.select(key);
    } else {
      String title =
          switch (key) {
            case "calendar" -> "Lịch";
            case "team" -> "Thành viên";
            case "settings" -> "Cài đặt";
            default -> "Tính năng";
          };
      placeholderHeader.setTitle(title);
      placeholderHeader.setSubtitle("Mô-đun này sẽ được hoàn thiện ở giai đoạn tiếp theo.");
      contentLayout.show(content, "placeholder");
      content.setCurrentScreen(placeholderScreen);
      sidebar.select(key);
    }
    scrollToTop();
    content.revalidate();
    content.repaint();
  }

  private void showForm() {
    taskForm.reset();
    contentLayout.show(content, "form");
    content.setCurrentScreen(taskForm);
    scrollToTop();
    content.revalidate();
    content.repaint();
  }

  private void saveTask(TaskItem task) {
    tasks.add(task);
    dashboard.setTasks(tasks);
    navigate("dashboard");
  }

  private void scrollToTop() {
    if (scroll != null) {
      SwingUtilities.invokeLater(() -> scroll.getVerticalScrollBar().setValue(0));
    }
  }

  private void applyResponsive(int width) {
    if (!appBuilt || width <= 0 || width == lastWidth) {
      return;
    }
    lastWidth = width;
    boolean compact = Layout.isCompact(width);
    sidebar.setCompact(compact);
    topBar.setCompact(compact);
    dashboard.setCompact(Layout.isStacked(width));
    projects.setCompact(width < Layout.BP_MD);
    taskForm.setCompact(Layout.isStacked(width));

    java.awt.Insets page = Layout.pagePadding(width);
    java.awt.Insets main = Layout.mainPadding(width);
    int shellWidth = Math.min(width - page.left - page.right, Layout.SHELL_MAX_WIDTH);
    int sidebarWidth = compact ? Layout.SIDEBAR_WIDTH_COMPACT : Layout.SIDEBAR_WIDTH;
    int available = shellWidth - sidebarWidth - main.left - main.right - 14;
    taskForm.setMaxWidth(available);

    mainSurface.setBorder(new EmptyBorder(main));
    mainSurface.revalidate();
    mainSurface.repaint();
  }

  /** Nền nội dung bo trái 48px theo spec shell. */
  private static final class MainSurface extends JPanel {
    MainSurface(LayoutManager layout) {
      super(layout);
      setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
      Graphics2D g2 = (Graphics2D) g.create();
      Painters.antialias(g2);
      int w = getWidth();
      int h = getHeight();
      int r = Radius.XL;
      Path2D path = new Path2D.Float();
      path.moveTo(0, r);
      path.quadTo(0, 0, r, 0);
      path.lineTo(w, 0);
      path.lineTo(w, h);
      path.lineTo(r, h);
      path.quadTo(0, h, 0, h - r);
      path.closePath();
      g2.setColor(Palette.CONTENT);
      g2.fill(path);
      g2.dispose();
    }
  }

  /** Panel cuộn luôn rộng bằng viewport để nội dung không bị co. */
  private static final class ScrollableContent extends JPanel implements Scrollable {
    private Component currentScreen;

    ScrollableContent(LayoutManager layout) {
      super(layout);
      setOpaque(false);
    }

    void setCurrentScreen(Component screen) {
      currentScreen = screen;
      revalidate();
    }

    @Override
    public Dimension getPreferredSize() {
      if (currentScreen != null) {
        return currentScreen.getPreferredSize();
      }
      return super.getPreferredSize();
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
      return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
      return 18;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
      return Math.max(visibleRect.height - 40, 40);
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
      return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
      return getParent() instanceof JViewport viewport
          && viewport.getHeight() > getPreferredSize().height;
    }
  }
}
