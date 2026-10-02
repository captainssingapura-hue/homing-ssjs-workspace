package hue.captains.singapura.js.homing.workspace.content;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The content-party pattern, held in Java: the six kinds, their fields, one content record the
 * same in Loaded and Content - and the content secretary the type's default. Anything else is
 * refused, saying what it has and what it should.
 */
class ContentPartyTest {

    /** A content type of the pattern: snippets of text. */
    sealed interface Snippets {
        record Snippet(String text) {}
        record Wanted(List<Param> params) implements Snippets {}
        record Fetch(List<Item> items) implements Snippets {}
        record Loaded(List<Param> params, Snippet content) implements Snippets {}
        record Failed(List<Param> params, String why) implements Snippets {}
        record Content(List<Param> params, Snippet content) implements Snippets {}
        record Unavailable(List<Param> params, String why) implements Snippets {}
    }

    sealed interface MissingOne {
        record Snippet(String text) {}
        record Wanted(List<Param> params) implements MissingOne {}
        record Fetch(List<Item> items) implements MissingOne {}
        record Loaded(List<Param> params, Snippet content) implements MissingOne {}
        record Failed(List<Param> params, String why) implements MissingOne {}
        record Content(List<Param> params, Snippet content) implements MissingOne {}
    }

    sealed interface WantedByAKey {
        record Snippet(String text) {}
        record Wanted(String key) implements WantedByAKey {}
        record Fetch(List<Item> items) implements WantedByAKey {}
        record Loaded(List<Param> params, Snippet content) implements WantedByAKey {}
        record Failed(List<Param> params, String why) implements WantedByAKey {}
        record Content(List<Param> params, Snippet content) implements WantedByAKey {}
        record Unavailable(List<Param> params, String why) implements WantedByAKey {}
    }

    sealed interface TwoContents {
        record Snippet(String text) {}
        record Other(String text) {}
        record Wanted(List<Param> params) implements TwoContents {}
        record Fetch(List<Item> items) implements TwoContents {}
        record Loaded(List<Param> params, Snippet content) implements TwoContents {}
        record Failed(List<Param> params, String why) implements TwoContents {}
        record Content(List<Param> params, Other content) implements TwoContents {}
        record Unavailable(List<Param> params, String why) implements TwoContents {}
    }

    @Test
    void aVocabularyOfThePattern_isAPartyTypeWithTheContentSecretary() {
        var t = ContentParty.type("snippets", Snippets.class);
        assertEquals("snippets", t.name());
        assertEquals(ContentParty.SECRETARY, t.secretary().orElseThrow());
        assertTrue(t.steward().isEmpty(), "its steward is its own to declare - or its host's");
        assertTrue(t.js().contains("Loaded: Object.freeze({ params: Object.freeze([Object.freeze({ name: \"string\", value: \"string\" })]), content: Object.freeze({ text: \"string\" }) })"), t.js());
    }

    @Test
    void theFlowIsOfThePattern() {
        assertEquals("flow", FlowContent.TYPE.name());
        assertTrue(FlowContent.TYPE.js().contains("parts: Object.freeze([Object.freeze({ type: \"string\", params: Object.freeze([Object.freeze({ name: \"string\", value: \"string\" })]) })])"),
                FlowContent.TYPE.js());
    }

    @Test
    void aKindMissing_isRefused() {
        var e = assertThrows(IllegalArgumentException.class, () -> ContentParty.type("missing", MissingOne.class));
        assertTrue(e.getMessage().contains("a content party speaks [Wanted, Fetch, Loaded, Failed, Content, Unavailable]"), e.getMessage());
    }

    @Test
    void aKindOfOtherFields_isRefused_sayingWhatItHas() {
        var e = assertThrows(IllegalArgumentException.class, () -> ContentParty.type("by-key", WantedByAKey.class));
        assertTrue(e.getMessage().contains("Wanted has [key: java.lang.String]"), e.getMessage());
    }

    @Test
    void oneContent_theSameInLoadedAndContent() {
        var e = assertThrows(IllegalArgumentException.class, () -> ContentParty.type("two", TwoContents.class));
        assertTrue(e.getMessage().contains("Loaded carries Snippet and Content Other"), e.getMessage());
    }
}
