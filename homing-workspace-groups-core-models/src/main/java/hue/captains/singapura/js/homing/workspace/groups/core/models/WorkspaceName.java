package hue.captains.singapura.js.homing.workspace.groups.core.models;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * A workspace's name: its identity, and the kind its log is kept under - so the
 * grammar is the log's, letters, digits, hyphen and underscore. It is also the
 * last segment of the workspace's path in a group, which the grammar keeps safe
 * in an address. Re-filing a workspace changes its path, never its name, so it
 * keeps its log.
 *
 * @param value the name
 */
public record WorkspaceName(String value) {

    private static final Pattern GRAMMAR = Pattern.compile("[A-Za-z0-9_-]+");

    public WorkspaceName {
        Objects.requireNonNull(value, "WorkspaceName.value");
        if (!GRAMMAR.matcher(value).matches()) {
            throw new IllegalArgumentException("WorkspaceName.value '" + value + "' - letters, digits, hyphen, underscore");
        }
    }

    public static WorkspaceName of(String value) { return new WorkspaceName(value); }

    @Override public String toString() { return value; }
}
