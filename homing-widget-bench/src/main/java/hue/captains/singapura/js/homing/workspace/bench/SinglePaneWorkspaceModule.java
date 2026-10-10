package hue.captains.singapura.js.homing.workspace.bench;

import hue.captains.singapura.js.homing.component.keyboard.FocusPartyModule;
import hue.captains.singapura.js.homing.component.keyboard.focusParties;
import hue.captains.singapura.js.homing.conformance.rules.ConformanceDebt;
import hue.captains.singapura.js.homing.conformance.rules.WidgetDrawsOnlyComponentsRule;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.layers.PaneLayerModule;
import hue.captains.singapura.js.homing.workspace.layers.RosterLayerModule;
import hue.captains.singapura.js.homing.core.js.DomOpsPartyModule;
import hue.captains.singapura.js.homing.core.js.domOpsParties;
import hue.captains.singapura.js.homing.ui.elements.StatesAndEventsModule;
import hue.captains.singapura.js.homing.ui.elements.PlainButtonModule;
import hue.captains.singapura.js.homing.workspace.core.KindAndParamsTitleModule;
import hue.captains.singapura.js.homing.workspace.core.WorkspaceCoreModule;
import hue.captains.singapura.js.homing.workspace.core.WorkspacePartiesModule;
import hue.captains.singapura.js.homing.workspace.core.WorkspaceRequestModule;
import hue.captains.singapura.js.homing.workspace.widgets.SelfContainedWidget;

import java.util.List;

/**
 * A workspace of one pane, self-contained as a widget is: {@code new
 * SinglePaneWorkspace(container, { kinds, secretaries })} - its own roots,
 * grafted when the page mounts it; its widgets its headless core's, their
 * parties beside it; a single pane showing one of them at a time.
 */
@ConformanceDebt(rule = WidgetDrawsOnlyComponentsRule.class, findings = 27,
        reason = "It makes elements by a companion, makes its own elements, places elements, wears classes of its own, writes attributes and writes text of its own: it waits for the designed containers, the widget slot and the content components to draw only components.")
public record SinglePaneWorkspaceModule() implements DomModule<SinglePaneWorkspaceModule> {

    public record SinglePaneWorkspace() implements SelfContainedWidget<SinglePaneWorkspaceModule> {
        @Override public String summary() { return "A workspace of one pane, self-contained: its roots its own, its widgets its headless core's, one of them shown at a time."; }
    }

    public static final SinglePaneWorkspaceModule INSTANCE = new SinglePaneWorkspaceModule();

    @Override
    public ImportsFor<SinglePaneWorkspaceModule> imports() {
        return ImportsFor.<SinglePaneWorkspaceModule>builder()
                .add(new ModuleImports<>(List.of(new StatesAndEventsModule.EVENT()), StatesAndEventsModule.INSTANCE))
                // its own roots
                .add(new ModuleImports<>(List.of(new domOpsParties(), new DomOpsPartyModule.MobileDomOpsParty()), DomOpsPartyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new focusParties()), FocusPartyModule.INSTANCE))
                // the headless core, and the parties beside it
                .add(new ModuleImports<>(List.of(new WorkspaceCoreModule.WorkspaceCore()), WorkspaceCoreModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkspaceRequestModule.WorkspaceRequest()), WorkspaceRequestModule.INSTANCE))
                // the rule its widgets are titled by
                .add(new ModuleImports<>(List.of(new KindAndParamsTitleModule.KindAndParamsTitle()), KindAndParamsTitleModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkspacePartiesModule.WorkspaceParties()), WorkspacePartiesModule.INSTANCE))
                // its layers of the log, live: recorded, and come back to
                .add(new ModuleImports<>(List.of(new RosterLayerModule.RosterLayer()), RosterLayerModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new PaneLayerModule.PaneLayer()), PaneLayerModule.INSTANCE))
                // its placement, and its bar
                .add(new ModuleImports<>(List.of(new PaneSlotsModule.PaneSlots()), PaneSlotsModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new SinglePaneModule.SinglePane()), SinglePaneModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new PlainButtonModule.PlainButton(), new PlainButtonModule.PlainButtonFactory()), PlainButtonModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WidgetBenchStyles.wb_ws(), new WidgetBenchStyles.wb_ws_bar(), new WidgetBenchStyles.wb_ws_label(), new WidgetBenchStyles.wb_ws_name(),
                        new WidgetBenchStyles.wb_bench(), new WidgetBenchStyles.wb_sim_pick()), WidgetBenchStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<SinglePaneWorkspaceModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new SinglePaneWorkspace())); }
}
