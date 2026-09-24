// =============================================================================
// WorkspaceStateModel — pure in-memory model of workspace effective state.
//
// Replaces physical replay (each event triggers an MTP mutation) with
// virtual replay: events fold into this model, then orchestrator projects
// the final state to MTP once. A spawn-then-close pair becomes a no-op
// at projection time; a spawn-then-move-five-times becomes one place to
// the final pane.
//
// Three pieces:
//   _layout       : MTP-native layout tree ({kind:'leaf',slotId} | split)
//   _tabsBySlot   : Map<slotId, Array<TabDescriptor>>
//   _activeUuid   : widget UUID currently workspace-active (or null)
//   _activeBySlot : slotId → the uuid that pane is showing
//
// TabDescriptor shape:
//   { widgetInstanceUuid, widgetKind, params, title, pinned }
//
// Events folded via apply(eventRow). Recognised event names:
//   WidgetSpawnedPinned       — add a pinned widget
//   WidgetSpawnedFromPicker   — add a picker-spawned widget
//   TabMoved                  — relocate by widgetInstanceId
//   TabClosed                 — remove by widgetInstanceId
//   SplitCreated              — a new empty pane beside one, on a side
//   SplitMerged               — a pane goes, its room to the heir
//   TracksChanged             — a split re-shared, all its tracks
//   WorkspaceActiveChanged    — set active widget UUID
// Any other event name is silently ignored.
//
// Explicit Substrate doctrine:
//   - Per-workspace instance; no INSTANCE singleton.
//   - No external collaborators (no MTP ref, no recorder, no DOM).
//   - All mutations idempotent where it makes sense.
// =============================================================================

// A side names where a pane lands; the AXIS follows from it and is derived
// here and nowhere else, which is what makes it impossible to write down
// backwards. Same table as PaneDirection's, in the other language.
var _SIDES = Object.freeze({ left: 'horizontal', right: 'horizontal', top: 'vertical', bottom: 'vertical' });

class WorkspaceStateModel {

    /**
     * @param seedLayout optional MTP-native layout to start from — the workspace
     *        shell passes WorkspaceSpec.arrangement()'s (RFC 0060 D9).
     *
     * This constructor used to hold its own copy of the 2x2, under a comment
     * saying it had to track MTP's default by hand. It no longer guesses: it is
     * TOLD, and its own fallback is the trivial one. That is what D9 is for —
     * a default nobody owned was written in two places and could disagree.
     */
    constructor(seedLayout) {
        this._layout = seedLayout
            ? this._cloneNode(seedLayout)
            : { kind: 'leaf', slotId: 'main' };   // RFC 0060 D8
        this._tabsBySlot = new Map();
        for (const id of WorkspaceStateModel._leafSlotIds(this._layout)) {
            this._tabsBySlot.set(id, []);
        }
        this._activeUuid   = null;
        // slotId → the uuid that pane is SHOWING. Per pane, because a pane shows
        // one of its tabs whether or not the keyboard is anywhere near it.
        this._activeBySlot = new Map();
        this._nextSplitId  = 1;
    }

    /** Every leaf slotId in a layout, in document order. */
    static _leafSlotIds(node) {
        if (!node) return [];
        if (node.kind === 'leaf') return [node.slotId];
        var out = [];
        for (const c of (node.children || [])) {
            out = out.concat(WorkspaceStateModel._leafSlotIds(c.pane));
        }
        return out;
    }

    // ── Public API ──────────────────────────────────────────────────────

    /** Fold one event into the model. Unknown event names are ignored. */
    apply(eventRow) {
        if (!eventRow) return;
        const name = eventRow.name;
        const p    = eventRow.payload || {};
        switch (name) {
            case 'WidgetSpawnedPinned':     this._spawn(p, true);  break;
            case 'WidgetSpawnedFromPicker': this._spawn(p, false); break;
            case 'TabMoved':                this._move(p);         break;
            case 'TabClosed':               this._close(p);        break;
            case 'SplitCreated':            this._split(p);        break;
            case 'SplitMerged':             this._merge(p);        break;
            case 'TabActivated':            this._activated(p);    break;
            case 'TracksChanged':           this._tracks(p);       break;
            case 'WorkspaceActiveChanged':  this._setActive(p);    break;
            default: /* unknown event — ignore */                  break;
        }
    }

