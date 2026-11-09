package com.java.teamtask.elements;

import java.sql.Connection;
<<<<<<< HEAD
=======
import java.time.LocalDateTime;
>>>>>>> 0fa24d37a0866998c3420842af6047baf1dfaa84
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

  public abstract void updateProject(Connection conn);

  public abstract String deleteProject(Connection conn, int pid);
}
