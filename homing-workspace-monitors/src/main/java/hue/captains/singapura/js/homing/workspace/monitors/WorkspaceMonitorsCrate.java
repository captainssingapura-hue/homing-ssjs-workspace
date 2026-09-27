package hue.captains.singapura.js.homing.workspace.monitors;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;
import hue.captains.singapura.js.homing.core.js.CoreJsCrate;
import hue.captains.singapura.js.homing.design.DesignCrate;
import hue.captains.singapura.js.homing.server.ServerCrate;
import hue.captains.singapura.js.homing.ui.focus.UiFocusCrate;
import hue.captains.singapura.js.homing.workspace.widgets.WidgetDeclaration;
import hue.captains.singapura.js.homing.workspace.widgets.WorkspaceWidgetsCrate;

import java.util.List;

/** The monitors, as widgets: the page's parties on view, each monitor self-contained. */
public final class WorkspaceMonitorsCrate implements Crate {

    public static final WorkspaceMonitorsCrate INSTANCE = new WorkspaceMonitorsCrate();

    /** The monitor kinds, for a host that stands them up. */
    public static final List<WidgetDeclaration<?>> KINDS = List.of(
            FocusTreeDeclaration.INSTANCE, StewardLampDeclaration.INSTANCE,
            DomOpsTreeDeclaration.INSTANCE, PartyLogDeclaration.INSTANCE);

    private WorkspaceMonitorsCrate() {}

    @Override public String name() { return "homing-workspace-monitors"; }

    @Override public List<Crate> requires() {
        return List.of(
                // the DomOps party a monitor mints its elements from, and the one it reads
                CoreJsCrate.INSTANCE,
                // the focus party, the steward, the keys' convention, the css manager
                ServerCrate.INSTANCE,
                // the focus tree and the steward's lamp, and the rows the trees and the log wear
                UiFocusCrate.INSTANCE,
                // the design words the monitors' sheet wears
                DesignCrate.INSTANCE,
                // what a widget fills its container by
                WorkspaceWidgetsCrate.INSTANCE);
    }

    @Override
    public List<CrateEntry> entries() {
        return List.of(
                CrateEntry.of(MonitorStyles.INSTANCE),
                CrateEntry.of(MonitorModule.INSTANCE, StandardJsModuleType.CONSUMER),
                CrateEntry.of(FocusTreeModule.INSTANCE, StandardJsModuleType.CONSUMER),
                CrateEntry.of(StewardLampModule.INSTANCE, StandardJsModuleType.CONSUMER),
                CrateEntry.of(DomOpsTreeModule.INSTANCE, StandardJsModuleType.CONSUMER),
                CrateEntry.of(PartyLogModule.INSTANCE, StandardJsModuleType.CONSUMER));
    }
}
