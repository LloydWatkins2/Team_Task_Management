package com.java.teamtask.elements;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

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
    this.email = email;
    this.password = password;
    this.phone = phone;
    this.dob = dob;
  }

  public users(String lname, String fname, String username, String email, String password) {
    this.lname = lname;
    this.fname = fname;

    this.email = email;
    this.password = password;
    this.relatedProjects = new HashMap<>();
  }

  public long getUserId() {
    return this.userId;
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
    this.relatedProjects = relatedProjects;
  }

  public String getEmail() {
    return this.email;
  }

  public Map<projects, projectsAssignments> getRelatedProjects() {
    return this.relatedProjects;
  }

  public void deleteAccount(Connection conn, users User) {
    String sql = "DELETE FROM users WHERE id = ?";
    try (conn;
        PreparedStatement del = conn.prepareStatement(sql)) {
      del.setInt(1, User.userId);
      del.executeUpdate();
    } catch (Exception e) {
      System.out.println("Lỗi xóa" + e);
    }
  }

  public void updateAccount(Connection conn, users User) {
    String sql = "UPDATE users SET lname = ?, fname = ?, email = ?, password = ? WHERE id = ?";
    try (conn;
        PreparedStatement upt = conn.prepareStatement(sql)) {
      upt.setString(1, User.lname);
      upt.setString(2, User.fname);
      upt.setString(3, User.email);
      upt.setString(4, User.password);
      upt.setInt(5, this.userId);
      upt.executeUpdate();
      this.lname = User.lname;
      this.fname = User.fname;
      this.email = User.email;
      this.password = User.password;
    } catch (Exception e) {
      System.out.println("Lỗi cập nhật" + e);
    }
  }

  public projects createProject(
      Connection conn,
      users User,
      String n,
      String des,
      LocalDateTime dl,
      LocalDateTime ca,
      LocalDateTime up,
      ArrayList<tasks> list) {
    String insertProjectSql =
        "INSERT INTO Projects(Name, Description, Deadline, Create_at, Update_at) VALUES(?, ?, ?,?,"
            + " ?) RETURNING Projectid";
    String insertAssignSql =
        "INSERT INTO ProjectsAssgiments(user_id, Project_id, role) VALUES(?, ?, ?)";
    try {
      int projectId = 0;
      try (PreparedStatement stmt = conn.prepareStatement(insertProjectSql)) {
        stmt.setString(1, n);
        stmt.setString(2, des);
        stmt.setTimestamp(3, Timestamp.valueOf(dl));
        stmt.setTimestamp(4, Timestamp.valueOf(ca));
        stmt.setTimestamp(5, Timestamp.valueOf(up));

        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
          projectId = rs.getInt(1);
        }
      }

      if (projectId > 0) {
        try (PreparedStatement assignStmt = conn.prepareStatement(insertAssignSql)) {
          assignStmt.setInt(1, this.userId);
          assignStmt.setInt(2, projectId);
          assignStmt.setString(3, "owner");
          assignStmt.executeUpdate();
        }

        projects project = new projects(n, des, dl, ca, up, list);
        project.id = projectId;
        projectsAssignments assignment = new projectsAssignments(User, project, ca, "owner");

        if (this.relatedProjects == null) {
          this.relatedProjects = new HashMap<>();
        }
        this.relatedProjects.put(project, assignment);
        return project;
      }
    } catch (SQLException e) {
      System.out.println(e.getMessage());
    }
    return null;
  }

  public boolean deleteProject(Connection conn, projects project) {
    projectsAssignments assignment = this.relatedProjects.get(project);
    if (assignment == null) {
      System.out.println("Bạn không tham gia dự án này.");
      return false;
    }

    if (!"owner".equalsIgnoreCase(assignment.getRole())) {
      System.out.println("Chỉ Owner mới có quyền xóa dự án.");
      return false;
    }

    String deleteAssgimentsSql = "DELETE FROM ProjectsAssgiments WHERE Project_id = ?";
    String deleteTasksSql = "DELETE FROM Tasks WHERE Project_id = ?";
    String deleteProjectSql = "DELETE FROM Projects WHERE Projectid = ?";

    try {
      try (PreparedStatement stmt1 = conn.prepareStatement(deleteAssgimentsSql)) {
        stmt1.setInt(1, project.id);
        stmt1.executeUpdate();
      }

      try (PreparedStatement stmt2 = conn.prepareStatement(deleteTasksSql)) {
        stmt2.setInt(1, project.id);
        stmt2.executeUpdate();
      }

      try (PreparedStatement stmt3 = conn.prepareStatement(deleteProjectSql)) {
        stmt3.setInt(1, project.id);
        int rows = stmt3.executeUpdate();
        if (rows > 0) {
          this.relatedProjects.remove(project);
          System.out.println("Đã xóa dự án " + project.getName() + " thành công.");
          return true;
        }
      }
    } catch (SQLException e) {
      System.out.println(e.getMessage());
    }
    return false;
  }

  public ArrayList<projects> viewProjects() { // chỉ xem thông tin cơ bản
    ArrayList<projects> Projects = new ArrayList<>(this.relatedProjects.keySet());
    return Projects;
  }

  public Double viewProjectStatus(projects project) {
    int goal = project.tasksList.size();
    int cnt = 0;
    for (tasks t : project.tasksList) {
      if (t.taskStatus == "Hoàn thành") cnt += 1;
    }
    return (double) (cnt / goal);
  }
}
