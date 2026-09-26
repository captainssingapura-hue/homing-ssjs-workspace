// =============================================================================
// WorkspaceLogIdentity — whose log a page keeps: the kind of workspace it is
// and the one workspace of that kind — the address's ?workspace=<uuid> when it
// names one, else the kind's own, the same uuid every visit (the seed
// "workspace:<kind>" hashed as Java's WorkspaceInstanceId.placeholderFor
// hashes it).
//
//   WorkspaceLogIdentity.header(kind, search)  → a LogHeader
//     kind    the workspace's kind, "demo"
//     search  the address's query, "?workspace=…", or ""
//   WorkspaceLogIdentity.placeholder(kind)     → the kind's own uuid
// =============================================================================

class WorkspaceLogIdentity {
    static header(kind, search) {
        var asked = WorkspaceLogIdentity._asked(search);
        var id = asked || WorkspaceLogIdentity.placeholder(kind);
        return new LogHeader(LogHeader.FORMAT, LogHeader.VERSION, new WorkspaceSpecKind(kind), new WorkspaceInstanceId(id));
    }

    static _asked(search) {
        var m = /[?&]workspace=([^&#]*)/.exec(search || "");
        if (!m) return null;
        var v = decodeURIComponent(m[1]).toLowerCase();
        return WorkspaceLogIdentity._UUID.test(v) ? v : null;
    }

    static _UUID = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/;

    static placeholder(kind) {
        var seed = "workspace:" + kind, hash = 0;
        for (var i = 0; i < seed.length; i++) hash = ((hash << 5) - hash + seed.charCodeAt(i)) | 0;
        var h = (hash >>> 0).toString(16).padStart(8, "0");
        var high = ((hash | 0x7000) >>> 0).toString(16).padStart(8, "0");
        // high: the hash over 0x7000 in its top word, then 5000-9000; low: the hash, then 00000001
        return high + "-5000-9000-" + h.slice(0, 4) + "-" + h.slice(4) + "00000001";
    }
}
