package hue.captains.singapura.js.homing.workspace.groups.core.models;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * A kind of workspace: its identity, and the kind its log is kept under - so
 * the grammar is the log's, letters, digits, hyphen and underscore. A kind has
 * instances - workspaces of it, each a log of its own, each named by a user;
 * this is the kind, never an instance's name. It is also the last segment of
 * the kind's path in a group, which the grammar keeps safe in an address.
 * Re-filing a kind changes its path, never its kind, so its instances stay.
 *
 * @param value the kind
 */
public record WorkspaceKind(String value) {

    private static final Pattern GRAMMAR = Pattern.compile("[A-Za-z0-9_-]+");

    public WorkspaceKind {
        Objects.requireNonNull(value, "WorkspaceKind.value");
        if (!GRAMMAR.matcher(value).matches()) {
            throw new IllegalArgumentException("WorkspaceKind.value '" + value + "' - letters, digits, hyphen, underscore");
        }
    }

    public static WorkspaceKind of(String value) { return new WorkspaceKind(value); }

    @Override public String toString() { return value; }
}
