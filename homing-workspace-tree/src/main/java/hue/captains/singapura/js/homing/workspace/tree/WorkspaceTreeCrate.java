package hue.captains.singapura.js.homing.workspace.tree;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;
import hue.captains.singapura.js.homing.core.js.CoreJsCrate;
import hue.captains.singapura.js.homing.design.DesignCrate;
import hue.captains.singapura.js.homing.relgrid.protocol.RelGridProtocolCrate;
import hue.captains.singapura.js.homing.reltree.RelTreeCrate;
import hue.captains.singapura.js.homing.server.ServerCrate;
import hue.captains.singapura.js.homing.workspace.widgets.WorkspaceWidgetsCrate;

import java.util.List;

/**
 * The tree placement's engine and its sheet, a set of their own: a fixed tree laid out as a
 * reading flow, its widgets made from their types and params. Nothing here knows a doc, a
 * plan, or a page.
 */
public final class WorkspaceTreeCrate implements Crate {

    public static final WorkspaceTreeCrate INSTANCE = new WorkspaceTreeCrate();

    private WorkspaceTreeCrate() {}

    @Override public String name() { return "homing-workspace-tree"; }

    @Override public List<Crate> requires() {
        return List.of(
                // the DomOpsParty the layout mints its own from
                CoreJsCrate.INSTANCE,
                // the focus party, the css manager
                ServerCrate.INSTANCE,
                // the design words the sheet wears
                DesignCrate.INSTANCE,
                // what a widget is: the sheet it fills its container by
                WorkspaceWidgetsCrate.INSTANCE,
                // the table of contents' relation tree, and its questions
                RelTreeCrate.INSTANCE, RelGridProtocolCrate.INSTANCE);
    }

    @Override
    public List<CrateEntry> entries() {
        return List.of(
                CrateEntry.of(TreeStyles.INSTANCE),
                CrateEntry.of(TreeLayoutModule.INSTANCE, StandardJsModuleType.CONSUMER),
                CrateEntry.of(TreeTocModule.INSTANCE, StandardJsModuleType.CONSUMER));
    }
}
