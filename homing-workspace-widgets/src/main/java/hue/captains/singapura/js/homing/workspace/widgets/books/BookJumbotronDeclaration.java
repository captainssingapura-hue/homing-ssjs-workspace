package hue.captains.singapura.js.homing.workspace.widgets.books;

import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.parties.PartyType;
import hue.captains.singapura.js.homing.workspace.widgets.NoParams;
import hue.captains.singapura.js.homing.workspace.widgets.WidgetDeclaration;
import hue.captains.singapura.js.homing.workspace.widgets.WidgetQuery;

import java.util.List;

/** The book jumbotron, declared: {@code book-jumbotron}, with no params, joining the book selection party. */
public record BookJumbotronDeclaration() implements WidgetDeclaration<NoParams> {

    public static final BookJumbotronDeclaration INSTANCE = new BookJumbotronDeclaration();

    @Override public String kind() { return "book-jumbotron"; }
    @Override public Class<NoParams> paramsType() { return NoParams.class; }
    @Override public WidgetQuery<NoParams> query() { return new NoParams.Query(); }
    @Override public List<PartyType<?>> parties() { return List.of(BookSelection.TYPE); }

    @Override
    public ModuleImports<?> constructs() {
        return new ModuleImports<>(List.of(new BookJumbotronModule.BookJumbotron()), BookJumbotronModule.INSTANCE);
    }
}
