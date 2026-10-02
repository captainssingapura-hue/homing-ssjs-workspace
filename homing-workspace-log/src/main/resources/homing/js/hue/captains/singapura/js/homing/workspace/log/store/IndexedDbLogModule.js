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
// or setting it aside drops its checkpoint with it. And "catalogue", the
// workspaces of each kind - { kind, workspaceId, entry }, one per workspace,
// kept whatever becomes of its log. And "deleted", the workspaces taken out of
// the catalogue - { kind, workspaceId, entry, deleted } - a soft delete: the
// log, the checkpoint and the address stay, and it can be listed again.
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
//   log.entries(kind)             → Promise<the kind's catalogue records>
//   log.writeEntry(kind, workspaceId, make)
//                                 → Promise<the record kept>; make(the kind's
//                                   records) → the workspace's entry, read and
//                                   written in one transaction
//   log.deleteEntry(kind, workspaceId, at)
//                                 → Promise<the record moved, or null when it was not
//                                   listed>: the entry out of the catalogue into
//                                   "deleted", with when - its log untouched - in one
//                                   transaction
//   log.deletedEntries(kind)      → Promise<the kind's deleted records>
//   log.restoreEntry(kind, workspaceId, make)
//                                 → Promise<the record kept, or null when it was not
//                                   deleted>; make(its deleted record, the kind's listed
//                                   records) → its entry, back in the catalogue, in one
//                                   transaction
// =============================================================================

class IndexedDbLog {
    static DB_NAME = "homing.workspace.log";
    static STORE = "events";
    static ASIDE = "aside";
    static CHECKPOINTS = "checkpoints";
    static CATALOGUE = "catalogue";
    static DELETED = "deleted";
    /** 2: the aside store joined the rows; 3: the checkpoints; 4: the catalogue; 5: the deleted, apart from it. */
    static VERSION = 5;

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
                if (!db.objectStoreNames.contains(IndexedDbLog.DELETED)) {
                    db.createObjectStore(IndexedDbLog.DELETED, { keyPath: ["kind", "workspaceId"] });
                }
                if (!db.objectStoreNames.contains(IndexedDbLog.CATALOGUE)) {
                    db.createObjectStore(IndexedDbLog.CATALOGUE, { keyPath: ["kind", "workspaceId"] });
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

    /** Every catalogue key of the kind: a workspace id is a lowercase uuid, under the last code unit. */
    static _ofKind(kind) { return IDBKeyRange.bound([kind, ""], [kind, "￿"]); }

    entries(kind) {
        return this._tx(IndexedDbLog.CATALOGUE, "readonly", function (tx, out) {
            var req = tx.objectStore(IndexedDbLog.CATALOGUE).getAll(IndexedDbLog._ofKind(kind));
            req.onsuccess = function () { out.value = req.result; };
        });
    }

    writeEntry(kind, workspaceId, make) {
        return this._tx(IndexedDbLog.CATALOGUE, "readwrite", function (tx, out) {
            var store = tx.objectStore(IndexedDbLog.CATALOGUE), req = store.getAll(IndexedDbLog._ofKind(kind));
            req.onsuccess = function () {
                var kept;
                try { kept = { kind: kind, workspaceId: workspaceId, entry: make(req.result) }; }
                catch (e) { out.why = e; tx.abort(); return; }
                store.put(kept);
                out.value = kept;
            };
        });
    }

    deleteEntry(kind, workspaceId, at) {
        return this._tx([IndexedDbLog.CATALOGUE, IndexedDbLog.DELETED], "readwrite", function (tx, out) {
            var listed = tx.objectStore(IndexedDbLog.CATALOGUE), req = listed.get([kind, workspaceId]);
            req.onsuccess = function () {
                if (!req.result) { out.value = null; return; }
                var moved = { kind: kind, workspaceId: workspaceId, entry: req.result.entry, deleted: at };
                tx.objectStore(IndexedDbLog.DELETED).put(moved);
                listed.delete([kind, workspaceId]);
                out.value = moved;
            };
        });
    }

    deletedEntries(kind) {
        return this._tx(IndexedDbLog.DELETED, "readonly", function (tx, out) {
            var req = tx.objectStore(IndexedDbLog.DELETED).getAll(IndexedDbLog._ofKind(kind));
            req.onsuccess = function () { out.value = req.result; };
        });
    }

    restoreEntry(kind, workspaceId, make) {
        return this._tx([IndexedDbLog.CATALOGUE, IndexedDbLog.DELETED], "readwrite", function (tx, out) {
            var gone = tx.objectStore(IndexedDbLog.DELETED), req = gone.get([kind, workspaceId]);
            req.onsuccess = function () {
                var was = req.result;
                if (!was) { out.value = null; return; }
                var listed = tx.objectStore(IndexedDbLog.CATALOGUE), all = listed.getAll(IndexedDbLog._ofKind(kind));
                all.onsuccess = function () {
                    var kept;
                    try { kept = { kind: kind, workspaceId: workspaceId, entry: make(was, all.result) }; }
                    catch (e) { out.why = e; tx.abort(); return; }
                    listed.put(kept);
                    gone.delete([kind, workspaceId]);
                    out.value = kept;
                };
            };
        });
    }
}
