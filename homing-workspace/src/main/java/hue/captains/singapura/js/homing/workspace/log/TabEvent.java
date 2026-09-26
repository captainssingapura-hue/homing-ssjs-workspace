package hue.captains.singapura.js.homing.workspace.log;

import hue.captains.singapura.js.homing.workspace.log.LogIds.TabId;
import hue.captains.singapura.js.homing.workspace.log.LogIds.WidgetKind;
import hue.captains.singapura.js.homing.workspace.log.LogIds.WidgetTitle;

import java.util.Objects;

/** What happens to a tab: it is opened, becomes another kind, is renamed, moved, shown, closed. */
public sealed interface TabEvent extends WorkspaceEvent {

    /** A tab was opened in a host, at an index there, holding a widget of a kind, under a title. */
    record TabOpened(TabId id, WidgetKind kind, WidgetTitle title, Host host, int index) implements TabEvent {
        public TabOpened {
            Objects.requireNonNull(id, "TabOpened.id");
            Objects.requireNonNull(kind, "TabOpened.kind");
            Objects.requireNonNull(title, "TabOpened.title");
            Objects.requireNonNull(host, "TabOpened.host");
            if (index < 0) throw new IllegalArgumentException("TabOpened.index " + index + " — non-negative");
        }
    }

    /** A tab became a tab of another kind, in place: the same tab, a new widget, a new title. */
    record TabBecame(TabId id, WidgetKind kind, WidgetTitle title) implements TabEvent {
        public TabBecame {
            Objects.requireNonNull(id, "TabBecame.id");
            Objects.requireNonNull(kind, "TabBecame.kind");
            Objects.requireNonNull(title, "TabBecame.title");
        }
    }

    /** A tab was renamed where it is: by its widget, its holder or its pane. */
    record TabRenamed(TabId id, WidgetTitle title) implements TabEvent {
        public TabRenamed {
            Objects.requireNonNull(id, "TabRenamed.id");
            Objects.requireNonNull(title, "TabRenamed.title");
        }
    }

    /** A tab moved to an index in a host: another host — a region, a float — or its own. */
    record TabMoved(TabId id, Host host, int index) implements TabEvent {
        public TabMoved {
            Objects.requireNonNull(id, "TabMoved.id");
            Objects.requireNonNull(host, "TabMoved.host");
            if (index < 0) throw new IllegalArgumentException("TabMoved.index " + index + " — non-negative");
        }
    }

    /** A host came to show a tab. */
    record TabShown(Host host, TabId id) implements TabEvent {
        public TabShown {
            Objects.requireNonNull(host, "TabShown.host");
            Objects.requireNonNull(id, "TabShown.id");
        }
    }

    /** A tab was closed. */
    record TabClosed(TabId id) implements TabEvent {
        public TabClosed {
            Objects.requireNonNull(id, "TabClosed.id");
        }
    }
}
