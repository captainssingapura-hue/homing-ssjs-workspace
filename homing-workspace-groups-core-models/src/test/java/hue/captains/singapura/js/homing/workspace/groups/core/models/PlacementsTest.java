package hue.captains.singapura.js.homing.workspace.groups.core.models;

import hue.captains.singapura.js.homing.workspace.groups.core.models.SplitGrid.Part;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The engines' placements: the split grid's frame, its regions in the grid's
 * order, each region's tabs and the one it shows; one pane's held and shown.
 */
class PlacementsTest {

    /** The switcher on the left; on the right the books above, the chosen one below it. */
    static final SplitGrid GRID = SplitGrid.of(SplitGrid.row(
            Part.of(SplitGrid.region("kinds", "switcher"), 1),
            Part.of(SplitGrid.column(SplitGrid.region("books", "grid", "browser").showing("browser"), SplitGrid.region("chosen", "jumbotron")), 2)));

    private static List<String> names(List<WidgetRef> refs) { return refs.stream().map(WidgetRef::value).toList(); }

    @Test
    void theSplitGridPlacesRegionByRegion_inTheGridsOrder() {
        assertEquals(SplitGrid.ENGINE, GRID.engine());
        assertEquals(List.of("kinds", "books", "chosen"), GRID.regions().stream().map(r -> r.name().value()).toList());
        assertEquals(List.of("switcher", "grid", "browser", "jumbotron"), names(GRID.placed()));
        assertEquals("books", GRID.regionOf(WidgetRef.of("browser")).orElseThrow().name().value());
        assertTrue(GRID.regionOf(WidgetRef.of("nothing")).isEmpty());
    }

    @Test
    void aRegionShowsTheOneSaid_elseItsFirst_elseNone() {
        var regions = GRID.regions();
        assertEquals(Optional.of(WidgetRef.of("switcher")), regions.get(0).showing());
        assertEquals(Optional.of(WidgetRef.of("browser")), regions.get(1).showing());
        assertEquals(Optional.empty(), SplitGrid.region("waiting").showing(), "a dock waiting for a tab");
        assertThrows(IllegalArgumentException.class, () -> SplitGrid.region("books", "grid").showing("jumbotron"));
        assertThrows(IllegalArgumentException.class, () -> SplitGrid.region("books", "grid", "grid"));
    }

    @Test
    void aSplitHasTwoPartsOrMore_eachAWholeWeight() {
        var row = (SplitGrid.Split) GRID.frame();
        assertEquals(SplitGrid.Axis.HORIZONTAL, row.axis());
        assertEquals(3, row.weight());
        assertEquals(SplitGrid.Axis.VERTICAL, SplitGrid.column(SplitGrid.region("a"), SplitGrid.region("b")).axis());
        assertEquals(List.of(1, 1), SplitGrid.row(SplitGrid.region("a"), SplitGrid.region("b")).parts().stream().map(Part::weight).toList(), "frames alone: equal parts");
        assertThrows(IllegalArgumentException.class, () -> SplitGrid.row(Part.of(SplitGrid.region("a"))));
        assertThrows(IllegalArgumentException.class, () -> Part.of(SplitGrid.region("a"), 0));
        assertThrows(IllegalArgumentException.class, () -> SplitGrid.row(SplitGrid.region("a"), SplitGrid.row(SplitGrid.region("b"), SplitGrid.region("c"))),
                "a row in a row is one row");
    }

    @Test
    void eachRegionNamedOnce_eachWidgetInOneRegion() {
        assertThrows(IllegalArgumentException.class, () -> SplitGrid.of(SplitGrid.row(SplitGrid.region("a", "x"), SplitGrid.region("a", "y"))));
        assertThrows(IllegalArgumentException.class, () -> SplitGrid.of(SplitGrid.row(SplitGrid.region("a", "x"), SplitGrid.region("b", "x"))));
        assertThrows(IllegalArgumentException.class, () -> SplitGrid.RegionName.of("no spaces"));
    }

    @Test
    void onePaneShowsOne_andHoldsTheRest() {
        var pane = OnePane.showing("grid", "jumbotron", "switcher");
        assertEquals(OnePane.ENGINE, pane.engine());
        assertEquals(List.of("grid", "jumbotron", "switcher"), names(pane.placed()));
        assertEquals("grid", pane.shown().value());
        assertThrows(IllegalArgumentException.class, () -> new OnePane(List.of(WidgetRef.of("grid")), WidgetRef.of("jumbotron")));
        assertThrows(IllegalArgumentException.class, () -> OnePane.showing("grid", "grid"));
    }
}
