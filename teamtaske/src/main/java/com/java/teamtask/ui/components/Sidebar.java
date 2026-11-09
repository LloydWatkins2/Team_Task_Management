package com.java.teamtask.ui.components;

import com.java.teamtask.elements.projectsAssignments;
import com.java.teamtask.ui.data.UiMapper;
import com.java.teamtask.ui.theme.Layout;
import com.java.teamtask.ui.theme.Palette;
import com.java.teamtask.ui.theme.Space;
import com.java.teamtask.ui.theme.Typography;
import com.java.teamtask.ui.theme.UiScale;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

/** Sidebar: logo, menu chính, danh sách dự án động và khối người dùng. */
public final class Sidebar extends JPanel {
    public interface Listener {
        void onNavigate(String key);
    }

    private final List<NavItem> items = new ArrayList<>();
    private final List<String> keys = new ArrayList<>();
    private final ButtonGroup group = new ButtonGroup();
    private final Listener listener;
    private final JLabel menuLabel = sectionLabel("MENU");
    private final JLabel projectLabel = sectionLabel("DỰ ÁN");
    private final JPanel projectsPanel = new JPanel();
    private final JPanel avatarHolder = new JPanel(new BorderLayout());
    private final JLabel userNameLabel = new JLabel();

    public Sidebar(String userName, Listener listener) {
        this.listener = listener == null ? key -> {
        } : listener;
        setOpaque(true);
        setBackground(Palette.SHELL);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(UiScale.px(Layout.SIDEBAR_WIDTH), 0));
        setBorder(new EmptyBorder(Layout.sidebarPadding(Layout.SHELL_MAX_WIDTH)));

        add(logo());
        add(Box.createVerticalStrut(Space.X5L));
        add(menuLabel);
        add(Box.createVerticalStrut(Space.SM));

        addNav("dashboard", "Tổng quan", "dashboard");
        addNav("tasks", "Công việc", "tasks");
        addNav("projects", "Dự án", "projects");
        addNav("calendar", "Lịch", "calendar");
        addNav("team", "Thành viên", "users");
        addNav("settings", "Hồ sơ", "user");

        add(Box.createVerticalStrut(Space.X3L));
        add(divider());
        add(Box.createVerticalStrut(Space.XXL));
        add(projectLabel);
        add(Box.createVerticalStrut(Space.SM));

        projectsPanel.setOpaque(false);
        projectsPanel.setLayout(new BoxLayout(projectsPanel, BoxLayout.Y_AXIS));
        projectsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        projectsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        add(projectsPanel);

        add(Box.createVerticalGlue());
        add(divider());
        add(Box.createVerticalStrut(Space.LG));
        add(userBlock());

