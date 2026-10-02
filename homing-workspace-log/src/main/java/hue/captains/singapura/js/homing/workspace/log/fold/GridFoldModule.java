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
import hue.captains.singapura.js.homing.workspace.log.js.GridStateModule;

import java.util.List;

/** The split grid's layer of the fold: a grid event on the GridState, as Java's GridFold folds it. */
public record GridFoldModule() implements DomModule<GridFoldModule> {

    public record GridFold() implements Exportable._Class<GridFoldModule> {}

    public static final GridFoldModule INSTANCE = new GridFoldModule();

    @Override
    public ImportsFor<GridFoldModule> imports() {
        return ImportsFor.<GridFoldModule>builder()
                .add(new ModuleImports<>(List.of(new LayoutAlgebraModule.LayoutAlgebra()), LayoutAlgebraModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new LogIdsModule.RegionId(), new LogIdsModule.FloatId(), new LogIdsModule.TabId()), LogIdsModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new HostModule.InRegion(), new HostModule.InFloat()), HostModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new LayoutModule.Cell()), LayoutModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new TabEventModule.TabOpened(), new TabEventModule.TabBecame(), new TabEventModule.TabRenamed(), new TabEventModule.TabMoved(), new TabEventModule.TabShown(), new TabEventModule.TabClosed()), TabEventModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new RegionEventModule.RegionParted(), new RegionEventModule.RegionRemoved(), new RegionEventModule.TracksChanged()), RegionEventModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new FloatEventModule.FloatOpened(), new FloatEventModule.FloatMoved(), new FloatEventModule.FloatResized(), new FloatEventModule.FloatRaised(), new FloatEventModule.FloatClosed()), FloatEventModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new GridStateModule.GridState(), new GridStateModule.RegionState(), new GridStateModule.FloatState(), new GridStateModule.TabState()), GridStateModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<GridFoldModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new GridFold())); }
}
