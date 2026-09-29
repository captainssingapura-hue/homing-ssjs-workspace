package hue.captains.singapura.js.homing.workspace.groups.core.models;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A workspace's arrangements: each checked whole against the workspace - the
 * kinds it offers - and against its placement - each widget placed, once, nothing
 * placed that it does not open; and at most one an engine, each typed as its
 * engine's placement for the engine that reads it.
 */
class ArrangementTest {

    /** A workspace as an arrangement sees it: the books, their jumbotron and browser, and the switcher. */
    record Books() implements WorkspaceSpec {
        @Override public WorkspaceKind workspaceKind() { return WorkspaceKind.of("books"); }
        @Override public Set<WidgetKind> widgetKinds() {
            return Set.of(WidgetKind.of("books-grid"), WidgetKind.of("book-jumbotron"), WidgetKind.of("book-browser"), WidgetKind.of("workspace-switcher"));
        }
    }

    static final Books BOOKS = new Books();

    static final ArrangedWidget[] WIDGETS = {
            ArrangedWidget.of("switcher", "workspace-switcher"),
            ArrangedWidget.of("grid", "books-grid", Map.of("numbers", "on", "columns", "title,rating")),
            ArrangedWidget.of("browser", "book-browser"),
            ArrangedWidget.of("jumbotron", "book-jumbotron")};

    static final Arrangement<Books, SplitGrid> ON_THE_GRID = Arrangement.of(BOOKS, PlacementsTest.GRID, WIDGETS);

    static final Arrangement<Books, OnePane> IN_ONE_PANE = Arrangement.of(BOOKS, OnePane.showing("browser", "switcher"),
            ArrangedWidget.of("browser", "book-browser"), ArrangedWidget.of("switcher", "workspace-switcher"));

    @Test
    void anArrangementOpensWidgetsTheWorkspaceOffers_andItsPlacementPutsEach() {
        assertEquals(SplitGrid.ENGINE, ON_THE_GRID.engine());
        assertEquals(List.of("switcher", "grid", "browser", "jumbotron"), ON_THE_GRID.byRef().keySet().stream().map(WidgetRef::value).toList());
        var grid = ON_THE_GRID.widget(WidgetRef.of("grid")).orElseThrow();
        assertEquals("books-grid", grid.kind().value());
        assertEquals(List.of("columns", "numbers"), List.copyOf(grid.params().keySet()), "params in the order of their keys");
        assertTrue(ON_THE_GRID.widget(WidgetRef.of("nothing")).isEmpty());
    }

    @Test
    void aWidgetOfAKindTheWorkspaceDoesNotOffer_isRefused() {
        var e = assertThrows(IllegalArgumentException.class, () -> Arrangement.of(BOOKS, OnePane.showing("lamp"), ArrangedWidget.of("lamp", "steward-lamp")));
        assertTrue(e.getMessage().contains("steward-lamp, which the workspace does not offer"), e.getMessage());
    }

    @Test
    void eachWidgetIsPlacedOnce_andNothingPlacedThatItDoesNotOpen() {
        var unplaced = assertThrows(IllegalArgumentException.class, () -> Arrangement.of(BOOKS, OnePane.showing("switcher"),
                ArrangedWidget.of("switcher", "workspace-switcher"), ArrangedWidget.of("grid", "books-grid")));
        assertTrue(unplaced.getMessage().contains("opens grid and places it nowhere"), unplaced.getMessage());
        var unknown = assertThrows(IllegalArgumentException.class, () -> Arrangement.of(BOOKS, OnePane.showing("switcher", "ghost"),
                ArrangedWidget.of("switcher", "workspace-switcher")));
        assertTrue(unknown.getMessage().contains("places ghost, which it does not open"), unknown.getMessage());
        var twice = assertThrows(IllegalArgumentException.class, () -> Arrangement.of(BOOKS, OnePane.showing("switcher"),
                ArrangedWidget.of("switcher", "workspace-switcher"), ArrangedWidget.of("switcher", "books-grid")));
        assertTrue(twice.getMessage().contains("names two widgets switcher"), twice.getMessage());
    }

    @Test
    void aWidgetsParams_haveKeys() {
        assertThrows(IllegalArgumentException.class, () -> ArrangedWidget.of("grid", "books-grid", Map.of("", "on")));
        assertThrows(IllegalArgumentException.class, () -> ArrangedWidget.of("grid", "Books Grid"));
    }

    @Test
    void aWorkspaceHasOneArrangementAnEngine_eachReadAsItsEnginesPlacement() {
        var all = WorkspaceArrangements.of(BOOKS, ON_THE_GRID, IN_ONE_PANE);
        assertEquals(List.of(SplitGrid.ENGINE, OnePane.ENGINE), all.engines());
        SplitGrid grid = all.forEngine(SplitGrid.ENGINE, SplitGrid.class).orElseThrow().placement();
        assertEquals(3, grid.regions().size());
        assertEquals("browser", all.forEngine(OnePane.ENGINE, OnePane.class).orElseThrow().placement().shown().value());
        assertTrue(all.forEngine(PlacementEngine.of("magnetic-tiles")).isEmpty(), "an engine it has none for: nothing open");
        assertTrue(all.forEngine(SplitGrid.ENGINE, OnePane.class).isEmpty(), "read as another engine's placement: none");
        assertThrows(IllegalArgumentException.class, () -> WorkspaceArrangements.of(BOOKS, ON_THE_GRID, ON_THE_GRID));
    }

    @Test
    void aWorkspacesArrangementsAreItsOwn() {
        record Monitors() implements WorkspaceSpec {
            @Override public WorkspaceKind workspaceKind() { return WorkspaceKind.of("monitors"); }
            @Override public Set<WidgetKind> widgetKinds() { return Set.of(WidgetKind.of("workspace-switcher")); }
        }
        var theirs = Arrangement.of(new Monitors(), OnePane.showing("switcher"), ArrangedWidget.of("switcher", "workspace-switcher"));
        @SuppressWarnings({"unchecked", "rawtypes"})
        Arrangement<Books, ?> smuggled = (Arrangement) theirs;
        assertThrows(IllegalArgumentException.class, () -> WorkspaceArrangements.of(BOOKS, smuggled));
    }
}
