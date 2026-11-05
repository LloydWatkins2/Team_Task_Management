package com.java.teamtask.elements;

public class members implements seeProjects {
  private users Users;
  private projects Projects;

  public members(user Users, project Projects) {
    this.Users = Users;
    this.Projects = Projects;
  }

  @Override
  public String viewProjectInfo() {}

  @Override
  public String viewMembers() {}

  @Override
  public Double viewOverrallProgress() {}

  @Override
  public Double viewMemberProgress(member k) {}

  @Override
  public String viewTask() {}

  @Override
  public String checkWork() {}

  @Override
  public void commentTask(tasks tk, String cmt) {}

  public void viewMemberInfo() {}
}
