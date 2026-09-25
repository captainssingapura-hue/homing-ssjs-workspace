package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The room — the PANE half of a tab-pane — against a shimmed DOM, branch, css
 * manager and Keys: its membership and the press that claims it, the one
 * tenant it holds and retires, the host it hands a widget (a name, the
 * parties), how it takes and hands on keys, where it says the keys are, the
 * seam with a widget's native controls, and its going.
 */
class WidgetPaneTest extends JsModuleTestBase {

    private static final String MODULE = "/homing/js/hue/captains/singapura/js/homing/workspace/shell/WidgetPaneModule.js";

    // Elements that know their children, parent, attributes and listeners; a
    // branch that mints them and records its going; Keys that records what the
    // room asks of the party; a document whose focus the tests move.
    private static final String SHIM = """
        var log = [];
        function el(tag) {
            var attrs = {}, classes = new Set();
            var node = { tag: tag, children: [], parentNode: null, listeners: {}, textContent: '',
                get firstChild() { return this.children.length ? this.children[0] : null; },
                appendChild: function (c) { if (c.parentNode) c.parentNode.removeChild(c); this.children.push(c); c.parentNode = this; return c; },
                removeChild: function (c) { var i = this.children.indexOf(c); if (i >= 0) this.children.splice(i, 1); c.parentNode = null; return c; },
                setAttribute: function (k, v) { attrs[k] = String(v); }, getAttribute: function (k) { return k in attrs ? attrs[k] : null; },
                removeAttribute: function (k) { delete attrs[k]; },
                addEventListener: function (t, fn) { (this.listeners[t] = this.listeners[t] || []).push(fn); },
                contains: function (c) { for (var n = c; n; n = n.parentNode) if (n === this) return true; return false; },
                fire: function (t, ev) { (this.listeners[t] || []).slice().forEach(function (fn) { fn(ev); }); },
                blur: function () { log.push('blur:' + tag); if (document.activeElement === this) document.activeElement = document.body; },
                classList: classes };
            return node;
        }
        function fakeBranch(name) {
            var names = new Set();
            return { name: name, active: false, dissolved: false, kids: [],
                activate: function () { if (this.active) throw new Error('already activated'); this.active = true; },
                createElement: function (n, tag) { if (!this.active) throw new Error('not activated'); if (names.has(n)) throw new Error('name taken: ' + n); names.add(n); return el(tag); },
                createBranch: function (n) { var b = fakeBranch(n); this.kids.push(b); return b; },
                dissolve: function () { this.dissolved = true; log.push('dissolved:' + name); } };
        }
        var css = { addClass: function (e, c) { e.classList.add(c); }, toggleClass: function (e, c, on) { if (on) e.classList.add(c); else e.classList.delete(c); } };
        var wp_host = 'wp_host';
        var document = { body: el('body'), activeElement: null };
        document.activeElement = document.body;
        var claimed = [], yielded = [], unclaimed = 0;
        var Keys = {
            claimOn: function (root, m) { log.push('claimOn'); return function () { unclaimed++; }; },
            claim: function (m) { claimed.push(m); },
            yield: function (m) { yielded.push(m); }
        };
        var membership = { in: true, left: 0, leave: function () { this.left++; this.in = false; } };
        var joined = [];
        var dockFocus = { join: function (name, member) { joined.push({ name: name, member: member }); return membership; } };
        var titles = [];
        var parties = { chat: { name: 'chat' } };
        var roomBranch = fakeBranch('room1');
        function room(opts) { return new WidgetPane(roomBranch, Object.assign({ focus: dockFocus, onTitle: function (t) { titles.push(t); }, parties: parties }, opts || {})); }
        function widget(key, extra) {
            var w = Object.assign({ root: el('w-' + key),
                dispose: function () { log.push(key + ':dispose'); },
                partyDeregister: function () { log.push(key + ':deregister'); } }, extra || {});
            return w;
        }
        function ev(key, target, prevented) {
            var e = { key: key, target: target, defaultPrevented: !!prevented, prevented: false, stopped: false,
                      preventDefault: function () { this.prevented = true; }, stopPropagation: function () { this.stopped = true; } };
            return e;
        }
        """;

    @BeforeEach
    void load() {
        js = buildContext();
        js.eval(Source.newBuilder("js", SHIM, "shim.js").buildLiteral());
        loadModule(MODULE);
    }

    private Value eval(String src) { return js.eval("js", src); }
    private String log() { return eval("log.join(' ')").asString(); }

    // ── the member ──────────────────────────────────────────────────────────

