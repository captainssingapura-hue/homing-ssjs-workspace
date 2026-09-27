package hue.captains.singapura.js.homing.workspace.bench;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.Wearable;

import java.util.List;

import static hue.captains.singapura.js.homing.design.Box.Control;
import static hue.captains.singapura.js.homing.design.DesignClass.of;
import static hue.captains.singapura.js.homing.design.Interaction.Current;
import static hue.captains.singapura.js.homing.design.Interaction.Interactive;
import static hue.captains.singapura.js.homing.design.Layer.Raised;
import static hue.captains.singapura.js.homing.design.Target.Affordance;
import static hue.captains.singapura.js.homing.design.Target.Color;
import static hue.captains.singapura.js.homing.design.Target.Motion;
import static hue.captains.singapura.js.homing.design.Target.Shape;
import static hue.captains.singapura.js.homing.design.Target.Type;
import static hue.captains.singapura.js.homing.design.Text.Body;
import static hue.captains.singapura.js.homing.design.Text.Heading;
import static hue.captains.singapura.js.homing.design.Text.Label;

/**
 * The bench's sheet: its page - the monitors' bar, and the container it gives
 * a widget, which can be resized - and how the container looks when a widget
 * misbehaves in it.
 */
public record WidgetBenchStyles() implements CssGroup<WidgetBenchStyles> {

    public static final WidgetBenchStyles INSTANCE = new WidgetBenchStyles();

    /**
     * The MPA's slot, as the bench lays it out: the whole of what the page
     * leaves it, not the reading column's, positioned for the monitors' floats
     * to lie over - the bar, then the container, one under the other.
     * Unlayered, so it outranks the column the slot wears in the MPA's layout
     * layer.
     */
    public record wb_page() implements CssClass<WidgetBenchStyles> {
        @Override public String body() { return """
            position: relative;
            flex: 1 1 auto;
            display: flex;
            flex-direction: column;
            align-items: flex-start;
            gap: 16px;
            width: auto;
            max-width: none;
            min-height: 0;
            margin: 0;
            padding: 16px 24px;
            box-sizing: border-box;
            overflow: hidden;
            """;
        }
    }

    /**
     * The container the bench lends a widget: a box of its own size,
     * positioned for the widget to fill, and resizable by its corner, so a
     * widget is seen to fill whatever it is given.
     */
    public record wb_bench() implements CssClass<WidgetBenchStyles> {
        @Override public String body() { return """
            position: relative;
            flex: 0 0 auto;
            width: 760px;
            height: 440px;
            min-width: 160px;
            min-height: 120px;
            max-width: none;
            margin: 0;
            padding: 0;
            box-sizing: border-box;
            overflow: hidden;
            resize: both;
            border: 1px dashed currentColor;
            """;
        }
    }

    /**
     * The container, while its widget misbehaves - something in it but the
     * widget's one root, a root that does not fill it, anything overflowing it:
     * a heavy line drawn round it, until the widget behaves again. An outline,
     * not a border: marking the container must not change the size of the box
     * the widget fills.
     */
    public record wb_misbehaves() implements CssClass<WidgetBenchStyles> {
        @Override public String body() { return """
            outline: 3px solid currentColor;
            outline-offset: 2px;
            """;
        }
    }

    /** The monitors' bar: their toggles in a row. */
    public record wb_bar() implements CssClass<WidgetBenchStyles> {
        @Override public String body() { return """
            display: flex;
            flex: 0 0 auto;
            align-items: center;
            gap: 6px;
            """;
        }
    }

    /**
     * A monitor's toggle: a square button, the monitor's mark on it - a button
     * as the design draws one, raised in body ink while its monitor is closed.
     */
    public record wb_toggle() implements CssClass<WidgetBenchStyles> {
        @Override public List<? extends Wearable> wears() {
            return List.of(of(Control.Button.Base.class, Shape.Rule.class), of(Control.Button.Base.class, Shape.Corner.class), of(Control.Button.Base.class, Color.Edge.class),
                           of(Raised.class, Color.Surface.class), of(Body.class, Color.Ink.class),
                           of(Label.class, Type.Scale.class), of(Heading.class, Type.Face.class),
                           of(Interactive.class, Affordance.Cursor.class), of(Interactive.class, Motion.Ease.class));
        }
        @Override public String body() { return """
            display: inline-flex;
            align-items: center;
            justify-content: center;
            flex: 0 0 auto;
            width: 32px;
            height: 32px;
            padding: 0;
            box-sizing: border-box;
            """;
        }
    }

    /** A toggle whose monitor is open: the current thing's surface and edge, as a tab's shown chip wears them. */
    public record wb_toggle_on() implements CssClass<WidgetBenchStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Current.class, Color.Surface.class), of(Current.class, Color.Edge.class)); }
        @Override public String body() { return ""; }
    }

    @Override
    public List<CssClass<WidgetBenchStyles>> cssClasses() {
        return List.of(new wb_page(), new wb_bench(), new wb_misbehaves(), new wb_bar(), new wb_toggle(), new wb_toggle_on());
    }
}
