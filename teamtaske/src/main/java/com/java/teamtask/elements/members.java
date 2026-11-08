package com.java.teamtask.elements;

public class members implements seeProjects {
  private users User;
  private projects Project;

  public members(users User, projects Project) {
    this.User = User;
    this.Project = Project;
  }

  @Override
  public String viewProjectInfo() {}

  @Override
  public String viewMembers() {}

  @Override
  public Double viewOverrallProgress() {}

  @Override
  public Double viewMemberProgress(members k) {}

  @Override
  public String viewTask() {}

  @Override
  public String checkWork() {}

  @Override
  public String commentTask(tasks tk, String cmt) {}

  @Override 
  public String viewMemberInfo(members k) {}
}
