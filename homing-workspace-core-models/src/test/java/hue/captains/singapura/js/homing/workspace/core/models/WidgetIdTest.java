package hue.captains.singapura.js.homing.workspace.core.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** A widget's id is a prefix and a sequence, and nothing else is one. */
class WidgetIdTest {

    @Test
    void theIdIsAPrefixAndASequence() {
        assertEquals("books-grid_title-rating", WidgetId.of("books-grid_title-rating-12").prefix());
        assertEquals(12, WidgetId.of("books-grid_title-rating-12").sequence());
        assertEquals(WidgetId.of("books-grid-3"), WidgetId.of("books-grid", 3));
    }

    @Test
    void whatIsNotOne_isRefused() {
        assertThrows(IllegalArgumentException.class, () -> WidgetId.of("books-grid"), "no sequence");
        assertThrows(IllegalArgumentException.class, () -> WidgetId.of("books-grid-0"), "a sequence from 1");
        assertThrows(IllegalArgumentException.class, () -> WidgetId.of("books grid-1"), "a space");
        assertThrows(IllegalArgumentException.class, () -> WidgetId.of("books-grid-1234567890"), "more than nine digits");
        assertThrows(IllegalArgumentException.class, () -> WidgetId.of("books-grid", 0));
    }
}
