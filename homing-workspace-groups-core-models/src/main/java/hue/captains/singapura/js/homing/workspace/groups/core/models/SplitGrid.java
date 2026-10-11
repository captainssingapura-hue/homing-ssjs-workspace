package hue.captains.singapura.js.homing.workspace.groups.core.models;

import hue.captains.singapura.js.homing.layout.Axis;
import hue.captains.singapura.js.homing.layout.CellId;
import hue.captains.singapura.js.homing.layout.Layout;
import hue.captains.singapura.js.homing.layout.LayoutAlgebra;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Where a workspace's widgets go in the split grid: its layout - the regions, split in rows and
 * columns, a value of the layout algebra (RFC 0068) - and beside it, each region's dock - its tabs
 * in the order its strip shows them and the one it shows - and each split's whole weights, how it
 * shares its room. What floats is left out: a float is transient, never a workspace's first state.
 *
 * <p>Declared in the grid's own vocabulary, which reads as the layout it makes - a frame of
 * regions and splits, whole weights, names - and is translated so: each region a cell of the
 * layout, named as the region; each region named once; each widget in one region, once.</p>
 *
 * <pre>{@code
 * SplitGrid.of(SplitGrid.row(
 *         Part.of(SplitGrid.region("kinds", "switcher"), 1),
 *         Part.of(SplitGrid.column(SplitGrid.region("books", "grid"), SplitGrid.region("chosen", "jumbotron")), 2)))
 * }</pre>
 *
 * @param layout  the regions and how they split the room: structure alone
 * @param regions each region's dock, in the layout's order of its cells
 * @param weights each split of the layout, and its parts' whole weights in order: how it shares its room
 */
public record SplitGrid(Layout layout, List<Region> regions, Map<Layout, List<Integer>> weights) implements Placement {
    /** The split grid. */
    public static final PlacementEngine ENGINE = PlacementEngine.of("split-grid");

    /** What a declaration writes for a part of the room: a region, or a split of it. */
    public sealed interface Frame permits Region, Split {}

    /** A region's name in the frame: letters, digits, hyphen and underscore. */
    public record RegionName(String value) {
        private static final Pattern GRAMMAR = Pattern.compile("[A-Za-z0-9_-]+");
        public RegionName {
            Objects.requireNonNull(value, "RegionName.value");
            if (!GRAMMAR.matcher(value).matches()) throw new IllegalArgumentException("RegionName.value '" + value + "' - letters, digits, hyphen, underscore");
        }
        public static RegionName of(String value) { return new RegionName(value); }
        /** The layout's cell the region is. */
        public CellId cell() { return new CellId(value); }
        @Override public String toString() { return value; }
    }

    /**
     * A region: a dock - its tabs in the order its strip shows them, and the one
     * it shows, the first unless said. With no tabs, a dock waiting for one.
     */
    public record Region(RegionName name, List<WidgetRef> tabs, Optional<WidgetRef> shown) implements Frame {
        public Region {
            Objects.requireNonNull(name, "Region.name");
            tabs = List.copyOf(Objects.requireNonNull(tabs, "Region.tabs"));
            Objects.requireNonNull(shown, "Region.shown");
            if (new HashSet<>(tabs).size() != tabs.size()) throw new IllegalArgumentException("the region " + name + " holds a widget twice: " + tabs);
            if (shown.isPresent() && !tabs.contains(shown.get())) {
                throw new IllegalArgumentException("the region " + name + " shows " + shown.get() + ", which it does not hold");
            }
        }
        /** The one it shows: the one said, else its first; none when it holds none. */
        public Optional<WidgetRef> showing() { return shown.isPresent() ? shown : tabs.stream().findFirst(); }
        /** The same region, showing that one of its tabs. */
        public Region showing(String tab) { return new Region(name, tabs, Optional.of(WidgetRef.of(tab))); }
    }

    /**
     * Two or more parts along an axis - HORIZONTAL in a row, VERTICAL in a stack. A part is a
     * region, or a split along the other axis: a row in a row is one row, said once, with its
     * parts' weights.
     */
    public record Split(Axis axis, List<Part> parts) implements Frame {
        public Split {
            Objects.requireNonNull(axis, "Split.axis");
            parts = List.copyOf(Objects.requireNonNull(parts, "Split.parts"));
            if (parts.size() < 2) throw new IllegalArgumentException("a split of " + parts.size() + " - two parts or more");
            for (Part p : parts) {
                if (p.frame() instanceof Split s && s.axis() == axis) {
                    throw new IllegalArgumentException("a " + axis + " split in a " + axis + " split - one split: give its parts to the outer one");
                }
            }
        }
        /** The weights, all together: what each part's weight is a share of. */
        public int weight() { return parts.stream().mapToInt(Part::weight).sum(); }
    }

    /** One part of a split, and its weight of the room: a whole number, one or more. */
    public record Part(Frame frame, int weight) {
        public Part {
            Objects.requireNonNull(frame, "Part.frame");
            if (weight < 1) throw new IllegalArgumentException("a part's weight " + weight + " - one or more");
        }
        public static Part of(Frame frame, int weight) { return new Part(frame, weight); }
        public static Part of(Frame frame) { return new Part(frame, 1); }
    }

