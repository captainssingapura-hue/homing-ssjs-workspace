package hue.captains.singapura.js.homing.workspace.tree;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.CssVar;
import hue.captains.singapura.js.homing.core.Wearable;

import java.util.List;
import java.util.Set;

import static hue.captains.singapura.js.homing.design.DesignClass.of;
import static hue.captains.singapura.js.homing.design.Emphasis.Muted;
import static hue.captains.singapura.js.homing.design.Emphasis.Primary;
import static hue.captains.singapura.js.homing.design.Emphasis.Secondary;
import static hue.captains.singapura.js.homing.design.Interaction.Current;
import static hue.captains.singapura.js.homing.design.Interaction.Selectable;
import static hue.captains.singapura.js.homing.design.Layer.Base;
import static hue.captains.singapura.js.homing.design.Structure.Hairline;
import static hue.captains.singapura.js.homing.design.Target.Color;
import static hue.captains.singapura.js.homing.design.Target.Motion;
import static hue.captains.singapura.js.homing.design.Target.Shape;
import static hue.captains.singapura.js.homing.design.Target.Type;
import static hue.captains.singapura.js.homing.design.Text.Body;
import static hue.captains.singapura.js.homing.design.Text.Heading;
import static hue.captains.singapura.js.homing.design.Text.Label;

/**
 * The tree layout's sheet, in the design's words and nothing of its own: a reading
 * column, each node a section under its heading - the root's and the first level's
 * headings as headings, deeper ones as labels - and each widget of a leaf a box it
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

    /** The table of contents: the tree, in the body's face, scrolling on its own. */
    public record tl_toc() implements CssClass<TreeStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Body.class, Type.Face.class), of(Body.class, Color.Ink.class)); }
        @Override public String body() { return """
            padding: 8px 4px;
            box-sizing: border-box;
            """;
        }
    }

    /** A node's section: its heading, then its body. */
    public record tl_section() implements CssClass<TreeStyles> {
        @Override public String body() { return """
            display: flex;
            flex-direction: column;
            gap: 12px;
            """;
        }
    }

    /**
     * A section's own part - its heading and its leaf, not its children: what is marked while
     * it is the current section, the change eased as the design eases a selectable's.
     */
    public record tl_own() implements CssClass<TreeStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Selectable.class, Motion.Ease.class)); }
        @Override public String body() { return """
            display: flex;
            flex-direction: column;
            gap: 12px;
            padding: 8px 12px;
            margin: 0 -12px;
            """;
        }
    }

    /**
     * The current section's own part: the design's current surface and its mark - the section the
     * contents' cursor is on. The mark is drawn in a colour of the design's: the primary, the
     * secondary, the base surface or the body's ink.
     */
    public record tl_current() implements CssClass<TreeStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Current.class, Color.Surface.class), of(Current.class, Shape.Shadow.class)); }
        @Override public List<? extends Wearable> reads() {
            return List.of(of(Primary.class, Color.Surface.class), of(Secondary.class, Color.Surface.class), of(Base.class, Color.Surface.class), of(Body.class, Color.Ink.class));
        }
        @Override public String body() { return ""; }
    }

    /** A section's leaf, and its children: each folds away under its heading. */
    public record tl_body() implements CssClass<TreeStyles> {
        @Override public String body() { return """
            display: flex;
            flex-direction: column;
            gap: 12px;
            """;
        }
    }

    /**
     * A section below the root: indented under its parent, a hairline down its left the length
     * of the section, so the tree's depth reads as the contents' does. The line's colour is the
     * design's; only its side, width and style are the sheet's.
     */
    public record tl_nested() implements CssClass<TreeStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Hairline.class, Color.Edge.class)); }
        @Override public String body() { return """
            margin-left: 4px;
            padding-left: 16px;
            border-left-width: 1px;
            border-left-style: solid;
            """;
        }
    }

    /** What is folded away: a folded section's leaf and children. */
    public record tl_hidden() implements CssClass<TreeStyles> {
        @Override public String body() { return "display: none;\n"; }
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

    /** The box of a widget of a leaf, lent to it: as tall as a flowing widget's content. */
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

    /** Where the host offers no widget of the type asked. */
    public record tl_missing() implements CssClass<TreeStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Muted.class, Color.Ink.class)); }
        @Override public String body() { return "margin: 0;\n"; }
    }

    /** Where a widget lent to the stage was: its height held, so nothing moves while it is away; a dashed hairline, and a word for where it went. */
    public record tl_held() implements CssClass<TreeStyles> {
        @Override public Set<CssVar> runtimeVars() { return Set.of(new CssVar("--tl-held")); }
        @Override public List<? extends Wearable> wears() { return List.of(of(Hairline.class, Color.Edge.class), of(Muted.class, Color.Ink.class)); }
        @Override public String body() {
            return "box-sizing: border-box;\nmin-height: var(--tl-held);\nmargin: 0;\ndisplay: flex;\nalign-items: center;\njustify-content: center;\n"
                    + "border-width: 1px;\nborder-style: dashed;\nfont-style: italic;\n";
        }
    }

    @Override
    public List<CssClass<TreeStyles>> cssClasses() {
        return List.of(new tl_toc(), new tl_column(), new tl_section(), new tl_own(), new tl_current(), new tl_body(), new tl_nested(), new tl_hidden(), new tl_heading(), new tl_subheading(), new tl_code(),
                new tl_leaf(), new tl_leaf_fill(), new tl_missing(), new tl_held());
    }
}
