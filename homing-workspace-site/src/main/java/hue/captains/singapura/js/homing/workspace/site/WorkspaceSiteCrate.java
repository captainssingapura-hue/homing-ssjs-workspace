package hue.captains.singapura.js.homing.workspace.site;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;
import hue.captains.singapura.js.homing.core.js.CoreJsCrate;
import hue.captains.singapura.js.homing.server.ServerCrate;
import hue.captains.singapura.js.homing.workspace.groups.WorkspaceGroupsCrate;
import hue.captains.singapura.js.homing.workspace.log.js.WorkspaceLogCodecCrate;
import hue.captains.singapura.js.homing.workspace.log.store.WorkspaceLogCrate;
import hue.captains.singapura.js.homing.workspace.parties.WorkspacePartiesCrate;
import hue.captains.singapura.js.homing.workspace.shell.WorkspaceShellCrate;
import hue.captains.singapura.js.homing.workspace.switcher.WorkspaceSwitcherCrate;

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
                // a kind's own workspace, as its log names it; a new one listed in the catalogue, by its log's key
                WorkspaceLogCrate.INSTANCE,
                WorkspaceLogCodecCrate.INSTANCE,
                // the page's own choice party, and the switcher it summons in the system dialog
                WorkspacePartiesCrate.INSTANCE,
                WorkspaceSwitcherCrate.INSTANCE,
                // the page's DomOps party
                CoreJsCrate.INSTANCE,
                // the href manager: the one way a page goes somewhere
                ServerCrate.INSTANCE);
    }

    @Override
    public List<CrateEntry> entries() {
        return List.of(
                CrateEntry.of(GroupedWorkspacePageModule.INSTANCE, StandardJsModuleType.CONSUMER),
                // what the page does to its workspaces: made, renamed, deleted - softly
                CrateEntry.of(WorkspaceKeepingModule.INSTANCE, StandardJsModuleType.CONSUMER));
    }
}
