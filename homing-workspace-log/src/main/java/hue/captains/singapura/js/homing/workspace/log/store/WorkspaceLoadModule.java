package hue.captains.singapura.js.homing.workspace.log.store;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.log.fold.WorkspaceFoldModule;
import hue.captains.singapura.js.homing.workspace.log.js.CheckpointModule;

import java.util.List;

/**
 * A workspace's log read back to what it folds to - its latest checkpoint and
 * what came after, every layer at once - or set aside when it will not: the
 * one load every placement's page shares.
 */
public record WorkspaceLoadModule() implements DomModule<WorkspaceLoadModule> {

    public record WorkspaceLoad() implements Exportable._Class<WorkspaceLoadModule> {}

    public static final WorkspaceLoadModule INSTANCE = new WorkspaceLoadModule();

    @Override
    public ImportsFor<WorkspaceLoadModule> imports() {
        return ImportsFor.<WorkspaceLoadModule>builder()
                .add(new ModuleImports<>(List.of(new WorkspaceFoldModule.WorkspaceFold()), WorkspaceFoldModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new CheckpointModule.Checkpoint()), CheckpointModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkspaceLogStoreModule.WorkspaceLogStore()), WorkspaceLogStoreModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new MemoryLogModule.MemoryLog()), MemoryLogModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<WorkspaceLoadModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new WorkspaceLoad())); }
}
