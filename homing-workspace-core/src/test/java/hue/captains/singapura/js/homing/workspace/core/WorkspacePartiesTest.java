package hue.captains.singapura.js.homing.workspace.core;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The parties beside the core, headless: the workspace's root parties made
 * with it, before any widget, each with the secretary at its root; a widget
 * joined when opened - given its kind's types, not what it says of itself -
 * and left when closed, whether anything shows it or not, which the core never
 * knows.
 */
class WorkspacePartiesTest extends JsModuleTestBase {

    private static final String CORE = "/homing/js/hue/captains/singapura/js/homing/workspace/core/";
    private static final String PARTY = "/homing/js/hue/captains/singapura/js/homing/workspace/parties/MessagingPartyModule.js";

    private static final String SHIM = """
        var errors = [];
        var console = { error: function (m) { errors.push(m); } };
        var CHOICE = Object.freeze({ name: "choice", kinds: Object.freeze({ Pick: Object.freeze({ what: "string" }), Picked: Object.freeze({ what: "string" }) }) });
        var OTHER = Object.freeze({ name: "other", kinds: Object.freeze({ Ping: Object.freeze({}) }) });
        var relay = { initial: { last: null }, behavior: function (s, env) {
            return env.message.kind === "Pick" ? { newState: { last: env.message.what }, actions: [{ kind: "BroadcastToMembers", message: { kind: "Picked", what: env.message.what } }] }
                                               : { newState: s, actions: [] }; } };
        var heard = [];
        class Chooser {
            constructor(container, params) { this.parties = [OTHER]; this.name = container; }
            join(given) { var self = this; this.m = given.choice.join(this.name, { Picked: function (m) { heard.push(self.name + " heard " + m.what); } }); this.given = Object.keys(given).join(","); }
            leave() { if (this.m) { this.m.leave(); this.m = null; heard.push(this.name + " left"); } }
            pick(what) { this.m.tell({ kind: "Pick", what: what }); }
            dispose() { this.leave(); }
        }
        class Plain { constructor() {} dispose() {} }
        class Deaf { constructor() {} dispose() {} }
        var kinds = { chooser: { Widget: Chooser, parties: [CHOICE] }, plain: { Widget: Plain, parties: [] }, deaf: { Widget: Deaf, parties: [CHOICE] } };
        var core = new WorkspaceCore({ kinds: kinds, panes: { lend: function (e) { return e.id; }, rename: function () {}, release: function () {} },
                                       placement: { mount: function () {}, unmount: function () {} } });
        function open(kind) { return core.execute(WorkspaceRequest.open(kind, {}, null)); }
        function close(id) { core.execute(WorkspaceRequest.close(id)); }
        var parties = new WorkspaceParties(core, { parties: [{ type: CHOICE, secretary: relay }], kinds: kinds });
        """;

    @BeforeEach
    void load() {
        loadModule(PARTY);
        loadModule(CORE + "WidgetIdsModule.js");
        loadModule(CORE + "WorkspaceRequestModule.js");
        loadModule(CORE + "WorkspaceCoreModule.js");
        loadModule(CORE + "WorkspacePartiesModule.js");
        js.eval("js", SHIM);
    }

    private String str(String src) { return js.eval("js", src).asString(); }

    @Test
    void theRootPartiesAreMadeWithTheWorkspace_andAWidgetIsGivenItsKindsTypes() {
        assertEquals("choice", str("parties.names().join(',')"), "made before any widget opens");
        js.eval("js", "var a = open('chooser').widget; var b = open('chooser').widget; open('plain');");
        assertEquals("choice", str("a.given"), "its kind's types - not the OTHER it says of itself");
        js.eval("js", "a.pick('Solaris')");
        assertEquals("chooser-1 heard Solaris | chooser-2 heard Solaris", str("heard.join(' | ')"));
        assertEquals("Solaris", str("parties.party('choice').state().last"), "the secretary at its root");
        assertEquals(true, js.eval("js", "parties.party('other') === null").asBoolean(), "no type the workspace's kinds do not declare");
    }

    @Test
    void aWidgetClosedLeaves_theOthersStillHear() {
        js.eval("js", "var a = open('chooser').widget; var b = open('chooser').widget; close('chooser-1'); heard = []; b.pick('QED')");
        assertEquals("chooser-2 heard QED", str("heard.join(' | ')"));
        assertEquals(1, js.eval("js", "parties.party('choice').members().length").asInt());
    }

    @Test
    void aKindGivenATypeWithNoRootParty_isRefusedBeforeAnyWidget_andAWidgetThatCannotJoinIsSaid() {
        String why = str("(function () { try { new WorkspaceParties(core, { parties: [], kinds: kinds }); return ''; } catch (e) { return e.message; } })()");
        assertTrue(why.contains("the kind 'chooser' is given 'choice', which has no root party"), why);
        js.eval("js", "open('deaf')");
        assertEquals(1, js.eval("js", "core.entries().length").asInt(), "opened all the same: a notice's listener does not undo it");
        assertTrue(str("errors.join('|')").contains("'deaf-1' is of a kind given choice, and cannot join"), str("errors.join('|')"));
    }
}
