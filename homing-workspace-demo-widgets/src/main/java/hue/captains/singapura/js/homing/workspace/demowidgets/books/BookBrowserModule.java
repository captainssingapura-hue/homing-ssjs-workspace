package hue.captains.singapura.js.homing.workspace.demowidgets.books;

import hue.captains.singapura.js.homing.component.keyboard.FocusPartyModule;
import hue.captains.singapura.js.homing.component.keyboard.focusParties;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.js.DomOpsPartyModule;
import hue.captains.singapura.js.homing.core.js.domOpsParties;
import hue.captains.singapura.js.homing.workspace.parties.MessagingPartyModule;
import hue.captains.singapura.js.homing.workspace.widgets.SelfContainedWidget;

import java.util.List;

/**
 * The books and the chosen one: {@code new BookBrowser(container, params)} -
 * a composed widget over the books grid and the book jumbotron, their host,
 * where they meet in a book selection party of its own: a scope linked to the
 * party it is given, its secretary keeping the edge.
 */
public record BookBrowserModule() implements DomModule<BookBrowserModule> {

    public record BookBrowser() implements SelfContainedWidget<BookBrowserModule> {
        @Override public String summary() { return "The books in the grid and the chosen one big, composed: they meet in a book selection scope of its own, linked to the party it joins."; }
    }

    public static final BookBrowserModule INSTANCE = new BookBrowserModule();

    @Override
    public ImportsFor<BookBrowserModule> imports() {
        return ImportsFor.<BookBrowserModule>builder()
                .add(new ModuleImports<>(List.of(new domOpsParties()), DomOpsPartyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new focusParties()), FocusPartyModule.INSTANCE))
                // its subordinates
                .add(new ModuleImports<>(List.of(new BooksGridModule.BooksGrid()), BooksGridModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new BookJumbotronModule.BookJumbotron()), BookJumbotronModule.INSTANCE))
                // its scope: a party of the type they meet in, and the secretary that keeps its edge
                .add(new ModuleImports<>(List.of(new MessagingPartyModule.MessagingParty()), MessagingPartyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new BookSelectionModule.BOOK_SELECTION()), BookSelectionModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new BookBrowserSecretaryModule.BookBrowserSecretary()), BookBrowserSecretaryModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new BookStyles.bb_split(), new BookStyles.bb_grid(), new BookStyles.bb_chosen()), BookStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<BookBrowserModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new BookBrowser())); }
}
