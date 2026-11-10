package com.java.teamtask.ui.data;

import com.java.teamtask.elements.projects;
import com.java.teamtask.elements.projectsAssignments;
import com.java.teamtask.elements.seeProjects;
import com.java.teamtask.elements.tasks;
import com.java.teamtask.elements.users;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Cổng dữ liệu cho UI. Có thể thay bằng bản giả lập trong test mà không cần kết nối PostgreSQL.
 */
public abstract class SessionGateway {
  public abstract users authenticate(String identifier, String password);

  public abstract boolean register(Registration registration);

  public abstract LinkedHashMap<projects, projectsAssignments> loadProjects(users user);

  public abstract users findUser(String identifier);

  public abstract projects createProject(
      users user, String name, String description, LocalDateTime deadline);

  public abstract boolean deleteProject(users user, projects project);

  public abstract ArrayList<projectsAssignments> loadMembers(seeProjects role, projects project);

  public abstract boolean addMember(seeProjects role, projects project, users member);

  public abstract boolean removeMember(seeProjects role, projects project, users member);

  public abstract tasks createTask(
      projects project,
      String name,
      String description,
      String status,
      LocalDateTime deadline,
      users assignee);

  public abstract boolean deleteTask(projects project, tasks task);

  public abstract ArrayList<CommentView> loadComments(tasks task);

  /** Chỉ tải bình luận có id lớn hơn mốc đã biết, dùng cho auto refresh 1 giây. */
  public abstract ArrayList<CommentView> loadCommentsAfter(tasks task, int afterCommentId);

  public abstract boolean addComment(seeProjects role, tasks task, String text);

  /** Thông tin thành viên + tiến độ, lấy qua logic viewMemberInfo/viewMemberProgress. */
  public abstract MemberInfoView loadMemberInfo(
      seeProjects role, projectsAssignments member);

  public abstract boolean updateTaskStatus(seeProjects role, tasks task, String status);

  public abstract boolean updateProject(
      seeProjects role, String name, String description, LocalDateTime deadline);

  public abstract boolean authenticateForProfile(users user, String password);

  public abstract boolean updateProfile(users user, ProfileChange change);

  /** Tải lại một assignment (kèm task mới nhất); trả về null nếu không còn truy cập được. */
  public projectsAssignments reloadProject(users user, int projectId) {
    return null;
  }

  public void close() {
    // Mặc định không có tài nguyên cần đóng.
  }
}
