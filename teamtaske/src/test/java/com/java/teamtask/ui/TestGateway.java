package com.java.teamtask.ui;

import com.java.teamtask.elements.projects;
import com.java.teamtask.elements.projectsAssignments;
import com.java.teamtask.elements.seeProjects;
import com.java.teamtask.elements.tasks;
import com.java.teamtask.elements.users;
import com.java.teamtask.ui.data.CommentView;
import com.java.teamtask.ui.data.ProfileChange;
import com.java.teamtask.ui.data.Registration;
import com.java.teamtask.ui.data.SessionGateway;
import com.java.teamtask.ui.data.UiMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/** Gateway giả lập cho test UI, không cần PostgreSQL. */
final class TestGateway extends SessionGateway {
  final users an;
  final users minh;
  final projects website;
  final projects mobile;
  final projects unrelated;
  final projectsAssignments anOwner;
  final projectsAssignments anMember;

  final LinkedHashMap<projects, projectsAssignments> anProjects = new LinkedHashMap<>();
  final List<projectsAssignments> members = new ArrayList<>();
  final List<CommentView> comments = new ArrayList<>();
  final List<users> registeredUsers = new ArrayList<>();

  Registration lastRegistration;
  ProfileChange lastProfileChange;
  RuntimeException registerFailure;
  int profileUpdateCalls;
  int updateProjectCalls;
  int deleteProjectCalls;
  int reloadProjectCalls;
  String lastTaskStatus;

  private int nextProjectId = 100;
  private int nextTaskId = 500;
  private int nextUserId = 3;

  TestGateway() {
    an =
        new users(
            1, "Nguyễn", "An", "an", "123456", "an@example.com", "0901234567", "01/01/2000");
    minh =
        new users(
            2, "Trần", "Minh", "minh", "123456", "minh@example.com", "0909876543", "02/02/1998");

    LocalDateTime now = LocalDateTime.now();
    website =
        new projects(
            10,
            "Website Redesign",
            "Thiết kế lại website công ty",
            now.plusDays(10),
            now,
            now,
            new ArrayList<>());
    mobile =
        new projects(
            11,
            "Mobile App",
            "Ứng dụng di động nội bộ",
            now.plusDays(20),
            now,
            now,
            new ArrayList<>());
    unrelated =
        new projects(
            12,
            "Dự án bí mật",
            "Người dùng hiện tại không tham gia",
            now.plusDays(30),
            now,
            now,
            new ArrayList<>());

    tasks websiteTask =
        new tasks(
            nextTaskId++,
            "Thiết kế trang Tổng quan",
            "Wireframe và bố cục dashboard",
            "IN_PROGRESS",
            now,
            now,
            now.plusDays(2),
            minh);
    tasks mobileTask =
        new tasks(
            nextTaskId++,
            "Hoàn thiện luồng đăng nhập",
            "Kiểm tra đăng nhập trên thiết bị di động",
            "TODO",
            now,
            now,
            now.plusDays(3),
            an);
    website.getTasksList().add(websiteTask);
    mobile.getTasksList().add(mobileTask);

    anOwner = new projectsAssignments(an, website, now, "owner");
    anMember = new projectsAssignments(an, mobile, now, "member");
    anProjects.put(website, anOwner);
    anProjects.put(mobile, anMember);

    projectsAssignments minhOwner = new projectsAssignments(minh, mobile, now, "owner");
    projectsAssignments minhMember = new projectsAssignments(minh, website, now, "member");
    members.add(anOwner);
    members.add(minhMember);
    members.add(minhOwner);
    members.add(anMember);
  }

  @Override
  public users authenticate(String identifier, String password) {
    if (!"123456".equals(password) || identifier == null) {
      return null;
    }
    if ("an".equals(identifier) || "an@example.com".equals(identifier)) {
      return an;
    }
    if ("minh".equals(identifier) || "minh@example.com".equals(identifier)) {
      return minh;
    }
    for (users user : registeredUsers) {
      boolean matches =
          identifier.equals(user.getUsername()) || identifier.equals(user.getGmail());
      if (matches && password.equals(user.getPassword())) {
        return user;
      }
    }
    return null;
  }

  @Override
  public boolean register(Registration registration) {
    lastRegistration = registration;
    if (registerFailure != null) {
      throw registerFailure;
    }
    if (registration == null) {
      return false;
    }
    for (users existing : registeredUsers) {
      if (registration.username().equals(existing.getUsername())
          || registration.gmail().equals(existing.getGmail())) {
        return false;
      }
    }
    if ("an".equals(registration.username()) || "minh".equals(registration.username())) {
      return false;
    }
    users created =
        new users(
            nextUserId++,
            registration.lname(),
            registration.fname(),
            registration.username(),
            registration.password(),
            registration.gmail(),
            registration.phone(),
            registration.dob());
    registeredUsers.add(created);
    return true;
  }

