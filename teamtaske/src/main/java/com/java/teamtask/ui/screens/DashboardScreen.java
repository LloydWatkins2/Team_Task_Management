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
import java.util.Map;
import java.util.function.Consumer;

/** Dashboard chỉ hiển thị các dự án mà người dùng hiện tại tham gia, kèm role. */
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

    AppButton emptyCreate = new AppButton("Tạo dự án", AppButton.Variant.PRIMARY).withIcon("plus");
    emptyCreate.addActionListener(e -> openCreateDialog());
    emptyWrapper.setOpaque(false);
    emptyWrapper.add(
        new EmptyState(
            "Chưa có dự án nào",
            "Tạo dự án đầu tiên để bắt đầu quản lý công việc cùng nhóm của bạn.",
            emptyCreate));
    refresh();
  }

  /** Tải lại danh sách dự án từ DB cho user hiện tại. */
  public void refresh() {
    if (!session.isAuthenticated()) {
      render(Map.of());
      return;
    }
    try {
      LinkedHashMap<projects, projectsAssignments> loaded =
          gateway.loadProjects(session.getUser());
      session.getUser().setRelatedProjects(new LinkedHashMap<>(loaded));
      render(loaded);
      onProjectsChanged.run();
    } catch (DataAccessException ex) {
      showError(ex.getMessage());
    }
  }

  private void render(Map<projects, projectsAssignments> assignments) {
    list.removeAll();
    remove(list);
    remove(emptyWrapper);

    if (assignments == null || assignments.isEmpty()) {
      header.setSubtitle("Bạn chưa tham gia dự án nào");
      add(emptyWrapper, BorderLayout.CENTER);
    } else {
      for (Map.Entry<projects, projectsAssignments> entry : assignments.entrySet()) {
        projects project = entry.getKey();
        projectsAssignments assignment = entry.getValue();
        String role = assignment == null ? "member" : assignment.getRole();
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
      header.setSubtitle(assignments.size() + " dự án đang tham gia");
      add(list, BorderLayout.CENTER);
    }
    revalidate();
    repaint();
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