        setUserName(userName);
        setProjects(List.of());
        select("dashboard");
    }

    private JPanel logo() {
        JPanel row = new JPanel(new BorderLayout(Space.SM, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, UiScale.px(44)));
        row.add(new CircleIconPanel(44, Palette.PRIMARY, "logo", Palette.SHELL), BorderLayout.WEST);

        JLabel word = new JLabel("Taskia");
        word.setFont(Typography.logo());
        word.setForeground(Palette.TEXT);
        row.add(word, BorderLayout.CENTER);
        return row;
    }

    private static JLabel sectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Typography.small());
        label.setForeground(Palette.TEXT_MUTED_STRONG);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private void addNav(String key, String text, String icon) {
        NavItem item = new NavItem(text, icon);
        item.setAlignmentX(Component.LEFT_ALIGNMENT);
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, UiScale.px(48)));
        item.addActionListener(e -> {
            if (!item.isSelected()) {
                item.setSelected(true);
            }
            listener.onNavigate(key);
        });
        group.add(item);
        items.add(item);
        keys.add(key);
        add(item);
        add(Box.createVerticalStrut(Space.SM));
    }

    private static JPanel divider() {
        JPanel divider = new JPanel();
        divider.setOpaque(true);
        divider.setBackground(Palette.BORDER);
        divider.setAlignmentX(Component.LEFT_ALIGNMENT);
        divider.setPreferredSize(new Dimension(Integer.MAX_VALUE, 1));
        divider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        divider.setMinimumSize(new Dimension(0, 1));
        return divider;
    }

    private JPanel userBlock() {
        JPanel row = new JPanel(new BorderLayout(Space.SM, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, UiScale.px(48)));

        avatarHolder.setOpaque(false);
        avatarHolder.setPreferredSize(new Dimension(UiScale.px(40), UiScale.px(40)));
        row.add(avatarHolder, BorderLayout.WEST);

        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        userNameLabel.setFont(Typography.meta());
        userNameLabel.setForeground(Palette.TEXT);
        userNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel role = new JLabel("Thành viên");
        role.setFont(Typography.small());
        role.setForeground(Palette.TEXT_MUTED_STRONG);
        role.setAlignmentX(Component.LEFT_ALIGNMENT);
        texts.add(userNameLabel);
        texts.add(role);
        row.add(texts, BorderLayout.CENTER);

        IconButton logout = new IconButton("logout", "Đăng xuất", 44, true);
        logout.addActionListener(e -> listener.onNavigate("logout"));
        row.add(logout, BorderLayout.EAST);
        return row;
    }

    /** Đổi tên hiển thị ở khối người dùng và avatar. */
    public void setUserName(String userName) {
        String display = userName == null || userName.isBlank() ? "Người dùng" : userName;
        userNameLabel.setText(display);
        avatarHolder.removeAll();
        avatarHolder.add(new Avatar(display, 40), BorderLayout.CENTER);
        avatarHolder.revalidate();
        avatarHolder.repaint();
    }

    /** Cập nhật danh sách dự án của người dùng hiện tại; không hiển thị dự án ngoài phạm vi. */
    public void setProjects(List<projectsAssignments> assignments) {
        for (int i = keys.size() - 1; i >= 0; i--) {
            if (keys.get(i).startsWith("project:")) {
                NavItem removed = items.remove(i);
                keys.remove(i);
                group.remove(removed);
            }
        }
        projectsPanel.removeAll();

        if (assignments != null) {
            for (projectsAssignments assignment : assignments) {
                if (assignment == null || assignment.getProject() == null) {
                    continue;
                }
                addProject(assignment);
            }
        }

        boolean visible = projectsPanel.getComponentCount() > 0;
        projectLabel.setVisible(visible);
        projectsPanel.setVisible(visible);
        projectsPanel.revalidate();
        projectsPanel.repaint();
        revalidate();
        repaint();
    }

    private void addProject(projectsAssignments assignment) {
        String name = assignment.getProject().getName();
        String text = name == null || name.isBlank() ? "Dự án" : name;
        if (text.length() > 24) {
            text = text.substring(0, 21) + "...";
        }
        String key = "project:" + assignment.getProject().getId();
        NavItem item = new NavItem(text, "projects");
        item.setToolTipText(
            UiMapper.displayName(assignment.getUser()) + " · " + UiMapper.roleLabel(assignment.getRole()));
        item.setAlignmentX(Component.LEFT_ALIGNMENT);
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, UiScale.px(48)));
        item.addActionListener(e -> {
            if (!item.isSelected()) {
                item.setSelected(true);
            }
            listener.onNavigate(key);
        });
        group.add(item);
        items.add(item);
        keys.add(key);
        projectsPanel.add(item);
        projectsPanel.add(Box.createVerticalStrut(Space.SM));
    }

    /** Chọn mục menu theo key mà không phát sự kiện điều hướng. */
    public void select(String key) {
        for (int i = 0; i < keys.size(); i++) {
            if (keys.get(i).equals(key)) {
                items.get(i).setSelected(true);
                return;
            }
        }
    }

    public void setCompact(boolean compact) {
        setPreferredSize(new Dimension(
                UiScale.px(compact ? Layout.SIDEBAR_WIDTH_COMPACT : Layout.SIDEBAR_WIDTH), 0));
        setBorder(new EmptyBorder(Layout.sidebarPadding(compact ? Layout.BP_SM : Layout.SHELL_MAX_WIDTH)));
        boolean hasProjects = projectsPanel.getComponentCount() > 0;
        projectLabel.setVisible(!compact && hasProjects);
        projectsPanel.setVisible(!compact && hasProjects);
        revalidate();
        repaint();
    }
}
