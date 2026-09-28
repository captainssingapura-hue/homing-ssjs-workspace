package hue.captains.singapura.js.homing.workspace.log;

import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * The workspace log's identifiers and names: each a record of one scalar,
 * written bare on the wire. A holder, not a type of its own — it gathers the
 * small ones so that they are one JavaScript module, as they are one Java file.
 */
public final class LogIds {

    private LogIds() {}

    /**
     * Which tab: the id its desk's register gave it, {@code "tab-3"}, kept for the
     * tab's whole life and written into the log under it. It says which tab, never
     * what the tab holds — a tab may become something else.
     *
     * @param value letters, digits, hyphen, underscore
     */
    public record TabId(String value) {

        private static final Pattern GRAMMAR = Pattern.compile("[A-Za-z0-9_-]+");

        public TabId {
            Objects.requireNonNull(value, "TabId.value");
            if (!GRAMMAR.matcher(value).matches()) {
                throw new IllegalArgumentException("TabId.value '" + value + "' — letters, digits, hyphen, underscore");
            }
        }

        public static TabId of(String value) { return new TabId(value); }

        @Override public String toString() { return value; }
    }

    /**
     * Which region of the workspace: a cell of its split grid and the dock in it,
     * named by the grid — {@code "main"}, and the cells it mints as it subdivides.
     *
     * @param value letters, digits, hyphen, underscore
     */
    public record RegionId(String value) {

        private static final Pattern GRAMMAR = Pattern.compile("[A-Za-z0-9_-]+");

        public RegionId {
            Objects.requireNonNull(value, "RegionId.value");
            if (!GRAMMAR.matcher(value).matches()) {
                throw new IllegalArgumentException("RegionId.value '" + value + "' — letters, digits, hyphen, underscore");
            }
        }

        public static RegionId of(String value) { return new RegionId(value); }

        @Override public String toString() { return value; }
    }

    /**
     * Which float: a frame on the desk around a host of its own, named by the desk
     * — {@code "float-1"} and on, never a name another float of that desk had.
     *
     * @param value letters, digits, hyphen, underscore
     */
    public record FloatId(String value) {

        private static final Pattern GRAMMAR = Pattern.compile("[A-Za-z0-9_-]+");

        public FloatId {
            Objects.requireNonNull(value, "FloatId.value");
            if (!GRAMMAR.matcher(value).matches()) {
                throw new IllegalArgumentException("FloatId.value '" + value + "' — letters, digits, hyphen, underscore");
            }
        }

        public static FloatId of(String value) { return new FloatId(value); }

        @Override public String toString() { return value; }
    }

    /**
     * Which kind of widget a tab holds — the key the page's tab source looks up
     * "how to construct one of these" by. Identifier-shaped: {@code "note"},
     * {@code "spinning-animals"}, {@code "doc_view"}.
     *
     * @param value letters, digits, hyphen, underscore
     */
    public record WidgetKind(String value) {

        private static final Pattern GRAMMAR = Pattern.compile("[A-Za-z0-9_-]+");

        public WidgetKind {
            Objects.requireNonNull(value, "WidgetKind.value");
            if (!GRAMMAR.matcher(value).matches()) {
                throw new IllegalArgumentException(
                        "WidgetKind.value '" + value + "' — identifier-shaped required (letters, digits, hyphen, underscore)");
            }
        }

        public static WidgetKind of(String value) { return new WidgetKind(value); }

        @Override public String toString() { return value; }
    }

    /**
     * Which widget: the id the workspace's core gave it when it was opened — its
     * PREFIX, what it is in a word (its kind, and a concise form of its params
     * when it has any), and a SEQUENCE climbing for that prefix, never reused in
     * the workspace: {@code books-grid-1}, {@code books-grid_title-rating-2}. The
     * widget's, never a tab's: a placement that shows the widget names it by
     * this. The core's WidgetIds makes the prefix; the log knows only that an id
     * is a prefix and a sequence, and holds the sequence to climbing.
     *
     * @param value a prefix of letters, digits, hyphens and underscores; a hyphen; a sequence from 1, at most nine digits
     */
    public record WidgetId(String value) {

        private static final Pattern GRAMMAR = Pattern.compile("[A-Za-z0-9_-]+-[1-9]\\d*");

        /** The most digits a sequence has: nine, so it is an int in both languages. */
        public static final int SEQUENCE_DIGITS = 9;

        public WidgetId {
            Objects.requireNonNull(value, "WidgetId.value");
            if (!GRAMMAR.matcher(value).matches()) {
                throw new IllegalArgumentException("WidgetId.value '" + value + "' — a prefix (letters, digits, hyphen, underscore), a hyphen, a sequence from 1");
            }
            if (value.length() - value.lastIndexOf('-') - 1 > SEQUENCE_DIGITS) {
                throw new IllegalArgumentException("WidgetId.value '" + value + "' — a sequence of at most " + SEQUENCE_DIGITS + " digits");
            }
        }

        public static WidgetId of(String value) { return new WidgetId(value); }

        /** The n-th id of a prefix. */
        public static WidgetId of(String prefix, int sequence) {
            if (sequence < 1) throw new IllegalArgumentException("WidgetId — a sequence starts at 1: " + sequence);
            return new WidgetId(prefix + "-" + sequence);
        }

        /** What the widget is, in a word: all before the last hyphen. */
        public String prefix() { return value.substring(0, value.lastIndexOf('-')); }

        /** Its place among its prefix's: the number after the last hyphen. */
        public int sequence() { return Integer.parseInt(value.substring(value.lastIndexOf('-') + 1)); }

        @Override public String toString() { return value; }
    }

    /**
     * What a tab is called: the label on its chip. A tab is opened, and comes back
     * on a restore, under the title the log last gave it. Free-form: any string.
     *
     * @param value the displayed label
     */
    public record WidgetTitle(String value) {

