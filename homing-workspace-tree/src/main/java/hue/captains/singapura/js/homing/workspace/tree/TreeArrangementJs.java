package hue.captains.singapura.js.homing.workspace.tree;

import hue.captains.singapura.js.homing.workspace.groups.core.models.ArrangedWidget;
import hue.captains.singapura.js.homing.workspace.groups.core.models.Arrangement;
import hue.captains.singapura.js.homing.workspace.groups.core.models.TreePlacement;
import hue.captains.singapura.js.homing.workspace.groups.core.models.WidgetRef;

import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A tree's arrangement as a page has it: a frozen constant, generated from its declaration -
 * an {@link Arrangement} whose placement is a {@link TreePlacement} - never written by hand;
 * what {@code TreeLayout} lays out.
 *
 * <pre>{@code
 * const NAME = Object.freeze({ engine: "tree", workspace: "tree-bench",
 *     widgets: Object.freeze({ "intro": Object.freeze({ kind: "note", params: Object.freeze({ "note": "intro" }) }), … }),
 *     root: Object.freeze({ name: "", label: Object.freeze({ text: "The doc", runs: Object.freeze([]) }),
 *         leaves: Object.freeze(["intro"]), children: Object.freeze([ … ]) }) });
 * }</pre>
 */
public final class TreeArrangementJs {

    /** A constant's name: upper case, words by underscores. */
    public static final Pattern CONST_NAME = Pattern.compile("[A-Z][A-Z0-9_]*");

    private TreeArrangementJs() {}

    /** {@code const <name> = Object.freeze({...});} - the arrangement, frozen through. */
    public static String constant(String name, Arrangement<?, TreePlacement> arrangement) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(arrangement, "arrangement");
        if (!CONST_NAME.matcher(name).matches()) throw new IllegalArgumentException("not a constant's name: " + name);
        return "const " + name + " = " + expression(arrangement) + ";";
    }

    /** The arrangement as an expression. */
    public static String expression(Arrangement<?, TreePlacement> arrangement) {
        Objects.requireNonNull(arrangement, "arrangement");
        String widgets = arrangement.widgets().stream().map(TreeArrangementJs::widget).collect(Collectors.joining(", "));
        return "Object.freeze({ engine: " + quote(arrangement.engine().value())
                + ", workspace: " + quote(arrangement.workspace().workspaceKind().value())
                + ", widgets: Object.freeze({ " + widgets + " })"
                + ", root: " + node(arrangement.placement().root()) + " })";
    }

    private static String widget(ArrangedWidget w) {
        String params = w.params().entrySet().stream().map(e -> quote(e.getKey()) + ": " + quote(e.getValue())).collect(Collectors.joining(", "));
        return quote(w.ref().value()) + ": Object.freeze({ kind: " + quote(w.kind().value()) + ", params: Object.freeze({" + (params.isEmpty() ? "" : " " + params + " ") + "}) })";
    }

    private static String node(TreePlacement.Node n) {
        return "Object.freeze({ name: " + quote(n.name().map(TreePlacement.Name::value).orElse(""))
                + ", label: " + label(n.label())
                + ", leaves: Object.freeze([" + n.leaves().stream().map(WidgetRef::value).map(TreeArrangementJs::quote).collect(Collectors.joining(", ")) + "])"
                + ", children: Object.freeze([" + n.children().stream().map(TreeArrangementJs::node).collect(Collectors.joining(", ")) + "]) })";
    }

    private static String label(TreePlacement.Label l) {
        String runs = l.runs().stream().map(r -> "Object.freeze({ kind: " + quote(kind(r)) + ", text: " + quote(r.text()) + " })").collect(Collectors.joining(", "));
        return "Object.freeze({ text: " + quote(l.text()) + ", runs: Object.freeze([" + runs + "]) })";
    }

    private static String kind(TreePlacement.Run r) {
        return switch (r) {
            case TreePlacement.Run.Text t -> "text";
            case TreePlacement.Run.Code c -> "code";
            case TreePlacement.Run.Strong s -> "strong";
            case TreePlacement.Run.Emphasis e -> "emphasis";
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
                    if (c < 0x20 || c == ' ' || c == ' ' || c == '<') out.append(String.format("\\u%04x", (int) c));
                    else out.append(c);
                }
            }
        }
        return out.append('"').toString();
    }
}
