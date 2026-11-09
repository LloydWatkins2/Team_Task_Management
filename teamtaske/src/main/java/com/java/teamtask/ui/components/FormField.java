package com.java.teamtask.ui.components;

import com.formdev.flatlaf.FlatClientProperties;
import com.java.teamtask.ui.theme.Icons;
import com.java.teamtask.ui.theme.Painters;
import com.java.teamtask.ui.theme.Palette;
import com.java.teamtask.ui.theme.Space;
import com.java.teamtask.ui.theme.Typography;
import com.java.teamtask.ui.theme.UiScale;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/** Field chuẩn: label trên, control pill 52px, lỗi inline. */
public final class FormField extends JPanel {
    private final JLabel label = new JLabel();
    private final JLabel error = new JLabel();
    private final FieldControl control;
    private final JComponent input;
    private boolean hasError;

    public static FormField text(String labelText, String placeholder, String iconName) {
        JTextField field = new JTextField();
        field.setFont(Typography.meta());
        field.setForeground(Palette.TEXT);
        field.setOpaque(false);
        field.setBorder(BorderFactory.createEmptyBorder());
        field.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        return new FormField(labelText, iconName, field, 52);
    }

    public static FormField textArea(String labelText, String placeholder, int height) {
        JTextArea area = new JTextArea();
        area.setFont(Typography.meta());
        area.setForeground(Palette.TEXT);
        area.setOpaque(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(new EmptyBorder(14, 0, 14, 0));
        area.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);

        JScrollPane scroll = new JScrollPane(area);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        return new FormField(labelText, null, scroll, height);
    }

    public static FormField password(String labelText, String placeholder, String iconName) {
        JPasswordField field = new JPasswordField();
        field.setFont(Typography.meta());
        field.setForeground(Palette.TEXT);
        field.setOpaque(false);
        field.setBorder(BorderFactory.createEmptyBorder());
        field.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        return new FormField(labelText, iconName, field, 52);
    }

    public static FormField combo(String labelText, String[] options, String iconName) {
        JComboBox<String> combo = new JComboBox<>(options);
        combo.setFont(Typography.meta());
        combo.setForeground(Palette.TEXT);
        combo.setBackground(Palette.CARD);
        combo.setOpaque(false);
        combo.setBorder(BorderFactory.createEmptyBorder());
        return new FormField(labelText, iconName, combo, 52);
    }

    private FormField(String labelText, String iconName, JComponent input, int controlHeight) {
        this.input = input;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);
        setAlignmentX(LEFT_ALIGNMENT);

        label.setText(labelText);
        label.setFont(Typography.label());
        label.setForeground(Palette.TEXT);
        label.setAlignmentX(LEFT_ALIGNMENT);
        label.setBorder(new EmptyBorder(0, 0, UiScale.px(10), 0));
        label.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, label.getPreferredSize().height));

        control = new FieldControl(iconName);
        control.setAlignmentX(LEFT_ALIGNMENT);
        control.setPreferredSize(new java.awt.Dimension(0, UiScale.px(controlHeight)));
        control.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, UiScale.px(controlHeight)));
        control.add(input, BorderLayout.CENTER);

        error.setFont(Typography.small());
        error.setForeground(Palette.DANGER);
        error.setAlignmentX(LEFT_ALIGNMENT);
        error.setBorder(new EmptyBorder(Space.XS, Space.SM, 0, 0));
        error.setVisible(false);

        JComponent focusTarget = input;
        if (input instanceof JScrollPane scroll
                && scroll.getViewport().getView() instanceof JComponent view) {
            focusTarget = view;
        }
        focusTarget.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                control.setFocused(true);
            }

            @Override
            public void focusLost(FocusEvent e) {
                control.setFocused(false);
            }
        });

        add(label);
        add(control);
        add(error);
    }

    public String getValue() {
        JComponent target = input;
        if (input instanceof JScrollPane scroll && scroll.getViewport().getView() instanceof JComponent view) {
            target = view;
        }
        if (target instanceof JPasswordField passwordField) {
            return new String(passwordField.getPassword()).trim();
        }
        if (target instanceof JTextArea textArea) {
            return textArea.getText().trim();
        }
        if (target instanceof JTextField textField) {
            return textField.getText().trim();
        }
        if (target instanceof JComboBox<?> combo) {
            Object value = combo.getSelectedItem();
            return value == null ? "" : value.toString();
        }
        return "";
    }

    public void setValue(String value) {
        JComponent target = input;
        if (input instanceof JScrollPane scroll && scroll.getViewport().getView() instanceof JComponent view) {
            target = view;
        }
        if (target instanceof JPasswordField passwordField) {
            passwordField.setText(value);
        } else if (target instanceof JTextArea textArea) {
            textArea.setText(value);
        } else if (target instanceof JTextField textField) {
            textField.setText(value);
        }
    }

    public void setSelected(String value) {
        JComponent target = input;
        if (input instanceof JScrollPane scroll && scroll.getViewport().getView() instanceof JComponent view) {
            target = view;
        }
        if (target instanceof JComboBox<?> combo) {
            combo.setSelectedItem(value);
        }
    }

    public void setError(String message) {
        boolean hasError = message != null && !message.isBlank();
        this.hasError = hasError;
        control.setError(hasError);
        error.setText(hasError ? message : "");
        error.setVisible(hasError);
        revalidate();
        repaint();
    }

    public boolean hasError() {
        return hasError;
    }

    /** Nội dung lỗi inline hiện tại; rỗng khi không có lỗi. */
    public String getErrorText() {
        return error.getText() == null ? "" : error.getText();
    }

    public void clearError() {
        setError(null);
    }

    public void focusInput() {
        JComponent target = input;
        if (input instanceof JScrollPane scroll && scroll.getViewport().getView() instanceof JComponent view) {
            target = view;
        }
        target.requestFocusInWindow();
    }

    private static final class FieldControl extends JPanel {
        private boolean focused;
        private boolean error;

        FieldControl(String iconName) {
            super(new BorderLayout(Space.SM, 0));
            setOpaque(false);
            setBorder(new EmptyBorder(0, Space.LG, 0, Space.LG));
            if (iconName != null && !iconName.isBlank()) {
                add(new JLabel(Icons.muted(iconName, 22)), BorderLayout.WEST);
            }
        }

        void setFocused(boolean focused) {
            this.focused = focused;
            repaint();
        }

        void setError(boolean error) {
            this.error = error;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            Painters.antialias(g2);
            int w = getWidth();
            int h = getHeight();
            int radius = h / 2;
            Painters.fill(g2, 0, 0, w, h, radius, Palette.CARD);

            if (error) {
                Painters.border(g2, 0, 0, w, h, radius, Palette.DANGER, 2f);
            } else {
                Painters.border(g2, 0, 0, w, h, radius, focused ? Palette.PRIMARY : Palette.BORDER, 1f);
                if (focused) {
                    g2.setColor(Palette.FOCUS_RING);
                    g2.setStroke(new BasicStroke(3f));
                    g2.draw(Painters.roundRect(1, 1, w - 3, h - 3, radius));
                }
            }
            g2.dispose();
        }
    }
}
