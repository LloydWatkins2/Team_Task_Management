package com.java.teamtask.ui;

import com.java.teamtask.elements.users;
import com.java.teamtask.ui.components.Badge;
import com.java.teamtask.ui.components.ProgressBar;
import com.java.teamtask.ui.components.TaskCard;
import com.java.teamtask.ui.data.MemberInfoView;
import com.java.teamtask.ui.data.MemberTaskView;
import com.java.teamtask.ui.screens.MemberInfoDialog;
import com.java.teamtask.ui.theme.Theme;
import com.java.teamtask.ui.theme.UiScale;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Popup thành viên hiển thị thông tin, tiến độ và task chỉ với tên + trạng thái. */
class MemberInfoDialogTest {
  @BeforeAll
  static void setUp() {
    Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(), "Cần môi trường có màn hình");
    Theme.install();
  }

  @Test
  void showsProfileProgressAndBasicTaskRows() throws Exception {
    users minh =
        new users(
            2, "Trần", "Minh", "minh", "123456", "minh@example.com", "0909876543", "02/02/1998");
    List<MemberTaskView> tasks = new ArrayList<>();
    tasks.add(new MemberTaskView("Thiết kế trang Tổng quan", TaskCard.Status.DONE));
    tasks.add(new MemberTaskView("Kiểm thử đăng nhập", TaskCard.Status.IN_PROGRESS));
    MemberInfoView info =
        new MemberInfoView(
            minh, "member", LocalDateTime.now().minusDays(3), 0.5, 1, 2, tasks);

    MemberInfoDialog[] holder = new MemberInfoDialog[1];
    SwingUtilities.invokeAndWait(() -> {
      MemberInfoDialog dialog = new MemberInfoDialog(null, info);
      dialog.addNotify();
      dialog.validate();
      layoutTree(dialog.getContentPane());
      holder[0] = dialog;
    });

    MemberInfoDialog dialog = holder[0];
    try {
      Container root = dialog.getContentPane();
      ProgressBar progress =
          find(root, ProgressBar.class, bar -> "member.progress".equals(bar.getName()));
      assertNotNull(progress);
      assertEquals(50, progress.getValue());

      String text = labelText(root);
      assertTrue(text.contains("Trần Minh"));
      assertTrue(text.contains("Đã hoàn thành 1/2 công việc"));
      assertTrue(text.contains("Thiết kế trang Tổng quan"));
      assertTrue(text.contains("Kiểm thử đăng nhập"));
      assertTrue(text.contains("HOÀN THÀNH"));
      assertTrue(text.contains("ĐANG LÀM"));
      assertFalse(text.contains("Hạn chót"));
      assertFalse(text.contains("Người phụ trách"));
      assertNull(
          find(root, TaskCard.class, ignored -> true),
          "Popup chỉ hiển thị tên + trạng thái, không dùng card task đầy đủ");

      List<JPanel> rows =
          findAll(root, JPanel.class).stream()
              .filter(row -> "member.task.row".equals(row.getName()))
              .toList();
      assertEquals(2, rows.size());
      for (JPanel row : rows) {
        assertEquals(UiScale.px(36), row.getHeight(), "Các task phải có cùng chiều cao");
      }
      for (Badge badge : findAll(root, Badge.class)) {
        assertEquals(UiScale.px(28), badge.getHeight(), "Status không được bị kéo giãn");
      }
    } finally {
      SwingUtilities.invokeAndWait(dialog::dispose);
    }
  }

  private static String labelText(Container root) {
    StringBuilder text = new StringBuilder();
    for (JLabel label : findAll(root, JLabel.class)) {
      text.append(label.getText()).append('\n');
    }
    return text.toString();
  }

  private static <T extends Component> T find(
      Container root, Class<T> type, java.util.function.Predicate<T> filter) {
    if (type.isInstance(root) && filter.test(type.cast(root))) {
      return type.cast(root);
    }
    for (Component child : root.getComponents()) {
      if (type.isInstance(child) && filter.test(type.cast(child))) {
        return type.cast(child);
      }
      if (child instanceof Container container) {
        T found = find(container, type, filter);
        if (found != null) {
          return found;
        }
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

  private static void layoutTree(Component component) {
    if (component instanceof Container container) {
      container.doLayout();
      for (Component child : container.getComponents()) {
        layoutTree(child);
      }
    }
  }
}