    public SplitGrid {
        Objects.requireNonNull(layout, "SplitGrid.layout");
        regions = List.copyOf(Objects.requireNonNull(regions, "SplitGrid.regions"));
        weights = Map.copyOf(Objects.requireNonNull(weights, "SplitGrid.weights"));
        var cells = regions.stream().map(r -> r.name().cell()).toList();
        if (!cells.equals(layout.cells())) {
            throw new IllegalArgumentException("the split grid's regions " + cells + " are not its layout's cells " + layout.cells() + ", in order");
        }
        var held = new HashSet<WidgetRef>();
        for (Region r : regions) {
            for (WidgetRef t : r.tabs()) {
                if (!held.add(t)) throw new IllegalArgumentException("the split grid holds " + t + " in two regions");
            }
        }
        var splits = splitsOf(layout);
        if (!weights.keySet().equals(new HashSet<>(splits))) {
            throw new IllegalArgumentException("the split grid weighs " + weights.keySet().stream().map(Layout::text).toList()
                    + ", not its layout's splits " + splits.stream().map(Layout::text).toList());
        }
        for (Layout s : splits) {
            var w = weights.get(s);
            if (w.size() != partsOf(s).size() || w.stream().anyMatch(x -> x < 1)) {
                throw new IllegalArgumentException("the split " + s.text() + " is weighed " + w + " - a whole weight, one or more, for each of its parts");
            }
        }
    }

    /** The split grid a frame declares: its layout, its regions, its weights. */
    public static SplitGrid of(Frame frame) {
        var regions = new ArrayList<Region>();
        var weights = new LinkedHashMap<Layout, List<Integer>>();
        var names = new HashSet<RegionName>();
        Layout layout = declared(Objects.requireNonNull(frame, "frame"), regions, weights, names);
        return new SplitGrid(layout, regions, weights);
    }

    private static Layout declared(Frame frame, List<Region> regions, Map<Layout, List<Integer>> weights, Set<RegionName> names) {
        return switch (frame) {
            case Region r -> {
                if (!names.add(r.name())) throw new IllegalArgumentException("the split grid names two regions " + r.name());
                regions.add(r);
                yield new Layout.Whole(r.name().cell());
            }
            case Split s -> {
                var parts = s.parts().stream().map(p -> declared(p.frame(), regions, weights, names)).toArray(Layout[]::new);
                Layout l = LayoutAlgebra.INSTANCE.split(s.axis(), parts);
                weights.put(l, s.parts().stream().map(Part::weight).toList());
                yield l;
            }
        };
    }

    /** A region of that name, holding those tabs, showing the first. */
    public static Region region(String name, String... tabs) {
        var refs = new ArrayList<WidgetRef>();
        for (String t : tabs) refs.add(WidgetRef.of(t));
        return new Region(RegionName.of(name), refs, Optional.empty());
    }
    /** Parts side by side. */
    public static Split row(Part... parts) { return new Split(Axis.HORIZONTAL, List.of(parts)); }
    /** Frames side by side, each an equal part. */
    public static Split row(Frame... frames) { return new Split(Axis.HORIZONTAL, equal(frames)); }
    /** Parts one above the other. */
    public static Split column(Part... parts) { return new Split(Axis.VERTICAL, List.of(parts)); }
    /** Frames one above the other, each an equal part. */
    public static Split column(Frame... frames) { return new Split(Axis.VERTICAL, equal(frames)); }
    private static List<Part> equal(Frame... frames) {
        var out = new ArrayList<Part>();
        for (Frame f : frames) out.add(Part.of(f));
        return out;
    }

    @Override public PlacementEngine engine() { return ENGINE; }

    /** The widgets, region by region in the layout's order, each region's tabs in its strip's. */
    @Override
    public List<WidgetRef> placed() {
        var out = new ArrayList<WidgetRef>();
        for (Region r : regions) out.addAll(r.tabs());
        return List.copyOf(out);
    }

    /** The region holding that widget, if one does. */
    public Optional<Region> regionOf(WidgetRef widget) {
        for (Region r : regions) if (r.tabs().contains(widget)) return Optional.of(r);
        return Optional.empty();
    }

    /** The layout's splits, the outermost first, then each part's, depth first. */
    public List<Layout> splits() { return splitsOf(layout); }

    private static List<Layout> splitsOf(Layout layout) {
        var out = new ArrayList<Layout>();
        collect(layout, out);
        return List.copyOf(out);
    }

    private static void collect(Layout l, List<Layout> out) {
        if (l instanceof Layout.Whole) return;
        out.add(l);
        for (Layout p : partsOf(l)) collect(p, out);
    }

    private static List<? extends Layout> partsOf(Layout l) {
        return switch (l) {
            case Layout.Whole w -> List.of();
            case Layout.Horizontal_NSplit h -> h.parts();
            case Layout.Vertical_NSplit v -> v.parts();
        };
    }
}
