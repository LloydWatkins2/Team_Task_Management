package com.java.teamtask.ui.components;

import com.java.teamtask.ui.theme.Icons;
import com.java.teamtask.ui.theme.Painters;
import com.java.teamtask.ui.theme.Palette;
import com.java.teamtask.ui.theme.Space;
import com.java.teamtask.ui.theme.Typography;
import com.java.teamtask.ui.theme.UiScale;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** Card chọn được (dự án) với trạng thái selected, hover và dấu ✓. */
public final class SelectableCard extends JPanel {
    private final String title;
    private final Icon checkIcon = Icons.white("check", 14);
    private final Runnable onSelected;
    private boolean selected;
    private boolean hover;
    private SelectableGroup group;

    public SelectableCard(String title, String subtitle, Runnable onSelected) {
        this.title = title;
        this.onSelected = onSelected == null ? () -> {
        } : onSelected;
        setLayout(new BorderLayout());
        setOpaque(false);
        setFocusable(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(new EmptyBorder(Space.MD, Space.LG, Space.MD, UiScale.px(52)));
        getAccessibleContext().setAccessibleName(title);

        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(Typography.of(Typography.Weight.BOLD, UiScale.font(17)));
        titleLabel.setForeground(Palette.TEXT);

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(Typography.body());
        subtitleLabel.setForeground(Palette.TEXT_MUTED_STRONG);

        texts.add(titleLabel);
        texts.add(subtitleLabel);
        add(texts, BorderLayout.CENTER);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                choose();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                hover = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hover = false;
                repaint();
            }
        });

        getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "choose");
        getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "choose");
        getActionMap().put("choose", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                choose();
            }
        });
    }

    public String getTitleText() {
        return title;
    }

    public boolean isSelected() {
        return selected;
    }

    void attach(SelectableGroup group) {
        this.group = group;
    }

    void setSelectedInternal(boolean selected) {
        this.selected = selected;
        repaint();
    }

    private void choose() {
        if (group != null) {
            group.select(this);
        } else {
            setSelectedInternal(true);
        }
        onSelected.run();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Painters.antialias(g2);
        int w = getWidth();
        int h = getHeight();
        int radius = com.java.teamtask.ui.theme.Radius.SM;

        Painters.fill(g2, 0, 0, w, h, radius,
                selected ? Palette.CARD : hover ? Palette.SELECT_HOVER : Palette.CONTENT);
        Painters.border(g2, 0, 0, w, h, radius,
                selected ? Palette.PRIMARY_DARK : Palette.BORDER, 2f);

        if (selected) {
            g2.setColor(Palette.FOCUS_RING);
            g2.setStroke(new java.awt.BasicStroke(3f));
            g2.draw(Painters.roundRect(2, 2, w - 5, h - 5, radius));
        }

        if (selected) {
            int d = UiScale.px(22);
            int x = w - Space.LG - d;
            int y = (h - d) / 2;
            g2.setColor(Palette.SUCCESS);
            g2.fillOval(x, y, d, d);
            checkIcon.paintIcon(this, g2, x + (d - checkIcon.getIconWidth()) / 2,
                    y + (d - checkIcon.getIconHeight()) / 2);
        }
        g2.dispose();
        super.paintComponent(g);

        if (isFocusOwner()) {
            Graphics2D focus = (Graphics2D) g.create();
            Painters.antialias(focus);
            Painters.focusRing(focus, w, h, radius);
            focus.dispose();
        }
    }
}
