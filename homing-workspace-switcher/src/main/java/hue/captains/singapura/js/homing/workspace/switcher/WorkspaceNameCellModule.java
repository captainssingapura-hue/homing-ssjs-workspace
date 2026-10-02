package hue.captains.singapura.js.homing.workspace.switcher;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.relgrid.RelGridStockCellsModule;

import java.util.List;

/**
 * A workspace's name in the switcher's table: {@code WorkspaceNameCell} - the
 * relation grid's stock text cell, editable only when armed (F2, Rename), so that
 * Enter on a workspace keeps opening it.
 */
public record WorkspaceNameCellModule() implements DomModule<WorkspaceNameCellModule> {

    public record WorkspaceNameCell() implements Exportable._Class<WorkspaceNameCellModule> {}

    public static final WorkspaceNameCellModule INSTANCE = new WorkspaceNameCellModule();

    @Override
    public ImportsFor<WorkspaceNameCellModule> imports() {
        return ImportsFor.<WorkspaceNameCellModule>builder()
                .add(new ModuleImports<>(List.of(new RelGridStockCellsModule.RelGridTextCell()), RelGridStockCellsModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<WorkspaceNameCellModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new WorkspaceNameCell())); }
}
