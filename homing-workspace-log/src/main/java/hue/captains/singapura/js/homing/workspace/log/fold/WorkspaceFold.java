package hue.captains.singapura.js.homing.workspace.log.fold;

import hue.captains.singapura.js.homing.workspace.log.EventSeq;
import hue.captains.singapura.js.homing.workspace.log.FloatId;
import hue.captains.singapura.js.homing.workspace.log.FloatState;
import hue.captains.singapura.js.homing.workspace.log.FoldedState;
import hue.captains.singapura.js.homing.workspace.log.Host;
import hue.captains.singapura.js.homing.workspace.log.Layout;
import hue.captains.singapura.js.homing.workspace.log.LoggedEvent;
import hue.captains.singapura.js.homing.workspace.log.RegionId;
import hue.captains.singapura.js.homing.workspace.log.RegionState;
import hue.captains.singapura.js.homing.workspace.log.TabId;
import hue.captains.singapura.js.homing.workspace.log.TabState;
import hue.captains.singapura.js.homing.workspace.log.WorkspaceEvent;
import hue.captains.singapura.js.homing.workspace.log.WorkspaceState;
import hue.captains.singapura.js.homing.workspace.log.store.WorkspaceLogFile;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A workspace log's meaning: its events folded, from the workspace's opening,
 * into the {@link WorkspaceState} they leave. Each event must be possible where
 * it falls — a tab opened is not already open, a tab moved or shown is where
 * the log last put it, a region removed or a float closed holds nothing — and
 * one that is not is refused, naming it. The JavaScript fold is this, line for
 * line, and the two agree to the byte.
 *
 * <p>How the hosts follow the events: a tab leaving a host takes the host's
 * showing with it when it was the one shown — a reorder within the host keeps
 * it — and whatever the host shows next, the log says (TabShown). A region's
 * room goes as the grid's did ({@link LayoutAlgebra}); a float raised goes to
 * the top of the stack.</p>
 */
public final class WorkspaceFold {

    private WorkspaceFold() {}

    /** An event the state it falls on cannot take. */
    public static final class Refused extends IllegalArgumentException {
        public Refused(String why) { super(why); }
    }

    /** A file's events, from the opening: the state, through its last event — or refused at the line that cannot be. */
    public static FoldedState fold(WorkspaceLogFile file) {
        WorkspaceState s = WorkspaceState.opening();
        long through = 0;
        for (int i = 0; i < file.events().size(); i++) {
            LoggedEvent e = file.events().get(i);
            try { s = apply(s, e.event()); }
            catch (IllegalArgumentException x) {
                throw new WorkspaceLogFile.Refused(i + 2, e.event().getClass().getSimpleName() + " cannot be: " + x.getMessage());
            }
            through = e.seq().value();
        }
        return new FoldedState(file.header(), EventSeq.of(through), s);
    }

    /** One event on a state: the state after it. */
    public static WorkspaceState apply(WorkspaceState state, WorkspaceEvent event) {
        var w = new Working(state);
        switch (event) {
            case WorkspaceEvent.TabOpened e -> {
                if (w.tabs.containsKey(e.id())) throw new Refused("the tab " + e.id() + " is already open");
                w.insert(e.host(), e.id(), e.index());
                w.tabs.put(e.id(), new TabState(e.id(), e.kind(), e.title()));
            }
            case WorkspaceEvent.TabBecame e -> w.tabs.put(e.id(), new TabState(w.open(e.id()).id(), e.kind(), e.title()));
            case WorkspaceEvent.TabRenamed e -> {
                TabState t = w.open(e.id());
                w.tabs.put(e.id(), new TabState(t.id(), t.kind(), e.title()));
            }
            case WorkspaceEvent.TabMoved e -> {
                w.open(e.id());
                Host from = w.hostOf(e.id());
                boolean kept = from.equals(e.host()) && w.hosted(from).shown.map(e.id()::equals).orElse(false);
                w.takeOut(e.id());
                w.insert(e.host(), e.id(), e.index());
                if (kept) w.hosted(from).shown = Optional.of(e.id());
            }
            case WorkspaceEvent.TabShown e -> {
                Hosted h = w.hosted(e.host());
                if (!h.tabs.contains(e.id())) throw new Refused("the " + name(e.host()) + " does not hold " + e.id());
                h.shown = Optional.of(e.id());
            }
            case WorkspaceEvent.TabClosed e -> {
                w.open(e.id());
                w.takeOut(e.id());
                w.tabs.remove(e.id());
            }
            case WorkspaceEvent.RegionParted e -> {
                w.layout = LayoutAlgebra.subdivide(w.layout, e.region(), e.side(), e.newRegion());
                w.regions.put(e.newRegion(), new Hosted());
            }
            case WorkspaceEvent.RegionRemoved e -> {
                Hosted h = w.hosted(new Host.InRegion(e.region()));
                if (!h.tabs.isEmpty()) throw new Refused("the region " + e.region() + " still holds " + h.tabs);
                w.layout = LayoutAlgebra.remove(w.layout, e.region(), e.toward());
                w.regions.remove(e.region());
            }
            case WorkspaceEvent.TracksChanged e -> w.layout = LayoutAlgebra.tracks(w.layout, e.path().value(), e.shares());
            case WorkspaceEvent.FloatOpened e -> {
                if (w.floats.containsKey(e.id())) throw new Refused("the float " + e.id() + " is already open");
                var f = new Hosted();
                f.x = e.x(); f.y = e.y(); f.w = e.w(); f.h = e.h();
                w.floats.put(e.id(), f);
            }
            case WorkspaceEvent.FloatMoved e -> { Hosted f = w.floatOf(e.id()); f.x = e.x(); f.y = e.y(); }
            case WorkspaceEvent.FloatResized e -> { Hosted f = w.floatOf(e.id()); f.w = e.w(); f.h = e.h(); }
            case WorkspaceEvent.FloatRaised e -> w.floats.put(e.id(), w.floats.remove(w.floatId(e.id())));
            case WorkspaceEvent.FloatClosed e -> {
                Hosted f = w.floatOf(e.id());
                if (!f.tabs.isEmpty()) throw new Refused("the float " + e.id() + " still holds " + f.tabs);
                w.floats.remove(e.id());
            }
        }
        return w.state();
    }

