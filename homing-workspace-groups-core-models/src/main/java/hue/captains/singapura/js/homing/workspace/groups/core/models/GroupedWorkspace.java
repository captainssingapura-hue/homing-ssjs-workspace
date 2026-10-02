package hue.captains.singapura.js.homing.workspace.groups.core.models;

import java.util.Objects;

/**
 * A kind of workspace as a group files it: its kind - its identity, and its
 * log's kind - and its title, how it is called where it is listed. Nothing of
 * where it is filed: that is the group's.
 *
 * @param kind  the kind
 * @param title how it is called: not blank
 */
public record GroupedWorkspace(WorkspaceKind kind, String title) {

    public GroupedWorkspace {
        Objects.requireNonNull(kind, "GroupedWorkspace.kind");
        Objects.requireNonNull(title, "GroupedWorkspace.title");
        if (title.isBlank()) throw new IllegalArgumentException("GroupedWorkspace '" + kind + "': title - not blank");
    }

    public static GroupedWorkspace of(String kind, String title) { return new GroupedWorkspace(WorkspaceKind.of(kind), title); }
}
