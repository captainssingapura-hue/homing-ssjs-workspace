// =============================================================================
// WorkspaceWriteLock — one writer per log: a page takes its log's lock before
// it records, so that two pages of one workspace never interleave two
// histories in one log. The browser's Web Locks keep it - one exclusive lock
// per log, named WriteLock.NAME_PREFIX/<kind>/<workspace> - and it goes with
// the page when the page goes. What it says is a WriteLock, declared in Java.
//
//   new WorkspaceWriteLock({ log, locks?, onChange? })
//     log       a LogKey: whose lock
//     locks     the lock manager; the browser's navigator.locks unless said,
//               and none there means the page writes unguarded
//     onChange  (WriteLock) → nothing, told each time it changes - TAKEN among them
//   lock.acquire()  → Promise<WriteLock>: HERE when this page took it, ELSEWHERE
//                     when another page holds it, UNGUARDED when there are no locks
//   lock.takeOver() → Promise<WriteLock>: HERE, taken from the page that held it -
//                     which is told TAKEN
//   lock.release()  let go; it is let go with the page too
//   lock.state      the WriteLock it last said
//   WorkspaceWriteLock.writes(writeLock) → whether a page holding it writes the log
//   WorkspaceWriteLock.nameOf(logKey)   → the lock's name
// =============================================================================

class WorkspaceWriteLock {
    constructor(opts) {
        var o = opts || {};
        if (!(o.log instanceof LogKey)) throw new TypeError("[WorkspaceWriteLock] opts.log must be a LogKey");
        this._log = o.log;
        this._locks = o.locks !== undefined ? o.locks : ((typeof navigator !== "undefined" && navigator.locks) || null);
        this._onChange = typeof o.onChange === "function" ? o.onChange : null;
        this._let = null;
        this.state = null;
    }

    static nameOf(log) {
        return WriteLock.NAME_PREFIX + "/" + log.kind.value + "/" + log.workspace.id;
    }

    static writes(lock) {
        return lock !== null && (lock.held === Held.HERE || lock.held === Held.UNGUARDED);
    }

    acquire() { return this._request({ mode: "exclusive", ifAvailable: true }); }

    takeOver() { return this._request({ mode: "exclusive", steal: true }); }

    release() {
        var go = this._let;
        this._let = null;
        if (go) go();
    }

    /** One request for the lock: held until let go or taken, the promise it returns said as TAKEN when taken. */
    _request(options) {
        if (!this._locks) return Promise.resolve(this._say(Held.UNGUARDED));
        var self = this;
        return new Promise(function (resolve) {
            var granted = false;
            var held = self._locks.request(WorkspaceWriteLock.nameOf(self._log), options, function (lock) {
                if (!lock) { resolve(self._say(Held.ELSEWHERE)); return undefined; }
                granted = true;
                resolve(self._say(Held.HERE));
                return new Promise(function (letGo) { self._let = letGo; });
            });
            held.then(null, function () {
                // taken by another page's takeOver - or refused before it was granted
                if (granted) { self._let = null; self._say(Held.TAKEN); }
                else resolve(self._say(Held.ELSEWHERE));
            });
        });
    }

    _say(held) {
        this.state = new WriteLock(this._log, held);
        if (this._onChange) this._onChange(this.state);
        return this.state;
    }
}
