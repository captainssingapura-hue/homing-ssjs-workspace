package hue.captains.singapura.js.homing.workspace.demo;

import hue.captains.singapura.js.homing.workspace.WidgetEntry;
import hue.captains.singapura.js.homing.workspace.WidgetGroup;
import hue.captains.singapura.js.homing.workspace.WidgetIcon;
import hue.captains.singapura.js.homing.workspace.WidgetLabel;
import hue.captains.singapura.js.homing.workspace.shell.WorkspaceSpec;
import hue.captains.singapura.js.homing.workspace.shell.WorkspaceSpecRegistry;

import java.util.List;

/**
 * The demo's workspaces, built from the demo's own widgets only.
 *
 * <p>Both widgets are written to the new contract — handed a branch, they give
 * back {@code { root }} and touch nothing else — so everything this demo shows
 * about panes, splitting, floating and docking is shown on widgets that know
 * nothing about any of it. The workspace's older widgets are not here on
 * purpose: they run through an adapter that comes later.</p>
 *
 * <p>The DEMO kind offers every widget the demo has: the note and the counter,
 * and three views — the books as a relation grid, the same books as shelves
 * in a relation tree (one store, so the two agree live), and a picture.</p>
 */
public final class WorkspaceDemoSpecs {

    private WorkspaceDemoSpecs() {}

    /** Every demo widget: a note, a counter, the books as a grid and as shelves, a picture. */
    public static final WorkspaceSpec DEMO = new Demo();

    /** Notes only. */
    public static final WorkspaceSpec NOTES = new Notes();

    private static boolean registered;

    /**
     * Register both, once. The registry is process-wide and populated at boot,
     * so a site loaded twice in one JVM — a test and then a server — must not
     * register twice: the second is a duplicate-kind error, correctly.
     */
    public static synchronized void register() {
        if (registered) return;
        registered = true;
        WorkspaceSpecRegistry.INSTANCE.register(DEMO);
        WorkspaceSpecRegistry.INSTANCE.register(NOTES);
    }

    private static final WidgetGroup DEMO_GROUP = WidgetGroup.of("Demo");

    private static WidgetEntry note() {
        return WidgetEntry.of(DemoNoteWidget.class, WidgetLabel.of("Note"))
                .withIcon(new WidgetIcon.Emoji("📝"))
                .withGroup(DEMO_GROUP);
    }

    private static final WidgetGroup VIEWS_GROUP = WidgetGroup.of("Views");

    private static WidgetEntry books() {
        return WidgetEntry.of(DemoBooksWidget.class, WidgetLabel.of("Books"))
                .withIcon(new WidgetIcon.Emoji("📚"))
                .withGroup(VIEWS_GROUP);
    }

    private static WidgetEntry shelves() {
        return WidgetEntry.of(DemoShelvesWidget.class, WidgetLabel.of("Shelves"))
                .withIcon(new WidgetIcon.Emoji("🗂"))
                .withGroup(VIEWS_GROUP);
    }

    private static WidgetEntry picture() {
        return WidgetEntry.of(DemoPictureWidget.class, WidgetLabel.of("Picture"))
                .withIcon(new WidgetIcon.Emoji("🖼"))
                .withGroup(VIEWS_GROUP);
    }

    private static WidgetEntry counter() {
        return WidgetEntry.of(DemoCounterWidget.class, WidgetLabel.of("Counter"))
                .withIcon(new WidgetIcon.Emoji("🔢"))
                .withGroup(DEMO_GROUP);
    }

    private static final class Demo implements WorkspaceSpec {
        @Override public String kind()  { return "demo"; }
        @Override public String title() { return "Demo"; }
        @Override public List<WidgetEntry> widgetEntries() { return List.of(note(), counter(), books(), shelves(), picture()); }
    }

    private static final class Notes implements WorkspaceSpec {
        @Override public String kind()  { return "notes"; }
        @Override public String title() { return "Notes"; }
        @Override public List<WidgetEntry> widgetEntries() { return List.of(note()); }
    }
}
