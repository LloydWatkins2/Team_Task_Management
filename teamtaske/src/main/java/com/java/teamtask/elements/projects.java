package com.java.teamtask.elements;

import java.util.*;
// import java.sql.*;
// import java.time.LocalDateTime;

public class projects {
  protected int id;
  protected String name;
  protected String description;
  protected String deadline;
  protected String createdAt;
  protected String lastUpdatedAt;
  protected ArrayList<tasks> tasksList;

  public projects(String n, String des, String dl, String ca, String up, ArrayList<tasks> list) {
    this.id = id;
    this.name = n;
    this.description = des;
    this.deadline = dl;
    this.createdAt = ca;
    this.lastUpdatedAt = up;
    this.tasksList = list;
  }

  public String getName() {}
  
  public String getDes() {}

  public String getDL() {}

  public String getCreateTime() {}

  public String getUpdateTime() {}

  public ArrayList<tasks> getTasksList() {}

  public void addTasks(int taskId) {}

  public void removeTasks(int taskId) {}
}
