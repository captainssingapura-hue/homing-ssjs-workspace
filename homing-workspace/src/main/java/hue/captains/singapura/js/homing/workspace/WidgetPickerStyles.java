package hue.captains.singapura.js.homing.workspace;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.CssVar;
import hue.captains.singapura.js.homing.core.Wearable;

import java.util.Set;
import java.util.List;

import static hue.captains.singapura.js.homing.design.DesignClass.of;
import static hue.captains.singapura.js.homing.design.Target.*;
import static hue.captains.singapura.js.homing.design.Box.*;
import static hue.captains.singapura.js.homing.design.Emphasis.*;
import static hue.captains.singapura.js.homing.design.Interaction.*;
import static hue.captains.singapura.js.homing.design.Layer.*;
import static hue.captains.singapura.js.homing.design.Pairing.*;
import static hue.captains.singapura.js.homing.design.Text.*;


/**
 * The widget picker: a grid of tiles, then a params form.
 *
 * <p>A tile wears {@link Box.Control#Tile} — one BOX of a grid, picked, where
 * an Option is one row of a list. The word carries the box: its extent and
 * proportion make every tile in the grid the same size however long the label,
 * its inset the air inside, its gap between the mark and the label, its corner
 * and rule the face it presents. Structure only here; the measures are each
 * design's.</p>
 *
 * <p>The cursor is a design STATE, not a colour this file picks: a tile reads
 * {@code data-cursor} and answers on its own word, as a pane answers
 * {@code data-keys}. Keyboard and pointer land on the same mark.</p>
 */
public record WidgetPickerStyles() implements CssGroup<WidgetPickerStyles> {
    public static final WidgetPickerStyles INSTANCE = new WidgetPickerStyles();

