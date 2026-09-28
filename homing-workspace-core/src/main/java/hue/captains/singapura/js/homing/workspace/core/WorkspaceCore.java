package hue.captains.singapura.js.homing.workspace.core;

import hue.captains.singapura.js.homing.workspace.core.models.WidgetId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * The workspace's headless core (RFC 0066 E3, the workspace detour): the
 * ROSTER - the widgets the workspace holds, each under an id of its own that
 * says what it is ({@link WidgetIds}) - and their LIFE. A widget is opened -
 * a container lent by the placement, the widget made in it, and said opened -
 * and closed - said closing, disposed, its container handed back, said
 * closed. Whether it is shown, and where, is the placement's: a widget is
 * whether it is mounted anywhere or not. The core knows the placement only as
 * a port, and nothing of what it does.
 *
 * <p>The JavaScript is this, step for step (WorkspaceCoreModule.js); the two
 * agree on the ids and on what they say (WorkspaceCoreParityTest). What else a
 * widget's opening means - its DomOps root grafted, its messaging parties
 * joined - is a listener's, never the core's.</p>
 *
 * @param <C> a container, as the placement lends it
 * @param <W> a widget
 */
public final class WorkspaceCore<C, W> {

    /** A kind of widget: how one is made in a container, with its params, and disposed. */
    public interface Kind<C, W> {
        W make(C container, Map<String, String> params);
        void dispose(W widget);
    }

    /** What lends a widget its container, and takes it back: the placement, as the core knows it. */
    public interface Placement<C> {
        C lend(Entry<?> entry);
        void release(Entry<?> entry);
    }

    /** A widget the workspace holds: its id, its kind, its params, and the widget - null while it is being made. */
    public record Entry<W>(WidgetId id, String kind, Map<String, String> params, W widget) {
        public Entry { params = Map.copyOf(params); }
    }

    /** What the core says, in the order it happens. */
    public sealed interface Notice {
        /** A widget was made, and is in the roster. */
        record WidgetOpened(Entry<?> entry) implements Notice {}
        /** A widget is about to be disposed: still whole, still in the roster. */
        record WidgetClosing(Entry<?> entry) implements Notice {}
        /** A widget is gone: disposed, its container handed back, out of the roster. */
        record WidgetClosed(WidgetId id, String kind) implements Notice {}
    }

    private final Map<String, Kind<C, W>> kinds;
    private final Placement<C> placement;
    private final Map<WidgetId, Entry<W>> roster = new LinkedHashMap<>();
    private final Map<String, Integer> sequences = new HashMap<>();
    private final List<Consumer<Notice>> listeners = new CopyOnWriteArrayList<>();

    public WorkspaceCore(Map<String, Kind<C, W>> kinds, Placement<C> placement) {
        this.kinds = Map.copyOf(Objects.requireNonNull(kinds, "WorkspaceCore.kinds"));
        this.placement = Objects.requireNonNull(placement, "WorkspaceCore.placement");
    }

    /** A widget of a kind opened, under the next id of its prefix. */
    public Entry<W> open(String kind, Map<String, String> params) { return open(kind, params, null); }

    /**
     * A widget of a kind opened - under the id given, when one is (a widget coming
     * back), else the next of its prefix. An id given must be one its kind and
     * params would make, and not held already; the sequence goes on past it.
     */
    public Entry<W> open(String kind, Map<String, String> params, WidgetId id) {
        var k = kinds.get(kind);
        if (k == null) throw new IllegalArgumentException("[WorkspaceCore] no kind '" + kind + "': " + String.join(", ", new java.util.TreeSet<>(kinds.keySet())));
        var p = params == null ? Map.<String, String>of() : params;
        String prefix = WidgetIds.prefix(kind, p);
        WidgetId wid = id == null ? WidgetIds.of(prefix, sequences.getOrDefault(prefix, 0) + 1) : given(id, prefix);
        sequences.merge(prefix, wid.sequence(), Math::max);
        var lent = new Entry<W>(wid, kind, p, null);
        C container = placement.lend(lent);
        W widget;
        try { widget = k.make(container, p); }
        catch (RuntimeException e) { placement.release(lent); throw e; }   // the id is spent: never reused
        var entry = new Entry<>(wid, kind, p, widget);
        roster.put(wid, entry);
        say(new Notice.WidgetOpened(entry));
        return entry;
    }

    /**
     * The ids of a prefix spent up to the sequence given, whether the widgets are
     * held or not - a workspace coming back, which gave them before: the next of
     * the prefix is past it. Never back.
     */
    public void spend(String prefix, int last) {
        if (last < 1) throw new IllegalArgumentException("[WorkspaceCore] a sequence starts at 1: " + last);
        sequences.merge(Objects.requireNonNull(prefix, "prefix"), last, Math::max);
    }

    private WidgetId given(WidgetId id, String prefix) {
        if (roster.containsKey(id)) throw new IllegalArgumentException("[WorkspaceCore] '" + id + "' is held already");
        if (!id.prefix().equals(prefix)) throw new IllegalArgumentException("[WorkspaceCore] '" + id + "' is not what its kind and params make: " + prefix + "-n");
        return id;
    }

    /** A widget closed: said closing, disposed, its container handed back, said closed. */
    public void close(WidgetId id) {
        var entry = roster.get(id);
        if (entry == null) throw new IllegalArgumentException("[WorkspaceCore] no widget '" + id + "'");
        say(new Notice.WidgetClosing(entry));
        try { kinds.get(entry.kind()).dispose(entry.widget()); }
        finally {
            roster.remove(id);
            placement.release(entry);
            say(new Notice.WidgetClosed(id, entry.kind()));
        }
    }

    public Optional<Entry<W>> entry(WidgetId id) { return Optional.ofNullable(roster.get(id)); }

    /** The roster, in the order its widgets were opened. */
    public List<Entry<W>> entries() { return List.copyOf(new ArrayList<>(roster.values())); }

    /** A listener, told every notice; the runnable returned takes it off. */
    public Runnable on(Consumer<Notice> listener) {
        listeners.add(Objects.requireNonNull(listener));
        return () -> listeners.remove(listener);
    }

    private void say(Notice n) { for (var l : listeners) l.accept(n); }
}
