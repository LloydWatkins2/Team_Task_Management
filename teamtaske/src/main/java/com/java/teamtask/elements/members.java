package com.java.teamtask.elements;

import java.util.*;
import java.sql.Connection;
import com.java.teamtask.connections.SQL;

public class members implements seeProjects {
  private users user;
  private projects project;

  public members(users User, projects Project) {
    this.user = User;
    this.project = Project;
  }

  @Override
  public projects viewProjectInfo(Connection conn, int pid) {
    ArrayList<projects> Projects = new ArrayList<>();
    for (projects p : SQL.selectRelatedProject(conn, user).keySet()) {
      Projects.add(p);
    }

    for (projects p : Projects) {
      if (p.id == pid) {
        return p;
      }
    }
    return null;
  }

  @Override
  public String viewMembers(Connection conn) {}

  @Override
  public Double viewOverrallProgress() {
    int goal = this.project.tasksList.size();
    int cnt = 0;
    for (tasks t : this.project.tasksList) {
      if (t.taskStatus == "Hoàn thành") cnt += 1;
    }
    return (double) (cnt / goal);
  }

  @Override
  public Double viewMemberProgress(
    Connection conn,
    members k
  ) {}

  @Override
  public String viewTask(Connection conn) {} // xem thông tin chi tiết của task

  @Override
  public String commentTask(
    Connection conn,
    tasks tk,
    String cmt
  ) {}

  @Override 
  public String viewMemberInfo(
    Connection conn,
    members k
  ) {}
}
