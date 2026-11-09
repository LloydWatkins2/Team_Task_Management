package com.java.teamtask.elements;

import com.java.teamtask.connections.SQL;
import java.util.*;
import java.sql.*;

public class owners implements projectsManage {
  private users user;
  private projects project;

  public owners(users Users, projects Projects) {
    this.user = Users;
    this.project = Projects;
  }

  @Override
  public void deleteMember(members Member, Conncection conn) {
      int memberId = Member.getUser().userId;
      if(memberId == this.user.userId){
          System.out.println("Bạn là Owner, bạn không thể tự xoá chính mình khỏi dự án");
          return;
      }
      String checkSql = "SELECT 1 FROM ProjectsAssgiments WHERE user_id = ? AND Project_id = ? AND role <> 'owner'";
      String reassignSql = "UPDATE Tasks SET user_id = ?, taskLastUpdatedAt = ? WHERE user_id = ? AND Project_id = ?";
      String deleteSql = "DELETE FROM ProjectsAssgiments WHERE user_id = ? AND Project_id = ? AND role <> 'owner'";
      try{
        try (PreparedStatement check = conn.prepareStatement(checkSql)){
            check.setInt(1, memberId);
            check.setInt(2, this.project.id);
            try (ResultSet rs = check.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("Thành viên không tồn tại trong dự án");
                    return;
                }
            }
            try (PreparedStatement del = conn.prepareStatement(deleteSql)) {
                del.setInt(1, memberId);
                del.setInt(2, this.project.id);
                del.executeUpdate();
            }   
            for (tasks t : this.project.tasksList) {
                if (t.assginee != null && t.assginee.userId == memberId) {
                    t.assginee = this.user;
                }
            }
            System.out.println("Đã xoá thành viên khỏi dự án.");
        }
        catch(SQLException e){
            System.out.println(e.getMessage());
        }
      }
  }

  @Override
  public void addMember(users us, Connection conn) {
      if(us == null){
          System.out.println("người dùng không hợp lệ");
          return;
      }
      String checkSql = "SELECT 1 FROM ProjectsAssgiments WHERE user_id = ? AND Project_id = ?";
      String insertSql = "INSERT INTO ProjectsAssgiments(user_id, Project_id, role, assignat) VALUES(?, ?, 'member', ?)"; 
      try{
          try(PreparedStatement check = conn.prepareStatement(checkSql)){
              check.setInt(1, us.userId);
              check.setInt(2, this.project.id);
              try(ResultSet rs = check.executeQuery()){
                  if(rs.next()){
                      System.out.println("Người dùng đã là thành viên của dự án.");
                      return;
                  }
              }
          }
          try(PreparedStatement ins = conn.prepareStatement(insertSql)){
              ins.setInt(1, us.userId);
              ins.setInt(2, this.project.id);
              ins.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
              ins.executeUpdate();
              System.out.println("Đã thêm thành viên.");
          }
      }
      catch(SQLException e){
          System.out.println(e.getMessage());
      }
  }

  @Override
  public void assignTask(users Member, ArrayList<tasks> Tasks, Connection conn) {
    for (tasks e : Tasks) {
      e.assginee = Member;
      this.project.tasksList.add(e);
    }
  }

  @Override
  public void updateTask(
      tasks Tasks, String taskName, String taskStatus, String taskDescription, String taskDL) {

    ArrayList<String> changingData =
        Tasks.updatetask(taskName, taskStatus, taskDescription, taskDL);
    SQL.taskUpdate(changingData);
  }

  @Override
  public void updateProject() {}

  @Override
  public void deleteProject() {}

  @Override
  public String viewProjectInfo() {}

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
  public String viewMemberInfo(Connection conn, members k) {
      if(k==null) return "Không tìm thấy thành viên";
      return k.viewMemberInfo(conn, k);
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
  public Double viewMemberProgress(Connection conn ,members k) {
      if(k==null){
          return 0.0;
      }
      return k.viewMemberProgress(conn, k);
  }

  @Override
  public String viewTask() {} // xem thông tin chi tiết task

  @Override
  public String commentTask(tasks Tasks, String comment) {}
}
