package hue.captains.singapura.js.homing.workspace.demo;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.relgrid.RelGridStockCellsModule;
import hue.captains.singapura.js.homing.relgrid.protocol.RelGridProtocolModule;
import hue.captains.singapura.js.homing.reltree.RelTreeStockCellsModule;

import java.util.List;

/**
 * The demo's domain: a small book store and the two relations the grid and the
 * tree are given over it — the gallery's, taken as they are, since a demo
 * workspace does not depend on the gallery site. Domain code: it builds cells
 * from the family's stock cells and answers the tree's questions with the
 * protocol's own classes, and never names a component.
 *
 * <p>One store per page ({@code BooksStore.shared()}), so every books view in
 * the workspace shows the same books.</p>
 */
public record DemoRelationsModule() implements DomModule<DemoRelationsModule> {

    public record BooksStore()         implements Exportable._Constant<DemoRelationsModule> {}
    public record BooksRelation()      implements Exportable._Constant<DemoRelationsModule> {}
    public record ShelfTreeRelation()  implements Exportable._Constant<DemoRelationsModule> {}

    public static final DemoRelationsModule INSTANCE = new DemoRelationsModule();

    @Override
    public ImportsFor<DemoRelationsModule> imports() {
        return ImportsFor.<DemoRelationsModule>builder()
                .add(new ModuleImports<>(List.of(new RelGridStockCellsModule.RelGridTextCell()), RelGridStockCellsModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new RelTreeStockCellsModule.RelTreeTextCell()), RelTreeStockCellsModule.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new RelGridProtocolModule.RelTreeView(),
                        new RelGridProtocolModule.RelTreeUnfold(),
                        new RelGridProtocolModule.RelTreeFold()
                ), RelGridProtocolModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<DemoRelationsModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new BooksStore(), new BooksRelation(), new ShelfTreeRelation()));
    }
}
