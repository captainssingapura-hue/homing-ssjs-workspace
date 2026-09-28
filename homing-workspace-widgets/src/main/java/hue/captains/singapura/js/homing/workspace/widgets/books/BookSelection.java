package hue.captains.singapura.js.homing.workspace.widgets.books;

import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.parties.PartyType;

import java.util.List;

/**
 * What a book selection party carries: the choosing of a book, and the fact of
 * it. A member does - {@link Select}, {@link Clear}, {@link CurrentRequested};
 * the party says - {@link Selected}, {@link Cleared}. A book travels as what a
 * member that holds no books needs to show it: its id, its title, its author.
 */
public sealed interface BookSelection {

    /** A member chose this book. */
    record Select(String id, String title, String author) implements BookSelection {}

    /** A member chose none. */
    record Clear() implements BookSelection {}

    /** A member asks what is chosen - one that joins late - and is answered alone. */
    record CurrentRequested() implements BookSelection {}

    /** The party says: this book is chosen. */
    record Selected(String id, String title, String author) implements BookSelection {}

    /** The party says: no book is chosen. */
    record Cleared() implements BookSelection {}

    /**
     * The type: {@code book-selection}, its identity on a page - its constant
     * served as {@code BOOK_SELECTION}, and a root instance's secretary, unless
     * a workspace puts its own, {@code BookSelectionSecretary}.
     */
    PartyType<BookSelection> TYPE = new PartyType<>("book-selection", BookSelection.class)
            .servedFrom(new ModuleImports<>(List.of(new BookSelectionModule.BOOK_SELECTION()), BookSelectionModule.INSTANCE))
            .withSecretary(new ModuleImports<>(List.of(new BookSelectionSecretaryModule.BookSelectionSecretary()), BookSelectionSecretaryModule.INSTANCE));
}
