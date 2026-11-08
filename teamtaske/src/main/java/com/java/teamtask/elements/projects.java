package com.java.teamtask.elements;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class projects {
  protected int id = 0;
  protected String name;
  protected String description;
  protected LocalDateTime deadline;
  protected LocalDateTime createdAt;
  protected LocalDateTime lastUpdatedAt;
  protected ArrayList<tasks> tasksList = new ArrayList<>();

  public projects(
      String n,
      String des,
      LocalDateTime dl,
      LocalDateTime ca,
      LocalDateTime up,
      ArrayList<tasks> list) {
    this.name = n;
    this.description = des;
    this.deadline = dl;
    this.createdAt = ca;
    this.lastUpdatedAt = up;
    this.tasksList = (list != null) ? list : new ArrayList<>();
  }

  public projects(
      int projectsid,
      String n,
      String des,
      LocalDateTime dl,
      LocalDateTime ca,
      LocalDateTime up,
      ArrayList<tasks> list) {
    this.id = projectsid;
    this.name = n;
    this.description = des;
    this.deadline = dl;
    this.createdAt = ca;
    this.lastUpdatedAt = up;
    this.tasksList = (list != null) ? list : new ArrayList<>();
  }

  public String getName() {
    return this.name;
  }

  public String getDes() {
    return this.description;
  }

  public LocalDateTime getDL() {
    return this.deadline;
  }

  public LocalDateTime getCreateTime() {
    return this.createdAt;
  }

  public LocalDateTime getUpdateTime() {
    return this.lastUpdatedAt;
  }

  public ArrayList<tasks> getTasksList() {
    return this.tasksList;
  }

  public tasks addTasks(
      Connection conn, String name, String des, String status, LocalDateTime dl, users User)
      throws SQLException {

    String sql =
        """
        INSERT INTO Tasks(taskName, taskDescription, taskDeadline, taskStatus,
                          taskCreatedAt, taskLastUpdatedAt, user_id, Project_id)
        VALUES(?, ?, ?, ?, ?, ?, ?, ?)
        """;
    LocalDateTime now = LocalDateTime.now();

    try (conn;
        PreparedStatement stmt =
            conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS) // trả về id tự động sinh
        ) {
      stmt.setString(1, name);
      stmt.setString(2, des);
      stmt.setTimestamp(3, Timestamp.valueOf(dl));
      stmt.setString(4, "To do"); // status mặc định lúc mới tạo
      stmt.setTimestamp(5, Timestamp.valueOf(now));
      stmt.setTimestamp(6, Timestamp.valueOf(now));
      stmt.setLong(7, User.userId);
      stmt.setLong(8, this.id); // projectId

      int added = stmt.executeUpdate();
      if (added == 0) {
        throw new SQLException("Lỗi thêm task");
      }
      try (ResultSet rs = stmt.getGeneratedKeys()) {
        if (rs.next()) {
          int id = rs.getInt(1);
          tasks t = new tasks(id, name, description, "To do", now, now, dl, User);
          tasksList.add(t);
          return t;
        }
        throw new SQLException("Không lấy được id");
      }
    } catch (Exception e) {
      System.out.println("Lỗi thêm task");
      return null;
    }
  }

  public String removeTasks(Connection conn, int id) {
    String sql = "DELETE FROM Tasks WHERE taskid = ?";
    try (conn;
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setLong(1, id);
      stmt.executeUpdate();
      
      tasksList.removeIf(t -> t.taskid == id);
      return "Đã xóa task";
    } catch (Exception e) {
      return "Lỗi xóa task";
    }
  }
}
