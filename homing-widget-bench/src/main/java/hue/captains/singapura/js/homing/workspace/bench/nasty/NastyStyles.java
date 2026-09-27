package hue.captains.singapura.js.homing.workspace.bench.nasty;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;

import java.util.List;

/** The nasty widgets' sheet: how each one breaks the container's rule. */
public record NastyStyles() implements CssGroup<NastyStyles> {

    public static final NastyStyles INSTANCE = new NastyStyles();

    /** A root of its own fixed size: not the container's, and not following it. */
    public record nasty_fixed() implements CssClass<NastyStyles> {
        @Override public String body() { return """
            width: 640px;
            height: 360px;
            padding: 16px;
            box-sizing: border-box;
            border: 2px solid currentColor;
            """;
        }
    }

    @Override
    public List<CssClass<NastyStyles>> cssClasses() { return List.of(new nasty_fixed()); }
}
