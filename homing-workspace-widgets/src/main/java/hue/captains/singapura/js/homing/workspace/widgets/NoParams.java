package hue.captains.singapura.js.homing.workspace.widgets;

import java.util.List;
import java.util.Map;

/** The params of a kind that has none: the widget is the same whatever the address says. */
public record NoParams() implements WidgetParams {

    public static final NoParams INSTANCE = new NoParams();

    /** Reads nothing, and writes nothing. */
    public record Query() implements WidgetQuery<NoParams> {
        @Override public Read<NoParams> from(Map<String, List<String>> query) { return Read.ok(INSTANCE); }
        @Override public Map<String, List<String>> to(NoParams params) { return Map.of(); }
    }
}
