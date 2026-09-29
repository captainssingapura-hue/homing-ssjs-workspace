package hue.captains.singapura.js.homing.workspace.groups;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.graalvm.polyglot.PolyglotException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * The page's opener, in the root party a page runs: it chooses where the page is;
 * what it is asked to open it goes to, by the page's address - not where the page
 * already is, not where the page knows no address; and it hears nothing once left.
 */
class WorkspaceOpenerTest extends JsModuleTestBase {

    private static final String PARTY = "/homing/js/hue/captains/singapura/js/homing/workspace/parties/MessagingPartyModule.js";

    @BeforeEach
    void load() {
        loadModule(PARTY);
        loadModule(WorkspaceChoiceSecretaryTest.DIR + "WorkspaceChoiceSecretaryModule.js");
        loadModule(WorkspaceChoiceSecretaryTest.DIR + "WorkspaceOpenerModule.js");
        js.eval("js", WorkspaceChoice.TYPE.js());
        js.eval("js", """
            var console = { error: function () {} };
            var root = new MessagingParty(WORKSPACE_CHOICE, WorkspaceChoiceSecretary), went = [], wentNew = [], made = [], chosen = [];
            var switcher = root.join("switcher", { Chosen: function (m) { chosen.push(m.workspaceKind); } });
            function addressOf(kind, id) { return kind === "elsewhere" ? null : "/set/" + kind + (id ? "?ws_id=" + id : ""); }
            var opener = new WorkspaceOpener({ party: root, here: { workspaceKind: "books", workspaceId: "b-own", own: true },
                                               addressOf: addressOf, go: function (a) { went.push(a); }, goNew: function (a) { wentNew.push(a); },
                                               create: function (kind, name) { made.push(kind + ":" + name); return Promise.resolve("/set/" + kind + "?ws_id=new-" + made.length); } });
            """);
    }

    private String eval(String code) { return js.eval("js", code).toString(); }

    @Test
    void onJoiningItChoosesWhereThePageIs() {
        assertEquals("books", eval("chosen.join()"));
        assertEquals("books", eval("root.state().chosen"));
    }

    @Test
    void whatItIsAskedToOpen_itGoesTo_byThePagesAddress() {
        eval("switcher.tell({ kind: 'Open', workspaceKind: 'monitors', workspaceId: '' }); switcher.tell({ kind: 'Open', workspaceKind: 'books', workspaceId: 'b-2' })");
        assertEquals("/set/monitors|/set/books?ws_id=b-2", eval("went.join('|')"));
        assertEquals("went went", eval("opener.asked().map(function (a) { return a.did; }).join(' ')"));
    }

    @Test
    void notWhereThePageAlreadyIs_norWhereItKnowsNoAddress() {
        eval("switcher.tell({ kind: 'Open', workspaceKind: 'books', workspaceId: '' });"
                + "switcher.tell({ kind: 'Open', workspaceKind: 'books', workspaceId: 'b-own' });"
                + "switcher.tell({ kind: 'Open', workspaceKind: 'elsewhere', workspaceId: '' })");
        assertEquals("", eval("went.join('|')"));
        assertEquals("here here unknown", eval("opener.asked().map(function (a) { return a.did; }).join(' ')"));
    }

    @Test
    void aPageNotShowingTheKindsOwn_goesToIt() {
        eval("opener.leave(); opener = new WorkspaceOpener({ party: root, here: { workspaceKind: 'books', workspaceId: 'b-2', own: false },"
                + " addressOf: addressOf, go: function (a) { went.push(a); } });"
                + "switcher.tell({ kind: 'Open', workspaceKind: 'books', workspaceId: '' })");
        assertEquals("/set/books", eval("went.join('|')"));
    }

    @Test
    void aNewOneAskedFor_thePageMakesIt_andItIsGoneTo_hereOrInANewTab() {
        eval("switcher.tell({ kind: 'OpenNew', workspaceKind: 'books', workspaceName: 'Reading list', newTab: false });"
                + "switcher.tell({ kind: 'OpenNew', workspaceKind: 'monitors', workspaceName: '', newTab: true })");
        assertEquals("books:Reading list|monitors:", eval("made.join('|')"), "made as asked: a blank name, the catalogue's next");
        assertEquals("/set/books?ws_id=new-1", eval("went.join('|')"));
        assertEquals("/set/monitors?ws_id=new-2", eval("wentNew.join('|')"), "a new tab: goNew");
        assertEquals("making making", eval("opener.asked().map(function (a) { return a.did; }).join(' ')"));
    }

    @Test
    void withNoWayToMakeOne_aNewOneAskedForIsNotMade() {
        eval("opener.leave(); opener = new WorkspaceOpener({ party: root, here: { workspaceKind: 'books', workspaceId: 'b-own', own: true },"
                + " addressOf: addressOf, go: function (a) { went.push(a); } });"
                + "switcher.tell({ kind: 'OpenNew', workspaceKind: 'books', workspaceName: 'x', newTab: false })");
        assertEquals("", eval("went.join('|')"));
        assertEquals("unknown", eval("opener.asked().map(function (a) { return a.did; }).join(' ')"));
    }

    @Test
    void left_itHearsNothing() {
        eval("opener.leave(); switcher.tell({ kind: 'Open', workspaceKind: 'monitors', workspaceId: '' })");
        assertEquals("", eval("went.join('|')"));
        assertEquals("0", eval("root.inspect().refused.length"));
    }

    @Test
    void itIsMadeWithAllItNeeds() {
        assertThrows(PolyglotException.class, () -> eval("new WorkspaceOpener({ party: root, here: { workspaceKind: 'books' }, go: function () {} })"));
        assertThrows(PolyglotException.class, () -> eval("new WorkspaceOpener({ here: { workspaceKind: 'books' }, addressOf: addressOf, go: function () {} })"));
    }
}
