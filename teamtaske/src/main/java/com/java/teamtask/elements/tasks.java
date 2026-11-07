package com.java.teamtask.elements;

import com.java.teamtask.connections.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;

/** tasks */
public class tasks {
  protected int taskid;
  protected String taskName, taskDescription, taskStatus;
  protected LocalDateTime taskCreatedAt, taskLastUpdatedAt, taskDL;
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

  public HashMap<members, String> takeComment() {
    return SQL.getComments();
  }

  public String viewTaskStatus() {
    return this.taskStatus;
  }

  public ArrayList<String> updatetask(
      String taskName, String taskStatus, String taskDescription, String taskDL) {
    ArrayList<String> ans = new ArrayList<>();
    if (taskName != "") {
      this.taskName = taskName;
      ans.add(taskName);
    } else {
      ans.add(this.taskName);
    }

    ans.add(this.taskCreatedAt.toString());
    this.taskLastUpdatedAt = LocalDateTime.now();
    ans.add(this.taskLastUpdatedAt.toString());
    if (taskDL != "") {
      this.taskDL = LocalDateTime.parse(taskDL);
      ans.add(taskDL);
    } else {
      ans.add(this.taskDL.toString());
    }
    if (taskStatus != "") {
      this.taskStatus = taskStatus;
      ans.add(taskStatus);
    } else {
      ans.add(this.taskStatus);
    }
    if (taskDescription != "") {
      this.taskName = taskDescription;
      ans.add(taskDescription);
    } else {
      ans.add(this.taskDescription);
    }
    return ans;
  }

  public void deleteTask(int taskid) {}
}
