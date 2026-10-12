package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;

import java.util.List;


/** The workspace's floor: the one box it draws, as the gallery's docking page draws its own; what its control strip says; and the fake widgets' structure while the tabs are built. */
public record WorkspaceStyles() implements CssGroup<WorkspaceStyles> {

    public static final WorkspaceStyles INSTANCE = new WorkspaceStyles();

    /**
     * The floor: fills what holds it, and is where the desk's floats lie and the grid's regions sit. NO FRAME
     * OF ITS OWN, as the docking page's box has none: the grid draws its own outer line in the width and the
     * colour of the lines between its rooms, and a frame here would be that line drawn twice.
     */
    public record ws_floor() implements CssClass<WorkspaceStyles> {
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

    /** What the workspace is called, said on its strip when it is not offered as the way to another. */
    public record ws_logbar_name() implements CssClass<WorkspaceStyles> {
        @Override public String body() { return ""; }
    }

    /** What the log holds, said: small and quiet, taking the room the controls leave and giving it up before they do. */
    public record ws_logbar_count() implements CssClass<WorkspaceStyles> {
        @Override public String body() { return """
            flex: 1 1 auto;
            min-width: 0;
            overflow: hidden;
            text-overflow: ellipsis;
            """;
        }
    }

    /** What else the strip says - who writes the workspace, a log set aside, how the address was read - in the body's ink, small. */
    public record ws_logbar_note() implements CssClass<WorkspaceStyles> {
        @Override public String body() { return """
            min-width: 0;
            overflow: hidden;
            text-overflow: ellipsis;
            """;
        }
    }

    /** The way to a new workspace of this kind: a link, as the design draws one. */
    public record ws_logbar_fresh() implements CssClass<WorkspaceStyles> {
        @Override public String body() { return ""; }
    }

    /** The link the file is handed over by: never shown. */
    public record ws_logbar_link() implements CssClass<WorkspaceStyles> {
        @Override public String body() { return "display: none;"; }
    }

    /** A fake widget: its parts down the room, with air around them. */
    public record ws_fake() implements CssClass<WorkspaceStyles> {
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
        @Override public String body() { return "min-width: 3ch;"; }
    }

    /** A row of controls. */
    public record ws_fake_row() implements CssClass<WorkspaceStyles> {
        @Override public String body() { return "display: flex; gap: 8px;"; }
    }

    @Override
    public List<CssClass<WorkspaceStyles>> cssClasses() { return List.of(new ws_floor(), new ws_logbar_name(), new ws_logbar_count(), new ws_logbar_note(), new ws_logbar_fresh(), new ws_logbar_link(), new ws_fake(), new ws_fake_count(), new ws_fake_row()); }
}
