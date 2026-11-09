package com.java.teamtask.ui;

import com.java.teamtask.elements.projectsAssignments;
import com.java.teamtask.elements.users;
import com.java.teamtask.ui.components.AppButton;
import com.java.teamtask.ui.components.EmptyState;
import com.java.teamtask.ui.components.PageHeader;
import com.java.teamtask.ui.components.Sidebar;
import com.java.teamtask.ui.components.TopBar;
import com.java.teamtask.ui.data.SessionGateway;
import com.java.teamtask.ui.data.UiMapper;
import com.java.teamtask.ui.data.UserSession;
import com.java.teamtask.ui.screens.DashboardScreen;
import com.java.teamtask.ui.screens.LoginScreen;
import com.java.teamtask.ui.screens.ProfileScreen;
import com.java.teamtask.ui.screens.ProjectDetailScreen;
import com.java.teamtask.ui.screens.RegisterScreen;
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

/** Cửa sổ chính: login/register ↔ app shell, sidebar + topbar + nội dung DB-backed. */
public final class MainFrame extends JFrame {
  private final SessionGateway gateway;
  private final UserSession session = new UserSession();
  private final CardLayout rootLayout = new CardLayout();
  private final JPanel root = new JPanel(rootLayout);
  private final CardLayout contentLayout = new CardLayout();
  private final ScrollableContent content = new ScrollableContent(contentLayout);

  private LoginScreen login;
  private RegisterScreen register;
  private Sidebar sidebar;
  private TopBar topBar;
  private MainSurface mainSurface;
  private JScrollPane scroll;
  private DashboardScreen dashboard;
  private ProjectDetailScreen detail;
  private ProfileScreen profile;
  private JPanel placeholderScreen;
  private PageHeader placeholderHeader;
  private int lastWidth = -1;
  private boolean appBuilt;

  public MainFrame(SessionGateway gateway) {
    super("Team Task Managements");
    this.gateway = gateway;
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(1360, 900);
    setMinimumSize(new Dimension(Layout.BP_MD, 700));
    setLocationRelativeTo(null);
    setContentPane(root);

    login = new LoginScreen(gateway, this::signIn, this::showRegister);
    register = new RegisterScreen(gateway, this::signIn, this::showLogin);
    root.add(login, "login");
    root.add(register, "register");

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
    sidebar = new Sidebar("", this::navigate);
    topBar = new TopBar("");

    dashboard =
        new DashboardScreen(gateway, session, this::openProject, this::syncProjects);
    detail =
        new ProjectDetailScreen(gateway, session, () -> navigate("dashboard"), this::syncProjects);
    profile = new ProfileScreen(gateway, session, () -> navigate("dashboard"));

    content.add(dashboard, "dashboard");
    content.add(detail, "detail");
    content.add(profile, "profile");
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

  /** Đăng nhập/đăng ký thành công: lưu user đầy đủ từ DB và mở Dashboard. */
  public void signIn(users user) {
    session.setUser(user);
    syncProjects();
    dashboard.refresh();
    showApp();
  }

  void showApp() {
    rootLayout.show(root, "app");
    sidebar.select("dashboard");
    contentLayout.show(content, "dashboard");
    content.setCurrentScreen(dashboard);
    scrollToTop();
    applyResponsive(getContentPane().getWidth());
  }

  private void showRegister() {
    register.reset();
    rootLayout.show(root, "register");
  }

  private void showLogin() {
    login.reset();
    rootLayout.show(root, "login");
  }

  private void navigate(String key) {
    if ("logout".equals(key)) {
      logout();
      return;
    }
    if (key != null && key.startsWith("project:")) {
      openProjectById(key.substring("project:".length()));
      return;
    }
    if ("dashboard".equals(key) || "tasks".equals(key) || "projects".equals(key)) {
      leaveProject();
      contentLayout.show(content, "dashboard");
      content.setCurrentScreen(dashboard);
      sidebar.select("tasks".equals(key) ? "tasks" : "dashboard");
    } else if ("settings".equals(key)) {
      leaveProject();
      profile.onShown();
      contentLayout.show(content, "profile");
      content.setCurrentScreen(profile);
      sidebar.select("settings");
    } else {
      leaveProject();
      String title =
          switch (key) {
            case "calendar" -> "Lịch";
            case "team" -> "Thành viên";
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

  private void openProject(projectsAssignments assignment) {
    if (assignment == null || assignment.getProject() == null) {
      navigate("dashboard");
      return;
    }
    detail.open(assignment);
    contentLayout.show(content, "detail");
    content.setCurrentScreen(detail);
    sidebar.select("project:" + assignment.getProject().getId());
    scrollToTop();
    content.revalidate();
    content.repaint();
  }

  private void openProjectById(String idText) {
    int projectId;
    try {
      projectId = Integer.parseInt(idText);
    } catch (NumberFormatException ex) {
      navigate("dashboard");
      return;
    }
    projectsAssignments found = findProject(projectId);
    if (found == null && session.isAuthenticated()) {
      dashboard.refresh();
      found = findProject(projectId);
    }
    if (found == null) {
      navigate("dashboard");
      return;
    }
    openProject(found);
  }

  private projectsAssignments findProject(int projectId) {
    if (!session.isAuthenticated()) {
      return null;
    }
    for (projectsAssignments assignment : session.getUser().getRelatedProjects().values()) {
      if (assignment.getProject() != null && assignment.getProject().getId() == projectId) {
        return assignment;
      }
    }
    return null;
  }

  private void leaveProject() {
    if (session.hasActiveProject()) {
      session.clearActiveProject();
    }
  }

  private void logout() {
    session.logout();
    sidebar.setProjects(List.of());
    sidebar.setUserName("");
    topBar.setUserName("");
    showLogin();
  }

  /** Đồng bộ danh sách dự án và tên người dùng trên sidebar/topbar từ session hiện tại. */
  private void syncProjects() {
    if (!session.isAuthenticated()) {
      sidebar.setProjects(List.of());
      sidebar.setUserName("");
      topBar.setUserName("");
      return;
    }
    users user = session.getUser();
    String name = UiMapper.displayName(user);
    sidebar.setUserName(name);
    topBar.setUserName(name);
    sidebar.setProjects(new ArrayList<>(user.getRelatedProjects().values()));
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
    detail.setCompact(Layout.isStacked(width));

    java.awt.Insets main = Layout.mainPadding(width);

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
