package com.java.teamtask.elements;

import java.util.*;
// import java.sql.*;
// import java.time.LocalDateTime;

public class projects {
  protected int id = 0;
  protected String name;
  protected String description;
  protected String deadline;
  protected String createdAt;
  protected String lastUpdatedAt;
  protected ArrayList<tasks> tasksList = new ArrayList<>();

  public projects(
    String n,
    String des,
    String dl,
    String ca,
    String up,
    ArrayList<tasks> list
  ) {
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

  public String getDL() {
    return this.deadline;
  }

  public String getCreateTime() {
    return this.createdAt;
  }

  public String getUpdateTime() {
    return this.lastUpdatedAt;
  }

  public ArrayList<tasks> getTasksList() {
    return this.tasksList;
  }

  public void addTasks(int taskId) {}

  public void removeTasks(int taskId) {}
}
