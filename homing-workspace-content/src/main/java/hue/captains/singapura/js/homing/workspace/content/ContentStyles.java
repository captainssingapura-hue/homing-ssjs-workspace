package hue.captains.singapura.js.homing.workspace.content;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.Wearable;

import java.util.List;

import static hue.captains.singapura.js.homing.design.DesignClass.of;
import static hue.captains.singapura.js.homing.design.Emphasis.Muted;
import static hue.captains.singapura.js.homing.design.Target.Color;

/**
 * The flow's sheet, in the design's words and nothing of its own: a column of parts, each a
 * box its widget is lent - one that flows takes the height its content needs, one that fills is
 * given a height the reader may drag - and a muted line where there is nothing to show.
 */
public record ContentStyles() implements CssGroup<ContentStyles> {

    public static final ContentStyles INSTANCE = new ContentStyles();

    /** The column a flow's parts stand in. */
    public record fl_column() implements CssClass<ContentStyles> {
        @Override public String body() { return """
            display: flex;
            flex-direction: column;
            gap: 12px;
            min-width: 0;
            """;
        }
    }

    /** A part's box, lent to its widget: as tall as a flowing widget's content. */
    public record fl_part() implements CssClass<ContentStyles> {
        @Override public String body() { return """
            position: relative;
            min-width: 0;
            """;
        }
    }

    /** A box for a widget that fills what it is lent: a height of its own, which the reader may drag. */
    public record fl_part_fill() implements CssClass<ContentStyles> {
        @Override public String body() { return """
            height: 20rem;
            min-height: 6rem;
            overflow: hidden;
            resize: vertical;
            """;
        }
    }

    /** What a flow says when it has nothing to show: waiting, unavailable, a type not offered. */
    public record fl_note() implements CssClass<ContentStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Muted.class, Color.Ink.class)); }
        @Override public String body() { return "margin: 0;\n"; }
    }

    /** What is said away: a note with nothing to say. */
    public record fl_hidden() implements CssClass<ContentStyles> {
        @Override public String body() { return "display: none;\n"; }
    }

    @Override
    public List<CssClass<ContentStyles>> cssClasses() { return List.of(new fl_column(), new fl_part(), new fl_part_fill(), new fl_note(), new fl_hidden()); }
}
