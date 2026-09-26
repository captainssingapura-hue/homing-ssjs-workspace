package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.Wearable;

import java.util.List;

import static hue.captains.singapura.js.homing.design.DesignClass.of;
import static hue.captains.singapura.js.homing.design.Text.Body;
import static hue.captains.singapura.js.homing.design.Text.Numeral;
import static hue.captains.singapura.js.homing.design.Target.Type;
import static hue.captains.singapura.js.homing.design.Layer.Recessed;
import static hue.captains.singapura.js.homing.design.Target.Color;

/** The workspace's floor: the one box it draws, as the gallery's docking page draws its own; and the fake widgets' structure while the tabs are built. */
public record WorkspaceStyles() implements CssGroup<WorkspaceStyles> {

    public static final WorkspaceStyles INSTANCE = new WorkspaceStyles();

    /**
     * The floor: fills what holds it, and is where the desk's floats lie and the grid's regions sit. NO FRAME
     * OF ITS OWN, as the docking page's box has none: the grid draws its own outer line in the width and the
     * colour of the lines between its rooms, and a frame here would be that line drawn twice.
     */
    public record ws_floor() implements CssClass<WorkspaceStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Recessed.class, Color.Surface.class)); }
        @Override public String body() { return """
            position: relative;
            flex: 1 1 auto;
            min-width: 0;
            min-height: 0;
            display: flex;
            flex-direction: column;
            overflow: hidden;
            """;
        }
    }

    /** A fake widget: its parts down the room, with air around them. */
    public record ws_fake() implements CssClass<WorkspaceStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Body.class, Color.Ink.class), of(Body.class, Type.Face.class)); }
        @Override public String body() { return """
            display: flex;
            flex-direction: column;
            align-items: flex-start;
            gap: 12px;
            padding: 16px 20px;
            max-width: 60ch;
            """;
        }
    }

    /** The fake counter's number. */
    public record ws_fake_count() implements CssClass<WorkspaceStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Numeral.class, Type.Scale.class)); }
        @Override public String body() { return "min-width: 3ch;"; }
    }

    /** A row of controls. */
    public record ws_fake_row() implements CssClass<WorkspaceStyles> {
        @Override public String body() { return "display: flex; gap: 8px;"; }
    }

    @Override
    public List<CssClass<WorkspaceStyles>> cssClasses() { return List.of(new ws_floor(), new ws_fake(), new ws_fake_count(), new ws_fake_row()); }
}
