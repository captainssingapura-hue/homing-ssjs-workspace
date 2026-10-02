package hue.captains.singapura.js.homing.workspace.log;

import hue.captains.singapura.js.homing.workspace.core.models.WidgetId;
import hue.captains.singapura.js.homing.workspace.core.models.WidgetName;
import hue.captains.singapura.js.homing.workspace.log.LogIds.WidgetKind;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * What happens to the workspace's roster - the widgets its core holds: one is
 * opened, one is renamed, one is closed. The core's layer of the log, the one every placement
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

    /**
     * A widget was named by a user - or its name was taken back, none, and it is
     * titled as it opened again, by the title derived from its identity. The
     * name is the widget's, whatever shows it.
     */
    record WidgetRenamed(WidgetId id, Optional<WidgetName> name) implements RosterEvent {
        public WidgetRenamed {
            Objects.requireNonNull(id, "WidgetRenamed.id");
            Objects.requireNonNull(name, "WidgetRenamed.name");
        }
    }

    /** A widget was closed: disposed, and out of the roster; its id is never given again. */
    record WidgetClosed(WidgetId id) implements RosterEvent {
        public WidgetClosed {
            Objects.requireNonNull(id, "WidgetClosed.id");
        }
    }
}
