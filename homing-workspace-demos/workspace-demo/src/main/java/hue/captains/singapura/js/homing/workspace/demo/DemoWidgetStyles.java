package hue.captains.singapura.js.homing.workspace.demo;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.CssVar;
import hue.captains.singapura.js.homing.core.Wearable;

import java.util.List;
import java.util.Set;

import static hue.captains.singapura.js.homing.design.Box.Container;
import static hue.captains.singapura.js.homing.design.DesignClass.of;
import static hue.captains.singapura.js.homing.design.Layer.Inverted;
import static hue.captains.singapura.js.homing.design.Layer.Raised;
import static hue.captains.singapura.js.homing.design.Layer.Recessed;
import static hue.captains.singapura.js.homing.design.Emphasis.*;
import static hue.captains.singapura.js.homing.design.Target.*;
import static hue.captains.singapura.js.homing.design.Text.*;

/**
 * The demo widgets' structure. The words they wear are the design's; nothing
 * here picks a colour or a face.
 */
public record DemoWidgetStyles() implements CssGroup<DemoWidgetStyles> {
    public static final DemoWidgetStyles INSTANCE = new DemoWidgetStyles();

    /** A note: a paragraph of body text, set in the room with air around it. */
    public record dw_note() implements CssClass<DemoWidgetStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Body.class, Color.Ink.class), of(Body.class, Type.Face.class)); }
        @Override public String body() { return """
            padding: 20px 24px;
            max-width: 60ch;
            line-height: 1.55;
            """; }
    }

    /** A counter: its number over its buttons, over a line saying how it takes keys. */
    public record dw_counter() implements CssClass<DemoWidgetStyles> {
        @Override public String body() { return """
            display: flex;
            flex-direction: column;
            align-items: flex-start;
            gap: 14px;
            padding: 20px 24px;
            """; }
    }

    public record dw_count() implements CssClass<DemoWidgetStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Numeral.class, Type.Scale.class), of(Body.class, Color.Ink.class)); }
        @Override public String body() { return "min-width: 3ch;"; }
    }

    public record dw_row() implements CssClass<DemoWidgetStyles> {
        @Override public String body() { return """
            display: flex;
            gap: 8px;
            """; }
    }

    public record dw_hint() implements CssClass<DemoWidgetStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Muted.class, Color.Ink.class), of(Caption.class, Type.Scale.class)); }
        @Override public String body() { return "max-width: 48ch;"; }
    }

    // ── The views: a widget that takes the whole room ─────────────────────

    /** A view filling the room it runs in: the room is a column, and the view takes all of it. */
    public record dw_fill() implements CssClass<DemoWidgetStyles> {
        @Override public String body() { return """
            flex: 1 1 auto;
            display: flex;
            flex-direction: column;
            min-width: 0;
            min-height: 0;
            box-sizing: border-box;
            """; }
    }

    /** The box a relation component is mounted in: what the view leaves, and it scrolls on its own. */
    public record dw_host() implements CssClass<DemoWidgetStyles> {
        @Override public String body() { return """
            flex: 1 1 auto;
            min-width: 0;
            min-height: 0;
            overflow: auto;
            """; }
    }

    /** The picture's box: the plate centred in what the view leaves, and nothing spilling out of it. */
    public record dw_picture() implements CssClass<DemoWidgetStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Recessed.class, Color.Surface.class)); }
        @Override public String body() { return """
            flex: 1 1 auto;
            min-width: 0;
            min-height: 0;
            display: flex;
            align-items: center;
            justify-content: center;
            overflow: hidden;
            """; }
    }

    /**
     * The picture: a plate drawn with the design's own words — an inverted sky,
     * a sun in the primary surface, two hills and the ground — so it is themed
     * like everything else and carries no colour of its own. The zoom the keys
     * set scales it from the middle.
     */
    public record dw_plate() implements CssClass<DemoWidgetStyles> {
        @Override public Set<CssVar> runtimeVars() { return Set.of(new CssVar("--dw-zoom")); }
        @Override public List<? extends Wearable> wears() { return List.of(of(Inverted.class, Color.Surface.class), of(Container.class, Shape.Corner.class)); }
        @Override public String body() { return """
            position: relative;
            flex: none;
            inline-size: min(100%, 420px);
            aspect-ratio: 8 / 5;
            overflow: hidden;
            transform: scale(var(--dw-zoom, 1));
            transform-origin: center;
            """; }
    }

    /** The sun: a disc in the primary surface, high and to the end. */
    public record dw_plate_sun() implements CssClass<DemoWidgetStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Primary.class, Color.Surface.class)); }
        @Override public String body() { return """
            position: absolute;
            inset-inline-end: 18%;
            inset-block-start: 12%;
            inline-size: 15%;
            aspect-ratio: 1;
            border-radius: 50%;
            """; }
    }

    /** The far hill: recessed, behind the near one, so the near one reads over it. */
    public record dw_plate_far() implements CssClass<DemoWidgetStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Recessed.class, Color.Surface.class)); }
        @Override public String body() { return """
            position: absolute;
            inset-block-end: 24%;
            inset-inline-end: 6%;
            inline-size: 58%;
            block-size: 52%;
            clip-path: polygon(50% 0, 100% 100%, 0 100%);
            """; }
    }

    /** The near hill: the secondary surface, lower and toward the start, so the two overlap. */
    public record dw_plate_near() implements CssClass<DemoWidgetStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Secondary.class, Color.Surface.class)); }
        @Override public String body() { return """
            position: absolute;
            inset-block-end: 24%;
            inset-inline-start: 2%;
            inline-size: 46%;
            block-size: 38%;
            clip-path: polygon(50% 0, 100% 100%, 0 100%);
            """; }
    }

    /** The ground: a raised band across the plate, the light the hills stand on. */
    public record dw_plate_ground() implements CssClass<DemoWidgetStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Raised.class, Color.Surface.class)); }
        @Override public String body() { return """
            position: absolute;
            inset-inline: 0;
            inset-block-end: 0;
            block-size: 24%;
            """; }
    }

    /** The line under the picture: what it is, and how far it is zoomed. */
    public record dw_picture_note() implements CssClass<DemoWidgetStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Caption.class, Type.Scale.class), of(Muted.class, Color.Ink.class), of(Body.class, Type.Face.class)); }
        @Override public String body() { return """
            flex: none;
            margin: 0;
            padding: 6px 10px;
            """; }
    }

    @Override
    public List<CssClass<DemoWidgetStyles>> cssClasses() {
        return List.of(new dw_note(), new dw_counter(), new dw_count(), new dw_row(), new dw_hint(),
                new dw_fill(), new dw_host(), new dw_picture(), new dw_plate(), new dw_plate_sun(),
                new dw_plate_far(), new dw_plate_near(), new dw_plate_ground(), new dw_picture_note());
    }
}
