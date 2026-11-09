package com.java.teamtask.elements;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/** Hành động của owner trong một project. */
public class owners implements projectsManage {
  private final users user;
  private final projects project;
  private final projectsAssignments pa;

  public owners(users user, projects project, projectsAssignments assignment) {
    this.user = user;
    this.project = project;
    this.pa = assignment;
  }

  public users getUser() {
    return this.user;
  }

  public projects getProject() {
    return this.project;
  }

  @Override
  public void deleteMember(members member, Connection conn) {
    if (member == null || member.getUser() == null || conn == null) {
      return;
    }
    int memberId = member.getUser().userId;
    if (memberId == this.user.userId) {
      System.out.println("Bạn là Owner, không thể tự xoá chính mình khỏi dự án");
      return;
    }
    String checkSql =
        "SELECT 1 FROM projectsassgiments "
            + "WHERE user_id = ? AND project_id = ? AND role <> 'owner'";
    String updateTasksSql =
        "UPDATE tasks SET user_id = ? WHERE project_id = ? AND user_id = ?";
    String deleteSql =
        "DELETE FROM projectsassgiments "
            + "WHERE user_id = ? AND project_id = ? AND role <> 'owner'";
    try {
      try (PreparedStatement check = conn.prepareStatement(checkSql)) {
        check.setInt(1, memberId);
        check.setInt(2, this.project.getId());
        try (ResultSet rs = check.executeQuery()) {
          if (!rs.next()) {
            System.out.println("Thành viên không tồn tại trong dự án");
            return;
          }
        }
      }
      try (PreparedStatement update = conn.prepareStatement(updateTasksSql)) {
        update.setInt(1, this.user.userId);
        update.setInt(2, this.project.getId());
        update.setInt(3, memberId);
        update.executeUpdate();
      }
      try (PreparedStatement del = conn.prepareStatement(deleteSql)) {
        del.setInt(1, memberId);
        del.setInt(2, this.project.getId());
        del.executeUpdate();
      }
      for (tasks task : this.project.getTasksList()) {
        if (task.getAssginee() != null && task.getAssginee().userId == memberId) {
          task.assginee = this.user;
        }
      }
      System.out.println("Đã xoá thành viên khỏi dự án.");
    } catch (SQLException e) {
      System.out.println("Lỗi xoá thành viên: " + e.getMessage());
    }
  }

  @Override
  public void addMember(users newMember, Connection conn) {
    addMemberChecked(newMember, conn);
  }

  /** Thêm thành viên và trả về true nếu thực sự thêm mới. */
  public boolean addMemberChecked(users newMember, Connection conn) {
    if (newMember == null || conn == null) {
      return false;
    }
    String checkSql =
        "SELECT 1 FROM projectsassgiments WHERE user_id = ? AND project_id = ?";
    String insertSql =
        "INSERT INTO projectsassgiments(user_id, project_id, role, assignat) "
            + "VALUES(?, ?, 'member', ?)";
    try {
      try (PreparedStatement check = conn.prepareStatement(checkSql)) {
        check.setInt(1, newMember.userId);
        check.setInt(2, this.project.getId());
        try (ResultSet rs = check.executeQuery()) {
          if (rs.next()) {
            System.out.println("Người dùng đã là thành viên của dự án.");
            return false;
          }
        }
      }
      try (PreparedStatement ins = conn.prepareStatement(insertSql)) {
        ins.setInt(1, newMember.userId);
        ins.setInt(2, this.project.getId());
        ins.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
        ins.executeUpdate();
        System.out.println("Đã thêm thành viên.");
        return true;
      }
    } catch (SQLException e) {
      System.out.println("Lỗi thêm thành viên: " + e.getMessage());
      return false;
    }
  }

