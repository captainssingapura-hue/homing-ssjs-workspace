package hue.captains.singapura.js.homing.workspace.log.store;

import hue.captains.singapura.js.homing.workspace.log.js.WorkspaceLogCodecCrate;
import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import hue.captains.singapura.js.homing.workspace.log.TabEvent;
import hue.captains.singapura.js.homing.workspace.log.RegionEvent;
import hue.captains.singapura.js.homing.workspace.log.FloatEvent;
import hue.captains.singapura.js.homing.workspace.log.LogIds.EventSeq;
import hue.captains.singapura.js.homing.workspace.log.LogIds.FloatId;
import hue.captains.singapura.js.homing.workspace.log.Host;
import hue.captains.singapura.js.homing.workspace.log.LogHeader;
import hue.captains.singapura.js.homing.workspace.log.LoggedEvent;
import hue.captains.singapura.js.homing.workspace.log.LogIds.RegionId;
import hue.captains.singapura.js.homing.workspace.log.Scaled;
import hue.captains.singapura.js.homing.workspace.log.RegionEvent.Side;
import hue.captains.singapura.js.homing.workspace.log.LogIds.TabId;
import hue.captains.singapura.js.homing.workspace.log.WorkspaceEvent;
import hue.captains.singapura.js.homing.workspace.log.LogIds.WorkspaceKind;
import hue.captains.singapura.js.homing.workspace.log.json.Json;
import hue.captains.singapura.js.homing.workspace.log.LogIds.SplitPath;
import hue.captains.singapura.js.homing.workspace.log.LogIds.WidgetKind;
import hue.captains.singapura.js.homing.workspace.log.LogIds.WidgetTitle;
import hue.captains.singapura.js.homing.workspace.log.LogIds.WorkspaceInstanceId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The parity the log exists for: the same log is the same bytes in either
 * language. A file Java writes, JavaScript reads into its generated classes and
 * writes back unchanged; a file JavaScript writes, Java reads into its records
 * and writes back unchanged. The corpus leans on what differs between the two
 * when nobody is careful: escapes, surrogates, the largest safe integers.
 */
class WorkspaceLogParityTest extends JsModuleTestBase {

    private static final String EXPORT = "/homing/js/hue/captains/singapura/js/homing/workspace/log/store/WorkspaceLogExportModule.js";

    @BeforeEach
    void load() {
        js = buildContext();
        for (String script : WorkspaceLogCodecCrate.scripts()) loadModule(script);
        loadModule(EXPORT);
    }

    private static final String AWKWARD = "q\" b\\ \b\f\n\r\t \u0001\u001f \u007f é ☃ 😀 \ud800 lone \udc00 / </script>";

    private static List<LoggedEvent> corpus() {
        var t1 = TabId.of("tab-1");
        var main = RegionId.of("main");
        var events = List.<WorkspaceEvent>of(
                new TabEvent.TabOpened(t1, WidgetKind.of("opener"), WidgetTitle.of("Open"), new Host.InRegion(main), 0),
                new TabEvent.TabBecame(t1, WidgetKind.of("note"), WidgetTitle.of(AWKWARD)),
                new RegionEvent.RegionParted(main, RegionId.of("cell_2"), Side.BOTTOM),
                new TabEvent.TabMoved(t1, Host.region("cell_2"), Integer.MAX_VALUE),
                new TabEvent.TabShown(Host.region("cell_2"), t1),
                new TabEvent.TabRenamed(t1, WidgetTitle.of(AWKWARD + " renamed")),
                new FloatEvent.FloatOpened(FloatId.of("float-1"), -40, 0, 320, 220),
                new TabEvent.TabMoved(t1, Host.floating("float-1"), 0),
                new TabEvent.TabShown(Host.floating("float-1"), t1),
                new FloatEvent.FloatMoved(FloatId.of("float-1"), Integer.MIN_VALUE, Integer.MAX_VALUE),
                new FloatEvent.FloatResized(FloatId.of("float-1"), 1, Integer.MAX_VALUE),
                new FloatEvent.FloatRaised(FloatId.of("float-1")),
                new TabEvent.TabMoved(t1, Host.region("main"), 0),
                new FloatEvent.FloatClosed(FloatId.of("float-1")),
                new RegionEvent.TracksChanged(SplitPath.of("0/12"), List.of(Scaled.of(333_333, 6), Scaled.of(333_333, 6), Scaled.of(333_334, 6))),
                new RegionEvent.TracksChanged(SplitPath.ROOT, List.of(Scaled.of(1, 1), Scaled.of(9, 1))),
                new RegionEvent.RegionRemoved(RegionId.of("cell_2"), Optional.of(main)),
                new RegionEvent.RegionRemoved(RegionId.of("cell_3"), Optional.empty()),
                new TabEvent.TabOpened(TabId.of("tab-2"), WidgetKind.of("counter"), WidgetTitle.of(""), Host.region("main"), 1),
                new TabEvent.TabClosed(t1));
        var out = new ArrayList<LoggedEvent>();
        for (int i = 0; i < events.size(); i++) {
            // the first at the epoch itself, the rest a millisecond apart
            out.add(new LoggedEvent(EventSeq.of(i + 1), i == 0 ? Instant.EPOCH : Instant.ofEpochMilli(1_790_000_000_000L + i), events.get(i)));
        }
        // the largest number either language holds exactly, as the seq and as the time
        out.add(new LoggedEvent(EventSeq.of(Json.MAX_SAFE), Instant.ofEpochMilli(Json.MAX_SAFE), new TabEvent.TabClosed(TabId.of("tab-2"))));
        return out;
    }

