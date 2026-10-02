package hue.captains.singapura.js.homing.workspace.switcher;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.relgrid.RelGridStockCellsModule;

import java.util.List;

/**
 * The switcher table's relation: {@code WorkspaceRows} - the workspaces of one
 * kind, each a row, and last the new row; a cell a noun of its own, only a name
 * committing. A DOM module, though it touches no DOM itself: its cells do.
 */
public record WorkspaceRowsModule() implements DomModule<WorkspaceRowsModule> {

    public record WorkspaceRows() implements Exportable._Class<WorkspaceRowsModule> {}

    public static final WorkspaceRowsModule INSTANCE = new WorkspaceRowsModule();

    @Override
    public ImportsFor<WorkspaceRowsModule> imports() {
        return ImportsFor.<WorkspaceRowsModule>builder()
                .add(new ModuleImports<>(List.of(new RelGridStockCellsModule.RelGridTextCell()), RelGridStockCellsModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WorkspaceNameCellModule.WorkspaceNameCell()), WorkspaceNameCellModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<WorkspaceRowsModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new WorkspaceRows())); }
}
