package hue.captains.singapura.js.homing.workspace.state;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Where a split is in the layout tree: the child indexes from the root, joined
 * by {@code "/"}. The root split's path is the empty string.
 *
 * <p>A pane has a {@link PaneId} and is named by it everywhere. A split has no
 * name — nothing outside the tree refers to one, and giving it an id would mean
 * minting, storing and migrating identifiers for something no widget, no event
 * and no URL ever points at. So a split is addressed by where it is, and only
 * where an id genuinely does not exist: {@code TracksChanged} is the one event
 * that carries a path, because re-sharing is the one thing that happens to a
 * split rather than to a pane.</p>
 *
 * <p>The grammar is the live grid's, so a path crosses between them unchanged.</p>
 *
 * @param value child indexes joined by {@code "/"}, or empty for the root
 * @since schema 2
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
