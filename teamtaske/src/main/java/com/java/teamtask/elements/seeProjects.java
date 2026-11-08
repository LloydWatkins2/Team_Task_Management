package com.java.teamtask.elements;

/** projectsManage */
public interface seeProjects {
  public abstract String viewProjectInfo();

  public abstract String viewMembers();

  public abstract Double viewOverrallProgress();

  public abstract Double viewMemberProgress(members k);

  public abstract String viewTask();

  public abstract String checkWork();

  public abstract String commentTask(tasks tk, String cmt);

  public abstract String viewMemberInfo(members k);
}
