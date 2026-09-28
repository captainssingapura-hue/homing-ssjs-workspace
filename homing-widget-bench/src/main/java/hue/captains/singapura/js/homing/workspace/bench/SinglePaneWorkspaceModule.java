package hue.captains.singapura.js.homing.workspace.bench;

import hue.captains.singapura.js.homing.component.keyboard.FocusPartyModule;
import hue.captains.singapura.js.homing.component.keyboard.focusParties;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.layers.PaneLayerModule;
import hue.captains.singapura.js.homing.workspace.layers.RosterLayerModule;
import hue.captains.singapura.js.homing.core.js.DomOpsPartyModule;
import hue.captains.singapura.js.homing.core.js.domOpsParties;
import hue.captains.singapura.js.homing.ui.elements.Elements;
import hue.captains.singapura.js.homing.workspace.core.WorkspaceCoreModule;
import hue.captains.singapura.js.homing.workspace.core.WorkspacePartiesModule;
import hue.captains.singapura.js.homing.workspace.widgets.SelfContainedWidget;

import java.util.List;

/**
 * A workspace of one pane, self-contained as a widget is: {@code new
 * SinglePaneWorkspace(container, { kinds, secretaries })} - its own roots,
 * grafted when the page mounts it; its widgets its headless core's, their
 * parties beside it; a single pane showing one of them at a time.
 */
public record SinglePaneWorkspaceModule() implements DomModule<SinglePaneWorkspaceModule> {

    public record SinglePaneWorkspace() implements SelfContainedWidget<SinglePaneWorkspaceModule> {
        @Override public String summary() { return "A workspace of one pane, self-contained: its roots its own, its widgets its headless core's, one of them shown at a time."; }
    }

    public static final SinglePaneWorkspaceModule INSTANCE = new SinglePaneWorkspaceModule();

    @Override
    public ImportsFor<SinglePaneWorkspaceModule> imports() {
        return ImportsFor.<SinglePaneWorkspaceModule>builder()
                // its own roots
                .add(new ModuleImports<>(List.of(new domOpsParties(), new DomOpsPartyModule.MobileDomOpsParty()), DomOpsPartyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new focusParties()), FocusPartyModule.INSTANCE))
                // the headless core, and the parties beside it
                .add(new ModuleImports<>(List.of(new WorkspaceCoreModule.WorkspaceCore()), WorkspaceCoreModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkspacePartiesModule.WorkspaceParties()), WorkspacePartiesModule.INSTANCE))
                // its layers of the log, live: recorded, and come back to
                .add(new ModuleImports<>(List.of(new RosterLayerModule.RosterLayer()), RosterLayerModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new PaneLayerModule.PaneLayer()), PaneLayerModule.INSTANCE))
                // its placement, and its bar
                .add(new ModuleImports<>(List.of(new SinglePaneModule.SinglePane()), SinglePaneModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new Elements.ButtonBuilder()), Elements.INSTANCE))
                .add(new ModuleImports<>(List.of(new WidgetBenchStyles.wb_ws(), new WidgetBenchStyles.wb_ws_bar(), new WidgetBenchStyles.wb_ws_label(),
                        new WidgetBenchStyles.wb_bench(), new WidgetBenchStyles.wb_sim_pick()), WidgetBenchStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<SinglePaneWorkspaceModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new SinglePaneWorkspace())); }
}
