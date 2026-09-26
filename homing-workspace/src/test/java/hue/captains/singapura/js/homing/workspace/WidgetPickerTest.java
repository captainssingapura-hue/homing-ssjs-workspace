package hue.captains.singapura.js.homing.workspace;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The picker over a fake DOM: a tile picks at once, and a tile greyed because
 * its widget is already open is a REDIRECTOR (RFC 0066 E3, keyboard §17.3) —
 * it asks first, in place of the tiles; Cancel comes back to them, Go to it
 * delivers "that one", and nothing moves before it is said.
 */
class WidgetPickerTest extends JsModuleTestBase {

    private static final String MODULE = "/homing/js/hue/captains/singapura/js/homing/workspace/WidgetPickerModule.js";

    private static final String SHIM = """
        function el(tag) {
            var classes = new Set(), attrs = {};
            return { tag: tag, children: [], parentNode: null, listeners: {}, textContent: "", offsetTop: 0,
                get firstChild() { return this.children[0] || null; },
                classList: { add: function (c) { classes.add(c); }, remove: function (c) { classes.delete(c); }, contains: function (c) { return classes.has(c); } },
                appendChild: function (c) { if (c.parentNode) c.parentNode.removeChild(c); this.children.push(c); c.parentNode = this; return c; },
                removeChild: function (c) { var i = this.children.indexOf(c); if (i >= 0) this.children.splice(i, 1); c.parentNode = null; return c; },
                setAttribute: function (k, v) { attrs[k] = String(v); }, getAttribute: function (k) { return attrs[k] == null ? null : attrs[k]; },
                addEventListener: function (t, fn) { (this.listeners[t] = this.listeners[t] || []).push(fn); },
                fire: function (t) { (this.listeners[t] || []).slice().forEach(function (fn) { fn({}); }); },
                focus: function () {}, has: function (c) { return classes.has(c); } };
        }
        // the party's rule: a name is free on its branch until the branch holding it dissolves
        function fakeBranch(name, parent) {
            var b = { name: name, names: new Set(), kids: new Map(),
                activate: function () { this.active = true; },
                createElement: function (n, tag) { if (!this.active) throw new Error("not activated: " + name); if (this.names.has(n)) throw new RangeError("name " + n + " is taken on " + name); this.names.add(n); return el(tag); },
                createBranch: function (n) { if (this.kids.has(n)) throw new RangeError("branch " + n + " is taken on " + name); var k = fakeBranch(n, this); this.kids.set(n, k); return k; },
                dissolve: function () { if (parent) parent.kids.delete(name); } };
            return b;
        }
        var css = { addClass: function (e) { for (var i = 1; i < arguments.length; i++) e.classList.add(arguments[i]); },
                    removeClass: function (e) { for (var i = 1; i < arguments.length; i++) e.classList.remove(arguments[i]); } };
        ["hwp_form", "hwp_form_actions", "hwp_form_btn", "hwp_form_btn_primary", "hwp_form_input", "hwp_form_label", "hwp_form_row",
         "hwp_grid", "hwp_tile", "hwp_tile_disabled", "hwp_tile_icon", "hwp_tile_label", "hwp_tile_on"].forEach(function (c) { globalThis[c] = c; });
        var picks = [], cancels = 0, host = el("host");
        var picker = new WidgetPicker(fakeBranch("picker"), {
            entries: [ { simpleName: "Books", label: "Books" }, { simpleName: "Note", label: "Note" } ],
            disabledIds: { Books: "tab-1" },
            onPick: function (e, p) { picks.push(e.simpleName + ":" + JSON.stringify(p)); },
            onCancel: function () { cancels++; } }).mountInto(host);
        function tile(i) { return picker._gridEl.children[i]; }
        function asking() { var b = host.children[0]; return b && b.getAttribute("role") === "alertdialog" ? b : null; }
        function button(text) { return asking().children[1].children.filter(function (c) { return c.textContent === text; })[0]; }
        function key(k) { return picker.keyDown({ key: k }); }
        """;

    @BeforeEach
    void load() {
        js = buildContext();
        loadModule(MODULE);
        js.eval("js", SHIM);
    }

    private Value eval(String src) { return js.eval("js", src); }

    @Test
    void aGreyedTileAsksFirst_andNothingIsDeliveredUntilGoToIt() {
        eval("tile(0).fire('click')");
        assertTrue(eval("asking() !== null && host.children.length === 1 && picker._gridEl.parentNode === null").asBoolean(), "it asks, in place of the tiles");
        assertEquals("Books is already open. Go to it?", eval("asking().children[0].textContent").asString());
        assertEquals("", eval("picks.join()").asString(), "nothing delivered, nothing moved");
        eval("button('Go to it').fire('click')");
        assertEquals("Books:null", eval("picks.join()").asString(), "that one, by the user's word");
    }

    @Test
    void cancelComesBackToTheTiles_andItCanBeAskedAgain() {
        eval("tile(0).fire('click'); button('Cancel').fire('click')");
        assertTrue(eval("asking() === null && host.children[0] === picker._gridEl").asBoolean(), "the tiles back");
        assertEquals("", eval("picks.join()").asString());
        assertEquals(0, eval("cancels").asInt(), "the chooser itself is not cancelled");
        eval("tile(0).fire('click')");
        assertTrue(eval("asking() !== null").asBoolean(), "asked again, on a branch of its own");
    }

    @Test
    void byKeys_EnterAsks_EscapeComesBack_EnterGoes() {
        eval("key('Home'); key('Enter')");
        assertTrue(eval("asking() !== null").asBoolean(), "Enter on the greyed tile asks");
        assertFalse(eval("key('ArrowRight')").asBoolean(), "while it asks, the tiles' keys are not its");
        eval("key('Escape')");
        assertTrue(eval("asking() === null && cancels === 0").asBoolean(), "Escape comes back to the tiles, and cancels nothing");
        eval("key('Enter'); key('Enter')");
        assertEquals("Books:null", eval("picks.join()").asString(), "Enter again goes");
    }

    @Test
    void anOpenTile_stillPicksAtOnce() {
        eval("tile(1).fire('click')");
        assertEquals("Note:{}", eval("picks.join()").asString());
        assertTrue(eval("asking() === null").asBoolean());
    }
}
