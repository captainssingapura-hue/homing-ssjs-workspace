package hue.captains.singapura.js.homing.workspace.groups;

import hue.captains.singapura.js.homing.ssjs.test.SecretaryTestBase;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A switcher's scope, at its edge (Diligent Secretaries): a kind chosen goes up
 * when it changed what is chosen; an open always goes up; a question never does;
 * a kind chosen above is taken as the scope's own and told to it, never sent back
 * up; an opening above is heard - and then in two parties, the scope linked under
 * one above, as a page runs them.
 */
class WorkspaceSwitcherSecretaryTest extends SecretaryTestBase {

    private static final String DIR = WorkspaceChoiceSecretaryTest.DIR;
    private static final String PARTY = "/homing/js/hue/captains/singapura/js/homing/workspace/parties/MessagingPartyModule.js";

    @BeforeEach
    void load() {
        loadModule(DIR + "WorkspaceChoiceSecretaryModule.js");
        loadSecretary(DIR + "WorkspaceSwitcherSecretaryModule.js", "WorkspaceSwitcherSecretary");
    }

    private static Value chosen(Value step) { return step.getMember("newState").getMember("choice").getMember("chosen"); }
    private static int count(Value step, String field) { return step.getMember("newState").getMember(field).asInt(); }
    private static String kinds(Value step) {
        var out = new StringBuilder();
        Value a = step.getMember("actions");
        for (int i = 0; i < a.getArraySize(); i++) out.append(i == 0 ? "" : " ").append(a.getArrayElement(i).getMember("kind").asString())
                .append(":").append(a.getArrayElement(i).getMember("message").getMember("kind").asString());
        return out.toString();
    }
    private Value choseDemo() { return dispatch(initial(), envelope("Choose", Map.of("workspaceKind", "demo"), "kinds")).getMember("newState"); }

    @Test
    void whatGoesUpIsDeclared_kindByKind() {
        Value b = secretary.getMember("BUBBLES");
        assertTrue(b.getMember("Choose").asBoolean());
        assertTrue(b.getMember("Open").asBoolean());
        assertTrue(b.getMember("OpenNew").asBoolean());
        assertTrue(b.getMember("Rename").asBoolean());
        assertTrue(b.getMember("Delete").asBoolean());
        assertTrue(!b.getMember("Report").asBoolean(), "how it went is the keeper's word, never a view's");
        assertTrue(!b.getMember("CurrentRequested").asBoolean());
    }

    @Test
    void aKindChosenInTheScopeIsToldToTheScope_andGoesUp() {
        Value step = dispatch(initial(), envelope("Choose", Map.of("workspaceKind", "demo"), "kinds"));
        assertEquals("BroadcastToMembers:Chosen SendToParent:Choose", kinds(step));
        assertEquals("demo", chosen(step).asString());
        assertEquals(1, count(step, "bubbled"));
        assertEquals(0, count(step, "kept"));
    }

    @Test
    void theSameKindAgainIsKeptHere_andNothingGoesUp() {
        Value step = dispatch(choseDemo(), envelope("Choose", Map.of("workspaceKind", "demo"), "instances"));
        assertEquals("", kinds(step));
        assertEquals(1, count(step, "kept"));
    }

    @Test
    void anOpenAlwaysGoesUp_saidInTheScopeToo() {
        Value step = dispatch(choseDemo(), envelope("Open", Map.of("workspaceKind", "demo", "workspaceId", "w-1"), "instances"));
        assertEquals("BroadcastToMembers:Opening SendToParent:Open", kinds(step));
        Value again = dispatch(step.getMember("newState"), envelope("Open", Map.of("workspaceKind", "demo", "workspaceId", "w-1"), "instances"));
        assertEquals("BroadcastToMembers:Opening SendToParent:Open", kinds(again), "opening twice is asked twice");
        assertEquals(2, again.getMember("newState").getMember("choice").getMember("opened").asInt());
    }

    @Test
    void aQuestionIsAnsweredInTheScope_neverUp() {
        Value step = dispatch(choseDemo(), envelope("CurrentRequested", Map.of(), "instances"));
        assertEquals("SendToMember:Chosen", kinds(step));
        assertEquals(1, count(step, "kept"));
    }

    @Test
    void aKindChosenAboveIsTakenAsTheScopesOwn_toldToIt_neverSentBackUp() {
        Value step = dispatch(choseDemo(), envelope("Chosen", Map.of("workspaceKind", "books"), "upstream"));
        assertEquals("BroadcastToMembers:Chosen", kinds(step));
        assertEquals("books", chosen(step).asString());
        assertEquals("upstream", step.getMember("newState").getMember("choice").getMember("lastChangedBy").asString());
        assertEquals(1, count(step, "adopted"));
        Value again = dispatch(step.getMember("newState"), envelope("Chosen", Map.of("workspaceKind", "books"), "upstream"));
        assertEquals("", kinds(again), "the same again is nothing");
        assertEquals(1, count(again, "adopted"));
    }

