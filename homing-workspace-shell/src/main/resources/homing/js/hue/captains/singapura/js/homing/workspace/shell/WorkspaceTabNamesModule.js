// =============================================================================
// WorkspaceTabNames — a tab's name and icon, for the WorkspacePanes that holds
// the tab: the tab-pane's own, wherever it is. Static, over the panes it is
// handed; the icon elements are kept on the panes.
//
//   WorkspaceTabNames.retitle(panes, tabId, title)   the name, wherever the tab is
//   WorkspaceTabNames.setIcon(panes, tabId, icon)    a widget kind's icon, { kind, value }, or null
//   WorkspaceTabNames.forget(panes, tabId)           the tab gone: its icon with it
//
// The chip is the tab-pane's, and goes wherever it goes, docked or afloat, so
// the name and icon are said to the tab-pane, and written on the holder's
// record of the tab as well. The icon is an element made and kept here, one per tab,
// so a tab that changes kind (a chooser that becomes a widget) changes its
// icon in place.
// =============================================================================

const _namesOwner = Object.freeze({ toString: () => "workspaceTabNames" });

class WorkspaceTabNames {

    static retitle(panes, tabId, title) {
        var tab = panes._tabObjs.get(tabId);
        if (tab) tab.title = title;
        var tp = panes._desk.register.get(tabId);
        if (tp) tp.title(title);
    }

    static setIcon(panes, tabId, icon) {
        var el = icon ? WorkspaceTabNames._iconFor(panes, tabId, icon) : null;
        var tab = panes._tabObjs.get(tabId);
        if (tab) tab.icon = el;
        var tp = panes._desk.register.get(tabId);
        if (tp) tp.icon(el);
    }

    static forget(panes, tabId) {
        var rec = panes._icons.get(tabId);
        if (!rec) return;
        panes._icons.delete(tabId);
        try { rec.branch.dissolve(); } catch (e) {}
    }

    /** The tab's icon element, made on first asking and shown with the kind's glyph; an icon the page cannot draw yet shows the kind's default. */
    static _iconFor(panes, tabId, icon) {
        var rec = panes._icons.get(tabId);
        if (!rec) {
            var b = panes._branch.createBranch("icon" + (++panes._iconSeq));
            b.activate(_namesOwner);
            rec = { branch: b, el: b.createElement("glyph", "span") };
            panes._icons.set(tabId, rec);
        }
        // an Svg icon is a reference the page has no renderer for yet; the picker shows the default too
        rec.el.textContent = icon.kind === "emoji" && icon.value ? icon.value : "\u{1F4E6}";
        return rec.el;
    }
}
