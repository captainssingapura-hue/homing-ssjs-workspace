package hue.captains.singapura.js.homing.workspace.log.fold;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.log.js.CheckpointModule;
import hue.captains.singapura.js.homing.workspace.log.js.LogHeaderModule;

import java.util.List;

/** The next checkpoint of a log, a periodic fold: Java's CheckpointFold, transcribed. */
public record CheckpointFoldModule() implements DomModule<CheckpointFoldModule> {

    public record CheckpointFold() implements Exportable._Class<CheckpointFoldModule> {}

    public static final CheckpointFoldModule INSTANCE = new CheckpointFoldModule();

    @Override
    public ImportsFor<CheckpointFoldModule> imports() {
        return ImportsFor.<CheckpointFoldModule>builder()
                .add(new ModuleImports<>(List.of(new WorkspaceFoldModule.WorkspaceFold()), WorkspaceFoldModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new LogHeaderModule.LogHeaderCodec()), LogHeaderModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new CheckpointModule.Checkpoint()), CheckpointModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<CheckpointFoldModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new CheckpointFold())); }
}
