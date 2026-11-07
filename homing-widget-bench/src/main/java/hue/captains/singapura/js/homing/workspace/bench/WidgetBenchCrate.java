package hue.captains.singapura.js.homing.workspace.bench;

import hue.captains.singapura.js.homing.ui.look.UiLookCrate;
import hue.captains.singapura.js.homing.workspace.tree.WorkspaceTreeCrate;
import hue.captains.singapura.js.homing.ui.splitgrid.UiSplitGridCrate;
import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;
import hue.captains.singapura.js.homing.core.js.CoreJsCrate;
import hue.captains.singapura.js.homing.server.ServerCrate;
import hue.captains.singapura.js.homing.ui.docking.UiDockingCrate;
import hue.captains.singapura.js.homing.ui.elements.UiElementsCrate;
import hue.captains.singapura.js.homing.ui.monitors.UiMonitorsCrate;
import hue.captains.singapura.js.homing.workspace.bench.nasty.NastyFixedModule;
import hue.captains.singapura.js.homing.workspace.content.WorkspaceContentCrate;
import hue.captains.singapura.js.homing.workspace.bench.nasty.NastyStyles;
import hue.captains.singapura.js.homing.workspace.core.WorkspaceCoreCrate;
import hue.captains.singapura.js.homing.workspace.demowidgets.WorkspaceDemoWidgetsCrate;
import hue.captains.singapura.js.homing.workspace.layers.WorkspaceLayersCrate;
import hue.captains.singapura.js.homing.workspace.log.js.WorkspaceLogCodecCrate;
import hue.captains.singapura.js.homing.workspace.log.store.WorkspaceLogCrate;
import hue.captains.singapura.js.homing.workspace.groups.WorkspaceGroupsCrate;
import hue.captains.singapura.js.homing.workspace.monitors.WorkspaceMonitorsCrate;
import hue.captains.singapura.js.homing.workspace.parties.WorkspacePartiesCrate;
import hue.captains.singapura.js.homing.workspace.switcher.WorkspaceSwitcherCrate;
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
                // the demo's widgets: the books
                WorkspaceDemoWidgetsCrate.INSTANCE,
                // the monitors, which it stands up as widgets like any other, and floats
                WorkspaceMonitorsCrate.INSTANCE,
                // the workspace switcher, which it stands up as widgets like any other; and the directory its page provides
                WorkspaceSwitcherCrate.INSTANCE,
                WorkspaceGroupsCrate.INSTANCE,
                // the desk the monitors' floats lie on
                UiDockingCrate.INSTANCE,
                // the design words the toggles wear
                // the parties a widget joins on the bench: the runtime
                WorkspacePartiesCrate.INSTANCE,
                // the simulator's button, and its log's lines as the monitors' rows
                UiLookCrate.INSTANCE,
                UiElementsCrate.INSTANCE,
                UiMonitorsCrate.INSTANCE,
                // the workspace of one pane: its headless core, and the parties beside it
                WorkspaceCoreCrate.INSTANCE,
                // its layers of the log, live; and the log it keeps: the store, the load, the lock
                WorkspaceLayersCrate.INSTANCE,
                WorkspaceLogCrate.INSTANCE,
                WorkspaceLogCodecCrate.INSTANCE,
                // the tree placement's engine, for the tree bench, and the split grid it is laid out in
                WorkspaceTreeCrate.INSTANCE, UiSplitGridCrate.INSTANCE,
                // content parties and the flow, for the content bench
                WorkspaceContentCrate.INSTANCE);
    }

    @Override
    public List<CrateEntry> entries() {
        return List.of(
                new CrateEntry(WidgetBenchApp.INSTANCE, StandardJsModuleType.CONSUMER),
                new CrateEntry(BenchWidgetsModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                new CrateEntry(BenchWorkspaceModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                // its groups, for its page's directory
                new CrateEntry(BenchGroupsModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                new CrateEntry(WidgetBenchStyles.INSTANCE),
                // the monitors' bar, and the desk their floats lie on
                new CrateEntry(BenchMonitorsModule.INSTANCE, StandardJsModuleType.CONSUMER),
                // the parties the widget joins, at the root: the manual secretary, and its simulator
                new CrateEntry(BenchSecretaryModule.INSTANCE, StandardJsModuleType.SECRETARY),
                new CrateEntry(PartySimulatorModule.INSTANCE, StandardJsModuleType.CONSUMER),
                // a workspace of one pane, on its headless core: its placement, the workspace, its page
                new CrateEntry(PaneSlotsModule.INSTANCE, StandardJsModuleType.CONSUMER),
                new CrateEntry(PanePickerModule.INSTANCE, StandardJsModuleType.CONSUMER),
                new CrateEntry(SinglePaneModule.INSTANCE, StandardJsModuleType.CONSUMER),
                new CrateEntry(SinglePaneWorkspaceModule.INSTANCE, StandardJsModuleType.CONSUMER),
                new CrateEntry(WorkspaceBenchApp.INSTANCE, StandardJsModuleType.CONSUMER),
                // the nasty widgets, which misbehave on purpose for the bench to catch
                new CrateEntry(NastyFixedModule.INSTANCE, StandardJsModuleType.CONSUMER),
                new CrateEntry(NastyStyles.INSTANCE),
                // the tree placement, on the bench: its tree, its stand-in card, its page
                new CrateEntry(TreeBenchStyles.INSTANCE),
                new CrateEntry(BenchTreeModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                new CrateEntry(ParamsCardModule.INSTANCE, StandardJsModuleType.CONSUMER),
                new CrateEntry(TreeBenchApp.INSTANCE, StandardJsModuleType.CONSUMER),
                // content parties, on the bench: its note type and stewards, its card, its scopes, the traffic, its page
                new CrateEntry(ContentBenchStyles.INSTANCE),
                new CrateEntry(BenchNoteModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                new CrateEntry(BenchContentStewardModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                new CrateEntry(BenchNoteStewardModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                new CrateEntry(BenchFlowStewardModule.INSTANCE, StandardJsModuleType.PURE_LOGIC),
                new CrateEntry(NoteCardModule.INSTANCE, StandardJsModuleType.CONSUMER),
                new CrateEntry(ContentPanelModule.INSTANCE, StandardJsModuleType.CONSUMER),
                new CrateEntry(PartyTrafficModule.INSTANCE, StandardJsModuleType.CONSUMER),
                new CrateEntry(ContentBenchApp.INSTANCE, StandardJsModuleType.CONSUMER));
    }
}
