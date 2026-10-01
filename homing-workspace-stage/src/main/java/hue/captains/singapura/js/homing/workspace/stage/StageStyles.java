package hue.captains.singapura.js.homing.workspace.stage;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.Wearable;

import java.util.List;

import static hue.captains.singapura.js.homing.design.Box.Container;
import static hue.captains.singapura.js.homing.design.DesignClass.of;
import static hue.captains.singapura.js.homing.design.Emphasis.Primary;
import static hue.captains.singapura.js.homing.design.Layer.Base;
import static hue.captains.singapura.js.homing.design.Layer.Inverted;
import static hue.captains.singapura.js.homing.design.Layer.Overlay;
import static hue.captains.singapura.js.homing.design.Layer.Raised;
import static hue.captains.singapura.js.homing.design.Layer.Recessed;
import static hue.captains.singapura.js.homing.design.Structure.Hairline;
import static hue.captains.singapura.js.homing.design.Target.Color;
import static hue.captains.singapura.js.homing.design.Target.Effect;
import static hue.captains.singapura.js.homing.design.Target.Shape;
import static hue.captains.singapura.js.homing.design.Target.Type;
import static hue.captains.singapura.js.homing.design.Text.Body;
import static hue.captains.singapura.js.homing.design.Text.Heading;

/**
 * The stage's sheet, in the design's words and nothing of its own but layout: one layer over the
 * page; the scrim behind, as a modal dialog's; the frame a floating pane's, raised, filling the
 * view but for a margin; its head - the title, the close - over a hairline; and the seat, a box
 * of a size the stage gives, which what sits in it fills.
 */
public record StageStyles() implements CssGroup<StageStyles> {

    public static final StageStyles INSTANCE = new StageStyles();

    /** The layer: over everything, the whole view. */
    public record sg_layer() implements CssClass<StageStyles> {
        @Override public String body() { return "position: fixed;\ninset: 0;\nz-index: 10020;\ndisplay: flex;\npadding: 24px;\nbox-sizing: border-box;\n"; }
    }

    /** Down: not on the page to see. */
    public record sg_hidden() implements CssClass<StageStyles> {
        @Override public String body() { return "display: none;\n"; }
    }

    /** The scrim: the page behind, put back. */
    public record sg_scrim() implements CssClass<StageStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Overlay.class, Effect.Filter.class)); }
        @Override public String body() { return "position: absolute;\ninset: 0;\n"; }
    }

    /** The frame: a floating pane's corner and edge, raised over the scrim; a column of the head and the seat. */
    public record sg_frame() implements CssClass<StageStyles> {
        @Override public List<? extends Wearable> wears() {
            return List.of(of(Container.Pane.Floating.class, Shape.Corner.class), of(Container.Pane.Floating.class, Shape.Rule.class),
                    of(Container.Pane.Floating.class, Color.Edge.class), of(Raised.class, Color.Surface.class), of(Raised.class, Color.Edge.class),
                    of(Body.class, Color.Ink.class), of(Body.class, Type.Face.class), of(Overlay.class, Shape.Shadow.class));
        }
        /** What an overlay's shadow is cast from, design by design - the inverted, recessed, primary or base surface: read by reference, so bound wherever the frame is worn. */
        @Override public List<? extends Wearable> reads() {
            return List.of(of(Inverted.class, Color.Surface.class), of(Recessed.class, Color.Surface.class), of(Primary.class, Color.Surface.class), of(Base.class, Color.Surface.class));
        }
        @Override public String body() {
            return "position: relative;\nflex: 1 1 auto;\ndisplay: flex;\nflex-direction: column;\nmin-width: 0;\nmin-height: 0;\noverflow: hidden;\n";
        }
    }

    /** The head: the title, then the close, over a hairline. */
    public record sg_head() implements CssClass<StageStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Hairline.class, Color.Edge.class)); }
        @Override public String body() {
            return "flex: none;\ndisplay: flex;\nalign-items: center;\ngap: 8px;\npadding: 8px 12px;\nborder-bottom-width: 1px;\nborder-bottom-style: solid;\n";
        }
    }

    /** The title: what is on the stage, on one line. */
    public record sg_title() implements CssClass<StageStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Heading.class, Type.Face.class), of(Heading.class, Type.Weight.class)); }
        @Override public String body() { return "flex: 1 1 auto;\nmin-width: 0;\nmargin: 0;\nfont-size: inherit;\noverflow: hidden;\ntext-overflow: ellipsis;\nwhite-space: nowrap;\n"; }
    }

    /** The seat: the rest of the frame, a column whose height the stage gives - what sits in it fills it. */
    public record sg_seat() implements CssClass<StageStyles> {
        @Override public String body() {
            return "flex: 1 1 auto;\nmin-height: 0;\ndisplay: flex;\nflex-direction: column;\noverflow: auto;\npadding: 16px;\n";
        }
    }

    @Override
    public List<CssClass<StageStyles>> cssClasses() {
        return List.of(new sg_layer(), new sg_hidden(), new sg_scrim(), new sg_frame(), new sg_head(), new sg_title(), new sg_seat());
    }
}
