package com.java.teamtask.ui;

import com.java.teamtask.ui.theme.Radius;
import com.java.teamtask.ui.theme.Space;
import com.java.teamtask.ui.theme.TaskiaLaf;
import com.java.teamtask.ui.theme.Theme;
import com.java.teamtask.ui.theme.Typography;
import com.java.teamtask.ui.theme.UiScale;
import com.formdev.flatlaf.FlatLightLaf;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.swing.UIManager;
import java.awt.Font;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Kiểm tra theme, font và các thang token được cài đặt đúng. */
class ThemeSmokeTest {
    @BeforeAll
    static void installTheme() {
        Theme.install();
    }

    @Test
    void installsTaskiaLightLaf() {
        assertInstanceOf(TaskiaLaf.class, UIManager.getLookAndFeel());
        assertInstanceOf(FlatLightLaf.class, UIManager.getLookAndFeel());
    }

    @Test
    void defaultFontUsesBundledFamily() {
        Font font = UIManager.getFont("defaultFont");
        assertNotNull(font, "defaultFont phải được cài đặt");
        assertTrue(font.getSize() > 0);
        assertEquals(Typography.body().getFamily(), font.getFamily());
    }

    @Test
    void scaleHelpersKeepLogicalPixels() {
        assertEquals(12, UiScale.px(12));
        assertEquals(18, UiScale.px(18));
        assertEquals(13.5f, UiScale.font(16), 0.001f);
        assertEquals(12.0f, Typography.body().getSize2D(), 0.001f);
        assertEquals(10.5f, Typography.small().getSize2D(), 0.001f);
    }

    @Test
    void spacingAndRadiusAreAscending() {
        int[] spaces = {Space.XXS, Space.XS, Space.SM, Space.MD, Space.LG,
                Space.XL, Space.XXL, Space.X3L, Space.X4L, Space.X5L, Space.X6L, Space.X7L};
        for (int i = 1; i < spaces.length; i++) {
            assertTrue(spaces[i] > spaces[i - 1], "Space phải tăng dần");
        }
        int[] radii = {Radius.XS, Radius.SM, Radius.MD, Radius.LG, Radius.XL};
        for (int i = 1; i < radii.length; i++) {
            assertTrue(radii[i] > radii[i - 1], "Radius phải tăng dần");
        }
        assertEquals(48, Radius.XL);
        assertEquals(32, Radius.LG);
    }
}
