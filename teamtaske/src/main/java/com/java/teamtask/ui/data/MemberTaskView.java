package com.java.teamtask.ui.data;

import com.java.teamtask.ui.components.TaskCard;

/** Công việc liên quan đến một thành viên, chỉ chứa dữ liệu cần hiển thị trong popup. */
public record MemberTaskView(String name, TaskCard.Status status) {}
