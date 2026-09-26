package hue.captains.singapura.js.homing.workspace.log.fold;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.log.js.LogIdsModule;
import hue.captains.singapura.js.homing.workspace.log.js.HostModule;
import hue.captains.singapura.js.homing.workspace.log.js.LayoutModule;
import hue.captains.singapura.js.homing.workspace.log.js.TabEventModule;
import hue.captains.singapura.js.homing.workspace.log.js.RegionEventModule;
import hue.captains.singapura.js.homing.workspace.log.js.FloatEventModule;
import hue.captains.singapura.js.homing.workspace.log.js.WorkspaceStateModule;
import hue.captains.singapura.js.homing.workspace.log.js.FoldedStateModule;

import java.util.List;

/** A workspace log's meaning: its events folded into the WorkspaceState they leave, as Java's WorkspaceFold folds them. */
public record WorkspaceFoldModule() implements DomModule<WorkspaceFoldModule> {

    public record WorkspaceFold() implements Exportable._Class<WorkspaceFoldModule> {}

    public static final WorkspaceFoldModule INSTANCE = new WorkspaceFoldModule();

    @Override
    public ImportsFor<WorkspaceFoldModule> imports() {
        return ImportsFor.<WorkspaceFoldModule>builder()
                .add(new ModuleImports<>(List.of(new LayoutAlgebraModule.LayoutAlgebra()), LayoutAlgebraModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new LogIdsModule.EventSeq(), new LogIdsModule.RegionId(), new LogIdsModule.FloatId(), new LogIdsModule.TabId()), LogIdsModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new HostModule.InRegion(), new HostModule.InFloat()), HostModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new LayoutModule.Cell()), LayoutModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new TabEventModule.TabOpened(), new TabEventModule.TabBecame(), new TabEventModule.TabRenamed(), new TabEventModule.TabMoved(), new TabEventModule.TabShown(), new TabEventModule.TabClosed()), TabEventModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new RegionEventModule.RegionParted(), new RegionEventModule.RegionRemoved(), new RegionEventModule.TracksChanged()), RegionEventModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new FloatEventModule.FloatOpened(), new FloatEventModule.FloatMoved(), new FloatEventModule.FloatResized(), new FloatEventModule.FloatRaised(), new FloatEventModule.FloatClosed()), FloatEventModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkspaceStateModule.WorkspaceState(), new WorkspaceStateModule.RegionState(), new WorkspaceStateModule.FloatState(), new WorkspaceStateModule.TabState()), WorkspaceStateModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new FoldedStateModule.FoldedState()), FoldedStateModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<WorkspaceFoldModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new WorkspaceFold())); }
}
