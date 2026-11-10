package com.java.teamtask.connections;

import com.java.teamtask.elements.*;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/** Lớp truy cập cơ sở dữ liệu dùng chung. */
public class SQL {
  private SQL() {}

  public static HikariDataSource connectionsPoolInit(
      String url, String username, String password) {
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(url);
    config.setUsername(username);
    config.setPassword(password);
    config.setMinimumIdle(5);
    config.setMaximumPoolSize(20);
    config.setConnectionTimeout(15000);
    config.setIdleTimeout(60000);
    config.setMaxLifetime(300000);
    return new HikariDataSource(config);
  }

  /**
   * Tạo bảng nếu chưa tồn tại. Không đóng connection truyền vào — người gọi chịu trách nhiệm đóng.
   */
  public static void createTables(Connection conn) {
    if (conn == null) {
      return;
    }
    ArrayList<String> tables = new ArrayList<>();
    tables.add(
        """
        CREATE TABLE IF NOT EXISTS users (
          id BIGSERIAL PRIMARY KEY,
          lname VARCHAR(30) NOT NULL,
          fname VARCHAR(20) NOT NULL,
          username VARCHAR(30) NOT NULL UNIQUE,
          password VARCHAR(255) NOT NULL,
          gmail VARCHAR(120) NOT NULL UNIQUE,
          phone VARCHAR(15),
          dob DATE
        )
        """);
    tables.add(
        """
        CREATE TABLE IF NOT EXISTS projects (
          projectid BIGSERIAL PRIMARY KEY,
          name TEXT NOT NULL,
          description TEXT NOT NULL,
          deadline TIMESTAMP NOT NULL,
          create_at TIMESTAMP NOT NULL,
          update_at TIMESTAMP NOT NULL
        )
        """);
    tables.add(
        """
        CREATE TABLE IF NOT EXISTS projectsassgiments (
          user_id BIGINT NOT NULL REFERENCES users(id),
          project_id BIGINT NOT NULL REFERENCES projects(projectid),
          role TEXT NOT NULL DEFAULT 'member',
          assignat TIMESTAMP,
          PRIMARY KEY (user_id, project_id)
        )
        """);
    tables.add(
        """
        CREATE TABLE IF NOT EXISTS tasks (
          taskid BIGSERIAL PRIMARY KEY,
          taskname TEXT NOT NULL,
          taskdescription TEXT NOT NULL,
          taskdeadline TIMESTAMP NOT NULL,
          taskstatus VARCHAR(20) NOT NULL DEFAULT 'TODO',
          taskcreatedat TIMESTAMP NOT NULL,
          tasklastupdatedat TIMESTAMP NOT NULL,
          user_id BIGINT NOT NULL REFERENCES users(id),
          project_id BIGINT NOT NULL REFERENCES projects(projectid)
        )
        """);
    tables.add(
        """
        CREATE TABLE IF NOT EXISTS comments (
          commentid BIGSERIAL PRIMARY KEY,
          taskid BIGINT NOT NULL REFERENCES tasks(taskid),
          fromwhom BIGINT NOT NULL REFERENCES users(id),
          commentdetail TEXT NOT NULL
        )
        """);

    try (Statement st = conn.createStatement()) {
      for (String sql : tables) {
        st.execute(sql);
      }
    } catch (SQLException e) {
      System.out.println("Loi tao bang: " + e.getMessage());
    }
  }

