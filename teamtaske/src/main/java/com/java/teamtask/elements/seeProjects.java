package com.java.teamtask.elements;

import java.sql.Connection;
import java.util.ArrayList;

/** projectsManage */
public interface seeProjects {
  public abstract String viewProjectInfo(Connection conn); // xem thông tin chi tiết 1 project

  public abstract String viewMembers(Connection conn); // xem danh sách thành viên

  public abstract Double viewOverrallProgress(); //

  public abstract Double viewMemberProgress(Connection conn, members k); //

  public abstract ArrayList<tasks> viewTask();

  public abstract void commentTask(Connection conn, tasks tk, String cmt);

  public abstract String viewMemberInfo(
      Connection conn, members k); // xem thông tin chi tiết 1 member
}
