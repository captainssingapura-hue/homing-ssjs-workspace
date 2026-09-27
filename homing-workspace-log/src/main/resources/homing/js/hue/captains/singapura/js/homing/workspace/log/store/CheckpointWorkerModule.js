// =============================================================================
// CheckpointWorker — a checkpoint's fold, off the page's thread: a module
// worker, the whole of whose work is CheckpointFold.next. It keeps nothing and
// touches no store - the page reads the log and writes what comes back, so the
// page stays the log's one writer. Everything crosses as wire form, the only
// form structured cloning keeps faithfully, and is read here by the codecs.
//
//   in   { id, header, previous, events }
//          header    a LogHeader's wire
//          previous  a Checkpoint's wire, of this build's rules, or null
//          events    the LoggedEvents after it, as their wire
//   out  { id, checkpoint }  the next Checkpoint's wire
//        { id, error }       why there is none: a line that does not read, an
//                            event that cannot be where it falls
// =============================================================================

self.onmessage = function (ev) {
    var m = ev.data || {};
    try {
        var previous = m.previous === null ? null : CheckpointCodec.transformFrom(m.previous);
        var events = m.events.map(function (w) { return LoggedEventCodec.transformFrom(w); });
        var next = CheckpointFold.next(previous, LogHeaderCodec.transformFrom(m.header), events);
        self.postMessage({ id: m.id, checkpoint: CheckpointCodec.transformTo(next) });
    } catch (e) {
        self.postMessage({ id: m.id, error: String((e && e.message) || e) });
    }
};
