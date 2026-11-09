package com.java.teamtask.elements;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/** Hành động của member trong một project: chỉ xem và bình luận. */
public class members implements seeProjects {
  private final users user;
  private final projects project;

  public members(users user, projects project) {
    this.user = user;
    this.project = project;
  }

  public users getUser() {
    return this.user;
  }

  public projects getProject() {
    return this.project;
  }

  @Override
  public projects viewProjectInfo() {
    return this.project;
  }

  @Override
  public ArrayList<projectsAssignments> viewMembers(Connection conn) throws SQLException {
    String sql =
        "SELECT u.id, u.lname, u.fname, u.username, u.password, u.gmail, u.phone, u.dob, "
            + "pa.role, pa.assignat "
            + "FROM projectsassgiments pa "
            + "JOIN users u ON u.id = pa.user_id "
            + "WHERE pa.project_id = ? "
            + "ORDER BY CASE WHEN pa.role = 'owner' THEN 0 ELSE 1 END, u.lname, u.fname";
    ArrayList<projectsAssignments> list = new ArrayList<>();
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, this.project.getId());
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          users member =
              new users(
                  rs.getInt("id"),
                  rs.getString("lname"),
                  rs.getString("fname"),
                  rs.getString("username"),
                  rs.getString("password"),
                  rs.getString("gmail"),
                  rs.getString("phone"),
                  Dates.formatUi(rs.getDate("dob")));
          list.add(
              new projectsAssignments(
                  member,
                  this.project,
                  rs.getObject("assignat", LocalDateTime.class),
                  rs.getString("role")));
        }
      }
    }
    return list;
  }

  @Override
  public Double viewOverallProgress() {
    ArrayList<tasks> list = this.project.getTasksList();
    if (list == null || list.isEmpty()) {
      return 0.0;
    }
    int done = 0;
    for (tasks task : list) {
      if (isDone(task.getTaskStatus())) {
        done++;
      }
    }
    return (double) done / list.size();
  }

  @Override
  public Double viewMemberProgress(Connection conn, members member) {
    if (member == null
        || member.user == null
        || this.project == null
        || this.project.getTasksList().isEmpty()) {
      return 0.0;
    }
    int goal = 0;
    int done = 0;
    for (tasks task : this.project.getTasksList()) {
      if (task.getAssginee() != null && task.getAssginee().userId == member.user.userId) {
        goal++;
        if (isDone(task.getTaskStatus())) {
          done++;
        }
      }
    }
    if (goal == 0) {
      return 0.0;
    }
    return (double) done / goal;
  }

  @Override
  public ArrayList<tasks> viewTasks() {
    return this.project.getTasksList();
  }

  public tasks viewTasksInfo(tasks task) {
    return task;
  }

  @Override
  public projectsAssignments viewMemberInfo(Connection conn, users target) throws SQLException {
    if (target == null || conn == null) {
      return null;
    }
    String sql =
        "SELECT u.id, u.lname, u.fname, u.username, u.password, u.gmail, u.phone, u.dob, "
            + "pa.role, pa.assignat "
            + "FROM users u "
            + "JOIN projectsassgiments pa ON u.id = pa.user_id "
            + "WHERE u.id = ? AND pa.project_id = ?";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, target.userId);
      stmt.setInt(2, this.project.getId());
      try (ResultSet rs = stmt.executeQuery()) {
        if (!rs.next()) {
          return null;
        }
        users member =
            new users(
                rs.getInt("id"),
                rs.getString("lname"),
                rs.getString("fname"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("gmail"),
                rs.getString("phone"),
                Dates.formatUi(rs.getDate("dob")));
        return new projectsAssignments(
            member,
            this.project,
            rs.getObject("assignat", LocalDateTime.class),
            rs.getString("role"));
      }
    }
  }

  @Override
  public void commentTask(Connection conn, tasks task, String comment) {
    if (task == null || comment == null || comment.isBlank() || conn == null) {
      return;
    }
    String sql =
        "INSERT INTO comments(taskid, fromwhom, commentdetail) VALUES(?, ?, ?)";
    try (PreparedStatement ins = conn.prepareStatement(sql)) {
      ins.setInt(1, task.getTaskid());
      ins.setInt(2, this.user.userId);
      ins.setString(3, comment.trim());
      ins.executeUpdate();
      if (task.commentsList == null) {
        task.commentsList = new LinkedHashMap<>();
      }
      task.commentsList.put(this.user, comment.trim());
      System.out.println("Đã gửi bình luận");
    } catch (SQLException e) {
      System.out.println("Không thể bình luận: " + e.getMessage());
    }
  }

  /**
   * Thành viên chỉ được cập nhật trạng thái cho task được giao cho chính mình. Trả về true nếu cập
   * nhật thành công.
   */
  public boolean updateAssignedTaskStatus(Connection conn, tasks task, String status) {
    if (conn == null || task == null || status == null || status.isBlank()) {
      return false;
    }
    if (task.getAssginee() == null || task.getAssginee().userId != this.user.userId) {
      System.out.println("Bạn chỉ có thể cập nhật trạng thái công việc được giao cho mình.");
      return false;
    }
    return task.updateStatus(conn, status);
  }

  private static boolean isDone(String status) {
    return "DONE".equalsIgnoreCase(status) || "Hoàn thành".equalsIgnoreCase(status);
  }
}
