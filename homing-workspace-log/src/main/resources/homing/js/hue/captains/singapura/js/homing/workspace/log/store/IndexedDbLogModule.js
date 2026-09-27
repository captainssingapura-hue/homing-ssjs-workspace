// =============================================================================
// IndexedDbLog — where a WorkspaceLogStore keeps its rows in the browser: the
// IndexedDB database "homing.workspace.log", one object store of rows numbered
// by the database, climbing and never reused - a clear does not wind the count
// back - looked up by (kind, workspaceId, seq). A row is
// { seq, kind, workspaceId, at, event }, the event in its codec's wire form —
// plain data, which is all the database keeps faithfully.
//
// Beside the rows: "checkpoints", one per log - { kind, workspaceId, through,
// checkpoint }, the latest the page folded, never replaced by an older one -
// and "aside", where a log the page could not read is moved, in one
// transaction, as one record { id, kind, workspaceId, aside }. Clearing a log
// or setting it aside drops its checkpoint with it.
//
//   new IndexedDbLog({ indexedDB? })   the factory; the page's own unless said
//   log.add(row)                  → Promise<seq>
//   log.rows(kind, workspaceId)   → Promise<rows>, in seq order
//   log.rowsAfter(kind, workspaceId, seq) → Promise<the rows after seq>, in order
//   log.clear(kind, workspaceId)  → Promise<count>
//   log.checkpoint(kind, workspaceId) → Promise<its record, or null>
//   log.putCheckpoint(kind, workspaceId, through, checkpoint)
//                                 → Promise<whether it was kept: not when the
//                                   log's is through as far already>
//   log.dropCheckpoint(kind, workspaceId) → Promise
//   log.setAside(kind, workspaceId, make)
//                                 → Promise<the record kept, or null when there
//                                   were no rows>; make(rows) → what is kept
//   log.asides(kind, workspaceId) → Promise<records>, oldest first
//   log.discardAsides(kind, workspaceId) → Promise<count>
// =============================================================================

class IndexedDbLog {
    static DB_NAME = "homing.workspace.log";
    static STORE = "events";
    static ASIDE = "aside";
    static CHECKPOINTS = "checkpoints";
    /** 2: the aside store joined the rows; 3: the checkpoints. */
    static VERSION = 3;

    constructor(opts) {
        this._factory = (opts && opts.indexedDB) || indexedDB;
        this._opened = null;
    }

    _db() {
        if (this._opened) return this._opened;
        var factory = this._factory;
        this._opened = new Promise(function (resolve, reject) {
            var req = factory.open(IndexedDbLog.DB_NAME, IndexedDbLog.VERSION);
            req.onupgradeneeded = function () {
                var db = req.result;
                if (!db.objectStoreNames.contains(IndexedDbLog.STORE)) {
                    var store = db.createObjectStore(IndexedDbLog.STORE, { keyPath: "seq", autoIncrement: true });
                    store.createIndex("by_workspace", ["kind", "workspaceId", "seq"], { unique: true });
                }
                if (!db.objectStoreNames.contains(IndexedDbLog.ASIDE)) {
                    var aside = db.createObjectStore(IndexedDbLog.ASIDE, { keyPath: "id", autoIncrement: true });
                    aside.createIndex("by_workspace", ["kind", "workspaceId", "id"], { unique: true });
                }
                if (!db.objectStoreNames.contains(IndexedDbLog.CHECKPOINTS)) {
                    db.createObjectStore(IndexedDbLog.CHECKPOINTS, { keyPath: ["kind", "workspaceId"] });
                }
            };
            req.onsuccess = function () { resolve(req.result); };
            req.onerror = function () { reject(req.error); };
        });
        return this._opened;
    }

    _range(kind, workspaceId, after) {
        return IDBKeyRange.bound([kind, workspaceId, after ? after + 1 : 0], [kind, workspaceId, Number.MAX_SAFE_INTEGER]);
    }

    /** One transaction over the stores named: body(tx, done) sets what it resolves to; it resolves when the transaction completes. */
    _tx(stores, mode, body) {
        return this._db().then(function (db) {
            return new Promise(function (resolve, reject) {
                var tx = db.transaction(stores, mode), out = { value: undefined };
                tx.oncomplete = function () { resolve(out.value); };
                tx.onerror = function () { reject(tx.error); };
                tx.onabort = function () { reject(tx.error || out.why || new Error("[IndexedDbLog] the transaction was aborted")); };
                try { body(tx, out); }
                catch (e) { out.why = e; tx.abort(); }
            });
        });
    }

