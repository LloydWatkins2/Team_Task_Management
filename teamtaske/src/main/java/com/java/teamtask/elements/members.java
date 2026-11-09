package com.java.teamtask.elements;

import java.util.*;
import java.sql.Connection;
import com.java.teamtask.connections.SQL;
import java.sql.PreparedStatement;
import java.util.ArrayList;

public class members implements seeProjects {
  private users user;
  private projects project;

  public members(users User, projects Project) {
    this.user = User;
    this.project = Project;
  }

  @Override
  public projects viewProjectInfo() {
    return this.project;
  }

  @Override
  public String viewMembers(Connection conn) {}

  @Override
  public Double viewOverrallProgress() {
    int goal = this.project.tasksList.size();
    int cnt = 0;
    for (tasks t : this.project.tasksList) {
      if (t.taskStatus.equals("Hoàn thành")) cnt += 1;
    }
    return (double) (cnt / goal);
  }

  @Override
  public Double viewMemberProgress(Connection conn, members k) {}

  @Override
  public ArrayList<tasks> viewTasks() {
    return this.project.tasksList;
  } // xem thông tin chi tiết của task

  public tasks viewTasksInfo(tasks tk) {
    return tk;
  }

  @Override
  public void commentTask(Connection conn, tasks Tasks, String comment) {
    String sql = "INSERT INTO Comments VALUES(?,?,?);";
    try (conn;
        PreparedStatement ins = conn.prepareStatement(sql)) {
      ins.setInt(1, Tasks.taskid);
      ins.setInt(2, this.user.userId);
      ins.setString(3, comment);
      ins.executeUpdate();
      Tasks.commentsList.put(this.user, comment);
      System.out.println("Da gui comment");

    } catch (Exception e) {
      System.out.println("Khong the comments, xin thu lai " + e);
    }
  }
}