    /** Whether the model has any tabs at all. */
    isEmpty() {
        for (const tabs of this._tabsBySlot.values()) {
            if (tabs.length > 0) return false;
        }
        return true;
    }

    /** MTP-native layout tree (a fresh deep copy is safer for projection). */
    layout() { return this._cloneNode(this._layout); }

    /** Map<slotId, TabDescriptor[]> — fresh map; tab descriptors shared. */
    tabsBySlot() {
        const out = new Map();
        this._tabsBySlot.forEach(function (arr, slotId) {
            out.set(slotId, arr.slice());
        });
        return out;
    }

    /** Currently workspace-active widgetInstanceUuid, or null. */
    activeUuid() { return this._activeUuid; }

    /** The uuid the pane `slot` is showing, or null. */
    activeInSlot(slot) { return this._activeBySlot.get(slot) || null; }

    /** Every pane's showing tab, as a fresh Map. */
    activeBySlot() { return new Map(this._activeBySlot); }

    /** Inspect snapshot — for tests + DevTools surfacing. */
    inspect() {
        const tabs = [];
        this._tabsBySlot.forEach(function (arr, slot) {
            for (const t of arr) tabs.push({
                slot:               slot,
                widgetInstanceUuid: t.widgetInstanceUuid,
                widgetKind:         t.widgetKind,
                pinned:             !!t.pinned
            });
        });
        return {
            layout:       this._layout,
            tabs:         tabs,
            activeUuid:   this._activeUuid,
            activeBySlot: Array.from(this._activeBySlot.entries()),
            slotCount:    this._tabsBySlot.size,
            nextSplitId:  this._nextSplitId
        };
    }

    /**
     * Serialise to a checkpoint snapshot. Plain-JSON-safe (no Maps).
     * Stores the layout, tab descriptors per slot (as an array of
     * [slotId, tabs[]] pairs), active uuid, and split counter so the
     * next mint after restore doesn't collide with restored slot ids.
     */
    toSnapshot() {
        const tabsPairs = [];
        this._tabsBySlot.forEach(function (arr, slot) {
            tabsPairs.push([slot, arr.slice()]);
        });
        return {
            schemaVersion: 2,
            layout:        this._cloneNode(this._layout),
            tabsBySlot:    tabsPairs,
            activeUuid:    this._activeUuid,
            activeBySlot:  Array.from(this._activeBySlot.entries()),
            nextSplitId:   this._nextSplitId
        };
    }

    /**
     * Restore from a snapshot produced by {@code toSnapshot()}.
     * Permissive: missing fields default to fresh-model values; an
     * unknown schemaVersion throws (caller falls back to fresh model).
     */
    static fromSnapshot(snapshot) {
        const m = new WorkspaceStateModel();
        if (!snapshot) return m;
        // 1 is read as well as 2: its splits are binary and its panes remember
        // no showing tab, and both of those read as an absence rather than a
        // conflict - the layout below handles the first, and a pane with no
        // showing tab recorded simply shows its first.
        if (snapshot.schemaVersion !== 1 && snapshot.schemaVersion !== 2) {
            throw new Error('[WorkspaceStateModel] unknown snapshot '
                          + 'schemaVersion: ' + snapshot.schemaVersion);
        }
        if (snapshot.layout) m._layout = m._cloneNode(snapshot.layout);
        m._tabsBySlot = new Map();
        if (Array.isArray(snapshot.tabsBySlot)) {
            for (const pair of snapshot.tabsBySlot) {
                if (Array.isArray(pair) && pair.length === 2) {
                    m._tabsBySlot.set(pair[0], pair[1].slice());
                }
            }
        }
        // Ensure every leaf has a tabs entry (snapshot may have pruned empties).
        m._forEachLeaf(m._layout, function (slotId) {
            if (!m._tabsBySlot.has(slotId)) m._tabsBySlot.set(slotId, []);
        });
        m._activeUuid  = snapshot.activeUuid || null;
        for (const pair of (snapshot.activeBySlot || [])) {
            // Only where the pane still holds that tab: a snapshot outlives the
            // arrangement it was taken of, and a pane showing a tab it no longer
            // has is worse than a pane showing its first.
            if (m._tabHeldBy(pair[0], pair[1])) m._activeBySlot.set(pair[0], pair[1]);
        }
        m._nextSplitId = (typeof snapshot.nextSplitId === 'number')
                       ? snapshot.nextSplitId : 1;
        return m;
    }

