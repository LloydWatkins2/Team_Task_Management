package com.java.teamtask.ui.screens;

import com.java.teamtask.elements.projects;
import com.java.teamtask.elements.projectsAssignments;
import com.java.teamtask.elements.seeProjects;
import com.java.teamtask.elements.tasks;
import com.java.teamtask.elements.users;
import com.java.teamtask.ui.Card;
import com.java.teamtask.ui.components.AppButton;
import com.java.teamtask.ui.components.Avatar;
import com.java.teamtask.ui.components.Badge;
import com.java.teamtask.ui.components.CircleIconPanel;
import com.java.teamtask.ui.components.EmptyState;
import com.java.teamtask.ui.components.IconButton;
import com.java.teamtask.ui.components.PageHeader;
import com.java.teamtask.ui.components.StackPanel;
import com.java.teamtask.ui.components.TaskCard;
import com.java.teamtask.ui.data.DataAccessException;
import com.java.teamtask.ui.data.SessionGateway;
import com.java.teamtask.ui.data.TaskView;
import com.java.teamtask.ui.data.UiMapper;
import com.java.teamtask.ui.data.UserSession;
import com.java.teamtask.ui.theme.Icons;
import com.java.teamtask.ui.theme.Palette;
import com.java.teamtask.ui.theme.Space;
import com.java.teamtask.ui.theme.Typography;
import com.java.teamtask.ui.theme.UiScale;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/** Chi tiết dự án: thông tin, thành viên, công việc và hành động theo role. */
public final class ProjectDetailScreen extends JPanel {
  private final SessionGateway gateway;
  private final UserSession session;
  private final Runnable onBack;
  private final Runnable onProjectsChanged;
  private final PageHeader header;
  private final JPanel headerActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, Space.SM, 0));
  private final JPanel body = new JPanel();

  private projectsAssignments assignment;
  private projects project;
  private seeProjects role;
  private List<projectsAssignments> members = new ArrayList<>();

  public ProjectDetailScreen(
      SessionGateway gateway,
      UserSession session,
      Runnable onBack,
      Runnable onProjectsChanged) {
    this.gateway = gateway;
    this.session = session;
    this.onBack = onBack == null ? () -> {} : onBack;
    this.onProjectsChanged = onProjectsChanged == null ? () -> {} : onProjectsChanged;

    setOpaque(false);
    setLayout(new BorderLayout());
    headerActions.setOpaque(false);
    header = new PageHeader("Dự án", "", headerActions);
    add(header, BorderLayout.NORTH);

    body.setOpaque(false);
    body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
    add(body, BorderLayout.CENTER);
  }

  /** Mở dự án và khởi tạo instance quyền owner/member qua {@link UserSession}. */
  public void open(projectsAssignments assignment) {
    if (assignment == null || assignment.getProject() == null) {
      close();
      return;
    }
    this.assignment = assignment;
    this.project = assignment.getProject();
    this.role = session.openProject(assignment);
    header.setTitle(project.getName());
    header.setSubtitle(
        UiMapper.roleLabel(assignment.getRole())
            + " · Hạn chót "
            + UiMapper.formatDate(project.getDL()));

    loadMembers();
    rebuildActions();
    render();
  }

  private void loadMembers() {
    try {
      members = gateway.loadMembers(role, project);
    } catch (DataAccessException ex) {
      members = new ArrayList<>();
      showError(ex.getMessage());
    }
  }

  private void rebuildActions() {
    headerActions.removeAll();
    AppButton back = new AppButton("Quay lại", AppButton.Variant.OUTLINE).withIcon("chevron-right");
    back.addActionListener(e -> close());
    headerActions.add(back);

    if (role instanceof com.java.teamtask.elements.owners) {
      AppButton edit = new AppButton("Sửa dự án", AppButton.Variant.OUTLINE).withIcon("edit");
      edit.addActionListener(e -> editProject());
      headerActions.add(edit);

      AppButton addMember =
          new AppButton("Thêm thành viên", AppButton.Variant.OUTLINE).withIcon("plus");
      addMember.addActionListener(e -> addMember());
      headerActions.add(addMember);

      AppButton delete =
          new AppButton("Xoá dự án", AppButton.Variant.DANGER_OUTLINE).withIcon("trash");
      delete.addActionListener(e -> deleteProject());
      headerActions.add(delete);
    }
    headerActions.revalidate();
    headerActions.repaint();
  }

  private void render() {
    body.removeAll();
    body.add(projectInfo());
    body.add(Box.createVerticalStrut(Space.X3L));
    body.add(section("Thành viên", members.size() + " người", null));
    body.add(Box.createVerticalStrut(Space.MD));
    body.add(membersList());
    body.add(Box.createVerticalStrut(Space.X3L));
    body.add(tasksSection());
    body.add(Box.createVerticalStrut(Space.XL));
    body.revalidate();
    body.repaint();
  }

  private Card projectInfo() {
    Card card = new Card(new BorderLayout(Space.XL, 0));
    card.setBorder(new EmptyBorder(Space.XL, Space.X3L, Space.XL, Space.X3L));

    card.add(
        new CircleIconPanel(56, Palette.PRIMARY_SOFT, "projects", Palette.PRIMARY_DARK),
        BorderLayout.WEST);

    JPanel texts = new JPanel();
    texts.setOpaque(false);
    texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
    JLabel description =
        new JLabel(
            "<html><div style='width:560px'>"
                + escape(
                    project.getDescription() == null || project.getDescription().isBlank()
                        ? "Chưa có mô tả"
                        : project.getDescription())
                + "</div></html>");
    description.setFont(Typography.body());
    description.setForeground(Palette.TEXT_MUTED_STRONG);
    description.setAlignmentX(Component.LEFT_ALIGNMENT);

    JPanel meta = new JPanel(new FlowLayout(FlowLayout.LEFT, Space.XL, 0));
    meta.setOpaque(false);
    meta.setAlignmentX(Component.LEFT_ALIGNMENT);
    meta.setBorder(new EmptyBorder(Space.SM, 0, 0, 0));
    meta.add(metaLabel("calendar", UiMapper.formatDate(project.getDL())));
    meta.add(metaLabel("users", members.size() + " thành viên"));
    meta.add(metaLabel("tasks", project.getTasksList().size() + " công việc"));

    texts.add(description);
    texts.add(meta);
    card.add(texts, BorderLayout.CENTER);

    JPanel roleBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
    roleBox.setOpaque(false);
    roleBox.add(
        new Badge(
            UiMapper.roleLabel(assignment == null ? null : assignment.getRole()),
            assignment != null && assignment.isOwner() ? Badge.Kind.PRIMARY : Badge.Kind.NEUTRAL));
    card.add(roleBox, BorderLayout.EAST);
    return card;
  }

  private JPanel section(String title, String hint, AppButton action) {
    JPanel row = new JPanel(new BorderLayout(Space.MD, 0));
    row.setOpaque(false);
    row.setAlignmentX(Component.LEFT_ALIGNMENT);
    row.setMaximumSize(new Dimension(Integer.MAX_VALUE, UiScale.px(48)));

    JPanel texts = new JPanel();
    texts.setOpaque(false);
    texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
    JLabel titleLabel = new JLabel(title);
    titleLabel.setFont(Typography.h3());
    titleLabel.setForeground(Palette.TEXT);
    titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    JLabel hintLabel = new JLabel(hint == null ? "" : hint);
    hintLabel.setFont(Typography.small());
    hintLabel.setForeground(Palette.TEXT_MUTED_STRONG);
    hintLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    texts.add(titleLabel);
    texts.add(hintLabel);
    row.add(texts, BorderLayout.CENTER);
    if (action != null) {
      JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
      wrapper.setOpaque(false);
      wrapper.add(action);
      row.add(wrapper, BorderLayout.EAST);
    }
    return row;
  }

  private JPanel membersList() {
    StackPanel list = new StackPanel();
    list.setAlignmentX(Component.LEFT_ALIGNMENT);
    if (members.isEmpty()) {
      JPanel empty = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
      empty.setOpaque(false);
      empty.setAlignmentX(Component.LEFT_ALIGNMENT);
      JLabel label = new JLabel("Chưa có thành viên nào trong dự án.");
      label.setFont(Typography.body());
      label.setForeground(Palette.TEXT_MUTED_STRONG);
      empty.add(label);
      list.addRow(empty, 0);
      return list;
    }
    for (projectsAssignments member : members) {
      list.addRow(memberRow(member), Space.SM);
    }
    return list;
  }

  private Card memberRow(projectsAssignments member) {
    Card card = new Card(new BorderLayout(Space.MD, 0));
    card.setBorder(new EmptyBorder(Space.MD, Space.XL, Space.MD, Space.XL));

    users user = member.getUser();
    card.add(new Avatar(UiMapper.displayName(user), 40), BorderLayout.WEST);

    JPanel texts = new JPanel();
    texts.setOpaque(false);
    texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
    JLabel name = new JLabel(UiMapper.displayName(user));
    name.setFont(Typography.label());
    name.setForeground(Palette.TEXT);
    name.setAlignmentX(Component.LEFT_ALIGNMENT);
    JLabel detail =
        new JLabel(
            user == null
                ? ""
                : (user.getUsername() == null ? "" : "@" + user.getUsername())
                    + (user.getEmail() == null ? "" : " · " + user.getEmail()));
    detail.setFont(Typography.small());
    detail.setForeground(Palette.TEXT_MUTED_STRONG);
    detail.setAlignmentX(Component.LEFT_ALIGNMENT);
    texts.add(name);
    texts.add(detail);
    card.add(texts, BorderLayout.CENTER);

    JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, Space.SM, 0));
    actions.setOpaque(false);
    actions.add(
        new Badge(
            UiMapper.roleLabel(member.getRole()),
            member.isOwner() ? Badge.Kind.PRIMARY : Badge.Kind.NEUTRAL));
    if (role instanceof com.java.teamtask.elements.owners
        && !member.isOwner()
        && session.getUser() != null
        && user != null
        && user.userId != session.getUser().userId) {
      IconButton remove = new IconButton("trash", "Xoá thành viên", 40, true);
      remove.addActionListener(e -> removeMember(user));
      actions.add(remove);
    }
    card.add(actions, BorderLayout.EAST);
    return card;
  }

  private JPanel tasksSection() {
    AppButton create = null;
    if (role instanceof com.java.teamtask.elements.owners) {
      create = new AppButton("Tạo công việc", AppButton.Variant.PRIMARY).withIcon("plus");
      create.addActionListener(e -> createTask());
    }
    JPanel section = section("Công việc", project.getTasksList().size() + " công việc", create);

    JPanel wrapper = new JPanel();
    wrapper.setOpaque(false);
    wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
    wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
    wrapper.add(section);
    wrapper.add(Box.createVerticalStrut(Space.MD));

    StackPanel list = new StackPanel();
    list.setAlignmentX(Component.LEFT_ALIGNMENT);
    if (project.getTasksList().isEmpty()) {
      AppButton emptyCreate = null;
      if (role instanceof com.java.teamtask.elements.owners) {
        emptyCreate =
            new AppButton("Tạo công việc đầu tiên", AppButton.Variant.PRIMARY).withIcon("plus");
        emptyCreate.addActionListener(e -> createTask());
      }
      JPanel holder = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, Space.MD));
      holder.setOpaque(false);
      holder.setAlignmentX(Component.LEFT_ALIGNMENT);
      holder.add(
          new EmptyState(
              "Chưa có công việc nào",
              "Tạo công việc để phân công cho các thành viên trong dự án.",
              emptyCreate));
      list.addRow(holder, 0);
    } else {
      for (tasks task : project.getTasksList()) {
        TaskView view = UiMapper.taskView(task, project);
        TaskCard card =
            new TaskCard(
                view.title(),
                view.subtitle(),
                view.status(),
                view.projectName(),
                view.assignee(),
                view.deadline(),
                view.iconBackground(),
                view.iconName());
        card.setOnOpen(() -> openTask(task));
        list.addRow(card, Space.SM);
      }
    }
    wrapper.add(list);
    return wrapper;
  }

  private JLabel metaLabel(String icon, String text) {
    JLabel label = new JLabel(text, Icons.muted(icon, 18), JLabel.LEFT);
    label.setFont(Typography.meta());
    label.setForeground(Palette.TEXT_MUTED_STRONG);
    label.setIconTextGap(Space.XXS);
    return label;
  }

  private void createTask() {
    Window owner = SwingUtilities.getWindowAncestor(this);
    new TaskCreateDialog(
            owner,
            members,
            values -> {
              try {
                tasks created =
                    gateway.createTask(
                        project,
                        values.name(),
                        values.description(),
                        values.status(),
                        values.deadline(),
                        values.assignee());
                if (created == null) {
                  showError("Không thể tạo công việc. Vui lòng thử lại.");
                  return;
                }
                reload();
              } catch (DataAccessException ex) {
                showError(ex.getMessage());
              }
            })
        .setVisible(true);
  }

  private void openTask(tasks task) {
    Window owner = SwingUtilities.getWindowAncestor(this);
    new TaskDetailDialog(
            owner, gateway, role, session.getUser(), task, project, this::reload)
        .setVisible(true);
  }

  private void editProject() {
    Window owner = SwingUtilities.getWindowAncestor(this);
    new ProjectFormDialog(
            owner,
            "Sửa dự án",
            project.getName(),
            project.getDescription(),
            project.getDL(),
            values -> {
              try {
                if (gateway.updateProject(
                    role, values.name(), values.description(), values.deadline())) {
                  reload();
                } else {
                  showError("Không thể cập nhật dự án.");
                }
              } catch (DataAccessException ex) {
                showError(ex.getMessage());
              }
            })
        .setVisible(true);
  }

  private void addMember() {
    String input =
        JOptionPane.showInputDialog(
            this,
            "Nhập tên đăng nhập hoặc Gmail của thành viên:",
            "Thêm thành viên",
            JOptionPane.QUESTION_MESSAGE);
    if (input == null || input.isBlank()) {
      return;
    }
    try {
      users member = gateway.findUser(input.trim());
      if (member == null) {
        showError("Không tìm thấy người dùng này.");
        return;
      }
      if (gateway.addMember(role, project, member)) {
        reload();
      } else {
        showError("Người dùng đã là thành viên của dự án.");
      }
    } catch (DataAccessException ex) {
      showError(ex.getMessage());
    }
  }

  private void removeMember(users member) {
    int choice =
        JOptionPane.showConfirmDialog(
            this,
            "Xoá " + UiMapper.displayName(member) + " khỏi dự án?",
            "Xoá thành viên",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
    if (choice != JOptionPane.YES_OPTION) {
      return;
    }
    try {
      if (gateway.removeMember(role, project, member)) {
        reload();
      } else {
        showError("Không thể xoá thành viên.");
      }
    } catch (DataAccessException ex) {
      showError(ex.getMessage());
    }
  }

  private void deleteProject() {
    int choice =
        JOptionPane.showConfirmDialog(
            this,
            "Bạn có chắc muốn xoá dự án này? Toàn bộ công việc và bình luận cũng sẽ bị xoá.",
            "Xoá dự án",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
    if (choice != JOptionPane.YES_OPTION) {
      return;
    }
    try {
      if (gateway.deleteProject(session.getUser(), project)) {
        session.clearActiveProject();
        onProjectsChanged.run();
        onBack.run();
      } else {
        showError("Không thể xoá dự án.");
      }
    } catch (DataAccessException ex) {
      showError(ex.getMessage());
    }
  }

  private void reload() {
    if (project == null || !session.isAuthenticated()) {
      return;
    }
    try {
      LinkedHashMap<projects, projectsAssignments> loaded =
          gateway.loadProjects(session.getUser());
      session.getUser().setRelatedProjects(new LinkedHashMap<>(loaded));
      projectsAssignments fresh = null;
      for (projectsAssignments candidate : loaded.values()) {
        if (candidate.getProject() != null
            && candidate.getProject().getId() == project.getId()) {
          fresh = candidate;
          break;
        }
      }
      if (fresh == null) {
        close();
        return;
      }
      open(fresh);
      onProjectsChanged.run();
    } catch (DataAccessException ex) {
      showError(ex.getMessage());
    }
  }

  /** Rời dự án: xoá ngay instance owner/member và quay về Dashboard. */
  public void close() {
    session.clearActiveProject();
    assignment = null;
    project = null;
    role = null;
    onBack.run();
  }

  public projects getProject() {
    return project;
  }

  public seeProjects getRole() {
    return role;
  }

  private void showError(String message) {
    JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
  }

  private static String escape(String text) {
    return text == null
        ? ""
        : text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
  }

  public void setCompact(boolean compact) {
    header.setCompact(compact);
  }
}
