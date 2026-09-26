package hue.captains.singapura.js.homing.workspace.log.fold;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import hue.captains.singapura.js.homing.workspace.log.EventSeq;
import hue.captains.singapura.js.homing.workspace.log.FloatId;
import hue.captains.singapura.js.homing.workspace.log.FloatState;
import hue.captains.singapura.js.homing.workspace.log.Host;
import hue.captains.singapura.js.homing.workspace.log.Layout;
import hue.captains.singapura.js.homing.workspace.log.LogHeader;
import hue.captains.singapura.js.homing.workspace.log.LoggedEvent;
import hue.captains.singapura.js.homing.workspace.log.RegionId;
import hue.captains.singapura.js.homing.workspace.log.RegionState;
import hue.captains.singapura.js.homing.workspace.log.Scaled;
import hue.captains.singapura.js.homing.workspace.log.Side;
import hue.captains.singapura.js.homing.workspace.log.TabId;
import hue.captains.singapura.js.homing.workspace.log.TabState;
import hue.captains.singapura.js.homing.workspace.log.Track;
import hue.captains.singapura.js.homing.workspace.log.WorkspaceEvent;
import hue.captains.singapura.js.homing.workspace.log.WorkspaceEvent.*;
import hue.captains.singapura.js.homing.workspace.log.WorkspaceKind;
import hue.captains.singapura.js.homing.workspace.log.WorkspaceState;
import hue.captains.singapura.js.homing.workspace.log.codec.FoldedStateCodec;
import hue.captains.singapura.js.homing.workspace.log.json.JsonText;
import hue.captains.singapura.js.homing.workspace.log.store.ValidateWorkspaceLog;
import hue.captains.singapura.js.homing.workspace.log.store.WorkspaceLogFile;
import hue.captains.singapura.js.homing.workspace.log.SplitPath;
import hue.captains.singapura.js.homing.workspace.log.WidgetKind;
import hue.captains.singapura.js.homing.workspace.log.WidgetTitle;
import hue.captains.singapura.js.homing.workspace.log.WorkspaceInstanceId;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The two folds agree: random logs — every event possible where it falls, as a
 * workspace would make them, floats and merges and re-sharings among them —
 * folded by Java and by the browser's JavaScript, and the two states written
 * down are the same bytes. The validator, handed both, says so too.
 */
class WorkspaceFoldParityTest extends JsModuleTestBase {

    private static final String DIR = "/homing/js/hue/captains/singapura/js/homing/workspace/";
    private static final LogHeader HEADER = LogHeader.of(WorkspaceKind.of("demo"), WorkspaceInstanceId.parse("7f1b6c2e-5000-9000-7f1b-6c2e00000001"));

    private Value stateOf;

    @BeforeEach
    void load() {
        js = buildContext();
        loadModule(DIR + "codecs/WorkspaceLogCodecsModule.js");
        for (String m : new String[]{"ExactShare", "LayoutAlgebra", "WorkspaceFold"}) loadModule(DIR + "log/fold/" + m + "Module.js");
        loadModule(DIR + "log/store/WorkspaceLogExportModule.js");
        stateOf = js.eval("js", """
                (text) => {
                    const lines = text.split("\\n");
                    lines.pop();
                    const header = LogHeaderCodec.transformFrom(JSON.parse(lines[0]));
                    return WorkspaceLogExport.stateText(header, lines.slice(1).map((l) => LoggedEventCodec.transformFrom(JSON.parse(l))));
                }
                """);
    }

    /** A workspace at random: only what it could do where it is. */
    private static final class Walker {
        final Random rnd;
        WorkspaceState s = WorkspaceState.opening();
        final List<WorkspaceEvent> events = new ArrayList<>();
        int tabs, regions, floats;

        Walker(long seed) { rnd = new Random(seed); }

        void does(WorkspaceEvent e) { s = WorkspaceFold.apply(s, e); events.add(e); }

        <T> T any(List<T> xs) { return xs.get(rnd.nextInt(xs.size())); }

        List<Host> hosts() {
            var out = new ArrayList<Host>();
            for (RegionState r : s.regions()) out.add(new Host.InRegion(r.id()));
            for (FloatState f : s.floats()) out.add(new Host.InFloat(f.id()));
            return out;
        }

        List<TabId> in(Host h) {
            return switch (h) {
                case Host.InRegion r -> s.regions().stream().filter(x -> x.id().equals(r.id())).findFirst().orElseThrow().tabs();
                case Host.InFloat f -> s.floats().stream().filter(x -> x.id().equals(f.id())).findFirst().orElseThrow().tabs();
            };
        }

        /** Everything out of a host, as a merge or a closing float moves it: to a region that stays. */
        void empty(Host h, RegionId keep) {
            for (TabId t : List.copyOf(in(h))) does(new TabMoved(t, new Host.InRegion(keep), rnd.nextInt(in(new Host.InRegion(keep)).size() + 1)));
        }

        String title() { return any(List.of("Note", "Note 2", "Groceries", "q\" \\ \u0001 é 😀", "")); }

