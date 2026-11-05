package com.java.teamtask.elements;

import java.util.*;

public class projectsAssignments {
  protected users user;
  protected projects project;
  protected String assignedAt;
  protected String role;

  public projectsAssignments(users u, projects p, String aa, String r) {
    this.user = u;
    this.project = p;
    this.assignedAt = aa;
    this.role = r;
  }

  public String getRole() {}

  public users getUser() {}

  public projects getProject() {}
}