    @Test
    void itJoinsTheDocksBranchAsItsMember_andAPressClaimsIt() {
        eval("var r = room()");
        assertTrue(eval("roomBranch.active").asBoolean(), "the room activates the branch it is handed");
        assertEquals("room1", eval("joined[0].name").asString(), "a member of the dock's focus branch, by the branch's name");
        assertTrue(eval("joined[0].member === r && r.focus === membership").asBoolean());
        assertEquals("claimOn", log(), "a press on the room claims it");
        assertEquals("-1", eval("r.root.getAttribute('tabindex')").asString(), "never in the Tab order, but able to take the focus");
        assertTrue(eval("r.root.classList.has('wp_host')").asBoolean());
    }

    @Test
    void itRefusesToBeMadeWithoutABranchOrTheDocksFocus() {
        assertThrows(Exception.class, () -> eval("new WidgetPane(null, { focus: dockFocus })"));
        assertThrows(Exception.class, () -> eval("new WidgetPane(fakeBranch('x'), {})"));
    }

    // ── the tenant ──────────────────────────────────────────────────────────

    @Test
    void itHoldsOneTenant_andRetiresTheOneBefore() {
        eval("var r = room(); var a = widget('a'), b = widget('b'); r.setWidget(a)");
        assertTrue(eval("r.widget() === a && a.root.parentNode === r.root").asBoolean(), "the widget's root in the room");
        eval("log = []; r.setWidget(b)");
        assertEquals("a:dispose a:deregister", log(), "the tenant before is retired: disposed, and off its party");
        assertTrue(eval("a.root.parentNode === null && b.root.parentNode === r.root && r.widget() === b").asBoolean());
        assertThrows(Exception.class, () -> eval("r.setWidget({})"), "a widget gives back { root }");
    }

    @Test
    void aRoomWithNothingInItSaysSo_asItsTenant() {
        eval("var r = room(); r.say('Loading Books…')");
        assertEquals("Loading Books…", eval("r.widget().root.textContent").asString());
        assertEquals("status", eval("r.widget().root.getAttribute('role')").asString());
        eval("log = []; r.setWidget(widget('books'))");
        assertTrue(log().contains("dissolved:say"), "the line is retired like any tenant, its branch with it");
    }

    // ── the host ────────────────────────────────────────────────────────────

    @Test
    void theHostIsOnePerRoom_andCarriesANameAndTheParties() {
        eval("var r = room(); var h = r.host()");
        assertTrue(eval("r.host() === h && Object.isFrozen(h)").asBoolean(), "one handle, the same for the widget's whole life");
        assertTrue(eval("h.parties === parties").asBoolean(), "the parties the workspace exposes");
        eval("h.title('Report.md'); h.title(42)");
        assertEquals("Report.md,42", eval("titles.join(',')").asString(), "a name, to the holder, as text");
        eval("h.title(''); h.title('   '); h.title(null); h.title(undefined)");
        assertEquals(2, eval("titles.length").asInt(), "a blank name is no name");
    }

    @Test
    void aRoomNobodyListensToForNamesStillHandsOutAHost() {
        eval("var r = new WidgetPane(fakeBranch('quiet'), { focus: dockFocus })");
        eval("r.host().title('anything')");   // nowhere to go; not an error
        assertEquals(0, eval("Object.keys(r.host().parties).length").asInt(), "no parties when none are exposed");
    }

    // ── the keys ────────────────────────────────────────────────────────────

    @Test
    void toldToActivate_itClaimsTheKeys_andTheWidgetHearsWhenTheyArrive() {
        eval("var r = room(); var heard = 0; r.setWidget(widget('grid', { activate: function () { heard++; } })); r.activate()");
        assertTrue(eval("claimed.length === 1 && claimed[0] === membership").asBoolean(), "the room claims; a widget has nothing to claim with");
        assertEquals(0, eval("heard").asInt(), "the widget is told when the keys ARRIVE, not when they are asked for");
        eval("r.granted()");
        assertEquals(1, eval("heard").asInt());
        assertEquals("held", eval("r.root.getAttribute('data-keys')").asString());
    }

    @Test
    void theKeysAreLentWhileANativeControlInsideHasTheFocus() {
        eval("var r = room(); var w = widget('form'); var input = el('input'); w.root.appendChild(input); r.setWidget(w)");
        eval("w.activate = function () { document.activeElement = input; }; r.granted()");
        assertEquals("lent", eval("r.root.getAttribute('data-keys')").asString(), "the widget put the focus in its own control");
        eval("document.activeElement = document.body; r.root.fire('focusin', {})");
        assertEquals("held", eval("r.root.getAttribute('data-keys')").asString(), "and back to held when the focus leaves it");
        eval("r.taken(); document.activeElement = input; r.root.fire('focusin', {})");
        assertEquals("null", String.valueOf(eval("r.root.getAttribute('data-keys')")), "a room without the keys marks nothing, whatever has the focus");
    }

