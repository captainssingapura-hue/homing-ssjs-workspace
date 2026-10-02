package hue.captains.singapura.js.homing.workspace.bench;

import hue.captains.singapura.js.homing.workspace.groups.core.models.GroupedWorkspace;
import hue.captains.singapura.js.homing.workspace.groups.core.models.WorkspaceGroup;
import hue.captains.singapura.js.homing.workspace.groups.core.models.WorkspaceGroups;
import hue.captains.singapura.js.homing.workspace.groups.core.models.WorkspaceKind;

/**
 * The bench's workspace groups, as its page's directory has them: its own
 * workspace of one pane, the one whose workspaces this browser keeps here; and
 * the demo's, filed beside it so a switcher has more than one kind to show -
 * kept by the demo's origin, so none of it is kept here.
 */
public final class BenchGroups {

    private BenchGroups() {}

    public static final WorkspaceGroups GROUPS = WorkspaceGroups.of(
            WorkspaceGroup.of("bench", "Widget bench")
                    .section("On this bench", GroupedWorkspace.of(BenchWorkspace.INSTANCE.name(), "One pane"))
                    .section("Elsewhere", GroupedWorkspace.of("demo", "Demo: books and monitors"))
                    .defaultTo(WorkspaceKind.of(BenchWorkspace.INSTANCE.name()))
                    .build());
}
