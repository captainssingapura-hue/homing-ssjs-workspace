package hue.captains.singapura.js.homing.workspace.widgets.books;

import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.parties.PartyType;
import hue.captains.singapura.js.homing.workspace.widgets.NoParams;
import hue.captains.singapura.js.homing.workspace.widgets.WidgetDeclaration;
import hue.captains.singapura.js.homing.workspace.widgets.WidgetQuery;

import java.util.List;
import java.util.stream.Stream;

/**
 * The book browser, declared: {@code book-browser}, with no params. The
 * parties it joins are its subordinates', the union of theirs - derived from
 * their declarations, never written again here.
 */
public record BookBrowserDeclaration() implements WidgetDeclaration<NoParams> {

    public static final BookBrowserDeclaration INSTANCE = new BookBrowserDeclaration();

    @Override public String kind() { return "book-browser"; }
    @Override public Class<NoParams> paramsType() { return NoParams.class; }
    @Override public WidgetQuery<NoParams> query() { return new NoParams.Query(); }

    /** The union of its subordinates': the grid, made at its defaults, and the jumbotron. */
    @Override
    public List<PartyType<?>> parties(NoParams params) {
        return Stream.of(BooksGridDeclaration.INSTANCE.parties(BooksGridDeclaration.Params.DEFAULT),
                         BookJumbotronDeclaration.INSTANCE.parties(NoParams.INSTANCE))
                .flatMap(List::stream).distinct().toList();
    }

    @Override
    public ModuleImports<?> constructs() {
        return new ModuleImports<>(List.of(new BookBrowserModule.BookBrowser()), BookBrowserModule.INSTANCE);
    }
}
