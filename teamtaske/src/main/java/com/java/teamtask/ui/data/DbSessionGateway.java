package com.java.teamtask.ui.data;

import com.java.teamtask.connections.SQL;
import com.java.teamtask.elements.members;
import com.java.teamtask.elements.owners;
import com.java.teamtask.elements.projects;
import com.java.teamtask.elements.projectsAssignments;
import com.java.teamtask.elements.seeProjects;
import com.java.teamtask.elements.system;
import com.java.teamtask.elements.tasks;
import com.java.teamtask.elements.users;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/** Triển khai gateway trên PostgreSQL/HikariCP. Gateway sở hữu vòng đời connection. */
public final class DbSessionGateway extends SessionGateway {
  private final HikariDataSource dataSource;

  public DbSessionGateway(HikariDataSource dataSource) {
    this.dataSource = dataSource;
  }

  private Connection open() throws SQLException {
    return dataSource.getConnection();
  }

  @Override
  public users authenticate(String identifier, String password) {
    try (Connection conn = open()) {
      if (!system.authentication(identifier, password, conn)) {
        return null;
      }
      users user = SQL.findUserByIdentifier(conn, identifier);
      if (user == null) {
        return null;
      }
      user.setRelatedProjects(new LinkedHashMap<>(SQL.selectRelatedProject(conn, user)));
      return user;
    } catch (SQLException e) {
      throw new DataAccessException("Không thể đăng nhập lúc này.", e);
    }
  }

  @Override
  public boolean register(Registration registration) {
    if (registration == null) {
      return false;
    }
    try (Connection conn = open()) {
      return system.register(
          registration.lname(),
          registration.fname(),
          registration.username(),
          registration.password(),
          registration.gmail(),
          registration.phone(),
          registration.dob(),
          conn);
    } catch (SQLException e) {
      String state = e.getSQLState();
      if ("23505".equals(state)) {
        throw new DataAccessException("Tên đăng nhập hoặc Gmail đã tồn tại.", e);
      }
      throw new DataAccessException("Không thể tạo tài khoản lúc này. Vui lòng thử lại.", e);
    } catch (IllegalArgumentException e) {
      throw new DataAccessException(e.getMessage(), e);
    }
  }

  @Override
  public LinkedHashMap<projects, projectsAssignments> loadProjects(users user) {
    if (user == null) {
      return new LinkedHashMap<>();
    }
    try (Connection conn = open()) {
      return new LinkedHashMap<>(SQL.selectRelatedProject(conn, user));
    } catch (SQLException e) {
      throw new DataAccessException("Không thể tải danh sách dự án.", e);
    }
  }

  @Override
  public users findUser(String identifier) {
    if (Validators.blank(identifier)) {
      return null;
    }
    try (Connection conn = open()) {
      return SQL.findUserByIdentifier(conn, identifier);
    } catch (SQLException e) {
      throw new DataAccessException("Không thể tìm người dùng.", e);
    }
  }

  @Override
  public projects createProject(
      users user, String name, String description, LocalDateTime deadline) {
    if (user == null) {
      return null;
    }
    try (Connection conn = open()) {
      return user.createProject(
          conn, user, name, description, deadline, LocalDateTime.now(), LocalDateTime.now(),
          new ArrayList<>());
    } catch (SQLException e) {
      throw new DataAccessException("Không thể tạo dự án.", e);
    }
  }

  @Override
  public boolean deleteProject(users user, projects project) {
    if (user == null || project == null) {
      return false;
    }
    try (Connection conn = open()) {
      return user.deleteProject(conn, project);
    } catch (SQLException e) {
      throw new DataAccessException("Không thể xoá dự án.", e);
    }
  }

  @Override
  public ArrayList<projectsAssignments> loadMembers(seeProjects role, projects project) {
    if (project == null) {
      return new ArrayList<>();
    }
    seeProjects source = role == null ? new members(null, project) : role;
    try (Connection conn = open()) {
      return source.viewMembers(conn);
    } catch (SQLException e) {
      throw new DataAccessException("Không thể tải thành viên dự án.", e);
    }
  }

  @Override
  public boolean addMember(seeProjects role, projects project, users member) {
    if (!(role instanceof owners owner) || member == null) {
      return false;
    }
    try (Connection conn = open()) {
      return owner.addMemberChecked(member, conn);
    } catch (SQLException e) {
      throw new DataAccessException("Không thể thêm thành viên.", e);
    }
  }

