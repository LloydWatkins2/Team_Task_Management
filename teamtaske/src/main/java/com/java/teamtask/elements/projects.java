package com.java.teamtask.elements;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;

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
    this.tasksList = list == null ? new ArrayList<>() : list;
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
    this.tasksList = list == null ? new ArrayList<>() : list;
  }

  public int getId() {
    return this.id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDes() {
    return this.description;
  }

  public String getDescription() {
    return this.description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public LocalDateTime getDL() {
    return this.deadline;
  }

  public void setDeadline(LocalDateTime deadline) {
    this.deadline = deadline;
  }

  public LocalDateTime getCreateTime() {
    return this.createdAt;
  }

  public LocalDateTime getUpdateTime() {
    return this.lastUpdatedAt;
  }

  public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) {
    this.lastUpdatedAt = lastUpdatedAt;
  }

  public ArrayList<tasks> getTasksList() {
    if (this.tasksList == null) {
      this.tasksList = new ArrayList<>();
    }
    return this.tasksList;
  }

  public void setTasksList(ArrayList<tasks> tasksList) {
    this.tasksList = tasksList == null ? new ArrayList<>() : tasksList;
  }

  // public tasks getTaskById(int taskId) {
  //   for (tasks task : getTasksList()) {
  //     if (task.getTaskid() == taskId) {
  //       return task;
  //     }
  //   }
  //   return null;
  // }
  //
  /** Thêm task vào project và database. Trả về task vừa tạo hoặc null nếu lỗi. */
  public tasks addTasks(
      Connection conn,
      String name,
      String des,
      String status,
      LocalDateTime dl,
      users user)
      throws SQLException {
    if (conn == null || user == null || name == null || name.isBlank()) {
      return null;
    }
    String sql =
        "INSERT INTO tasks(taskname, taskdescription, taskdeadline, taskstatus, "
            + "taskcreatedat, tasklastupdatedat, user_id, project_id) "
            + "VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime due = dl == null ? now.plusDays(1) : dl;
    String taskStatus = status == null || status.isBlank() ? "TODO" : status;

    try (PreparedStatement stmt =
        conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      stmt.setString(1, name.trim());
      stmt.setString(2, des == null ? "" : des.trim());
      stmt.setTimestamp(3, Timestamp.valueOf(due));
      stmt.setString(4, taskStatus);
      stmt.setTimestamp(5, Timestamp.valueOf(now));
      stmt.setTimestamp(6, Timestamp.valueOf(now));
      stmt.setInt(7, user.userId);
      stmt.setInt(8, this.id);
      if (stmt.executeUpdate() == 0) {
        return null;
      }
      try (ResultSet rs = stmt.getGeneratedKeys()) {
        if (!rs.next()) {
          return null;
        }
        tasks task =
            new tasks(
                rs.getInt(1),
                name.trim(),
                des == null ? "" : des.trim(),
                taskStatus,
                now,
                now,
                due,
                user);
        getTasksList().add(task);
        return task;
      }
    }
  }

  /** Xóa task cùng comments của task. */
  public String removeTasks(Connection conn, int taskId) {
    if (conn == null) {
      return "Lỗi xóa task";
    }
    String deleteCommentsSql = "DELETE FROM comments WHERE taskid = ?";
    String deleteTaskSql = "DELETE FROM tasks WHERE taskid = ?";
    boolean oldAutoCommit = true;
    try {
      oldAutoCommit = conn.getAutoCommit();
      conn.setAutoCommit(false);
      try (PreparedStatement stmt = conn.prepareStatement(deleteCommentsSql)) {
        stmt.setInt(1, taskId);
        stmt.executeUpdate();
      }
      try (PreparedStatement stmt = conn.prepareStatement(deleteTaskSql)) {
        stmt.setInt(1, taskId);
        stmt.executeUpdate();
      }
      getTasksList().removeIf(task -> task.getTaskid() == taskId);
      conn.commit();
      return "Đã xóa task";
    } catch (SQLException e) {
      try {
        conn.rollback();
      } catch (SQLException ignored) {
        // bỏ qua
      }
      return "Lỗi xóa task";
    } finally {
      try {
        conn.setAutoCommit(oldAutoCommit);
      } catch (SQLException ignored) {
        // bỏ qua
      }
    }
  }
}