    @Test
    void itSaysWhenItIsOfferedTheKeys_withoutOverruling_heldOrLent() {
        eval("var r = room(); r.offered()");
        assertEquals("candidate", eval("r.root.getAttribute('data-keys')").asString());
        eval("r.withdrawn()");
        assertEquals("null", String.valueOf(eval("r.root.getAttribute('data-keys')")));
        eval("r.granted(); r.offered(); r.withdrawn()");
        assertEquals("held", eval("r.root.getAttribute('data-keys')").asString(), "an offer does not overrule the keys held");
    }

    @Test
    void aKeyGoesToTheWidgetFirst_andEscapeGivesTheKeysBack() {
        eval("var r = room(); var got = []; r.setWidget(widget('counter', { keyDown: function (e) { got.push(e.key); return e.key === 'ArrowUp'; } }))");
        assertTrue(eval("r.keyDown(ev('ArrowUp'))").asBoolean(), "taken by the widget");
        assertFalse(eval("r.keyDown(ev('x'))").asBoolean(), "not taken by anyone");
        assertEquals(0, eval("yielded.length").asInt());
        assertTrue(eval("r.keyDown(ev('Escape'))").asBoolean(), "the widget did not want it; the room does");
        assertTrue(eval("yielded.length === 1 && yielded[0] === membership").asBoolean(), "Escape gives the keys back to the dock");
        assertEquals("ArrowUp,x,Escape", eval("got.join(',')").asString(), "the widget was asked first each time");
    }

    @Test
    void aWidgetWithNoKeysLeavesEscapeToTheRoom() {
        eval("var r = room(); r.setWidget(widget('note'))");
        assertTrue(eval("r.keyDown(ev('Escape'))").asBoolean());
        assertEquals(1, eval("yielded.length").asInt());
    }

    // ── the seam with a widget's native controls ────────────────────────────

    @Test
    void anEscapeANativeControlDidNotTake_takesTheFocusOutOfIt() {
        eval("var r = room(); var w = widget('grid'); var cell = el('td'); w.root.appendChild(cell); r.setWidget(w); r.granted(); document.activeElement = cell");
        eval("var e = ev('Escape', cell); r.root.fire('keydown', e)");
        assertTrue(eval("e.prevented && e.stopped").asBoolean(), "the room takes that Escape");
        assertTrue(log().contains("blur:td"), "the control lets the focus go");
        assertEquals("held", eval("r.root.getAttribute('data-keys')").asString(), "the keys are the room's again; the next Escape is the room's own");
    }

    @Test
    void anEscapeTheControlTook_orOneOnTheRoomItself_isLeftAlone() {
        eval("var r = room(); var w = widget('grid'); var input = el('input'); w.root.appendChild(input); r.setWidget(w)");
        eval("var taken = ev('Escape', input, true); r.root.fire('keydown', taken)");
        assertFalse(eval("taken.prevented || taken.stopped").asBoolean(), "an Escape the control prevented (an editor cancelling) is the control's");
        eval("var own = ev('Escape', r.root); r.root.fire('keydown', own)");
        assertFalse(eval("own.prevented || own.stopped").asBoolean(), "an Escape on the room itself goes to the steward, the room's keyDown");
        eval("var other = ev('Enter', input); r.root.fire('keydown', other)");
        assertFalse(eval("other.prevented").asBoolean(), "only Escape is the seam's");
        assertFalse(log().contains("blur"), "nothing was blurred");
    }

    // ── the rest ────────────────────────────────────────────────────────────

    @Test
    void theWidgetHearsWhetherItsTabIsShowing() {
        eval("var r = room(); var seen = []; r.setWidget(widget('clock', { setActive: function (on) { seen.push(on); } })); r.setActive(true); r.setActive(0)");
        assertEquals("true,false", eval("seen.join(',')").asString());
        eval("r.setWidget(widget('plain')); r.setActive(true)");   // a widget with no setActive: nothing to tell, no error
    }

    @Test
    void itGoesWithEverythingInIt() {
        eval("var r = room(); r.setWidget(widget('last')); log = []; r.dispose()");
        assertTrue(log().contains("last:dispose") && log().contains("last:deregister"), "the tenant retired");
        assertEquals(1, eval("unclaimed").asInt(), "the press no longer claims");
        assertEquals(1, eval("membership.left").asInt(), "off the dock's branch");
        assertTrue(eval("roomBranch.dissolved && r.widget() === null").asBoolean(), "and its branch dissolved");
    }
}
