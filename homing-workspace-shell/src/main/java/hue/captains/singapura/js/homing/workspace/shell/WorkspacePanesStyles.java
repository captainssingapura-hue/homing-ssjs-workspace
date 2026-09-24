package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;

import java.util.List;

/**
 * Where a widget lives inside a tab.
 *
 * <p>The two panes hand the room over differently, and this is the one seam.
 * The studio's gave the caller a bare content element and let it mount into
 * that afterwards; the component takes a WIDGET at {@code addTab} and puts
 * {@code widget.root} in a panel of its own. The shell still works the first
 * way — add the tab, ask where it went, mount — so the assembly gives each tab
 * a host of its own to be that root, and hands the same element back when the
 * shell asks.</p>
 *
 * <p>Structure only, and only the structure that seam needs: a box that fills
 * the panel it is in and lays its widget out down the page, which is what the
 * studio's content element did with an inline style.</p>
 */
public record WorkspacePanesStyles() implements CssGroup<WorkspacePanesStyles> {

    public static final WorkspacePanesStyles INSTANCE = new WorkspacePanesStyles();

    /** A tab's room: fills the panel, lays the widget down the page. */
    public record wp_host() implements CssClass<WorkspacePanesStyles> {
        @Override public String body() { return """
            display: flex;
            flex-direction: column;
            flex: 1 1 auto;
            min-width: 0;
            min-height: 0;
            height: 100%;
            """; }
    }

    @Override
    public List<CssClass<WorkspacePanesStyles>> cssClasses() {
        return List.of(new wp_host());
    }
}
