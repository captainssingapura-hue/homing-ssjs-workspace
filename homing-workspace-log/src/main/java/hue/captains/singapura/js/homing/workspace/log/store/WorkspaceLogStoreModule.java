package hue.captains.singapura.js.homing.workspace.log.store;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.codecs.WorkspaceLogCodecsModule;

import java.util.List;

/** One workspace's log in the browser: typed events in, their codecs' wire form kept, typed events out. */
public record WorkspaceLogStoreModule() implements DomModule<WorkspaceLogStoreModule> {

    public record WorkspaceLogStore() implements Exportable._Class<WorkspaceLogStoreModule> {}

    public static final WorkspaceLogStoreModule INSTANCE = new WorkspaceLogStoreModule();

    @Override
    public ImportsFor<WorkspaceLogStoreModule> imports() {
        return ImportsFor.<WorkspaceLogStoreModule>builder()
                .add(new ModuleImports<>(List.of(new WorkspaceLogCodecsModule.LogHeader(), new WorkspaceLogCodecsModule.WorkspaceEvent(), new WorkspaceLogCodecsModule.WorkspaceEventCodec(), new WorkspaceLogCodecsModule.LoggedEvent(), new WorkspaceLogCodecsModule.LoggedEventCodec(), new WorkspaceLogCodecsModule.EventSeq()), WorkspaceLogCodecsModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<WorkspaceLogStoreModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new WorkspaceLogStore())); }
}
