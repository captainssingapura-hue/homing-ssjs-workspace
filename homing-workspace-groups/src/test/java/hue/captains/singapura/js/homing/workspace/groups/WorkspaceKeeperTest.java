package hue.captains.singapura.js.homing.workspace.groups;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.graalvm.polyglot.PolyglotException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * The page's keeper, in the root party a page runs: what it is asked of the
 * workspaces it has the page do, and says how it went - done, refused, failed -
 * to every member; and a page with more than one party is told, to tell the rest.
 */
class WorkspaceKeeperTest extends JsModuleTestBase {

    private static final String PARTY = "/homing/js/hue/captains/singapura/js/homing/workspace/parties/MessagingPartyModule.js";

    @BeforeEach
    void load() {
        loadModule(PARTY);
        loadModule(WorkspaceChoiceSecretaryTest.DIR + "WorkspaceChoiceSecretaryModule.js");
        loadModule(WorkspaceChoiceSecretaryTest.DIR + "WorkspaceKeeperModule.js");
        js.eval("js", WorkspaceChoice.TYPE.js());
        js.eval("js", """
            var console = { error: function () {} };
            var root = new MessagingParty(WORKSPACE_CHOICE, WorkspaceChoiceSecretary), heard = [], did = [], done = [];
            var view = root.join("view", { Reported: function (m) { heard.push(m.workspaceKind + ":" + m.changed + ":" + m.note); } });
            var keeper = new WorkspaceKeeper({ party: root,
                rename: function (kind, id, name) {
                    did.push("rename " + id + " " + name);
                    return name === "taken" ? Promise.reject(new TypeError("a books workspace is called \\"taken\\" already"))
                                            : Promise.resolve({ changed: true, note: "renamed \\"" + name + "\\"" });
                },
                remove: function (kind, id) { did.push("remove " + id); return Promise.resolve({ changed: false, note: "this page shows it" }); },
                done: function (kind, changed) { done.push(kind + ":" + changed); } });
            """);
    }

    private String eval(String code) { return js.eval("js", code).toString(); }

    @Test
    void whatItIsAskedItHasThePageDo_andSaysHowItWent() {
        eval("view.tell({ kind: 'Rename', workspaceKind: 'books', workspaceId: 'w-2', workspaceName: 'Reading' })");
        assertEquals("rename w-2 Reading", eval("did.join('|')"));
        assertEquals("books:true:renamed \"Reading\"", eval("heard.join('|')"), "changed, and the note, to every view");
        assertEquals("books:true", eval("done.join('|')"), "and the page told, to tell its other parties");
    }

    @Test
    void refusedOrFailed_itSaysWhy_andNothingChanged() {
        eval("view.tell({ kind: 'Rename', workspaceKind: 'books', workspaceId: 'w-2', workspaceName: 'taken' });"
                + "view.tell({ kind: 'Delete', workspaceKind: 'books', workspaceId: 'w-2' })");
        assertEquals("books:false:a books workspace is called \"taken\" already|books:false:this page shows it", eval("heard.join('|')"));
        assertEquals("false false", eval("keeper.reported().map(function (r) { return r.changed; }).join(' ')"));
    }

    @Test
    void withNoWayToDoIt_itSaysSo() {
        eval("keeper.leave(); keeper = new WorkspaceKeeper({ party: root }); view.tell({ kind: 'Delete', workspaceKind: 'books', workspaceId: 'w-2' })");
        assertEquals("books:false:this page cannot have a workspace deleted", eval("heard.join('|')"));
    }

    @Test
    void itReportsWhenThePageTellsIt_forAChangeAnotherPartysKeeperMade() {
        eval("keeper.report('books', '', true)");
        assertEquals("books:true:", eval("heard.join('|')"));
        assertThrows(PolyglotException.class, () -> eval("new WorkspaceKeeper({})"));
    }
}
