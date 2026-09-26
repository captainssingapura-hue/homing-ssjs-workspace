package hue.captains.singapura.js.homing.workspace.log;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Which kind of workspace a log belongs to: the kind its spec registers under
 * and the page is addressed by — {@code "demo"}, {@code "focus-lab"}.
 *
 * @param value letters, digits, hyphen, underscore
 */
public record WorkspaceSpecKind(String value) {

    private static final Pattern GRAMMAR = Pattern.compile("[A-Za-z0-9_-]+");

    public WorkspaceSpecKind {
        Objects.requireNonNull(value, "WorkspaceSpecKind.value");
        if (!GRAMMAR.matcher(value).matches()) {
            throw new IllegalArgumentException("WorkspaceSpecKind.value '" + value + "' — letters, digits, hyphen, underscore");
        }
    }

    public static WorkspaceSpecKind of(String value) { return new WorkspaceSpecKind(value); }

    @Override public String toString() { return value; }
}
