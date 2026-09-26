package hue.captains.singapura.js.homing.workspace.log;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Which region of the workspace: a cell of its split grid and the dock in it,
 * named by the grid — {@code "main"}, and the cells it mints as it subdivides.
 *
 * @param value letters, digits, hyphen, underscore
 */
public record RegionId(String value) {

    private static final Pattern GRAMMAR = Pattern.compile("[A-Za-z0-9_-]+");

    public RegionId {
        Objects.requireNonNull(value, "RegionId.value");
        if (!GRAMMAR.matcher(value).matches()) {
            throw new IllegalArgumentException("RegionId.value '" + value + "' — letters, digits, hyphen, underscore");
        }
    }

    public static RegionId of(String value) { return new RegionId(value); }

    @Override public String toString() { return value; }
}
