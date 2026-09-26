package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.ui.docking.DeskModule;
import hue.captains.singapura.js.homing.ui.docking.DockGridModule;
import hue.captains.singapura.js.homing.ui.menu.ContextMenuStewardModule;
import hue.captains.singapura.js.homing.ui.panes.TabOpenerModule;
import hue.captains.singapura.js.homing.ui.panes.TabSourceModule;

import java.util.List;

/**
 * The workspace, built as the gallery's docking page is, from the same
 * components (RFC 0066 E3, the workspace detour): a desk and its docks in a
 * dock grid, on a floor filling what holds it, and its tabs from one tab
 * source - the plus opening the chooser, a pick turning that tab into the
 * kind picked. No event store yet.
 */
public record WorkspaceModule() implements DomModule<WorkspaceModule> {

    /** The class: {@code new Workspace(branch, { host, kinds?, keyboard?, menus?, budget?, log? })}. */
    public record Workspace() implements Exportable._Class<WorkspaceModule> {}

    public static final WorkspaceModule INSTANCE = new WorkspaceModule();

    @Override
    public ImportsFor<WorkspaceModule> imports() {
        return ImportsFor.<WorkspaceModule>builder()
                .add(new ModuleImports<>(List.of(new DeskModule.Desk()), DeskModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new DockGridModule.DockGrid()), DockGridModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new TabSourceModule.TabSource()), TabSourceModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new TabOpenerModule.TabOpener()), TabOpenerModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new ContextMenuStewardModule.ContextMenuSteward()), ContextMenuStewardModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkspaceMenus.MENUS()), WorkspaceMenus.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkspaceStyles.ws_floor()), WorkspaceStyles.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkspaceRecorderModule.WorkspaceRecorder()), WorkspaceRecorderModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkspaceProjectionModule.WorkspaceProjection()), WorkspaceProjectionModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkspaceLogBarModule.WorkspaceLogBar()), WorkspaceLogBarModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<WorkspaceModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new Workspace()));
    }
}
