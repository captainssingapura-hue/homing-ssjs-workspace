package hue.captains.singapura.js.homing.workspace.parties;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The route to the steward, run headless: one steward per hierarchy. A root party hires
 * its steward the first time its secretary sends to it, and once; a scope linked under
 * another never hires one - its send goes up through the link; a party with neither
 * hears its send back, from "unrouted"; a scope unlinked hires its own.
 */
class StewardRouteTest extends JsModuleTestBase {

    private static final String MODULE = "/homing/js/hue/captains/singapura/js/homing/workspace/parties/MessagingPartyModule.js";

    /** What a party of errands says: a member asks, the steward is sent to fetch, what it got is told. */
    sealed interface Errand {
        record Ask(String what) implements Errand {}
        record Fetch(String what) implements Errand {}
        record Got(String what, String how) implements Errand {}
    }

    /**
     * A secretary that sends every Ask and every Fetch from below to the steward, tells every Got to all, and
     * answers a Fetch back from "unrouted" with a Got of nothing; a steward class that counts its making and
     * its runs, and answers at once.
     */
    private static final String SHIM = new PartyType<>("errands", Errand.class).js() + """

        var console = { error: function () {} };
        var errands = {
            initial: { sent: 0 },
            behavior: function (state, env) {
                var m = env.message;
                if (m.kind === "Ask" || (m.kind === "Fetch" && env.from !== "unrouted"))
                    return { newState: { sent: state.sent + 1 }, actions: [{ kind: "SendToSteward", message: { kind: "Fetch", what: m.what } }] };
                if (m.kind === "Fetch") return { newState: state, actions: [{ kind: "BroadcastToMembers", message: { kind: "Got", what: m.what, how: "none" } }] };
                if (m.kind === "Got") return { newState: state, actions: [{ kind: "BroadcastToMembers", message: m }] };
                return { newState: state, actions: [] };
            }
        };
        var made = 0;
        class Runner {
            constructor(tell) {
                made++;
                var self = this;
                this.runs = [];
                this.reactors = { Fetch: function (m) { self.runs.push(m.what); tell({ kind: "Got", what: m.what, how: "run" }); } };
            }
        }
        var heard = [];
        function listener(who) { return { Got: function (m) { heard.push(who + " " + m.what + " " + m.how); } }; }
        """;

    @BeforeEach
    void load() {
        loadModule(MODULE);
        js.eval("js", SHIM);
    }

    private Value eval(String src) { return js.eval("js", src); }
    private String str(String src) { return eval(src).asString(); }

    @Test
    void aRootHiresItsSteward_theFirstTimeItIsSentTo_andOnce() {
        eval("var root = new MessagingParty(ERRANDS, errands, Runner); var a = root.join('a', listener('a'));");
        assertEquals(0, eval("made").asInt(), "nothing hired before anything is sent");
        assertEquals("not yet", str("root.inspect().steward"));
        assertTrue(eval("root.steward() === null").asBoolean());
        eval("a.tell({ kind: 'Ask', what: 'x' }); a.tell({ kind: 'Ask', what: 'y' })");
        assertEquals(1, eval("made").asInt(), "hired once, however often it is sent to");
        assertEquals("x,y", str("root.steward().runs.join(',')"));
        assertEquals("hired", str("root.inspect().steward"));
        assertEquals("a,steward", str("root.members().map(function (m) { return m.name; }).join(',')"), "an ordinary member, named steward");
        assertEquals("a x run | a y run", str("heard.join(' | ')"));
    }

    @Test
    void aLinkedScopeNeverHiresOne_itsSendGoesUpThroughTheLink() {
        eval("""
            var root = new MessagingParty(ERRANDS, errands, Runner), scope = new MessagingParty(ERRANDS, errands, Runner);
            scope.link(root, 'scope');
            var w = scope.join('w', listener('w'));
            var out = []; scope.on(function (p) { if (p.dir === 'out') out.push(p.message.kind); });
            w.tell({ kind: 'Ask', what: 'z' });
            """);
        assertTrue(eval("scope.steward() === null").asBoolean(), "the scope hires none");
        assertEquals("above", str("scope.inspect().steward"));
        assertEquals("Fetch", str("out.join(',')"), "its send went up the link");
        assertEquals(1, eval("made").asInt(), "one steward in the hierarchy: the root's");
        assertEquals("z", str("root.steward().runs.join(',')"));
        assertEquals("w z run", str("heard.join(' | ')"), "what the root's steward got came down to the asker below");
    }

    @Test
    void withNoLinkAndNoSteward_theSendComesBackUnrouted() {
        eval("""
            var lone = new MessagingParty(ERRANDS, errands); var a = lone.join('a', listener('a'));
            var dirs = []; lone.on(function (p) { dirs.push(p.dir); });
            a.tell({ kind: 'Ask', what: 'q' });
            """);
        assertEquals("none", str("lone.inspect().steward"));
        assertTrue(str("dirs.join(' ')").contains("unrouted"));
        assertEquals("a q none", str("heard.join(' | ')"), "the secretary heard its send back, and answered");
    }

    @Test
    void aScopeUnlinked_hiresItsOwn() {
        eval("""
            var root = new MessagingParty(ERRANDS, errands, Runner), scope = new MessagingParty(ERRANDS, errands, Runner);
            scope.link(root, 'scope');
            var w = scope.join('w', listener('w'));
            w.tell({ kind: 'Ask', what: 'one' });
            scope.unlink();
            w.tell({ kind: 'Ask', what: 'two' });
            """);
        assertEquals(2, eval("made").asInt(), "the root's, then the scope's own");
        assertEquals("one", str("root.steward().runs.join(',')"));
        assertEquals("two", str("scope.steward().runs.join(',')"));
    }

    @Test
    void aStewardIsAClass() {
        assertTrue(str("(function () { try { new MessagingParty(ERRANDS, errands, { reactors: {} }); return 'made'; } catch (e) { return e.message; } })()")
                .contains("a steward is a class"));
    }
}
