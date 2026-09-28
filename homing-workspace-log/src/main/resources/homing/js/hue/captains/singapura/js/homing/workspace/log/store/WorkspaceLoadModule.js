// =============================================================================
// WorkspaceLoad — a workspace's log, read back to what it folds to: from its
// latest checkpoint, folding only what was logged after it; or, there being
// none of this build's rules, from its opening. Every layer at once — the
// roster, the one pane, the split grid — so a page of either placement loads
// alike, and comes back to its own layer.
//
// A checkpoint of other rules, or one that does not read, is not folded on — it
// is only ever derived — and the log is folded whole; the page that writes the
// log drops it. A log that will not read or fold is SET ASIDE, not cleared: kept
// whole, to be exported, and the page starts afresh. Should even that fail, the
// stored log is left as it is and the session is kept in memory only. A page
// that does not write the log leaves it to the one that does.
//
//   WorkspaceLoad.load(log, { writes? }) → Promise<{ log, folded, logged, why }>
//     log      a WorkspaceLogStore
//     writes   whether this page writes the log (holds its write lock); true unless said
//     → log      the store to go on with: the one given, or one in memory when the stored
//                log could be neither read nor set aside
//       folded   the FoldedState it comes back to, or null to start afresh
//       logged   how many events the log holds
//       why      why it starts afresh, or null
// =============================================================================

class WorkspaceLoad {
    static load(log, opts) {
        var writes = !opts || opts.writes !== false;
        function dropped(why) {
            console.warn("[WorkspaceLoad] " + why + ": not folded on, and the log folded whole");
            return writes ? log.dropCheckpoint().then(function () { return null; }) : null;
        }
        function latest() {
            return log.checkpoint().then(function (c) {
                return !c || c.fold === Checkpoint.FOLD ? c : dropped("the checkpoint was folded by rules " + c.fold + ", not " + Checkpoint.FOLD);
            }, function (e) { return dropped("the checkpoint does not read (" + e.message + ")"); });
        }
        function afresh(why) {
            var fresh = { log: log, folded: null, logged: 0, why: why };
            if (!writes) {
                console.warn("[WorkspaceLoad] the stored log does not read, and another page writes it: left to that page - " + why);
                return fresh;
            }
            return log.setAside(why).then(function (aside) {
                console.warn("[WorkspaceLoad] the stored log does not read and is set aside" + (aside ? ", " + aside.lines.length + " lines" : "") + ": " + why);
                return fresh;
            }, function (e) {
                console.error("[WorkspaceLoad] the stored log does not read and could not be set aside, so it is left as it is and this session is kept in memory only: "
                              + (e && e.message));
                fresh.log = new WorkspaceLogStore({ header: log.header, backend: new MemoryLog() });
                return fresh;
            });
        }
        return latest().then(function (c) {
            return log.eventsAfter(c ? c.folded.through.value : 0).then(function (events) {
                var folded;
                try { folded = WorkspaceFold.foldFrom(c ? c.folded : WorkspaceFold.start(log.header), events); }
                catch (e) { return afresh(e.message); }
                return { log: log, folded: folded, logged: (c ? c.events : 0) + events.length, why: null };
            });
        }).then(null, function (e) { return afresh(e && e.message); });
    }
}