    // ── Event handlers ──────────────────────────────────────────────────

    _spawn(p, isPinned) {
        const uuid = p.widgetInstanceId;
        if (!uuid) return;
        if (this._findTab(uuid)) return;   // idempotent
        const slot = this._slotIdOfPaneId(p.to && p.to.paneId)
                  || this._defaultSlot();
        const tabs = this._tabsBySlot.get(slot);
        if (!tabs) return;                  // unknown slot — drop
        const idx = (p.to && typeof p.to.tabIndex === 'number')
                  ? Math.max(0, Math.min(p.to.tabIndex, tabs.length))
                  : tabs.length;
        tabs.splice(idx, 0, {
            widgetInstanceUuid: uuid,
            widgetKind:         p.widgetKind,
            params:             p.params || {},
            title:              p.title || null,
            pinned:             !!isPinned
        });
        // The arrival default is FRONT: a tab that lands is the one its pane
        // shows. Quiet arrivals do not spawn - they are moves.
        this._activeBySlot.set(slot, uuid);
    }

    /** The pane `paneId` is showing this tab now. */
    _activated(p) {
        const slot = this._slotIdOfPaneId(p.paneId);
        const uuid = p.widgetInstanceId;
        if (!slot || !uuid) return;
        if (!this._tabHeldBy(slot, uuid)) return;   // not that pane's tab to show
        this._activeBySlot.set(slot, uuid);
    }

    /** Whether `slot` holds the tab `uuid`. */
    _tabHeldBy(slot, uuid) {
        const tabs = this._tabsBySlot.get(slot);
        if (!tabs) return false;
        for (const t of tabs) if (t.widgetInstanceUuid === uuid) return true;
        return false;
    }

    /**
     * The tab a pane was showing has gone from it. The pane shows the one that
     * took its place, or the one before it when it was last, or nothing when it
     * was the only one — the same answer a tab strip gives, so the two agree
     * without the strip having to say.
     */
    _showingLeft(slot, wasIndex) {
        const tabs = this._tabsBySlot.get(slot) || [];
        if (!tabs.length) { this._activeBySlot.delete(slot); return; }
        const i = Math.min(wasIndex, tabs.length - 1);
        this._activeBySlot.set(slot, tabs[i].widgetInstanceUuid);
    }

    _move(p) {
        const uuid = p.widgetInstanceId;
        if (!uuid) return;
        const found = this._findTab(uuid);
        if (!found) return;
        const destSlot = this._slotIdOfPaneId(p.to && p.to.paneId)
                      || this._defaultSlot();
        if (!this._tabsBySlot.has(destSlot)) return;
        // Remove from src
        const srcTabs = this._tabsBySlot.get(found.slot);
        const [tab]   = srcTabs.splice(found.index, 1);
        // Insert at dest
        const destTabs = this._tabsBySlot.get(destSlot);
        const idx = (p.to && typeof p.to.tabIndex === 'number')
                  ? Math.max(0, Math.min(p.to.tabIndex, destTabs.length))
                  : destTabs.length;
        destTabs.splice(idx, 0, tab);
        // A tab that moved is shown where it landed, and the pane it left shows
        // whatever took its place.
        if (this._activeBySlot.get(found.slot) === uuid) this._showingLeft(found.slot, found.index);
        this._activeBySlot.set(destSlot, uuid);
    }

