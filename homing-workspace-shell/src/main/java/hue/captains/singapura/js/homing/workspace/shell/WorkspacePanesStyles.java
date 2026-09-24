package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;

import java.util.List;

/**
 * Where a widget lives inside a tab: the room ({@link WidgetPaneModule}).
 *
 * <p>Structure only: a box that fills whatever holds it — a dock's panel, or a
 * floating pane's body on the desk — lays its widget out down the page, and
 * scrolls a widget taller than the room rather than letting it spill over the
 * pane beside it. How the widget looks inside is the widget's.</p>
 */
public record WorkspacePanesStyles() implements CssGroup<WorkspacePanesStyles> {

    public static final WorkspacePanesStyles INSTANCE = new WorkspacePanesStyles();

    /** A tab's room: fills what holds it, lays the widget down the page, scrolls what does not fit. */
    public record wp_host() implements CssClass<WorkspacePanesStyles> {
        @Override public String body() { return """
            display: flex;
            flex-direction: column;
            flex: 1 1 auto;
            min-width: 0;
            min-height: 0;
            height: 100%;
            overflow: auto;
            """; }
    }

    @Override
    public List<CssClass<WorkspacePanesStyles>> cssClasses() {
        return List.of(new wp_host());
    }
}
