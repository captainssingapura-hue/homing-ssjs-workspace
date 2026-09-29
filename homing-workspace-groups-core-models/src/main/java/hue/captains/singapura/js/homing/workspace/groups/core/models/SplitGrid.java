package hue.captains.singapura.js.homing.workspace.groups.core.models;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Where a workspace's widgets go in the split grid: its frame - regions, split
 * in rows and columns, each part its weight of the room - and in each region,
 * its dock's tabs in the order its strip shows them and the one it shows. What
 * floats is left out: a float is transient, never a workspace's first state.
 *
 * <p>The grid's own vocabulary, as a declarer writes it: whole weights, not
 * the shares the grid keeps; regions named, not the ids the grid gives. Each
 * region named once; each widget in one region, once.</p>
 *
 * <pre>{@code
 * SplitGrid.of(SplitGrid.row(
 *         Part.of(SplitGrid.region("kinds", "switcher"), 1),
 *         Part.of(SplitGrid.column(SplitGrid.region("books", "grid"), SplitGrid.region("chosen", "jumbotron")), 2)))
 * }</pre>
 *
 * @param frame the regions, and how they split the room
 */
public record SplitGrid(Frame frame) implements Placement {

    /** The split grid. */
    public static final PlacementEngine ENGINE = PlacementEngine.of("split-grid");

    /** What fills a part of the room: a region, or a split of it. */
    public sealed interface Frame permits Region, Split {}

    /** A region's name in the frame: letters, digits, hyphen and underscore. */
    public record RegionName(String value) {
        private static final Pattern GRAMMAR = Pattern.compile("[A-Za-z0-9_-]+");

        public RegionName {
            Objects.requireNonNull(value, "RegionName.value");
            if (!GRAMMAR.matcher(value).matches()) throw new IllegalArgumentException("RegionName.value '" + value + "' - letters, digits, hyphen, underscore");
        }

        public static RegionName of(String value) { return new RegionName(value); }

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

    /** Which way a split lays its parts: HORIZONTAL in a row, VERTICAL in a stack - as the grid's layout says it. */
    public enum Axis { HORIZONTAL, VERTICAL }

    /**
     * Two or more parts along an axis. A part is a region, or a split along the
     * other axis: a row in a row is one row, and the grid lays it so - said once,
     * with its parts' weights.
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
        Objects.requireNonNull(frame, "SplitGrid.frame");
        var names = new HashSet<RegionName>();
        var held = new HashSet<WidgetRef>();
        for (Region r : regionsOf(frame)) {
            if (!names.add(r.name())) throw new IllegalArgumentException("the split grid names two regions " + r.name());
            for (WidgetRef t : r.tabs()) {
                if (!held.add(t)) throw new IllegalArgumentException("the split grid holds " + t + " in two regions");
            }
        }
    }

    public static SplitGrid of(Frame frame) { return new SplitGrid(frame); }

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

    /** The widgets, region by region in the frame's order, each region's tabs in its strip's. */
    @Override
    public List<WidgetRef> placed() {
        var out = new ArrayList<WidgetRef>();
        for (Region r : regions()) out.addAll(r.tabs());
        return List.copyOf(out);
    }

    /** The regions, in the frame's order: left to right, top to bottom, depth first - as the grid's layout orders them. */
    public List<Region> regions() { return regionsOf(frame); }

    /** The region holding that widget, if one does. */
    public Optional<Region> regionOf(WidgetRef widget) {
        for (Region r : regions()) if (r.tabs().contains(widget)) return Optional.of(r);
        return Optional.empty();
    }

    private static List<Region> regionsOf(Frame frame) {
        var out = new ArrayList<Region>();
        collect(frame, out);
        return List.copyOf(out);
    }

    private static void collect(Frame frame, List<Region> out) {
        switch (frame) {
            case Region r -> out.add(r);
            case Split s -> { for (Part p : s.parts()) collect(p.frame(), out); }
        }
    }
}
