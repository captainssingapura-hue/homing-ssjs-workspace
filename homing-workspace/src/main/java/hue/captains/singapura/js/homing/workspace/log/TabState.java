package hue.captains.singapura.js.homing.workspace.log;


import java.util.Objects;

/** An open tab: which, what it holds, what it is called. */
public record TabState(TabId id, WidgetKind kind, WidgetTitle title) {
    public TabState {
        Objects.requireNonNull(id, "TabState.id");
        Objects.requireNonNull(kind, "TabState.kind");
        Objects.requireNonNull(title, "TabState.title");
    }
}
