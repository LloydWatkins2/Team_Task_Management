package com.java.teamtask.ui.theme;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.font.TextAttribute;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;

/** Typography Taskia: Plus Jakarta Sans, fallback Segoe UI. */
public final class Typography {
    public enum Weight { REGULAR, MEDIUM, SEMIBOLD, BOLD, EXTRABOLD }

    private static final Map<Weight, Font> FONTS = new EnumMap<>(Weight.class);
    private static boolean loaded;

    private Typography() {
    }

    public static synchronized void load() {
        if (loaded) {
            return;
        }
        loaded = true;
        register(Weight.REGULAR, "/fonts/PlusJakartaSans-Regular.ttf");
        register(Weight.MEDIUM, "/fonts/PlusJakartaSans-Medium.ttf");
        register(Weight.SEMIBOLD, "/fonts/PlusJakartaSans-SemiBold.ttf");
        register(Weight.BOLD, "/fonts/PlusJakartaSans-Bold.ttf");
        register(Weight.EXTRABOLD, "/fonts/PlusJakartaSans-ExtraBold.ttf");
    }

    private static void register(Weight weight, String resource) {
        Font fallback = new Font("Segoe UI", styleOf(weight), 14);
        try (InputStream in = Typography.class.getResourceAsStream(resource)) {
            if (in == null) {
                FONTS.put(weight, fallback);
                return;
            }
            Font font = Font.createFont(Font.TRUETYPE_FONT, in);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
            FONTS.put(weight, font);
        } catch (Exception e) {
            FONTS.put(weight, fallback);
        }
    }

    private static int styleOf(Weight weight) {
        return switch (weight) {
            case REGULAR, MEDIUM, SEMIBOLD -> Font.PLAIN;
            case BOLD, EXTRABOLD -> Font.BOLD;
        };
    }

    public static Font of(Weight weight, float points) {
        load();
        Font base = FONTS.get(weight);
        if (base == null) {
            base = new Font("Segoe UI", styleOf(weight), 14);
        }
        return base.deriveFont(points);
    }

    public static Font body() {
        return of(Weight.REGULAR, UiScale.font(14));
    }

    public static Font label() {
        return of(Weight.BOLD, UiScale.font(14));
    }

    public static Font meta() {
        return of(Weight.SEMIBOLD, UiScale.font(14));
    }

    public static Font placeholder() {
        return of(Weight.MEDIUM, UiScale.font(14));
    }

    public static Font small() {
        return of(Weight.BOLD, UiScale.font(12));
    }

    public static Font button() {
        return of(Weight.BOLD, UiScale.font(14));
    }

    public static Font nav() {
        return of(Weight.SEMIBOLD, UiScale.font(14));
    }

    public static Font cardTitle() {
        return of(Weight.BOLD, UiScale.font(18));
    }

    public static Font h3() {
        return of(Weight.BOLD, UiScale.font(18));
    }

    public static Font h2() {
        return of(Weight.EXTRABOLD, UiScale.font(20));
    }

    public static Font h1() {
        return tracked(of(Weight.EXTRABOLD, UiScale.font(32)), -0.02f);
    }

    public static Font logo() {
        return tracked(of(Weight.EXTRABOLD, UiScale.font(30)), -0.03f);
    }

    private static Font tracked(Font font, float em) {
        return font.deriveFont(Map.of(TextAttribute.TRACKING, em));
    }
}
