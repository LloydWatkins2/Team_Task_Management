package com.java.teamtask.elements;

import java.util.ArrayList;

/** projectsManage */
public interface seeProjects {
  public abstract String viewProjectInfo(); // xem thông tin chi tiết 1 project

  public abstract String viewMembers(); // xem danh sách thành viên

  public abstract Double viewOverrallProgress(); //

  public abstract Double viewMemberProgress(members k); //

  public abstract ArrayList<tasks> viewTask();

  public abstract String commentTask(tasks tk, String cmt);

  public abstract String viewMemberInfo(members k); // xem thông tin chi tiết 1 member
}
