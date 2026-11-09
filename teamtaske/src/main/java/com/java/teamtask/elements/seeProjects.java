package com.java.teamtask.elements;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

/** projectsManage */
public interface seeProjects {
  public abstract projects viewProjectInfo(); // xem thông tin chi tiết 1 project

  public abstract ArrayList<projectsAssignments> viewMembers(Connection conn) throws SQLException;
 // xem danh sách thành viên

  public abstract projectsAssignments viewMemberInfo(Connection conn, users k) throws SQLException;// 

  public abstract Double viewMemberProgress(Connection conn, members k); //

  public abstract ArrayList<tasks> viewTasks();

  public abstract void commentTask(
    Connection conn,
    tasks tk,
    String cmt
  );
}
