package com.java.teamtask.ui.theme;

import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.Icon;
import java.awt.Color;

/** Nạp icon SVG từ classpath và tô màu theo ngữ cảnh. */
public final class Icons {
    private Icons() {
    }

    public static Icon icon(String name, int size, Color color) {
        FlatSVGIcon svg = new FlatSVGIcon("icons/" + name + ".svg", size, size);
        if (color != null) {
            svg.setColorFilter(new FlatSVGIcon.ColorFilter(c -> color));
        }
        return svg;
    }

    public static Icon primary(String name, int size) {
        return icon(name, size, Palette.PRIMARY);
    }

    public static Icon dark(String name, int size) {
        return icon(name, size, Palette.TEXT);
    }

    public static Icon muted(String name, int size) {
        return icon(name, size, Palette.TEXT_MUTED);
    }

    public static Icon white(String name, int size) {
        return icon(name, size, Color.WHITE);
    }

    public static Icon danger(String name, int size) {
        return icon(name, size, Palette.DANGER);
    }
}
