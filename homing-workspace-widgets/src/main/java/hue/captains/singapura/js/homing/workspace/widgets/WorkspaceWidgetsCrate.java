package hue.captains.singapura.js.homing.workspace.widgets;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;
import hue.captains.singapura.js.homing.core.js.CoreJsCrate;
import hue.captains.singapura.js.homing.design.DesignCrate;
import hue.captains.singapura.js.homing.server.ServerCrate;
import hue.captains.singapura.js.homing.workspace.parties.WorkspacePartiesCrate;

import java.util.List;

/**
 * What a widget is to a workspace, and no widget: the sheet a widget fills its
 * container by, and a widget where a component would stand. The widgets
 * themselves are sets of their own, each in a module of its own - the demo's
 * books, the monitors.
 */
public final class WorkspaceWidgetsCrate implements Crate {

    public static final WorkspaceWidgetsCrate INSTANCE = new WorkspaceWidgetsCrate();

    private WorkspaceWidgetsCrate() {}

    @Override public String name() { return "homing-workspace-widgets"; }

    @Override public List<Crate> requires() {
        return List.of(
                // the DomOpsParty a widget mints its root from
                CoreJsCrate.INSTANCE,
                // the focus party, the keys' convention, the css manager
                ServerCrate.INSTANCE,
                // the design words the sheet wears
                DesignCrate.INSTANCE,
                // the messaging parties' runtime: a composed widget's own scope
                WorkspacePartiesCrate.INSTANCE);
    }

    @Override
    public List<CrateEntry> entries() {
        return List.of(
                CrateEntry.of(WidgetStyles.INSTANCE),
                // a widget where a component would stand: the host's side of the graft
                CrateEntry.of(HostedWidgetModule.INSTANCE, StandardJsModuleType.CONSUMER));
    }
}
