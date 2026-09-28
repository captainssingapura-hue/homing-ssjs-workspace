package hue.captains.singapura.js.homing.workspace.layers;

import hue.captains.singapura.js.homing.workspace.log.LogIds.WidgetId;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * The one pane, headless (RFC 0066 E3, the workspace detour): the widgets lent
 * to it, in the order lent, and the one it shows, or none. What a page's pane
 * does with them - a slot for each, one in view - is the page's; what it SHOWS
 * is this, the same on any page. When the widget shown is taken back, the
 * pane shows the next - the one lent after it, else the one before, else none
 * - and says so before the widget is gone from the roster, so its log says
 * what it shows next before the widget closes.
 *
 * <p>The JavaScript is this, step for step (PanePlacementModule.js), and the
 * two agree (LayersParityTest).</p>
 */
public final class PanePlacement {

    /** What the pane says: that it came to show a widget, or none. */
    public sealed interface Notice {
        record PaneShown(Optional<WidgetId> widget) implements Notice {
            public PaneShown { Objects.requireNonNull(widget, "PaneShown.widget"); }
        }
    }

    private final List<WidgetId> lent = new ArrayList<>();
    private WidgetId shown;
    private final List<Consumer<Notice>> listeners = new CopyOnWriteArrayList<>();

    /** A widget lent a place in the pane: not shown. */
    public void lend(WidgetId id) {
        if (lent.contains(Objects.requireNonNull(id, "id"))) throw new IllegalArgumentException("[PanePlacement] '" + id + "' is lent already");
        lent.add(id);
    }

    /** A widget taken back: shown no more - the pane shows the next, when it showed this one. */
    public void release(WidgetId id) {
        int i = lent.indexOf(id);
        if (i < 0) throw new IllegalArgumentException("[PanePlacement] '" + id + "' is not lent");
        lent.remove(i);
        if (id.equals(shown)) set(i < lent.size() ? lent.get(i) : i > 0 ? lent.get(i - 1) : null);
    }

    /** A widget lent shown - or none. */
    public void show(Optional<WidgetId> id) {
        id.ifPresent(w -> { if (!lent.contains(w)) throw new IllegalArgumentException("[PanePlacement] '" + w + "' is not lent"); });
        set(id.orElse(null));
    }

    public Optional<WidgetId> shown() { return Optional.ofNullable(shown); }

    /** The widgets lent, in the order lent. */
    public List<WidgetId> lent() { return List.copyOf(lent); }

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
