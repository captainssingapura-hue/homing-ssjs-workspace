package hue.captains.singapura.js.homing.workspace.demo;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.relgrid.RelGridModule;
import hue.captains.singapura.js.homing.reltree.RelTreeModule;

import java.util.List;

/**
 * Three views for the demo workspace, each a widget and nothing more: handed a
 * branch, they give back {@code { root }}, and the room is the party's member.
 *
 * <p>The books as a relation grid and the shelves as a relation tree are the
 * native world — their keys are their own, and they take the focus when the
 * room says it holds the keys; the room lets an unwanted Escape out. The
 * picture has no native control and zooms by keys the room hands on.</p>
 */
public record DemoViewsModule() implements DomModule<DemoViewsModule> {

    public static final DemoViewsModule INSTANCE = new DemoViewsModule();

    /** The books, as a relation grid over the page's store. */
    public record BooksWidget() implements Exportable._Class<DemoViewsModule> {}

    /** The same books as shelf → book, in the relation tree. */
    public record ShelvesWidget() implements Exportable._Class<DemoViewsModule> {}

    /** A plate drawn in the design's words, zoomed by + − 0. */
    public record PictureWidget() implements Exportable._Class<DemoViewsModule> {}

    @Override
    public ImportsFor<DemoViewsModule> imports() {
        return ImportsFor.<DemoViewsModule>builder()
                .add(new ModuleImports<>(List.of(new RelGridModule.RelGrid()), RelGridModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new RelTreeModule.RelTree()), RelTreeModule.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new DemoRelationsModule.BooksStore(),
                        new DemoRelationsModule.BooksRelation(),
                        new DemoRelationsModule.ShelfTreeRelation()), DemoRelationsModule.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new DemoWidgetStyles.dw_fill(), new DemoWidgetStyles.dw_host(),
                        new DemoWidgetStyles.dw_picture(), new DemoWidgetStyles.dw_plate(),
                        new DemoWidgetStyles.dw_plate_sun(), new DemoWidgetStyles.dw_plate_far(),
                        new DemoWidgetStyles.dw_plate_near(), new DemoWidgetStyles.dw_plate_ground(),
                        new DemoWidgetStyles.dw_picture_note()), DemoWidgetStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<DemoViewsModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new BooksWidget(), new ShelvesWidget(), new PictureWidget()));
    }
}
