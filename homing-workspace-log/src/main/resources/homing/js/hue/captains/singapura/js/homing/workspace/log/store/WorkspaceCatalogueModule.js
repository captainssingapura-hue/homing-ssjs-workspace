// =============================================================================
// WorkspaceCatalogue — the workspaces of each kind this browser keeps, as it
// lists them: each a WorkspaceEntry, declared in Java - which log, what it is
// called, when it was first opened and when last. A workspace is listed the
// first time a page writes it, under a name of its own among its kind's - the
// one asked for, or the kind's, then "<kind> 2", "<kind> 3", ... - and noted
// each time a page opens it to write. Kept beside the logs, by the same
// backend; a workspace is read and written in one step with its kind's others,
// so two pages opening two workspaces at once never take one name.
//
// A NAME IS ONE WORKSPACE'S among its kind's: another of the kind already
// called so - its case aside - refuses it, to a new one and to a rename alike.
//
// A DELETE IS SOFT: the workspace's entry leaves the list for the deleted,
// with when, and nothing else of it is touched - its log, its checkpoint, its
// address stay. Opened again, it is listed again as it was, under its name
// when that is still free, else the kind's next.
//
//   new WorkspaceCatalogue({ backend, now? })
//     backend  an IndexedDbLog or a MemoryLog: entries(kind), writeEntry(kind, workspaceId, make),
//              deleteEntry(kind, workspaceId, at), deletedEntries(kind), restoreEntry(kind, workspaceId, make)
//     now      () → milliseconds since the epoch; Date.now unless said
//   catalogue.list(kind)          → Promise<WorkspaceEntry[]>, the latest opened
//                                   first; an entry that does not read is passed
//                                   over, and said
//   catalogue.opened(log, name?)  → Promise<WorkspaceEntry>: listed, the first time,
//                                   under the name asked for - refused when another
//                                   of its kind has it - or one of its own; a deleted
//                                   one listed again; its opening noted
//   catalogue.rename(log, name)   → Promise<WorkspaceEntry>; a blank name, a name
//                                   another of its kind has, or a workspace not
//                                   listed, refused
//   catalogue.remove(log)         → Promise<WorkspaceEntry>: out of the list, into the
//                                   deleted; one not listed, refused
//   catalogue.deleted(kind)       → Promise<[{ entry, deleted }]>, the latest deleted first
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

    opened(log, name) {
        var at = this._now(), self = this, asked = String(name == null ? "" : name).trim();
        if (!(log instanceof LogKey)) return Promise.reject(new TypeError("[WorkspaceCatalogue] opened takes a LogKey, got " + JSON.stringify(log)));
        return this._backend.restoreEntry(log.kind.value, log.workspace.id, function (was, listed) {
            var it = WorkspaceEntryCodec.transformFrom(was.entry), others = WorkspaceCatalogue._read(listed);
            var named = WorkspaceCatalogue._holds(others, it.name.value) ? WorkspaceCatalogue.nameFor(log.kind, WorkspaceCatalogue._names(others)) : it.name.value;
            return WorkspaceEntryCodec.transformTo(new WorkspaceEntry(it.log, new WorkspaceName(named), it.created, at));
        }).then(function (back) {
            if (back) return WorkspaceEntryCodec.transformFrom(back.entry);
            return self._write(log, "opened", function (mine, others) {
                if (mine) return new WorkspaceEntry(mine.log, mine.name, mine.created, at);
                if (asked && WorkspaceCatalogue._holds(others, asked)) throw new TypeError(WorkspaceCatalogue._taken(log.kind, others, asked));
                return new WorkspaceEntry(log, new WorkspaceName(asked || WorkspaceCatalogue.nameFor(log.kind, WorkspaceCatalogue._names(others))), at, at);
            });
        });
    }

    rename(log, name) {
        var said = String(name == null ? "" : name).trim();
        if (!said) return Promise.reject(new TypeError("[WorkspaceCatalogue] a workspace is not renamed to nothing"));
        return this._write(log, "rename", function (mine, others) {
            if (!mine) throw new TypeError("[WorkspaceCatalogue] " + log.kind.value + "/" + log.workspace.id + " is not listed, so not renamed");
            if (WorkspaceCatalogue._holds(others, said)) throw new TypeError(WorkspaceCatalogue._taken(log.kind, others, said));
            return new WorkspaceEntry(mine.log, new WorkspaceName(said), mine.created, mine.opened);
        });
    }

    remove(log) {
        if (!(log instanceof LogKey)) return Promise.reject(new TypeError("[WorkspaceCatalogue] remove takes a LogKey, got " + JSON.stringify(log)));
        return this._backend.deleteEntry(log.kind.value, log.workspace.id, this._now()).then(function (moved) {
            if (!moved) throw new TypeError("[WorkspaceCatalogue] " + log.kind.value + "/" + log.workspace.id + " is not listed, so not deleted");
            return WorkspaceEntryCodec.transformFrom(moved.entry);
        });
    }

    deleted(kind) {
        if (!(kind instanceof WorkspaceKind)) return Promise.reject(new TypeError("[WorkspaceCatalogue] deleted takes a WorkspaceKind, got " + JSON.stringify(kind)));
        return this._backend.deletedEntries(kind.value).then(function (kept) {
            var out = [];
            kept.forEach(function (k) {
                try { out.push(Object.freeze({ entry: WorkspaceEntryCodec.transformFrom(k.entry), deleted: k.deleted })); }
                catch (e) { console.warn("[WorkspaceCatalogue] the deleted entry of " + k.kind + "/" + k.workspaceId + " does not read, and is passed over: " + e.message); }
            });
            return out.sort(function (a, b) { return b.deleted - a.deleted; });
        });
    }

    /** The first of the kind's names none of the taken is, their case aside. */
    static nameFor(kind, taken) {
        var base = kind.value, held = taken.map(function (t) { return String(t).trim().toLowerCase(); });
        if (held.indexOf(base.toLowerCase()) < 0) return base;
        for (var n = 2; ; n++) if (held.indexOf((base + " " + n).toLowerCase()) < 0) return base + " " + n;
    }

    static _names(entries) { return entries.map(function (e) { return e.name.value; }); }

    /** Whether one of these is called so already, its case aside. */
    static _holds(entries, name) {
        var n = String(name).trim().toLowerCase();
        return entries.some(function (e) { return e.name.value.trim().toLowerCase() === n; });
    }

    /** Refused: the name as the workspace that has it is called - its case its own. */
    static _taken(kind, entries, name) {
        var n = String(name).trim().toLowerCase(), holder = entries.filter(function (e) { return e.name.value.trim().toLowerCase() === n; })[0];
        return "a " + kind.value + " workspace is called “" + (holder ? holder.name.value : name) + "” already";
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
