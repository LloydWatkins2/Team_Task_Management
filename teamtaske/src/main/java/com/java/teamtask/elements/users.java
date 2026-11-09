package com.java.teamtask.elements;

import com.java.teamtask.connections.SQL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class users {
  public int userId;

  protected String lname;
  protected String fname;
  protected String username;
  protected String password;
  protected String email;
  protected String phone;
  protected String dob;
  protected HashMap<projects, projectsAssignments> relatedProjects;

  public users(
      int userId,
      String lname,
      String fname,
      String username,
      String password,
      String email,
      String phone,
      String dob) {
    this.userId = userId;
    this.lname = lname;
    this.fname = fname;
    this.username = username;
    this.password = password;
    this.email = email;
    this.phone = phone;
    this.dob = dob;
    this.relatedProjects = new LinkedHashMap<>();
  }

  public users(String lname, String fname, String username, String email, String password) {
    this.lname = lname;
    this.fname = fname;
    this.username = username;
    this.email = email;
    this.password = password;
    this.relatedProjects = new LinkedHashMap<>();
  }

  public users(
      int userId,
      String lname,
      String fname,
      String password,
      String email,
      String phone,
      String dob,
      HashMap<projects, projectsAssignments> relatedProjects) {
    this.userId = userId;
    this.lname = lname;
    this.fname = fname;
    this.email = email;
    this.password = password;
    this.phone = phone;
    this.dob = dob;
    this.relatedProjects =
        relatedProjects == null ? new LinkedHashMap<>() : relatedProjects;
  }

  public long getUserId() {
    return this.userId;
  }

  public String getLname() {
    return this.lname;
  }

  public String getFname() {
    return this.fname;
  }

  public String getUsername() {
    return this.username;
  }

  public String getPassword() {
    return this.password;
  }

  public String getEmail() {
    return this.email;
  }

  public String getGmail() {
    return this.email;
  }

  public String getPhone() {
    return this.phone;
  }

  public String getDob() {
    return this.dob;
  }

  public String getFullName() {
    String first = this.lname == null ? "" : this.lname.trim();
    String last = this.fname == null ? "" : this.fname.trim();
    String full = (first + " " + last).trim();
    return full.isEmpty() ? (this.username == null ? "Người dùng" : this.username) : full;
  }

  public Map<projects, projectsAssignments> getRelatedProjects() {
    ensureProjects();
    return this.relatedProjects;
  }

  public void setRelatedProjects(HashMap<projects, projectsAssignments> relatedProjects) {
    this.relatedProjects =
        relatedProjects == null ? new LinkedHashMap<>() : relatedProjects;
  }

  private void ensureProjects() {
    if (this.relatedProjects == null) {
      this.relatedProjects = new LinkedHashMap<>();
    }
  }

  public void deleteAccount(Connection conn, users user) {
    if (conn == null || user == null) {
      return;
    }
    String sql = "DELETE FROM users WHERE id = ?";
    try (PreparedStatement del = conn.prepareStatement(sql)) {
      del.setInt(1, user.userId);
      del.executeUpdate();
    } catch (SQLException e) {
      System.out.println("Lỗi xóa tài khoản: " + e.getMessage());
    }
  }

  /**
   * Cập nhật các trường được truyền vào (khác null và không rỗng). Trả về true nếu có ít nhất một
   * trường được cập nhật.
   */
  public boolean updateProfile(
      Connection conn,
      String newLname,
      String newFname,
      String newGmail,
      String newPhone,
      String newDob,
      String newPassword) {
    if (conn == null) {
      return false;
    }
    ArrayList<String> sets = new ArrayList<>();
    ArrayList<Object> values = new ArrayList<>();
    addUpdate(sets, values, "lname", newLname);
    addUpdate(sets, values, "fname", newFname);
    addUpdate(sets, values, "gmail", newGmail);
    addUpdate(sets, values, "phone", newPhone);
    java.sql.Date sqlDob = Dates.toSqlDate(newDob);
    if (sqlDob != null) {
      sets.add("dob = ?");
      values.add(sqlDob);
    }
    addUpdate(sets, values, "password", newPassword);
    if (sets.isEmpty()) {
      return false;
    }

    String sql = "UPDATE users SET " + String.join(", ", sets) + " WHERE id = ?";
    try (PreparedStatement upt = conn.prepareStatement(sql)) {
      int index = 1;
      for (Object value : values) {
        upt.setObject(index++, value);
      }
      upt.setInt(index, this.userId);
      if (upt.executeUpdate() == 0) {
        return false;
      }
      if (hasText(newLname)) this.lname = newLname.trim();
      if (hasText(newFname)) this.fname = newFname.trim();
      if (hasText(newGmail)) this.email = newGmail.trim();
      if (hasText(newPhone)) this.phone = newPhone.trim();
      if (sqlDob != null) this.dob = Dates.formatUi(sqlDob);
      if (hasText(newPassword)) this.password = newPassword;
      return true;
    } catch (SQLException e) {
      System.out.println("Lỗi cập nhật tài khoản: " + e.getMessage());
      return false;
    }
  }

  public void updateAccount(Connection conn, users user) {
    if (user == null) {
      return;
    }
    updateProfile(
        conn, user.lname, user.fname, user.email, user.phone, user.dob, user.password);
  }

  private static void addUpdate(
      ArrayList<String> sets, ArrayList<Object> values, String column, String value) {
    if (hasText(value)) {
      sets.add(column + " = ?");
      values.add(value.trim());
    }
  }

  private static boolean hasText(String value) {
    return value != null && !value.isBlank();
  }

  /**
   * Tạo project và gán người tạo làm owner. Trả về project vừa tạo hoặc null nếu thất bại.
   */
  public projects createProject(
      Connection conn,
      users user,
      String name,
      String description,
      LocalDateTime deadline,
      LocalDateTime createdAt,
      LocalDateTime updatedAt,
      ArrayList<tasks> taskList) {
    if (conn == null || !hasText(name)) {
      return null;
    }
    ensureProjects();
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime created = createdAt == null ? now : createdAt;
    LocalDateTime updated = updatedAt == null ? now : updatedAt;
    LocalDateTime due = deadline == null ? now.plusDays(7) : deadline;
    users creator = user == null ? this : user;

    String insertProjectSql =
        "INSERT INTO projects(name, description, deadline, create_at, update_at) "
            + "VALUES(?, ?, ?, ?, ?) RETURNING projectid";
    String insertAssignSql =
        "INSERT INTO projectsassgiments(user_id, project_id, role, assignat) "
            + "VALUES(?, ?, 'owner', ?)";

    boolean oldAutoCommit = true;
    try {
      oldAutoCommit = conn.getAutoCommit();
      conn.setAutoCommit(false);
      int projectId;
      try (PreparedStatement stmt = conn.prepareStatement(insertProjectSql)) {
        stmt.setString(1, name.trim());
        stmt.setString(2, description == null ? "" : description.trim());
        stmt.setTimestamp(3, Timestamp.valueOf(due));
        stmt.setTimestamp(4, Timestamp.valueOf(created));
        stmt.setTimestamp(5, Timestamp.valueOf(updated));
        try (ResultSet rs = stmt.executeQuery()) {
          if (!rs.next()) {
            conn.rollback();
            return null;
          }
          projectId = rs.getInt(1);
        }
      }

      try (PreparedStatement assignStmt = conn.prepareStatement(insertAssignSql)) {
        assignStmt.setInt(1, this.userId);
        assignStmt.setInt(2, projectId);
        assignStmt.setTimestamp(3, Timestamp.valueOf(created));
        assignStmt.executeUpdate();
      }

      projects project =
          new projects(
              projectId,
              name.trim(),
              description == null ? "" : description.trim(),
              due,
              created,
              updated,
              taskList == null ? new ArrayList<>() : taskList);
      projectsAssignments assignment =
          new projectsAssignments(creator, project, created, "owner");
      this.relatedProjects.put(project, assignment);
      conn.commit();
      return project;
    } catch (SQLException e) {
      try {
        conn.rollback();
      } catch (SQLException ignored) {
        // bỏ qua
      }
      System.out.println("Lỗi tạo project: " + e.getMessage());
      return null;
    } finally {
      try {
        conn.setAutoCommit(oldAutoCommit);
      } catch (SQLException ignored) {
        // bỏ qua
      }
    }
  }

  /** Xóa project cùng toàn bộ dữ liệu con. Chỉ owner mới được xóa. */
  public boolean deleteProject(Connection conn, projects project) {
    if (conn == null || project == null) {
      return false;
    }
    ensureProjects();
    projectsAssignments assignment = this.relatedProjects.get(project);
    String role = assignment == null ? null : assignment.getRole();
    if (role == null) {
      try {
        role = SQL.findRole(conn, this.userId, project.getId());
      } catch (SQLException ignored) {
        role = null;
      }
    }
    if (!"owner".equalsIgnoreCase(role)) {
      System.out.println("Chỉ Owner mới có quyền xóa dự án.");
      return false;
    }

    String deleteCommentsSql =
        "DELETE FROM comments WHERE taskid IN (SELECT taskid FROM tasks WHERE project_id = ?)";
    String deleteTasksSql = "DELETE FROM tasks WHERE project_id = ?";
    String deleteAssignmentsSql = "DELETE FROM projectsassgiments WHERE project_id = ?";
    String deleteProjectSql = "DELETE FROM projects WHERE projectid = ?";

    boolean oldAutoCommit = true;
    try {
      oldAutoCommit = conn.getAutoCommit();
      conn.setAutoCommit(false);
      try (PreparedStatement stmt = conn.prepareStatement(deleteCommentsSql)) {
        stmt.setInt(1, project.getId());
        stmt.executeUpdate();
      }
      try (PreparedStatement stmt = conn.prepareStatement(deleteTasksSql)) {
        stmt.setInt(1, project.getId());
        stmt.executeUpdate();
      }
      try (PreparedStatement stmt = conn.prepareStatement(deleteAssignmentsSql)) {
        stmt.setInt(1, project.getId());
        stmt.executeUpdate();
      }
      int rows;
      try (PreparedStatement stmt = conn.prepareStatement(deleteProjectSql)) {
        stmt.setInt(1, project.getId());
        rows = stmt.executeUpdate();
      }
      if (rows == 0) {
        conn.rollback();
        return false;
      }
      this.relatedProjects.remove(project);
      conn.commit();
      return true;
    } catch (SQLException e) {
      try {
        conn.rollback();
      } catch (SQLException ignored) {
        // bỏ qua
      }
      System.out.println("Lỗi xóa project: " + e.getMessage());
      return false;
    } finally {
      try {
        conn.setAutoCommit(oldAutoCommit);
      } catch (SQLException ignored) {
        // bỏ qua
      }
    }
  }

  /** Danh sách project cơ bản, sắp xếp theo deadline rồi tên. */
  public ArrayList<projects> viewProjects() {
    ensureProjects();
    ArrayList<projects> list = new ArrayList<>(this.relatedProjects.keySet());
    list.sort(
        Comparator.comparing(
                projects::getDL,
                Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(projects::getName, Comparator.nullsLast(String::compareToIgnoreCase)));
    return list;
  }

  /** Lấy assignment tương ứng với project, null nếu không tham gia. */
  public projectsAssignments getAssignment(projects project) {
    ensureProjects();
    return this.relatedProjects.get(project);
  }

  public void putAssignment(projects project, projectsAssignments assignment) {
    ensureProjects();
    this.relatedProjects.put(project, assignment);
  }

  public void removeAssignment(projects project) {
    ensureProjects();
    this.relatedProjects.remove(project);
  }
}
