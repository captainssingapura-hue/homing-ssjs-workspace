package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.log.js.LogIdsModule;
import hue.captains.singapura.js.homing.workspace.log.js.ScaledModule;
import hue.captains.singapura.js.homing.workspace.log.js.HostModule;
import hue.captains.singapura.js.homing.workspace.log.js.TabEventModule;
import hue.captains.singapura.js.homing.workspace.log.js.RegionEventModule;
import hue.captains.singapura.js.homing.workspace.log.js.FloatEventModule;

import java.util.List;

/**
 * What the workspace's components report, written into its log as the
 * {@code WorkspaceEvent} each report is — the classes generated from the Java
 * declarations, never an event spelled by hand. Headless.
 */
public record WorkspaceRecorderModule() implements DomModule<WorkspaceRecorderModule> {

    public record WorkspaceRecorder() implements Exportable._Class<WorkspaceRecorderModule> {}

    public static final WorkspaceRecorderModule INSTANCE = new WorkspaceRecorderModule();

    @Override
    public ImportsFor<WorkspaceRecorderModule> imports() {
        return ImportsFor.<WorkspaceRecorderModule>builder()
                .add(new ModuleImports<>(List.of(new LogIdsModule.TabId(), new LogIdsModule.RegionId(), new LogIdsModule.FloatId(), new LogIdsModule.WidgetKind(), new LogIdsModule.WidgetTitle(), new LogIdsModule.SplitPath()), LogIdsModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new ScaledModule.Scaled()), ScaledModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new HostModule.InRegion(), new HostModule.InFloat()), HostModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new TabEventModule.TabOpened(), new TabEventModule.TabRenamed(), new TabEventModule.TabMoved(), new TabEventModule.TabShown(), new TabEventModule.TabClosed()), TabEventModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new RegionEventModule.Side(), new RegionEventModule.RegionParted(), new RegionEventModule.RegionRemoved(), new RegionEventModule.TracksChanged()), RegionEventModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new FloatEventModule.FloatOpened(), new FloatEventModule.FloatMoved(), new FloatEventModule.FloatResized(), new FloatEventModule.FloatRaised(), new FloatEventModule.FloatClosed()), FloatEventModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<WorkspaceRecorderModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new WorkspaceRecorder())); }
}
