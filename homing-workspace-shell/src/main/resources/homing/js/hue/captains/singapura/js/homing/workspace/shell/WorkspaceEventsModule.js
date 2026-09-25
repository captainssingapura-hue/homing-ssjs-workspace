// =============================================================================
// WorkspaceEventsModule — the workspace event a component event IS, or nothing.
//
//   WorkspaceEvents.of(componentEvent) -> { name, payload } | null
//
// The panes, the grid and the desk each report their mutations on one sink, as
// frozen data tagged by `kind`. The workspace keeps a log of its own, as frozen
// data tagged by `name`. The two vocabularies were arrived at twice and agree
// about almost everything; this is the almost, written once, with no DOM, no
// state and no opinion about what the holder then does with the answer.
//
// WHAT IS NOT TRANSLATED, and why each one is a decision rather than a gap:
//
//   AddRequested     a question, not a mutation: the holder opens the picker,
//                    and what the user picks is the holder's to author.
//   TabAdded         the desk cannot know whether a tab came from the picker or
//                    was pinned at boot, and the workspace records which. The
//                    holder authored the spawn, so the holder names it.
//   CursorMoved      which pane the cursor is over is live state. Recording it
//                    would restore a cursor nobody put there.
//   a float          floating is a transient state: nothing to keep between
//                    visits. The panes never pass a float on - a tab afloat is
//                    where it left, and comes down from a float as a move from
//                    there - so nothing here knows of one.
//
// Everything else is a fact about the arrangement, and the same fact either
// language says it in.
// =============================================================================

/** The sides a grid subdivides on, and which is "after" the cell it came from. */
const _AFTER = Object.freeze({ right: true, bottom: true, left: false, top: false });

function _uuid(tab) {
    return (tab && tab.widgetInstanceUuid) || null;
}

class WorkspaceEvents {

    /** The kinds this answers for; anything else is deliberately null. */
    static KINDS = Object.freeze([
        "TabRemoved", "TabMoved", "TabActivated",
        "Subdivided", "Removed", "TracksChanged"]);

    /**
     * The workspace event `ev` is, or null when the workspace keeps no record
     * of it. Never throws on an unknown kind: a component may grow a kind this
     * does not know, and a log that refuses to be written is worse than a fact
     * that is not in it.
     */
    static of(ev) {
        if (!ev || typeof ev.kind !== "string") return null;
        // Part of a merge: the tabs a pane carries across as it goes, and what
        // each dock shows after. The merge is the fact, recorded as the grid
        // reports it, and its replay moves the tabs the same way.
        if (ev.merging) return null;
        switch (ev.kind) {
            case "TabRemoved":    return WorkspaceEvents._closed(ev);
            case "TabMoved":      return WorkspaceEvents._moved(ev);
            case "TabActivated":  return WorkspaceEvents._activated(ev);
            case "Subdivided":    return WorkspaceEvents._subdivided(ev);
            case "Removed":       return WorkspaceEvents._removed(ev);
            case "TracksChanged": return WorkspaceEvents._tracks(ev);
            default:              return null;
        }
    }

    /** A tab closed, by its cross or by the holder. */
    static _closed(ev) {
        const uuid = _uuid(ev.tab);
        if (!uuid) return null;
        return { name: "TabClosed", payload: {
            widgetInstanceId: uuid,
            widgetKind:       ev.tab.widgetKind || null,
            from:             { paneId: ev.slotId, tabIndex: ev.fromIndex }
        } };
    }

    /** A tab moved, within a pane or between two: a move always has a source, so `from` is always filled here. */
    static _moved(ev) {
        const uuid = _uuid(ev.tab);
        if (!uuid) return null;
        return { name: "TabMoved", payload: {
            widgetInstanceId: uuid,
            from:             { paneId: ev.srcSlotId,  tabIndex: ev.srcIndex },
            to:               { paneId: ev.destSlotId, tabIndex: ev.destIndex }
        } };
    }

    /**
     * Which tab a pane is showing, by the widget in it. A dock names the tab by
     * its id, which is the dock's and says nothing a log can keep; the holder
     * hands the tab along, and the tab says which widget it is. A tab with no
     * widget yet - a chooser - is showing nothing worth coming back to.
     */
    static _activated(ev) {
        const uuid = _uuid(ev.tab);
        if (!uuid) return null;
        return { name: "TabActivated", payload: {
            paneId:           ev.slotId,
            widgetInstanceId: uuid
        } };
    }

    /**
     * A new, empty pane beside one. The grid names the new cell, which the
     * workspace's event wants and used to have to infer; the side carries the
     * axis, so no orientation is written down and none can be written backwards.
     */
    static _subdivided(ev) {
        if (_AFTER[ev.side] === undefined) return null;
        return { name: "SplitCreated", payload: {
            paneId:    ev.cellId,
            newPaneId: ev.newCellId,
            side:      ev.side
        } };
    }

    /**
     * A pane gone. The grid says which one went; where its room goes is the
     * grid's own rule, and the holder supplies `toward` when it asked for a
     * particular heir.
     */
    static _removed(ev) {
        return { name: "SplitMerged", payload: { paneId: ev.cellId, toward: ev.toward || null } };
    }

    /** A split re-shared: the same path and the same shares, in both languages. */
    static _tracks(ev) {
        return { name: "TracksChanged", payload: { path: ev.path, ratios: ev.ratios } };
    }
}
