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
 * Two workspaces for the demo, and deliberately two.
 *
 * <p>One would show that a workspace mounts. Two show what only the registry
 * knows: the catalogue of kinds the switcher offers and the address a change
 * of kind goes to — the part of {@code WorkspaceSpecsModule} that no single
 * spec could fill in. Switch from Monitors to Graph and back, and the flat
 * address the MPA serves is what carries you.</p>
 *
 * <p>Both are built from widgets the shell already ships, on purpose: a demo
 * that had to invent widgets would be testing the widgets. These test the
 * mounting.</p>
 */
public final class WorkspaceDemoSpecs {

    private WorkspaceDemoSpecs() {}

    /** The tree and the sheets side by side: what the party did, and what the design says. */
    public static final WorkspaceSpec MONITORS = new Monitors();

    /** The design graph alone, filling the room. */
    public static final WorkspaceSpec GRAPH = new Graph();

    private static boolean registered;

    /**
     * Register both, once. The registry is process-wide and populated at boot,
     * so a site that is loaded twice in one JVM — a test and then a server —
     * must not register twice: the second is a duplicate-kind error, correctly.
     */
    public static synchronized void register() {
        if (registered) return;
        registered = true;
        WorkspaceSpecRegistry.INSTANCE.register(MONITORS);
        WorkspaceSpecRegistry.INSTANCE.register(GRAPH);
    }

    private static final WidgetGroup MONITOR_GROUP = WidgetGroup.of("Monitors");

    private static WidgetEntry party() {
        return WidgetEntry.of(DomOpsPartyMonitorWidget.class, WidgetLabel.of("Party monitor"))
                .withIcon(new WidgetIcon.Emoji("\uD83C\uDF3F"))
                .withGroup(MONITOR_GROUP);
    }

    private static WidgetEntry graph() {
        return WidgetEntry.of(CssGraphWorkbenchWidget.class, WidgetLabel.of("CSS graph"))
                .withIcon(new WidgetIcon.Emoji("\uD83C\uDFA8"))
                .withGroup(MONITOR_GROUP);
    }

    private static final class Monitors implements WorkspaceSpec {
        @Override public String kind()  { return "monitors"; }
        @Override public String title() { return "Monitors"; }
        @Override public List<WidgetEntry> widgetEntries() { return List.of(party(), graph()); }
    }

    private static final class Graph implements WorkspaceSpec {
        @Override public String kind()  { return "graph"; }
        @Override public String title() { return "Design graph"; }
        @Override public List<WidgetEntry> widgetEntries() { return List.of(graph()); }
    }
}
