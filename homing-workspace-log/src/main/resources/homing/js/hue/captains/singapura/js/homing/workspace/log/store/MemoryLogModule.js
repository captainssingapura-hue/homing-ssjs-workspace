// =============================================================================
// MemoryLog — a WorkspaceLogStore's rows kept in memory, as IndexedDbLog keeps
// them in the database: numbered from one, never reused, looked up by kind and
// workspace. For a page that must not keep anything, and for tests.
//
//   new MemoryLog()
//   log.add(row) → Promise<seq>    log.rows(kind, workspaceId) → Promise<rows>
//   log.clear(kind, workspaceId) → Promise<count>
// =============================================================================

class MemoryLog {
    constructor() {
        this._rows = [];
        this._last = 0;
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
}
