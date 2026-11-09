package com.java.teamtask.elements;

import java.sql.Connection;
import java.sql.*;

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
  public String viewMembers(Connection conn) {
      String sql = "SELECT u.lname, u.fname, u.gmail, pa.role"
              + "FROM projectsAssignments as pa"
              + "JOIN users as u ON u.id= pa.user_id"
              + "WHERE pa.Project_id=?";
      StringBuilder sb = new StringBuilder("Danh sách thành viên: \n");
      int count =0;
      try(PreparedStatement stmt = conn.prepareStatement(sql)){
          stmt.setLong(1, this.project.id);
          try(ResultSet rs = stmt.executeQuery()){
              while(rs.next()){
                  count++;
                  sb.append(count).append(". ")
                    .append(rs.getString("lname")).append(" ").append(rs.getString("fname"))
                    .append("\nemail: ").append(rs.getString("gmail"))
                    .append("\nvai trò: ").append(rs.getString("role")).append("\n");
              }
          }
          sb.insert(0, "Tổng số thành viên: " + count + "\n");
          return sb.toString();
      }
      catch(SQLException e){
          return e.getMessage();
      }
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
  public String viewMemberInfo(
    Connection conn,
    members k
  ) {
      if (k == null || k.user == null){
          return "Không tìm thấy thành viên";
      }
      String sql = "SELECT u.lname, u.fname, u.username, u.gmail, u.phone, u.dob, pa.role "
             + "FROM users u "
             + "JOIN ProjectsAssgiments pa ON u.id = pa.user_id "
             + "WHERE u.id = ? AND pa.Project_id = ?";
      try(PreparedStatement stmt = conn.prepareStatement(sql)){
          stmt.setLong(1, k.user.getUserId());
          stmt.setLong(2, this.project.id);
          try(ResultSet rs = stmt.executeQuery()){
              if(rs.next()){
                  return "Họ và tên: " + rs.getString("lname") + " " + rs.getString("fname") + "\n"
                          + "username: " + rs.getString("username") + "\n"
                          + "gmail: " + rs.getString("gmail") + "\n"
                          + "SDT: " + rs.getString("phone") + "\n"
                          + "dob: " + rs.getString("dob") +"\n"
                          + "Vai trò: " + rs.getString("role");
              }
          }
      }
      catch (SQLException e){
          return e.getMessage();
      }
      return "Không tồn tại";
  }
}
