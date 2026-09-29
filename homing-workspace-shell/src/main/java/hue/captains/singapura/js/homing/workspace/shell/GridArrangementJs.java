package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.workspace.groups.core.models.ArrangedWidget;
import hue.captains.singapura.js.homing.workspace.groups.core.models.Arrangement;
import hue.captains.singapura.js.homing.workspace.groups.core.models.SplitGrid;
import hue.captains.singapura.js.homing.workspace.groups.core.models.WidgetRef;

import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A split grid's arrangement as a page has it: a frozen constant, generated from
 * its declaration - an {@link Arrangement} whose placement is a {@link SplitGrid} -
 * never written by hand; what {@code GridArrangement.apply} lays out.
 *
 * <pre>{@code
 * const NAME = Object.freeze({ engine: "split-grid", workspace: "demo",
 *     widgets: Object.freeze({ "left": Object.freeze({ kind: "video", params: Object.freeze({}) }), … }),
 *     frame: Object.freeze({ kind: "split", axis: "horizontal", parts: Object.freeze([
 *         Object.freeze({ weight: 1, frame: Object.freeze({ kind: "region", name: "west", tabs: Object.freeze(["left"]), shown: "left" }) }), … ]) }) });
 * }</pre>
 */
public final class GridArrangementJs {

    /** A constant's name: upper case, words by underscores. */
    public static final Pattern CONST_NAME = Pattern.compile("[A-Z][A-Z0-9_]*");

    private GridArrangementJs() {}

    /** {@code const <name> = Object.freeze({...});} - the arrangement, frozen through. */
    public static String constant(String name, Arrangement<?, SplitGrid> arrangement) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(arrangement, "arrangement");
        if (!CONST_NAME.matcher(name).matches()) throw new IllegalArgumentException("not a constant's name: " + name);
        String widgets = arrangement.widgets().stream().map(GridArrangementJs::widget).collect(Collectors.joining(", "));
        return "const " + name + " = Object.freeze({ engine: " + quote(arrangement.engine().value())
                + ", workspace: " + quote(arrangement.workspace().workspaceKind().value())
                + ", widgets: Object.freeze({ " + widgets + " })"
                + ", frame: " + frame(arrangement.placement().frame()) + " });";
    }

    private static String widget(ArrangedWidget w) {
        String params = w.params().entrySet().stream().map(e -> quote(e.getKey()) + ": " + quote(e.getValue())).collect(Collectors.joining(", "));
        return quote(w.ref().value()) + ": Object.freeze({ kind: " + quote(w.kind().value()) + ", params: Object.freeze({" + (params.isEmpty() ? "" : " " + params + " ") + "}) })";
    }

    private static String frame(SplitGrid.Frame frame) {
        return switch (frame) {
            case SplitGrid.Region r -> "Object.freeze({ kind: \"region\", name: " + quote(r.name().value())
                    + ", tabs: Object.freeze([" + r.tabs().stream().map(t -> quote(t.value())).collect(Collectors.joining(", ")) + "])"
                    + ", shown: " + r.showing().map(WidgetRef::value).map(GridArrangementJs::quote).orElse("null") + " })";
            case SplitGrid.Split s -> "Object.freeze({ kind: \"split\", axis: " + quote(s.axis().name().toLowerCase())
                    + ", parts: Object.freeze([" + s.parts().stream().map(p -> "Object.freeze({ weight: " + p.weight() + ", frame: " + frame(p.frame()) + " })")
                            .collect(Collectors.joining(", ")) + "]) })";
        };
    }

    /** A JavaScript string literal: quotes, backslashes, the controls, the line separators and '<' escaped. */
    static String quote(String s) {
        var out = new StringBuilder("\"");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                default -> {
                    if (c < 0x20 || c == '\u2028' || c == '\u2029' || c == '<') out.append(String.format("\\u%04x", (int) c));
                    else out.append(c);
                }
            }
        }
        return out.append('"').toString();
    }
}
