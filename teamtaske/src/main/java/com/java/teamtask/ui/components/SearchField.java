package com.java.teamtask.ui.components;

import com.formdev.flatlaf.FlatClientProperties;
import com.java.teamtask.ui.theme.Icons;
import com.java.teamtask.ui.theme.Typography;
import com.java.teamtask.ui.theme.UiScale;

import javax.swing.JTextField;
import java.awt.Dimension;

/** Ô tìm kiếm pill, placeholder + icon phải, focus ring của FlatLaf. */
public final class SearchField extends JTextField {
    public SearchField() {
        setFont(Typography.body());
        setPreferredSize(new Dimension(UiScale.px(400), UiScale.px(48)));
        setMaximumSize(new Dimension(UiScale.px(400), UiScale.px(48)));
        setMinimumSize(new Dimension(UiScale.px(120), UiScale.px(48)));
        putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Tìm kiếm dự án...");
        putClientProperty(FlatClientProperties.TEXT_FIELD_TRAILING_ICON, Icons.muted("search", 22));
        putClientProperty(FlatClientProperties.STYLE, "arc: 999; focusWidth: 3");
        setMargin(new java.awt.Insets(0, 20, 0, 20));
    }

    public void setCompact(boolean compact) {
        int width = UiScale.px(compact ? 220 : 400);
        setPreferredSize(new Dimension(width, UiScale.px(48)));
        setMaximumSize(new Dimension(width, UiScale.px(48)));
        revalidate();
    }
}
