// =============================================================================
// MemoryLog — a WorkspaceLogStore's rows kept in memory, as IndexedDbLog keeps
// them in the database: numbered climbing, never reused, looked up by kind and
// workspace; a log that cannot be read set aside, not cleared. For a page that
// must not keep anything, and for tests.
//
//   new MemoryLog()
//   log.add(row) → Promise<seq>    log.rows(kind, workspaceId) → Promise<rows>
//   log.clear(kind, workspaceId) → Promise<count>
//   log.setAside(kind, workspaceId, make) → Promise<the record kept, or null>
//   log.asides(kind, workspaceId) → Promise<records>, oldest first
//   log.discardAsides(kind, workspaceId) → Promise<count>
// =============================================================================

class MemoryLog {
    constructor() {
        this._rows = [];
        this._last = 0;
        this._asides = [];
        this._lastAside = 0;
    }

    add(row) {
        var seq = ++this._last;
        this._rows.push(Object.freeze(Object.assign({ seq: seq }, JSON.parse(JSON.stringify(row)))));
        return Promise.resolve(seq);
    }

    rows(kind, workspaceId) {
        return Promise.resolve(this._rows.filter(function (r) { return r.kind === kind && r.workspaceId === workspaceId; }));
    }

    clear(kind, workspaceId) {
        var before = this._rows.length;
        this._rows = this._rows.filter(function (r) { return !(r.kind === kind && r.workspaceId === workspaceId); });
        return Promise.resolve(before - this._rows.length);
    }

    setAside(kind, workspaceId, make) {
        var found = this._rows.filter(function (r) { return r.kind === kind && r.workspaceId === workspaceId; });
        if (!found.length) return Promise.resolve(null);
        var kept;
        try { kept = { id: this._lastAside + 1, kind: kind, workspaceId: workspaceId, aside: JSON.parse(JSON.stringify(make(found))) }; }
        catch (e) { return Promise.reject(e); }
        this._lastAside = kept.id;
        this._asides.push(Object.freeze(kept));
        this._rows = this._rows.filter(function (r) { return found.indexOf(r) < 0; });
        return Promise.resolve(kept);
    }

    asides(kind, workspaceId) {
        return Promise.resolve(this._asides.filter(function (a) { return a.kind === kind && a.workspaceId === workspaceId; }));
    }

    discardAsides(kind, workspaceId) {
        var before = this._asides.length;
        this._asides = this._asides.filter(function (a) { return !(a.kind === kind && a.workspaceId === workspaceId); });
        return Promise.resolve(before - this._asides.length);
    }
}