  public static users findUserByIdentifier(Connection conn, String identifier)
      throws SQLException {
    if (identifier == null || identifier.isBlank()) {
      return null;
    }
    String sql =
        "SELECT id, lname, fname, username, password, gmail, phone, dob "
            + "FROM users WHERE username = ? OR gmail = ? LIMIT 1";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setString(1, identifier.trim());
      stmt.setString(2, identifier.trim());
      try (ResultSet rs = stmt.executeQuery()) {
        return rs.next() ? readUser(rs) : null;
      }
    }
  }

  public static users findUserById(Connection conn, int userId) throws SQLException {
    String sql =
        "SELECT id, lname, fname, username, password, gmail, phone, dob "
            + "FROM users WHERE id = ?";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, userId);
      try (ResultSet rs = stmt.executeQuery()) {
        return rs.next() ? readUser(rs) : null;
      }
    }
  }

  public static users findUserByUsernameOrGmail(Connection conn, String identifier)
      throws SQLException {
    return findUserByIdentifier(conn, identifier);
  }

  public static String findRole(Connection conn, int userId, int projectId)
      throws SQLException {
    String sql =
        "SELECT role FROM projectsassgiments WHERE user_id = ? AND project_id = ?";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, userId);
      stmt.setInt(2, projectId);
      try (ResultSet rs = stmt.executeQuery()) {
        return rs.next() ? rs.getString("role") : null;
      }
    }
  }

  public static projectsAssignments findAssignment(
      Connection conn, users user, projects project) throws SQLException {
    String sql =
        "SELECT role, assignat FROM projectsassgiments WHERE user_id = ? AND project_id = ?";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, user.userId);
      stmt.setInt(2, project.getId());
      try (ResultSet rs = stmt.executeQuery()) {
        if (!rs.next()) {
          return null;
        }
        return new projectsAssignments(
            user,
            project,
            rs.getObject("assignat", LocalDateTime.class),
            rs.getString("role"));
      }
    }
  }

  /**
   * Lấy toàn bộ project liên quan tới user kèm role và danh sách task.
   *
   * <p>Chỉ các project có assignment của user mới được trả về.
   */
  public static LinkedHashMap<projects, projectsAssignments> selectRelatedProject(
      Connection conn, users user) {
    LinkedHashMap<projects, projectsAssignments> result = new LinkedHashMap<>();
    if (conn == null || user == null) {
      return result;
    }

    String projectSql =
        "SELECT p.projectid, p.name, p.description, p.deadline, p.create_at, p.update_at, "
            + "pa.role, pa.assignat "
            + "FROM projects p "
            + "JOIN projectsassgiments pa ON p.projectid = pa.project_id "
            + "WHERE pa.user_id = ? "
            + "ORDER BY p.deadline NULLS LAST, p.name";

    try (PreparedStatement getProjects = conn.prepareStatement(projectSql)) {
      getProjects.setInt(1, user.userId);
      try (ResultSet rs = getProjects.executeQuery()) {
        while (rs.next()) {
          int projectId = rs.getInt("projectid");
          ArrayList<tasks> taskList = selectTasksByProject(conn, projectId);
          projects project =
              new projects(
                  projectId,
                  rs.getString("name"),
                  rs.getString("description"),
                  rs.getObject("deadline", LocalDateTime.class),
                  rs.getObject("create_at", LocalDateTime.class),
                  rs.getObject("update_at", LocalDateTime.class),
                  taskList);
          result.put(
              project,
              new projectsAssignments(
                  user,
                  project,
                  rs.getObject("assignat", LocalDateTime.class),
                  rs.getString("role")));
        }
      }
    } catch (SQLException e) {
      System.out.println("Loi truy van project: " + e.getMessage());
    }
    return result;
  }

  /**
   * Lấy đúng một project liên quan tới user, dùng cho refresh màn hình chi tiết.
   *
   * <p>Không tải toàn bộ danh sách project như {@link #selectRelatedProject(Connection, users)}.
   */
  public static projectsAssignments selectRelatedProjectById(
      Connection conn, users user, int projectId) throws SQLException {
    if (conn == null || user == null) {
      return null;
    }

    String projectSql =
        "SELECT p.projectid, p.name, p.description, p.deadline, p.create_at, p.update_at, "
            + "pa.role, pa.assignat "
            + "FROM projects p "
            + "JOIN projectsassgiments pa ON p.projectid = pa.project_id "
            + "WHERE pa.user_id = ? AND p.projectid = ? "
            + "ORDER BY p.deadline NULLS LAST, p.name";

    try (PreparedStatement getProject = conn.prepareStatement(projectSql)) {
      getProject.setInt(1, user.userId);
      getProject.setInt(2, projectId);
      try (ResultSet rs = getProject.executeQuery()) {
        if (!rs.next()) {
          return null;
        }
        int id = rs.getInt("projectid");
        projects project =
            new projects(
                id,
                rs.getString("name"),
                rs.getString("description"),
                rs.getObject("deadline", LocalDateTime.class),
                rs.getObject("create_at", LocalDateTime.class),
                rs.getObject("update_at", LocalDateTime.class),
                selectTasksByProject(conn, id));
        return new projectsAssignments(
            user,
            project,
            rs.getObject("assignat", LocalDateTime.class),
            rs.getString("role"));
      }
    }
  }

  public static ArrayList<tasks> selectTasksByProject(Connection conn, int projectId)
      throws SQLException {
    String sql =
        "SELECT t.taskid, t.taskname, t.taskdescription, t.taskstatus, t.taskdeadline, "
            + "t.taskcreatedat, t.tasklastupdatedat, "
            + "u.id AS uid, u.lname, u.fname, u.username, u.password, u.gmail, u.phone, u.dob "
            + "FROM tasks t "
            + "JOIN users u ON t.user_id = u.id "
            + "WHERE t.project_id = ? "
            + "ORDER BY t.taskcreatedat";
    ArrayList<tasks> list = new ArrayList<>();
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, projectId);
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          users assignee =
              new users(
                  rs.getInt("uid"),
                  rs.getString("lname"),
                  rs.getString("fname"),
                  rs.getString("username"),
                  rs.getString("password"),
                  rs.getString("gmail"),
                  rs.getString("phone"),
                  Dates.formatUi(rs.getDate("dob")));
          list.add(
              new tasks(
                  rs.getInt("taskid"),
                  rs.getString("taskname"),
                  rs.getString("taskdescription"),
                  rs.getString("taskstatus"),
                  rs.getObject("taskcreatedat", LocalDateTime.class),
                  rs.getObject("tasklastupdatedat", LocalDateTime.class),
                  rs.getObject("taskdeadline", LocalDateTime.class),
                  assignee));
        }
      }
    }
    return list;
  }

  public static projects selectProjectById(Connection conn, int projectId) throws SQLException {
    String sql =
        "SELECT projectid, name, description, deadline, create_at, update_at "
            + "FROM projects WHERE projectid = ?";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, projectId);
      try (ResultSet rs = stmt.executeQuery()) {
        if (!rs.next()) {
          return null;
        }
        return new projects(
            rs.getInt("projectid"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getObject("deadline", LocalDateTime.class),
            rs.getObject("create_at", LocalDateTime.class),
            rs.getObject("update_at", LocalDateTime.class),
            selectTasksByProject(conn, projectId));
      }
    }
  }

  /** Dòng bình luận kèm người viết, dùng cho UI. */
  public record CommentRow(int commentId, users author, String detail) {}

  public static ArrayList<CommentRow> selectCommentsByTask(Connection conn, int taskId)
      throws SQLException {
    String sql =
        "SELECT c.commentid, c.commentdetail, "
            + "u.id AS uid, u.lname, u.fname, u.username, u.password, u.gmail, u.phone, u.dob "
            + "FROM comments c "
            + "JOIN users u ON c.fromwhom = u.id "
            + "WHERE c.taskid = ? "
            + "ORDER BY c.commentid";
    ArrayList<CommentRow> list = new ArrayList<>();
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, taskId);
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          users author =
              new users(
                  rs.getInt("uid"),
                  rs.getString("lname"),
                  rs.getString("fname"),
                  rs.getString("username"),
                  rs.getString("password"),
                  rs.getString("gmail"),
                  rs.getString("phone"),
                  Dates.formatUi(rs.getDate("dob")));
          list.add(
              new CommentRow(rs.getInt("commentid"), author, rs.getString("commentdetail")));
        }
      }
    }
    return list;
  }

  public static users readUser(ResultSet rs) throws SQLException {
    return new users(
        rs.getInt("id"),
        rs.getString("lname"),
        rs.getString("fname"),
        rs.getString("username"),
        rs.getString("password"),
        rs.getString("gmail"),
        rs.getString("phone"),
        Dates.formatUi(rs.getDate("dob")));
  }
}
