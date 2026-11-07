package com.java.teamtask.elements;

import java.sql.*;
import java.util.Scanner;

/**
 * @author DELL
 */
public class system {
  public static void startApp(Connection conn) {
    Scanner sc = new Scanner(System.in);
    while (true) {
      System.out.println("1. Đăng ký tài khoản");
      System.out.println("2. Đăng nhập");
      System.out.println("0. Thoát");
      System.out.print("Chọn chức năng: ");
      int choice = sc.nextInt();
      sc.nextLine();
      switch (choice) {
        case 1:
          System.out.println("Đăng ký tài khoản");
          System.out.println("Họ: ");
          String lname = sc.nextLine();

          System.out.print("Tên: ");
          String fname = sc.nextLine();

          System.out.print("Username: ");
          String username = sc.nextLine();

          System.out.print("Mật khẩu: ");
          String password = sc.nextLine();

          System.out.print("Gmail: ");
          String gmail = sc.nextLine();

          System.out.print("Số điện thoại: ");
          String phone = sc.nextLine();

          System.out.print("Ngày sinh (dob): ");
          String dob = sc.nextLine();

          register(lname, fname, username, password, gmail, phone, dob, conn);
          break;
        case 2:
          System.out.println("Đăng nhập tài khoản");
          System.out.println("Username hoặc gmail: ");
          String UN = sc.nextLine();
          System.out.println("Mật khẩu: ");
          String MK = sc.nextLine();

          if (authentication(UN, MK, conn)) {
            int chon;
            System.out.println("chon 5 de xem subject");
            System.out.println("chon 6 de thoat");
            while ((chon = sc.nextInt()) != 6) {
              System.out.println("chon 5 de xem subject");
              System.out.println("chon 6 de thoat");
            }
          }
          break;
        case 0:
          System.out.println("Đã thoát chương trình");
          break;
        default:
          System.out.println("Lựa chọn không hợp lệ, vui lòng chọn lại");
      }
      if (choice == 0) {
        break;
      }
      sc.close();
    }
  }

  public static boolean register(
      String lname,
      String fname,
      String username,
      String password,
      String gmail,
      String phone,
      String dob,
      Connection conn) {
    String checkSql = "SELECT COUNT(*) FROM users WHERE username = ? OR gmail = ?";
    String insertSql =
        "INSERT INTO users(lname, fname, username, password, gmail, phone, dob) VALUES(?, ?, ?, ?,"
            + " ?, ?, ?)";
    try (conn) {
      try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
        checkStmt.setString(1, username);
        checkStmt.setString(2, gmail);
        ResultSet rs = checkStmt.executeQuery();
        if (rs.next() && rs.getInt(1) > 0) {
          System.out.println("Username hoặc Gmail đã tồn tại");
          return false;
        }
      }
      try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
        insertStmt.setString(1, lname);
        insertStmt.setString(2, fname);
        insertStmt.setString(3, username);
        insertStmt.setString(4, password);
        insertStmt.setString(5, gmail);
        insertStmt.setString(6, phone);
        insertStmt.setString(7, dob);

        int insert = insertStmt.executeUpdate();
        if (insert > 0) {
          System.out.println("Tạo tài khoản thành công");
          return true;
        }
      }
    } catch (SQLException e) {
      System.out.println("Lỗi SQL: " + e.getMessage());
    }
    return false;
  }

  public static boolean authentication(String username, String password, Connection conn) {
    String loginSql =
        "SELECT * FROM users WHERE ((username = ? AND password = ?) OR (gmail = ? AND password ="
            + " ?))";

    try (conn) {
      try (PreparedStatement stmt = conn.prepareStatement(loginSql)) {

        stmt.setString(1, username);
        stmt.setString(2, password);

        stmt.setString(3, username);
        stmt.setString(4, password);

        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
          String fullName = rs.getString("lname") + " " + rs.getString("fname");
          System.out.println("Đăng nhập thành công! Chào mừng " + fullName);
          return true;
        } else {
          System.out.println("Thất bại: Sai username hoặc mật khẩu!");
          return false;
        }
      }
    } catch (SQLException e) {
      System.out.println("Lỗi SQL: " + e.getMessage());
    }
    return false;
  }

  public void action(users currentuser, projects project) {}
}
