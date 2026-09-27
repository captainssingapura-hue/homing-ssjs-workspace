package hue.captains.singapura.js.homing.workspace.widgets.books;

import hue.captains.singapura.js.homing.ssjs.test.SecretaryTestBase;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The book selection secretary, kind by kind (Diligent Secretaries): every
 * kind it handles, the same book told again, the default, and the state handed
 * in never changed - and then in a party of its type, as the page has it.
 */
class BookSelectionSecretaryTest extends SecretaryTestBase {

    private static final String MODULE = "/homing/js/hue/captains/singapura/js/homing/workspace/widgets/books/BookSelectionSecretaryModule.js";
    private static final String PARTY = "/homing/js/hue/captains/singapura/js/homing/workspace/parties/MessagingPartyModule.js";

    private static final Map<String, Object> SOLARIS = Map.of("id", "solaris", "title", "Solaris", "author", "Stanislaw Lem");

    @BeforeEach
    void load() { loadSecretary(MODULE, "BookSelectionSecretary"); }

    private static String said(Value step, int i) { return step.getMember("actions").getArrayElement(i).getMember("message").getMember("kind").asString(); }
    private static Value selected(Value step) { return step.getMember("newState").getMember("selected"); }

    private Value chosen() { return dispatch(initial(), envelope("Select", SOLARIS, "p1")).getMember("newState"); }

    @Test
    void initiallyNothingIsChosen_byNoOne() {
        Value s = initial();
        assertTrue(s.getMember("selected").isNull());
        assertTrue(s.getMember("lastChangedBy").isNull());
        assertEquals(0, s.getMember("changes").asInt());
        assertEquals(0, s.getMember("recentUnknown").getArraySize());
    }

    @Test
    void aSelectChoosesTheBook_saysSoToEveryMember_andRecordsWhoAndHowOften() {
        Value step = dispatch(initial(), envelope("Select", SOLARIS, "p1"));
        assertEquals("Solaris", selected(step).getMember("title").asString());
        assertStateField(step, "lastChangedBy", "p1");
        assertStateField(step, "changes", 1);
        assertActionCount(step, 1);
        assertActionKind(step, 0, "BroadcastToMembers");
        Value m = action(step, 0).getMember("message");
        assertEquals("Selected solaris Solaris Stanislaw Lem",
                m.getMember("kind").asString() + " " + m.getMember("id").asString() + " " + m.getMember("title").asString() + " " + m.getMember("author").asString());
    }

    @Test
    void theSameBookToldAgainIsNothing_butANewTitleIsAChange() {
        Value again = dispatch(chosen(), envelope("Select", SOLARIS, "p2"));
        assertActionCount(again, 0);
        assertStateField(again, "lastChangedBy", "p1");
        Value renamed = dispatch(chosen(), envelope("Select", Map.of("id", "solaris", "title", "Solaris (1961)", "author", "Stanislaw Lem"), "p2"));
        assertActionCount(renamed, 1);
        assertEquals("Solaris (1961)", selected(renamed).getMember("title").asString());
        assertStateField(renamed, "changes", 2);
    }

    @Test
    void aClearUnchoosesAndSaysSo_andIsNothingWhenNothingIsChosen() {
        Value step = dispatch(chosen(), envelope("Clear", Map.of(), "p2"));
        assertTrue(selected(step).isNull());
        assertStateField(step, "lastChangedBy", "p2");
        assertStateField(step, "changes", 2);
        assertActionKind(step, 0, "BroadcastToMembers");
        assertEquals("Cleared", said(step, 0));
        assertActionCount(dispatch(initial(), envelope("Clear", Map.of(), "p2")), 0);
    }

    @Test
    void aMemberThatAsksIsAnsweredAlone_theStateUnchanged() {
        Value none = dispatch(initial(), envelope("CurrentRequested", Map.of(), "p3"));
        assertActionKind(none, 0, "SendToMember");
        assertEquals("p3", action(none, 0).getMember("to").asString());
        assertEquals("Cleared", said(none, 0));
        Value some = dispatch(chosen(), envelope("CurrentRequested", Map.of(), "p3"));
        assertEquals("Selected", said(some, 0));
        assertEquals("solaris", action(some, 0).getMember("message").getMember("id").asString());
        assertStateField(some, "changes", 1);
    }

    @Test
    void anythingElseIsKept_theLastFewOnly_andNothingIsDone() {
        Value s = chosen();
        for (int i = 0; i < 7; i++) s = dispatch(s, envelope("Selected", SOLARIS, "p" + i)).getMember("newState");
        Value step = dispatch(s, envelope("Cleared", Map.of(), "p9"));
        assertActionCount(step, 0);
        Value unknown = step.getMember("newState").getMember("recentUnknown");
        assertEquals(5, unknown.getArraySize(), "bounded");
        assertEquals("Cleared p9", unknown.getArrayElement(4).getMember("kind").asString() + " " + unknown.getArrayElement(4).getMember("from").asString());
        assertEquals("Solaris", selected(step).getMember("title").asString(), "what is chosen, kept");
    }

    @Test
    void theStateHandedInIsNeverChanged() {
        Value s = chosen();
        String before = js.eval("js", "JSON.stringify").execute(s).asString();
        dispatch(s, envelope("Select", Map.of("id", "qed", "title", "QED", "author", "Richard Feynman"), "p2"));
        dispatch(s, envelope("Clear", Map.of(), "p2"));
        dispatch(s, envelope("Knock", Map.of(), "p2"));
        assertEquals(before, js.eval("js", "JSON.stringify").execute(s).asString());
    }

    /** In a party of its type, as the page has it: a member chooses, every member hears; one that joins late asks, and is told. */
    @Test
    void inAPartyOfItsType_aChoiceReachesEveryMember_andALateJoinerIsTold() {
        loadModule(PARTY);
        js.eval("js", BookSelection.TYPE.js());
        js.eval("js", """
            var console = { error: function () {} };
            var party = new MessagingParty(BOOK_SELECTION, BookSelectionSecretary), heard = [];
            var grid = party.join("booksGrid", { Selected: function (m) { heard.push("grid " + m.title); } });
            grid.tell({ kind: "Select", id: "qed", title: "QED", author: "Richard Feynman" });
            var jumbo = party.join("bookJumbotron", { Selected: function (m) { heard.push("jumbo " + m.title + " by " + m.author); } });
            jumbo.tell({ kind: "CurrentRequested" });
            """);
        assertEquals("grid QED | jumbo QED by Richard Feynman", js.eval("js", "heard.join(' | ')").asString());
        assertEquals(0, js.eval("js", "party.inspect().refused.length").asInt(), "every word the secretary says is of its type");
    }
}
