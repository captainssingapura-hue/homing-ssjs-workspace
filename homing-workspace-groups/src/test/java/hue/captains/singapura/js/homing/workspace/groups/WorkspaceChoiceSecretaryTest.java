package hue.captains.singapura.js.homing.workspace.groups;

import hue.captains.singapura.js.homing.ssjs.test.SecretaryTestBase;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The workspace choice secretary, kind by kind (Diligent Secretaries): every kind it handles, and what it keeps. */
class WorkspaceChoiceSecretaryTest extends SecretaryTestBase {

    static final String DIR = "/homing/js/hue/captains/singapura/js/homing/workspace/groups/";

    @BeforeEach
    void load() { loadSecretary(DIR + "WorkspaceChoiceSecretaryModule.js", "WorkspaceChoiceSecretary"); }

    private static Value state(Value step) { return step.getMember("newState"); }
    private Value chose(String kind) { return state(dispatch(initial(), envelope("Choose", Map.of("workspaceKind", kind), "kinds"))); }

    @Test
    void aKindChosenIsToldToEveryMember_andKept() {
        Value step = dispatch(initial(), envelope("Choose", Map.of("workspaceKind", "demo"), "kinds"));
        assertActionCount(step, 1);
        assertActionKind(step, 0, "BroadcastToMembers");
        assertEquals("Chosen", action(step, 0).getMember("message").getMember("kind").asString());
        assertEquals("demo", action(step, 0).getMember("message").getMember("workspaceKind").asString());
        assertEquals("demo", state(step).getMember("chosen").asString());
        assertEquals("kinds", state(step).getMember("lastChangedBy").asString());
        assertEquals(1, state(step).getMember("changes").asInt());
    }

    @Test
    void theSameKindAgainIsNothing() {
        Value step = dispatch(chose("demo"), envelope("Choose", Map.of("workspaceKind", "demo"), "instances"));
        assertActionCount(step, 0);
        assertEquals(1, state(step).getMember("changes").asInt());
        assertEquals("kinds", state(step).getMember("lastChangedBy").asString());
    }

    @Test
    void aQuestionIsAnsweredAlone_whenAKindIsChosen() {
        Value step = dispatch(chose("demo"), envelope("CurrentRequested", Map.of(), "instances"));
        assertActionCount(step, 1);
        assertActionKind(step, 0, "SendToMember");
        assertEquals("instances", action(step, 0).getMember("to").asString());
        assertEquals("demo", action(step, 0).getMember("message").getMember("workspaceKind").asString());
        assertActionCount(dispatch(initial(), envelope("CurrentRequested", Map.of(), "instances")), 0);
    }

    @Test
    void anOpenIsSaidToEveryMember_andCounted() {
        Value step = dispatch(chose("demo"), envelope("Open", Map.of("workspaceKind", "demo", "workspaceId", "w-1"), "instances"));
        assertActionCount(step, 1);
        Value said = action(step, 0).getMember("message");
        assertEquals("BroadcastToMembers Opening demo w-1", action(step, 0).getMember("kind").asString() + " " + said.getMember("kind").asString()
                + " " + said.getMember("workspaceKind").asString() + " " + said.getMember("workspaceId").asString());
        assertEquals(1, state(step).getMember("opened").asInt());
        assertEquals("instances", state(step).getMember("lastOpen").getMember("by").asString());
        assertEquals("demo", state(step).getMember("chosen").asString(), "an opening chooses nothing");
    }

    @Test
    void thePartysOwnWords_andStrangers_areKeptAsUnknown() {
        Value s = initial();
        for (String k : new String[]{"Chosen", "Opening", "Knock", "A", "B", "C"}) s = state(dispatch(s, envelope(k, Map.of(), "someone")));
        Value unknown = s.getMember("recentUnknown");
        assertEquals(5, unknown.getArraySize(), "the last five");
        assertEquals("Opening", unknown.getArrayElement(0).getMember("kind").asString());
        assertTrue(s.getMember("chosen").isNull());
    }

    @Test
    void theStateHandedInIsNeverChanged() {
        Value s = chose("demo");
        Value stringify = js.eval("js", "JSON.stringify");
        String before = stringify.execute(s).asString();
        dispatch(s, envelope("Choose", Map.of("workspaceKind", "books"), "kinds"));
        dispatch(s, envelope("Open", Map.of("workspaceKind", "demo", "workspaceId", ""), "kinds"));
        dispatch(s, envelope("Knock", Map.of(), "kinds"));
        assertEquals(before, stringify.execute(s).asString());
    }

    @Test
    void theTypeIsGeneratedFromJava_andAKindTravelsAsWorkspaceKind() {
        js.eval("js", WorkspaceChoice.TYPE.js());
        assertEquals("workspace-choice", js.eval("js", "WORKSPACE_CHOICE.name").asString());
        assertEquals("string", js.eval("js", "WORKSPACE_CHOICE.kinds.Open.workspaceId").asString());
        assertEquals("string", js.eval("js", "WORKSPACE_CHOICE.kinds.Chosen.workspaceKind").asString());
        assertEquals("Choose,Chosen,CurrentRequested,Open,Opening", js.eval("js", "Object.keys(WORKSPACE_CHOICE.kinds).sort().join()").asString());
    }
}
