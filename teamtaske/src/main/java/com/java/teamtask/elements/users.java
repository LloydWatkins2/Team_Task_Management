package com.java.teamtask.elements;

import java.sql.*;
import java.util.*;

public class users {
  public int userId;

  protected String lname;
  protected String fname;
  protected String password;
  protected String email;

  protected String phone;
  protected String dob;
  protected HashMap<projects, projectsAssignments> relatedProjects;

  public users(
      int userId,
      String lname,
      String fname,
      String password,
      String email,
      String phone,
      String dob) {
    this.userId = userId;
    this.lname = lname;
    this.fname = fname;
    this.email = email;
    this.password = password;
    this.phone = phone;
    this.dob = dob;
  }

  public users(String lname, String fname, String email, String password) {
    this.lname = lname;
    this.fname = fname;
    this.email = email;
    this.password = password;
    this.relatedProjects = new HashMap<>();
  }

  public users(
      int userId,
      String lname,
      String fname,
      String password,
      String email,
      String phone,
      String dob,
      HashMap<projects, projectsAssignments> relatedProjects) {
    this.userId = userId;
    this.lname = lname;
    this.fname = fname;
    this.email = email;
    this.password = password;
    this.phone = phone;
    this.dob = dob;
    this.relatedProjects = relatedProjects;
  }

  public void deleteAccount() {}

  public void updateAccount() {}

  public projects createProject(
      String n, String des, String dl, String ca, String up, ArrayList<tasks> list) {
    projects Project = new projects(n, des, dl, ca, up, list);
    return Project;
  }

  public ArrayList<projects> viewProjects() {}

  public String viewProjectStatus(projects project) {}

  public void action(projects project) {}
}
