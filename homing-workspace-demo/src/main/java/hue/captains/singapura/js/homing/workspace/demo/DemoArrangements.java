package hue.captains.singapura.js.homing.workspace.demo;

import hue.captains.singapura.js.homing.workspace.groups.core.models.ArrangedWidget;
import hue.captains.singapura.js.homing.workspace.groups.core.models.Arrangement;
import hue.captains.singapura.js.homing.workspace.groups.core.models.SplitGrid;
import hue.captains.singapura.js.homing.workspace.groups.core.models.SplitGrid.Part;
import hue.captains.singapura.js.homing.workspace.groups.core.models.WorkspaceArrangements;

import static hue.captains.singapura.js.homing.workspace.groups.core.models.SplitGrid.column;
import static hue.captains.singapura.js.homing.workspace.groups.core.models.SplitGrid.region;
import static hue.captains.singapura.js.homing.workspace.groups.core.models.SplitGrid.row;

/**
 * How the demo's workspaces are arranged the first time, in the split grid -
 * the launcher's decision, as the declarations and the groups are: each with
 * the switcher on the left, to go elsewhere, and its own widgets beside it.
 */
public final class DemoArrangements {

    private DemoArrangements() {}

    /** The monitors, two regions of two: the trees above, the party's log and the steward's lamp below. */
    private static SplitGrid.Split monitors() {
        return column(region("trees", "focus-tree", "domops-tree"), region("parties", "party-log", "steward-lamp"));
    }

    private static final ArrangedWidget SWITCHER = ArrangedWidget.of("switcher", "workspace-switcher");
    private static final ArrangedWidget[] MONITORS = {
            ArrangedWidget.of("focus-tree", "focus-tree"), ArrangedWidget.of("domops-tree", "domops-tree"),
            ArrangedWidget.of("party-log", "party-log"), ArrangedWidget.of("steward-lamp", "steward-lamp")};

    /** Together: the switcher, the books and the chosen one composed - twice as wide - and the monitors. */
    public static final WorkspaceArrangements<DemoWorkspace> TOGETHER = WorkspaceArrangements.of(DemoWorkspace.INSTANCE,
            Arrangement.of(DemoWorkspace.INSTANCE,
                    SplitGrid.of(row(Part.of(region("kinds", "switcher"), 1), Part.of(region("books", "browser"), 2), Part.of(monitors(), 1))),
                    SWITCHER, ArrangedWidget.of("browser", "book-browser"),
                    MONITORS[0], MONITORS[1], MONITORS[2], MONITORS[3]));

    /** The books: the switcher, and the grid above the chosen book, meeting in the workspace's book selection. */
    public static final WorkspaceArrangements<BooksWorkspace> BOOKS = WorkspaceArrangements.of(BooksWorkspace.INSTANCE,
            Arrangement.of(BooksWorkspace.INSTANCE,
                    SplitGrid.of(row(Part.of(region("kinds", "switcher"), 1), Part.of(column(region("books", "grid"), region("chosen", "jumbotron")), 2))),
                    SWITCHER, ArrangedWidget.of("grid", "books-grid"), ArrangedWidget.of("jumbotron", "book-jumbotron")));

    /** The monitors: the switcher, and the monitors twice as wide. */
    public static final WorkspaceArrangements<MonitorsWorkspace> MONITORING = WorkspaceArrangements.of(MonitorsWorkspace.INSTANCE,
            Arrangement.of(MonitorsWorkspace.INSTANCE,
                    SplitGrid.of(row(Part.of(region("kinds", "switcher"), 1), Part.of(monitors(), 2))),
                    SWITCHER, MONITORS[0], MONITORS[1], MONITORS[2], MONITORS[3]));
}
