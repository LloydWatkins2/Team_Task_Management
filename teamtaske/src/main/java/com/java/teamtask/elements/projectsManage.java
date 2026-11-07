package com.java.teamtask.elements;

import java.util.ArrayList;

/** projectsManage */
public interface projectsManage extends seeProjects {
  public abstract void deleteMember(members Mem);

  public abstract void addMember(users us);

  public abstract void assignTask(users mb, ArrayList<tasks> taske);

  public abstract void updateTask(
      tasks Tasks, String taskName, String taskStatus, String taskDescription, String taskDL);

  public abstract void updateProject();

  public abstract void deleteProject();
}
