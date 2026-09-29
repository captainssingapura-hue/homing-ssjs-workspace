package hue.captains.singapura.js.homing.workspace.groups.core.models;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A site's workspace groups. No two share an id, and a workspace is filed in
 * one of them at most: on one site, a name is one log and has one path. Across
 * sites nothing is shared - another site may file the same workspace in a group
 * of its own, under another heading.
 *
 * @param groups the site's groups, in order
 */
public record WorkspaceGroups(List<WorkspaceGroup> groups) {

    public WorkspaceGroups {
        Objects.requireNonNull(groups, "WorkspaceGroups.groups");
        groups = List.copyOf(groups);
        var ids = new LinkedHashMap<GroupId, WorkspaceGroup>();
        var filed = new LinkedHashMap<WorkspaceName, GroupId>();
        for (WorkspaceGroup g : groups) {
            if (ids.putIfAbsent(g.id(), g) != null) throw new IllegalArgumentException("two groups are '" + g.id() + "' on one site");
            for (GroupedWorkspace w : g.workspaces()) {
                GroupId in = filed.putIfAbsent(w.name(), g.id());
                if (in != null) {
                    throw new IllegalArgumentException("the workspace '" + w.name() + "' is filed in the groups '" + in + "' and '" + g.id()
                            + "' - on one site, a workspace is filed once");
                }
            }
        }
    }

    public static WorkspaceGroups of(WorkspaceGroup... groups) { return new WorkspaceGroups(List.of(groups)); }

    /** The group of that id, when the site has it. */
    public Optional<WorkspaceGroup> group(GroupId id) {
        for (WorkspaceGroup g : groups) if (g.id().equals(id)) return Optional.of(g);
        return Optional.empty();
    }

    /** The group a workspace is filed in, when the site files it. */
    public Optional<WorkspaceGroup> groupOf(WorkspaceName name) {
        for (WorkspaceGroup g : groups) if (g.files(name)) return Optional.of(g);
        return Optional.empty();
    }
}