        void step() {
            int pick = rnd.nextInt(20);
            List<TabState> open = s.tabs();
            List<RegionId> cells = Layout.regions(s.layout());
            if (pick < 4 || open.isEmpty()) {
                Host h = any(hosts());
                does(new TabOpened(TabId.of("tab-" + (++tabs)), WidgetKind.of(any(List.of("note", "counter", "opener"))), WidgetTitle.of(title()), h, rnd.nextInt(in(h).size() + 1)));
            } else if (pick < 5) {
                does(new TabBecame(any(open).id(), WidgetKind.of("field"), WidgetTitle.of(title())));
            } else if (pick < 6) {
                does(new TabRenamed(any(open).id(), WidgetTitle.of(title())));
            } else if (pick < 9) {
                TabId t = any(open).id();
                Host to = any(hosts());
                int size = in(to).size() - (in(to).contains(t) ? 1 : 0);
                does(new TabMoved(t, to, rnd.nextInt(size + 1)));
            } else if (pick < 11) {
                var holding = hosts().stream().filter(h -> !in(h).isEmpty()).toList();
                Host h = any(holding);
                does(new TabShown(h, any(in(h))));
            } else if (pick < 12) {
                does(new TabClosed(any(open).id()));
            } else if (pick < 14) {
                does(new RegionParted(any(cells), RegionId.of("r" + (++regions)), Side.values()[rnd.nextInt(4)]));
            } else if (pick < 15 && cells.size() > 1) {
                RegionId gone = any(cells);
                RegionId keep = any(cells.stream().filter(c -> !c.equals(gone)).toList());
                empty(new Host.InRegion(gone), keep);
                does(new RegionRemoved(gone, rnd.nextBoolean() ? Optional.of(any(cells)) : Optional.empty()));
            } else if (pick < 16) {
                var at = new ArrayList<String>();
                splits(s.layout(), "", at);
                if (at.isEmpty()) return;
                String path = any(at);
                int n = sizeAt(s.layout(), path);
                long[] units = new long[n];
                long left = Layout.WHOLE;
                for (int i = 0; i < n - 1; i++) { units[i] = 1 + rnd.nextInt((int) (left / (n - i))); left -= units[i]; }
                units[n - 1] = left;
                var shares = new ArrayList<Scaled>();
                for (long u : units) shares.add(Scaled.of(u, Track.SCALE));
                does(new TracksChanged(SplitPath.of(path), shares));
            } else if (pick < 17) {
                does(new FloatOpened(FloatId.of("float-" + (++floats)), rnd.nextInt(800) - 100, rnd.nextInt(600), 1 + rnd.nextInt(600), 1 + rnd.nextInt(400)));
            } else if (!s.floats().isEmpty()) {
                FloatId f = any(s.floats()).id();
                switch (pick) {
                    case 17 -> does(new FloatMoved(f, rnd.nextInt(1000) - 200, rnd.nextInt(700) - 50));
                    case 18 -> does(rnd.nextBoolean() ? new FloatResized(f, 1 + rnd.nextInt(900), 1 + rnd.nextInt(700)) : new FloatRaised(f));
                    default -> { empty(new Host.InFloat(f), any(cells)); does(new FloatClosed(f)); }
                }
            }
        }

        static void splits(Layout l, String path, List<String> out) {
            if (l instanceof Layout.Split sp) {
                out.add(path);
                for (int i = 0; i < sp.tracks().size(); i++) splits(sp.tracks().get(i).node(), path.isEmpty() ? String.valueOf(i) : path + "/" + i, out);
            }
        }

        static int sizeAt(Layout l, String path) {
            if (!path.isEmpty()) for (String k : path.split("/")) l = ((Layout.Split) l).tracks().get(Integer.parseInt(k)).node();
            return ((Layout.Split) l).tracks().size();
        }
    }

    @Test
    void randomLogsFoldToTheSameStateInBothLanguages() {
        int total = 0;
        for (long seed = 1; seed <= 40; seed++) {
            var w = new Walker(seed);
            for (int i = 0; i < 70; i++) w.step();
            var logged = new ArrayList<LoggedEvent>();
            for (int i = 0; i < w.events.size(); i++) logged.add(new LoggedEvent(EventSeq.of(i + 1), Instant.ofEpochMilli(1_790_000_000_000L + i), w.events.get(i)));
            var file = new WorkspaceLogFile(HEADER, logged);
            String java = JsonText.write(FoldedStateCodec.INSTANCE.transformTo(WorkspaceFold.fold(file))) + "\n";
            String text = file.write();
            final long s = seed;
            assertEquals(java, stateOf.execute(text).asString(), () -> "seed " + s);
            assertEquals(ValidateWorkspaceLog.VALID, ValidateWorkspaceLog.validate("log", text, "state", java, System.out, System.err));
            total += w.events.size();
        }
        System.out.println("[WorkspaceFoldParityTest] " + total + " events folded alike");
    }
}
