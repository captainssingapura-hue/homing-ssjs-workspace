package hue.captains.singapura.js.homing.workspace.bench;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.Wearable;

import java.util.List;

import static hue.captains.singapura.js.homing.design.DesignClass.of;
import static hue.captains.singapura.js.homing.design.Emphasis.Muted;
import static hue.captains.singapura.js.homing.design.Layer.Base;
import static hue.captains.singapura.js.homing.design.Layer.Raised;
import static hue.captains.singapura.js.homing.design.Structure.Hairline;
import static hue.captains.singapura.js.homing.design.Target.Color;
import static hue.captains.singapura.js.homing.design.Target.Shape;
import static hue.captains.singapura.js.homing.design.Target.Type;
import static hue.captains.singapura.js.homing.design.Text.Body;
import static hue.captains.singapura.js.homing.design.Text.Label;

/**
 * The content bench's sheet: its page - a word on what it shows, two panels side by side, the
 * traffic under them - a panel, raised, its parts in a column; a note card on the base, edged;
 * and the traffic's lines, as the monitors' are.
 */
public record ContentBenchStyles() implements CssGroup<ContentBenchStyles> {

    public static final ContentBenchStyles INSTANCE = new ContentBenchStyles();

    /** The MPA's slot, as the content bench lays it out: a column that scrolls. Unlayered, so it outranks the slot's own column. */
    public record cb_page() implements CssClass<ContentBenchStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Body.class, Type.Face.class), of(Body.class, Color.Ink.class)); }
        @Override public String body() { return """
            flex: 1 1 auto;
            display: flex;
            flex-direction: column;
            gap: 16px;
            width: auto;
            max-width: none;
            min-height: 0;
            margin: 0;
            padding: 12px 24px 24px;
            box-sizing: border-box;
            overflow: auto;
            """;
        }
    }

    /** What the page shows, said. */
    public record cb_intro() implements CssClass<ContentBenchStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Muted.class, Color.Ink.class)); }
        @Override public String body() { return "margin: 0;\nmax-width: 64rem;\n"; }
    }

    /** The two panels, side by side while there is room. */
    public record cb_panels() implements CssClass<ContentBenchStyles> {
        @Override public String body() { return """
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(18rem, 1fr));
            gap: 16px;
            align-items: start;
            """;
        }
    }

    /** A panel: raised, its title, then its parts in a column. */
    public record cb_panel() implements CssClass<ContentBenchStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Raised.class, Color.Surface.class), of(Raised.class, Shape.Corner.class)); }
        @Override public String body() { return """
            display: flex;
            flex-direction: column;
            gap: 12px;
            margin: 0;
            padding: 12px 16px 16px;
            min-width: 0;
            """;
        }
    }

    /** A panel's title. */
    public record cb_panel_title() implements CssClass<ContentBenchStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Label.class, Type.Scale.class), of(Label.class, Type.Weight.class)); }
        @Override public String body() { return "margin: 0;\n"; }
    }

    /** A part that makes parts of its own - a flow: its caption, then its parts, a hairline down its side. */
    public record cb_group() implements CssClass<ContentBenchStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Hairline.class, Color.Edge.class)); }
        @Override public String body() { return """
            display: flex;
            flex-direction: column;
            gap: 8px;
            padding-left: 12px;
            border-left-width: 2px;
            border-left-style: solid;
            """;
        }
    }

    /** A note card: on the base, edged with the hairline. */
    public record cb_card() implements CssClass<ContentBenchStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Base.class, Color.Surface.class), of(Hairline.class, Color.Edge.class)); }
        @Override public String body() { return """
            display: flex;
            flex-direction: column;
            gap: 4px;
            margin: 0;
            padding: 8px 12px;
            border-width: 1px;
            border-style: solid;
            """;
        }
    }

    /** What a card or a line names an item by: its key. */
    public record cb_key() implements CssClass<ContentBenchStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Label.class, Type.Scale.class), of(Muted.class, Color.Ink.class)); }
        @Override public String body() { return "margin: 0;\nfont-family: monospace;\n"; }
    }

    /** A note's title. */
    public record cb_title() implements CssClass<ContentBenchStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Label.class, Type.Weight.class)); }
        @Override public String body() { return "margin: 0;\n"; }
    }

    /** A note's text, or what a card says while it has none. */
    public record cb_text() implements CssClass<ContentBenchStyles> {
        @Override public String body() { return "margin: 0;\n"; }
    }

    /** What a card says while it waits, or when its note is unavailable. */
    public record cb_waiting() implements CssClass<ContentBenchStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Muted.class, Color.Ink.class)); }
        @Override public String body() { return "font-style: italic;\n"; }
    }

    /** The traffic: raised, its summary over its lines. */
    public record cb_traffic() implements CssClass<ContentBenchStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Raised.class, Color.Surface.class), of(Raised.class, Shape.Corner.class)); }
        @Override public String body() { return """
            display: flex;
            flex-direction: column;
            gap: 8px;
            margin: 0;
            padding: 12px 16px;
            min-width: 0;
            """;
        }
    }

    /** Lines of text, as they are: the summary, the log. */
    public record cb_lines() implements CssClass<ContentBenchStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Label.class, Type.Scale.class)); }
        @Override public String body() { return """
            margin: 0;
            font-family: monospace;
            white-space: pre-wrap;
            overflow-wrap: anywhere;
            """;
        }
    }

    /** The log's lines: a box of their own height, scrolling. */
    public record cb_log() implements CssClass<ContentBenchStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Base.class, Color.Surface.class), of(Hairline.class, Color.Edge.class)); }
        @Override public String body() { return """
            max-height: 20rem;
            overflow: auto;
            padding: 8px 10px;
            border-width: 1px;
            border-style: solid;
            """;
        }
    }

    @Override
    public List<CssClass<ContentBenchStyles>> cssClasses() {
        return List.of(new cb_page(), new cb_intro(), new cb_panels(), new cb_panel(), new cb_panel_title(), new cb_group(), new cb_card(), new cb_key(),
                new cb_title(), new cb_text(), new cb_waiting(), new cb_traffic(), new cb_lines(), new cb_log());
    }
}
