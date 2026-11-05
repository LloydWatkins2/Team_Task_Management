package com.java.teamtask.elements;

import com.java.teamtask.connections.*;
import java.time.LocalDateTime;
import java.util.HashMap;

/** tasks */
public class tasks {
  protected int taskid;
  protected String taskName, taskDescription, taskStatus;
  protected LocalDateTime taskCreatedAt, taskLastUpdatedAt;
  public users assginee;
  public HashMap<users, String> commentsList;

  public tasks(
      int taskid,
      String taskName,
      String taskDescription,
      String taskStatus,
      String taskCreatedAt,
      String taskLastUpdatedAt,
      users assginee) {
    this.taskid = taskid;
    this.taskName = taskName;
    this.taskDescription = taskDescription;
    this.taskStatus = taskStatus;
    this.taskCreatedAt =
        taskCreatedAt == "" ? LocalDateTime.now() : LocalDateTime.parse(taskCreatedAt);
    this.taskLastUpdatedAt =
        taskLastUpdatedAt == "" ? LocalDateTime.now() : LocalDateTime.parse(taskLastUpdatedAt);
    this.assginee = assginee;
    this.commentsList = takeComment();
  }

  public HashMap<users, String> takeComment() {
    return SQL.getComments();
  }

  public String viewTaskStatus() {}

  public void updateTask(String taskName) {}

  public void updateTask(String taskDescription) {}

  public void updateTask(String taskStatus) {}

  public void updateTask(String taskLastUpdatedAt) {}

  public void updateTask(String taskCreatedAt) {}

  public void updateTask(users assginee) {}

  public void deleteTask(int taskid) {}
}
