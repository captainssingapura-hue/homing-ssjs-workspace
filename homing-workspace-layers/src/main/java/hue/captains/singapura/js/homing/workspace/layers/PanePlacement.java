package hue.captains.singapura.js.homing.workspace.layers;

import hue.captains.singapura.js.homing.workspace.core.WorkspaceCore;
import hue.captains.singapura.js.homing.workspace.core.models.WidgetId;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * The one pane, headless (RFC 0066 E3, the workspace detour): a placement -
 * it mounts and unmounts widgets' panes, and never creates or closes one. The
 * widgets mounted, in the order mounted, and the one it shows, or none. What a
 * page's pane does with them - a slot in view - is the page's; what it SHOWS
 * is this, the same on any page. Mounted at {@link Location#SHOWN}, a widget
 * is shown; at {@link Location#BEHIND}, it joins the pane, the one shown kept.
 * Unmounted while shown, the pane shows the next - the one mounted after it,
 * else the one before, else none - and says so: the core executing a close
 * unmounts first, so the pane's word comes before the widget is gone.
 *
 * <p>The core's placement as it is ({@link WorkspaceCore.Placement}). The
 * JavaScript is this, step for step (PanePlacementModule.js), and the two
 * agree (LayersParityTest).</p>
 */
public final class PanePlacement implements WorkspaceCore.Placement<PanePlacement.Location> {

    /** Where in the pane a widget is mounted: shown, or behind the one shown. */
    public enum Location { SHOWN, BEHIND }

    /** What the pane says: that it came to show a widget, or none. */
    public sealed interface Notice {
        record PaneShown(Optional<WidgetId> widget) implements Notice {
            public PaneShown { Objects.requireNonNull(widget, "PaneShown.widget"); }
        }
    }

    private final List<WidgetId> mounted = new ArrayList<>();
    private WidgetId shown;
    private final List<Consumer<Notice>> listeners = new CopyOnWriteArrayList<>();

    /** A widget's pane mounted: shown, or behind the one shown. */
    @Override
    public void mount(WorkspaceCore.Entry<?> entry, Location at) {
        WidgetId id = entry.id();
        if (mounted.contains(id)) throw new IllegalArgumentException("[PanePlacement] '" + id + "' is mounted already");
        mounted.add(id);
        if (Objects.requireNonNull(at, "at") == Location.SHOWN) set(id);
    }

    /** A widget's pane unmounted: shown no more - the pane shows the next, when it showed this one. */
    @Override
    public void unmount(WorkspaceCore.Entry<?> entry) {
        WidgetId id = entry.id();
        int i = mounted.indexOf(id);
        if (i < 0) throw new IllegalArgumentException("[PanePlacement] '" + id + "' is not mounted");
        mounted.remove(i);
        if (id.equals(shown)) set(i < mounted.size() ? mounted.get(i) : i > 0 ? mounted.get(i - 1) : null);
    }

    /** A widget mounted shown - or none: the pane's own request. */
    public void show(Optional<WidgetId> id) {
        id.ifPresent(w -> { if (!mounted.contains(w)) throw new IllegalArgumentException("[PanePlacement] '" + w + "' is not mounted"); });
        set(id.orElse(null));
    }

    public Optional<WidgetId> shown() { return Optional.ofNullable(shown); }

    /** The widgets mounted, in the order mounted. */
    public List<WidgetId> mounted() { return List.copyOf(mounted); }

    /** A listener, told every notice; the runnable returned takes it off. */
    public Runnable on(Consumer<Notice> listener) {
        listeners.add(Objects.requireNonNull(listener));
        return () -> listeners.remove(listener);
    }

    private void set(WidgetId id) {
        if (Objects.equals(id, shown)) return;
        shown = id;
        var n = new Notice.PaneShown(Optional.ofNullable(id));
        for (var l : listeners) l.accept(n);
    }
}
