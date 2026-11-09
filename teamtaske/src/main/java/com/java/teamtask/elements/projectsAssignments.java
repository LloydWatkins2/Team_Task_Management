package com.java.teamtask.elements;

import java.time.LocalDateTime;

/** Liên kết giữa user và project kèm role. */
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

  public void setRole(String role) {
    this.role = role;
  }

  public users getUser() {
    return this.user;
  }

  public projects getProject() {
    return this.project;
  }

  public void setProject(projects project) {
    this.project = project;
  }

  public LocalDateTime getAssignedAt() {
    return this.assignedAt;
  }

  public boolean isOwner() {
    return "owner".equalsIgnoreCase(this.role);
  }
}
