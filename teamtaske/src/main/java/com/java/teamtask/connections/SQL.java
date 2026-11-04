package com.java.teamtask.connections;

import java.sql.*;

/** sql */
public class SQL {
  public static void actions() {
    String url = "jdbc:sqlite:teamtask.db";
    try (Connection conn = DriverManager.getConnection(url)) {
      if (conn != null) {
        String sql =
            "create table if not exists users (\n"
                + "id INT auto_increment primary key, \n"
                + "lname varchar(30) not null,\n"
                + "fname varchar(20) not null,\n"
                + "username varchar(30) not null ,\n"
                + "password varchar(20) not null,\n"
                + "gmail varchar(30) not null,\n"
                + "phone varchar(15),\n"
                + "dob varchar(30)"
                + ");";
        try (Statement st = conn.createStatement()) {
          st.execute(sql);
          System.out.println("created table user");
        }
      }
    } catch (Exception e) {
      System.out.println("Loi db " + e);
    }
  }
}
