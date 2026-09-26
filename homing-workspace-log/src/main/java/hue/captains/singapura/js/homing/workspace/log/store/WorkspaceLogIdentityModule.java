package hue.captains.singapura.js.homing.workspace.log.store;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.log.js.LogIdsModule;
import hue.captains.singapura.js.homing.workspace.log.js.LogHeaderModule;

import java.util.List;

/** Whose log a page keeps: its kind, and the workspace the address names or the kind's own. */
public record WorkspaceLogIdentityModule() implements DomModule<WorkspaceLogIdentityModule> {

    public record WorkspaceLogIdentity() implements Exportable._Class<WorkspaceLogIdentityModule> {}

    public static final WorkspaceLogIdentityModule INSTANCE = new WorkspaceLogIdentityModule();

    @Override
    public ImportsFor<WorkspaceLogIdentityModule> imports() {
        return ImportsFor.<WorkspaceLogIdentityModule>builder()
                .add(new ModuleImports<>(List.of(new LogIdsModule.WorkspaceKind(), new LogIdsModule.WorkspaceInstanceId()), LogIdsModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new LogHeaderModule.LogHeader()), LogHeaderModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<WorkspaceLogIdentityModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new WorkspaceLogIdentity())); }
}
