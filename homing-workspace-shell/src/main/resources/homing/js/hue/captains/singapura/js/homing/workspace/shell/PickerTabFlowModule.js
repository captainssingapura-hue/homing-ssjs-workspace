// =============================================================================
// PickerTabFlow — Phase 13 of the workspace-shell chrome.
//
// The '+' affordance on every pane strip lands here. Flow:
//
//   1. openInSlot(slotId)
//        ├─ open a tab in the pane (mtp.openTab: the desk names it) + switchTab +
//        ├─ mount WidgetPicker into the tab's room with
//        │     entries     = spec.entries minus pinned
//        │     disabledIds = current singletons (one per kind)
//        │     onPick(entry, params)  → mutate tab in place OR go to the one open
//        │     onCancel()             → mtp.removeTab
//        └─ done (sync)
//
//   2. picker.onPick(entry, params)
//        ├─ if params === null  → the singleton already open: the user confirmed
//        │                         going to it in the picker's redirector (§17.3)
//        └─ else                → _mutateIntoWidget: same tabId, swap content
//
//   3. _mutateIntoWidget(slotId, tabId, entry, params, holder)
//        ├─ sync: retitle tab; create branch; show loading
//        └─ mounter.resolve(entry).then(mod => { sync mount + attach })
//
// Explicit Substrate doctrine:
//   - Stateful per-workspace instance (NOT a singleton — one per chrome).
//   - All methods are instance methods (no static helpers).
//   - No async sugar — Promises returned/composed explicitly.
//   - I/O boundary is exactly one .then() in _mutateIntoWidget.
// =============================================================================

class PickerTabFlow {

    /**
     * Constructor takes per-instance data (mtp, widgetsBranch, spec) AND
     * collaborator overrides (mounter, WidgetPickerCtor). A widget's word to
     * where it runs is its room's host, not something this flow carries.
     * Per-call methods receive only call-time data; no per-call collaborator
     * overrides — the dep-graph walker can therefore enumerate everything
     * this flow depends on from instance fields alone.
     */
    constructor(opts) {
        if (!opts || !opts.mtp)           throw new Error('[PickerTabFlow] opts.mtp required');
        if (!opts.widgetsBranch)          throw new Error('[PickerTabFlow] opts.widgetsBranch required');
        if (!opts.spec)                   throw new Error('[PickerTabFlow] opts.spec required');
        this._mtp              = opts.mtp;
        // (MTP is focus-agnostic). Optional so unit tests can exercise the spawn
        // flow without focus side effects.
        this._widgetsBranch    = opts.widgetsBranch;
        this._spec             = opts.spec;
        this._mounter          = opts.mounter          || WidgetMounter.INSTANCE;
        this._WidgetPickerCtor = opts.WidgetPickerCtor || WidgetPicker;
        // Phase 12 + Phase 6 — optional, but the orchestrator always
        // supplies them in production. Tests can pass null for both
        // to exercise the spawn flow without registry/recorder side
        // effects.
        this._tabRegistry      = opts.tabRegistry || null;
        this._keyboard         = opts.keyboard    || null;
        this._recorder         = opts.recorder    || null;
        // Optional model — when supplied, every spawn from this picker
        // also applies a WidgetSpawnedFromPicker event to it so the
        // in-memory virtual-replay state stays in sync with the live
        // MTP. Without this the model is correct at boot but drifts as
        // soon as the user opens a new tab via the picker.
        this._model            = opts.model       || null;
        this._counter          = 0;
    }

    /** Inspect-able state for dev tools (Diligent Secretaries pillar 2). */
    inspect() {
        return {
            singletons: this._liveSingletons(),
            tabsIssued:       this._counter
        };
    }

    /**
     * Open a new tab in a pane, and put the chooser in it.
     *
     * The TAB-PANE owns this. The tab is the desk's, named by it, and is
     * called "New tab" until it is one; its room holds the picker now and the
     * picked widget after, and the dock never learns the tenant changed. The
     * picker is not handed the keyboard: the ROOM is the member of the party,
     * the dock rests the keys in it, and it hands them on to whatever it is
     * holding.
     */
    openInSlot(slotId) {
        const self  = this;
        const tab   = { title: 'New tab' };
        const room  = this._mtp.openTab(slotId, tab);
        if (!room) return null;
        const tabId = tab.id;
        ++this._counter;

        const hostBranch = room.branchFor('chooser');
        hostBranch.activate(Object.freeze({ toString: () => 'chooser:' + tabId }));
        const host = hostBranch.createElement('host', 'div');
        const picker = new this._WidgetPickerCtor(room.branchFor('picker'), {
            entries:     this._spec.entries || [],
            disabledIds: this._liveSingletons(),
            onPick:      function (entry, params) {
                if (params === null) self._goToOpenSingleton(entry, slotId, tabId);
                else self._mutateIntoWidget(slotId, tabId, entry, params);
            },
            onCancel:    function () { self._mtp.removeTab(slotId, tabId); }
        });
        picker.mountInto(host);
        room.setWidget({
            root:    host,
            keyDown: function (ev) { return picker.keyDown(ev); },
            dispose: function () { picker.dispose(); try { hostBranch.dissolve(); } catch (e) {} }
        });

        if (this._mtp.switchTab) this._mtp.switchTab(slotId, tabId);
        if (this._mtp.land) this._mtp.land(slotId, tabId);
        return tabId;
    }


