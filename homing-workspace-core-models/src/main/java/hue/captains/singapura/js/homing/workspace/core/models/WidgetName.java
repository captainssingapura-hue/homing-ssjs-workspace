package hue.captains.singapura.js.homing.workspace.core.models;

import java.util.Objects;

/**
 * What a user called a widget: a name given, which wins over the title derived
 * from the widget's identity. A widget opens under its derived title, and keeps
 * a name only once one is given - by a rename, a request the core executes and
 * the roster keeps. Free-form, but never blank: taking a name back is the name
 * absent, not a blank one.
 *
 * @param value the name, not blank
 */
public record WidgetName(String value) {

    public WidgetName {
        Objects.requireNonNull(value, "WidgetName.value");
        if (value.isBlank()) throw new IllegalArgumentException("WidgetName.value — not blank: a name taken back is none");
    }

    public static WidgetName of(String value) { return new WidgetName(value); }

    @Override public String toString() { return value; }
}
