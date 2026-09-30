package hue.captains.singapura.js.homing.workspace.tree;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.Wearable;

import java.util.List;

import static hue.captains.singapura.js.homing.design.DesignClass.of;
import static hue.captains.singapura.js.homing.design.Emphasis.Muted;
import static hue.captains.singapura.js.homing.design.Target.Color;
import static hue.captains.singapura.js.homing.design.Target.Type;
import static hue.captains.singapura.js.homing.design.Text.Body;
import static hue.captains.singapura.js.homing.design.Text.Heading;
import static hue.captains.singapura.js.homing.design.Text.Label;

/**
 * The tree layout's sheet, in the design's words and nothing of its own: a reading
 * column, each node a section under its heading - the root's and the first level's
 * headings as headings, deeper ones as labels - and each nameless leaf a box its widget
 * is lent: one that flows takes the height its content needs, one that fills is given a
 * height the reader may drag.
 */
public record TreeStyles() implements CssGroup<TreeStyles> {

    public static final TreeStyles INSTANCE = new TreeStyles();

    /** The column the tree is read in. */
    public record tl_column() implements CssClass<TreeStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Body.class, Type.Face.class), of(Body.class, Color.Ink.class)); }
        @Override public String body() { return """
            display: flex;
            flex-direction: column;
            gap: 12px;
            max-width: 64rem;
            padding: 16px 24px 48px;
            box-sizing: border-box;
            """;
        }
    }

    /** A node's section: its heading, its leaves, its children. */
    public record tl_section() implements CssClass<TreeStyles> {
        @Override public String body() { return """
            display: flex;
            flex-direction: column;
            gap: 12px;
            """;
        }
    }

    /** The root's heading and the first level's. */
    public record tl_heading() implements CssClass<TreeStyles> {
        @Override public List<? extends Wearable> wears() {
            return List.of(of(Heading.class, Type.Face.class), of(Heading.class, Type.Scale.class), of(Heading.class, Type.Weight.class), of(Heading.class, Color.Ink.class));
        }
        @Override public String body() { return "margin: 8px 0 0;\n"; }
    }

    /** A heading below the first level. */
    public record tl_subheading() implements CssClass<TreeStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Label.class, Type.Scale.class), of(Label.class, Type.Weight.class)); }
        @Override public String body() { return "margin: 4px 0 0;\n"; }
    }

    /** A label's code run. */
    public record tl_code() implements CssClass<TreeStyles> {
        @Override public String body() { return "font-family: monospace;\n"; }
    }

    /** A nameless leaf's box, lent to its widget: as tall as a flowing widget's content. */
    public record tl_leaf() implements CssClass<TreeStyles> {
        @Override public String body() { return """
            position: relative;
            min-width: 0;
            """;
        }
    }

    /** A box for a widget that fills what it is lent: a height of its own, which the reader may drag. */
    public record tl_leaf_fill() implements CssClass<TreeStyles> {
        @Override public String body() { return """
            height: 20rem;
            min-height: 6rem;
            overflow: hidden;
            resize: vertical;
            """;
        }
    }

    /** Where the host offers no widget of the leaf's type. */
    public record tl_missing() implements CssClass<TreeStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Muted.class, Color.Ink.class)); }
        @Override public String body() { return "margin: 0;\n"; }
    }

    @Override
    public List<CssClass<TreeStyles>> cssClasses() {
        return List.of(new tl_column(), new tl_section(), new tl_heading(), new tl_subheading(), new tl_code(),
                new tl_leaf(), new tl_leaf_fill(), new tl_missing());
    }
}
