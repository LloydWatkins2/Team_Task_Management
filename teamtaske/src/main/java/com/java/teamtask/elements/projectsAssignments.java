package com.java.teamtask.elements;

import java.time.LocalDateTime;
import java.util.*;

public class projectsAssignments {
  protected users user;
  protected projects project;
  protected LocalDateTime assignedAt;
  protected String role;

  public projectsAssignments(users u, projects p, LocalDateTime aa, String r) {
    this.user = u;
    this.project = p;
    this.assignedAt = aa;
    this.role = r;
  }

  public String getRole() {
    return this.role;
  }

  public users getUser() { // dùng để truy cập thuộc tính
    return this.user;
  }

  public projects getProject() { // dùng để truy cập thuộc tính
    return this.project;
  }
}
