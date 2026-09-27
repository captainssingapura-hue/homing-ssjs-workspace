package hue.captains.singapura.js.homing.workspace.log.store;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.log.js.CheckpointModule;
import hue.captains.singapura.js.homing.workspace.log.js.LogHeaderModule;

import java.util.List;

/** A log's checkpoints, taken as it goes: folded by the checkpoint worker, kept by the page, posted to a server that takes them. */
public record WorkspaceCheckpointerModule() implements DomModule<WorkspaceCheckpointerModule> {

    public record WorkspaceCheckpointer() implements Exportable._Class<WorkspaceCheckpointerModule> {}

    public static final WorkspaceCheckpointerModule INSTANCE = new WorkspaceCheckpointerModule();

    @Override
    public ImportsFor<WorkspaceCheckpointerModule> imports() {
        return ImportsFor.<WorkspaceCheckpointerModule>builder()
                .add(new ModuleImports<>(List.of(new LogHeaderModule.LogHeaderCodec()), LogHeaderModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new CheckpointModule.Checkpoint(), new CheckpointModule.CheckpointCodec()), CheckpointModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<WorkspaceCheckpointerModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new WorkspaceCheckpointer())); }
}
