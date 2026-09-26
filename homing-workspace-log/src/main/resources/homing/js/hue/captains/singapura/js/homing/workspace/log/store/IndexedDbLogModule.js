// =============================================================================
// IndexedDbLog — where a WorkspaceLogStore keeps its rows in the browser: the
// IndexedDB database "homing.workspace.log", one object store of rows numbered
// by the database from one, looked up by (kind, workspaceId, seq). A row is
// { seq, kind, workspaceId, at, event }, the event in its codec's wire form —
// plain data, which is all the database keeps faithfully.
//
//   new IndexedDbLog({ indexedDB? })   the factory; the page's own unless said
//   log.add(row)                  → Promise<seq>
//   log.rows(kind, workspaceId)   → Promise<rows>, in seq order
//   log.clear(kind, workspaceId)  → Promise<count>
// =============================================================================

class IndexedDbLog {
    static DB_NAME = "homing.workspace.log";
    static STORE = "events";
    static VERSION = 1;

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
            };
            req.onsuccess = function () { resolve(req.result); };
            req.onerror = function () { reject(req.error); };
        });
        return this._opened;
    }

    _range(kind, workspaceId) {
        return IDBKeyRange.bound([kind, workspaceId, 0], [kind, workspaceId, Number.MAX_SAFE_INTEGER]);
    }

    add(row) {
        return this._db().then(function (db) {
            return new Promise(function (resolve, reject) {
                var tx = db.transaction(IndexedDbLog.STORE, "readwrite");
                var req = tx.objectStore(IndexedDbLog.STORE).add(row);
                tx.oncomplete = function () { resolve(req.result); };
                tx.onerror = function () { reject(tx.error || req.error); };
                tx.onabort = function () { reject(tx.error || new Error("[IndexedDbLog] the write was aborted")); };
            });
        });
    }

    rows(kind, workspaceId) {
        var range = this._range(kind, workspaceId);
        return this._db().then(function (db) {
            return new Promise(function (resolve, reject) {
                var tx = db.transaction(IndexedDbLog.STORE, "readonly");
                var req = tx.objectStore(IndexedDbLog.STORE).index("by_workspace").getAll(range);
                req.onsuccess = function () { resolve(req.result); };
                req.onerror = function () { reject(req.error); };
            });
        });
    }

    clear(kind, workspaceId) {
        var range = this._range(kind, workspaceId);
        return this._db().then(function (db) {
            return new Promise(function (resolve, reject) {
                var tx = db.transaction(IndexedDbLog.STORE, "readwrite");
                var store = tx.objectStore(IndexedDbLog.STORE), n = 0;
                var req = store.index("by_workspace").openKeyCursor(range);
                req.onsuccess = function () {
                    var c = req.result;
                    if (!c) return;
                    store.delete(c.primaryKey);
                    n++;
                    c.continue();
                };
                tx.oncomplete = function () { resolve(n); };
                tx.onerror = function () { reject(tx.error); };
            });
        });
    }
}
