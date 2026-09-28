package hue.captains.singapura.js.homing.workspace.core;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * A widget's id in a workspace: what it is, in a word - its kind, a concise form
 * of its params when it has any, and a sequence - {@code books-grid-1},
 * {@code books-grid_title-rating-2}. Unique within its workspace, and only
 * there; never reused in it. The id is the widget's, not a tab's: a placement
 * that shows the widget refers to it by this (RFC 0066 E3, the workspace
 * detour: the widget apart from its tab). Made by {@link WidgetIds}.
 *
 * @param value letters, digits, hyphen, underscore - the grammar of the log's ids
 */
public record WidgetId(String value) {

    /** The grammar: the log's ids'. */
    public static final Pattern GRAMMAR = Pattern.compile("[A-Za-z0-9_-]+");

    public WidgetId {
        Objects.requireNonNull(value, "WidgetId.value");
        if (!GRAMMAR.matcher(value).matches()) throw new IllegalArgumentException("WidgetId.value '" + value + "' - letters, digits, hyphen, underscore");
    }

    @Override public String toString() { return value; }
}
