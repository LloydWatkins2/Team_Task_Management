package com.java.teamtask.ui.screens;

import com.java.teamtask.elements.projects;
import com.java.teamtask.elements.projectsAssignments;
import com.java.teamtask.ui.components.AppButton;
import com.java.teamtask.ui.components.Badge;
import com.java.teamtask.ui.components.EmptyState;
import com.java.teamtask.ui.components.PageHeader;
import com.java.teamtask.ui.components.ProjectCard;
import com.java.teamtask.ui.components.StackPanel;
import com.java.teamtask.ui.data.DataAccessException;
import com.java.teamtask.ui.data.ProjectSearch;
import com.java.teamtask.ui.data.SessionGateway;
import com.java.teamtask.ui.data.UiMapper;
import com.java.teamtask.ui.data.UserSession;
import com.java.teamtask.ui.theme.Space;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/** Dashboard chỉ hiển thị các dự án mà người dùng hiện tại tham gia, kèm role và bộ lọc tìm kiếm. */
public final class DashboardScreen extends JPanel {
  private final SessionGateway gateway;
  private final UserSession session;
  private final Consumer<projectsAssignments> onOpenProject;
  private final Runnable onProjectsChanged;
  private final PageHeader header;
  private final StackPanel list = new StackPanel();
  private final JPanel emptyWrapper =
      new JPanel(new FlowLayout(FlowLayout.CENTER, 0, Space.X6L));
  private final AppButton createButton;
  private final Map<projects, projectsAssignments> allProjects = new LinkedHashMap<>();
  private String query = "";

  public DashboardScreen(
      SessionGateway gateway,
      UserSession session,
      Consumer<projectsAssignments> onOpenProject,
      Runnable onProjectsChanged) {
    this.gateway = gateway;
    this.session = session;
    this.onOpenProject = onOpenProject == null ? assignment -> {} : onOpenProject;
    this.onProjectsChanged = onProjectsChanged == null ? () -> {} : onProjectsChanged;

    setOpaque(false);
    setLayout(new BorderLayout());

    createButton = new AppButton("Tạo dự án", AppButton.Variant.PRIMARY).withIcon("plus");
    createButton.addActionListener(e -> openCreateDialog());
    header = new PageHeader("Tổng quan", "", createButton);
    add(header, BorderLayout.NORTH);
    add(list, BorderLayout.CENTER);

    emptyWrapper.setOpaque(false);
    refresh();
  }

  /** Tải lại danh sách dự án từ DB cho user hiện tại. */
  public void refresh() {
    if (!session.isAuthenticated()) {
      allProjects.clear();
      render();
      return;
    }
    try {
      LinkedHashMap<projects, projectsAssignments> loaded =
          gateway.loadProjects(session.getUser());
      session.getUser().setRelatedProjects(new LinkedHashMap<>(loaded));
      allProjects.clear();
      allProjects.putAll(loaded);
      render();
      onProjectsChanged.run();
    } catch (DataAccessException ex) {
      showError(ex.getMessage());
    }
  }

  /** Đặt từ khoá lọc dự án; không truy vấn lại DB. */
  public void setSearchQuery(String query) {
    String next = query == null ? "" : query.trim();
    if (next.equals(this.query)) {
      return;
    }
    this.query = next;
    render();
  }

  private void render() {
    list.removeAll();
    remove(list);
    remove(emptyWrapper);

    List<projectsAssignments> visible = ProjectSearch.filter(allProjects.values(), query);
    if (visible.isEmpty()) {
      renderEmptyState();
      add(emptyWrapper, BorderLayout.CENTER);
    } else {
      for (projectsAssignments assignment : visible) {
        projects project = assignment.getProject();
        String role = assignment.getRole();
        ProjectCard card =
            new ProjectCard(
                project.getName(),
                project.getDescription(),
                UiMapper.formatDate(project.getDL()),
                UiMapper.roleLabel(role),
                "owner".equalsIgnoreCase(role) ? Badge.Kind.PRIMARY : Badge.Kind.NEUTRAL,
                () -> onOpenProject.accept(assignment));
        list.addRow(card, Space.LG);
      }
      add(list, BorderLayout.CENTER);
    }
    header.setSubtitle(subtitle(visible.size()));
    revalidate();
    repaint();
  }

  private String subtitle(int visibleCount) {
    if (allProjects.isEmpty()) {
      return "Bạn chưa tham gia dự án nào";
    }
    if (!query.isEmpty()) {
      return visibleCount + "/" + allProjects.size() + " dự án khớp \"" + query + "\"";
    }
    return allProjects.size() + " dự án đang tham gia";
  }

  private void renderEmptyState() {
    emptyWrapper.removeAll();
    if (allProjects.isEmpty()) {
      AppButton emptyCreate = new AppButton("Tạo dự án", AppButton.Variant.PRIMARY).withIcon("plus");
      emptyCreate.addActionListener(e -> openCreateDialog());
      emptyWrapper.add(
          new EmptyState(
              "Chưa có dự án nào",
              "Tạo dự án đầu tiên để bắt đầu quản lý công việc cùng nhóm của bạn.",
              emptyCreate));
    } else {
      emptyWrapper.add(
          new EmptyState(
              "Không tìm thấy dự án",
              "Không có dự án nào khớp với từ khoá \"" + query + "\". Hãy thử từ khoá khác.",
              new AppButton[0]));
    }
    emptyWrapper.revalidate();
    emptyWrapper.repaint();
  }

  private void openCreateDialog() {
    if (!session.isAuthenticated()) {
      return;
    }
    Window owner = SwingUtilities.getWindowAncestor(this);
    new ProjectFormDialog(
            owner,
            "Tạo dự án mới",
            "",
            "",
            null,
            values -> {
              try {
                projects created =
                    gateway.createProject(
                        session.getUser(), values.name(), values.description(), values.deadline());
                if (created == null) {
                  showError("Không thể tạo dự án. Vui lòng thử lại.");
                  return;
                }
                refresh();
              } catch (DataAccessException ex) {
                showError(ex.getMessage());
              }
            })
        .setVisible(true);
  }

  private void showError(String message) {
    JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
  }

  public void setCompact(boolean compact) {
    header.setCompact(compact);
  }
}
