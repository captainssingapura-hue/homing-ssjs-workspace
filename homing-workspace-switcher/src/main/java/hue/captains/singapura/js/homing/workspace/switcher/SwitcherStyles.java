package hue.captains.singapura.js.homing.workspace.switcher;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.Wearable;

import java.util.List;

import static hue.captains.singapura.js.homing.design.DesignClass.of;
import static hue.captains.singapura.js.homing.design.Emphasis.Muted;
import static hue.captains.singapura.js.homing.design.Layer.Recessed;
import static hue.captains.singapura.js.homing.design.Target.Color;
import static hue.captains.singapura.js.homing.design.Target.Type;
import static hue.captains.singapura.js.homing.design.Text.Caption;
import static hue.captains.singapura.js.homing.design.Text.Heading;

/**
 * The switcher's sheet, in the design's words and nothing of its own: the
 * bodies lay out, the designs colour and set. Each widget is a column - a head
 * saying what it shows, then what scrolls, a quiet note where there is nothing
 * to show; the composed switcher lays its two side by side, or one above the
 * other where its box is narrow.
 */
public record SwitcherStyles() implements CssGroup<SwitcherStyles> {

    public static final SwitcherStyles INSTANCE = new SwitcherStyles();

    /** A switcher widget's root, beside what fills its container: a little air, and its parts apart. */
    public record sw_column() implements CssClass<SwitcherStyles> {
        @Override public String body() { return """
            gap: 6px;
            padding: 10px 12px;
            """;
        }
    }

    /** What a widget shows, and how many, on one baseline. */
    public record sw_head() implements CssClass<SwitcherStyles> {
        @Override public String body() { return """
            flex: 0 0 auto;
            display: flex;
            align-items: baseline;
            justify-content: space-between;
            gap: 12px;
            min-width: 0;
            """;
        }
    }

    /** What it shows, in the design's heading voice, on one line. */
    public record sw_title() implements CssClass<SwitcherStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Heading.class, Type.Face.class), of(Heading.class, Type.Weight.class), of(Heading.class, Color.Ink.class)); }
        @Override public String body() { return """
            margin: 0;
            min-width: 0;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            """;
        }
    }

    /** How many, quietly; its figures of one width, so the heading beside it never moves. */
    public record sw_count() implements CssClass<SwitcherStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Caption.class, Type.Scale.class), of(Muted.class, Color.Ink.class)); }
        @Override public String body() { return """
            flex: 0 0 auto;
            font-variant-numeric: tabular-nums;
            """;
        }
    }

    /** What it says where there is nothing to show - or how to go on - quietly. */
    public record sw_note() implements CssClass<SwitcherStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Caption.class, Type.Scale.class), of(Muted.class, Color.Ink.class)); }
        @Override public String body() { return """
            flex: 0 0 auto;
            margin: 0;
            min-width: 0;
            """;
        }
    }

    /** A question put to a person, in a dialog's body: a little air around it. */
    public record sw_question() implements CssClass<SwitcherStyles> {
        @Override public String body() { return """
            margin: 0;
            padding: 16px 20px;
            line-height: 1.5;
            """;
        }
    }

    /** A part not shown. */
    public record sw_hidden() implements CssClass<SwitcherStyles> {
        @Override public String body() { return "display: none;"; }
    }

    /** The composed switcher's root: the whole of its box, its two side by side - or one above the other, where the box is narrow. */
    public record sw_split() implements CssClass<SwitcherStyles> {
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

    /** The box the switcher lends its kinds: the narrower, set a layer back. */
    public record sw_kinds() implements CssClass<SwitcherStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Recessed.class, Color.Surface.class)); }
        @Override public String body() { return """
            position: relative;
            flex: 1 1 200px;
            min-width: 0;
            min-height: 0;
            """;
        }
    }

    /** The box the switcher lends its workspaces: the wider. */
    public record sw_instances() implements CssClass<SwitcherStyles> {
        @Override public String body() { return """
            position: relative;
            flex: 2 1 320px;
            min-width: 0;
            min-height: 0;
            """;
        }
    }

    @Override
    public List<CssClass<SwitcherStyles>> cssClasses() {
        return List.of(new sw_column(), new sw_head(), new sw_title(), new sw_count(), new sw_note(), new sw_question(), new sw_hidden(),
                       new sw_split(), new sw_kinds(), new sw_instances());
    }
}
