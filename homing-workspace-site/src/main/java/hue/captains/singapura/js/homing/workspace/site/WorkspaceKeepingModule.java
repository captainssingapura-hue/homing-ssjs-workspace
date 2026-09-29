package hue.captains.singapura.js.homing.workspace.site;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.js.DomOpsPartyModule;
import hue.captains.singapura.js.homing.core.js.domOpsParty;
import hue.captains.singapura.js.homing.workspace.log.js.LogIdsModule;
import hue.captains.singapura.js.homing.workspace.log.js.LogKeyModule;
import hue.captains.singapura.js.homing.workspace.log.store.WorkspaceLogIdentityModule;
import hue.captains.singapura.js.homing.workspace.log.store.WorkspaceWriteLockModule;
import hue.captains.singapura.js.homing.workspace.switcher.WorkspaceConfirmModule;

import java.util.List;

/**
 * What a grouped page does to its site's workspaces when its party asks:
 * {@code WorkspaceKeeping} - a new one made, one called otherwise, one deleted -
 * softly - by the catalogue this browser keeps; a name another of the kind has,
 * refused; a delete refused where it would pull the ground from under a page,
 * and else asked of a person first, in the system dialog.
 */
public record WorkspaceKeepingModule() implements DomModule<WorkspaceKeepingModule> {

    public record WorkspaceKeeping() implements Exportable._Class<WorkspaceKeepingModule> {}

    public static final WorkspaceKeepingModule INSTANCE = new WorkspaceKeepingModule();

    @Override
    public ImportsFor<WorkspaceKeepingModule> imports() {
        return ImportsFor.<WorkspaceKeepingModule>builder()
                // a workspace by its log's key: its kind, its id - a fresh one, or a kind's own
                .add(new ModuleImports<>(List.of(new LogKeyModule.LogKey()), LogKeyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new LogIdsModule.WorkspaceKind(), new LogIdsModule.WorkspaceInstanceId()), LogIdsModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkspaceLogIdentityModule.WorkspaceLogIdentity()), WorkspaceLogIdentityModule.INSTANCE))
                // whether another page is writing one: its write lock's name
                .add(new ModuleImports<>(List.of(new WorkspaceWriteLockModule.WorkspaceWriteLock()), WorkspaceWriteLockModule.INSTANCE))
                // a person asked before a delete, in the system dialog, on a branch of the page's
                .add(new ModuleImports<>(List.of(new WorkspaceConfirmModule.WorkspaceConfirm()), WorkspaceConfirmModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new domOpsParty()), DomOpsPartyModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<WorkspaceKeepingModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new WorkspaceKeeping())); }
}