    /**
     * The user asked to go to the singleton already open: the picker's
     * redirector, confirmed (RFC 0066 E3, keyboard §17.3) — so the keys may
     * follow. The live one is found now, not remembered: it may have moved or
     * closed since the chooser opened. Found: shown where it is and handed the
     * keys, and the chooser's tab closes. Gone: opened here instead, in the
     * chooser's own tab, as a pick of it would have been.
     */
    _goToOpenSingleton(entry, slotId, tabId) {
        const openId = this._liveSingletons()[entry.simpleName];
        if (openId && this._mtp.goTo && this._mtp.goTo(openId)) {
            this._mtp.removeTab(slotId, tabId);
            return;
        }
        this._mutateIntoWidget(slotId, tabId, entry, entry.defaults || {});
    }

    /**
     * The chooser picked: the same tab, a new tenant. The room says it is
     * loading, the chip takes the widget's name, and the widget arrives into
     * the room when its module has. Chip, title and place stay the tab's.
     */
    _mutateIntoWidget(slotId, tabId, entry, params) {
        const self = this;
        const tab  = this.findTabObj(tabId);
        if (!tab || !tab.widget) return;
        const room = tab.widget;

        tab.widgetKind         = entry.simpleName;
        tab.widgetInstanceUuid = this._mintUuid(entry.simpleName);
        const uuid = tab.widgetInstanceUuid;
        room.say('Loading ' + entry.label + '…');
        tab.title = entry.label;
        // The tab takes the kind's name and icon; the widget may name itself
        // once it runs, through its host.
        this._mtp.retitle(tabId, entry.label);
        if (this._mtp.setIcon) this._mtp.setIcon(tabId, entry.icon);
        if (this._mtp.switchTab) this._mtp.switchTab(slotId, tabId);

        const wBranch = room.branchFor('w-' + uuid.replace(/[^A-Za-z0-9_-]/g, '_'));
        // Handed UNACTIVATED: the widget activates its own branch, as every
        // component does - it is the widget's, not the room's.

        // The ONE async boundary in this flow: resolve, then mount into the room.
        this._mounter.resolve(entry).then(function (mod) {
            const controller = self._mounter.mount(mod, wBranch, entry, params, room.host());
            room.setWidget(controller);
            tab.controller = controller;
            if (self._tabRegistry) {
                try {
                    self._tabRegistry.register({
                        widgetInstanceUuid: uuid,
                        tab:                tab,
                        slotId:             slotId,
                        widgetKind:         entry.simpleName,
                        controller:         controller
                    });
                } catch (e) { console.error('[PickerTabFlow] tabRegistry.register threw:', e); }
            }
            // Record the real destination: the pane it was spawned into, and its
            // live place in that pane's strip.
            const toPaneId = (self._mtp.paneIdOf && self._mtp.paneIdOf(slotId)) || slotId;
            const rawIdx   = self._mtp.tabIndexOf ? self._mtp.tabIndexOf(slotId, tabId) : -1;
            const spawnPayload = {
                widgetInstanceId: uuid,
                widgetKind:       entry.simpleName,
                title:            entry.label,
                params:           params,
                to: { paneId: toPaneId, tabIndex: rawIdx < 0 ? 0 : rawIdx }
            };
            if (self._model && typeof self._model.apply === 'function') {
                try { self._model.apply({ name: 'WidgetSpawnedFromPicker', payload: spawnPayload }); }
                catch (e) { console.error('[PickerTabFlow] model.apply threw:', e); }
            }
            if (self._recorder && typeof self._recorder.emit === 'function') {
                self._recorder.emit('WidgetSpawnedFromPicker', spawnPayload);
            }
            // If the pane is STILL SHOWING this tab after the async mount, the
            // controller catches up: it did not exist when the tab was made
            // active, so nothing could tell it then.
            if (self._mtp.activeTabOf && self._mtp.activeTabOf(slotId) === tabId) room.setActive(true);
        }).catch(function (err) {
            console.error('[PickerTabFlow] mount failed for', entry.simpleName, ':', err);
            room.say('Failed to load ' + entry.label + ': ' + (err && err.message ? err.message : err));
        });
    }

    /**
     * A widget id no live widget has. The counter starts again with every
     * visit and the widgets restored from the last one keep their ids, so a
     * count alone would hand a new widget the id of one already open.
     */
    _mintUuid(kind) {
        let uuid;
        do { uuid = kind + ':' + (++this._counter); }
        while (this._tabRegistry && this._tabRegistry.lookup(uuid));
        return uuid;
    }

    /**
     * The SINGLETON kinds that are open right now, as kind -> the tab id
     * holding one.
     *
     * Asked of what is live, because a tally kept on the side is only right
     * until something else opens a tab. It was such a tally, written only by
     * this flow's own spawns — so a widget restored by PROJECTION never
     * counted, and after a reload the picker offered a singleton that was
     * already open and a second one was attempted.
     */
    _liveSingletons() {
        const out = {};
        if (!this._tabRegistry || !this._tabRegistry.uuids) return out;
        const single = {};
        for (const e of (this._spec.entries || [])) {
            if (e.lifecycleHint === 'SINGLETON') single[e.simpleName] = true;
        }
        for (const uuid of this._tabRegistry.uuids()) {
            const hit = this._tabRegistry.lookup(uuid);
            if (hit && single[hit.widgetKind]) out[hit.widgetKind] = this._tabRegistry.tabIdOf(uuid) || uuid;
        }
        return out;
    }

    /** Finds the live tab descriptor by id. Reaches through MTP's
     *  _tabsBySlot internal map (public getState returns a flattened
     *  copy missing render/setActive). */
    /** The descriptor a tab was made from - the pane's to hand back, not ours to go looking for. */
    findTabObj(tabId) {
        return this._mtp.tabOf ? this._mtp.tabOf(tabId) : null;
    }
}
