// =============================================================================
// WidgetTabs — a split-grid workspace's register of panes: the core's port
// (RFC 0066 E3, the workspace detour: requests), over its desk's register. A
// widget and its pane live and die together, so the core asks here for a
// widget's pane when it creates it — a TAB-PANE opened in the desk's register
// under the widget's id, titled after the widget, in no host — titles it again
// when the widget is renamed, and closes it here with the widget. Where it is
// shown is the placement's (GridPlacement); it never makes or closes one.
//
// The tab-pane holds a HostedWidget lent empty: its container is what the core
// makes the widget in, and the widget is handed to it once made (hold) — its
// parties grafted where the tab is, and travelling with it.
//
// A close ASKED for on the tab — its cross, the tab menu's Close, its float's
// cross — is no close here: it is said, onClose(id), for the page to ask the
// core, which unmounts and then closes, in its order.
//
//   new WidgetTabs(desk, { title, onClose })
//     title    (entry) → the widget's title: the name a user gave it, else its identity's
//     onClose  (id) → a close asked for on the widget's tab
//   tabs.lend(entry) → the container, in a tab-pane in no host   (the core's port)
//   tabs.rename(entry)    the tab titled again                   (the core's port)
//   tabs.release(entry)   the tab-pane closed                    (the core's port)
//   tabs.hold(entry)      the widget made in its container, grafted: once the core says it opened
//   tabs.tab(id) → the widget's tab-pane, or null
// =============================================================================

class WidgetTabs {
    constructor(desk, opts) {
        var o = opts || {};
        if (!desk || !desk.register) throw new Error("[WidgetTabs] the desk is required: its register opens the tabs");
        this._desk = desk;
        this._title = typeof o.title === "function" ? o.title : function (e) { return e.id; };
        this._onClose = typeof o.onClose === "function" ? o.onClose : null;
        this._held = new Map();   // a widget's id → the HostedWidget lent empty in its tab-pane
    }

    lend(entry) {
        var self = this, held = null;
        this._desk.register.open({
            id: entry.id, title: this._title(entry),
            make: function (branch, tab) { held = new HostedWidget(branch, tab); return held; },
            onCloseRequested: this._onClose ? function () { self._onClose(entry.id); } : null
        });
        this._held.set(entry.id, held);
        return held.root;
    }

    hold(entry) {
        var h = this._held.get(entry.id);
        if (h && !h.widget) h.hold(entry.widget);
    }

    rename(entry) {
        var tp = this.tab(entry.id);
        if (tp) tp.title(this._title(entry));
    }

    release(entry) {
        var tp = this.tab(entry.id);
        this._held.delete(entry.id);
        if (tp) tp.close();
    }

    tab(id) { return this._desk.register.get(id); }
}
