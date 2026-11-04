package com.java.teamtask.connections;

import java.sql.*;
import java.util.ArrayList;

/** sql */
public class SQL {
  public static void createTables() {
    String url = "jdbc:sqlite:teamtask.db";
    try (Connection conn = DriverManager.getConnection(url)) {
      if (conn != null) {
        ArrayList<String> tabs = new ArrayList<>();
        tabs.add(
            "create table if not exists users (\n"
                + "id INT auto_increment primary key, \n"
                + "lname varchar(30) not null,\n"
                + "fname varchar(20) not null,\n"
                + "username varchar(30) not null ,\n"
                + "password varchar(20) not null,\n"
                + "gmail varchar(30) not null,\n"
                + "phone varchar(15),\n"
                + "dob DATE"
                + ");");
        tabs.add(
            "create table if not exists Projects (\n"
                + "Projectid INT auto_increment primary key, \n"
                + "Name TEXT not null,\n"
                + "Description TEXT not null,\n"
                + "Deadline DATETIME not null ,\n"
                + "Create_at DATETIME not null,\n"
                + "Update_at DATETIME not null\n"
                + ");");
        tabs.add(
            "create table if not exists ProjectsAssgiments (\n"
                + "user_id INT , \n"
                + "Project_id INT , \n"
                + "role TEXT default member, \n"
                + "foreign key (user_id) references users(id), \n"
                + "foreign key (Project_id) references Projects(Projectid), \n"
                + "primary key(user_id,Project_id)\n"
                + ");");
        tabs.add(
            "create table if not exists Tasks (\n"
                + "taskid INT auto_increment primary key, \n"
                + "taskName TEXT not null,\n"
                + "taskDescription TEXT not null,\n"
                + "taskDeadline DATETIME not null ,\n"
                + "taskStatus DATETIME not null,\n"
                + "taskCreatedAt DATETIME not null,\n"
                + "taskLastUpdatedAt DATETIME not null,\n"
                + "user_id INT not null,\n"
                + "Project_id INT not null,\n"
                + "foreign key (user_id) references users(id), \n"
                + "foreign key (Project_id) references Projects(Projectid) \n"
                + ");");
        tabs.add(
            "create table if not exists Comments (\n"
                + "taskid INT auto_increment primary key, \n"
                + "fromwhom INT not null,\n"
                + "commentdetail TEXT not null,\n"
                + "foreign key (taskid) references Tasks(taskid), \n"
                + "foreign key (fromwhom) references users(id) \n"
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
