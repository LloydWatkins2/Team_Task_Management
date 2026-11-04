package com.java.teamtask.ui.theme;

import com.formdev.flatlaf.FlatLaf;

import javax.swing.UIManager;
import java.awt.Color;
import java.util.Map;

/** Cài đặt toàn bộ theme một lần khi khởi động. */
public final class Theme {
    private Theme() {
    }

    public static void install() {
        Typography.load();

        FlatLaf.setGlobalExtraDefaults(Map.ofEntries(
                Map.entry("Component.arc", String.valueOf(Radius.SM)),
                Map.entry("Button.arc", String.valueOf(Radius.PILL)),
                Map.entry("TextComponent.arc", String.valueOf(Radius.PILL)),
                Map.entry("Component.focusWidth", "3"),
                Map.entry("Component.innerFocusWidth", "1"),
                Map.entry("Component.focusColor", "#7A5CFA"),
                Map.entry("Component.borderColor", "#E7E9F3"),
                Map.entry("ScrollBar.width", "10"),
                Map.entry("ScrollBar.thumbArc", String.valueOf(Radius.PILL)),
                Map.entry("ScrollBar.track", "rgba(0,0,0,0)"),
                Map.entry("ScrollBar.thumb", "#D6D9E9"),
                Map.entry("ScrollBar.hoverThumbColor", "#BFC4D9"),
                Map.entry("ScrollBar.pressedThumbColor", "#AEB4CC")
        ));

        TaskiaLaf.setup();
        applyDefaults();
        FlatLaf.updateUI();
    }

    private static void applyDefaults() {
        UIManager.put("defaultFont", Typography.body());
        UIManager.put("Label.foreground", Palette.TEXT);
        UIManager.put("Label.disabledForeground", Palette.TEXT_MUTED);
        UIManager.put("Button.foreground", Palette.TEXT);
        UIManager.put("TextField.foreground", Palette.TEXT);
        UIManager.put("TextField.placeholderForeground", Palette.TEXT_PLACEHOLDER);
        UIManager.put("PasswordField.foreground", Palette.TEXT);
        UIManager.put("ComboBox.foreground", Palette.TEXT);
        UIManager.put("ComboBox.background", Palette.CARD);
        UIManager.put("Component.focusColor", Palette.FOCUS_RING);
        UIManager.put("ToolTip.background", Palette.TEXT);
        UIManager.put("ToolTip.foreground", Color.WHITE);
        UIManager.put("ToolTip.font", Typography.small());
        UIManager.put("TextField.selectionBackground", Palette.PRIMARY_SOFT);
        UIManager.put("TextField.selectionForeground", Palette.TEXT);
        UIManager.put("Table.rowHeight", UiScale.px(48));
        UIManager.put("Table.selectionBackground", Palette.PRIMARY_SOFT);
        UIManager.put("Table.selectionForeground", Palette.TEXT);
        UIManager.put("Table.gridColor", Palette.BORDER);
    }
}
