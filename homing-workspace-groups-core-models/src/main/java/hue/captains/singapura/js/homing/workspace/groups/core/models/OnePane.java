package hue.captains.singapura.js.homing.workspace.groups.core.models;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/**
 * Where a workspace's widgets go in one pane: the pane shows one of them, and
 * holds the rest, made and waiting, in the order given.
 *
 * @param held  the widgets it holds, the shown one among them: each once, at least one
 * @param shown the one it shows
 */
public record OnePane(List<WidgetRef> held, WidgetRef shown) implements Placement {

    /** One pane. */
    public static final PlacementEngine ENGINE = PlacementEngine.of("one-pane");

    public OnePane {
        held = List.copyOf(Objects.requireNonNull(held, "OnePane.held"));
        Objects.requireNonNull(shown, "OnePane.shown");
        if (new HashSet<>(held).size() != held.size()) throw new IllegalArgumentException("the pane holds a widget twice: " + held);
        if (!held.contains(shown)) throw new IllegalArgumentException("the pane shows " + shown + ", which it does not hold");
    }

    /** The pane showing that widget, and holding the others after it. */
    public static OnePane showing(String shown, String... others) {
        var held = new ArrayList<WidgetRef>();
        held.add(WidgetRef.of(shown));
        for (String o : others) held.add(WidgetRef.of(o));
        return new OnePane(held, WidgetRef.of(shown));
    }

    @Override public PlacementEngine engine() { return ENGINE; }

    @Override public List<WidgetRef> placed() { return held; }
}