    private static LogHeader header() {
        return LogHeader.of(WorkspaceKind.of("focus-lab"), WorkspaceInstanceId.parse("0f1b6c2e-5000-9000-7f1b-6c2e00000001"));
    }

    /** JavaScript reads a file into its classes and writes it back. */
    private String throughJs(String text) {
        return js.eval("js", """
                (text) => {
                    const lines = text.split("\\n");
                    lines.pop();
                    const header = LogHeaderCodec.transformFrom(JSON.parse(lines[0]));
                    const events = lines.slice(1).map((l) => LoggedEventCodec.transformFrom(JSON.parse(l)));
                    return WorkspaceLogExport.text(header, events);
                }
                """).execute(text).asString();
    }

    @Test
    void aFileJavaWritesJavaScriptWritesBackTheSame() {
        String text = new WorkspaceLogFile(header(), corpus()).write();
        assertEquals(text, throughJs(text));
    }

    @Test
    void aFileJavaScriptWritesJavaWritesBackTheSame() {
        String text = js.eval("js", """
                (() => {
                    const header = new LogHeader(LogHeader.FORMAT, LogHeader.VERSION, new WorkspaceKind("demo"),
                                                 new WorkspaceInstanceId("7f1b6c2e-5000-9000-7f1b-6c2e00000001"));
                    const t = new TabId("tab-7"), r = new RegionId("main");
                    const ev = [
                        new TabOpened(t, new WidgetKind("field"), new WidgetTitle("q\\" \\\\ \\n\\u0001 \\u2028 \\ud83d\\ude00 \\udbff"), new InRegion(r), 0),
                        new RegionParted(r, new RegionId("cell-9"), Side.LEFT),
                        new TracksChanged(new SplitPath(""), [new Scaled(250000, 6), new Scaled(750000, 6)]),
                        new TabShown(new InRegion(r), t),
                        new TabRenamed(t, new WidgetTitle("Mine")),
                        new FloatOpened(new FloatId("float-3"), 12, 34, 320, 220),
                        new TabMoved(t, new InFloat(new FloatId("float-3")), 0),
                        new FloatRaised(new FloatId("float-3")),
                        new TabMoved(t, new InRegion(r), 0),
                        new FloatClosed(new FloatId("float-3")),
                        new TabClosed(t)
                    ];
                    return WorkspaceLogExport.text(header, ev.map((e, i) => new LoggedEvent(new EventSeq(i + 1), 1790000000000 + i, e)));
                })()
                """).asString();
        assertEquals(text, WorkspaceLogFile.read(text).write());
        assertEquals(ValidateWorkspaceLog.VALID, ValidateWorkspaceLog.validate("js", text, System.out, System.err));
    }

    @Test
    void theJavaObjectsAreTheOnesJavaWrote() {
        var file = new WorkspaceLogFile(header(), corpus());
        assertEquals(file, WorkspaceLogFile.read(throughJs(file.write())));
    }
}
