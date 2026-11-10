package com.java.teamtask.ui.data;

import com.java.teamtask.elements.users;

import java.time.LocalDateTime;
import java.util.List;

/** Thông tin thành viên và tiến độ công việc dùng cho popup chi tiết. */
public record MemberInfoView(
    users user,
    String role,
    LocalDateTime assignedAt,
    double progress,
    int doneTasks,
    int totalTasks,
    List<MemberTaskView> tasks) {}
