package hue.captains.singapura.js.homing.workspace.demo;

import hue.captains.singapura.js.homing.workspace.WidgetEntry;
import hue.captains.singapura.js.homing.workspace.WidgetGroup;
import hue.captains.singapura.js.homing.workspace.WidgetIcon;
import hue.captains.singapura.js.homing.workspace.WidgetLabel;
import hue.captains.singapura.js.homing.workspace.shell.CssGraphWorkbenchWidget;
import hue.captains.singapura.js.homing.workspace.shell.DomOpsPartyMonitorWidget;
import hue.captains.singapura.js.homing.workspace.shell.WorkspaceSpec;
import hue.captains.singapura.js.homing.workspace.shell.WorkspaceSpecRegistry;

import java.util.List;

/**
 * The demo's workspaces, built from the demo's own widgets only.
 *
 * <p>The demo's widgets are written to the new contract — handed a branch, they
 * give back {@code { root }} and touch nothing else — so everything this demo
 * shows about panes, splitting, floating and docking is shown on widgets that
 * know nothing about any of it.</p>
 *
 * <p>The DIAGNOSTICS kind is the workspace's own two instruments, which the
 * shell ships and nothing else offers: the DomOpsParty monitor and the CSS
 * graph workbench. A kind of their own, so a workspace for work is not handed
 * the tools for looking at the machinery.</p>
 *
 * <p>The FOCUS LAB kind stresses the steward's marker (RFC 0066 E3, keyboard
 * §17.5): forms of native fields and a logical list, a summoner focusing them
 * by script, a monitor, and the picture and the counter beside them — the
 * picture's room the case of a focusable scroller.</p>
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

    /** The shell's diagnostics: the party monitor and the CSS graph. */
    public static final WorkspaceSpec DIAGNOSTICS = new Diagnostics();

    /** The Focus lab: forms, a summoner, a monitor, a picture and a counter. */
    public static final WorkspaceSpec FOCUS_LAB = new FocusLab();

    private static boolean registered;

    /**
     * Register them, once. The registry is process-wide and populated at boot,
     * so a site loaded twice in one JVM — a test and then a server — must not
     * register twice: the second is a duplicate-kind error, correctly.
     */
    public static synchronized void register() {
        if (registered) return;
        registered = true;
        WorkspaceSpecRegistry.INSTANCE.register(DEMO);
        WorkspaceSpecRegistry.INSTANCE.register(NOTES);
        WorkspaceSpecRegistry.INSTANCE.register(DIAGNOSTICS);
        WorkspaceSpecRegistry.INSTANCE.register(FOCUS_LAB);
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

    private static final WidgetGroup DIAGNOSTICS_GROUP = WidgetGroup.of("Diagnostics");

    private static WidgetEntry partyMonitor() {
        return WidgetEntry.of(DomOpsPartyMonitorWidget.class, WidgetLabel.of("Party monitor"))
                .withIcon(new WidgetIcon.Emoji("🌳"))
                .withGroup(DIAGNOSTICS_GROUP);
    }

    private static WidgetEntry cssGraph() {
        return WidgetEntry.of(CssGraphWorkbenchWidget.class, WidgetLabel.of("CSS graph"))
                .withIcon(new WidgetIcon.Emoji("🎨"))
                .withGroup(DIAGNOSTICS_GROUP);
    }

    private static final class Diagnostics implements WorkspaceSpec {
        @Override public String kind()  { return "diagnostics"; }
        @Override public String title() { return "Diagnostics"; }
        @Override public List<WidgetEntry> widgetEntries() { return List.of(partyMonitor(), cssGraph()); }
    }

    private static final WidgetGroup LAB_GROUP = WidgetGroup.of("Focus lab");

    private static WidgetEntry form() {
        return WidgetEntry.of(DemoFormWidget.class, WidgetLabel.of("Form"))
                .withIcon(new WidgetIcon.Emoji("🧾"))
                .withGroup(LAB_GROUP);
    }

    private static WidgetEntry summoner() {
        return WidgetEntry.of(DemoSummonerWidget.class, WidgetLabel.of("Summoner"))
                .withIcon(new WidgetIcon.Emoji("🪄"))
                .withGroup(LAB_GROUP);
    }

    private static WidgetEntry monitor() {
        return WidgetEntry.of(DemoMonitorWidget.class, WidgetLabel.of("Focus monitor"))
                .withIcon(new WidgetIcon.Emoji("🔭"))
                .withGroup(LAB_GROUP);
    }

    private static final class FocusLab implements WorkspaceSpec {
        @Override public String kind()  { return "focus-lab"; }
        @Override public String title() { return "Focus lab"; }
        @Override public List<WidgetEntry> widgetEntries() { return List.of(form(), summoner(), monitor(), picture(), counter()); }
    }

    private static final class Notes implements WorkspaceSpec {
        @Override public String kind()  { return "notes"; }
        @Override public String title() { return "Notes"; }
        @Override public List<WidgetEntry> widgetEntries() { return List.of(note()); }
    }
}
