package com.java.teamtask.elements;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.ArrayList;

/** projectsManage */
public interface projectsManage extends seeProjects {
  public abstract void deleteMember(members Mem, Connection conn);

  public abstract void addMember(users us, Connection conn);

  public abstract void assignTask(ArrayList<tasks> taske, Connection conn);

  public abstract void updateTask(
      tasks Tasks,
      String taskName,
      String taskStatus,
      String taskDescription,
      LocalDateTime taskDL,
      Connection conn);

  public abstract String updateProject(
    Connection conn,
    String name, String des, LocalDateTime dl
  );

  public abstract void deleteProject(Connection conn);
}
