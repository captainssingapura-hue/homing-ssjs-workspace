package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.log.js.LogIdsModule;
import hue.captains.singapura.js.homing.workspace.log.js.LayoutModule;
import hue.captains.singapura.js.homing.workspace.log.js.GridStateModule;

import java.util.List;

/**
 * The split grid's layer of a WorkspaceState - a GridState - and a live workspace, both ways: the state laid out - the
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
                .add(new ModuleImports<>(List.of(new LogIdsModule.RegionId(), new LogIdsModule.TabId(), new LogIdsModule.WidgetKind(), new LogIdsModule.WidgetTitle(), new LogIdsModule.FloatId()), LogIdsModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new LayoutModule.Cell(), new LayoutModule.Split(), new LayoutModule.Track(), new LayoutModule.Axis()), LayoutModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new GridStateModule.RegionState(), new GridStateModule.TabState(), new GridStateModule.FloatState(), new GridStateModule.GridState(), new GridStateModule.GridStateCodec()), GridStateModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<WorkspaceProjectionModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new WorkspaceProjection())); }
}
