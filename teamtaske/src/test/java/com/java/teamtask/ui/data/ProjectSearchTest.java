package com.java.teamtask.ui.data;

import com.java.teamtask.elements.projects;
import com.java.teamtask.elements.projectsAssignments;
import com.java.teamtask.elements.users;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Kiểm tra bộ lọc dự án: bỏ dấu tiếng Việt, AND token và xếp hạng theo tên. */
class ProjectSearchTest {
  private final users user =
      new users(1, "Nguyễn", "An", "an", "123456", "an@example.com", "0901234567", "01/01/2000");

  @Test
  void normalizeRemovesVietnameseAccents() {
    assertEquals("du an website", ProjectSearch.normalize("  Dự   Án Website "));
    assertEquals("dang lam viec", ProjectSearch.normalize("Đang làm việc"));
    assertEquals("", ProjectSearch.normalize(null));
  }

  @Test
  void blankQueryKeepsOriginalOrder() {
    List<projectsAssignments> source =
        List.of(assignment("Website Redesign", "Thiết kế"), assignment("Mobile App", "Ứng dụng"));

    assertEquals(source, ProjectSearch.filter(source, "  "));
  }

  @Test
  void searchIgnoresCaseAndAccents() {
    List<projectsAssignments> source =
        List.of(assignment("Dự án nội bộ", "Quản lý nhân sự"), assignment("Website", "Thiết kế"));

    List<projectsAssignments> filtered = ProjectSearch.filter(source, "DU AN");

    assertEquals(1, filtered.size());
    assertEquals("Dự án nội bộ", filtered.get(0).getProject().getName());
    assertTrue(ProjectSearch.matches("du an", "Dự án nội bộ", ""));
  }

  @Test
  void allTokensMustMatchNameOrDescription() {
    List<projectsAssignments> source =
        List.of(
            assignment("Mobile App", "Ứng dụng di động nội bộ"),
            assignment("Website Redesign", "Thiết kế lại website công ty"));

    assertTrue(ProjectSearch.filter(source, "ung dung").size() == 1);
    assertTrue(ProjectSearch.filter(source, "website mobile").isEmpty());
    assertFalse(ProjectSearch.matches("website mobile", "Website Redesign", "Thiết kế"));
  }

  @Test
  void nameMatchesRankAboveDescriptionMatches() {
    List<projectsAssignments> source =
        List.of(
            assignment("Beta", "Alpha nội bộ"),
            assignment("Alpha", "Mô tả khác"));

    List<projectsAssignments> filtered = ProjectSearch.filter(source, "alpha");

    assertEquals(2, filtered.size());
    assertEquals("Alpha", filtered.get(0).getProject().getName(),
        "Khớp ở tên phải đứng trước khớp ở mô tả");
  }

  @Test
  void filtersNullAssignmentsSafely() {
    List<projectsAssignments> source = new ArrayList<>();
    source.add(null);
    source.add(assignment("Website", "Thiết kế"));

    assertEquals(1, ProjectSearch.filter(source, "website").size());
  }

  private projectsAssignments assignment(String name, String description) {
    LocalDateTime now = LocalDateTime.now();
    projects project =
        new projects(0, name, description, now.plusDays(3), now, now, new ArrayList<>());
    return new projectsAssignments(user, project, now, "owner");
  }
}
