package com.java.teamtask.ui;

import com.java.teamtask.elements.owners;
import com.java.teamtask.ui.data.CommentView;
import com.java.teamtask.ui.screens.CommentsPanel;
import com.java.teamtask.ui.screens.TaskDetailDialog;
import com.java.teamtask.ui.theme.Palette;
import com.java.teamtask.ui.theme.Space;
import com.java.teamtask.ui.theme.Theme;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/** Bố cục phần bình luận trong TaskDetailDialog: tiêu đề sát trái, nội dung thụt 1 tab, divider full-width. */
class TaskDetailLayoutTest {
  @BeforeAll
  static void setUp() {
    Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(), "Cần môi trường có màn hình");
    Theme.install();
  }

  @Test
  void commentSectionFollowsDesignLayout() throws Exception {
    TestGateway gateway = new TestGateway();
    gateway.comments.add(new CommentView("Nguyễn An", "Hôm nay", "Bình luận mẫu"));

    TaskDetailDialog[] holder = new TaskDetailDialog[1];
    SwingUtilities.invokeAndWait(() -> {
      TaskDetailDialog dialog =
          new TaskDetailDialog(
              null,
              gateway,
              new owners(gateway.an, gateway.website, gateway.anOwner),
              gateway.an,
              gateway.website.getTasksList().get(0),
              gateway.website,
              () -> {});
      dialog.addNotify();
      dialog.validate();
      layoutTree(dialog.getContentPane());
      holder[0] = dialog;
    });

    TaskDetailDialog dialog = holder[0];
    try {
      Container root = dialog.getContentPane();
      Card card = find(root, Card.class);
      CommentsPanel comments = find(root, CommentsPanel.class);
      assertNotNull(card);
      assertNotNull(comments);

      JLabel title = find(root, JLabel.class, label -> "Bình luận".equals(label.getText()));
      assertNotNull(title, "Phải có tiêu đề Bình luận");
      Rectangle titleBounds = toCard(title, card);
      assertEquals(Space.X3L, titleBounds.x, "Tiêu đề bình luận phải sát lề trái của card");

      Rectangle commentsBounds = toCard(comments, card);
      assertEquals(Space.X3L + Space.X4L, commentsBounds.x,
          "Khối bình luận phải thụt đúng 1 tab (32px) so với tiêu đề");

      Rectangle divider = dividerBounds(card, card);
      assertNotNull(divider, "Phải có thanh phân chia bình luận với trạng thái");
      assertEquals(0, divider.x, "Divider phải bắt đầu từ mép trái của card");
      assertEquals(card.getWidth(), divider.width,
          "Divider phải kéo hết chiều ngang của khung");

      JLabel author = find(comments, JLabel.class, label -> "Nguyễn An".equals(label.getText()));
      JLabel content = find(comments, JLabel.class,
          label -> label.getText() != null && label.getText().startsWith("<html>"));
      assertNotNull(author);
      assertNotNull(content);
      Component head = author.getParent();
      Rectangle headBounds = toCard(head, card);
      Rectangle contentBounds = toCard(content, card);
      assertEquals(Space.XS, contentBounds.y - (headBounds.y + headBounds.height),
          "Khoảng cách tên người bình luận và nội dung phải cố định");
    } finally {
      SwingUtilities.invokeAndWait(dialog::dispose);
    }
  }

  private static Rectangle toCard(Component component, Card card) {
    return SwingUtilities.convertRectangle(
        component.getParent(), component.getBounds(), card);
  }

  private static Rectangle dividerBounds(Container root, Container card) {
    for (Component child : root.getComponents()) {
      if (child instanceof JPanel panel
          && panel.isOpaque()
          && panel.getBackground() == Palette.BORDER
          && panel.getHeight() <= 1) {
        return SwingUtilities.convertRectangle(panel.getParent(), panel.getBounds(), card);
      }
      if (child instanceof Container container) {
        Rectangle found = dividerBounds(container, card);
        if (found != null) {
          return found;
        }
      }
    }
    return null;
  }

  private static <T extends Component> T find(Container root, Class<T> type) {
    return find(root, type, value -> true);
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

  private static void layoutTree(Component component) {
    if (component instanceof Container container) {
      container.doLayout();
      for (Component child : container.getComponents()) {
        layoutTree(child);
      }
    }
  }
}
