package com.java.teamtask.elements;

import com.java.teamtask.connections.SQL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.*;

public class owners implements projectsManage {
  private users user;
  private projects project;

  public owners(users Users, projects Projects) {
    this.user = Users;
    this.project = Projects;
  }

  @Override
  public void deleteMember(members Member) {}

  @Override
  public void addMember(users User) {}

  @Override
  public void assignTask(users Member, ArrayList<tasks> Tasks, Connection conn) {
    for (tasks e : Tasks) {
      e.assginee = Member;
      this.project.tasksList.add(e);
    }
  }

  @Override
  public void updateTask(
      tasks Tasks, String taskName, String taskStatus, String taskDescription, String taskDL) {

    ArrayList<String> changingData =
        Tasks.updatetask(taskName, taskStatus, taskDescription, taskDL);
    SQL.taskUpdate(changingData);
  }

  @Override
  public void updateProject(Connection conn, int id) {}

  @Override
  public String deleteProject(Connection conn, int pid) {
    String delProj = "DELETE FROM Projects WHERE Project_id = ?";
    String delTask = "DELETE FROM Tasks WHERE Project_id = ?";
    try (conn;
    PreparedStatement del_p = conn.prepareStatement(delProj)) {
      del_p.setLong(1, pid);
      del_p.executeUpdate();

      ArrayList<projects> Projects = new ArrayList<>();
      for (projects p : SQL.selectRelatedProject(conn, user).keySet()) {
        Projects.add(p);
      }
      Projects.removeIf(p -> p.id == pid);
      return "Đã xóa project";
    } catch (Exception e) {
      return "Lỗi xóa project";
    }
  }

  @Override
  public String viewProjectInfo() {}

  @Override
  public members[] viewMembers() {}

  @Override
  public String viewMemberInfo(members Member) {}

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
  public Double viewMemberProgress(members Member) {}

  @Override
  public String viewTask() {} // xem thông tin chi tiết task

  @Override
  public String commentTask(tasks Tasks, String comment) {}
}