    @Test
    void aNewOneAskedForAlwaysGoesUp_saidInTheScopeToo_andItsOpeningAboveHeard() {
        Value step = dispatch(choseDemo(), envelope("OpenNew", Map.of("workspaceKind", "books", "workspaceName", "", "newTab", false), "instances"));
        assertEquals("BroadcastToMembers:OpeningNew SendToParent:OpenNew", kinds(step));
        Value heard = dispatch(step.getMember("newState"), envelope("OpeningNew", Map.of("workspaceKind", "books", "workspaceName", "", "newTab", false), "upstream"));
        assertEquals("", kinds(heard));
        assertEquals(1, count(heard, "heard"));
    }

    @Test
    void howAnAskingWent_saidAbove_isToldToTheScope_andTheAskingsAboveHeard() {
        Value up = dispatch(choseDemo(), envelope("Rename", Map.of("workspaceKind", "demo", "workspaceId", "w-1", "workspaceName", "x"), "instances"));
        assertEquals("BroadcastToMembers:Renaming SendToParent:Rename", kinds(up));
        Value heard = dispatch(up.getMember("newState"), envelope("Renaming", Map.of("workspaceKind", "demo", "workspaceId", "w-1", "workspaceName", "x"), "upstream"));
        assertEquals("", kinds(heard));
        Value told = dispatch(heard.getMember("newState"), envelope("Reported", Map.of("workspaceKind", "demo", "note", "renamed", "changed", true), "upstream"));
        assertEquals("BroadcastToMembers:Reported", kinds(told));
        assertEquals(1, count(told, "relayed"));
    }

    @Test
    void anOpeningAboveIsHeard_andNothingIsDone() {
        Value step = dispatch(choseDemo(), envelope("Opening", Map.of("workspaceKind", "demo", "workspaceId", "w-1"), "upstream"));
        assertEquals("", kinds(step));
        assertEquals(1, count(step, "heard"));
    }

    @Test
    void aMembersWordFromAboveIsKeptAsUnknown_andNothingIsDone() {
        Value step = dispatch(choseDemo(), envelope("Choose", Map.of("workspaceKind", "books"), "upstream"));
        assertEquals("", kinds(step));
        assertEquals("demo", chosen(step).asString());
        Value unknown = step.getMember("newState").getMember("choice").getMember("recentUnknown");
        assertEquals("Choose upstream", unknown.getArrayElement(0).getMember("kind").asString() + " " + unknown.getArrayElement(0).getMember("from").asString());
    }

    @Test
    void theStateHandedInIsNeverChanged() {
        Value s = choseDemo();
        Value stringify = js.eval("js", "JSON.stringify");
        String before = stringify.execute(s).asString();
        dispatch(s, envelope("Choose", Map.of("workspaceKind", "books"), "kinds"));
        dispatch(s, envelope("Chosen", Map.of("workspaceKind", "books"), "upstream"));
        dispatch(s, envelope("Opening", Map.of("workspaceKind", "books", "workspaceId", ""), "upstream"));
        dispatch(s, envelope("Knock", Map.of(), "upstream"));
        assertEquals(before, stringify.execute(s).asString());
    }

    /**
     * The scope, linked under a party above - the workspace choice secretary's: the kinds'
     * choice reaches the instances and goes up; a choice made above reaches both; an open goes
     * up, and the party above says it; nothing rings for ever.
     */
    @Test
    void linkedUnderAPartyAbove_aChoiceGoesUpAndComesDownToBoth_andAnOpenIsSaidAbove() {
        loadModule(PARTY);
        js.eval("js", WorkspaceChoice.TYPE.js());
        js.eval("js", """
            var console = { error: function () {} };
            var above = new MessagingParty(WORKSPACE_CHOICE, WorkspaceChoiceSecretary), heard = [];
            var page = above.join("page", { Chosen: function (m) { heard.push("page " + m.workspaceKind); },
                                            Opening: function (m) { heard.push("page opens " + m.workspaceKind + "/" + m.workspaceId); } });
            var scope = new MessagingParty(WORKSPACE_CHOICE, WorkspaceSwitcherSecretary);
            scope.link(above, "switcher").tell({ kind: "CurrentRequested" });
            var kinds = scope.join("kinds", { Chosen: function (m) { heard.push("kinds " + m.workspaceKind); } });
            var instances = scope.join("instances", { Chosen: function (m) { heard.push("instances " + m.workspaceKind); } });
            kinds.tell({ kind: "Choose", workspaceKind: "demo" });
            """);
        assertEquals("kinds demo | instances demo | page demo", js.eval("js", "heard.join(' | ')").asString(),
                "told to the scope, then up as the switcher's word, and the party above told its members");
        js.eval("js", "heard = []; page.tell({ kind: 'Choose', workspaceKind: 'books' })");
        assertEquals("page books | kinds books | instances books", js.eval("js", "heard.join(' | ')").asString(), "a choice above reaches both");
        js.eval("js", "heard = []; instances.tell({ kind: 'Open', workspaceKind: 'books', workspaceId: 'w-7' })");
        assertEquals("page opens books/w-7", js.eval("js", "heard.join(' | ')").asString(), "the party above says it, for whoever opens");
        assertEquals(1, js.eval("js", "scope.state().adopted").asInt());
        assertEquals(1, js.eval("js", "scope.state().heard").asInt(), "its own opening, said back down, is heard");
        assertEquals(2, js.eval("js", "scope.state().bubbled").asInt(), "what came down was not sent back up");
        assertEquals(0, js.eval("js", "scope.inspect().refused.length + above.inspect().refused.length").asInt());
    }
}
