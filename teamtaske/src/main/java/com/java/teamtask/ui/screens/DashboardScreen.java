package com.java.teamtask.ui.screens;

import com.java.teamtask.ui.components.AppButton;
import com.java.teamtask.ui.components.EmptyState;
import com.java.teamtask.ui.components.PageHeader;
import com.java.teamtask.ui.components.StackPanel;
import com.java.teamtask.ui.components.TaskCard;
import com.java.teamtask.ui.sample.TaskItem;
import com.java.teamtask.ui.theme.Space;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Dashboard: tiêu đề trang, danh sách công việc và trạng thái rỗng. */
public final class DashboardScreen extends JPanel {
    private final List<TaskItem> tasks = new ArrayList<>();
    private final PageHeader header;
    private final StackPanel list = new StackPanel();
    private final JPanel emptyWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, Space.X6L + Space.XXL));
    private final Consumer<TaskItem> onOpenTask;
    private final Runnable onCreateTask;

    public DashboardScreen(List<TaskItem> tasks, Runnable onCreateTask, Runnable onOpenProjects,
                           Consumer<TaskItem> onOpenTask) {
        this.onOpenTask = onOpenTask == null ? task -> {
        } : onOpenTask;
        this.onCreateTask = onCreateTask == null ? () -> {
        } : onCreateTask;

        setOpaque(false);
        setLayout(new BorderLayout());

        AppButton create = new AppButton("Tạo công việc", AppButton.Variant.PRIMARY).withIcon("plus");
        create.addActionListener(e -> this.onCreateTask.run());

        header = new PageHeader("Công việc của tôi", "", create);
        add(header, BorderLayout.NORTH);
        add(list, BorderLayout.CENTER);

        AppButton emptyCreate = new AppButton("Tạo công việc", AppButton.Variant.PRIMARY).withIcon("plus");
        emptyCreate.addActionListener(e -> this.onCreateTask.run());
        AppButton emptyProjects = new AppButton("Khám phá dự án", AppButton.Variant.OUTLINE)
                .withIcon("projects");
        emptyProjects.addActionListener(e -> {
            if (onOpenProjects != null) {
                onOpenProjects.run();
            }
        });
        emptyWrapper.setOpaque(false);
        emptyWrapper.add(new EmptyState("Chưa có công việc nào",
                "Bắt đầu bằng cách tạo công việc đầu tiên hoặc khám phá các dự án đang diễn ra.",
                emptyCreate, emptyProjects));

        setTasks(tasks == null ? List.of() : tasks);
    }

    public void setTasks(List<TaskItem> newTasks) {
        tasks.clear();
        if (newTasks != null) {
            tasks.addAll(newTasks);
        }
        rebuild();
    }

    public List<TaskItem> getTasks() {
        return tasks;
    }

    private void rebuild() {
        list.removeAll();
        remove(list);
        remove(emptyWrapper);

        if (tasks.isEmpty()) {
            add(emptyWrapper, BorderLayout.CENTER);
            header.setSubtitle("Chưa có công việc nào");
        } else {
            for (TaskItem task : tasks) {
                TaskCard card = new TaskCard(task.title(), task.subtitle(), task.status(),
                        task.project(), task.assignee(), task.deadline(),
                        task.iconBackground(), task.iconName());
                card.setOnOpen(() -> this.onOpenTask.accept(task));
                list.addRow(card, Space.LG);
            }
            add(list, BorderLayout.CENTER);
            long inProgress = tasks.stream()
                    .filter(task -> task.status() == TaskCard.Status.IN_PROGRESS)
                    .count();
            header.setSubtitle(tasks.size() + " công việc • " + inProgress + " đang thực hiện");
        }
        revalidate();
        repaint();
    }

    public void setCompact(boolean compact) {
        header.setCompact(compact);
    }
}
