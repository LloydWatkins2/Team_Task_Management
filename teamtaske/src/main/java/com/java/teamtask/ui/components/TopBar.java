package com.java.teamtask.ui.components;

import com.java.teamtask.ui.Card;
import com.java.teamtask.ui.theme.Palette;
import com.java.teamtask.ui.theme.Space;
import com.java.teamtask.ui.theme.Typography;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Component;

/** Thanh trên cùng: ô tìm kiếm dự án + khối người dùng. */
public final class TopBar extends Card {
    private static final int SEARCH_DELAY_MS = 180;

    private final SearchField search = new SearchField();
    private final JPanel greeting = new JPanel();
    private final JLabel greetingLabel = new JLabel("Xin chào");
    private final JLabel nameLabel = new JLabel();
    private final JPanel avatarHolder = new JPanel(new BorderLayout());
    private final Timer searchDebounce = new Timer(SEARCH_DELAY_MS, e -> notifySearch());
    private Runnable searchListener = () -> {};

    public TopBar(String userName) {
        super(new BorderLayout(Space.MD, 0));
        setBorder(new EmptyBorder(Space.MD, Space.XXL, Space.MD, Space.XXL));
        setRadius(com.java.teamtask.ui.theme.Radius.MD);

        searchDebounce.setRepeats(false);
        search.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                restartSearchDebounce();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                restartSearchDebounce();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                restartSearchDebounce();
            }
        });
        search.addActionListener(e -> flushSearch());

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

        add(search, BorderLayout.WEST);
        add(user, BorderLayout.EAST);
    }

    private void restartSearchDebounce() {
        searchDebounce.restart();
    }

    /** Gọi listener ngay lập tức, dùng khi người dùng nhấn Enter. */
    public void flushSearch() {
        searchDebounce.stop();
        notifySearch();
    }

    private void notifySearch() {
        searchListener.run();
    }

    /** Đăng ký nhận thay đổi từ khoá; callback đọc giá trị qua {@link #getSearchQuery()}. */
    public void setSearchListener(Runnable listener) {
        searchListener = listener == null ? () -> {} : listener;
    }

    public String getSearchQuery() {
        return search.getText() == null ? "" : search.getText().trim();
    }

    /** Xoá từ khoá và thông báo ngay cho listener. */
    public void clearSearch() {
        search.setText("");
        searchDebounce.stop();
        notifySearch();
    }

    public SearchField getSearchField() {
        return search;
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
