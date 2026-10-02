package hue.captains.singapura.js.homing.workspace.groups.core.models;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * A kind of widget, as a workspace offers it and an arrangement opens it: the
 * name a widget's declaration gives its kind - lower case, a letter first, then
 * letters, digits and hyphens, {@code books-grid}.
 */
public record WidgetKind(String value) {

    private static final Pattern GRAMMAR = Pattern.compile("[a-z][a-z0-9-]*");

    public WidgetKind {
        Objects.requireNonNull(value, "WidgetKind.value");
        if (!GRAMMAR.matcher(value).matches()) {
            throw new IllegalArgumentException("WidgetKind.value '" + value + "' - lower case, a letter first, then letters, digits, hyphen");
        }
    }

    public static WidgetKind of(String value) { return new WidgetKind(value); }

    @Override public String toString() { return value; }
}
