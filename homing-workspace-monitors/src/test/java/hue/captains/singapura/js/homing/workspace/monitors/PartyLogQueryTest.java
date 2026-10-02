package hue.captains.singapura.js.homing.workspace.monitors;

import hue.captains.singapura.js.homing.workspace.monitors.PartyLogDeclaration.Params;
import hue.captains.singapura.js.homing.workspace.widgets.WidgetDeclaration;
import hue.captains.singapura.js.homing.workspace.widgets.WidgetQuery.Read;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The party log's one param on an address, and the monitor kinds as a host finds them. */
class PartyLogQueryTest {

    private static final PartyLogDeclaration.Query QUERY = new PartyLogDeclaration.Query();

    @Test
    void anAddressThatSaysNothingIsTheDefault() {
        assertEquals(new Read.Ok<>(Params.DEFAULT), QUERY.from(Map.of()));
        assertEquals(Map.of(), QUERY.to(Params.DEFAULT), "the default is written as nothing");
    }

    @Test
    void keepIsRead_andWrittenBackAsItWasRead() {
        var q = Map.of("keep", List.of("50"));
        var ok = assertInstanceOf(Read.Ok.class, QUERY.from(q));
        assertEquals(new Params(50), ok.params());
        assertEquals(q, QUERY.to((Params) ok.params()));
    }

    @Test
    void whatCannotBeReadIsRefused_namingItsKey() {
        for (String bad : List.of("", "many", "9", "1001", "-5", "2.5")) {
            var r = assertInstanceOf(Read.Refused.class, QUERY.from(Map.of("keep", List.of(bad))), bad);
            assertEquals("keep", r.key());
            assertEquals(bad, r.value());
        }
    }

    @Test
    void theKindsAreNamedAsKinds_eachConstructingOneClass() {
        assertEquals(List.of("focus-tree", "steward-lamp", "domops-tree", "party-log"),
                WorkspaceMonitorsCrate.KINDS.stream().map(WidgetDeclaration::kind).toList());
        for (var k : WorkspaceMonitorsCrate.KINDS) {
            assertTrue(WidgetDeclaration.KIND.matcher(k.kind()).matches(), k.kind());
            assertEquals(List.of("FocusTree", "StewardLamp", "DomOpsTree", "PartyLog").get(WorkspaceMonitorsCrate.KINDS.indexOf(k)), k.className());
        }
    }
}
