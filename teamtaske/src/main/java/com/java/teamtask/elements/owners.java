package com.java.teamtask.elements;

import com.java.teamtask.connections.SQL;
import java.util.*;

public class owners implements projectsManage {
  private users Users;
  private projects Projects;

  public owners(users Users, projects Projects) {
    this.Users = Users;
    this.Projects = Projects;
  }

  @Override
  public void deleteMember(members Member) {}

  @Override
  public void addMember(users User) {}

  @Override
  public void assignTask(users Member, ArrayList<tasks> Tasks) {
    for (tasks e : Tasks) {
      e.assginee = Member;
      this.Projects.tasksList.add(e);
    }
  }

  @Override
  public void updateTask(
      tasks Tasks, String taskName, String taskStatus, String taskDescription, String taskDL) {

    ArrayList<String> changingData =
        Tasks.updatetask(taskName, taskStatus, taskDescription, taskDL);
    SQL.taskUpdate(changingData);
  }

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
