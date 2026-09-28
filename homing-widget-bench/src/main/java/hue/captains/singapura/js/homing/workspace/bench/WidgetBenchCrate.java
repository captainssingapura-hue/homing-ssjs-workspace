package hue.captains.singapura.js.homing.workspace.bench;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;
import hue.captains.singapura.js.homing.core.js.CoreJsCrate;
import hue.captains.singapura.js.homing.design.DesignCrate;
import hue.captains.singapura.js.homing.server.ServerCrate;
import hue.captains.singapura.js.homing.ui.docking.UiDockingCrate;
import hue.captains.singapura.js.homing.ui.elements.UiElementsCrate;
import hue.captains.singapura.js.homing.ui.focus.UiFocusCrate;
import hue.captains.singapura.js.homing.workspace.bench.nasty.NastyFixedModule;
import hue.captains.singapura.js.homing.workspace.bench.nasty.NastyStyles;
import hue.captains.singapura.js.homing.workspace.core.WorkspaceCoreCrate;
import hue.captains.singapura.js.homing.workspace.layers.WorkspaceLayersCrate;
import hue.captains.singapura.js.homing.workspace.log.js.WorkspaceLogCodecCrate;
import hue.captains.singapura.js.homing.workspace.log.store.WorkspaceLogCrate;
import hue.captains.singapura.js.homing.workspace.monitors.WorkspaceMonitorsCrate;
import hue.captains.singapura.js.homing.workspace.parties.WorkspacePartiesCrate;
import hue.captains.singapura.js.homing.workspace.widgets.WorkspaceWidgetsCrate;

import java.util.List;

/** The bench: its page, the kinds it looks up, the container it lends, and the monitors it floats. */
public final class WidgetBenchCrate implements Crate {

    public static final WidgetBenchCrate INSTANCE = new WidgetBenchCrate();

    private WidgetBenchCrate() {}

    @Override public String name() { return "homing-widget-bench"; }

    @Override public List<Crate> requires() {
        return List.of(
                // the css manager the page's classes are added by
                ServerCrate.INSTANCE,
                // the DomOpsParty a nasty widget mints its root from, as every widget does
                CoreJsCrate.INSTANCE,
                // the widgets it stands up, and the host's side of the graft for one in a tab
                WorkspaceWidgetsCrate.INSTANCE,
                // the monitors, which it stands up as widgets like any other, and floats
                WorkspaceMonitorsCrate.INSTANCE,
                // the desk the monitors' floats lie on
                UiDockingCrate.INSTANCE,
                // the design words the toggles wear
                DesignCrate.INSTANCE,
                // the parties a widget joins on the bench: the runtime
                WorkspacePartiesCrate.INSTANCE,
                // the simulator's button, and its log's lines as the monitors' rows
                UiElementsCrate.INSTANCE,
                UiFocusCrate.INSTANCE,
                // the workspace of one pane: its headless core, and the parties beside it
                WorkspaceCoreCrate.INSTANCE,
                // its layers of the log, live; and the log it keeps: the store, the load, the lock
                WorkspaceLayersCrate.INSTANCE,
                WorkspaceLogCrate.INSTANCE,
                WorkspaceLogCodecCrate.INSTANCE);
    }

    @Override
    public List<CrateEntry> entries() {
        return List.of(
                CrateEntry.of(WidgetBenchApp.INSTANCE, StandardJsModuleType.CONSUMER),
                CrateEntry.of(BenchWidgetsModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(BenchWorkspaceModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                CrateEntry.of(WidgetBenchStyles.INSTANCE),
                // the monitors' bar, and the desk their floats lie on
                CrateEntry.of(BenchMonitorsModule.INSTANCE, StandardJsModuleType.CONSUMER),
                // the parties the widget joins, at the root: the manual secretary, and its simulator
                CrateEntry.of(BenchSecretaryModule.INSTANCE, StandardJsModuleType.SECRETARY),
                CrateEntry.of(PartySimulatorModule.INSTANCE, StandardJsModuleType.CONSUMER),
                // a workspace of one pane, on its headless core: its placement, the workspace, its page
                CrateEntry.of(SinglePaneModule.INSTANCE, StandardJsModuleType.CONSUMER),
                CrateEntry.of(SinglePaneWorkspaceModule.INSTANCE, StandardJsModuleType.CONSUMER),
                CrateEntry.of(WorkspaceBenchApp.INSTANCE, StandardJsModuleType.CONSUMER),
                // the nasty widgets, which misbehave on purpose for the bench to catch
                CrateEntry.of(NastyFixedModule.INSTANCE, StandardJsModuleType.CONSUMER),
                CrateEntry.of(NastyStyles.INSTANCE));
    }
}