    private static String name(Host h) {
        return switch (h) {
            case Host.InRegion r -> "region " + r.id();
            case Host.InFloat f -> "float " + f.id();
        };
    }

    /** A host while the fold works on it: its tabs, the one it shows, and a float's frame. */
    private static final class Hosted {
        final List<TabId> tabs = new ArrayList<>();
        Optional<TabId> shown = Optional.empty();
        int x, y, w, h;
    }

    private static final class Working {
        Layout layout;
        final Map<RegionId, Hosted> regions = new LinkedHashMap<>();
        final Map<FloatId, Hosted> floats = new LinkedHashMap<>();   // bottom first
        final Map<TabId, TabState> tabs = new LinkedHashMap<>();     // by id; the state lists them in the order the hosts hold them

        Working(WorkspaceState s) {
            layout = s.layout();
            for (RegionState r : s.regions()) { var h = new Hosted(); h.tabs.addAll(r.tabs()); h.shown = r.shown(); regions.put(r.id(), h); }
            for (FloatState f : s.floats()) {
                var h = new Hosted(); h.tabs.addAll(f.tabs()); h.shown = f.shown();
                h.x = f.x(); h.y = f.y(); h.w = f.w(); h.h = f.h();
                floats.put(f.id(), h);
            }
            for (TabState t : s.tabs()) tabs.put(t.id(), t);
        }

        TabState open(TabId id) {
            TabState t = tabs.get(id);
            if (t == null) throw new Refused("the tab " + id + " is not open");
            return t;
        }

        Hosted hosted(Host host) {
            Hosted h = switch (host) {
                case Host.InRegion r -> regions.get(r.id());
                case Host.InFloat f -> floats.get(f.id());
            };
            if (h == null) throw new Refused("there is no " + name(host));
            return h;
        }

        FloatId floatId(FloatId id) { floatOf(id); return id; }

        Hosted floatOf(FloatId id) { return hosted(new Host.InFloat(id)); }

        Host hostOf(TabId id) {
            for (var r : regions.entrySet()) if (r.getValue().tabs.contains(id)) return new Host.InRegion(r.getKey());
            for (var f : floats.entrySet()) if (f.getValue().tabs.contains(id)) return new Host.InFloat(f.getKey());
            throw new Refused("the tab " + id + " is in no host");
        }

        void insert(Host host, TabId id, int index) {
            Hosted h = hosted(host);
            if (index > h.tabs.size()) throw new Refused("the " + name(host) + " holds " + h.tabs.size() + " tabs: no index " + index);
            h.tabs.add(index, id);
        }

        void takeOut(TabId id) {
            Hosted h = hosted(hostOf(id));
            h.tabs.remove(id);
            if (h.shown.map(id::equals).orElse(false)) h.shown = Optional.empty();
        }

        WorkspaceState state() {
            var rs = new ArrayList<RegionState>();
            for (RegionId r : Layout.regions(layout)) {
                Hosted h = regions.get(r);
                if (h == null) throw new Refused("the layout has a region " + r + " the fold does not");
                rs.add(new RegionState(r, h.tabs, h.shown));
            }
            if (rs.size() != regions.size()) throw new Refused("the fold has regions the layout does not");
            var fs = new ArrayList<FloatState>();
            for (var f : floats.entrySet()) {
                Hosted h = f.getValue();
                fs.add(new FloatState(f.getKey(), h.x, h.y, h.w, h.h, h.tabs, h.shown));
            }
            var listed = new ArrayList<TabState>();
            for (RegionState r : rs) for (TabId t : r.tabs()) listed.add(tabs.get(t));
            for (FloatState f : fs) for (TabId t : f.tabs()) listed.add(tabs.get(t));
            return new WorkspaceState(layout, rs, fs, listed);
        }
    }
}