  @Override
  public void assignTask(tasks task, users assignee, Connection conn) {
    if (task == null || assignee == null || conn == null) {
      return;
    }
    String sql = "UPDATE tasks SET user_id = ? WHERE taskid = ?";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, assignee.userId);
      stmt.setInt(2, task.getTaskid());
      if (stmt.executeUpdate() > 0) {
        task.assginee = assignee;
        if (!this.project.getTasksList().contains(task)) {
          this.project.getTasksList().add(task);
        }
        System.out.println("Giao việc thành công");
      }
    } catch (SQLException e) {
      System.out.println("Lỗi giao việc: " + e.getMessage());
    }
  }

  @Override
  public void updateTask(
      tasks task,
      String taskName,
      String taskStatus,
      String taskDescription,
      LocalDateTime taskDeadline,
      Connection conn) {
    if (task == null || conn == null) {
      return;
    }
    LocalDateTime now = LocalDateTime.now();
    tasks updated =
        new tasks(
            task.getTaskid(),
            taskName == null || taskName.isBlank() ? task.getTaskName() : taskName,
            taskDescription == null ? task.getTaskDescription() : taskDescription,
            taskStatus == null || taskStatus.isBlank() ? task.getTaskStatus() : taskStatus,
            task.getTaskCreatedAt(),
            now,
            taskDeadline == null ? task.getTaskDeadline() : taskDeadline,
            task.getAssginee());
    task.updatetask(conn, updated);
  }

  @Override
  public String updateProject(
      Connection conn, String name, String description, LocalDateTime deadline) {
    if (conn == null) {
      return "Lỗi cập nhật project";
    }
    String sql =
        "UPDATE projects SET name = ?, description = ?, deadline = ?, update_at = ? "
            + "WHERE projectid = ?";
    LocalDateTime now = LocalDateTime.now();
    String projectName = name == null || name.isBlank() ? this.project.getName() : name;
    String projectDescription =
        description == null ? this.project.getDes() : description;
    LocalDateTime projectDeadline =
        deadline == null ? this.project.getDL() : deadline;
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setString(1, projectName);
      stmt.setString(2, projectDescription);
      stmt.setTimestamp(3, Timestamp.valueOf(projectDeadline));
      stmt.setTimestamp(4, Timestamp.valueOf(now));
      stmt.setInt(5, this.project.getId());
      if (stmt.executeUpdate() == 0) {
        return "Không tìm thấy project";
      }
      this.project.name = projectName;
      this.project.description = projectDescription;
      this.project.deadline = projectDeadline;
      this.project.lastUpdatedAt = now;
      if (this.pa != null) {
        this.pa.setProject(this.project);
        this.user.putAssignment(this.project, this.pa);
      }
      return "Cập nhật thành công";
    } catch (SQLException e) {
      return "Lỗi cập nhật project: " + e.getMessage();
    }
  }

  @Override
  public void deleteProject(Connection conn) {
    if (this.user.deleteProject(conn, this.project)) {
      System.out.println("Đã xóa project");
    }
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
  public projectsAssignments viewMemberInfo(Connection conn, users user) throws SQLException {
    if (user == null) {
      return null;
    }
    return new members(this.user, this.project).viewMemberInfo(conn, user);
  }

  @Override
  public Double viewOverallProgress() {
    ArrayList<tasks> list = this.project.getTasksList();
    if (list == null || list.isEmpty()) {
      return 0.0;
    }
    int done = 0;
    for (tasks task : list) {
      if ("DONE".equalsIgnoreCase(task.getTaskStatus())
          || "Hoàn thành".equalsIgnoreCase(task.getTaskStatus())) {
        done++;
      }
    }
    return (double) done / list.size();
  }

  @Override
  public Double viewMemberProgress(Connection conn, members member) {
    if (member == null) {
      return 0.0;
    }
    return member.viewMemberProgress(conn, member);
  }

  @Override
  public ArrayList<tasks> viewTasks() {
    return this.project.getTasksList();
  }

  public tasks viewTasksInfo(tasks task) {
    return task;
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
}
