package com.java.teamtask.connections;

import com.java.teamtask.elements.*;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;

/** sql */
public class SQL {
  public static HikariDataSource connectionsPoolInit(String url, String username, String password) {

    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(url);
    config.setUsername(username);
    config.setPassword(password);

    config.setMaximumPoolSize(10);
    config.setIdleTimeout(30000);
    config.setMaxLifetime(1800000);

    return new HikariDataSource(config);
  }

  public static void createTables(Connection conn) {
    try (conn) {
      if (conn != null) {
        ArrayList<String> tabs = new ArrayList<>();
        tabs.add(
            "CREATE TABLE IF NOT EXISTS users (\n"
                + "id SERIAL PRIMARY KEY,\n"
                + "lname VARCHAR(30) NOT NULL,\n"
                + "fname VARCHAR(20) NOT NULL,\n"
                + "username VARCHAR(30) NOT NULL,\n"
                + "password VARCHAR(255) NOT NULL,\n"
                + "gmail VARCHAR(30) NOT NULL,\n"
                + "phone VARCHAR(15),\n"
                + "dob TEXT\n"
                + ");");

        tabs.add(
            "CREATE TABLE IF NOT EXISTS Projects (\n"
                + "Project_id SERIAL PRIMARY KEY,\n"
                + "Name TEXT NOT NULL,\n"
                + "Description TEXT NOT NULL,\n"
                + "Deadline TIMESTAMP NOT NULL,\n"
                + "Create_at TIMESTAMP NOT NULL,\n"
                + "Update_at TIMESTAMP NOT NULL\n"
                + ");");

        tabs.add(
            "CREATE TABLE IF NOT EXISTS ProjectsAssgiments (\n"
                + "user_id INT NOT NULL,\n"
                + "Project_id INT NOT NULL,\n"
                + "role TEXT DEFAULT 'member',\n"
                + "assignat TIMESTAMP,\n"
                + "FOREIGN KEY (user_id) REFERENCES users(id),\n"
                + "FOREIGN KEY (Project_id) REFERENCES Projects(Projectid),\n"
                + "PRIMARY KEY(user_id, Project_id)\n"
                + ");");

        tabs.add(
            "CREATE TABLE IF NOT EXISTS Tasks (\n"
                + "taskid SERIAL PRIMARY KEY,\n"
                + "taskName TEXT NOT NULL,\n"
                + "taskDescription TEXT NOT NULL,\n"
                + "taskDeadline TIMESTAMP NOT NULL,\n"
                + "taskStatus VARCHAR(20) NOT NULL DEFAULT 'pending',\n"
                + "taskCreatedAt TIMESTAMP NOT NULL,\n"
                + "taskLastUpdatedAt TIMESTAMP NOT NULL,\n"
                + "user_id INT NOT NULL,\n"
                + "Project_id INT NOT NULL,\n"
                + "FOREIGN KEY (user_id) REFERENCES users(id),\n"
                + "FOREIGN KEY (Project_id) REFERENCES Projects(Project_id) ON DELETE CASCADE\n"
                + ");");

        tabs.add(
            "CREATE TABLE IF NOT EXISTS Comments (\n"
                + "commentid SERIAL PRIMARY KEY,\n"
                + "taskid INT NOT NULL,\n"
                + "fromwhom INT NOT NULL,\n"
                + "commentdetail TEXT NOT NULL,\n"
                + "FOREIGN KEY (taskid) REFERENCES Tasks(taskid) ON DELETE CASCADE,\n"
                + "FOREIGN KEY (fromwhom) REFERENCES users(id) ON DELETE CASCADE\n"
                + ");");
        try (Statement st = conn.createStatement()) {
          for (String sql : tabs) {
            st.execute(sql);
            System.out.println("created table user");
          }
        }
      }
    } catch (Exception e) {
      System.out.println("Loi db " + e);
    }
  }

  public static HashMap<projects, projectsAssignments> selectRelatedProject(
      Connection conn, users user) {
    String projectSql =
        "SELECT p.Project_id, p.Name, p.Description, p.Deadline, p.Create_at, p.Update_at, "
            + "pa.role, pa.assignat "
            + "FROM Projects p "
            + "JOIN ProjectsAssgiments pa ON p.Project_id = pa.Project_id "
            + "WHERE pa.user_id = ?";

    String taskSql =
        "SELECT t.taskid, t.taskName, t.taskDescription, t.taskStatus,t.taskDeadline "
            + "t.taskCreatedAt, t.taskLastUpdatedAt, "
            + "u.id AS uid, u.lname, u.fname, u.username, u.password, u.gmail, u.phone, u.dob "
            + "FROM Tasks t "
            + "JOIN users u ON t.user_id = u.id "
            + "WHERE t.Project_id = ?";

    try (conn;
        PreparedStatement getProject = conn.prepareStatement(projectSql);
        PreparedStatement getTask = conn.prepareStatement(taskSql)) {

      HashMap<projects, projectsAssignments> ans = new HashMap<>();
      getProject.setInt(1, user.userId);

      try (ResultSet rs = getProject.executeQuery()) {
        while (rs.next()) {
          int projectId = rs.getInt("p.Projectid");

          // Lấy danh sách task của project này
          ArrayList<tasks> list = new ArrayList<>();
          getTask.setInt(1, projectId);
          try (ResultSet rst = getTask.executeQuery()) {
            while (rst.next()) {
              users assignee =
                  new users(
                      rst.getInt("uid"),
                      rst.getString("u.lname"),
                      rst.getString("u.fname"),
                      rst.getString("u.username"),
                      rst.getString("u.password"),
                      rst.getString("u.gmail"),
                      rst.getString("u.phone"),
                      rst.getString("u.dob"));

              list.add(
                  new tasks(
                      rst.getInt("t.taskid"),
                      rst.getString("t.taskName"),
                      rst.getString("t.taskDescription"),
                      rst.getString("t.taskStatus"),
                      rst.getObject("t.taskCreatedAt", LocalDateTime.class),
                      rst.getObject("t.taskLastUpdatedAt", LocalDateTime.class),
                      rst.getObject("t.taskDeadline", LocalDateTime.class),
                      assignee,
                      conn));
            }
          }

          projects p =
              new projects(
                  (int) projectId,
                  rs.getString("p.Name"),
                  rs.getString("p.Description"),
                  rs.getObject("p.Deadline", LocalDateTime.class),
                  rs.getObject("p.Create_at", LocalDateTime.class),
                  rs.getObject("p.Update_at", LocalDateTime.class),
                  list);

          LocalDateTime assignAt = rs.getObject("pa.assignat", LocalDateTime.class);
          ans.put(
              p,
              new projectsAssignments(
                  user, p, assignAt == null ? null : assignAt, rs.getString("pa.role")));
        }
      }

      if (ans.isEmpty()) {
        System.out.println("no project created");
        return null;
      }
      return ans;

    } catch (Exception e) {
      System.out.println("loi truy van: " + e);
      return null;
    }
  }
}
