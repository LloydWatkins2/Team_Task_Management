package com.java.teamtask.ui.theme;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;

/** Look & Feel riêng của Taskia. */
public final class TaskiaLaf extends FlatLightLaf {
    public static boolean setup() {
        return FlatLaf.setup(new TaskiaLaf());
    }

    @Override
    public String getName() {
        return "Taskia Light";
    }
}
