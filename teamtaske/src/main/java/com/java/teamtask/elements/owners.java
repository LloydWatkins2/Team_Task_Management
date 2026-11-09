package com.java.teamtask.elements;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class owners implements projectsManage {
  private users user;
  private projects project;
  private projectsAssignments pa;

  public owners(users Users, projects Projects, projectsAssignments pa) {
    this.user = Users;
    this.project = Projects;
    this.pa = pa;
  }

  @Override
  public void deleteMember(members Member, Connection conn) {
    int memberId = Member.getUser().userId;
    if (memberId == this.user.userId) {
      System.out.println("Bạn là Owner, bạn không thể tự xoá chính mình khỏi dự án");
      return;
    }
    String checkSql =
        "SELECT 1 FROM ProjectsAssgiments WHERE user_id = ? AND Project_id = ? AND role <> 'owner'";
    String deleteSql =
        "DELETE FROM ProjectsAssgiments WHERE user_id = ? AND Project_id = ? AND role <> 'owner'";
    try {
      try (PreparedStatement check = conn.prepareStatement(checkSql)) {
        check.setInt(1, memberId);
        check.setInt(2, this.project.id);
        try (ResultSet rs = check.executeQuery()) {
          if (!rs.next()) {
            System.out.println("Thành viên không tồn tại trong dự án");
            return;
          }
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
    } catch (SQLException e) {
      System.out.println(e.getMessage());
    }
  }

  @Override
  public void addMember(users us, Connection conn) {
    if (us == null) {
      System.out.println("người dùng không hợp lệ");
      return;
    }
    String checkSql = "SELECT 1 FROM ProjectsAssgiments WHERE user_id = ? AND Project_id = ?";
    String insertSql =
        "INSERT INTO ProjectsAssgiments(user_id, Project_id, role, assignat) VALUES(?, ?, 'member',"
            + " ?)";
    try {
      try (PreparedStatement check = conn.prepareStatement(checkSql)) {
        check.setInt(1, us.userId);
        check.setInt(2, this.project.id);
        try (ResultSet rs = check.executeQuery()) {
          if (rs.next()) {
            System.out.println("Người dùng đã là thành viên của dự án.");
            return;
          }
        }
      }
      try (PreparedStatement ins = conn.prepareStatement(insertSql)) {
        ins.setInt(1, us.userId);
        ins.setInt(2, this.project.id);
        ins.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
        ins.executeUpdate();
        System.out.println("Đã thêm thành viên.");
      }
    } catch (SQLException e) {
      System.out.println(e.getMessage());
    }
  }

  @Override
  public void assignTask(ArrayList<tasks> taske, Connection conn) {
    String sql = "UPDATE Tasks SET user_id = ? where task_id = ?";
    try (conn;
        PreparedStatement upt = conn.prepareStatement(sql)) {
      for (tasks e : taske) {
        e.assginee = this.user;
        this.project.tasksList.add(e);
        upt.setInt(1, this.user.userId);
        upt.setInt(2, e.taskid);
        upt.executeUpdate();
      }
      System.out.println(" Update task thành công");
    } catch (Exception e) {
      System.out.println("loi giao viec " + e);
    }
  }

  @Override
  public void updateTask(
      tasks Tasks,
      String taskName,
      String taskStatus,
      String taskDescription,
      LocalDateTime taskDL,
      Connection conn) {
    tasks newTasks =
        new tasks(
            0,
            taskName,
            taskDescription,
            taskStatus,
            LocalDateTime.now(),
            LocalDateTime.now(),
            taskDL,
            this.user);
    Tasks.updatetask(conn, newTasks);
  }

  @Override
  public String updateProject(Connection conn, String name, String des, LocalDateTime dl) {

    String upProj =
        "UPDATE Projects SET Name = ?, Description = ?, Deadline = ?, Update_at = ? WHERE"
            + " Project_id = ?";
    LocalDateTime now = LocalDateTime.now();

    try (conn;
        PreparedStatement stmt = conn.prepareStatement(upProj)) {
      stmt.setString(1, name);
      stmt.setString(2, des);
      stmt.setTimestamp(3, Timestamp.valueOf(dl));
      stmt.setTimestamp(4, Timestamp.valueOf(now)); // Update_at
      stmt.setInt(5, this.project.id);

      this.project.name = name;
      this.project.description = des;
      this.project.deadline = dl;
      this.project.lastUpdatedAt = now;

      this.pa.project = project;
      this.user.relatedProjects.put(this.project, pa);

      return "Cập nhật thành công";

    } catch (Exception e) {
      return "Lỗi cập nhật project";
    }
  }

  @Override
  public void deleteProject(Connection conn) {

    String delProj = "DELETE FROM Projects WHERE Project_id = ?";

    try (conn;
        PreparedStatement stmt = conn.prepareStatement(delProj)) {
      stmt.setInt(1, this.project.id);
      stmt.executeUpdate();

      this.user.relatedProjects.remove(this.project);

      System.out.println("Đã xóa project");

    } catch (Exception e) {
      System.out.println("Lỗi xóa project" + e);
    }
  } // Sau khi xóa project, instance owner phải bị xóa role, trả về user và trở về dashboard

  @Override
  public projects viewProjectInfo() {
    return this.project;
  }

  @Override
  public ArrayList<projectsAssignments> viewMembers(Connection conn) throws SQLException {
    String sql =
        "SELECT u.id, u.lname, u.fname, u.username, u.gmail, u.phone, u.dob, "
            + "pa.role, pa.assignat "
            + "FROM ProjectsAssgiments pa "
            + "JOIN users u ON u.id = pa.user_id "
            + "WHERE pa.Project_id = ?";
    ArrayList<projectsAssignments> list = new ArrayList<>();
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, this.project.id);
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          users u =
              new users(
                  rs.getInt("id"),
                  rs.getString("lname"),
                  rs.getString("fname"),
                  rs.getString("username"),
                  null,
                  rs.getString("gmail"),
                  rs.getString("phone"),
                  rs.getString("dob"));
          list.add(
              new projectsAssignments(
                  u,
                  this.project,
                  rs.getObject("assignat", LocalDateTime.class),
                  rs.getString("role")));
        }
      }
    }
    return list;
  }

  @Override
  public projectsAssignments viewMemberInfo(Connection conn, users k) throws SQLException {
    if (k == null) return null;
    return new members(this.user, this.project).viewMemberInfo(conn, k);
  }

  @Override
  public Double viewOverallProgress() {
    int goal = this.project.tasksList.size();
    int cnt = 0;
    for (tasks t : this.project.tasksList) {
      if (t.taskStatus.equals("Hoàn thành")) cnt += 1;
    }
    return (double) (cnt / goal);
  }

  @Override
  public Double viewMemberProgress(Connection conn, members k) {
    if (k == null) {
      return 0.0;
    }
    return k.viewMemberProgress(conn, k);
  }

  @Override
  public ArrayList<tasks> viewTasks() {
    return this.project.tasksList;
  }

  public tasks viewTasksInfo(tasks tk) {
    return tk;
  }

  @Override
  public void commentTask(Connection conn, tasks Tasks, String comment) {
    String sql = "INSERT INTO Comments VALUES(?,?,?);";
    try (conn;
        PreparedStatement ins = conn.prepareStatement(sql)) {
      ins.setInt(1, Tasks.taskid);
      ins.setInt(2, this.user.userId);
      ins.setString(3, comment);
      ins.executeUpdate();
      Tasks.commentsList.put(this.user, comment);
      System.out.println("Da gui comment");

    } catch (Exception e) {
      System.out.println("Khong the comments, xin thu lai " + e);
    }
  }
}
