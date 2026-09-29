package hue.captains.singapura.js.homing.workspace.site;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;
import hue.captains.singapura.js.homing.server.ServerCrate;
import hue.captains.singapura.js.homing.workspace.groups.WorkspaceGroupsCrate;
import hue.captains.singapura.js.homing.workspace.log.store.WorkspaceLogCrate;
import hue.captains.singapura.js.homing.workspace.shell.WorkspaceShellCrate;

import java.util.List;

/**
 * A site of grouped workspaces, as its pages have it: the grouped workspace
 * page - the shell's, the workspace chosen by its route, the directory provided
 * from the groups, the page opening what it is asked to. The routes are Java's
 * ({@link GroupedWorkspaces}); a launcher's app hands its manifests and groups in.
 */
public final class WorkspaceSiteCrate implements Crate {

    public static final WorkspaceSiteCrate INSTANCE = new WorkspaceSiteCrate();

    private WorkspaceSiteCrate() {}

    @Override public String name() { return "homing-workspace-site"; }

    @Override public List<Crate> requires() {
        return List.of(
                // the page a workspace is
                WorkspaceShellCrate.INSTANCE,
                // the page's directory, the workspace choice party, and the opener
                WorkspaceGroupsCrate.INSTANCE,
                // a kind's own workspace, as its log names it
                WorkspaceLogCrate.INSTANCE,
                // the href manager: the one way a page goes somewhere
                ServerCrate.INSTANCE);
    }

    @Override
    public List<CrateEntry> entries() {
        return List.of(CrateEntry.of(GroupedWorkspacePageModule.INSTANCE, StandardJsModuleType.CONSUMER));
    }
}
