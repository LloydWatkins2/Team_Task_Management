package com.java.teamtask.ui.components;

import com.java.teamtask.ui.Card;
import com.java.teamtask.ui.theme.Palette;
import com.java.teamtask.ui.theme.Space;
import com.java.teamtask.ui.theme.Typography;
import com.java.teamtask.ui.theme.UiScale;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

/** Thanh trên cùng: ô tìm kiếm + chuông thông báo + khối người dùng. */
public final class TopBar extends Card {
    private final SearchField search = new SearchField();
    private final IconButton bell = new IconButton("bell", "Thông báo");
    private final JPanel greeting = new JPanel();
    private final JLabel greetingLabel = new JLabel("Xin chào");
    private final JLabel nameLabel = new JLabel();
    private final JPanel avatarHolder = new JPanel(new BorderLayout());

    public TopBar(String userName) {
        super(new BorderLayout(Space.MD, 0));
        setBorder(new EmptyBorder(Space.MD, Space.XXL, Space.MD, Space.XXL));
        setRadius(com.java.teamtask.ui.theme.Radius.MD);

        bell.setDot(true);

        JPanel actions = new JPanel();
        actions.setOpaque(false);
        actions.setLayout(new BoxLayout(actions, BoxLayout.X_AXIS));
        actions.add(bell);
        actions.add(Box.createHorizontalStrut(Space.LG));
        actions.add(divider());
        actions.add(Box.createHorizontalStrut(Space.LG));

        greeting.setOpaque(false);
        greeting.setLayout(new BoxLayout(greeting, BoxLayout.Y_AXIS));
        greetingLabel.setFont(Typography.small());
        greetingLabel.setForeground(Palette.TEXT_MUTED_STRONG);
        greetingLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        nameLabel.setText(userName);
        nameLabel.setFont(Typography.label());
        nameLabel.setForeground(Palette.TEXT);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        greeting.add(greetingLabel);
        greeting.add(nameLabel);

        JPanel user = new JPanel(new BorderLayout(Space.SM, 0));
        user.setOpaque(false);
        avatarHolder.setOpaque(false);
        avatarHolder.add(new Avatar(userName), BorderLayout.CENTER);
        user.add(avatarHolder, BorderLayout.WEST);
        user.add(greeting, BorderLayout.CENTER);
        actions.add(user);

        add(search, BorderLayout.WEST);
        add(actions, BorderLayout.EAST);
    }

    private static JPanel divider() {
        JPanel divider = new JPanel();
        divider.setOpaque(true);
        divider.setBackground(Palette.BORDER);
        divider.setPreferredSize(new Dimension(1, UiScale.px(32)));
        divider.setMaximumSize(new Dimension(1, UiScale.px(32)));
        divider.setMinimumSize(new Dimension(1, UiScale.px(32)));
        return divider;
    }

    public SearchField getSearchField() {
        return search;
    }

    public IconButton getBellButton() {
        return bell;
    }

    public void setUserName(String userName) {
        String display = userName == null || userName.isBlank() ? "Người dùng" : userName;
        nameLabel.setText(display);
        avatarHolder.removeAll();
        avatarHolder.add(new Avatar(display), BorderLayout.CENTER);
        avatarHolder.revalidate();
        avatarHolder.repaint();
    }

    public void setCompact(boolean compact) {
        search.setCompact(compact);
        greeting.setVisible(!compact);
    }
}