    /**
     * The grid the tiles sit in. Its columns come from the TILE'S OWN measure,
     * so the design decides how many fit and the grid only says they wrap: a
     * reader of this file cannot set a width the design disagrees with.
     */
    public record hwp_grid() implements CssClass<WidgetPickerStyles> {
        @Override public Set<CssVar> runtimeVars() { return Set.of(new CssVar("--control-tile-size-extent")); }
        @Override public List<? extends Wearable> reads() { return List.of(of(Control.Tile.class, Size.Extent.class), of(Control.Tile.class, Size.Gap.class)); }
        @Override public String body() { return """
                display: grid;
                grid-template-columns: repeat(auto-fill, minmax(var(--control-tile-size-extent, 128px), 1fr));
                gap: var(--control-tile-size-gap, 8px);
                padding: var(--control-tile-size-gap, 8px);
                align-content: start;
                """; }
    }
    public record hwp_group_label() implements CssClass<WidgetPickerStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Kicker.class, Type.Scale.class), of(Kicker.class, Type.Weight.class), of(Kicker.class, Type.Treatment.class), of(Muted.class, Color.Ink.class), of(Body.class, Type.Face.class)); }
        @Override public String body() { return """
                grid-column: 1 / -1;
                padding: 6px 2px 2px;
                """; }
    }
    /** One box of the grid: a mark over a label, on the tile's own word. */
    public record hwp_tile() implements CssClass<WidgetPickerStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(
                of(Control.Tile.class, Shape.Corner.class), of(Control.Tile.class, Shape.Rule.class),
                of(Control.Tile.class, Size.Inset.class), of(Control.Tile.class, Size.Gap.class),
                of(Control.Tile.class, Size.Proportion.class),
                of(Base.class, Color.Surface.class), of(Control.class, Color.Edge.class),
                of(Selectable.class, Color.Surface.class), of(Selectable.class, Color.Ink.class),
                of(Selectable.class, Motion.Ease.class), of(Selectable.class, Affordance.Cursor.class)); }
        @Override public String body() { return """
                display: flex;
                flex-direction: column;
                align-items: center;
                justify-content: center;
                text-align: center;
                min-width: 0;
                overflow: hidden;
                """; }
    }

    /**
     * The tile the keys are on. The SAME mark the pointer gets, because a
     * reader should not have to learn two ways of being shown where they are;
     * geometry is the design's and colour is the palette's, so this says only
     * that the state exists and the word answers it.
     */
    public record hwp_tile_cursor() implements CssClass<WidgetPickerStyles> {
        @Override public String selector() { return "&[data-cursor=\"on\"]"; }
        @Override public List<? extends Wearable> wears() { return List.of(
                of(Selected.class, Color.Surface.class), of(Selected.class, Color.Ink.class),
                of(Focus.class, Color.Edge.class), of(Focus.class, Shape.Shadow.class)); }
        @Override public String body() { return ""; }
    }
    public record hwp_tile_disabled() implements CssClass<WidgetPickerStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Inert.class, Effect.Opacity.class), of(Inert.class, Affordance.Cursor.class)); }
        @Override public String body() { return ""; }
    }
    public record hwp_tile_icon() implements CssClass<WidgetPickerStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Numeral.class, Type.Scale.class)); }
        @Override public String body() { return ""; }
    }
    public record hwp_tile_label() implements CssClass<WidgetPickerStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Body.class, Type.Face.class), of(Caption.class, Type.Scale.class)); }
        @Override public String body() { return """
                text-align: center;
                """; }
    }
    public record hwp_tile_desc() implements CssClass<WidgetPickerStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Body.class, Type.Face.class), of(Kicker.class, Type.Scale.class), of(Muted.class, Color.Ink.class)); }
        @Override public String body() { return """
                text-align: center;
                """; }
    }
    public record hwp_form() implements CssClass<WidgetPickerStyles> {
        @Override public String body() { return """
                padding: 12px;
                display: flex;
                flex-direction: column;
                gap: 8px;
                """; }
    }
    public record hwp_form_row() implements CssClass<WidgetPickerStyles> {
        @Override public String body() { return """
                display: flex;
                flex-direction: column;
                gap: 3px;
                """; }
    }
    public record hwp_form_label() implements CssClass<WidgetPickerStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Body.class, Type.Face.class), of(Caption.class, Type.Scale.class), of(Muted.class, Color.Ink.class)); }
        @Override public String body() { return ""; }
    }
    public record hwp_form_input() implements CssClass<WidgetPickerStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Base.class, Color.Surface.class), of(Raised.class, Color.Edge.class), of(Raised.class, Shape.Rule.class), of(Control.class, Shape.Corner.class), of(Body.class, Color.Ink.class), of(Body.class, Type.Face.class), of(Caption.class, Type.Scale.class)); }
        @Override public String body() { return """
                padding: 5px 8px;
                """; }
    }
    public record hwp_form_actions() implements CssClass<WidgetPickerStyles> {
        @Override public String body() { return """
                display: flex;
                justify-content: flex-end;
                gap: 6px;
                margin-top: 4px;
                """; }
    }
    public record hwp_form_btn() implements CssClass<WidgetPickerStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Base.class, Color.Surface.class), of(Raised.class, Color.Edge.class), of(Raised.class, Shape.Rule.class), of(Control.class, Shape.Corner.class), of(Body.class, Color.Ink.class), of(Body.class, Type.Face.class), of(Caption.class, Type.Scale.class), of(Interactive.class, Affordance.Cursor.class)); }
        @Override public String body() { return """
                padding: 5px 12px;
                """; }
    }
    /** The primary action: applied beside hwp_form_btn; its surface, ink and edge win by order. */
    public record hwp_form_btn_primary() implements CssClass<WidgetPickerStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Primary.class, Color.Surface.class), of(Primary.class, Color.Edge.class), of(OnPrimary.class, Color.Ink.class)); }
        @Override public String body() { return ""; }
    }

    @Override
    public List<CssClass<WidgetPickerStyles>> cssClasses() {
        return List.of(
                new hwp_grid(),
                new hwp_group_label(),
                new hwp_tile(),
                new hwp_tile_cursor(),
                new hwp_tile_disabled(),
                new hwp_tile_icon(),
                new hwp_tile_label(),
                new hwp_tile_desc(),
                new hwp_form(),
                new hwp_form_row(),
                new hwp_form_label(),
                new hwp_form_input(),
                new hwp_form_actions(),
                new hwp_form_btn(),
                new hwp_form_btn_primary()
        );
    }
}
