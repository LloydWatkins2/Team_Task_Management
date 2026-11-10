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
import com.java.teamtask.ui.components.EmptyState;
import com.java.teamtask.ui.components.IconButton;
import com.java.teamtask.ui.components.PageHeader;
import com.java.teamtask.ui.components.ProgressBar;
import com.java.teamtask.ui.components.StackPanel;
import com.java.teamtask.ui.components.TaskCard;
import com.java.teamtask.ui.data.DataAccessException;
import com.java.teamtask.ui.data.SessionGateway;
import com.java.teamtask.ui.data.TaskView;
import com.java.teamtask.ui.data.UiMapper;
import com.java.teamtask.ui.data.UserSession;
import com.java.teamtask.ui.theme.Palette;
import com.java.teamtask.ui.theme.Space;
import com.java.teamtask.ui.theme.Typography;
import com.java.teamtask.ui.theme.UiScale;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Chi tiết dự án: khung tiến độ phía trên, 2 cột cố định Thành viên / Công việc bên dưới.
 * Mỗi danh sách có scroll riêng và tự refresh nhẹ mỗi 25 giây.
 */
public final class ProjectDetailScreen extends JPanel {
    private static final int AUTO_REFRESH_MS = 25_000;

    private final SessionGateway gateway;
    private final UserSession session;
    private final Runnable onBack;
    private final Runnable onProjectsChanged;
    private final PageHeader header;
    private final JPanel headerActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, Space.SM, 0));
    private final JPanel membersHeader = new JPanel(new BorderLayout(Space.MD, 0));
    private final JPanel tasksHeader = new JPanel(new BorderLayout(Space.MD, 0));

    private final StackPanel membersList;
    private final StackPanel tasksList;
    private final JScrollPane membersScroll;
    private final JScrollPane tasksScroll;
    private final JLabel membersHint = hintLabel();
    private final JLabel tasksHint = hintLabel();
    private final ProgressBar progressBar = new ProgressBar();
    private final JLabel progressText = new JLabel("Chưa khởi động dự án");
    private final JLabel progressDetail = hintLabel();
    private final AppButton refreshButton =
            new AppButton("Làm mới", AppButton.Variant.OUTLINE);
    private final Timer autoRefresh;

    private projectsAssignments assignment;
    private projects project;
    private seeProjects role;
    private List<projectsAssignments> members = new ArrayList<>();
    private boolean refreshing;
    private String lastSignature;

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

        membersList = new StackPanel();
        tasksList = new StackPanel();
        membersScroll = createScroll(membersList);
        tasksScroll = createScroll(tasksList);

        Card membersCard = new Card(new BorderLayout());
        membersCard.setName("project.members");
        membersCard.setBorder(new EmptyBorder(Space.MD, Space.MD, Space.MD, Space.MD));
        membersCard.add(membersHeader, BorderLayout.NORTH);
        membersCard.add(membersScroll, BorderLayout.CENTER);

        Card tasksCard = new Card(new BorderLayout());
        tasksCard.setName("project.tasks");
        tasksCard.setBorder(new EmptyBorder(Space.MD, Space.MD, Space.MD, Space.MD));
        tasksCard.add(tasksHeader, BorderLayout.NORTH);
        tasksCard.add(tasksScroll, BorderLayout.CENTER);
        rebuildHeaders();

        JPanel columns = new JPanel(new GridBagLayout());
        columns.setOpaque(false);
        GridBagConstraints left = new GridBagConstraints();
        left.gridx = 0;
        left.gridy = 0;
        left.weightx = 0.35;
        left.weighty = 1;
        left.fill = GridBagConstraints.BOTH;
        left.insets = new Insets(0, 0, 0, Space.LG);
        columns.add(membersCard, left);

        GridBagConstraints right = new GridBagConstraints();
        right.gridx = 1;
        right.gridy = 0;
        right.weightx = 0.65;
        right.weighty = 1;
        right.fill = GridBagConstraints.BOTH;
        columns.add(tasksCard, right);

        progressBar.setName("project.progress");
        progressText.setFont(Typography.body());
        progressText.setForeground(Palette.TEXT_MUTED_STRONG);
        progressDetail.setForeground(Palette.TEXT_MUTED_STRONG);
        refreshButton.setName("project.refresh");
        refreshButton.addActionListener(e -> refreshAsync(true));

        JPanel content = new JPanel(new BorderLayout(0, Space.MD));
        content.setOpaque(false);
        content.add(progressCard(), BorderLayout.NORTH);
        content.add(columns, BorderLayout.CENTER);

        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(Space.MD, Space.MD, Space.MD, Space.MD));
        body.add(content, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);

        autoRefresh = new Timer(AUTO_REFRESH_MS, e -> refreshAsync(false));
        autoRefresh.setCoalesce(true);
    }

    private static JLabel hintLabel() {
        JLabel label = new JLabel();
        label.setFont(Typography.small());
        label.setForeground(Palette.TEXT_MUTED_STRONG);
        return label;
    }

    private static JScrollPane createScroll(StackPanel list) {
        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setUnitIncrement(UiScale.px(18));
        scroll.setPreferredSize(new Dimension(UiScale.px(320), UiScale.px(360)));
        return scroll;
    }

    private Card progressCard() {
        Card card = new Card(new BorderLayout(Space.MD, Space.SM));
        card.setName("project.summary");
        card.setBorder(new EmptyBorder(Space.MD, Space.XL, Space.MD, Space.XL));

        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Tiến độ dự án");
        title.setFont(Typography.h3());
        title.setForeground(Palette.TEXT);
        title.setAlignmentX(LEFT_ALIGNMENT);
        progressText.setAlignmentX(LEFT_ALIGNMENT);
        texts.add(title);
        texts.add(progressText);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        actions.setOpaque(false);
        actions.add(refreshButton);

        JPanel head = new JPanel(new BorderLayout(Space.MD, 0));
        head.setOpaque(false);
        head.add(texts, BorderLayout.CENTER);
        head.add(actions, BorderLayout.EAST);

        JPanel meter = new JPanel(new BorderLayout(Space.MD, 0));
        meter.setOpaque(false);
        meter.add(progressBar, BorderLayout.CENTER);
        meter.add(progressDetail, BorderLayout.EAST);

        card.add(head, BorderLayout.NORTH);
        card.add(meter, BorderLayout.CENTER);
        return card;
    }

    private void rebuildHeaders() {
        membersHeader.removeAll();
        membersHeader.setOpaque(false);
        membersHint.setAlignmentX(LEFT_ALIGNMENT);

        JPanel memberTexts = new JPanel();
        memberTexts.setOpaque(false);
        memberTexts.setLayout(new BoxLayout(memberTexts, BoxLayout.Y_AXIS));
        JLabel memberTitle = new JLabel("Thành viên");
        memberTitle.setFont(Typography.h3());
        memberTitle.setForeground(Palette.TEXT);
        memberTitle.setAlignmentX(LEFT_ALIGNMENT);
        memberTexts.add(memberTitle);
        memberTexts.add(membersHint);
        membersHeader.add(memberTexts, BorderLayout.CENTER);

        if (role instanceof com.java.teamtask.elements.owners) {
            AppButton addMemberButton =
                    new AppButton("Thêm thành viên", AppButton.Variant.PRIMARY).withIcon("plus");
            addMemberButton.addActionListener(e -> addMember());
            JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            wrapper.setOpaque(false);
            wrapper.add(addMemberButton);
            membersHeader.add(wrapper, BorderLayout.EAST);
        }
        membersHeader.revalidate();
        membersHeader.repaint();

        tasksHeader.removeAll();
        tasksHeader.setOpaque(false);
        tasksHint.setAlignmentX(LEFT_ALIGNMENT);

        JPanel taskTexts = new JPanel();
        taskTexts.setOpaque(false);
        taskTexts.setLayout(new BoxLayout(taskTexts, BoxLayout.Y_AXIS));
        JLabel tasksTitle = new JLabel("Công việc");
        tasksTitle.setFont(Typography.h3());
        tasksTitle.setForeground(Palette.TEXT);
        tasksTitle.setAlignmentX(LEFT_ALIGNMENT);
        taskTexts.add(tasksTitle);
        taskTexts.add(tasksHint);
        tasksHeader.add(taskTexts, BorderLayout.CENTER);

        if (role instanceof com.java.teamtask.elements.owners) {
            AppButton create =
                    new AppButton("Tạo công việc", AppButton.Variant.PRIMARY).withIcon("plus");
            create.addActionListener(e -> createTask());
            JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            wrapper.setOpaque(false);
            wrapper.add(create);
            tasksHeader.add(wrapper, BorderLayout.EAST);
        }
        tasksHeader.revalidate();
        tasksHeader.repaint();
    }

    /** Mở dự án và khởi tạo instance quyền owner/member qua {@link UserSession}. */
    public void open(projectsAssignments assignment) {
        if (assignment == null || assignment.getProject() == null) {
            close();
            return;
        }
        applyAssignment(assignment, true);
        if (!autoRefresh.isRunning()) {
            autoRefresh.start();
        }
    }

    private void applyAssignment(projectsAssignments fresh, boolean reloadMembers) {
        this.assignment = fresh;
        this.project = fresh.getProject();
        this.role = session.openProject(fresh);
        rebuildHeaders();
        header.setTitle(project.getName());
        header.setSubtitle(
                UiMapper.roleLabel(fresh.getRole())
                        + " · Hạn chót "
                        + UiMapper.formatDate(project.getDL()));

        if (reloadMembers) {
            loadMembers();
        }
        rebuildActions();
        render(reloadMembers);
        lastSignature = signature(fresh);
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

            AppButton delete =
                    new AppButton("Xoá dự án", AppButton.Variant.DANGER_OUTLINE).withIcon("trash");
            delete.addActionListener(e -> deleteProject());
            headerActions.add(edit);
            headerActions.add(delete);
        }
        headerActions.revalidate();
        headerActions.repaint();
    }

    private void render(boolean renderMembers) {
        int memberScroll = membersScroll.getVerticalScrollBar().getValue();
        int taskScroll = tasksScroll.getVerticalScrollBar().getValue();
        if (renderMembers) {
            renderMembersList();
        }
        renderTasksList();
        updateProgress();
        SwingUtilities.invokeLater(
                () -> {
                    restoreScroll(membersScroll, memberScroll);
                    restoreScroll(tasksScroll, taskScroll);
                });
        revalidate();
        repaint();
    }

    private static void restoreScroll(JScrollPane scroll, int value) {
        JScrollBar bar = scroll.getVerticalScrollBar();
        int max = Math.max(0, bar.getMaximum() - bar.getVisibleAmount());
        bar.setValue(Math.min(value, max));
    }

    private void updateProgress() {
        List<tasks> currentTasks =
                role == null ? project == null ? List.of() : project.getTasksList() : role.viewTasks();
        if (currentTasks == null || currentTasks.isEmpty()) {
            progressBar.setValue(0);
            progressText.setText("Chưa khởi động dự án");
            progressDetail.setText("0 công việc");
            return;
        }

        Double value = role == null ? null : role.viewOverallProgress();
        double raw = value == null ? 0.0 : value;
        int percent = (int) Math.round(Math.max(0.0, Math.min(1.0, raw)) * 100);
        progressBar.setValue(percent);
        progressText.setText("Đã hoàn thành " + percent + "%");
        progressDetail.setText(currentTasks.size() + " công việc");
    }

    private void renderMembersList() {
        membersList.removeAll();
        if (members.isEmpty()) {
            JPanel empty = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, Space.MD));
            empty.setOpaque(false);
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            JLabel label = new JLabel("Chưa có thành viên nào trong dự án.");
            label.setFont(Typography.body());
            label.setForeground(Palette.TEXT_MUTED_STRONG);
            empty.add(label);
            membersList.addRow(empty, 0);
        } else {
            for (projectsAssignments member : members) {
                membersList.addRow(memberRow(member), Space.SM);
            }
        }
        membersHint.setText(members.size() + " người");
    }

    private void renderTasksList() {
        tasksList.removeAll();
        if (project == null || project.getTasksList().isEmpty()) {
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
            tasksList.addRow(holder, 0);
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
                tasksList.addRow(card, Space.SM);
            }
        }
        tasksHint.setText(project.getTasksList().size() + " công việc");
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
                clearState();
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
            projectsAssignments fresh =
                    gateway.reloadProject(session.getUser(), project.getId());
            applyReload(fresh, true, true);
        } catch (DataAccessException ex) {
            showError(ex.getMessage());
        }
    }

    /** Refresh nền: auto thì im lặng, thủ công thì báo lỗi và tải cả thành viên. */
    private void refreshAsync(boolean showErrors) {
        if (project == null || !session.isAuthenticated() || refreshing) {
            return;
        }
        if (!showErrors && (!isShowing() || hasVisibleDialog())) {
            return;
        }

        int projectId = project.getId();
        refreshing = true;
        refreshButton.setEnabled(false);
        new SwingWorker<projectsAssignments, Void>() {
            @Override
            protected projectsAssignments doInBackground() {
                return gateway.reloadProject(session.getUser(), projectId);
            }

            @Override
            protected void done() {
                try {
                    projectsAssignments fresh = get();
                    if (project == null || project.getId() != projectId) {
                        return;
                    }
                    applyReload(fresh, showErrors, showErrors);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    if (showErrors) {
                        showError(messageOf(ex.getCause()));
                    }
                } finally {
                    refreshing = false;
                    refreshButton.setEnabled(true);
                }
            }
        }.execute();
    }

    private void applyReload(
            projectsAssignments fresh, boolean reloadMembers, boolean force) {
        if (fresh == null) {
            close();
            return;
        }
        if (!force && signature(fresh).equals(lastSignature)) {
            return;
        }
        syncSessionProject(fresh);
        applyAssignment(fresh, reloadMembers);
        onProjectsChanged.run();
    }

    private void syncSessionProject(projectsAssignments fresh) {
        if (!session.isAuthenticated() || fresh == null || fresh.getProject() == null) {
            return;
        }
        users user = session.getUser();
        int projectId = fresh.getProject().getId();
        user.getRelatedProjects()
                .entrySet()
                .removeIf(entry -> entry.getKey() != null && entry.getKey().getId() == projectId);
        user.putAssignment(fresh.getProject(), fresh);
    }

    private static String signature(projectsAssignments assignment) {
        if (assignment == null || assignment.getProject() == null) {
            return "";
        }
        projects current = assignment.getProject();
        StringBuilder signature = new StringBuilder();
        signature
                .append(assignment.getRole())
                .append('|')
                .append(current.getId())
                .append('|')
                .append(current.getName())
                .append('|')
                .append(current.getDescription())
                .append('|')
                .append(current.getDL())
                .append('|')
                .append(current.getUpdateTime())
                .append('|');
        for (tasks task : current.getTasksList()) {
            signature
                    .append(task.getTaskid())
                    .append(':')
                    .append(task.getTaskStatus())
                    .append(':')
                    .append(task.getTaskDeadline())
                    .append(':')
                    .append(task.getAssginee() == null ? 0 : task.getAssginee().userId)
                    .append(';');
        }
        return signature.toString();
    }

    private boolean hasVisibleDialog() {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window == null) {
            return false;
        }
        for (Window owned : window.getOwnedWindows()) {
            if (owned.isVisible()) {
                return true;
            }
        }
        return false;
    }

    private static String messageOf(Throwable error) {
        if (error == null || error.getMessage() == null || error.getMessage().isBlank()) {
            return "Không thể tải lại dự án.";
        }
        return error.getMessage();
    }

    private void clearState() {
        autoRefresh.stop();
        session.clearActiveProject();
        assignment = null;
        project = null;
        role = null;
        lastSignature = null;
    }

    /** Rời dự án: xoá ngay instance owner/member và quay về Dashboard. */
    public void close() {
        clearState();
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

    public void setCompact(boolean compact) {
        header.setCompact(compact);
    }
}
