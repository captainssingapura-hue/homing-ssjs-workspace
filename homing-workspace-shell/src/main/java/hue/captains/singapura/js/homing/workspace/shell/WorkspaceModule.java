package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.ui.docking.DeskModule;
import hue.captains.singapura.js.homing.ui.docking.DockGridModule;
import hue.captains.singapura.js.homing.ui.menu.ContextMenuStewardModule;

import java.util.List;

/**
 * The workspace, built as the gallery's docking page is, from the same
 * components (RFC 0066 E3, the workspace detour): a desk and its docks in a
 * dock grid, on a floor filling what holds it. Step one of the rebuild: no
 * event store, no way to add a widget; the regions part, merge and close.
 */
public record WorkspaceModule() implements DomModule<WorkspaceModule> {

    /** The class: {@code new Workspace(branch, { host, keyboard?, menus?, budget? })}. */
    public record Workspace() implements Exportable._Class<WorkspaceModule> {}

    public static final WorkspaceModule INSTANCE = new WorkspaceModule();

    @Override
    public ImportsFor<WorkspaceModule> imports() {
        return ImportsFor.<WorkspaceModule>builder()
                .add(new ModuleImports<>(List.of(new DeskModule.Desk()), DeskModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new DockGridModule.DockGrid()), DockGridModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new ContextMenuStewardModule.ContextMenuSteward()), ContextMenuStewardModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkspaceMenus.MENUS()), WorkspaceMenus.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkspaceStyles.ws_floor()), WorkspaceStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<WorkspaceModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new Workspace()));
    }
}
