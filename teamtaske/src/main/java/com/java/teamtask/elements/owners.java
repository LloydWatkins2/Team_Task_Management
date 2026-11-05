package com.java.teamtask.elements;

import java.util.*;

public class owners implements projectsManage {
  private users Users;
  private projects Projects;

  public Owner(users Users, projects Projects) {
    this.Users = Users;
    this.Projects = Projects;
  }

  @Override
  public void deleteMember(members Member) {}

  @Override
  public void addMember(users User) {}

  @Override
  public void assignTask(members Member, tasks Task, List<tasks> Tasks) {}

  @Override
  public void unassignTask(members Member, tasks Task, List<tasks> Tasks) {}

  @Override
  public void updateProject() {}

  @Override
  public void deleteProject() {}

  @Override
  public String viewProjectInfo() {}

  @Override
  public members[] viewMembers() {}

  @Override
  public String viewMemberInfo(members Member) {}

  @Override
  public double viewOverallProgress() {}

  @Override
  public double viewMemberProgress(members Member) {}

  @Override
  public String viewTask() {}

  @Override
  public String checkWork() {}

  @Override
  public String commentTasks(tasks Tasks, String comment) {}
}
