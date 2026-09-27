package hue.captains.singapura.js.homing.workspace.log.store;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.log.js.LogKeyModule;
import hue.captains.singapura.js.homing.workspace.log.js.WriteLockModule;

import java.util.List;

/** One writer per log: the browser's Web Locks, said as the WriteLock declared in Java. */
public record WorkspaceWriteLockModule() implements DomModule<WorkspaceWriteLockModule> {

    public record WorkspaceWriteLock() implements Exportable._Class<WorkspaceWriteLockModule> {}

    public static final WorkspaceWriteLockModule INSTANCE = new WorkspaceWriteLockModule();

    @Override
    public ImportsFor<WorkspaceWriteLockModule> imports() {
        return ImportsFor.<WorkspaceWriteLockModule>builder()
                .add(new ModuleImports<>(List.of(new LogKeyModule.LogKey()), LogKeyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WriteLockModule.WriteLock(), new WriteLockModule.Held()), WriteLockModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<WorkspaceWriteLockModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new WorkspaceWriteLock())); }
}
