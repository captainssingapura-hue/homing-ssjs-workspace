package hue.captains.singapura.js.homing.workspace.parties;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The flat instance, run headless: members join by name and hear the kinds
 * they have reactors for; a member tells, the secretary steps, its actions are
 * done; every message is checked where it enters; what goes to a parent stops
 * here, recorded; a reactor or a secretary that throws harms no one else.
 */
class MessagingPartyTest extends JsModuleTestBase {

    private static final String MODULE = "/homing/js/hue/captains/singapura/js/homing/workspace/parties/MessagingPartyModule.js";

    /**
     * A chimes party, as Java generates its type; a secretary that echoes a Ring to all - and up,
     * unless it came from above, which would ring for ever - and answers a Hush to its sender.
     */
    private static final String SHIM = new PartyType<>("door-chimes", PartyTypeTest.Chimes.class).js() + """

        var console = { error: function () {} };
        var CHIMES = DOOR_CHIMES;
        var echo = {
            initial: { rings: 0 },
            behavior: function (state, env) {
                var m = env.message;
                if (m.kind === "Ring") return { newState: { rings: state.rings + 1 }, actions: env.from === "upstream"
                        ? [{ kind: "BroadcastToMembers", message: m }]
                        : [{ kind: "BroadcastToMembers", message: m }, { kind: "SendToParent", message: m }] };
                if (m.kind === "Hush") return { newState: state, actions: [{ kind: "SendToMember", to: env.from, message: { kind: "Hush" } }] };
                return { newState: state, actions: [] };
            }
        };
        var party = new MessagingParty(CHIMES, echo);
        var heard = [], passages = [];
        party.on(function (p) { passages.push(p.dir + (p.name ? ":" + p.name : "")); });
        var a = party.join("a", { Ring: function (m, env) { heard.push("a Ring " + m.who + " from " + env.from); }, Hush: function () { heard.push("a Hush"); } });
        var b = party.join("b", { Ring: function (m) { heard.push("b Ring " + m.times); } });
        """;

    @BeforeEach
    void load() {
        loadModule(MODULE);
        js.eval("js", SHIM);
    }

    private Value eval(String src) { return js.eval("js", src); }
    private String str(String src) { return eval(src).asString(); }

    @Test
    void aMemberTells_theSecretarySteps_andItsActionsAreDone() {
        assertTrue(eval("a.tell({ kind: 'Ring', who: 'Ann', times: 2, loud: true })").asBoolean());
        assertEquals("a Ring Ann from secretary | b Ring 2", str("heard.join(' | ')"), "broadcast to every member, each by its reactor");
        assertEquals(1, eval("party.state().rings").asInt());
        eval("heard = []; b.tell({ kind: 'Hush' })");
        assertEquals("", str("heard.join(' | ')"), "sent back to b alone - and b hears no Hush");
        eval("a.tell({ kind: 'Hush' })");
        assertEquals("a Hush", str("heard.join(' | ')"));
    }

    @Test
    void whatGoesToAParentStopsHere_recorded() {
        eval("a.tell({ kind: 'Ring', who: 'Ann', times: 1, loud: false })");
        assertEquals("Ring", str("party.inspect().stopped[0].kind"));
        assertTrue(str("passages.join(' ')").contains("stopped"));
    }

    @Test
    void everyMessageIsCheckedWhereItEnters_andOneThatDoesNotReadIsRefused() {
        assertEquals("'Knock' is not a kind of door-chimes: Ring, Hush", str("MessagingParty.check(CHIMES, { kind: 'Knock' })"));
        assertEquals("Ring.times is a number, not string", str("MessagingParty.check(CHIMES, { kind: 'Ring', who: 'x', times: '2', loud: true })"));
        assertEquals("Ring.loud is a boolean, not missing", str("MessagingParty.check(CHIMES, { kind: 'Ring', who: 'x', times: 2 })"));
        assertEquals("Hush has no field 'soft'", str("MessagingParty.check(CHIMES, { kind: 'Hush', soft: true })"));
        assertFalse(eval("a.tell({ kind: 'Knock' })").asBoolean());
        assertEquals(0, eval("party.state().rings").asInt(), "the secretary never saw it");
        assertEquals(1, eval("party.inspect().refused.length").asInt());
        assertTrue(eval("party.inspect().refused[0].reason.indexOf('Knock') >= 0").asBoolean());
    }

