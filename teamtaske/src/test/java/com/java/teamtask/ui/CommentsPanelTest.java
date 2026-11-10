package com.java.teamtask.ui;

import com.java.teamtask.ui.components.AppButton;
import com.java.teamtask.ui.data.CommentView;
import com.java.teamtask.ui.screens.CommentsPanel;
import com.java.teamtask.ui.theme.Theme;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Comments không còn refresh tay; append chỉ thêm bình luận mới. */
class CommentsPanelTest {
  @BeforeAll
  static void setUp() {
    Theme.install();
  }

  @Test
  void hasNoManualRefreshAndAppendsWithoutDuplicates() throws Exception {
    CommentsPanel[] holder = new CommentsPanel[1];
    SwingUtilities.invokeAndWait(() -> {
      List<CommentView> initial = new ArrayList<>();
      initial.add(new CommentView(1, "Nguyễn An", "", "Bình luận cũ"));
      holder[0] = new CommentsPanel(initial, text -> true);
    });
    CommentsPanel panel = holder[0];

    assertNull(
        find(panel, AppButton.class, button -> "Làm mới".equals(button.getText())),
        "Không được còn nút refresh thủ công trong comments");

    JLabel oldContent =
        find(panel, JLabel.class,
            label -> label.getText() != null && label.getText().contains("Bình luận cũ"));
    assertNotNull(oldContent);

    List<CommentView> incoming = new ArrayList<>();
    incoming.add(new CommentView(1, "Nguyễn An", "", "Bản trùng id"));
    incoming.add(new CommentView(2, "Trần Minh", "", "Bình luận mới"));
    SwingUtilities.invokeAndWait(() -> panel.appendComments(incoming));

    String text = labelText(panel);
    assertTrue(text.contains("Bình luận cũ"));
    assertTrue(text.contains("Bình luận mới"));
    assertTrue(!text.contains("Bản trùng id"), "Phải bỏ qua comment đã có cùng id");
    assertEquals(1, occurrences(text, "Bình luận mới"), "Không được lặp comment mới");
    assertSame(
        oldContent,
        find(panel, JLabel.class, label -> label == oldContent),
        "Append không được dựng lại comment cũ");
  }

  @Test
  void alignsAvatarAndCommentWithAuthor() throws Exception {
    CommentsPanel[] holder = new CommentsPanel[1];
    SwingUtilities.invokeAndWait(() -> {
      holder[0] = new CommentsPanel(
          List.of(new CommentView(1, "Nguyễn An", "Hôm nay", "Ngắn")), text -> true);
      holder[0].setSize(600, 420);
      layoutTree(holder[0]);
    });

    CommentsPanel panel = holder[0];
    JPanel row = find(panel, JPanel.class, item -> "comment.row".equals(item.getName()));
    JLabel avatar = find(panel, JLabel.class, item -> "comment.avatar".equals(item.getName()));
    JLabel author = find(panel, JLabel.class, item -> "comment.author".equals(item.getName()));
    JLabel content = find(panel, JLabel.class, item -> "comment.content".equals(item.getName()));

    assertNotNull(row);
    assertNotNull(avatar);
    assertNotNull(author);
    assertNotNull(content);
    assertEquals(32, avatar.getHeight(), "Avatar không được bị kéo giãn theo chiều cao row");
    assertEquals(0, avatar.getY());
    assertEquals(0, author.getY());
    assertEquals(author.getX(), content.getX(), "Nội dung phải thẳng hàng với tên người dùng");
  }

  private static void layoutTree(Component component) {
    if (component instanceof Container container) {
      container.doLayout();
      for (Component child : container.getComponents()) {
        layoutTree(child);
      }
    }
  }

  private static int occurrences(String text, String needle) {
    int count = 0;
    int index = 0;
    while ((index = text.indexOf(needle, index)) >= 0) {
      count++;
      index += needle.length();
    }
    return count;
  }

  private static String labelText(Container root) {
    StringBuilder text = new StringBuilder();
    for (JLabel label : findAll(root, JLabel.class)) {
      text.append(label.getText()).append('\n');
    }
    return text.toString();
  }

  private static <T extends Component> T find(Container root, Class<T> type,
      java.util.function.Predicate<T> filter) {
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
}
