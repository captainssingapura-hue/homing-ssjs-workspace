package hue.captains.singapura.js.homing.workspace.parties;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A message with records and lists inside: checked all the way down where it
 * enters - the first thing that does not read said by where it is - and handed
 * on frozen all the way down, so what one member is handed no member can change
 * for another.
 */
class NestedMessagesTest extends JsModuleTestBase {

    private static final String MODULE = "/homing/js/hue/captains/singapura/js/homing/workspace/parties/MessagingPartyModule.js";

    @BeforeEach
    void load() {
        loadModule(MODULE);
        js.eval("js", new PartyType<>("boards", PartyTypeTest.Boards.class).js() + """

                var console = { error: function () {} };
                var relay = { initial: {}, behavior: function (s, env) { return { newState: s, actions: [{ kind: "BroadcastToMembers", message: env.message }] }; } };
                var party = new MessagingParty(BOARDS, relay), got = [];
                function position() {
                    return { move: 12, captured: ["pawn"], pieces: [{ name: "king", at: { col: 4, row: 0 } }, { name: "rook", at: { col: 0, row: 0 } }] };
                }
                var teller = party.join("teller", {});
                var meddler = party.join("meddler", { Shown: function (m) {
                    try { m.position.pieces[0].at.col = 9; } catch (e) {}
                    try { m.position.pieces.push({ name: "queen", at: { col: 3, row: 0 } }); } catch (e) {}
                    got.push("meddler"); } });
                var watcher = party.join("watcher", { Shown: function (m) { got.push(m.position.pieces[0].at.col + " " + m.position.pieces.length + " " + m.position.captured[0]); } });
                function why(m) { return MessagingParty.check(BOARDS, m); }
                """);
    }

    private String eval(String code) { return js.eval("js", code).toString(); }

    @Test
    void aMessageOfItsShapeAllTheWayDown_reads() {
        assertEquals("null", eval("String(why({ kind: 'Shown', to: 'p1', position: position() }))"));
        assertEquals("null", eval("String(why({ kind: 'Shown', to: '', position: { move: 0, pieces: [], captured: [] } }))"), "an empty list is a list");
    }

    @Test
    void theFirstThingThatDoesNotRead_isSaidByWhereItIs() {
        assertEquals("Shown.position.pieces[1].at.row is a number, not string",
                eval("var p = position(); p.pieces[1].at.row = '0'; why({ kind: 'Shown', to: 'p1', position: p })"));
        assertEquals("Shown.position.pieces is a list, not object",
                eval("var p = position(); p.pieces = { 0: p.pieces[0] }; why({ kind: 'Shown', to: 'p1', position: p })"));
        assertEquals("Shown.position.pieces[0].at is a record, not null",
                eval("var p = position(); p.pieces[0].at = null; why({ kind: 'Shown', to: 'p1', position: p })"));
        assertEquals("Shown.position.captured[0] is a string, not number",
                eval("var p = position(); p.captured = [1]; why({ kind: 'Shown', to: 'p1', position: p })"));
        assertEquals("Shown.position.pieces[0] has no field 'colour'",
                eval("var p = position(); p.pieces[0].colour = 'white'; why({ kind: 'Shown', to: 'p1', position: p })"));
        assertEquals("Shown.position is a record, not missing", eval("why({ kind: 'Shown', to: 'p1' })"));
    }

    @Test
    void whatOneMemberIsHanded_noneCanChangeForAnother_norTheTellerForAny() {
        eval("var p = position(); teller.tell({ kind: 'Shown', to: '', position: p }); p.pieces[0].at.col = 7;");
        assertEquals("meddler|4 2 pawn", eval("got.join('|')"), "the watcher hears the board as told - the meddler's changes and the teller's after both came to nothing");
    }

    @Test
    void aNestedMessageThatDoesNotRead_isRefusedWhereItEnters_andGoesNowhere() {
        eval("var p = position(); p.pieces[0].at.col = 'e'; teller.tell({ kind: 'Shown', to: '', position: p });");
        assertEquals("", eval("got.join('|')"));
        assertEquals("Shown.position.pieces[0].at.col is a number, not string", eval("party.inspect().refused[0].reason"));
    }
}