    _close(p) {
        const uuid = p.widgetInstanceId;
        if (!uuid) return;
        const found = this._findTab(uuid);
        if (!found) return;
        this._tabsBySlot.get(found.slot).splice(found.index, 1);
        if (this._activeUuid === uuid) this._activeUuid = null;
        if (this._activeBySlot.get(found.slot) === uuid) this._showingLeft(found.slot, found.index);
    }

    /**
     * A new, empty pane beside {@code paneId} on {@code side}.
     *
     * The grid's own rule, and the reason this is not just "wrap the leaf in a
     * split": when the row or column the pane already sits in runs the same way,
     * the new pane JOINS it as a sibling and the two share the target's track
     * between them. Only a pane with no such row around it becomes a split of
     * the two. Authoring a shape, dragging one out and replaying a log all land
     * on the same tree because all three apply this here.
     */
    _split(p) {
        const side = _SIDES[p.side] ? p.side : 'right';
        const axis = _SIDES[side];
        const after = (side === 'right' || side === 'bottom');
        const newSlotId = p.newPaneId || ('sp_' + (this._nextSplitId++));
        const found = this._findLeafBySlot(this._layout, String(p.paneId || ''));
        if (!found) return;
        const fresh = { kind: 'leaf', slotId: newSlotId };

        if (found.parent && found.parent.orientation === axis) {
            const kids = found.parent.children;
            const share = kids[found.index].ratio;
            kids[found.index].ratio = share / 2;
            kids.splice(after ? found.index + 1 : found.index, 0, { ratio: share / 2, pane: fresh });
        } else {
            const kept = { kind: 'leaf', slotId: found.node.slotId };
            found.replaceWith({
                kind: 'split', orientation: axis,
                children: after
                    ? [{ ratio: 0.5, pane: kept }, { ratio: 0.5, pane: fresh }]
                    : [{ ratio: 0.5, pane: fresh }, { ratio: 0.5, pane: kept }]
            });
        }
        if (!this._tabsBySlot.has(newSlotId)) this._tabsBySlot.set(newSlotId, []);
    }

    /**
     * {@code paneId} goes, by the GRID's rules and no copy of them: its room to
     * {@code toward} when that pane faces it across a whole divider, else to
     * the neighbour holding it, else to the one beside it; its tabs to the pane
     * that gains the room. The grid did exactly this on the day, and a replay
     * asks the same tree the same question, so the two cannot drift - which
     * they did while this kept a simpler rule of its own. The last pane cannot go.
     */
    _merge(p) {
        const id = String(p.paneId || '');
        const toward = p.toward || null;
        const tree = WorkspaceGrid.toGrid(this._layout);
        const hit = SplitGridTree.find(tree, id);
        if (!hit || !hit.parent) return;                     // no such pane, or the last one
        this._giveTabs(id, SplitGridTree.heirs(tree, id, toward)[0]);
        this._layout = WorkspaceGrid.fromGrid(SplitGridTree.remove(tree, id, toward));
    }

    /** The tabs of a pane that is going, appended to the pane that gains its room. */
    _giveTabs(fromSlot, toSlot) {
        const kept = this._tabsBySlot.get(toSlot) || [];
        for (const t of (this._tabsBySlot.get(fromSlot) || [])) kept.push(t);
        this._tabsBySlot.set(toSlot, kept);
        this._tabsBySlot.delete(fromSlot);
        // The heir keeps showing what it was showing; the pane that went shows
        // nothing, and only its tabs come across.
        this._activeBySlot.delete(fromSlot);
        if (!this._activeBySlot.has(toSlot) && kept.length) {
            this._activeBySlot.set(toSlot, kept[0].widgetInstanceUuid);
        }
    }

