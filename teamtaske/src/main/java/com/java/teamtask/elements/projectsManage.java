package com.java.teamtask.elements;

import java.util.ArrayList;

/** projectsManage */
public interface projectsManage extends seeProjects {
  public abstract void deleteMember(member Mem);

  public abstract void addMember(users us);

  public abstract void assignTask(member mb, ArrayList<tasks> taske);

  public abstract void updateProject();

  public abstract void deleteProject();
}
