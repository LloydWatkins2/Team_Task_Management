package com.java.teamtask;

import com.java.teamtask.connections.SQL;
import com.zaxxer.hikari.HikariDataSource;
import io.github.cdimascio.dotenv.Dotenv;
import java.sql.SQLException;

public final class App {
  private App() {}

  public static void main(String[] args) throws SQLException {
    // Theme.install();
    // SwingUtilities.invokeLater(
    //     () -> {
    //       MainFrame frame = new MainFrame();
    //       frame.setVisible(true);
    //     });
    Dotenv dotenv = Dotenv.load();
    String url = dotenv.get("url");
    String username = dotenv.get("DB_USER");
    String password = dotenv.get("DB_PASSWORD");
    HikariDataSource dataSource = SQL.connectionsPoolInit(url, username, password);
    SQL.createTables(dataSource.getConnection());
  }
}
