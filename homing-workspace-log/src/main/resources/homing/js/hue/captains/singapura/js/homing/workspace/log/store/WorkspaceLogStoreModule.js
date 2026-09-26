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
//              in seq order>, clear(kind, workspaceId) → Promise<count>
//     now      () → milliseconds since the epoch; Date.now unless said
//
//   store.header
//   store.append(event)  → Promise<LoggedEvent>; a value that is not a
//                          WorkspaceEvent is refused before anything is kept
//   store.events()       → Promise<LoggedEvent[]>, in order; a row the codecs
//                          refuse rejects it, naming the row's seq
//   store.clear()        → Promise<count>
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
}
