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
  private projectsAssignments pa;

  public owners(users Users, projects Projects, projectsAssignments pa) {
    this.user = Users;
    this.project = Projects;
    this.pa = pa;
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
    Connection conn,
    String name, String des, LocalDateTime dl
  ) {

    String upProj = "UPDATE Projects SET Name = ?, Description = ?, Deadline = ?, Update_at = ? WHERE Project_id = ?";
    LocalDateTime now = LocalDateTime.now();

    try (conn;
    PreparedStatement stmt = conn.prepareStatement(upProj)) {
      stmt.setString(1, name);
      stmt.setString(2, des);
      stmt.setTimestamp(3, Timestamp.valueOf(dl));
      stmt.setTimestamp(4, Timestamp.valueOf(now)); // Update_at
      stmt.setInt(5, this.project.id);
      
      this.project.name = name;
      this.project.description = des;
      this.project.deadline = dl;
      this.project.lastUpdatedAt = now;

      this.pa.project = project;
      this.user.relatedProjects.put(this.project, pa);
      
      return "Cập nhật thành công";

    } catch (Exception e) {
      return "Lỗi cập nhật project";
    }
  }

  @Override
  public void deleteProject(Connection conn) {

    String delProj = "DELETE FROM Projects WHERE Project_id = ?";

    try (conn;
        PreparedStatement stmt = conn.prepareStatement(delProj)) {
      stmt.setInt(1, this.project.id);
      stmt.executeUpdate();

      this.user.relatedProjects.remove(this.project);

      System.out.println("Đã xóa project");

    } catch (Exception e) {
      System.out.println("Lỗi xóa project" + e);
    }
  } // Sau khi xóa project, instance owner phải bị xóa role, trả về user và trở về dashboard

  @Override
  public projects viewProjectInfo() {
    return this.project;
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
      if (t.taskStatus.equals("Hoàn thành")) cnt += 1;
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
