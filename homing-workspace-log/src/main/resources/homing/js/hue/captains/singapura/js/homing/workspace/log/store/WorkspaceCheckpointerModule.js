// =============================================================================
// WorkspaceCheckpointer — a log's checkpoints, taken as it goes: every so many
// events, what was logged since the last checkpoint is folded on from it - by
// the checkpoint worker, off this thread - and kept as the log's checkpoint;
// and, when the page was given a server that takes them, posted there too. The
// page reads the log and writes the checkpoint; the worker only folds.
//
//   new WorkspaceCheckpointer({ store, worker, every?, upload?, post? })
//     store   the WorkspaceLogStore
//     worker  where the fold runs: a Worker running CheckpointWorkerModule, or
//             anything that answers its messages as one does
//     every   events between checkpoints; WorkspaceCheckpointer.EVERY unless said
//     upload  the address a checkpoint is posted to as well, or none
//     post    (address, text) → Promise; fetch unless said
//   cp.recorded(n)  the page has recorded n events in all: a checkpoint is taken
//                   once `every` have come since the last one began
//   cp.take()       → Promise<the Checkpoint kept, or null when nothing was new>;
//                   one at a time - a second asked for meanwhile is the first
//   cp.dispose()    the worker let go
// =============================================================================

class WorkspaceCheckpointer {
    static EVERY = 50;

    constructor(opts) {
        var o = opts || {};
        if (!o.store || !o.worker) throw new TypeError("[WorkspaceCheckpointer] opts.store and opts.worker are required");
        this._store = o.store;
        this._worker = o.worker;
        this._every = o.every || WorkspaceCheckpointer.EVERY;
        this._upload = o.upload || null;
        this._post = o.post || function (address, text) {
            return fetch(address, { method: "POST", headers: { "Content-Type": "application/json" }, body: text })
                .then(function (r) { if (!r.ok) throw new Error("the server answered " + r.status); return r; });
        };
        this._pending = new Map();
        this._nextId = 1;
        this._busy = null;
        this._recorded = 0;
        this._since = 0;
        this._told = false;
        var self = this;
        this._worker.onmessage = function (ev) {
            var m = ev.data || {}, p = self._pending.get(m.id);
            if (!p) return;
            self._pending.delete(m.id);
            if (m.error) p.reject(new Error("[WorkspaceCheckpointer] the fold refused: " + m.error));
            else p.resolve(m.checkpoint);
        };
        this._worker.onerror = function (e) {
            self._pending.forEach(function (p) { p.reject(new Error("[WorkspaceCheckpointer] the worker failed: " + ((e && e.message) || e))); });
            self._pending.clear();
        };
    }

    recorded(n) {
        this._since += n - this._recorded;
        this._recorded = n;
        if (this._since >= this._every && !this._busy) this.take().then(null, function () {});
    }

    take() {
        if (this._busy) return this._busy;
        var self = this, store = this._store;
        this._since = 0;
        this._busy = store.checkpoint().then(null, function () { return null; })
            .then(function (previous) {
                if (previous && previous.fold !== Checkpoint.FOLD) previous = null;
                return store.linesAfter(previous ? previous.folded.through.value : 0).then(function (lines) {
                    if (!lines.length) return null;
                    return self._fold({ header: LogHeaderCodec.transformTo(store.header),
                                        previous: previous ? CheckpointCodec.transformTo(previous) : null, events: lines });
                });
            })
            .then(function (wire) {
                if (wire === null) return null;
                var checkpoint = CheckpointCodec.transformFrom(wire);
                return store.putCheckpoint(checkpoint).then(function (kept) {
                    if (kept && self._upload) self._send(wire);
                    return checkpoint;
                });
            })
            .then(function (checkpoint) { self._done(); return checkpoint; },
                  function (e) { self._done(); console.warn("[WorkspaceCheckpointer] no checkpoint: " + (e && e.message)); throw e; });
        return this._busy;
    }

    /** Free for the next; and if enough came in meanwhile, the next taken now. */
    _done() {
        this._busy = null;
        if (this._since >= this._every) this.take().then(null, function () {});
    }

    _fold(message) {
        var self = this, id = this._nextId++;
        return new Promise(function (resolve, reject) {
            self._pending.set(id, { resolve: resolve, reject: reject });
            try { self._worker.postMessage(Object.assign({ id: id }, message)); }
            catch (e) { self._pending.delete(id); reject(e); }
        });
    }

    /** Posted to the server; a server that does not take it is said once, and the page goes on. */
    _send(wire) {
        var self = this;
        return this._post(this._upload, JSON.stringify(wire)).then(null, function (e) {
            if (!self._told) console.warn("[WorkspaceCheckpointer] the server did not take the checkpoint: " + ((e && e.message) || e));
            self._told = true;
        });
    }

    dispose() {
        this._pending.forEach(function (p) { p.reject(new Error("[WorkspaceCheckpointer] disposed")); });
        this._pending.clear();
        if (typeof this._worker.terminate === "function") this._worker.terminate();
    }
}
