package hue.captains.singapura.js.homing.workspace.demo;

import hue.captains.singapura.js.homing.workspace.groups.core.models.GroupedWorkspace;
import hue.captains.singapura.js.homing.workspace.groups.core.models.WorkspaceGroup;
import hue.captains.singapura.js.homing.workspace.groups.core.models.WorkspaceGroups;
import hue.captains.singapura.js.homing.workspace.groups.core.models.WorkspaceKind;
import hue.captains.singapura.js.homing.workspace.site.GroupedWorkspaces;

/**
 * The demo's workspaces, grouped: the three widget sets together, and each set on
 * its own - filed here, in one group, the page {@code /demo}; each where the group
 * files it, {@code #ws/<section>/<kind>}, the three together its default. A
 * workspace does not know where it is filed: the group decides.
 */
public final class DemoGroups {

    private DemoGroups() {}

    public static final WorkspaceGroups GROUPS = WorkspaceGroups.of(
            WorkspaceGroup.of("demo", "Workspace demo")
                    .section("Together", GroupedWorkspace.of(DemoWorkspace.INSTANCE.name(), "Books and monitors"))
                    .section("One set each", GroupedWorkspace.of(BooksWorkspace.INSTANCE.name(), "Books"),
                                             GroupedWorkspace.of(MonitorsWorkspace.INSTANCE.name(), "Monitors"))
                    .defaultTo(WorkspaceKind.of(DemoWorkspace.INSTANCE.name()))
                    .build());

    /** What the site serves: each workspace declared, each filed, each arranged the first time. */
    public static final GroupedWorkspaces SITE = GroupedWorkspaces.of(GROUPS,
            DemoWorkspace.INSTANCE, BooksWorkspace.INSTANCE, MonitorsWorkspace.INSTANCE)
            .arranged(DemoArrangements.TOGETHER, DemoArrangements.BOOKS, DemoArrangements.MONITORING);
}
