package hue.captains.singapura.js.homing.workspace.bench;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;

import java.util.List;

/** The bench's sheet: the one thing it gives a widget, a container that can be resized. */
public record WidgetBenchStyles() implements CssGroup<WidgetBenchStyles> {

    public static final WidgetBenchStyles INSTANCE = new WidgetBenchStyles();

    /**
     * The MPA's slot, as the bench lends it: a box of its own size, not the
     * reading column's, positioned for the widget to fill, and resizable by its
     * corner, so a widget is seen to fill whatever it is given. Unlayered, so
     * it outranks the column the slot wears in the MPA's layout layer.
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
            margin: 24px auto;
            padding: 0;
            box-sizing: border-box;
            overflow: hidden;
            resize: both;
            border: 1px dashed currentColor;
            """;
        }
    }

    @Override
    public List<CssClass<WidgetBenchStyles>> cssClasses() { return List.of(new wb_bench()); }
}
