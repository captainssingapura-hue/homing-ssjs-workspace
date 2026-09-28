package hue.captains.singapura.js.homing.workspace.core;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The parties beside the core, headless: a root instance of a type made when a
 * widget first needs it, with the secretary given for it; a widget joined when
 * opened, given only what it declares, and left when closed - whether anything
 * shows it or not, which the core never knows.
 */
class WorkspacePartiesTest extends JsModuleTestBase {

    private static final String CORE = "/homing/js/hue/captains/singapura/js/homing/workspace/core/";
    private static final String PARTY = "/homing/js/hue/captains/singapura/js/homing/workspace/parties/MessagingPartyModule.js";

    private static final String SHIM = """
        var console = { error: function () {} };
        var CHOICE = Object.freeze({ name: "choice", kinds: Object.freeze({ Pick: Object.freeze({ what: "string" }), Picked: Object.freeze({ what: "string" }) }) });
        var OTHER = Object.freeze({ name: "other", kinds: Object.freeze({ Ping: Object.freeze({}) }) });
        var relay = { initial: { last: null }, behavior: function (s, env) {
            return env.message.kind === "Pick" ? { newState: { last: env.message.what }, actions: [{ kind: "BroadcastToMembers", message: { kind: "Picked", what: env.message.what } }] }
                                               : { newState: s, actions: [] }; } };
        var heard = [];
        class Chooser {
            constructor(container, params) { this.parties = [CHOICE]; this.name = container; }
            join(given) { var self = this; this.m = given.choice.join(this.name, { Picked: function (m) { heard.push(self.name + " heard " + m.what); } }); this.given = Object.keys(given).join(","); }
            leave() { if (this.m) { this.m.leave(); this.m = null; heard.push(this.name + " left"); } }
            pick(what) { this.m.tell({ kind: "Pick", what: what }); }
            dispose() { this.leave(); }
        }
        class Plain { constructor() {} dispose() {} }
        var core = new WorkspaceCore({ kinds: { chooser: { Widget: Chooser }, plain: { Widget: Plain } },
                                       placement: { lend: function (e) { return e.id; }, release: function () {} } });
        var parties = new WorkspaceParties(core, { secretaries: { choice: relay } }), made = [];
        parties.on(function (n) { made.push(n.name); });
        """;

    @BeforeEach
    void load() {
        loadModule(PARTY);
        loadModule(CORE + "WidgetIdsModule.js");
        loadModule(CORE + "WorkspaceCoreModule.js");
        loadModule(CORE + "WorkspacePartiesModule.js");
        js.eval("js", SHIM);
    }

    private String str(String src) { return js.eval("js", src).asString(); }

    @Test
    void aWidgetOpenedIsJoined_toARootInstanceMadeWhenFirstNeeded() {
        js.eval("js", "var a = core.open('chooser', {}).widget; var b = core.open('chooser', {}).widget; core.open('plain', {});");
        assertEquals("choice", str("made.join(',')"), "made once, when first needed; a widget declaring none needs none");
        assertEquals("choice", str("a.given"), "given what it declares, and no other");
        js.eval("js", "a.pick('Solaris')");
        assertEquals("chooser-1 heard Solaris | chooser-2 heard Solaris", str("heard.join(' | ')"));
        assertEquals("Solaris", str("parties.party('choice').state().last"), "the secretary given for the type");
    }

    @Test
    void aWidgetClosedLeaves_theOthersStillHear() {
        js.eval("js", "var a = core.open('chooser', {}).widget; var b = core.open('chooser', {}).widget; core.close('chooser-1'); heard = []; b.pick('QED')");
        assertEquals("chooser-2 heard QED", str("heard.join(' | ')"));
        assertEquals(1, js.eval("js", "parties.party('choice').members().length").asInt());
    }

    @Test
    void aTypeWithNoSecretaryIsRefused_whereAWidgetFirstNeedsIt() {
        String why = str("(function () { class Needy { constructor() { this.parties = [OTHER]; } join() {} dispose() {} }"
                + " var c = new WorkspaceCore({ kinds: { needy: { Widget: Needy } }, placement: { lend: function () {}, release: function () {} } });"
                + " new WorkspaceParties(c, { secretaries: {} }); try { c.open('needy', {}); return ''; } catch (e) { return e.message; } })()");
        assertTrue(why.contains("no secretary given for 'other'"), why);
    }
}
