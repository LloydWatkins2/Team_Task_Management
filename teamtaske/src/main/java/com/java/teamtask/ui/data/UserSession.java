package com.java.teamtask.ui.data;

import com.java.teamtask.elements.projects;
import com.java.teamtask.elements.projectsAssignments;
import com.java.teamtask.elements.seeProjects;
import com.java.teamtask.elements.system;
import com.java.teamtask.elements.users;

/**
 * Phiên đăng nhập hiện tại. Giữ user đầy đủ từ DB và instance quyền owner/member của project đang
 * mở. Rời project hoặc xoá project phải gọi {@link #clearActiveProject()}.
 */
public final class UserSession {
  private users user;
  private projectsAssignments activeAssignment;
  private seeProjects activeRole;

  public users getUser() {
    return user;
  }

  public void setUser(users user) {
    this.user = user;
    clearActiveProject();
  }

  public boolean isAuthenticated() {
    return user != null;
  }

  /** Mở project và tạo instance quyền tương ứng qua {@code system.roleChecker}. */
  public seeProjects openProject(projectsAssignments assignment) {
    if (user == null || assignment == null || assignment.getProject() == null) {
      clearActiveProject();
      return null;
    }
    this.activeAssignment = assignment;
    this.activeRole = system.roleChecker(assignment, user, assignment.getProject());
    return this.activeRole;
  }

  public projectsAssignments getActiveAssignment() {
    return activeAssignment;
  }

  public projects getActiveProject() {
    return activeAssignment == null ? null : activeAssignment.getProject();
  }

  public seeProjects getActiveRole() {
    return activeRole;
  }

  public boolean hasActiveProject() {
    return activeAssignment != null && activeRole != null;
  }

  /** Xoá ngay instance owner/member đang hoạt động. */
  public void clearActiveProject() {
    this.activeAssignment = null;
    this.activeRole = null;
  }

  public void logout() {
    clearActiveProject();
    this.user = null;
  }
}
