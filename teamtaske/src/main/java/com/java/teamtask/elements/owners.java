package com.java.teamtask.elements;

import com.java.teamtask.connections.SQL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
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
  public void assignTask(ArrayList<tasks> taske, Connection conn) {
    String sql = "UPDATE Tasks SET user_id = ? where task_id = ?";
    try (conn;
        PreparedStatement upt = conn.prepareStatement(sql)) {
      for (tasks e : taske) {
        e.assginee = this.user;
        this.project.tasksList.add(e);
        upt.setInt(1, this.user.userId);
        upt.setInt(2, e.taskid);
        upt.executeUpdate();
      }
      System.out.println(" Update task thành công");
    } catch (Exception e) {
      System.out.println("loi giao viec " + e);
    }
  }

  @Override
  public void updateTask(
      tasks Tasks,
      String taskName,
      String taskStatus,
      String taskDescription,
      LocalDateTime taskDL,
      Connection conn) {
    tasks newTasks =
        new tasks(
            0,
            taskName,
            taskDescription,
            taskStatus,
            LocalDateTime.now(),
            LocalDateTime.now(),
            taskDL,
            this.user);
    Tasks.updatetask(conn, newTasks);
  }

  @Override
  public void updateProject(Connection conn, int id) {}

  @Override
  public String deleteProject(Connection conn, int pid) {
    String delProj = "DELETE FROM Projects WHERE Project_id = ?";
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
  public ArrayList<tasks> viewTasks() {
    return this.project.tasksList;
  }

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
