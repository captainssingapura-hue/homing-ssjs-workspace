package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.codecs.WorkspaceLogCodecsModule;

import java.util.List;

/**
 * A WorkspaceState and a live workspace, both ways: the state laid out - the
 * grid as the log has it, the tabs back under their ids, kinds and titles -
 * and the workspace read back into a state, to be compared byte for byte.
 * Headless: it drives the workspace's components and touches no element.
 */
public record WorkspaceProjectionModule() implements DomModule<WorkspaceProjectionModule> {

    public record WorkspaceProjection() implements Exportable._Class<WorkspaceProjectionModule> {}

    public static final WorkspaceProjectionModule INSTANCE = new WorkspaceProjectionModule();

    @Override
    public ImportsFor<WorkspaceProjectionModule> imports() {
        return ImportsFor.<WorkspaceProjectionModule>builder()
                .add(new ModuleImports<>(List.of(new WorkspaceRecorderModule.WorkspaceRecorder()), WorkspaceRecorderModule.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new WorkspaceLogCodecsModule.Cell(), new WorkspaceLogCodecsModule.Split(), new WorkspaceLogCodecsModule.Track(),
                        new WorkspaceLogCodecsModule.Axis(), new WorkspaceLogCodecsModule.RegionId(), new WorkspaceLogCodecsModule.TabId(),
                        new WorkspaceLogCodecsModule.WidgetKind(), new WorkspaceLogCodecsModule.WidgetTitle(),
                        new WorkspaceLogCodecsModule.RegionState(), new WorkspaceLogCodecsModule.TabState(),
                        new WorkspaceLogCodecsModule.FloatId(), new WorkspaceLogCodecsModule.FloatState(),
                        new WorkspaceLogCodecsModule.WorkspaceState(), new WorkspaceLogCodecsModule.WorkspaceStateCodec()),
                        WorkspaceLogCodecsModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<WorkspaceProjectionModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new WorkspaceProjection())); }
}
