package hue.captains.singapura.js.homing.workspace.groups.core.models;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * An arrangement's name for one widget it opens: what its placement places it
 * by. The arrangement's own - never a widget's id, which the workspace gives
 * when the widget is made. Letters, digits, hyphen and underscore.
 */
public record WidgetRef(String value) {

    private static final Pattern GRAMMAR = Pattern.compile("[A-Za-z0-9_-]+");

    public WidgetRef {
        Objects.requireNonNull(value, "WidgetRef.value");
        if (!GRAMMAR.matcher(value).matches()) {
            throw new IllegalArgumentException("WidgetRef.value '" + value + "' - letters, digits, hyphen, underscore");
        }
    }

    public static WidgetRef of(String value) { return new WidgetRef(value); }

    @Override public String toString() { return value; }
}
