package hue.captains.singapura.js.homing.workspace.demo;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.Wearable;

import java.util.List;

import static hue.captains.singapura.js.homing.design.DesignClass.of;
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

    @Override
    public List<CssClass<DemoWidgetStyles>> cssClasses() {
        return List.of(new dw_note(), new dw_counter(), new dw_count(), new dw_row(), new dw_hint());
    }
}
