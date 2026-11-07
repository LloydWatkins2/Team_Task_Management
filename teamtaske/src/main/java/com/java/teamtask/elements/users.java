package com.java.teamtask.elements;

import java.sql.*;
import java.util.*;

public class users {
  protected int userId;
  protected String name;
  protected String password;
  protected String email;
  protected Map<projects, projectsAssignments> relatedProjects;

  public users(String name, String email, String password) {
    this.name = name;
    this.email = email;
    this.password = password;
    this.relatedProjects = new HashMap<>();
  }

  public void deleteAccount() {}

  public void updateAccount() {}

  public projects createProject(
    String n,
    String des,
    String dl,
    String ca,
    String up,
    ArrayList<tasks> list
  ) {
    projects Project = new projects(n, des, dl, ca, up, list);
    return Project;
  }

  public ArrayList<projects> viewProjects() { // chỉ xem thông tin cơ bản
    ArrayList<projects> Projects = new ArrayList<>(this.relatedProjects.keySet());
    return Projects;
  }

  public String viewProjectStatus(projects project) {}

  public void action(projects project) {}
}
