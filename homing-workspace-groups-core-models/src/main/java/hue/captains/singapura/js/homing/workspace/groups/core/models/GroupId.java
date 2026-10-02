package hue.captains.singapura.js.homing.workspace.groups.core.models;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * A group's identity on its site: unique among the site's groups, and safe as a
 * segment of an address - letters, digits, hyphen and underscore.
 *
 * @param value the id
 */
public record GroupId(String value) {

    private static final Pattern GRAMMAR = Pattern.compile("[A-Za-z0-9_-]+");

    public GroupId {
        Objects.requireNonNull(value, "GroupId.value");
        if (!GRAMMAR.matcher(value).matches()) {
            throw new IllegalArgumentException("GroupId.value '" + value + "' - letters, digits, hyphen, underscore");
        }
    }

    public static GroupId of(String value) { return new GroupId(value); }

    @Override public String toString() { return value; }
}
