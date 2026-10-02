package hue.captains.singapura.js.homing.workspace.log.store;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.log.js.LogIdsModule;
import hue.captains.singapura.js.homing.workspace.log.js.WorkspaceEventModule;
import hue.captains.singapura.js.homing.workspace.log.js.LoggedEventModule;
import hue.captains.singapura.js.homing.workspace.log.js.LogHeaderModule;
import hue.captains.singapura.js.homing.workspace.log.js.SetAsideLogModule;
import hue.captains.singapura.js.homing.workspace.log.js.CheckpointModule;

import java.util.List;

/** One workspace's log in the browser: typed events in, their codecs' wire form kept, typed events out. */
public record WorkspaceLogStoreModule() implements DomModule<WorkspaceLogStoreModule> {

    public record WorkspaceLogStore() implements Exportable._Class<WorkspaceLogStoreModule> {}

    public static final WorkspaceLogStoreModule INSTANCE = new WorkspaceLogStoreModule();

    @Override
    public ImportsFor<WorkspaceLogStoreModule> imports() {
        return ImportsFor.<WorkspaceLogStoreModule>builder()
                .add(new ModuleImports<>(List.of(new LogIdsModule.EventSeq()), LogIdsModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkspaceEventModule.WorkspaceEvent(), new WorkspaceEventModule.WorkspaceEventCodec()), WorkspaceEventModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new LoggedEventModule.LoggedEvent(), new LoggedEventModule.LoggedEventCodec()), LoggedEventModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new LogHeaderModule.LogHeader()), LogHeaderModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new SetAsideLogModule.SetAsideLog(), new SetAsideLogModule.SetAsideLogCodec()), SetAsideLogModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new CheckpointModule.Checkpoint(), new CheckpointModule.CheckpointCodec()), CheckpointModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<WorkspaceLogStoreModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new WorkspaceLogStore())); }
}
