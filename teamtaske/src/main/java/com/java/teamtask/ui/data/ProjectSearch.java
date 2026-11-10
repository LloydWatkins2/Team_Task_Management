package com.java.teamtask.ui.data;

import com.java.teamtask.elements.projectsAssignments;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Tìm kiếm dự án trong bộ nhớ: không phân biệt hoa thường/dấu tiếng Việt, yêu cầu khớp toàn bộ
 * token và ưu tiên kết quả khớp ở tên dự án hơn phần mô tả.
 */
public final class ProjectSearch {
    private ProjectSearch() {
    }

    /** Chuẩn hoá chuỗi: chữ thường, bỏ dấu, gộp khoảng trắng. */
    public static String normalize(String text) {
        if (text == null) {
            return "";
        }
        String decomposed = Normalizer.normalize(text, Normalizer.Form.NFD);
        StringBuilder result = new StringBuilder(decomposed.length());
        for (int i = 0; i < decomposed.length(); i++) {
            char ch = decomposed.charAt(i);
            if (Character.getType(ch) == Character.NON_SPACING_MARK) {
                continue;
            }
            char lower = Character.toLowerCase(ch);
            result.append(lower == 'đ' ? 'd' : lower);
        }
        return result.toString().trim().replaceAll("\\s+", " ");
    }

    /** Lọc và xếp hạng danh sách dự án theo từ khoá; giữ nguyên thứ tự khi cùng điểm. */
    public static List<projectsAssignments> filter(
            Collection<projectsAssignments> assignments, String query) {
        List<projectsAssignments> source = assignments == null ? List.of() : new ArrayList<>(assignments);
        String normalizedQuery = normalize(query);
        if (normalizedQuery.isEmpty()) {
            return source;
        }
        String[] tokens = normalizedQuery.split(" ");
        List<Match> matches = new ArrayList<>();
        for (projectsAssignments assignment : source) {
            if (assignment == null || assignment.getProject() == null) {
                continue;
            }
            String name = normalize(assignment.getProject().getName());
            String description = normalize(assignment.getProject().getDescription());
            int score = score(tokens, name, description);
            if (score > 0) {
                matches.add(new Match(assignment, score));
            }
        }
        matches.sort(Comparator.comparingInt(Match::score).reversed());
        List<projectsAssignments> result = new ArrayList<>(matches.size());
        for (Match match : matches) {
            result.add(match.assignment());
        }
        return result;
    }

    /** Trả về true nếu mọi token đều xuất hiện trong tên hoặc mô tả dự án. */
    public static boolean matches(String query, String name, String description) {
        String normalizedQuery = normalize(query);
        if (normalizedQuery.isEmpty()) {
            return true;
        }
        return score(normalizedQuery.split(" "), normalize(name), normalize(description)) > 0;
    }

    private static int score(String[] tokens, String name, String description) {
        int score = 0;
        for (String token : tokens) {
            if (name.contains(token)) {
                score += 10;
            } else if (description.contains(token)) {
                score += 1;
            } else {
                return 0;
            }
        }
        return score;
    }

    private record Match(projectsAssignments assignment, int score) {
    }
}
