package com.java.teamtask.ui.screens;

import com.java.teamtask.ui.Card;
import com.java.teamtask.ui.components.AppButton;
import com.java.teamtask.ui.components.FormField;
import com.java.teamtask.ui.components.PageHeader;
import com.java.teamtask.ui.components.TaskCard;
import com.java.teamtask.ui.sample.TaskItem;
import com.java.teamtask.ui.theme.Palette;
import com.java.teamtask.ui.theme.Space;
import com.java.teamtask.ui.theme.Typography;
import com.java.teamtask.ui.theme.UiScale;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.function.Consumer;

/** Form tạo công việc: card 640, validate inline, nút Lưu/Huỷ. */
public final class TaskFormScreen extends JPanel {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);

    private final FormField name = FormField.text("Tên công việc *", "Ví dụ: Thiết kế trang Tổng quan", "tasks");
    private final FormField description = FormField.textArea("Mô tả", "Mô tả ngắn gọn nội dung công việc", 96);
    private final FormField deadline = FormField.text("Hạn chót *", "dd/MM/yyyy", "calendar");
    private final FormField status = FormField.combo("Trạng thái",
            new String[]{"Cần làm", "Đang làm", "Hoàn thành", "Quá hạn"}, "flag");
    private final FormField project = FormField.combo("Dự án",
            new String[]{"Website Redesign", "Mobile App", "Marketing Q4"}, "projects");
    private final FormField assignee = FormField.combo("Người phụ trách",
            new String[]{"Nguyễn An", "Trần Minh", "Lê Hà", "Phạm Khoa"}, "user");
    private final FormField priority = FormField.combo("Độ ưu tiên",
            new String[]{"Thấp", "Trung bình", "Cao"}, "alert");

    private final List<FormField> fields = List.of(name, description, deadline, status, project, assignee, priority);
    private final PageHeader header;
    private final Consumer<TaskItem> onSave;
    private final Runnable onCancel;
    private Card card;
    private int maxWidth = UiScale.px(640);

    public TaskFormScreen(Runnable onCancel, Consumer<TaskItem> onSave) {
        this.onCancel = onCancel == null ? () -> {
        } : onCancel;
        this.onSave = onSave == null ? task -> {
        } : onSave;

        setOpaque(false);
        setLayout(new BorderLayout());
        header = new PageHeader("Tạo công việc", "Điền thông tin để thêm công việc vào dự án.", null);
        add(header, BorderLayout.NORTH);
        add(holder(), BorderLayout.CENTER);
    }

    private JPanel holder() {
        JPanel holder = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        holder.setOpaque(false);
        holder.add(card());
        return holder;
    }

    private Card card() {
        Card card = new Card() {
            @Override
            public Dimension getPreferredSize() {
                Dimension size = super.getPreferredSize();
                return new Dimension(maxWidth, size.height);
            }
        };
        this.card = card;
        card.setBorder(new EmptyBorder(Space.X3L, Space.X3L, Space.X3L, Space.X3L));
        card.setLayout(new BorderLayout());

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Thông tin công việc");
        title.setFont(Typography.h3());
        title.setForeground(Palette.TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel required = new JLabel("Các trường có dấu * là bắt buộc.");
        required.setFont(Typography.body());
        required.setForeground(Palette.TEXT_MUTED_STRONG);
        required.setAlignmentX(Component.LEFT_ALIGNMENT);

        body.add(title);
        body.add(Box.createVerticalStrut(Space.XXS));
        body.add(required);
        body.add(Box.createVerticalStrut(Space.XXL));
        body.add(name);
        body.add(Box.createVerticalStrut(Space.XL));
        body.add(description);
        body.add(Box.createVerticalStrut(Space.XL));
        body.add(deadline);
        body.add(Box.createVerticalStrut(Space.XL));
        body.add(twoColumns(status, project));
        body.add(Box.createVerticalStrut(Space.XL));
        body.add(twoColumns(assignee, priority));
        body.add(Box.createVerticalStrut(Space.XXL));
        body.add(actions());

        card.add(body, BorderLayout.CENTER);

        name.focusInput();
        return card;
    }

    private JPanel twoColumns(FormField left, FormField right) {
        JPanel row = new JPanel(new GridLayout(1, 2, Space.MD, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.add(left);
        row.add(right);
        return row;
    }

    private JPanel actions() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT, Space.SM, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        AppButton cancel = new AppButton("Huỷ", AppButton.Variant.OUTLINE);
        cancel.addActionListener(e -> {
            reset();
            this.onCancel.run();
        });

        AppButton save = new AppButton("Lưu", AppButton.Variant.PRIMARY).withIcon("check");
        save.addActionListener(e -> submit());

        row.add(cancel);
        row.add(save);
        return row;
    }

    private void submit() {
        fields.forEach(FormField::clearError);

        boolean valid = true;
        if (name.getValue().isEmpty()) {
            name.setError("Vui lòng nhập tên công việc.");
            valid = false;
        }

        LocalDate due = null;
        String deadlineValue = deadline.getValue();
        if (deadlineValue.isEmpty()) {
            deadline.setError("Vui lòng nhập hạn chót.");
            valid = false;
        } else {
            try {
                due = LocalDate.parse(deadlineValue, DATE);
                if (due.isBefore(LocalDate.now())) {
                    deadline.setError("Hạn chót không được trước ngày hôm nay.");
                    valid = false;
                }
            } catch (DateTimeParseException ex) {
                deadline.setError("Định dạng ngày không hợp lệ (dd/MM/yyyy).");
                valid = false;
            }
        }

        if (!valid) {
            for (FormField field : fields) {
                if (field.hasError()) {
                    field.focusInput();
                    break;
                }
            }
            return;
        }

        TaskCard.Status taskStatus = switch (status.getValue()) {
            case "Đang làm" -> TaskCard.Status.IN_PROGRESS;
            case "Hoàn thành" -> TaskCard.Status.DONE;
            case "Quá hạn" -> TaskCard.Status.OVERDUE;
            default -> TaskCard.Status.TODO;
        };

        String descriptionValue = description.getValue().isEmpty()
                ? "Chưa có mô tả" : description.getValue();
        String subtitle = descriptionValue.length() > 64
                ? descriptionValue.substring(0, 61) + "..." : descriptionValue;

        TaskItem task = new TaskItem(name.getValue(), subtitle, taskStatus,
                project.getValue(), assignee.getValue(), deadlineValue,
                switch (taskStatus) {
                    case TODO -> Palette.TASK_BLUE;
                    case IN_PROGRESS -> Palette.TASK_YELLOW;
                    case DONE -> Palette.TASK_PURPLE;
                    case OVERDUE -> Palette.TASK_YELLOW;
                },
                "tasks", descriptionValue);

        reset();
        onSave.accept(task);
    }

    public void reset() {
        name.setValue("");
        description.setValue("");
        deadline.setValue("");
        status.setSelected("Cần làm");
        project.setSelected("Website Redesign");
        assignee.setSelected("Nguyễn An");
        priority.setSelected("Trung bình");
        fields.forEach(FormField::clearError);
        header.setTitle("Tạo công việc");
        header.setSubtitle("Điền thông tin để thêm công việc vào dự án.");
        revalidate();
        repaint();
    }

    public void setCompact(boolean compact) {
        header.setCompact(compact);
    }

    /** Giới hạn bề rộng card theo vùng nội dung khả dụng. */
    public void setMaxWidth(int width) {
        int next = Math.max(UiScale.px(320), Math.min(UiScale.px(640), width));
        if (next != maxWidth) {
            maxWidth = next;
            if (card != null) {
                card.revalidate();
                card.repaint();
                revalidate();
                repaint();
            }
        }
    }
}
