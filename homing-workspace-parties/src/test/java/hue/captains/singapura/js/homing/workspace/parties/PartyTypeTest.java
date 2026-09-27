package hue.captains.singapura.js.homing.workspace.parties;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** A party type is its name and a sealed vocabulary of records; its JavaScript is what they say, and nothing else. */
class PartyTypeTest {

    sealed interface Chimes {
        record Ring(String who, int times, boolean loud) implements Chimes {}
        record Hush() implements Chimes {}
    }

    sealed interface WithAList { record Many(List<String> names) implements WithAList {} }
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
    void whatIsNotAVocabularyIsRefused_whenDeclared() {
        assertThrows(IllegalArgumentException.class, () -> new PartyType<>("Door", Chimes.class), "a name as an address has it");
        assertThrows(IllegalArgumentException.class, () -> new PartyType<>("plain", NotSealed.class));
        assertThrows(IllegalArgumentException.class, () -> new PartyType<>("lists", WithAList.class), "a field is text, a number or a truth");
        var e = assertThrows(IllegalArgumentException.class, () -> new PartyType<>("kinds", WithAKind.class));
        assertTrue(e.getMessage().contains("kind"), e.getMessage());
    }
}
