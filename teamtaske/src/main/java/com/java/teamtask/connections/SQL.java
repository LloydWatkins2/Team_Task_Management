package com.java.teamtask.connections;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.*;
import java.util.ArrayList;

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
                + "id BIGSERIAL PRIMARY KEY,\n"
                + "lname VARCHAR(30) NOT NULL,\n"
                + "fname VARCHAR(20) NOT NULL,\n"
                + "username VARCHAR(30) NOT NULL,\n"
                + "password VARCHAR(255) NOT NULL,\n"
                + "gmail VARCHAR(30) NOT NULL,\n"
                + "phone VARCHAR(15),\n"
                + "dob DATE\n"
                + ");");

        tabs.add(
            "CREATE TABLE IF NOT EXISTS Projects (\n"
                + "Projectid BIGSERIAL PRIMARY KEY,\n"
                + "Name TEXT NOT NULL,\n"
                + "Description TEXT NOT NULL,\n"
                + "Deadline TIMESTAMP NOT NULL,\n"
                + "Create_at TIMESTAMP NOT NULL,\n"
                + "Update_at TIMESTAMP NOT NULL\n"
                + ");");

        tabs.add(
            "CREATE TABLE IF NOT EXISTS ProjectsAssgiments (\n"
                + "user_id BIGINT NOT NULL,\n"
                + "Project_id BIGINT NOT NULL,\n"
                + "role TEXT DEFAULT 'member',\n"
                + "FOREIGN KEY (user_id) REFERENCES users(id),\n"
                + "FOREIGN KEY (Project_id) REFERENCES Projects(Projectid),\n"
                + "PRIMARY KEY(user_id, Project_id)\n"
                + ");");

        tabs.add(
            "CREATE TABLE IF NOT EXISTS Tasks (\n"
                + "taskid BIGSERIAL PRIMARY KEY,\n"
                + "taskName TEXT NOT NULL,\n" + "taskDescription TEXT NOT NULL,\n"
                + "taskDeadline TIMESTAMP NOT NULL,\n"
                + "taskStatus VARCHAR(20) NOT NULL DEFAULT 'pending',\n"
                + "taskCreatedAt TIMESTAMP NOT NULL,\n"
                + "taskLastUpdatedAt TIMESTAMP NOT NULL,\n"
                + "user_id BIGINT NOT NULL,\n"
                + "Project_id BIGINT NOT NULL,\n"
                + "FOREIGN KEY (user_id) REFERENCES users(id),\n"
                + "FOREIGN KEY (Project_id) REFERENCES Projects(Projectid)\n"
                + ");");

        tabs.add(
            "CREATE TABLE IF NOT EXISTS Comments (\n"
                + "commentid BIGSERIAL PRIMARY KEY,\n"
                + "taskid BIGINT NOT NULL,\n"
                + "fromwhom BIGINT NOT NULL,\n"
                + "commentdetail TEXT NOT NULL,\n"
                + "FOREIGN KEY (taskid) REFERENCES Tasks(taskid),\n"
                + "FOREIGN KEY (fromwhom) REFERENCES users(id)\n"
                + ");");
        for (String sql : tabs) {
          try (Statement st = conn.createStatement()) {
            st.execute(sql);
            System.out.println("created table user");
          }
        }
      }
    } catch (Exception e) {
      System.out.println("Loi db " + e);
    }
  }
}
