package com.java.teamtask.ui.sample;

import com.java.teamtask.ui.components.TaskCard;

import java.awt.Color;

/** Dữ liệu mẫu cho một công việc (thay bằng model/DAO khi nối cơ sở dữ liệu). */
public record TaskItem(
        String title,
        String subtitle,
        TaskCard.Status status,
        String project,
        String assignee,
        String deadline,
        Color iconBackground,
        String iconName,
        String description) {
}
