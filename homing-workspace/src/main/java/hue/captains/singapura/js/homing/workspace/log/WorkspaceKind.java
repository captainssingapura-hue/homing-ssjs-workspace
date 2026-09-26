package hue.captains.singapura.js.homing.workspace.log;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Which kind of workspace a log belongs to: the kind the page is addressed by
 * — {@code "demo"}, {@code "notes"} — each kind keeping a log of its own.
 *
 * @param value letters, digits, hyphen, underscore
 */
public record WorkspaceKind(String value) {

    private static final Pattern GRAMMAR = Pattern.compile("[A-Za-z0-9_-]+");

    public WorkspaceKind {
        Objects.requireNonNull(value, "WorkspaceKind.value");
        if (!GRAMMAR.matcher(value).matches()) {
            throw new IllegalArgumentException("WorkspaceKind.value '" + value + "' — letters, digits, hyphen, underscore");
        }
    }

    public static WorkspaceKind of(String value) { return new WorkspaceKind(value); }

    @Override public String toString() { return value; }
}
