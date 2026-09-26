package hue.captains.singapura.js.homing.workspace.log.fold;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.codecs.WorkspaceLogCodecsModule;

import java.util.List;

/** The grid's algebra on the log's Layout, exact to the millionth: Java's LayoutAlgebra, transcribed. */
public record LayoutAlgebraModule() implements DomModule<LayoutAlgebraModule> {

    public record LayoutAlgebra() implements Exportable._Class<LayoutAlgebraModule> {}

    public static final LayoutAlgebraModule INSTANCE = new LayoutAlgebraModule();

    @Override
    public ImportsFor<LayoutAlgebraModule> imports() {
        return ImportsFor.<LayoutAlgebraModule>builder()
                .add(new ModuleImports<>(List.of(new ExactShareModule.ExactShare()), ExactShareModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkspaceLogCodecsModule.Cell(), new WorkspaceLogCodecsModule.Split(), new WorkspaceLogCodecsModule.Track(), new WorkspaceLogCodecsModule.Scaled(), new WorkspaceLogCodecsModule.RegionId(), new WorkspaceLogCodecsModule.Side(), new WorkspaceLogCodecsModule.Axis()), WorkspaceLogCodecsModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<LayoutAlgebraModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new LayoutAlgebra())); }
}