  @Override
  public LinkedHashMap<projects, projectsAssignments> loadProjects(users user) {
    if (user == null) {
      return new LinkedHashMap<>();
    }
    if (user.userId == an.userId) {
      return new LinkedHashMap<>(anProjects);
    }
    LinkedHashMap<projects, projectsAssignments> result = new LinkedHashMap<>();
    for (projectsAssignments assignment : members) {
      if (assignment.getUser() != null
          && assignment.getUser().userId == user.userId
          && assignment.getProject() != null) {
        result.put(assignment.getProject(), assignment);
      }
    }
    return result;
  }

  @Override
  public users findUser(String identifier) {
    if ("minh".equals(identifier) || "minh@example.com".equals(identifier)) {
      return minh;
    }
    if ("an".equals(identifier) || "an@example.com".equals(identifier)) {
      return an;
    }
    return null;
  }

  @Override
  public projects createProject(
      users user, String name, String description, LocalDateTime deadline) {
    projects project =
        new projects(
            nextProjectId++,
            name,
            description,
            deadline,
            LocalDateTime.now(),
            LocalDateTime.now(),
            new ArrayList<>());
    projectsAssignments assignment =
        new projectsAssignments(user == null ? an : user, project, LocalDateTime.now(), "owner");
    anProjects.put(project, assignment);
    members.add(assignment);
    return project;
  }

  @Override
  public boolean deleteProject(users user, projects project) {
    deleteProjectCalls++;
    anProjects.remove(project);
    members.removeIf(assignment -> assignment.getProject() == project);
    return true;
  }

  @Override
  public ArrayList<projectsAssignments> loadMembers(seeProjects role, projects project) {
    ArrayList<projectsAssignments> result = new ArrayList<>();
    if (project == null) {
      return result;
    }
    for (projectsAssignments assignment : members) {
      if (assignment.getProject() != null && assignment.getProject().getId() == project.getId()) {
        result.add(assignment);
      }
    }
    return result;
  }

  @Override
  public boolean addMember(seeProjects role, projects project, users member) {
    for (projectsAssignments assignment : members) {
      if (assignment.getProject() == project
          && assignment.getUser() != null
          && member != null
          && assignment.getUser().userId == member.userId) {
        return false;
      }
    }
    members.add(new projectsAssignments(member, project, LocalDateTime.now(), "member"));
    return true;
  }

  @Override
  public boolean removeMember(seeProjects role, projects project, users member) {
    return members.removeIf(
        assignment ->
            assignment.getProject() == project
                && assignment.getUser() != null
                && assignment.getUser().userId == member.userId);
  }

  @Override
  public tasks createTask(
      projects project,
      String name,
      String description,
      String status,
      LocalDateTime deadline,
      users assignee) {
    tasks task =
        new tasks(
            nextTaskId++,
            name,
            description,
            status,
            LocalDateTime.now(),
            LocalDateTime.now(),
            deadline,
            assignee);
    project.getTasksList().add(task);
    return task;
  }

  @Override
  public boolean deleteTask(projects project, tasks task) {
    return project.getTasksList().remove(task);
  }

  @Override
  public ArrayList<CommentView> loadComments(tasks task) {
    return new ArrayList<>(comments);
  }

  @Override
  public boolean addComment(seeProjects role, tasks task, String text) {
    comments.add(new CommentView(UiMapper.displayName(an), "", text));
    return true;
  }

  @Override
  public boolean updateTaskStatus(seeProjects role, tasks task, String status) {
    lastTaskStatus = status;
    return true;
  }

  @Override
  public boolean updateProject(
      seeProjects role, String name, String description, LocalDateTime deadline) {
    updateProjectCalls++;
    projects project = role == null ? null : projectOf(role);
    if (project == null) {
      project = website;
    }
    project.setName(name);
    project.setDescription(description);
    project.setDeadline(deadline);
    return true;
  }

  private projects projectOf(seeProjects role) {
    if (role instanceof com.java.teamtask.elements.owners owner) {
      return owner.getProject();
    }
    if (role instanceof com.java.teamtask.elements.members member) {
      return member.getProject();
    }
    return null;
  }

  @Override
  public boolean authenticateForProfile(users user, String password) {
    return "123456".equals(password);
  }

  @Override
  public boolean updateProfile(users user, ProfileChange change) {
    profileUpdateCalls++;
    lastProfileChange = change;
    return true;
  }

  @Override
  public projectsAssignments reloadProject(users user, int projectId) {
    reloadProjectCalls++;
    for (projectsAssignments assignment : loadProjects(user).values()) {
      if (assignment.getProject() != null && assignment.getProject().getId() == projectId) {
        return assignment;
      }
    }
    return null;
  }
}
