package hue.captains.singapura.js.homing.workspace.groups.core.models;

import java.util.Objects;

/**
 * A workspace as a group files it: its name - its identity, and its log's kind
 * - and its title, how it is called where it is listed. Nothing of where it is
 * filed: that is the group's.
 *
 * @param name  the workspace's name
 * @param title how it is called: not blank
 */
public record GroupedWorkspace(WorkspaceName name, String title) {

    public GroupedWorkspace {
        Objects.requireNonNull(name, "GroupedWorkspace.name");
        Objects.requireNonNull(title, "GroupedWorkspace.title");
        if (title.isBlank()) throw new IllegalArgumentException("GroupedWorkspace '" + name + "': title - not blank");
    }

    public static GroupedWorkspace of(String name, String title) { return new GroupedWorkspace(WorkspaceName.of(name), title); }
}
