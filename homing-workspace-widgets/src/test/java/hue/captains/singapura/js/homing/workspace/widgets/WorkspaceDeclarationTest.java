package hue.captains.singapura.js.homing.workspace.widgets;

import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.parties.PartyType;
import hue.captains.singapura.js.homing.workspace.widgets.books.BookBrowserDeclaration;
import hue.captains.singapura.js.homing.workspace.widgets.books.BookBrowserSecretaryModule;
import hue.captains.singapura.js.homing.workspace.widgets.books.BookJumbotronDeclaration;
import hue.captains.singapura.js.homing.workspace.widgets.books.BookSelection;
import hue.captains.singapura.js.homing.workspace.widgets.books.BooksGridDeclaration;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A workspace's root parties, resolved at type level from its kinds: one of each
 * type, each with the secretary at its root - the workspace's, else the type's
 * default - and what does not hold together refused at build time.
 */
class WorkspaceDeclarationTest {

    record Books(List<WidgetDeclaration<?>> kinds, Map<String, ModuleImports<?>> secretaries) implements WorkspaceDeclaration {
        Books(WidgetDeclaration<?>... kinds) { this(List.of(kinds), Map.of()); }
        @Override public String name() { return "books"; }
    }

    /** A type no page serves, and one with no secretary: as a widget might declare them by mistake. */
    sealed interface Chimes { record Ring() implements Chimes {} }

    record Chiming(PartyType<?> type) implements WidgetDeclaration<NoParams> {
        @Override public String kind() { return "chiming"; }
        @Override public Class<NoParams> paramsType() { return NoParams.class; }
        @Override public WidgetQuery<NoParams> query() { return new NoParams.Query(); }
        @Override public ModuleImports<?> constructs() { return BooksGridDeclaration.INSTANCE.constructs(); }
        @Override public List<PartyType<?>> parties() { return List.of(type); }
    }

    @Test
    void oneRootPartyOfEachType_withItsDefaultSecretary() {
        var roots = new Books(BooksGridDeclaration.INSTANCE, BookJumbotronDeclaration.INSTANCE, BookBrowserDeclaration.INSTANCE).rootParties();
        assertEquals(1, roots.size(), "three kinds, one type");
        assertEquals(BookSelection.TYPE, roots.get(0).type());
        assertEquals("BookSelectionSecretary", PartyType.exportName(roots.get(0).secretary()));
        assertEquals(List.of(), new Books(new NoPartiesKind()).rootParties(), "kinds that join nothing: no root");
    }

    @Test
    void theWorkspacePutsItsOwnSecretary_inPlaceOfTheDefault() {
        var own = new ModuleImports<>(List.of(new BookBrowserSecretaryModule.BookBrowserSecretary()), BookBrowserSecretaryModule.INSTANCE);
        var roots = new Books(List.of(BooksGridDeclaration.INSTANCE), Map.of("book-selection", own)).rootParties();
        assertEquals("BookBrowserSecretary", PartyType.exportName(roots.get(0).secretary()));
    }

    @Test
    void whatDoesNotHoldTogether_failsTheBuild() {
        var unserved = new PartyType<>("chimes", Chimes.class);
        var e = assertThrows(IllegalStateException.class, () -> new Books(new Chiming(unserved)).rootParties());
        assertTrue(e.getMessage().contains("the type chimes is not served on a page"), e.getMessage());
        assertTrue(e.getMessage().contains("the type chimes has no secretary for its root"), e.getMessage());
        var own = new ModuleImports<>(List.of(new BookBrowserSecretaryModule.BookBrowserSecretary()), BookBrowserSecretaryModule.INSTANCE);
        e = assertThrows(IllegalStateException.class, () -> new Books(List.of(BooksGridDeclaration.INSTANCE), Map.of("chimes", own)).rootParties());
        assertTrue(e.getMessage().contains("a secretary put at the root of chimes, which no kind joins"), e.getMessage());
        e = assertThrows(IllegalStateException.class, () -> new Books(BooksGridDeclaration.INSTANCE, BooksGridDeclaration.INSTANCE).rootParties());
        assertTrue(e.getMessage().contains("two kinds named books-grid"), e.getMessage());
    }

    @Test
    void theManifest_isTheKindsAndTheRoots() {
        String js = WorkspaceManifest.js("BOOKS", new Books(BooksGridDeclaration.INSTANCE, BookJumbotronDeclaration.INSTANCE));
        assertTrue(js.startsWith("const BOOKS = Object.freeze({ name: \"books\", kinds: Object.freeze({ \"books-grid\": Object.freeze({ Widget: BooksGrid, title: \"Books grid\", parties: Object.freeze([BOOK_SELECTION]) }), "), js);
        assertTrue(js.endsWith("parties: Object.freeze([Object.freeze({ type: BOOK_SELECTION, secretary: BookSelectionSecretary })]) });"), js);
    }

    record NoPartiesKind() implements WidgetDeclaration<NoParams> {
        @Override public String kind() { return "quiet"; }
        @Override public Class<NoParams> paramsType() { return NoParams.class; }
        @Override public WidgetQuery<NoParams> query() { return new NoParams.Query(); }
        @Override public ModuleImports<?> constructs() { return BookJumbotronDeclaration.INSTANCE.constructs(); }
    }
}
