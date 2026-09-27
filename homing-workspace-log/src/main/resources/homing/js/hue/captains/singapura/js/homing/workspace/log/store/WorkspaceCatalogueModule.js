// =============================================================================
// WorkspaceCatalogue — the workspaces of each kind this browser keeps, as it
// lists them: each a WorkspaceEntry, declared in Java - which log, what it is
// called, when it was first opened and when last. A workspace is listed the
// first time a page writes it, under a name of its own among its kind's - the
// kind's, then "<kind> 2", "<kind> 3", ... - and noted each time a page opens
// it to write. Kept beside the logs, by the same backend; a workspace is read
// and written in one step with its kind's others, so two pages opening two
// workspaces at once never take one name.
//
//   new WorkspaceCatalogue({ backend, now? })
//     backend  an IndexedDbLog or a MemoryLog: entries(kind), writeEntry(kind, workspaceId, make)
//     now      () → milliseconds since the epoch; Date.now unless said
//   catalogue.list(kind)        → Promise<WorkspaceEntry[]>, the latest opened
//                                 first; an entry that does not read is passed
//                                 over, and said
//   catalogue.opened(log)       → Promise<WorkspaceEntry>: listed, the first
//                                 time, under a name of its own; its opening noted
//   catalogue.rename(log, name) → Promise<WorkspaceEntry>; a blank name, or a
//                                 workspace not listed, refused
//   WorkspaceCatalogue.nameFor(kind, taken) → the first of the kind's names not taken
// =============================================================================

class WorkspaceCatalogue {
    constructor(opts) {
        var o = opts || {};
        if (!o.backend) throw new TypeError("[WorkspaceCatalogue] opts.backend is required");
        this._backend = o.backend;
        this._now = typeof o.now === "function" ? o.now : function () { return Date.now(); };
    }

    list(kind) {
        if (!(kind instanceof WorkspaceKind)) return Promise.reject(new TypeError("[WorkspaceCatalogue] list takes a WorkspaceKind, got " + JSON.stringify(kind)));
        return this._backend.entries(kind.value).then(function (kept) {
            return WorkspaceCatalogue._read(kept).sort(function (a, b) { return b.opened - a.opened; });
        });
    }

    opened(log) {
        var at = this._now();
        return this._write(log, "opened", function (mine, others) {
            if (mine) return new WorkspaceEntry(mine.log, mine.name, mine.created, at);
            var taken = others.map(function (e) { return e.name.value; });
            return new WorkspaceEntry(log, new WorkspaceName(WorkspaceCatalogue.nameFor(log.kind, taken)), at, at);
        });
    }

    rename(log, name) {
        var said = String(name == null ? "" : name).trim();
        if (!said) return Promise.reject(new TypeError("[WorkspaceCatalogue] a workspace is not renamed to nothing"));
        return this._write(log, "rename", function (mine) {
            if (!mine) throw new TypeError("[WorkspaceCatalogue] " + log.kind.value + "/" + log.workspace.id + " is not listed, so not renamed");
            return new WorkspaceEntry(mine.log, new WorkspaceName(said), mine.created, mine.opened);
        });
    }

    static nameFor(kind, taken) {
        var base = kind.value;
        if (taken.indexOf(base) < 0) return base;
        for (var n = 2; ; n++) if (taken.indexOf(base + " " + n) < 0) return base + " " + n;
    }

    /** The workspace's entry made from what the kind's hold - make(its own, or null; the others) - and kept, in one step. */
    _write(log, what, make) {
        if (!(log instanceof LogKey)) return Promise.reject(new TypeError("[WorkspaceCatalogue] " + what + " takes a LogKey, got " + JSON.stringify(log)));
        var id = log.workspace.id;
        return this._backend.writeEntry(log.kind.value, id, function (kept) {
            var all = WorkspaceCatalogue._read(kept);
            var mine = all.filter(function (e) { return e.log.workspace.id === id; })[0] || null;
            return WorkspaceEntryCodec.transformTo(make(mine, all.filter(function (e) { return e !== mine; })));
        }).then(function (kept) { return WorkspaceEntryCodec.transformFrom(kept.entry); });
    }

    static _read(kept) {
        var out = [];
        kept.forEach(function (k) {
            try { out.push(WorkspaceEntryCodec.transformFrom(k.entry)); }
            catch (e) { console.warn("[WorkspaceCatalogue] the entry of " + k.kind + "/" + k.workspaceId + " does not read, and is passed over: " + e.message); }
        });
        return out;
    }
}
