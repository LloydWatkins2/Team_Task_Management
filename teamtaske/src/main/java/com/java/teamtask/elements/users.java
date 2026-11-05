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

  public projects createProject() {}

  public List<projects> viewProjects() {}

  public String viewProjectStatus(projects project) {}

  public void action(projects project) {}
}
