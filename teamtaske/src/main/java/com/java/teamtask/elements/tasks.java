package com.java.teamtask.elements;

import com.java.teamtask.connections.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
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
      LocalDateTime taskCreatedAt,
      LocalDateTime taskLastUpdatedAt,
      users assginee,
      Connection conn) {
    this.taskid = taskid;
    this.taskName = taskName;
    this.taskDescription = taskDescription;
    this.taskStatus = taskStatus;
    this.taskCreatedAt = taskCreatedAt == null ? LocalDateTime.now() : taskCreatedAt;
    this.taskLastUpdatedAt = taskLastUpdatedAt == null ? LocalDateTime.now() : taskLastUpdatedAt;
    this.assginee = assginee;
    this.commentsList = takeComment(conn);
  }

  public HashMap<users, String> takeComment(Connection conn) {
    String commentSql =
        "SELECT c.commentid, c.commentdetail, "
            + "u.id AS uid, u.lname, u.fname, u.username, u.password, u.gmail, u.phone, u.dob "
            + "FROM Comments c "
            + "JOIN users u ON c.fromwhom = u.id "
            + "WHERE c.taskid = ?";
    HashMap<users, String> ans = new HashMap<>();
    try (conn;
        PreparedStatement tke = conn.prepareStatement(commentSql)) {
      tke.setInt(1, this.taskid);
      ResultSet rs = tke.executeQuery();
      while (rs.next()) {
        ans.put(
            new users(
                rs.getInt("uid"),
                rs.getString("u.lname"),
                rs.getString("u.fname"),
                rs.getString("u.username"),
                rs.getString("u.password"),
                rs.getString("u.gmail"),
                rs.getString("u.phone"),
                rs.getString("u.dob")),
            rs.getString("c.commentdetail"));
      }
      if (ans.size() == 0) {
        System.out.println("khong co comment");
        return null;
      }
      return ans;
    } catch (Exception e) {
      System.out.println("Không thể fetch comments " + e);
      return null;
    }
  }

  public String viewTaskStatus() {
    return this.taskStatus;
  }

  public void updatetask(Connection conn, tasks tk) {
    String sql =
        "UPDATE Tasks SET taskName = ?, taskDescription = ?, taskDeadline = ?, taskStatus ="
            + " ?,taskLastUpdatedAt=? WHERE id = ?";
    try (conn;
        PreparedStatement upt = conn.prepareStatement(sql)) {
      upt.setString(1, tk.taskName);
      upt.setString(2, tk.taskDescription);
      upt.setTimestamp(3, Timestamp.valueOf(tk.taskDL));
      upt.setString(4, tk.taskStatus);
      upt.setTimestamp(5, Timestamp.valueOf(tk.taskLastUpdatedAt));
      upt.setLong(6, this.taskid);
      this.taskName = tk.taskName;
      this.taskDL = tk.taskDL;
      this.taskDescription = tk.taskDescription;
      this.taskStatus = tk.taskStatus;
      this.taskLastUpdatedAt = tk.taskLastUpdatedAt;
    } catch (Exception e) {
      System.out.println("Lỗi update, thử lại sau " + e);
    }
  }

  public void deleteTask(int taskid) {}
}