    @Test
    void theSubstrateSends_toOneOrToAll() {
        assertTrue(eval("party.send({ kind: 'Ring', who: 'bench', times: 3, loud: false })").asBoolean());
        assertEquals("a Ring bench from substrate | b Ring 3", str("heard.join(' | ')"));
        eval("heard = []; party.send({ kind: 'Ring', who: 'bench', times: 4, loud: false }, b.id)");
        assertEquals("b Ring 4", str("heard.join(' | ')"));
        assertFalse(eval("party.send({ kind: 'Ring' })").asBoolean(), "the substrate's messages are checked too");
    }

    @Test
    void aMemberLeaves_andHearsNoMore_andAKindNotOfTheTypeCannotBeHeard() {
        eval("b.leave(); a.tell({ kind: 'Ring', who: 'Ann', times: 1, loud: true })");
        assertEquals("a Ring Ann from secretary", str("heard.join(' | ')"));
        assertEquals("joined:a joined:b left:b", str("passages.filter(function (p) { return /joined|left/.test(p); }).join(' ')"));
        assertFalse(eval("b.tell({ kind: 'Hush' })").asBoolean(), "gone, it cannot tell");
        assertTrue(str("(function () { try { party.join('c', { Knock: function () {} }); return ''; } catch (e) { return e.message; } })()").contains("not a kind"));
    }

    /**
     * A scope: a party linked to one above it. What its secretary sends to its parent goes up
     * through the link, as the scope's own word there; what the party above says comes in to
     * its secretary, from "upstream"; unlinked, it stops here again.
     */
    @Test
    void aScopeLinkedAbove_bubblesUpThroughTheLink_andHearsWhatComesDown() {
        eval("""
            var above = new MessagingParty(CHIMES, echo), aboveHeard = [];
            var peer = above.join("peer", { Ring: function (m) { aboveHeard.push("peer Ring " + m.who); } });
            var up = party.link(above, "scope");
            """);
        assertEquals("scope", str("party.inspect().linked"));
        assertTrue(str("above.members().map(function (m) { return m.name; }).join(' ')").contains("scope"), "the scope is a member above, by its name");
        eval("heard = []; a.tell({ kind: 'Ring', who: 'Ann', times: 1, loud: true })");
        assertEquals("peer Ring Ann", str("aboveHeard.join(' | ')"), "up through the link: the party above broadcast it");
        assertEquals(1, eval("above.state().rings").asInt());
        assertEquals(1, eval("party.inspect().bubbled.length").asInt());
        assertEquals(0, eval("party.inspect().stopped.length").asInt());
        // the party above's broadcast came back in to this secretary, from upstream - which rang again locally
        assertEquals(2, eval("party.state().rings").asInt());
        eval("heard = []; peer.tell({ kind: 'Hush' })");
        assertEquals("", str("heard.join(' | ')"), "above answers its sender alone: not the scope");
        eval("heard = []; above.send({ kind: 'Hush' })");
        assertTrue(str("passages.join(' ')").contains("in:scope"), "what the party above says comes in, from upstream");
        eval("party.unlink(); aboveHeard = []; a.tell({ kind: 'Ring', who: 'Bo', times: 1, loud: true })");
        assertEquals("", str("aboveHeard.join(' | ')"), "unlinked, nothing goes up");
        assertEquals(1, eval("party.inspect().stopped.length").asInt());
        assertTrue(str("(function () { try { party.link(new MessagingParty({ name: 'other', kinds: {} }, echo), 'x'); return ''; } catch (e) { return e.message; } })()").contains("its own type"));
    }

    @Test
    void aReactorThatThrowsHarmsNoOneElse_andASecretaryThatThrowsKeepsItsState() {
        eval("party.join('bad', { Ring: function () { throw new Error('boom'); } }); a.tell({ kind: 'Ring', who: 'Ann', times: 1, loud: true })");
        assertEquals("a Ring Ann from secretary | b Ring 1", str("heard.join(' | ')"));
        assertEquals("boom", str("party.inspect().threw[0].error"));
        eval("var angry = new MessagingParty(CHIMES, { initial: { n: 7 }, behavior: function () { throw new Error('no'); } }); var x = angry.join('x', {}); x.tell({ kind: 'Hush' })");
        assertEquals(7, eval("angry.state().n").asInt());
        assertEquals("the secretary", str("angry.inspect().threw[0].member"));
    }
}