    /** Every key in the index's range deleted, counted into out.value. */
    static _deleteAll(store, range, out) {
        out.value = 0;
        var req = store.index("by_workspace").openKeyCursor(range);
        req.onsuccess = function () {
            var c = req.result;
            if (!c) return;
            store.delete(c.primaryKey);
            out.value++;
            c.continue();
        };
    }

    add(row) {
        return this._tx(IndexedDbLog.STORE, "readwrite", function (tx, out) {
            var req = tx.objectStore(IndexedDbLog.STORE).add(row);
            req.onsuccess = function () { out.value = req.result; };
        });
    }

    rows(kind, workspaceId) { return this.rowsAfter(kind, workspaceId, 0); }

    rowsAfter(kind, workspaceId, seq) {
        var range = this._range(kind, workspaceId, seq);
        return this._tx(IndexedDbLog.STORE, "readonly", function (tx, out) {
            var req = tx.objectStore(IndexedDbLog.STORE).index("by_workspace").getAll(range);
            req.onsuccess = function () { out.value = req.result; };
        });
    }

    clear(kind, workspaceId) {
        var range = this._range(kind, workspaceId);
        return this._tx([IndexedDbLog.STORE, IndexedDbLog.CHECKPOINTS], "readwrite", function (tx, out) {
            tx.objectStore(IndexedDbLog.CHECKPOINTS).delete([kind, workspaceId]);
            IndexedDbLog._deleteAll(tx.objectStore(IndexedDbLog.STORE), range, out);
        });
    }

    checkpoint(kind, workspaceId) {
        return this._tx(IndexedDbLog.CHECKPOINTS, "readonly", function (tx, out) {
            var req = tx.objectStore(IndexedDbLog.CHECKPOINTS).get([kind, workspaceId]);
            req.onsuccess = function () { out.value = req.result || null; };
        });
    }

    putCheckpoint(kind, workspaceId, through, checkpoint) {
        return this._tx(IndexedDbLog.CHECKPOINTS, "readwrite", function (tx, out) {
            var store = tx.objectStore(IndexedDbLog.CHECKPOINTS), req = store.get([kind, workspaceId]);
            req.onsuccess = function () {
                out.value = !req.result || req.result.through < through;
                if (out.value) store.put({ kind: kind, workspaceId: workspaceId, through: through, checkpoint: checkpoint });
            };
        });
    }

    dropCheckpoint(kind, workspaceId) {
        return this._tx(IndexedDbLog.CHECKPOINTS, "readwrite", function (tx) {
            tx.objectStore(IndexedDbLog.CHECKPOINTS).delete([kind, workspaceId]);
        });
    }

    setAside(kind, workspaceId, make) {
        var range = this._range(kind, workspaceId);
        return this._tx([IndexedDbLog.STORE, IndexedDbLog.ASIDE, IndexedDbLog.CHECKPOINTS], "readwrite", function (tx, out) {
            var rows = tx.objectStore(IndexedDbLog.STORE), req = rows.index("by_workspace").getAll(range);
            out.value = null;
            req.onsuccess = function () {
                var found = req.result;
                if (!found.length) return;
                var kept;
                try { kept = { kind: kind, workspaceId: workspaceId, aside: make(found) }; }
                catch (e) { out.why = e; tx.abort(); return; }
                var add = tx.objectStore(IndexedDbLog.ASIDE).add(kept);
                add.onsuccess = function () { kept.id = add.result; out.value = kept; };
                found.forEach(function (r) { rows.delete(r.seq); });
                tx.objectStore(IndexedDbLog.CHECKPOINTS).delete([kind, workspaceId]);
            };
        });
    }

    asides(kind, workspaceId) {
        var range = this._range(kind, workspaceId);
        return this._tx(IndexedDbLog.ASIDE, "readonly", function (tx, out) {
            var req = tx.objectStore(IndexedDbLog.ASIDE).index("by_workspace").getAll(range);
            req.onsuccess = function () { out.value = req.result; };
        });
    }

    discardAsides(kind, workspaceId) {
        var range = this._range(kind, workspaceId);
        return this._tx(IndexedDbLog.ASIDE, "readwrite", function (tx, out) {
            IndexedDbLog._deleteAll(tx.objectStore(IndexedDbLog.ASIDE), range, out);
        });
    }
}