    /**
     * The split at {@code path} was re-shared: these are its tracks now. All of
     * them, because a drag moves the pair either side of one divider and a split
     * has two or more. A path is child indexes from the root joined by '/', the
     * root split's being empty — a split has no id to be named by.
     */
    _tracks(p) {
        const node = this._findSplitByPath(String(p.path == null ? '' : p.path));
        if (!node) return;
        const rs = p.ratios;
        if (!Array.isArray(rs) || rs.length !== node.children.length) return;
        let sum = 0;
        for (const r of rs) { if (typeof r !== 'number' || !(r > 0) || !isFinite(r)) return; sum += r; }
        for (let i = 0; i < rs.length; i++) node.children[i].ratio = rs[i] / sum;
    }

    _setActive(p) {
        if (!p.to || !p.to.widgetInstanceId) return;
        this._activeUuid = p.to.widgetInstanceId;
    }

    /**
     * Update the first-child ratio of the split at paneId. Second
     * child's ratio is derived as 1 - newRatio. No-op for missing
     * path or non-split target.
     */
    _setRatio(p) {
        const parentPath = p.paneId || '_';
        const r = (typeof p.ratio === 'number') ? p.ratio : null;
        if (r == null || !isFinite(r) || r <= 0 || r >= 1) return;
        const found = this._findNodeByPaneId(parentPath);
        if (!found || !found.node || found.node.kind !== 'split') return;
        const kids = found.node.children;
        if (!kids || kids.length !== 2) return;
        kids[0].ratio = r;
        kids[1].ratio = 1 - r;
    }

    // ── Path / lookup helpers ───────────────────────────────────────────

    /** Returns {tab, slot, index} for a uuid, or null. */
    _findTab(uuid) {
        let hit = null;
        this._tabsBySlot.forEach(function (arr, slot) {
            if (hit) return;
            for (let i = 0; i < arr.length; i++) {
                if (arr[i].widgetInstanceUuid === uuid) {
                    hit = { tab: arr[i], slot: slot, index: i };
                    return;
                }
            }
        });
        return hit;
    }

    /**
     * Walk the layout tree following the paneId path
     * ('_' = root, '_1' = first child of root, '_1_2' = second child of
     * first child of root) and return a handle:
     *   { node, replaceWith(newNode), parent, indexInParent }
     */
    /**
     * The leaf whose slotId is `slot`, with the split holding it and which track
     * of it that is:  { node, parent, index, replaceWith(newNode) }
     *
     * By IDENTITY, not by position. _findNodeByPaneId reads its argument as a
     * PATH — '_1_2' means the second child of the first — which is a different
     * question and stays for the callers that ask it. A pane has an id, so the
     * split events name it and this answers by it.
     */
    _findLeafBySlot(node, slot, parent, index) {
        const self = this;
        if (!node) return null;
        if (node.kind === 'leaf') {
            if (node.slotId !== slot) return null;
            return {
                node: node, parent: parent || null, index: (index == null ? -1 : index),
                replaceWith: parent
                    ? function (n) { parent.children[index].pane = n; }
                    : function (n) { self._layout = n; }
            };
        }
        for (let i = 0; i < node.children.length; i++) {
            const hit = this._findLeafBySlot(node.children[i].pane, slot, node, i);
            if (hit) return hit;
        }
        return null;
    }

    /**
     * The split at `path` — child indexes from the root joined by '/', the root
     * split's path being empty. The grid's spelling, so a path crosses between
     * them unchanged.
     */
    _findSplitByPath(path) {
        let cur = this._layout;
        const parts = path.length ? path.split('/') : [];
        for (const part of parts) {
            if (!cur || cur.kind !== 'split') return null;
            const i = parseInt(part, 10);
            if (!(i >= 0) || i >= cur.children.length) return null;
            cur = cur.children[i].pane;
        }
        return (cur && cur.kind === 'split') ? cur : null;
    }