  @Override
  public boolean removeMember(seeProjects role, projects project, users member) {
    if (!(role instanceof owners owner) || member == null || project == null) {
      return false;
    }
    try (Connection conn = open()) {
      owner.deleteMember(new members(member, project), conn);
      return true;
    } catch (SQLException e) {
      throw new DataAccessException("Không thể xoá thành viên.", e);
    }
  }

  @Override
  public tasks createTask(
      projects project,
      String name,
      String description,
      String status,
      LocalDateTime deadline,
      users assignee) {
    if (project == null || assignee == null) {
      return null;
    }
    try (Connection conn = open()) {
      return project.addTasks(conn, name, description, status, deadline, assignee);
    } catch (SQLException e) {
      throw new DataAccessException("Không thể tạo công việc.", e);
    }
  }

  @Override
  public boolean deleteTask(projects project, tasks task) {
    if (project == null || task == null) {
      return false;
    }
    try (Connection conn = open()) {
      return "Đã xóa task".equals(project.removeTasks(conn, task.getTaskid()));
    } catch (SQLException e) {
      throw new DataAccessException("Không thể xoá công việc.", e);
    }
  }

  @Override
  public ArrayList<CommentView> loadComments(tasks task) {
    ArrayList<CommentView> result = new ArrayList<>();
    if (task == null) {
      return result;
    }
    try (Connection conn = open()) {
      for (SQL.CommentRow row : SQL.selectCommentsByTask(conn, task.getTaskid())) {
        result.add(UiMapper.commentView(row));
      }
      return result;
    } catch (SQLException e) {
      throw new DataAccessException("Không thể tải bình luận.", e);
    }
  }

  @Override
  public boolean addComment(seeProjects role, tasks task, String text) {
    if (role == null || task == null || Validators.blank(text)) {
      return false;
    }
    try (Connection conn = open()) {
      role.commentTask(conn, task, text.trim());
      return true;
    } catch (SQLException e) {
      throw new DataAccessException("Không thể gửi bình luận.", e);
    }
  }

  @Override
  public boolean updateTaskStatus(seeProjects role, tasks task, String status) {
    if (role == null || task == null || Validators.blank(status)) {
      return false;
    }
    try (Connection conn = open()) {
      if (role instanceof members member) {
        return member.updateAssignedTaskStatus(conn, task, status);
      }
      return task.updateStatus(conn, status);
    } catch (SQLException e) {
      throw new DataAccessException("Không thể cập nhật trạng thái công việc.", e);
    }
  }

  @Override
  public boolean updateProject(
      seeProjects role, String name, String description, LocalDateTime deadline) {
    if (!(role instanceof owners owner)) {
      return false;
    }
    try (Connection conn = open()) {
      return "Cập nhật thành công".equals(owner.updateProject(conn, name, description, deadline));
    } catch (SQLException e) {
      throw new DataAccessException("Không thể cập nhật dự án.", e);
    }
  }

  @Override
  public boolean authenticateForProfile(users user, String password) {
    if (user == null || Validators.blank(password)) {
      return false;
    }
    try (Connection conn = open()) {
      return system.authentication(user.getUsername(), password, conn);
    } catch (SQLException e) {
      throw new DataAccessException("Không thể xác thực tài khoản.", e);
    }
  }

  @Override
  public boolean updateProfile(users user, ProfileChange change) {
    if (user == null || change == null || change.isEmpty()) {
      return false;
    }
    try (Connection conn = open()) {
      return user.updateProfile(
          conn,
          change.lname(),
          change.fname(),
          change.gmail(),
          change.phone(),
          change.dob(),
          change.newPassword());
    } catch (IllegalArgumentException e) {
      throw new DataAccessException(e.getMessage(), e);
    } catch (SQLException e) {
      throw new DataAccessException("Không thể cập nhật hồ sơ.", e);
    }
  }

  @Override
  public projectsAssignments reloadProject(users user, int projectId) {
    if (user == null) {
      return null;
    }
    try (Connection conn = open()) {
      return SQL.selectRelatedProjectById(conn, user, projectId);
    } catch (SQLException e) {
      throw new DataAccessException("Không thể tải lại dự án.", e);
    }
  }

  @Override
  public void close() {
    dataSource.close();
  }
}
