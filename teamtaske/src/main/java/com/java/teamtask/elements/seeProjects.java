package com.java.teamtask.elements;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

/** projectsManage */
public interface seeProjects {
  public abstract String viewProjectInfo(Connection conn); // xem thông tin chi tiết 1 project

  public abstract ArrayList<projectsAssignments> viewMembers(Connection conn) throws SQLException;
 // xem danh sách thành viên

  public abstract projectsAssignments viewMemberInfo(Connection conn, users k) throws SQLException;// 

  public abstract Double viewMemberProgress(
    Connection conn,
    members k
  ); // 

  public abstract String viewTask(Connection conn);

  public abstract String commentTask(
    Connection conn,
    tasks tk,
    String cmt
  );
}
