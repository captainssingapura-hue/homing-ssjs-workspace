package hue.captains.singapura.js.homing.workspace.demo;

import hue.captains.singapura.js.homing.workspace.groups.core.models.GroupedWorkspace;
import hue.captains.singapura.js.homing.workspace.groups.core.models.WorkspaceGroup;
import hue.captains.singapura.js.homing.workspace.groups.core.models.WorkspaceGroups;
import hue.captains.singapura.js.homing.workspace.groups.core.models.WorkspaceKind;

/**
 * The demo's workspace groups, as its page's directory has them: the demo
 * workspace itself, the one whose workspaces this browser keeps here; and the
 * bench's, filed beside it so a switcher has more than one kind to show - kept
 * by the bench's origin, so none of it is kept here.
 */
public final class DemoGroups {

    private DemoGroups() {}

    public static final WorkspaceGroups GROUPS = WorkspaceGroups.of(
            WorkspaceGroup.of("demo", "Workspace demo")
                    .section("This demo", GroupedWorkspace.of(DemoWorkspace.INSTANCE.name(), "Books, monitors and the switcher"))
                    .section("Elsewhere", GroupedWorkspace.of("bench-one-pane", "Bench: one pane"))
                    .defaultTo(WorkspaceKind.of(DemoWorkspace.INSTANCE.name()))
                    .build());
}
