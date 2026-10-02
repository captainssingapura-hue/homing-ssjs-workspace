package hue.captains.singapura.js.homing.workspace.groups.core.models;

import java.util.Set;

/**
 * A workspace, as an arrangement needs it: its kind, and the kinds of widget it
 * offers - so an arrangement opens none it does not. What a workspace declares
 * beyond that - its widgets' classes, its parties - is its declaration's, which
 * is this and more.
 */
public interface WorkspaceSpec {

    /** Its kind: the one its log is kept under. */
    WorkspaceKind workspaceKind();

    /** The kinds of widget it offers. */
    Set<WidgetKind> widgetKinds();

    /** Whether it offers widgets of that kind. */
    default boolean offers(WidgetKind kind) { return widgetKinds().contains(kind); }
}
