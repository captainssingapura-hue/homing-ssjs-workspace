package hue.captains.singapura.js.homing.workspace.log.fold;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.log.js.LogIdsModule;
import hue.captains.singapura.js.homing.workspace.log.js.ScaledModule;
import hue.captains.singapura.js.homing.workspace.log.js.LayoutModule;
import hue.captains.singapura.js.homing.workspace.log.js.RegionEventModule;

import java.util.List;

/** The grid's algebra on the log's Layout, exact to the millionth: Java's LayoutAlgebra, transcribed. */
public record LayoutAlgebraModule() implements DomModule<LayoutAlgebraModule> {

    public record LayoutAlgebra() implements Exportable._Class<LayoutAlgebraModule> {}

    public static final LayoutAlgebraModule INSTANCE = new LayoutAlgebraModule();

    @Override
    public ImportsFor<LayoutAlgebraModule> imports() {
        return ImportsFor.<LayoutAlgebraModule>builder()
                .add(new ModuleImports<>(List.of(new ExactShareModule.ExactShare()), ExactShareModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new LogIdsModule.RegionId()), LogIdsModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new ScaledModule.Scaled()), ScaledModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new LayoutModule.Cell(), new LayoutModule.Split(), new LayoutModule.Track(), new LayoutModule.Axis()), LayoutModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new RegionEventModule.Side()), RegionEventModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<LayoutAlgebraModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new LayoutAlgebra())); }
}
