package com.java.teamtask.elements;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Các thao tác hệ thống: đăng ký, đăng nhập và phân quyền. */
public class system {
  private system() {}

  public static boolean register(
      String lname,
      String fname,
      String username,
      String password,
      String gmail,
      String phone,
      String dob,
      Connection conn)
      throws SQLException {
    if (conn == null) {
      return false;
    }
    String checkSql = "SELECT 1 FROM users WHERE username = ? OR gmail = ? LIMIT 1";
    String insertSql =
        "INSERT INTO users(lname, fname, username, password, gmail, phone, dob) "
            + "VALUES(?, ?, ?, ?, ?, ?, ?)";
    try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
      checkStmt.setString(1, username);
      checkStmt.setString(2, gmail);
      try (ResultSet rs = checkStmt.executeQuery()) {
        if (rs.next()) {
          System.out.println("Username hoặc Gmail đã tồn tại");
          return false;
        }
      }
    }
    try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
      insertStmt.setString(1, lname);
      insertStmt.setString(2, fname);
      insertStmt.setString(3, username);
      insertStmt.setString(4, password);
      insertStmt.setString(5, gmail);
      insertStmt.setString(6, phone);
      java.sql.Date sqlDob = Dates.toSqlDate(dob);
      if (sqlDob == null) {
        throw new IllegalArgumentException("Ngày sinh không hợp lệ (dd/MM/yyyy).");
      }
      insertStmt.setDate(7, sqlDob);
      return insertStmt.executeUpdate() > 0;
    }
  }

  public static boolean authentication(String username, String password, Connection conn) {
    if (conn == null || username == null || password == null) {
      return false;
    }
    String loginSql =
        "SELECT 1 FROM users "
            + "WHERE (username = ? AND password = ?) OR (gmail = ? AND password = ?) "
            + "LIMIT 1";
    try (PreparedStatement stmt = conn.prepareStatement(loginSql)) {
      stmt.setString(1, username);
      stmt.setString(2, password);
      stmt.setString(3, username);
      stmt.setString(4, password);
      try (ResultSet rs = stmt.executeQuery()) {
        return rs.next();
      }
    } catch (SQLException e) {
      System.out.println("Lỗi SQL khi đăng nhập: " + e.getMessage());
      return false;
    }
  }

  /**
   * Trả về đối tượng quyền tương ứng với role trong project.
   *
   * <p>Owner dùng được toàn bộ hành động quản lý, member chỉ xem và bình luận.
   */
  public static seeProjects roleChecker(
      projectsAssignments projas, users user, projects project) {
    if (user == null || project == null) {
      throw new IllegalArgumentException("user và project không được null");
    }
    String role = projas == null ? null : projas.getRole();
    if ("owner".equalsIgnoreCase(role)) {
      return new owners(user, project, projas);
    }
    return new members(user, project);
  }
}
