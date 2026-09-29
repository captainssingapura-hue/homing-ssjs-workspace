package hue.captains.singapura.js.homing.workspace.groups.core.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * How a workspace is arranged under one placement engine: the widgets it opens,
 * and where the engine puts each - what a workspace of the kind is, the first
 * time, before anyone has changed it. An arrangement is the declarer's decision,
 * as filing is the group's: a workspace knows neither.
 *
 * <p>Checked whole: each widget named once, each of a kind the workspace offers;
 * each placed, once, and nothing placed that it does not open.</p>
 *
 * @param workspace what is arranged
 * @param widgets   the widgets it opens, as it lists them; the engine makes them in its placement's order
 * @param placement where they go, in the engine's vocabulary
 * @param <W> the workspace
 * @param <P> the engine's placement
 */
public record Arrangement<W extends WorkspaceSpec, P extends Placement>(W workspace, List<ArrangedWidget> widgets, P placement) {

    public Arrangement {
        Objects.requireNonNull(workspace, "Arrangement.workspace");
        Objects.requireNonNull(placement, "Arrangement.placement");
        widgets = List.copyOf(Objects.requireNonNull(widgets, "Arrangement.widgets"));
        Objects.requireNonNull(placement.engine(), "Arrangement.placement's engine");
        String of = "the " + placement.engine() + " arrangement of " + workspace.workspaceKind();
        var problems = new ArrayList<String>();
        var refs = new LinkedHashMap<WidgetRef, ArrangedWidget>();
        for (ArrangedWidget w : widgets) {
            if (refs.putIfAbsent(w.ref(), w) != null) problems.add("names two widgets " + w.ref());
            if (!workspace.offers(w.kind())) problems.add("opens " + w.ref() + " of the kind " + w.kind() + ", which the workspace does not offer");
        }
        var placed = new HashSet<WidgetRef>();
        for (WidgetRef r : placement.placed()) {
            if (!placed.add(r)) problems.add("places " + r + " twice");
            if (!refs.containsKey(r)) problems.add("places " + r + ", which it does not open");
        }
        for (WidgetRef r : refs.keySet()) if (!placed.contains(r)) problems.add("opens " + r + " and places it nowhere");
        if (!problems.isEmpty()) throw new IllegalArgumentException(of + ": " + String.join("; ", problems));
    }

    /** The widgets, then where they go. */
    public static <W extends WorkspaceSpec, P extends Placement> Arrangement<W, P> of(W workspace, P placement, ArrangedWidget... widgets) {
        return new Arrangement<>(workspace, List.of(widgets), placement);
    }

    /** The engine it is for: its placement's. */
    public PlacementEngine engine() { return placement.engine(); }

    /** The widget it names so. */
    public Optional<ArrangedWidget> widget(WidgetRef ref) {
        for (ArrangedWidget w : widgets) if (w.ref().equals(ref)) return Optional.of(w);
        return Optional.empty();
    }

    /** The widgets, by the names it gives them, as it lists them. */
    public Map<WidgetRef, ArrangedWidget> byRef() {
        var out = new LinkedHashMap<WidgetRef, ArrangedWidget>();
        for (ArrangedWidget w : widgets) out.put(w.ref(), w);
        return Collections.unmodifiableMap(out);
    }
}
