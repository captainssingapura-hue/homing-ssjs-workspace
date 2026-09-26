package hue.captains.singapura.js.homing.workspace.log;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Which tab: the id its desk's register gave it, {@code "tab-3"}, kept for the
 * tab's whole life and written into the log under it. It says which tab, never
 * what the tab holds — a tab may become something else.
 *
 * @param value letters, digits, hyphen, underscore
 */
public record TabId(String value) {

    private static final Pattern GRAMMAR = Pattern.compile("[A-Za-z0-9_-]+");

    public TabId {
        Objects.requireNonNull(value, "TabId.value");
        if (!GRAMMAR.matcher(value).matches()) {
            throw new IllegalArgumentException("TabId.value '" + value + "' — letters, digits, hyphen, underscore");
        }
    }

    public static TabId of(String value) { return new TabId(value); }

    @Override public String toString() { return value; }
}
