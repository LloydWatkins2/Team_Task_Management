package com.java.teamtask.ui.data;

import com.java.teamtask.elements.tasks;
import com.java.teamtask.ui.components.TaskCard;

import java.awt.Color;

/** Công việc đã ánh xạ sang dữ liệu hiển thị, giữ tham chiếu entity để cập nhật DB. */
public record TaskView(
    tasks task,
    String title,
    String subtitle,
    TaskCard.Status status,
    String projectName,
    String assignee,
    String deadline,
    Color iconBackground,
    String iconName,
    String description) {}
