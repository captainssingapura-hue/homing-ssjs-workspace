package hue.captains.singapura.js.homing.workspace.widgets.books;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.relgrid.RelGridStockCellsModule;

import java.util.List;

/** The books widget's own domain: a store of twelve books, and the relation the grid is given over it. */
public record BooksModule() implements DomModule<BooksModule> {

    public record BooksStore()    implements Exportable._Class<BooksModule> {}
    public record BooksRelation() implements Exportable._Class<BooksModule> {}

    public static final BooksModule INSTANCE = new BooksModule();

    @Override
    public ImportsFor<BooksModule> imports() {
        return ImportsFor.<BooksModule>builder()
                .add(new ModuleImports<>(List.of(new RelGridStockCellsModule.RelGridTextCell()), RelGridStockCellsModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<BooksModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new BooksStore(), new BooksRelation())); }
}
