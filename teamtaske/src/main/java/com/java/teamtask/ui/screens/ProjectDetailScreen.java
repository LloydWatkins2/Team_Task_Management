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
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Chi tiết dự án: thông tin, 2 cột bên trái Thành viên / bên phải Công việc,
 * lấp đầy cửa sổ với font tự co giãn theo kích thước.
 */
public final class ProjectDetailScreen extends JPanel {
    private final SessionGateway gateway;
    private final UserSession session;
    private final Runnable onBack;
    private final Runnable onProjectsChanged;
    private final PageHeader header;
    private final JPanel headerActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, Space.SM, 0));

    private projectsAssignments assignment;
    private projects project;
    private seeProjects role;
    private List<projectsAssignments> members = new ArrayList<>();

    // Two-column layout
    private final JSplitPane splitPane;
    private final StackPanel membersList;
    private final StackPanel tasksList;

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

        // Two-column body
        membersList = new StackPanel();
        membersList.setAlignmentX(Component.LEFT_ALIGNMENT);

        tasksList = new StackPanel();
        tasksList.setAlignmentX(Component.LEFT_ALIGNMENT);

        JScrollPane membersScroll = new JScrollPane(membersList);
        membersScroll.setBorder(null);
        membersScroll.setOpaque(false);
        membersScroll.getViewport().setOpaque(false);
        membersScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        membersScroll.getVerticalScrollBar().setUnitIncrement(UiScale.px(18));

        JScrollPane tasksScroll = new JScrollPane(tasksList);
        tasksScroll.setBorder(null);
        tasksScroll.setOpaque(false);
        tasksScroll.getViewport().setOpaque(false);
        tasksScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        tasksScroll.getVerticalScrollBar().setUnitIncrement(UiScale.px(18));

        // Left column: Members - wrapped in a Card
        JPanel leftColumn = new JPanel(new BorderLayout());
        leftColumn.setOpaque(false);
        Card membersCard = new Card(new BorderLayout());
        membersCard.setBorder(new EmptyBorder(Space.MD, Space.MD, Space.MD, Space.MD));
        membersCard.add(membersHeader(), BorderLayout.NORTH);
        membersCard.add(membersScroll, BorderLayout.CENTER);
        leftColumn.add(membersCard, BorderLayout.CENTER);

        // Right column: Tasks - wrapped in a Card
        JPanel rightColumn = new JPanel(new BorderLayout());
        rightColumn.setOpaque(false);
        Card tasksCard = new Card(new BorderLayout());
        tasksCard.setBorder(new EmptyBorder(Space.MD, Space.MD, Space.MD, Space.MD));
        tasksCard.add(tasksHeader(), BorderLayout.NORTH);
        tasksCard.add(tasksScroll, BorderLayout.CENTER);
        rightColumn.add(tasksCard, BorderLayout.CENTER);

        // Split pane with 35% / 65% initial split
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftColumn, rightColumn);
        splitPane.setOpaque(false);
        splitPane.setDividerSize(1);
        splitPane.setResizeWeight(0.35);
        splitPane.setContinuousLayout(true);
        splitPane.setBorder(null);

        // Wrap split pane in a container with padding
        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(Space.MD, Space.MD, Space.MD, Space.MD));
        body.add(splitPane, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);

        // Listen for resize to adjust fonts
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                adjustFontsForWidth();
            }
        });
    }

    private JPanel membersHeader() {
        JPanel row = new JPanel(new BorderLayout(Space.MD, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, UiScale.px(48)));

        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        JLabel titleLabel = new JLabel("Thành viên");
        titleLabel.setFont(Typography.h3());
        titleLabel.setForeground(Palette.TEXT);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel hintLabel = new JLabel(members.size() + " người");
        hintLabel.setFont(Typography.small());
        hintLabel.setForeground(Palette.TEXT_MUTED_STRONG);
        hintLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        texts.add(titleLabel);
        texts.add(hintLabel);
        row.add(texts, BorderLayout.CENTER);

        if (role instanceof com.java.teamtask.elements.owners) {
            AppButton addMember = new AppButton("Thêm thành viên", AppButton.Variant.PRIMARY).withIcon("plus");
            addMember.addActionListener(e -> addMember());
            JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            wrapper.setOpaque(false);
            wrapper.add(addMember);
            row.add(wrapper, BorderLayout.EAST);
        }
        return row;
    }

    private JPanel tasksHeader() {
        JPanel row = new JPanel(new BorderLayout(Space.MD, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, UiScale.px(48)));

        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        JLabel titleLabel = new JLabel("Công việc");
        titleLabel.setFont(Typography.h3());
        titleLabel.setForeground(Palette.TEXT);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel hintLabel = new JLabel(project != null ? project.getTasksList().size() + " công việc" : "0 công việc");
        hintLabel.setFont(Typography.small());
        hintLabel.setForeground(Palette.TEXT_MUTED_STRONG);
        hintLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        texts.add(titleLabel);
        texts.add(hintLabel);
        row.add(texts, BorderLayout.CENTER);

        if (role instanceof com.java.teamtask.elements.owners) {
            AppButton create = new AppButton("Tạo công việc", AppButton.Variant.PRIMARY).withIcon("plus");
            create.addActionListener(e -> createTask());
            JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            wrapper.setOpaque(false);
            wrapper.add(create);
            row.add(wrapper, BorderLayout.EAST);
        }
        return row;
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

            AppButton delete =
                    new AppButton("Xoá dự án", AppButton.Variant.DANGER_OUTLINE).withIcon("trash");
            delete.addActionListener(e -> deleteProject());
            headerActions.add(edit);
            headerActions.add(delete);
        }
        headerActions.revalidate();
        headerActions.repaint();
    }

    private void render() {
        renderMembersList();
        renderTasksList();
        adjustFontsForWidth();
        revalidate();
        repaint();
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
        // Update header hint
        JPanel membersHeader = findHeader(membersList, "Thành viên");
        if (membersHeader != null) {
            JLabel hint = findChild(membersHeader, JLabel.class, l -> l.getFont() == Typography.small());
            if (hint != null) {
                hint.setText(members.size() + " người");
            }
        }
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
        // Update header hint
        JPanel tasksHeader = findHeader(tasksList, "Công việc");
        if (tasksHeader != null) {
            JLabel hint = findChild(tasksHeader, JLabel.class, l -> l.getFont() == Typography.small());
            if (hint != null) {
                hint.setText(project.getTasksList().size() + " công việc");
            }
        }
    }

    private JPanel findHeader(StackPanel list, String title) {
        for (Component c : list.getComponents()) {
            if (c instanceof JPanel panel) {
                for (Component child : panel.getComponents()) {
                    if (child instanceof JPanel header) {
                        for (Component h : header.getComponents()) {
                            if (h instanceof JPanel texts) {
                                for (Component t : texts.getComponents()) {
                                    if (t instanceof JLabel label && title.equals(label.getText())) {
                                        return header;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private <T extends Component> T findChild(Container root, Class<T> type, java.util.function.Predicate<T> filter) {
        if (type.isInstance(root) && filter.test(type.cast(root))) {
            return type.cast(root);
        }
        for (Component child : root.getComponents()) {
            if (child instanceof Container container) {
                T found = findChild(container, type, filter);
                if (found != null) {
                    return found;
                }
            } else if (type.isInstance(child) && filter.test(type.cast(child))) {
                return type.cast(child);
            }
        }
        return null;
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
                : text.replace("&", "&").replace("<", "<").replace(">", ">");
    }

    public void setCompact(boolean compact) {
        header.setCompact(compact);
    }

    /**
     * Tự điều chỉnh kích thước font tiêu đề cột theo chiều rộng panel.
     * Khi cửa sổ co/giãn, font sẽ scale phù hợp.
     */
    private void adjustFontsForWidth() {
        if (splitPane == null) return;
        int width = splitPane.getWidth();
        if (width <= 0) return;

        // Calculate available width per column
        int dividerLoc = splitPane.getDividerLocation();
        if (dividerLoc <= 0) {
            dividerLoc = (int) (width * 0.35);
        }
        int leftWidth = dividerLoc;
        int rightWidth = width - dividerLoc - splitPane.getDividerSize();

        // Scale factor based on column width (reference: 400px = 1.0)
        float leftScale = Math.max(0.75f, Math.min(1.25f, leftWidth / 400f));
        float rightScale = Math.max(0.75f, Math.min(1.25f, rightWidth / 600f));

        // Apply to member list items
        scaleComponentFonts(membersList, leftScale);
        // Apply to task list items
        scaleComponentFonts(tasksList, rightScale);
    }

    private void scaleComponentFonts(Component root, float scale) {
        if (root instanceof JLabel label) {
            java.awt.Font baseFont = label.getFont();
            if (baseFont != null) {
                float newSize = Math.max(10f, baseFont.getSize2D() * scale);
                label.setFont(baseFont.deriveFont(newSize));
            }
        } else if (root instanceof Container container) {
            for (Component child : container.getComponents()) {
                scaleComponentFonts(child, scale);
            }
        }
    }
}