package com.java.teamtask;

import com.java.teamtask.connections.SQL;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.SQLException;

/** Điểm vào ứng dụng Taskia. */
public final class App {
  private App() {}

  public static void main(String[] args) throws SQLException {
    // Theme.install();
    // SwingUtilities.invokeLater(
    //     () -> {
    //       MainFrame frame = new MainFrame();
    //       frame.setVisible(true);
    //     });
    HikariDataSource dataSource = SQL.connectionsPoolInit();
    SQL.createTables(dataSource.getConnection());
  }
}
