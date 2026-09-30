package hue.captains.singapura.js.homing.workspace.bench;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.Wearable;

import java.util.List;

import static hue.captains.singapura.js.homing.design.DesignClass.of;
import static hue.captains.singapura.js.homing.design.Emphasis.Muted;
import static hue.captains.singapura.js.homing.design.Layer.Raised;
import static hue.captains.singapura.js.homing.design.Target.Color;
import static hue.captains.singapura.js.homing.design.Target.Shape;
import static hue.captains.singapura.js.homing.design.Target.Type;
import static hue.captains.singapura.js.homing.design.Text.Body;
import static hue.captains.singapura.js.homing.design.Text.Label;

/**
 * The tree bench's sheet: its page - a bar of the tree's sections, then the box the tree is
 * laid out in - and the stand-in card, a raised note of its params.
 */
public record TreeBenchStyles() implements CssGroup<TreeBenchStyles> {

    public static final TreeBenchStyles INSTANCE = new TreeBenchStyles();

    /**
     * The MPA's slot, as the tree bench lays it out: the whole of what the page leaves it,
     * the bar above the box. Unlayered, so it outranks the column the slot wears in the MPA's
     * layout layer.
     */
    public record tb_page() implements CssClass<TreeBenchStyles> {
        @Override public String body() { return """
            position: relative;
            flex: 1 1 auto;
            display: flex;
            flex-direction: column;
            gap: 12px;
            width: auto;
            max-width: none;
            min-height: 0;
            margin: 0;
            padding: 12px 24px 16px;
            box-sizing: border-box;
            overflow: hidden;
            """;
        }
    }

    /** The tree's sections, as buttons in a row that wraps. */
    public record tb_bar() implements CssClass<TreeBenchStyles> {
        @Override public String body() { return """
            display: flex;
            flex: 0 0 auto;
            flex-wrap: wrap;
            gap: 6px;
            """;
        }
    }

    /** The box the tree is laid out in: the rest of the slot. */
    public record tb_box() implements CssClass<TreeBenchStyles> {
        @Override public String body() { return """
            position: relative;
            flex: 1 1 auto;
            min-height: 0;
            overflow: hidden;
            """;
        }
    }

    /** The stand-in card: raised, its params listed. */
    public record tb_card() implements CssClass<TreeBenchStyles> {
        @Override public List<? extends Wearable> wears() {
            return List.of(of(Raised.class, Color.Surface.class), of(Raised.class, Shape.Corner.class),
                           of(Body.class, Type.Face.class), of(Body.class, Color.Ink.class));
        }
        @Override public String body() { return """
            display: grid;
            grid-template-columns: max-content 1fr;
            gap: 4px 12px;
            margin: 0;
            padding: 10px 14px;
            """;
        }
    }

    /** A param's name. */
    public record tb_key() implements CssClass<TreeBenchStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Label.class, Type.Scale.class), of(Label.class, Type.Weight.class), of(Muted.class, Color.Ink.class)); }
        @Override public String body() { return "margin: 0;\n"; }
    }

    /** A param's value. */
    public record tb_value() implements CssClass<TreeBenchStyles> {
        @Override public String body() { return "margin: 0;\n"; }
    }

    @Override
    public List<CssClass<TreeBenchStyles>> cssClasses() {
        return List.of(new tb_page(), new tb_bar(), new tb_box(), new tb_card(), new tb_key(), new tb_value());
    }
}
