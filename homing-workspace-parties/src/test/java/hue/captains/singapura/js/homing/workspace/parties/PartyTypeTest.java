package hue.captains.singapura.js.homing.workspace.parties;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A party type is its name and a sealed vocabulary of records; its JavaScript is
 * what they say, and nothing else. A field is plain data all the way down: text,
 * a number or a truth; a record of such; a list of any of these.
 */
class PartyTypeTest {

    sealed interface Chimes {
        record Ring(String who, int times, boolean loud) implements Chimes {}
        record Hush() implements Chimes {}
    }

    /** A board, told: a record inside a record, and a list of records inside that. */
    sealed interface Boards {
        record Square(int col, int row) {}
        record Piece(String name, Square at) {}
        record Position(int move, List<Piece> pieces, List<String> captured) {}
        record Shown(String to, Position position) implements Boards {}
        record Moved(Piece piece) implements Boards {}
    }

    sealed interface WithAMap { record Many(Map<String, String> names) implements WithAMap {} }
    sealed interface WithAnArray { record Many(String[] names) implements WithAnArray {} }
    sealed interface WithAnOptional { record Maybe(Optional<String> name) implements WithAnOptional {} }
    sealed interface WithARawList { @SuppressWarnings("rawtypes") record Many(List names) implements WithARawList {} }
    sealed interface WithAWildcard { record Many(List<? extends Number> counts) implements WithAWildcard {} }
    sealed interface HoldingItself {
        record Node(String name, List<Node> children) {}
        record Tree(Node root) implements HoldingItself {}
    }
    sealed interface WithAKind { record Odd(String kind) implements WithAKind {} }
    interface NotSealed {}

    @Test
    void theJavaScriptIsTheNameAndEachKindsFields_typedAsTypeofSaysThem() {
        var t = new PartyType<>("door-chimes", Chimes.class);
        assertEquals("DOOR_CHIMES", t.constName());
        assertEquals("const DOOR_CHIMES = Object.freeze({ name: \"door-chimes\", kinds: Object.freeze({ "
                + "Ring: Object.freeze({ who: \"string\", times: \"number\", loud: \"boolean\" }), Hush: Object.freeze({}) }) });", t.js());
        assertEquals(List.of(Chimes.Ring.class, Chimes.Hush.class), t.kinds());
    }

    @Test
    void aRecordFieldIsItsFieldsShapes_andAListTheOneShapeAllItHoldsHas() {
        var t = new PartyType<>("boards", Boards.class);
        String square = "Object.freeze({ col: \"number\", row: \"number\" })";
        String piece = "Object.freeze({ name: \"string\", at: " + square + " })";
        assertEquals("const BOARDS = Object.freeze({ name: \"boards\", kinds: Object.freeze({ "
                + "Shown: Object.freeze({ to: \"string\", position: Object.freeze({ move: \"number\", pieces: Object.freeze([" + piece + "]), "
                + "captured: Object.freeze([\"string\"]) }) }), "
                + "Moved: Object.freeze({ piece: " + piece + " }) }) });", t.js());
        assertEquals(List.of(Boards.Shown.class, Boards.Moved.class), t.kinds(), "a record held by a kind is not a kind");
    }

    @Test
    void whatIsNotAVocabularyIsRefused_whenDeclared() {
        assertThrows(IllegalArgumentException.class, () -> new PartyType<>("Door", Chimes.class), "a name as an address has it");
        assertThrows(IllegalArgumentException.class, () -> new PartyType<>("plain", NotSealed.class));
        var e = assertThrows(IllegalArgumentException.class, () -> new PartyType<>("kinds", WithAKind.class));
        assertTrue(e.getMessage().contains("kind"), e.getMessage());
    }

    @Test
    void whatIsNotPlainDataIsRefused_whenDeclared() {
        for (Class<?> v : List.of(WithAMap.class, WithAnArray.class, WithAnOptional.class, WithARawList.class, WithAWildcard.class)) {
            var e = assertThrows(IllegalArgumentException.class, () -> new PartyType<>("odd", v), v.getSimpleName());
            assertTrue(e.getMessage().contains("a record of such, or a list of such"), e.getMessage());
        }
        var e = assertThrows(IllegalArgumentException.class, () -> new PartyType<>("trees", HoldingItself.class));
        assertTrue(e.getMessage().contains("holds itself"), e.getMessage());
    }
}
