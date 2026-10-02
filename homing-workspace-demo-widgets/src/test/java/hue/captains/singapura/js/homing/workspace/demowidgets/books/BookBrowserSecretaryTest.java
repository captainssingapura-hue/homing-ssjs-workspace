package hue.captains.singapura.js.homing.workspace.demowidgets.books;

import hue.captains.singapura.js.homing.ssjs.test.SecretaryTestBase;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The book browser's scope, at its edge (Diligent Secretaries): a member's choice
 * goes up when it changed what is chosen; a question never does; what comes down
 * is taken as the scope's own and told to it, never sent back up - and then in
 * two parties, the scope linked under one above, as a page runs them.
 */
class BookBrowserSecretaryTest extends SecretaryTestBase {

    private static final String DIR = "/homing/js/hue/captains/singapura/js/homing/workspace/demowidgets/books/";
    private static final String PARTY = "/homing/js/hue/captains/singapura/js/homing/workspace/parties/MessagingPartyModule.js";

    private static final Map<String, Object> QED = Map.of("id", "qed", "title", "QED", "author", "Richard Feynman");
    private static final Map<String, Object> COSMOS = Map.of("id", "cosmos", "title", "Cosmos", "author", "Carl Sagan");

    @BeforeEach
    void load() {
        loadModule(DIR + "BookSelectionSecretaryModule.js");
        loadSecretary(DIR + "BookBrowserSecretaryModule.js", "BookBrowserSecretary");
    }

    private static Value selected(Value step) { return step.getMember("newState").getMember("selection").getMember("selected"); }
    private static int count(Value step, String field) { return step.getMember("newState").getMember(field).asInt(); }
    private static String kinds(Value step) {
        var out = new StringBuilder();
        Value a = step.getMember("actions");
        for (int i = 0; i < a.getArraySize(); i++) out.append(i == 0 ? "" : " ").append(a.getArrayElement(i).getMember("kind").asString())
                .append(":").append(a.getArrayElement(i).getMember("message").getMember("kind").asString());
        return out.toString();
    }
    private Value chosen() { return dispatch(initial(), envelope("Select", QED, "grid")).getMember("newState"); }

    @Test
    void whatGoesUpIsDeclared_kindByKind() {
        Value b = secretary.getMember("BUBBLES");
        assertTrue(b.getMember("Select").asBoolean());
        assertTrue(b.getMember("Clear").asBoolean());
        assertTrue(!b.getMember("CurrentRequested").asBoolean());
    }

    @Test
    void aChoiceInTheScopeIsToldToTheScope_andGoesUp() {
        Value step = dispatch(initial(), envelope("Select", QED, "grid"));
        assertEquals("BroadcastToMembers:Selected SendToParent:Select", kinds(step));
        assertEquals("QED", selected(step).getMember("title").asString());
        assertEquals(1, count(step, "bubbled"));
        assertEquals(0, count(step, "kept"));
    }

    @Test
    void theSameChoiceAgainIsKeptHere_andNothingGoesUp() {
        Value step = dispatch(chosen(), envelope("Select", QED, "grid"));
        assertEquals("", kinds(step));
        assertEquals(1, count(step, "bubbled"));
        assertEquals(1, count(step, "kept"));
    }

    @Test
    void aClearGoesUp_whenSomethingWasChosen() {
        assertEquals("BroadcastToMembers:Cleared SendToParent:Clear", kinds(dispatch(chosen(), envelope("Clear", Map.of(), "grid"))));
        assertEquals("", kinds(dispatch(initial(), envelope("Clear", Map.of(), "grid"))));
    }

    @Test
    void aQuestionIsAnsweredInTheScope_neverUp() {
        Value step = dispatch(chosen(), envelope("CurrentRequested", Map.of(), "jumbotron"));
        assertEquals("SendToMember:Selected", kinds(step));
        assertEquals("jumbotron", action(step, 0).getMember("to").asString());
        assertEquals(1, count(step, "kept"));
    }

