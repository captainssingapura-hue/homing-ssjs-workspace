package hue.captains.singapura.js.homing.workspace.log;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Which float: a frame on the desk around a host of its own, named by the desk
 * — {@code "float-1"} and on, never a name another float of that desk had.
 *
 * @param value letters, digits, hyphen, underscore
 */
public record FloatId(String value) {

    private static final Pattern GRAMMAR = Pattern.compile("[A-Za-z0-9_-]+");

    public FloatId {
        Objects.requireNonNull(value, "FloatId.value");
        if (!GRAMMAR.matcher(value).matches()) {
            throw new IllegalArgumentException("FloatId.value '" + value + "' — letters, digits, hyphen, underscore");
        }
    }

    public static FloatId of(String value) { return new FloatId(value); }

    @Override public String toString() { return value; }
}