        public WidgetTitle {
            Objects.requireNonNull(value, "WidgetTitle.value");
        }

        public static WidgetTitle of(String value) { return new WidgetTitle(value); }

        @Override public String toString() { return value; }
    }

    /**
     * Where a split is in the layout tree: the child indexes from the root, joined
     * by {@code "/"}. The root split's path is the empty string.
     *
     * <p>A region has a {@link RegionId} and is named by it everywhere. A split has no
     * name — nothing outside the tree refers to one, and giving it an id would mean
     * minting, storing and migrating identifiers for something no widget, no event
     * and no URL ever points at. So a split is addressed by where it is, and only
     * where an id genuinely does not exist: {@code TracksChanged} is the one event
     * that carries a path, because re-sharing is the one thing that happens to a
     * split rather than to a region.</p>
     *
     * <p>The grammar is the live grid's, so a path crosses between them unchanged.</p>
     *
     * @param value child indexes joined by {@code "/"}, or empty for the root
     */
    public record SplitPath(String value) {

        private static final Pattern GRAMMAR = Pattern.compile("(\\d+(/\\d+)*)?");

        /** The root split. */
        public static final SplitPath ROOT = new SplitPath("");

        public SplitPath {
            Objects.requireNonNull(value, "SplitPath.value");
            if (!GRAMMAR.matcher(value).matches()) {
                throw new IllegalArgumentException(
                        "SplitPath.value '" + value + "' — child indexes joined by '/', or empty for the root");
            }
        }

        public static SplitPath of(String value) { return new SplitPath(value); }

        /** This path's split, descended into its child {@code index}. */
        public SplitPath child(int index) {
            if (index < 0) throw new IllegalArgumentException("SplitPath.child: index must not be negative");
            return new SplitPath(value.isEmpty() ? Integer.toString(index) : value + "/" + index);
        }

        /** Whether this names the root split. */
        public boolean isRoot() { return value.isEmpty(); }

        @Override public String toString() { return value; }
    }

    /**
     * Where an event falls in its log: the store's sequence, climbing within one
     * log and never reused.
     *
     * @param value non-negative
     */
    public record EventSeq(long value) {

        public EventSeq {
            if (value < 0) {
                throw new IllegalArgumentException("EventSeq.value: must be non-negative, got " + value);
            }
        }

        /** Before the first event: what a fold of no events has gone through. */
        public static final EventSeq ZERO = new EventSeq(0);

        public static EventSeq of(long value) { return new EventSeq(value); }

        public boolean greaterThan(EventSeq other) { return this.value > other.value; }

        @Override public String toString() { return "EventSeq(" + value + ")"; }
    }

    /**
     * Which kind of workspace a log belongs to: the kind the page is addressed by
     * — {@code "demo"}, {@code "notes"} — each kind keeping a log of its own.
     *
     * @param value letters, digits, hyphen, underscore
     */
    public record WorkspaceKind(String value) {

        private static final Pattern GRAMMAR = Pattern.compile("[A-Za-z0-9_-]+");

        public WorkspaceKind {
            Objects.requireNonNull(value, "WorkspaceKind.value");
            if (!GRAMMAR.matcher(value).matches()) {
                throw new IllegalArgumentException("WorkspaceKind.value '" + value + "' — letters, digits, hyphen, underscore");
            }
        }

        public static WorkspaceKind of(String value) { return new WorkspaceKind(value); }

        @Override public String toString() { return value; }
    }

    /**
     * What a workspace is called, as its kind's workspaces are listed in the
     * browser: {@code "notes"}, {@code "notes 2"}, or what it was renamed to.
     * It says which one to a person, never to the log — the log is named by the
     * workspace's {@link WorkspaceInstanceId}. Free-form: any string.
     *
     * @param value the displayed name
     */
    public record WorkspaceName(String value) {

        public WorkspaceName {
            Objects.requireNonNull(value, "WorkspaceName.value");
        }

        public static WorkspaceName of(String value) { return new WorkspaceName(value); }

        @Override public String toString() { return value; }
    }

    /**
     * Which one workspace of its kind a log belongs to — a UUID under the wrapper,
     * lowercase on the wire. With the {@link WorkspaceKind}, it names a log.
     *
     * <p>A page that names no workspace keeps its kind's own: {@link
     * #placeholderFor(WorkspaceKind)}, the same UUID every visit, derived as the
     * browser's {@code WorkspaceLogIdentity.placeholder} derives it.</p>
     *
     * @param id the underlying UUID
     */
    public record WorkspaceInstanceId(UUID id) {

        public WorkspaceInstanceId { Objects.requireNonNull(id, "WorkspaceInstanceId.id"); }

        public static WorkspaceInstanceId fresh() {
            return new WorkspaceInstanceId(UUID.randomUUID());
        }

        public static WorkspaceInstanceId parse(String s) {
            return new WorkspaceInstanceId(UUID.fromString(s));
        }

        /**
         * The kind's own workspace: a UUID derived from {@code "workspace:" + kind}
         * by a stable hash, so the same kind always yields the same id.
         */
        public static WorkspaceInstanceId placeholderFor(WorkspaceKind kind) {
            Objects.requireNonNull(kind, "kind");
            String seed = "workspace:" + kind.value();
            int hash = 0;
            for (int i = 0; i < seed.length(); i++) {
                hash = (hash << 5) - hash + seed.charAt(i);
            }
            long high = (((long) hash) << 32) | 0x7000_5000_9000L;
            long low  = (((long) hash) << 32) | 0x0001L;
            return new WorkspaceInstanceId(new UUID(high, low));
        }

        @Override public String toString() { return id.toString(); }
    }
}