    @Test
    void whatComesDownIsTakenAsTheScopesOwn_toldToIt_neverSentBackUp() {
        Value step = dispatch(chosen(), envelope("Selected", COSMOS, "upstream"));
        assertEquals("BroadcastToMembers:Selected", kinds(step));
        assertEquals("Cosmos", selected(step).getMember("title").asString());
        assertEquals("upstream", step.getMember("newState").getMember("selection").getMember("lastChangedBy").asString());
        assertEquals(1, count(step, "adopted"));
        Value again = dispatch(step.getMember("newState"), envelope("Selected", COSMOS, "upstream"));
        assertEquals("", kinds(again), "the same again is nothing");
        assertEquals(1, count(again, "adopted"));
        Value cleared = dispatch(step.getMember("newState"), envelope("Cleared", Map.of(), "upstream"));
        assertEquals("BroadcastToMembers:Cleared", kinds(cleared));
        assertTrue(selected(cleared).isNull());
    }

    @Test
    void aMembersWordFromAboveIsKeptAsUnknown_andNothingIsDone() {
        Value step = dispatch(chosen(), envelope("Select", COSMOS, "upstream"));
        assertEquals("", kinds(step));
        assertEquals("QED", selected(step).getMember("title").asString());
        Value unknown = step.getMember("newState").getMember("selection").getMember("recentUnknown");
        assertEquals("Select upstream", unknown.getArrayElement(0).getMember("kind").asString() + " " + unknown.getArrayElement(0).getMember("from").asString());
    }

    @Test
    void theStateHandedInIsNeverChanged() {
        Value s = chosen();
        Value stringify = js.eval("js", "JSON.stringify");
        String before = stringify.execute(s).asString();
        dispatch(s, envelope("Select", COSMOS, "grid"));
        dispatch(s, envelope("Selected", COSMOS, "upstream"));
        dispatch(s, envelope("Knock", Map.of(), "upstream"));
        assertEquals(before, stringify.execute(s).asString());
    }

    /**
     * The scope, linked under a party above - the book selection secretary's: the grid's choice
     * reaches the jumbotron and goes up; a choice made above reaches both; and nothing rings for
     * ever, since what comes down is never sent back up, and the same book again is nothing.
     */
    @Test
    void linkedUnderAPartyAbove_aChoiceGoesUpAndComesDownToBoth() {
        loadModule(PARTY);
        js.eval("js", BookSelection.TYPE.js());
        js.eval("js", """
            var console = { error: function () {} };
            var above = new MessagingParty(BOOK_SELECTION, BookSelectionSecretary), heard = [];
            var peer = above.join("peer", { Selected: function (m) { heard.push("peer " + m.title); } });
            var scope = new MessagingParty(BOOK_SELECTION, BookBrowserSecretary);
            scope.link(above, "bookBrowser").tell({ kind: "CurrentRequested" });
            var grid = scope.join("booksGrid", { Selected: function (m) { heard.push("grid " + m.title); } });
            var jumbo = scope.join("bookJumbotron", { Selected: function (m) { heard.push("jumbo " + m.title); } });
            grid.tell({ kind: "Select", id: "qed", title: "QED", author: "Richard Feynman" });
            """);
        assertEquals("grid QED | jumbo QED | peer QED", js.eval("js", "heard.join(' | ')").asString(),
                "told to the scope, then up as the browser's word, and the party above told its members");
        assertEquals("bookBrowser", js.eval("js", "above.inspect().members.filter(function (m) { return m.name === 'bookBrowser'; })[0].name").asString());
        js.eval("js", "heard = []; peer.tell({ kind: 'Select', id: 'cosmos', title: 'Cosmos', author: 'Carl Sagan' })");
        assertEquals("peer Cosmos | grid Cosmos | jumbo Cosmos", js.eval("js", "heard.join(' | ')").asString(), "a choice above reaches both");
        assertEquals(1, js.eval("js", "scope.state().adopted").asInt());
        assertEquals(1, js.eval("js", "scope.state().bubbled").asInt(), "what came down was not sent back up");
        assertEquals(0, js.eval("js", "scope.inspect().refused.length + above.inspect().refused.length").asInt());
    }
}
