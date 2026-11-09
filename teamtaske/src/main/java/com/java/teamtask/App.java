package com.java.teamtask;

import com.java.teamtask.connections.SQL;
import com.java.teamtask.ui.MainFrame;
import com.java.teamtask.ui.data.DbSessionGateway;
import com.java.teamtask.ui.theme.Theme;
import com.zaxxer.hikari.HikariDataSource;
import io.github.cdimascio.dotenv.Dotenv;
import java.sql.Connection;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public final class App {
  private App() {}

  public static void main(String[] args) {
    Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
    String url = dotenv.get("url");
    String username = dotenv.get("DB_USER");
    String password = dotenv.get("DB_PASSWORD");

    if (url == null || url.isBlank()) {
      JOptionPane.showMessageDialog(
          null,
          "Thiếu cấu hình kết nối cơ sở dữ liệu trong .env (url).",
          "Lỗi cấu hình",
          JOptionPane.ERROR_MESSAGE);
      return;
    }

    HikariDataSource dataSource = SQL.connectionsPoolInit(url, username, password);
    try (Connection conn = dataSource.getConnection()) {
      SQL.createTables(conn);
    } catch (SQLException e) {
      System.err.println("Không thể kiểm tra bảng: " + e.getMessage());
    }

    Theme.install();
    SwingUtilities.invokeLater(
        () -> {
          MainFrame frame = new MainFrame(new DbSessionGateway(dataSource));
          frame.setVisible(true);
        });
    Runtime.getRuntime().addShutdownHook(new Thread(dataSource::close));
  }
}
