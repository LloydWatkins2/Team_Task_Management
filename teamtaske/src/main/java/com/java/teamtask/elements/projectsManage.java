package com.java.teamtask.elements;

import com.sun.jdi.connect.spi.Connection;
import java.util.ArrayList;

/** projectsManage */
public interface projectsManage extends seeProjects {
  public abstract void deleteMember(members Mem, Connection conn);

  public abstract void addMember(users us, Connection conn);

  public abstract void assignTask(users mb, ArrayList<tasks> taske, Connection conn);

  public abstract void updateTask(
      tasks Tasks,
      String taskName,
      String taskStatus,
      String taskDescription,
      String taskDL,
      Connection conn);

  public abstract void updateProject(Connection conn);

  public abstract void deleteProject(Connection conn);
}
