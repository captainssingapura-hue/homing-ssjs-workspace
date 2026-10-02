package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.ui.docking.DeskModule;
import hue.captains.singapura.js.homing.ui.docking.DockGridModule;
import hue.captains.singapura.js.homing.ui.menu.ContextMenuStewardModule;
import hue.captains.singapura.js.homing.workspace.core.KindAndParamsTitleModule;
import hue.captains.singapura.js.homing.workspace.core.WorkspaceCoreModule;
import hue.captains.singapura.js.homing.workspace.core.WorkspacePartiesModule;
import hue.captains.singapura.js.homing.workspace.core.WorkspaceRequestModule;
import hue.captains.singapura.js.homing.workspace.layers.RosterLayerModule;

import java.util.List;

/**
 * The split-grid workspace on its headless core (RFC 0066 E3, the workspace
 * detour: requests): the desk and its dock grid on a floor, as the gallery's
 * docking page builds them, every tab a widget's. What it is - its kinds, its
 * root parties - is its manifest's, generated from its declaration in Java; it
 * knows no widget. The core keeps the widgets' life and executes what a user
 * asks; the split grid mounts and unmounts; the controls - a dock's picker, a
 * tab's close - only ask. Its log keeps the roster's layer and the grid's, and
 * it comes back roster first.
 */
public record GridWorkspaceModule() implements DomModule<GridWorkspaceModule> {

    /** The class: {@code new GridWorkspace(branch, { host, manifest, keyboard?, menus?, budget?, log?, state?, … })}. */
    public record GridWorkspace() implements Exportable._Class<GridWorkspaceModule> {}

    public static final GridWorkspaceModule INSTANCE = new GridWorkspaceModule();

    @Override
    public ImportsFor<GridWorkspaceModule> imports() {
        return ImportsFor.<GridWorkspaceModule>builder()
                // the whole and its regions, as the gallery's docking page has them, and the page's menus
                .add(new ModuleImports<>(List.of(new DeskModule.Desk()), DeskModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new DockGridModule.DockGrid()), DockGridModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new ContextMenuStewardModule.ContextMenuSteward()), ContextMenuStewardModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkspaceMenus.MENUS()), WorkspaceMenus.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkspaceStyles.ws_floor()), WorkspaceStyles.INSTANCE))
                // the headless core, what it is asked, the titles by the rule, the parties beside it
                .add(new ModuleImports<>(List.of(new WorkspaceCoreModule.WorkspaceCore()), WorkspaceCoreModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkspaceRequestModule.WorkspaceRequest()), WorkspaceRequestModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new KindAndParamsTitleModule.KindAndParamsTitle()), KindAndParamsTitleModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkspacePartiesModule.WorkspaceParties()), WorkspacePartiesModule.INSTANCE))
                // its register of panes and its placement
                .add(new ModuleImports<>(List.of(new WidgetTabsModule.WidgetTabs()), WidgetTabsModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new GridPlacementModule.GridPlacement()), GridPlacementModule.INSTANCE))
                // its log: the roster's layer, the grid's recorder, the read-back, the bar
                .add(new ModuleImports<>(List.of(new RosterLayerModule.RosterLayer()), RosterLayerModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkspaceRecorderModule.WorkspaceRecorder()), WorkspaceRecorderModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkspaceProjectionModule.WorkspaceProjection()), WorkspaceProjectionModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkspaceLogBarModule.WorkspaceLogBar()), WorkspaceLogBarModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<GridWorkspaceModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new GridWorkspace())); }
}