    _findNodeByPaneId(paneId) {
        // Walk segments. Root case handled outside the loop.
        const parts = String(paneId || '_').split('_').filter(s => s.length > 0);
        const self  = this;
        if (parts.length === 0) {
            // Root: no parent, replace by mutating this._layout.
            return {
                node:          this._layout,
                replaceWith:   function (n) { self._layout = n; },
                parent:        null,
                indexInParent: -1
            };
        }
        let parent  = this._layout;
        let cur     = this._layout;
        let parentSlot = null;   // {parentNode, childIndex} for replaceWith
        for (const part of parts) {
            if (!cur || cur.kind !== 'split') return null;
            const idx = (part === '1') ? 0 : (part === '2') ? 1 : -1;
            if (idx < 0) return null;
            parent     = cur;
            parentSlot = { parentNode: cur, childIndex: idx };
            cur        = cur.children[idx].pane;
        }
        if (!parentSlot) return null;
        return {
            node:          cur,
            replaceWith:   function (n) { parentSlot.parentNode.children[parentSlot.childIndex].pane = n; },
            parent:        parent,
            indexInParent: parentSlot.childIndex
        };
    }

    /** Returns the live slotId at a paneId path, or null. */
    /**
     * The live slotId a widget location addresses.
     *
     * TWO VOCABULARIES MEET HERE, which is worth saying plainly because nothing
     * else does. A layout addresses panes by NAME (`tl`, `editor`, `sp_1`), while
     * _findNodeByPaneId reads a paneId POSITIONALLY — splitting on '_' and taking
     * each segment as first-or-second child, so `1_2` means "second child of the
     * first". Both are legitimate; they are simply different, and a name handed to
     * the positional walk yields -1 and resolves to nothing.
     *
     * RFC 0060 gives panes author-chosen names, so the name is tried FIRST and the
     * positional walk remains the fallback. Without this an arrangement's widgets
     * all landed in the default slot — silently, because the caller's `|| this
     * ._defaultSlot()` turns "not found" into "somewhere plausible".
     */
    _slotIdOfPaneId(paneId) {
        if (paneId == null) return null;
        if (paneId === '' || paneId === '_') {
            // Root only resolves if it's a leaf.
            return (this._layout && this._layout.kind === 'leaf')
                ? this._layout.slotId : null;
        }
        // By name — a leaf whose slotId IS this id.
        if (WorkspaceStateModel._leafSlotIds(this._layout).indexOf(paneId) >= 0) {
            return paneId;
        }
        // By position — the historical form, kept for logs that use it.
        const hit = this._findNodeByPaneId(paneId);
        return (hit && hit.node && hit.node.kind === 'leaf')
                ? hit.node.slotId : null;
    }

    /** First leaf's slotId — used as default spawn target. */
    _defaultSlot() {
        const leaf = this._firstLeaf(this._layout);
        return leaf ? leaf.slotId : null;
    }

    _firstLeaf(node) {
        if (!node) return null;
        if (node.kind === 'leaf') return node;
        return this._firstLeaf(node.children[0].pane);
    }

    /** Walk every leaf in the layout, invoking fn(slotId). */
    _forEachLeaf(node, fn) {
        if (!node) return;
        if (node.kind === 'leaf') { fn(node.slotId); return; }
        for (const c of node.children) this._forEachLeaf(c.pane, fn);
    }

    /** Deep clone a layout tree node — defensive snapshot. */
    _cloneNode(node) {
        if (!node) return null;
        if (node.kind === 'leaf') return { kind: 'leaf', slotId: node.slotId };
        return {
            kind: 'split',
            orientation: node.orientation,
            children: node.children.map(c => ({
                ratio: c.ratio,
                pane:  this._cloneNode(c.pane)
            }))
        };
    }
}
