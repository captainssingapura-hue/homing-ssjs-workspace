// =============================================================================
// WorkspaceLogStore — one workspace's log, in the browser: the same store the
// Java side keeps in memory. It takes TYPED events only — instances of the
// generated WorkspaceEvent classes, never a plain object — keeps each as its
// codec's wire form, numbered by the backend, climbing and never reused, stamped to the
// millisecond, and gives them back typed, through the same codecs.
//
//   new WorkspaceLogStore({ header, backend, now? })
//     header   a LogHeader: whose log — its kind and workspace key the rows
//     backend  where the rows are kept: an IndexedDbLog, or a MemoryLog —
//              add(row) → Promise<seq>, rows(kind, workspaceId) → Promise<rows
//              in seq order>, clear(kind, workspaceId) → Promise<count>, and
//              setAside / asides / discardAsides, as IndexedDbLog says
//     now      () → milliseconds since the epoch; Date.now unless said
//
//   store.header
//   store.append(event)  → Promise<LoggedEvent>; a value that is not a
//                          WorkspaceEvent is refused before anything is kept
//   store.events()       → Promise<LoggedEvent[]>, in order; a row the codecs
//                          refuse rejects it, naming the row's seq
//   store.clear()        → Promise<count>
//   store.setAside(why)  → Promise<SetAsideLog, or null when the log is empty>:
//                          the log moved aside, not lost - its lines as they
//                          were kept, whether or not they read - and the log
//                          left empty, in one step
//   store.asides()       → Promise<SetAsideLog[]>, oldest first
//   store.discardAsides() → Promise<count>
// =============================================================================

class WorkspaceLogStore {
    constructor(opts) {
        var o = opts || {};
        if (!(o.header instanceof LogHeader)) throw new TypeError("[WorkspaceLogStore] opts.header must be a LogHeader");
        if (!o.backend) throw new TypeError("[WorkspaceLogStore] opts.backend is required");
        this.header = o.header;
        this._backend = o.backend;
        this._now = typeof o.now === "function" ? o.now : function () { return Date.now(); };
        this._kind = o.header.kind.value;
        this._workspaceId = o.header.workspaceId.id;
    }

    append(event) {
        if (!(event instanceof WorkspaceEvent)) {
            return Promise.reject(new TypeError("[WorkspaceLogStore] append takes a WorkspaceEvent, got " + JSON.stringify(event)));
        }
        var at = this._now(), wire = WorkspaceEventCodec.transformTo(event);
        return this._backend.add({ kind: this._kind, workspaceId: this._workspaceId, at: at, event: wire })
            .then(function (seq) { return new LoggedEvent(new EventSeq(seq), at, event); });
    }

    events() {
        return this._backend.rows(this._kind, this._workspaceId).then(function (rows) {
            return rows.map(function (r) {
                try { return LoggedEventCodec.transformFrom({ seq: r.seq, at: r.at, event: r.event }); }
                catch (e) { throw new TypeError("[WorkspaceLogStore] the row at seq " + r.seq + " is not a logged event: " + e.message); }
            });
        });
    }

    clear() { return this._backend.clear(this._kind, this._workspaceId); }

    setAside(why) {
        var header = this.header, at = this._now();
        return this._backend.setAside(this._kind, this._workspaceId, function (rows) {
            var lines = rows.map(function (r) { return JSON.stringify({ seq: r.seq, at: r.at, event: r.event }); });
            return SetAsideLogCodec.transformTo(new SetAsideLog(header, at, String(why), lines));
        }).then(function (kept) { return kept ? SetAsideLogCodec.transformFrom(kept.aside) : null; });
    }

    asides() {
        return this._backend.asides(this._kind, this._workspaceId).then(function (kept) {
            return kept.map(function (k) { return SetAsideLogCodec.transformFrom(k.aside); });
        });
    }

    discardAsides() { return this._backend.discardAsides(this._kind, this._workspaceId); }
}
