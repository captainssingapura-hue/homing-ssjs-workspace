package hue.captains.singapura.js.homing.workspace.widgets;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;

import java.util.List;

/** The widgets' own sheet: how a widget fills the container it is lent. */
public record WidgetStyles() implements CssGroup<WidgetStyles> {

    public static final WidgetStyles INSTANCE = new WidgetStyles();

    /**
     * A widget's root: the whole of the container it is lent, whatever size the
     * host makes it. The container is the host's; a host that lends one
     * positions it, so that the root has something to fill.
     */
    public record wg_fill() implements CssClass<WidgetStyles> {
        @Override public String body() { return """
            position: absolute;
            inset: 0;
            display: flex;
            flex-direction: column;
            min-width: 0;
            min-height: 0;
            box-sizing: border-box;
            """;
        }
    }

    /** What scrolls inside a widget: the rest of its root, its own scroller. */
    public record wg_scroll() implements CssClass<WidgetStyles> {
        @Override public String body() { return """
            flex: 1 1 auto;
            min-width: 0;
            min-height: 0;
            overflow: auto;
            """;
        }
    }

    @Override
    public List<CssClass<WidgetStyles>> cssClasses() { return List.of(new wg_fill(), new wg_scroll()); }
}
