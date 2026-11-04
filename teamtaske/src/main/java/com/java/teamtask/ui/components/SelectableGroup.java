package com.java.teamtask.ui.components;

import java.util.ArrayList;
import java.util.List;

/** Quản lý chọn một trong nhiều SelectableCard. */
public final class SelectableGroup {
    private final List<SelectableCard> cards = new ArrayList<>();
    private SelectableCard selected;

    public void add(SelectableCard card) {
        cards.add(card);
        card.attach(this);
        if (selected == null) {
            selected = card;
            card.setSelectedInternal(true);
        }
    }

    void select(SelectableCard card) {
        selected = card;
        for (SelectableCard item : cards) {
            item.setSelectedInternal(item == card);
        }
    }

    /** Chọn một card theo lập trình (không phát sự kiện của card). */
    public void setSelected(SelectableCard card) {
        if (cards.contains(card)) {
            select(card);
        }
    }

    public SelectableCard getSelected() {
        return selected;
    }
}
