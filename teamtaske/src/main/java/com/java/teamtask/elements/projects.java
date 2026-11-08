package com.java.teamtask.elements;

import java.time.LocalDateTime;
import java.util.*;

// import java.sql.*;
// import java.time.LocalDateTime;

public class projects {
  protected int id = 0;
  protected String name;
  protected String description;
  protected LocalDateTime deadline;
  protected LocalDateTime createdAt;
  protected LocalDateTime lastUpdatedAt;
  protected ArrayList<tasks> tasksList = new ArrayList<>();

  public projects(
      String n,
      String des,
      LocalDateTime dl,
      LocalDateTime ca,
      LocalDateTime up,
      ArrayList<tasks> list) {
    this.name = n;
    this.description = des;
    this.deadline = dl;
    this.createdAt = ca;
    this.lastUpdatedAt = up;
    this.tasksList = (list != null) ? list : new ArrayList<>();
  }

  public projects(
      int projectsid,
      String n,
      String des,
      LocalDateTime dl,
      LocalDateTime ca,
      LocalDateTime up,
      ArrayList<tasks> list) {
    this.id = projectsid;
    this.name = n;
    this.description = des;
    this.deadline = dl;
    this.createdAt = ca;
    this.lastUpdatedAt = up;
    this.tasksList = (list != null) ? list : new ArrayList<>();
  }

  public String getName() {
    return this.name;
  }

  public String getDes() {
    return this.description;
  }

  public LocalDateTime getDL() {
    return this.deadline;
  }

  public LocalDateTime getCreateTime() {
    return this.createdAt;
  }

  public LocalDateTime getUpdateTime() {
    return this.lastUpdatedAt;
  }

  public ArrayList<tasks> getTasksList() {
    return this.tasksList;
  }

  public void addTasks(int taskId) {}

  public void removeTasks(int taskId) {}
}
