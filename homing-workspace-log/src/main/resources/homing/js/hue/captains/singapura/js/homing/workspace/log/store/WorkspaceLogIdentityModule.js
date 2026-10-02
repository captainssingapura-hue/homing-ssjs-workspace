// =============================================================================
// WorkspaceLogIdentity — whose log a page keeps: the kind of workspace it is
// and the one workspace of that kind — the one its address names, its ws_id,
// read and checked where the address is read, on the server; else the kind's
// own, the same uuid every visit (the seed "workspace:<kind>" hashed as Java's
// WorkspaceInstanceId.placeholderFor hashes it).
//
//   WorkspaceLogIdentity.header(kind, id)  → a LogHeader
//     kind  the workspace's kind, "notes"
//     id    the workspace the address names, a lowercase uuid; null, or absent,
//           for the kind's own
//   WorkspaceLogIdentity.fresh()           → a new workspace's WorkspaceInstanceId:
//                                            a random uuid, never one a log has had
//   WorkspaceLogIdentity.placeholder(kind) → the kind's own uuid
// =============================================================================

class WorkspaceLogIdentity {
    static header(kind, id) {
        return new LogHeader(LogHeader.FORMAT, LogHeader.VERSION, new WorkspaceKind(kind),
                             new WorkspaceInstanceId(id || WorkspaceLogIdentity.placeholder(kind)));
    }

    static fresh() {
        var c = typeof crypto !== "undefined" ? crypto : null;
        if (c && typeof c.randomUUID === "function") return new WorkspaceInstanceId(c.randomUUID());
        // a version 4 uuid by hand, where the page is not a secure context
        var b = [];
        for (var i = 0; i < 16; i++) b.push(Math.floor(Math.random() * 256));
        if (c && typeof c.getRandomValues === "function") b = Array.from(c.getRandomValues(new Uint8Array(16)));
        b[6] = (b[6] & 0x0f) | 0x40;
        b[8] = (b[8] & 0x3f) | 0x80;
        var h = b.map(function (x) { return x.toString(16).padStart(2, "0"); }).join("");
        return new WorkspaceInstanceId(h.slice(0, 8) + "-" + h.slice(8, 12) + "-" + h.slice(12, 16) + "-" + h.slice(16, 20) + "-" + h.slice(20));
    }

    static placeholder(kind) {
        var seed = "workspace:" + kind, hash = 0;
        for (var i = 0; i < seed.length; i++) hash = ((hash << 5) - hash + seed.charCodeAt(i)) | 0;
        var h = (hash >>> 0).toString(16).padStart(8, "0");
        var high = ((hash | 0x7000) >>> 0).toString(16).padStart(8, "0");
        // high: the hash over 0x7000 in its top word, then 5000-9000; low: the hash, then 00000001
        return high + "-5000-9000-" + h.slice(0, 4) + "-" + h.slice(4) + "00000001";
    }
}
