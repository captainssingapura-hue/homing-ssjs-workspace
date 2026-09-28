package hue.captains.singapura.js.homing.workspace.demowidgets.books;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.Wearable;

import java.util.List;

import static hue.captains.singapura.js.homing.design.DesignClass.of;
import static hue.captains.singapura.js.homing.design.Emphasis.Muted;
import static hue.captains.singapura.js.homing.design.Layer.Recessed;
import static hue.captains.singapura.js.homing.design.Target.Color;
import static hue.captains.singapura.js.homing.design.Target.Type;
import static hue.captains.singapura.js.homing.design.Text.Display;
import static hue.captains.singapura.js.homing.design.Text.Heading;
import static hue.captains.singapura.js.homing.design.Text.Lede;

/**
 * The books widgets' own sheet: the jumbotron's stage and its lines. The
 * lines are sized by the stage - a container of its own, measured - so the
 * chosen book is as big as the box it is lent allows, at every size.
 */
public record BookStyles() implements CssGroup<BookStyles> {

    public static final BookStyles INSTANCE = new BookStyles();

    /** The jumbotron's root, measured: the box it is lent, which everything in it is sized by. */
    public record bj_frame() implements CssClass<BookStyles> {
        @Override public String body() { return "container-type: size;"; }
    }

    /** The jumbotron's stage: the whole of its root, what it shows centred in it. */
    public record bj_stage() implements CssClass<BookStyles> {
        @Override public String body() { return """
            flex: 1 1 auto;
            min-width: 0;
            min-height: 0;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            gap: 2cqmin;
            padding: 6cqmin;
            box-sizing: border-box;
            overflow: hidden;
            text-align: center;
            """;
        }
    }

    /** The chosen book's title: as big as the stage allows, in the design's display voice. */
    public record bj_title() implements CssClass<BookStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Heading.class, Type.Face.class), of(Display.class, Type.Weight.class), of(Display.class, Color.Ink.class)); }
        @Override public String body() { return """
            font-size: clamp(20px, 11cqmin, 120px);
            line-height: 1.1;
            overflow-wrap: anywhere;
            """;
        }
    }

    /** Its author, under it: smaller, in a lede's colour. */
    public record bj_author() implements CssClass<BookStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Lede.class, Color.Ink.class)); }
        @Override public String body() { return """
            font-size: clamp(14px, 5.5cqmin, 56px);
            line-height: 1.2;
            overflow-wrap: anywhere;
            """;
        }
    }

    /** What it says with no book chosen: quietly. */
    public record bj_none() implements CssClass<BookStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Muted.class, Color.Ink.class)); }
        @Override public String body() { return "font-size: clamp(14px, 5cqmin, 40px);"; }
    }

    /** A line not shown. */
    public record bj_hidden() implements CssClass<BookStyles> {
        @Override public String body() { return "display: none;"; }
    }

    /**
     * The book browser's root: the whole of the box it is lent, its two boxes side by
     * side - or, where the box is too narrow for both, one above the other, each line of
     * them taking its share of the height.
     */
    public record bb_split() implements CssClass<BookStyles> {
        @Override public String body() { return """
            position: absolute;
            inset: 0;
            display: flex;
            flex-wrap: wrap;
            align-content: stretch;
            gap: 8px;
            min-width: 0;
            min-height: 0;
            box-sizing: border-box;
            """;
        }
    }

    /** The box the browser lends its grid: most of the width. */
    public record bb_grid() implements CssClass<BookStyles> {
        @Override public String body() { return """
            position: relative;
            flex: 3 1 320px;
            min-width: 0;
            min-height: 0;
            """;
        }
    }

    /** The box the browser lends its jumbotron: the rest, set a layer back. */
    public record bb_chosen() implements CssClass<BookStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Recessed.class, Color.Surface.class)); }
        @Override public String body() { return """
            position: relative;
            flex: 2 1 220px;
            min-width: 0;
            min-height: 0;
            """;
        }
    }

    @Override
    public List<CssClass<BookStyles>> cssClasses() {
        return List.of(new bj_frame(), new bj_stage(), new bj_title(), new bj_author(), new bj_none(), new bj_hidden(), new bb_split(), new bb_grid(), new bb_chosen());
    }
}
