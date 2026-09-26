package hue.captains.singapura.js.homing.workspace.codecs.log;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import hue.captains.singapura.js.homing.workspace.codecs.WorkspaceLogCodecsModule;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The generated JavaScript: what the Java records declare, the classes hold —
 * a wire value decodes to frozen typed instances and encodes back to the same
 * text; anything the records would refuse, the classes refuse.
 */
class WorkspaceLogJsGenTest extends JsModuleTestBase {

    private static final String MODULE = "/homing/js/hue/captains/singapura/js/homing/workspace/codecs/WorkspaceLogCodecsModule.js";

    @BeforeEach
    void load() {
        js = buildContext();
        loadModule(MODULE);
    }

    /** Decode the wire text with a codec, encode it back, and return the text. */
    private String roundTrip(String codec, String wireText) {
        return js.eval("js", "JSON.stringify(" + codec + ".transformTo(" + codec + ".transformFrom(JSON.parse("
                + jsString(wireText) + "))))").asString();
    }

    private static String jsString(String s) {
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    private void refused(String codec, String wireText, String because) {
        var e = assertThrows(PolyglotException.class, () -> roundTrip(codec, wireText), wireText);
        assertTrue(e.getMessage().contains(because), () -> "expected '" + because + "' in: " + e.getMessage());
    }

    @Test
    void theModuleExportsWhatTheGeneratorEmits() {
        var declared = WorkspaceLogCodecsModule.INSTANCE.exports().exports().stream()
                .map(x -> x.getClass().getSimpleName()).toList();
        assertEquals(WorkspaceLogJsGen.exportedNames(), declared);
        for (String n : declared) assertTrue(global(n).canInstantiate() || global(n).hasMembers(), n);
    }

    @Test
    void everyEventRoundTripsToTheSameText() {
        for (String line : List.of(
                "{\"seq\":1,\"at\":1790000000000,\"event\":{\"type\":\"TabOpened\",\"id\":\"tab-1\",\"kind\":\"opener\",\"title\":\"Open\",\"host\":{\"type\":\"InRegion\",\"id\":\"main\"},\"index\":0}}",
                "{\"seq\":2,\"at\":1790000000001,\"event\":{\"type\":\"TabBecame\",\"id\":\"tab-1\",\"kind\":\"note\",\"title\":\"Note 2\"}}",
                "{\"seq\":3,\"at\":1790000000002,\"event\":{\"type\":\"TabRenamed\",\"id\":\"tab-1\",\"title\":\"Groceries\"}}",
                "{\"seq\":4,\"at\":1790000000003,\"event\":{\"type\":\"TabMoved\",\"id\":\"tab-1\",\"host\":{\"type\":\"InRegion\",\"id\":\"cell-2\"},\"index\":3}}",
                "{\"seq\":5,\"at\":1790000000004,\"event\":{\"type\":\"TabShown\",\"host\":{\"type\":\"InRegion\",\"id\":\"main\"},\"id\":\"tab-1\"}}",
                "{\"seq\":6,\"at\":1790000000005,\"event\":{\"type\":\"FloatOpened\",\"id\":\"float-1\",\"x\":40,\"y\":-12,\"w\":320,\"h\":220}}",
                "{\"seq\":7,\"at\":1790000000006,\"event\":{\"type\":\"TabMoved\",\"id\":\"tab-1\",\"host\":{\"type\":\"InFloat\",\"id\":\"float-1\"},\"index\":0}}",
                "{\"seq\":8,\"at\":1790000000007,\"event\":{\"type\":\"FloatMoved\",\"id\":\"float-1\",\"x\":300,\"y\":180}}",
                "{\"seq\":9,\"at\":1790000000008,\"event\":{\"type\":\"FloatResized\",\"id\":\"float-1\",\"w\":480,\"h\":260}}",
                "{\"seq\":10,\"at\":1790000000009,\"event\":{\"type\":\"FloatRaised\",\"id\":\"float-1\"}}",
                "{\"seq\":11,\"at\":1790000000010,\"event\":{\"type\":\"FloatClosed\",\"id\":\"float-1\"}}",
                "{\"seq\":12,\"at\":1790000000011,\"event\":{\"type\":\"TabClosed\",\"id\":\"tab-1\"}}",
                "{\"seq\":13,\"at\":1790000000012,\"event\":{\"type\":\"RegionParted\",\"region\":\"main\",\"newRegion\":\"cell-2\",\"side\":\"RIGHT\"}}",
                "{\"seq\":14,\"at\":1790000000013,\"event\":{\"type\":\"RegionRemoved\",\"region\":\"cell-2\",\"toward\":\"main\"}}",
                "{\"seq\":16,\"at\":1790000000015,\"event\":{\"type\":\"RegionRemoved\",\"region\":\"cell-3\",\"toward\":null}}",
                "{\"seq\":15,\"at\":1790000000014,\"event\":{\"type\":\"TracksChanged\",\"path\":\"0/1\",\"shares\":[{\"units\":333333,\"scale\":6},{\"units\":666667,\"scale\":6}]}}")) {
            assertEquals(line, roundTrip("LoggedEventCodec", line));
        }
        String header = "{\"format\":\"homing.workspace.log\",\"version\":2,\"kind\":\"focus-lab\",\"workspaceId\":\"7f1b6c2e-5000-9000-7f1b-6c2e00000001\"}";
        assertEquals(header, roundTrip("LogHeaderCodec", header));
    }

    @Test
    void aRecordsConstantsAreTheJavaOnes() {
        assertEquals("homing.workspace.log 2 9007199254740991",
                js.eval("js", "LogHeader.FORMAT + ' ' + LogHeader.VERSION + ' ' + Scaled.MAX_SAFE").asString());
        assertTrue(js.eval("js", "new InFloat(new FloatId('float-2')) instanceof Host && !(new InRegion(new RegionId('main')) instanceof InFloat)").asBoolean());
    }

    @Test
    void decodedValuesAreTypedAndFrozen() {
        Value e = js.eval("js", "LoggedEventCodec.transformFrom({seq:1,at:5,event:{type:'TabClosed',id:'tab-9'}})");
        assertTrue(js.eval("js", "(e) => e.event instanceof WorkspaceEvent && e.event instanceof TabClosed && e.event.id instanceof TabId && Object.isFrozen(e) && Object.isFrozen(e.event)").execute(e).asBoolean());
        assertTrue(js.eval("js", "(e) => Side.of('LEFT') === Side.LEFT && SideCodec.transformFrom('TOP') === Side.TOP").execute(e).asBoolean());
    }

    @Test
    void refusesWhatTheRecordsRefuse() {
        refused("TabIdCodec", "\"tab 1\"", "does not match");
        refused("TabIdCodec", "7", "expected a string");
        refused("WorkspaceEventCodec", "{\"type\":\"TabMoved\",\"id\":\"t\",\"host\":{\"type\":\"InRegion\",\"id\":\"r\"},\"index\":1.5}", "TabMoved.index");
        refused("WorkspaceEventCodec", "{\"type\":\"TabMoved\",\"id\":\"t\",\"host\":{\"type\":\"InPocket\",\"id\":\"r\"},\"index\":1}", "Host.type");
        refused("WorkspaceEventCodec", "{\"type\":\"TabMoved\",\"id\":\"t\",\"region\":\"r\",\"index\":1}", "no member \"region\"");
        refused("WorkspaceEventCodec", "{\"type\":\"FloatOpened\",\"id\":\"f\",\"x\":0.5,\"y\":0,\"w\":1,\"h\":1}", "FloatOpened.x");
        refused("WorkspaceEventCodec", "{\"type\":\"TabClosed\",\"id\":\"t\",\"extra\":1}", "no member \"extra\"");
        refused("WorkspaceEventCodec", "{\"type\":\"TabClosed\"}", "\"id\" is missing");
        refused("WorkspaceEventCodec", "{\"type\":\"TabGone\",\"id\":\"t\"}", "no variant");
        refused("SideCodec", "\"left\"", "no constant");
        refused("WorkspaceInstanceIdCodec", "\"7F1B6C2E-5000-9000-7F1B-6C2E00000001\"", "lower-case UUID");
        refused("LoggedEventCodec", "{\"seq\":9007199254740992,\"at\":1,\"event\":{\"type\":\"TabClosed\",\"id\":\"t\"}}", "EventSeq.value");
        refused("SplitPathCodec", "\"0//1\"", "does not match");
        assertThrows(PolyglotException.class, () -> js.eval("js", "new WorkspaceEvent()"));
        assertThrows(PolyglotException.class, () -> js.eval("js", "new Side('LEFT')"));
    }
}
