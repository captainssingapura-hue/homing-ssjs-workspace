package hue.captains.singapura.js.homing.workspace.log.fold;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.codecs.WorkspaceLogCodecsModule;

import java.util.List;

/** A workspace log's meaning: its events folded into the WorkspaceState they leave, as Java's WorkspaceFold folds them. */
public record WorkspaceFoldModule() implements DomModule<WorkspaceFoldModule> {

    public record WorkspaceFold() implements Exportable._Class<WorkspaceFoldModule> {}

    public static final WorkspaceFoldModule INSTANCE = new WorkspaceFoldModule();

    @Override
    public ImportsFor<WorkspaceFoldModule> imports() {
        return ImportsFor.<WorkspaceFoldModule>builder()
                .add(new ModuleImports<>(List.of(new LayoutAlgebraModule.LayoutAlgebra()), LayoutAlgebraModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkspaceLogCodecsModule.WorkspaceState(), new WorkspaceLogCodecsModule.RegionState(), new WorkspaceLogCodecsModule.FloatState(), new WorkspaceLogCodecsModule.TabState(), new WorkspaceLogCodecsModule.FoldedState(), new WorkspaceLogCodecsModule.EventSeq(), new WorkspaceLogCodecsModule.RegionId(), new WorkspaceLogCodecsModule.FloatId(), new WorkspaceLogCodecsModule.TabId(), new WorkspaceLogCodecsModule.Cell(), new WorkspaceLogCodecsModule.InRegion(), new WorkspaceLogCodecsModule.InFloat(), new WorkspaceLogCodecsModule.TabOpened(), new WorkspaceLogCodecsModule.TabBecame(), new WorkspaceLogCodecsModule.TabRenamed(), new WorkspaceLogCodecsModule.TabMoved(), new WorkspaceLogCodecsModule.TabShown(), new WorkspaceLogCodecsModule.TabClosed(), new WorkspaceLogCodecsModule.RegionParted(), new WorkspaceLogCodecsModule.RegionRemoved(), new WorkspaceLogCodecsModule.TracksChanged(), new WorkspaceLogCodecsModule.FloatOpened(), new WorkspaceLogCodecsModule.FloatMoved(), new WorkspaceLogCodecsModule.FloatResized(), new WorkspaceLogCodecsModule.FloatRaised(), new WorkspaceLogCodecsModule.FloatClosed()), WorkspaceLogCodecsModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<WorkspaceFoldModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new WorkspaceFold())); }
}
