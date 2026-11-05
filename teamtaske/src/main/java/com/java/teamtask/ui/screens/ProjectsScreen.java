package com.java.teamtask.ui.screens;

import com.java.teamtask.ui.Card;
import com.java.teamtask.ui.components.AppButton;
import com.java.teamtask.ui.components.FormField;
import com.java.teamtask.ui.components.PageHeader;
import com.java.teamtask.ui.components.SelectableCard;
import com.java.teamtask.ui.components.SelectableGroup;
import com.java.teamtask.ui.theme.Layout;
import com.java.teamtask.ui.theme.Palette;
import com.java.teamtask.ui.theme.Space;
import com.java.teamtask.ui.theme.Typography;
import com.java.teamtask.ui.theme.UiScale;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/** Màn hình dự án: danh sách card chọn được, 2 cột khi đủ rộng. */
public final class ProjectsScreen extends JPanel {
    private record Project(String name, String description, String members) {
    }

    private final List<Project> projects = new ArrayList<>();
    private final SelectableGroup group = new SelectableGroup();
    private final JPanel grid = new JPanel();
    private final JLabel selectionLabel = new JLabel();
    private final PageHeader header;
    private final AppButton openTasks = new AppButton("Xem công việc", AppButton.Variant.OUTLINE)
            .withIcon("tasks");
    private final Runnable onOpenTasks;

    public ProjectsScreen(Runnable onOpenTasks) {
        this.onOpenTasks = onOpenTasks == null ? () -> {
        } : onOpenTasks;
        setOpaque(false);
        setLayout(new BorderLayout());

        AppButton create = new AppButton("Tạo dự án", AppButton.Variant.PRIMARY).withIcon("plus");
        create.addActionListener(e -> showCreateDialog());
        header = new PageHeader("Dự án của tôi", "", create);

        add(header, BorderLayout.NORTH);
        add(panel(), BorderLayout.CENTER);

        addProject("Website Redesign", "Thiết kế lại website giới thiệu sản phẩm", "6 thành viên");
        addProject("Mobile App", "Ứng dụng quản lý công việc cho iOS và Android", "4 thành viên");
        addProject("Marketing Q4", "Chiến dịch truyền thông quý 4", "3 thành viên");
        updateHeader();
    }

    private JPanel panel() {
        Card panel = new Card(new BorderLayout(0, Space.XXL));
        panel.setBorder(new EmptyBorder(Space.XXL, Space.XXL, Space.XXL, Space.XXL));

        JPanel head = new JPanel();
        head.setOpaque(false);
        head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Dự án của bạn");
        title.setFont(Typography.h3());
        title.setForeground(Palette.TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subtitle = new JLabel("Chọn một dự án để xem công việc liên quan.");
        subtitle.setFont(Typography.body());
        subtitle.setForeground(Palette.TEXT_MUTED_STRONG);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        head.add(title);
        head.add(Box.createVerticalStrut(Space.XXS));
        head.add(subtitle);

        grid.setOpaque(false);
        grid.setLayout(new GridLayout(0, 2, Space.MD, Space.MD));
        JPanel gridWrapper = new JPanel(new BorderLayout());
        gridWrapper.setOpaque(false);
        gridWrapper.add(grid, BorderLayout.NORTH);

        JPanel footer = new JPanel(new BorderLayout(Space.MD, 0));
        footer.setOpaque(false);
        selectionLabel.setFont(Typography.meta());
        selectionLabel.setForeground(Palette.TEXT_MUTED_STRONG);
        openTasks.addActionListener(e -> this.onOpenTasks.run());
        footer.add(selectionLabel, BorderLayout.CENTER);
        footer.add(openTasks, BorderLayout.EAST);

        panel.add(head, BorderLayout.NORTH);
        panel.add(gridWrapper, BorderLayout.CENTER);
        panel.add(footer, BorderLayout.SOUTH);
        return panel;
    }

    private void addProject(String name, String description, String members) {
        Project project = new Project(name, description, members);
        projects.add(project);

        SelectableCard card = new SelectableCard(name, description + " • " + members,
                () -> updateSelection(name));
        group.add(card);
        grid.add(card);
        group.setSelected(card);
        updateSelection(name);
        revalidate();
        repaint();
    }

    private void updateSelection(String name) {
        selectionLabel.setText("Đang chọn: " + name);
    }

    private void updateHeader() {
        header.setSubtitle(projects.size() + " dự án đang tham gia");
    }

    private void showCreateDialog() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, "Tạo dự án",
                Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Palette.CONTENT);
        root.setBorder(new EmptyBorder(Space.XXL, Space.XXL, Space.XXL, Space.XXL));

        Card card = new Card();
        card.setBorder(new EmptyBorder(Space.X3L, Space.X3L, Space.X3L, Space.X3L));
        root.add(card, BorderLayout.CENTER);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Dự án mới");
        title.setFont(Typography.h3());
        title.setForeground(Palette.TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        FormField name = FormField.text("Tên dự án", "Ví dụ: Website Redesign", "projects");
        FormField description = FormField.textArea("Mô tả", "Mục tiêu và phạm vi dự án", 96);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, Space.SM, 0));
        actions.setOpaque(false);
        actions.setAlignmentX(Component.LEFT_ALIGNMENT);
        AppButton cancel = new AppButton("Huỷ", AppButton.Variant.OUTLINE);
        cancel.addActionListener(e -> dialog.dispose());
        AppButton save = new AppButton("Tạo dự án", AppButton.Variant.PRIMARY).withIcon("check");
        save.addActionListener(e -> {
            name.clearError();
            if (name.getValue().isEmpty()) {
                name.setError("Vui lòng nhập tên dự án.");
                name.focusInput();
                return;
            }
            String descriptionValue = description.getValue().isEmpty()
                    ? "Chưa có mô tả" : description.getValue();
            addProject(name.getValue(), descriptionValue, "1 thành viên");
            updateHeader();
            dialog.dispose();
        });

        actions.add(cancel);
        actions.add(save);

        body.add(title);
        body.add(Box.createVerticalStrut(Space.XL));
        body.add(name);
        body.add(Box.createVerticalStrut(Space.XL));
        body.add(description);
        body.add(Box.createVerticalStrut(Space.XXL));
        body.add(actions);

        card.add(body, BorderLayout.CENTER);
        dialog.setContentPane(root);
        dialog.getRootPane().registerKeyboardAction(e -> dialog.dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        dialog.setSize(new Dimension(UiScale.px(520), UiScale.px(500)));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    public void setCompact(boolean compact) {
        header.setCompact(compact);
        if (grid.getLayout() instanceof GridLayout layout) {
            int columns = compact ? 1 : 2;
            if (layout.getColumns() != columns) {
                layout.setColumns(columns);
                grid.revalidate();
                grid.repaint();
            }
        }
    }
}
