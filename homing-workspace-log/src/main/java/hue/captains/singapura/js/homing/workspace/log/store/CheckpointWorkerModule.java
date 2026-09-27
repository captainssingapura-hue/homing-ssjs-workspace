package hue.captains.singapura.js.homing.workspace.log.store;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.log.fold.CheckpointFoldModule;
import hue.captains.singapura.js.homing.workspace.log.js.CheckpointModule;
import hue.captains.singapura.js.homing.workspace.log.js.LogHeaderModule;
import hue.captains.singapura.js.homing.workspace.log.js.LoggedEventModule;

import java.util.List;

/**
 * A checkpoint's fold, off the page's thread: the script a module worker runs.
 * It exports nothing - a worker is spoken to by messages - and imports what the
 * fold needs, served as any module is.
 */
public record CheckpointWorkerModule() implements DomModule<CheckpointWorkerModule> {

    public static final CheckpointWorkerModule INSTANCE = new CheckpointWorkerModule();

    @Override
    public ImportsFor<CheckpointWorkerModule> imports() {
        return ImportsFor.<CheckpointWorkerModule>builder()
                .add(new ModuleImports<>(List.of(new CheckpointFoldModule.CheckpointFold()), CheckpointFoldModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new LoggedEventModule.LoggedEventCodec()), LoggedEventModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new LogHeaderModule.LogHeaderCodec()), LogHeaderModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new CheckpointModule.CheckpointCodec()), CheckpointModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<CheckpointWorkerModule> exports() { return new ExportsOf<>(INSTANCE, List.of()); }
}
