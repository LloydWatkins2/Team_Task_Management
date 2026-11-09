package com.java.teamtask.ui.data;

import com.java.teamtask.connections.SQL;
import com.java.teamtask.elements.projects;
import com.java.teamtask.elements.tasks;
import com.java.teamtask.elements.users;
import com.java.teamtask.ui.components.TaskCard;
import com.java.teamtask.ui.theme.Palette;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Ánh xạ entity DB sang dữ liệu hiển thị và nhãn tiếng Việt. */
public final class UiMapper {
  private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
  private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

  private UiMapper() {}

  public static TaskCard.Status statusOf(String status) {
    if (status == null) {
      return TaskCard.Status.TODO;
    }
    return switch (status.trim().toUpperCase(java.util.Locale.ROOT)) {
      case "IN_PROGRESS", "ĐANG LÀM" -> TaskCard.Status.IN_PROGRESS;
      case "DONE", "HOÀN THÀNH" -> TaskCard.Status.DONE;
      case "OVERDUE", "QUÁ HẠN" -> TaskCard.Status.OVERDUE;
      default -> TaskCard.Status.TODO;
    };
  }

  public static String statusLabel(String status) {
    return statusOf(status).label;
  }

  public static String roleLabel(String role) {
    return "owner".equalsIgnoreCase(role) ? "Chủ dự án" : "Thành viên";
  }

  public static String displayName(users user) {
    if (user == null) {
      return "Người dùng";
    }
    String name = user.getFullName();
    return name == null || name.isBlank() ? user.getUsername() : name;
  }

  public static String formatDate(LocalDateTime value) {
    return value == null ? "Chưa đặt" : value.format(DATE);
  }

  public static String formatDateTime(LocalDateTime value) {
    return value == null ? "Chưa đặt" : value.format(DATE_TIME);
  }

  public static TaskView taskView(tasks task, projects project) {
    String description = task.getTaskDescription() == null ? "" : task.getTaskDescription();
    String subtitle = description.isBlank()
        ? "Chưa có mô tả"
        : description.length() > 72 ? description.substring(0, 69) + "..." : description;
    TaskCard.Status status = statusOf(task.getTaskStatus());
    String assignee = task.getAssginee() == null ? "Chưa giao" : displayName(task.getAssginee());
    return new TaskView(
        task,
        task.getTaskName(),
        subtitle,
        status,
        project == null ? "" : project.getName(),
        assignee,
        formatDate(task.getTaskDeadline()),
        iconBackground(status),
        iconName(status),
        description);
  }

  public static CommentView commentView(SQL.CommentRow row) {
    String author = row == null ? "Người dùng" : displayName(row.author());
    String detail = row == null ? "" : row.detail();
    return new CommentView(author, "", detail == null ? "" : detail);
  }

  public static java.awt.Color iconBackground(TaskCard.Status status) {
    return switch (status) {
      case IN_PROGRESS -> Palette.TASK_YELLOW;
      case DONE -> Palette.TASK_PURPLE;
      case OVERDUE -> Palette.TASK_YELLOW;
      default -> Palette.TASK_BLUE;
    };
  }

  public static String iconName(TaskCard.Status status) {
    return switch (status) {
      case IN_PROGRESS -> "clock";
      case DONE -> "check";
      case OVERDUE -> "alert";
      default -> "tasks";
    };
  }
}
