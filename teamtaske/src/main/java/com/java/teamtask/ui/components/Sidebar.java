package com.java.teamtask.ui.components;

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

/** Sidebar: logo, menu chính, danh sách dự án và khối người dùng. */
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
        addNav("settings", "Cài đặt", "settings");

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

        addProject("Website Redesign");
        addProject("Mobile App");
        addProject("Marketing Q4");

        add(Box.createVerticalGlue());
        add(divider());
        add(Box.createVerticalStrut(Space.LG));
        add(userBlock(userName));

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

    private void addProject(String name) {
        NavItem item = new NavItem(name, "projects");
        item.setAlignmentX(Component.LEFT_ALIGNMENT);
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, UiScale.px(48)));
        item.addActionListener(e -> {
            if (!item.isSelected()) {
                item.setSelected(true);
            }
            listener.onNavigate("project:" + name);
        });
        group.add(item);
        items.add(item);
        keys.add("project:" + name);
        projectsPanel.add(item);
        projectsPanel.add(Box.createVerticalStrut(Space.SM));
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

    private JPanel userBlock(String userName) {
        JPanel row = new JPanel(new BorderLayout(Space.SM, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, UiScale.px(48)));
        row.add(new Avatar(userName, 40), BorderLayout.WEST);

        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        JLabel name = new JLabel(userName);
        name.setFont(Typography.meta());
        name.setForeground(Palette.TEXT);
        name.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel role = new JLabel("Quản trị viên");
        role.setFont(Typography.small());
        role.setForeground(Palette.TEXT_MUTED_STRONG);
        role.setAlignmentX(Component.LEFT_ALIGNMENT);
        texts.add(name);
        texts.add(role);
        row.add(texts, BorderLayout.CENTER);

        IconButton logout = new IconButton("logout", "Đăng xuất", 44, true);
        logout.addActionListener(e -> listener.onNavigate("logout"));
        row.add(logout, BorderLayout.EAST);
        return row;
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
        projectLabel.setVisible(!compact);
        projectsPanel.setVisible(!compact);
        revalidate();
        repaint();
    }
}
