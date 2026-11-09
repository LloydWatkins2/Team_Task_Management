package com.java.teamtask.elements;

import java.sql.Connection;
import java.sql.*;
import java.util.*;
import java.time.LocalDateTime;

public class members implements seeProjects {
  private users user;
  private projects project;

  public members(users User, projects Project) {
    this.user = User;
    this.project = Project;
  }
  
  public users getUser() {
      return this.user;
  }
  
  @Override
  public String viewProjectInfo(Connection conn) {}

  @Override
  public ArrayList<projectsAssignments> viewMembers(Connection conn) throws SQLException{
      String sql = "SELECT u.id, u.lname, u.fname, u.username, u.gmail, u.phone, u.dob, pa.role, pa.assignat "
            + "FROM ProjectsAssgiments AS pa "
            + "JOIN users AS u ON u.id = pa.user_id "
            + "WHERE pa.Project_id = ?";
      ArrayList<projectsAssignments> list = new ArrayList<>();
      try(PreparedStatement stmt = conn.prepareStatement(sql)){
          stmt.setLong(1, this.project.id);
          try(ResultSet rs = stmt.executeQuery()){
              while(rs.next()){
                  users u = new users(
                        rs.getInt("id"), rs.getString("lname"), rs.getString("fname"),
                        rs.getString("username"), null, rs.getString("gmail"),
                        rs.getString("phone"), rs.getString("dob"));
                  list.add(new projectsAssignments(
                        u, this.project,
                        rs.getObject("assignat", LocalDateTime.class),
                        rs.getString("role")  
                  ));
              }
          }
      }
      return list;
  }

  @Override
  public Double viewOverrallProgress() {
    int goal = this.project.tasksList.size();
    int cnt = 0;
    for (tasks t : this.project.tasksList) {
      if (t.taskStatus == "Hoàn thành") cnt += 1;
    }
    return (double) (cnt / goal);
  }

  @Override
  public Double viewMemberProgress(
    Connection conn,
    members k
  ) {
      if (k == null || k.user == null || this.project == null || this.project.tasksList == null || this.project.tasksList.isEmpty()) {
          return 0.0;
      }
      int goal = 0;
      int cnt = 0;
      for (tasks t : this.project.tasksList) {
          if (t.assginee != null && t.assginee.userId == k.user.userId) {
            goal++;
            if ("Hoàn thành".equalsIgnoreCase(t.taskStatus)) {
                cnt++;
            }
          }
      }
      if (goal == 0) return 0.0;
      return (double) cnt/goal;
  }

  @Override
  public String viewTask(Connection conn) {} // xem thông tin chi tiết của task

  @Override
  public String commentTask(
    Connection conn,
    tasks tk,
    String cmt
  ) {}

  @Override 
  public projectsAssignments viewMemberInfo(Connection conn, users k) throws SQLException {
    if (k == null) return null;
    String sql = "SELECT u.id, u.lname, u.fname, u.username, u.gmail, u.phone, u.dob, "
          + "pa.role, pa.assignat "
          + "FROM users u "
          + "JOIN ProjectsAssgiments pa ON u.id = pa.user_id "
          + "WHERE u.id = ? AND pa.Project_id = ?";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, k.userId);
        stmt.setInt(2, this.project.id);
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                users u = new users(
                rs.getInt("id"), rs.getString("lname"), rs.getString("fname"),
                rs.getString("username"), null, rs.getString("gmail"),
                rs.getString("phone"), rs.getString("dob"));
            return new projectsAssignments(
                u, this.project,
                rs.getObject("assignat", LocalDateTime.class),
                rs.getString("role"));
            }
        }
    }
    return null;
   }
}
