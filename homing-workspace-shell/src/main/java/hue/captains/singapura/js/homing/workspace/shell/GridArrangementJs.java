package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.layout.Layout;
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
 *     layout: Object.freeze({ kind: "horizontal", parts: Object.freeze([Object.freeze({ kind: "whole", id: "west" }), … ]) }),
 *     regions: Object.freeze({ "west": Object.freeze({ tabs: Object.freeze(["left"]), shown: "left" }), … }),
 *     weights: Object.freeze({ "H(west, east)": Object.freeze([1, 1]) }) });
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
        return "const " + name + " = " + expression(arrangement) + ";";
    }

    /** The arrangement as an expression - for a constant of several, by kind. */
    public static String expression(Arrangement<?, SplitGrid> arrangement) {
        Objects.requireNonNull(arrangement, "arrangement");
        String widgets = arrangement.widgets().stream().map(GridArrangementJs::widget).collect(Collectors.joining(", "));
        return "Object.freeze({ engine: " + quote(arrangement.engine().value())
                + ", workspace: " + quote(arrangement.workspace().workspaceKind().value())
                + ", widgets: Object.freeze({ " + widgets + " })"
                + ", layout: " + layout(arrangement.placement().layout())
                + ", regions: Object.freeze({ " + arrangement.placement().regions().stream().map(GridArrangementJs::region).collect(Collectors.joining(", ")) + " })"
                + ", weights: Object.freeze({ " + arrangement.placement().splits().stream()
                        .map(s -> quote(s.text()) + ": Object.freeze([" + arrangement.placement().weights().get(s).stream().map(String::valueOf).collect(Collectors.joining(", ")) + "])")
                        .collect(Collectors.joining(", ")) + " }) })";
    }

    private static String widget(ArrangedWidget w) {
        String params = w.params().entrySet().stream().map(e -> quote(e.getKey()) + ": " + quote(e.getValue())).collect(Collectors.joining(", "));
        return quote(w.ref().value()) + ": Object.freeze({ kind: " + quote(w.kind().value()) + ", params: Object.freeze({" + (params.isEmpty() ? "" : " " + params + " ") + "}) })";
    }

    /** A layout as data, as the layout algebra's JavaScript reads it: frozen through. */
    private static String layout(Layout layout) {
        return switch (layout) {
            case Layout.Whole w -> "Object.freeze({ kind: \"whole\", id: " + quote(w.id().value()) + " })";
            case Layout.Horizontal_NSplit h -> "Object.freeze({ kind: \"horizontal\", parts: Object.freeze([" + h.parts().stream().map(GridArrangementJs::layout).collect(Collectors.joining(", ")) + "]) })";
            case Layout.Vertical_NSplit v -> "Object.freeze({ kind: \"vertical\", parts: Object.freeze([" + v.parts().stream().map(GridArrangementJs::layout).collect(Collectors.joining(", ")) + "]) })";
        };
    }

    /** A region's dock, by its name: its tabs in order, the one it shows. */
    private static String region(SplitGrid.Region r) {
        return quote(r.name().value()) + ": Object.freeze({ tabs: Object.freeze([" + r.tabs().stream().map(t -> quote(t.value())).collect(Collectors.joining(", ")) + "])"
                + ", shown: " + r.showing().map(WidgetRef::value).map(GridArrangementJs::quote).orElse("null") + " })";
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
