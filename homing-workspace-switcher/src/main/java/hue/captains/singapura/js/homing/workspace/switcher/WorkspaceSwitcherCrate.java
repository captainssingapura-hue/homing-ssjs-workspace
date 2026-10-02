package hue.captains.singapura.js.homing.workspace.switcher;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;
import hue.captains.singapura.js.homing.core.js.CoreJsCrate;
import hue.captains.singapura.js.homing.design.DesignCrate;
import hue.captains.singapura.js.homing.relgrid.RelGridCrate;
import hue.captains.singapura.js.homing.relgrid.protocol.RelGridProtocolCrate;
import hue.captains.singapura.js.homing.reltree.RelTreeCrate;
import hue.captains.singapura.js.homing.server.ServerCrate;
import hue.captains.singapura.js.homing.ui.dialog.UiDialogCrate;
import hue.captains.singapura.js.homing.workspace.groups.WorkspaceGroupsCrate;
import hue.captains.singapura.js.homing.workspace.log.js.WorkspaceLogCodecCrate;
import hue.captains.singapura.js.homing.workspace.log.store.WorkspaceLogCrate;
import hue.captains.singapura.js.homing.workspace.parties.WorkspacePartiesCrate;
import hue.captains.singapura.js.homing.workspace.widgets.WidgetDeclaration;
import hue.captains.singapura.js.homing.workspace.widgets.WorkspaceWidgetsCrate;

import java.util.List;

/**
 * The workspace switcher, as widgets and nothing else: the kinds, as a tree;
 * the chosen kind's workspaces, as a table; the two composed. Any workspace may
 * declare them among its kinds ({@link #KINDS}); nothing here knows a shell or a
 * page, and nothing here opens a workspace - the party says it is asked.
 */
public final class WorkspaceSwitcherCrate implements Crate {

    public static final WorkspaceSwitcherCrate INSTANCE = new WorkspaceSwitcherCrate();

    /** The switcher's kinds, for a workspace that offers them. */
    public static final List<WidgetDeclaration<?>> KINDS = List.of(
            WorkspaceKindsDeclaration.INSTANCE, WorkspaceInstancesDeclaration.INSTANCE, WorkspaceSwitcherDeclaration.INSTANCE);

    private WorkspaceSwitcherCrate() {}

    @Override public String name() { return "homing-workspace-switcher"; }

    @Override public List<Crate> requires() {
        return List.of(
                // the DomOps party a widget mints its root from
                CoreJsCrate.INSTANCE,
                // the focus party, the keys' convention, the css manager
                ServerCrate.INSTANCE,
                // the kinds' tree, the workspaces' table, their stock cells and their protocol
                RelTreeCrate.INSTANCE,
                RelGridCrate.INSTANCE,
                RelGridProtocolCrate.INSTANCE,
                // the system dialog the switcher is summoned in
                UiDialogCrate.INSTANCE,
                // the design words the switcher's sheet wears
                DesignCrate.INSTANCE,
                // the page's directory, and the workspace choice party: its type and secretaries
                WorkspaceGroupsCrate.INSTANCE,
                // the catalogue the table reads, and the kind it reads by
                WorkspaceLogCrate.INSTANCE,
                WorkspaceLogCodecCrate.INSTANCE,
                // the messaging parties' runtime: the composed switcher's own scope
                WorkspacePartiesCrate.INSTANCE,
                // what a widget is: the sheet it fills its container by
                WorkspaceWidgetsCrate.INSTANCE);
    }

    @Override
    public List<CrateEntry> entries() {
        return List.of(
                CrateEntry.of(SwitcherStyles.INSTANCE),
                // the kinds, as a tree: the relation tree's rows natively focused inside a widget
                CrateEntry.of(WorkspaceKindsModule.INSTANCE, StandardJsModuleType.CONSUMER),
                // the chosen kind's workspaces, as a table
                // its rows: the relation, a workspace's name edited only when asked
                CrateEntry.of(WorkspaceNameCellModule.INSTANCE, StandardJsModuleType.CONSUMER),
                CrateEntry.of(WorkspaceRowsModule.INSTANCE, StandardJsModuleType.CONSUMER),
                CrateEntry.of(WorkspaceInstancesModule.INSTANCE, StandardJsModuleType.CONSUMER),
                // the two composed: they meet in a scope of the switcher's own, its secretary keeping the edge
                CrateEntry.of(WorkspaceSwitcherModule.INSTANCE, StandardJsModuleType.CONSUMER),
                // the switcher summoned by a page, in the system dialog: a page's, kept by no log
                CrateEntry.of(WorkspaceSwitcherDialogModule.INSTANCE, StandardJsModuleType.CONSUMER),
                // a question before an act on a workspace, in the system dialog: a page's, as the switcher's is
                CrateEntry.of(WorkspaceConfirmModule.INSTANCE, StandardJsModuleType.CONSUMER));
    }
}
