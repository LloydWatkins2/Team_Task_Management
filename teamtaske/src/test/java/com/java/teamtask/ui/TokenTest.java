package com.java.teamtask.ui;

import com.java.teamtask.ui.theme.Palette;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/** Đảm bảo bảng màu khớp design và component/màn hình không hard-code màu. */
class TokenTest {
    private static final Path UI_ROOT = Path.of("src", "main", "java", "com", "java", "teamtask", "ui");
    private static final Pattern COLOR_LITERAL = Pattern.compile(
            "new\\s+(?:java\\.awt\\.)?Color\\s*\\(|0x[0-9A-Fa-f]{6}");

    @Test
    void paletteMatchesDesignTokens() {
        assertEquals(new Color(0x7A5CFA), Palette.PRIMARY);
        assertEquals(new Color(0x5B3FE0), Palette.PRIMARY_DARK);
        assertEquals(new Color(0xECEEF8), Palette.CONTENT);
        assertEquals(new Color(0xFFD66B), Palette.ACCENT);
        assertEquals(new Color(0xF04E4E), Palette.DANGER);
        assertEquals(new Color(0x1FA85A), Palette.SUCCESS);
        assertEquals(new Color(0xF5A524), Palette.WARNING);
        assertEquals(new Color(0x14122B), Palette.TEXT);
        assertEquals(new Color(0xE7E9F3), Palette.BORDER);
        assertEquals(new Color(0xCDEEFF), Palette.TASK_BLUE);
        assertEquals(new Color(0xFFE3A3), Palette.TASK_YELLOW);
        assertEquals(new Color(0xD9D1FF), Palette.TASK_PURPLE);
    }

    @Test
    void componentsAndScreensDoNotHardCodeColors() throws IOException {
        List<String> violations = new ArrayList<>();
        for (String folder : List.of("components", "screens")) {
            Path dir = UI_ROOT.resolve(folder);
            if (!Files.isDirectory(dir)) {
                continue;
            }
            try (Stream<Path> files = Files.walk(dir)) {
                files.filter(path -> path.toString().endsWith(".java")).forEach(path -> {
                    try {
                        String source = Files.readString(path, StandardCharsets.UTF_8);
                        if (COLOR_LITERAL.matcher(source).find()) {
                            violations.add(UI_ROOT.relativize(path).toString());
                        }
                    } catch (IOException e) {
                        violations.add(path + " (" + e.getMessage() + ")");
                    }
                });
            }
        }
        assertTrue(violations.isEmpty(),
                "Màu phải lấy từ Palette, không hard-code trong: " + violations);
    }

    @Test
    void noHexColorsInThemeConsumersOutsidePalette() throws IOException {
        Path themeDir = UI_ROOT.resolve("theme");
        try (Stream<Path> files = Files.list(themeDir)) {
            files.filter(path -> path.toString().endsWith(".java"))
                    .filter(path -> !path.getFileName().toString().equals("Palette.java"))
                    .forEach(path -> {
                        try {
                            String source = Files.readString(path, StandardCharsets.UTF_8);
                            if (Pattern.compile("0x[0-9A-Fa-f]{6}").matcher(source).find()) {
                                fail(path.getFileName() + " chứa mã màu hex, hãy thêm vào Palette");
                            }
                        } catch (IOException e) {
                            fail(e);
                        }
                    });
        }
    }
}
