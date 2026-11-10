package com.java.teamtask.ui;

import com.java.teamtask.ui.components.NavItem;
import com.java.teamtask.ui.components.ProjectCard;
import com.java.teamtask.ui.components.SearchField;
import com.java.teamtask.ui.components.TopBar;
import com.java.teamtask.ui.screens.DashboardScreen;
import com.java.teamtask.ui.screens.ProjectDetailScreen;
import com.java.teamtask.ui.theme.Theme;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Kiểm tra tìm kiếm dự án trên Dashboard và việc làm mới sau khi xoá dự án. */
class SearchFlowTest {
  @BeforeAll
  static void setUp() {
    Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(), "Cần môi trường có màn hình");
    Theme.install();
  }

  @Test
  void typingFromProjectDetailReturnsToFilteredDashboard() throws Exception {
    TestGateway gateway = new TestGateway();
    MainFrame frame = createFrame(gateway);
    try {
      DashboardScreen dashboard = find(frame.getContentPane(), DashboardScreen.class);
      TopBar topBar = find(frame.getContentPane(), TopBar.class);
      SearchField search = find(frame.getContentPane(), SearchField.class);
      assertNotNull(dashboard);
      assertNotNull(topBar);
      assertNotNull(search);

      NavItem projectNav = findNav(frame.getContentPane(), "Website Redesign");
      SwingUtilities.invokeAndWait(projectNav::doClick);
      ProjectDetailScreen detail = find(frame.getContentPane(), ProjectDetailScreen.class);
      assertTrue(detail.isVisible(), "Phải mở được chi tiết dự án trước khi tìm kiếm");

      SwingUtilities.invokeAndWait(() -> {
        search.setText("mobile");
        topBar.flushSearch();
      });

      assertTrue(dashboard.isVisible(), "Nhập từ khoá phải quay về Dashboard");
      assertFalse(detail.isVisible());
      assertEquals(1, projectCards(frame).size(), "Chỉ còn dự án khớp từ khoá");
      assertTrue(labelText(projectCards(frame).get(0)).contains("Mobile App"));

      SwingUtilities.invokeAndWait(() -> {
        search.setText("khong-co-du-an-nao");
        topBar.flushSearch();
      });
      assertTrue(projectCards(frame).isEmpty());
      assertTrue(labelText(dashboard).contains("Không tìm thấy dự án"));

      SwingUtilities.invokeAndWait(topBar::clearSearch);
      assertEquals(2, projectCards(frame).size(), "Xoá từ khoá phải hiện lại toàn bộ dự án");
    } finally {
      SwingUtilities.invokeAndWait(frame::dispose);
    }
  }

  @Test
  void leavingDeletedProjectRefreshesDashboardImmediately() throws Exception {
    TestGateway gateway = new TestGateway();
    MainFrame frame = createFrame(gateway);
    try {
      NavItem projectNav = findNav(frame.getContentPane(), "Website Redesign");
      SwingUtilities.invokeAndWait(projectNav::doClick);
      ProjectDetailScreen detail = find(frame.getContentPane(), ProjectDetailScreen.class);
      assertNotNull(detail);

      gateway.deleteProject(gateway.an, gateway.website);
      SwingUtilities.invokeAndWait(detail::close);

      DashboardScreen dashboard = find(frame.getContentPane(), DashboardScreen.class);
      assertTrue(dashboard.isVisible());
      List<ProjectCard> cards = projectCards(frame);
      assertEquals(1, cards.size(), "Dashboard phải làm mới ngay sau khi xoá dự án");
      assertTrue(labelText(cards.get(0)).contains("Mobile App"));
      assertFalse(labelText(dashboard).contains("Website Redesign"));
    } finally {
      SwingUtilities.invokeAndWait(frame::dispose);
    }
  }

  private static MainFrame createFrame(TestGateway gateway) throws Exception {
    MainFrame[] holder = new MainFrame[1];
    SwingUtilities.invokeAndWait(() -> {
      MainFrame frame = new MainFrame(gateway);
      frame.addNotify();
      frame.setSize(1360, 900);
      frame.validate();
      frame.signIn(gateway.an);
      frame.validate();
      holder[0] = frame;
    });
    return holder[0];
  }

  private static List<ProjectCard> projectCards(Container root) {
    return findAll(root, ProjectCard.class);
  }

  private static NavItem findNav(Container root, String text) {
    return find(root, NavItem.class, nav -> text.equals(nav.getText()));
  }

  private static String labelText(Container root) {
    StringBuilder text = new StringBuilder();
    for (JLabel label : findAll(root, JLabel.class)) {
      text.append(label.getText()).append('\n');
    }
    return text.toString();
  }

  private static <T extends Component> T find(Container root, Class<T> type) {
    return find(root, type, value -> true);
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
}
