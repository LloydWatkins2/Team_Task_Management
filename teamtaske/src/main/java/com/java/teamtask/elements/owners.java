package com.java.teamtask.elements;

import com.java.teamtask.connections.SQL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
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
  public String updateProject(
    Connection conn, int id,
    String name, String des, LocalDateTime dl
  ) {

    String upProj = "UPDATE Projects SET Name = ?, Description = ?, Deadline = ?, Update_at = ?";
    LocalDateTime now = LocalDateTime.now();

    try (conn;
    PreparedStatement stmt = conn.prepareStatement(upProj)) {
      stmt.setString(1, name);
      stmt.setString(2, des);
      stmt.setTimestamp(3, Timestamp.valueOf(dl));
      stmt.setTimestamp(4, Timestamp.valueOf(now)); // Update_at
      
      this.project.name = name;
      this.project.description = des;
      this.project.deadline = dl;
      this.project.lastUpdatedAt = now;
      
      return "Cập nhật thành công";

    } catch (Exception e) {
      return "Lỗi cập nhật project";
    }
  }

  @Override
  public String deleteProject(Connection conn, int pid) {

    String delProj = "DELETE FROM Projects WHERE Project_id = ?";

    try (conn;
        PreparedStatement stmt = conn.prepareStatement(delProj)) {
      stmt.setLong(1, pid);
      stmt.executeUpdate();

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
  public ArrayList<tasks> viewTask() {
    return this.project.tasksList;
  } // xem thông tin chi tiết task

  @Override
  public String commentTask(tasks Tasks, String comment) {}
}
