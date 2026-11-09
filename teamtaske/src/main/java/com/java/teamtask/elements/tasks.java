package com.java.teamtask.elements;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;

/** Một công việc thuộc project. */
public class tasks {
  protected int taskid;
  protected String taskName;
  protected String taskDescription;
  protected String taskStatus;
  protected LocalDateTime taskCreatedAt;
  protected LocalDateTime taskLastUpdatedAt;
  protected LocalDateTime taskDeadline;
  public users assginee;
  public HashMap<users, String> commentsList;

  public tasks(
      int taskid,
      String taskName,
      String taskDescription,
      String taskStatus,
      LocalDateTime taskCreatedAt,
      LocalDateTime taskLastUpdatedAt,
      LocalDateTime taskDeadline,
      users assginee) {
    this.taskid = taskid;
    this.taskName = taskName;
    this.taskDescription = taskDescription;
    this.taskStatus = taskStatus;
    this.taskCreatedAt = taskCreatedAt == null ? LocalDateTime.now() : taskCreatedAt;
    this.taskLastUpdatedAt =
        taskLastUpdatedAt == null ? LocalDateTime.now() : taskLastUpdatedAt;
    this.taskDeadline = taskDeadline;
    this.assginee = assginee;
    this.commentsList = new LinkedHashMap<>();
  }

  public tasks(
      int taskid,
      String taskName,
      String taskDescription,
      String taskStatus,
      LocalDateTime taskCreatedAt,
      LocalDateTime taskLastUpdatedAt,
      LocalDateTime taskDeadline,
      users assginee,
      Connection conn) {
    this(
        taskid,
        taskName,
        taskDescription,
        taskStatus,
        taskCreatedAt,
        taskLastUpdatedAt,
        taskDeadline,
        assginee);
    this.commentsList = takeComment(conn);
  }

  public int getTaskid() {
    return this.taskid;
  }

  public String getTaskName() {
    return this.taskName;
  }

  public String getTaskDescription() {
    return this.taskDescription;
  }

  public String getTaskStatus() {
    return this.taskStatus;
  }

  public LocalDateTime getTaskCreatedAt() {
    return this.taskCreatedAt;
  }

  public LocalDateTime getTaskLastUpdatedAt() {
    return this.taskLastUpdatedAt;
  }

  public LocalDateTime getTaskDeadline() {
    return this.taskDeadline;
  }

  public users getAssginee() {
    return this.assginee;
  }

  public HashMap<users, String> getCommentsList() {
    if (this.commentsList == null) {
      this.commentsList = new LinkedHashMap<>();
    }
    return this.commentsList;
  }

  /** Nạp comments vào map. Không đóng connection truyền vào. */
  public HashMap<users, String> takeComment(Connection conn) {
    HashMap<users, String> ans = new LinkedHashMap<>();
    if (conn == null) {
      return ans;
    }
    String commentSql =
        "SELECT c.commentid, c.commentdetail, "
            + "u.id AS uid, u.lname, u.fname, u.username, u.password, u.gmail, u.phone, u.dob "
            + "FROM comments c "
            + "JOIN users u ON c.fromwhom = u.id "
            + "WHERE c.taskid = ? "
            + "ORDER BY c.commentid";
    try (PreparedStatement stmt = conn.prepareStatement(commentSql)) {
      stmt.setInt(1, this.taskid);
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          users author =
              new users(
                  rs.getInt("uid"),
                  rs.getString("lname"),
                  rs.getString("fname"),
                  rs.getString("username"),
                  rs.getString("password"),
                  rs.getString("gmail"),
                  rs.getString("phone"),
                  Dates.formatUi(rs.getDate("dob")));
          ans.put(author, rs.getString("commentdetail"));
        }
      }
      return ans;
    } catch (Exception e) {
      System.out.println("Không thể fetch comments: " + e.getMessage());
      return ans;
    }
  }

  public String viewTaskStatus() {
    return this.taskStatus;
  }

  public void updatetask(Connection conn, tasks task) {
    if (conn == null || task == null) {
      return;
    }
    String sql =
        "UPDATE tasks SET taskname = ?, taskdescription = ?, taskdeadline = ?, "
            + "taskstatus = ?, tasklastupdatedat = ? WHERE taskid = ?";
    try (PreparedStatement upt = conn.prepareStatement(sql)) {
      upt.setString(1, task.taskName);
      upt.setString(2, task.taskDescription);
      upt.setTimestamp(3, Timestamp.valueOf(task.taskDeadline));
      upt.setString(4, task.taskStatus);
      upt.setTimestamp(5, Timestamp.valueOf(task.taskLastUpdatedAt));
      upt.setInt(6, this.taskid);
      upt.executeUpdate();
      this.taskName = task.taskName;
      this.taskDescription = task.taskDescription;
      this.taskDeadline = task.taskDeadline;
      this.taskStatus = task.taskStatus;
      this.taskLastUpdatedAt = task.taskLastUpdatedAt;
      if (task.assginee != null) {
        this.assginee = task.assginee;
      }
    } catch (Exception e) {
      System.out.println("Lỗi update task: " + e.getMessage());
    }
  }

  /** Cập nhật nhanh trạng thái task. */
  public boolean updateStatus(Connection conn, String status) {
    if (conn == null || status == null || status.isBlank()) {
      return false;
    }
    String sql = "UPDATE tasks SET taskstatus = ?, tasklastupdatedat = ? WHERE taskid = ?";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      LocalDateTime now = LocalDateTime.now();
      stmt.setString(1, status);
      stmt.setTimestamp(2, Timestamp.valueOf(now));
      stmt.setInt(3, this.taskid);
      if (stmt.executeUpdate() == 0) {
        return false;
      }
      this.taskStatus = status;
      this.taskLastUpdatedAt = now;
      return true;
    } catch (Exception e) {
      System.out.println("Lỗi cập nhật trạng thái task: " + e.getMessage());
      return false;
    }
  }
}
