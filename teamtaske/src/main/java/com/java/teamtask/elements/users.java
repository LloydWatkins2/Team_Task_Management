package com.java.teamtask.elements;

import java.sql.*;
import java.util.*;

public class users {
  public int userId;

  protected String lname;
  protected String fname;
  protected String password;
  protected String email;

  protected String phone;
  protected String dob;
  protected HashMap<projects, projectsAssignments> relatedProjects;

  public users(
      int userId,
      String lname,
      String fname,
      String password,
      String email,
      String phone,
      String dob) {
    this.userId = userId;
    this.lname = lname;
    this.fname = fname;
    this.email = email;
    this.password = password;
    this.phone = phone;
    this.dob = dob;
  }

  public users(String lname, String fname, String email, String password) {
    this.lname = lname;
    this.fname = fname;

    this.email = email;
    this.password = password;
    this.relatedProjects = new HashMap<>();
  }

  public int getUserId() {
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

  public boolean deleteAccount(Connection conn) {}

  public void updateAccount() {}

  public projects createProject(
      Connection conn,
      String n,
      String des,
      String dl,
      String ca,
      String up,
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
        projectsAssignments assignment = new projectsAssignments(this, project, ca, "owner");

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

  public ArrayList<projects> viewProjects() {
    return new ArrayList<>(this.relatedProjects.keySet());
  }

  public String viewProjectStatus(projects project) {}

  public void action(projects project) {
    projectsAssignments assignment = this.relatedProjects.get(project);
    if (assignment == null) {
      System.out.println("Bạn không thuộc dự án này.");
      return;
    }
    if ("owner".equalsIgnoreCase(assignment.getRole())) {
      owners ownerAction = new owners(this, project);
      ownerAction.viewProjectInfo();
      ownerAction.viewMembers();
    } else {
      members memberAction = new members(this, project);
      memberAction.viewProjectInfo();
    }
  }
}
