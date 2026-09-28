package hue.captains.singapura.js.homing.workspace.log;

import hue.captains.singapura.js.homing.workspace.log.LogIds.WidgetId;
import hue.captains.singapura.js.homing.workspace.log.LogIds.WidgetKind;

import java.util.List;
import java.util.Objects;

/**
 * What happens to the workspace's roster - the widgets its core holds: one is
 * opened, one is closed. The core's layer of the log, the one every placement
 * stands on: a widget is, whether anything shows it or not, so it is opened
 * and closed here and placed in the placement's own events. A workspace comes
 * back roster first - each widget made again under its id, from its kind and
 * params - and its placement after.
 */
public sealed interface RosterEvent extends WorkspaceEvent {

    /** A widget was opened: its id, its kind, its params - in the order of their keys, each once. */
    record WidgetOpened(WidgetId id, WidgetKind kind, List<WidgetParam> params) implements RosterEvent {
        public WidgetOpened {
            Objects.requireNonNull(id, "WidgetOpened.id");
            Objects.requireNonNull(kind, "WidgetOpened.kind");
            params = WidgetParam.checked(params, "WidgetOpened.params");
        }
    }

    /** A widget was closed: disposed, and out of the roster; its id is never given again. */
    record WidgetClosed(WidgetId id) implements RosterEvent {
        public WidgetClosed {
            Objects.requireNonNull(id, "WidgetClosed.id");
        }
    }
}
