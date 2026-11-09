package com.java.teamtask.elements;

import java.sql.Connection;
import java.time.LocalDateTime;

/** Các hành động chỉ owner của project mới được thực hiện. */
public interface projectsManage extends seeProjects {
  void deleteMember(members member, Connection conn);

  void addMember(users user, Connection conn);

  void assignTask(tasks task, users assignee, Connection conn);

  void updateTask(
      tasks task,
      String taskName,
      String taskStatus,
      String taskDescription,
      LocalDateTime taskDeadline,
      Connection conn);

  String updateProject(Connection conn, String name, String description, LocalDateTime deadline);

  void deleteProject(Connection conn);
}
