// =============================================================================
// CheckpointFold — the next checkpoint of a log: the events logged since the
// last one folded on from it - or, there being none, from the log's opening. A
// periodic fold, and no more: what it makes is what the whole log folds to,
// through the last of the events given. Java's CheckpointFold, transcribed.
//
//   CheckpointFold.next(previous, header, events) → a Checkpoint
//     previous  the last checkpoint, of this build's rules (Checkpoint.FOLD), or null
//     header    whose log: a LogHeader
//     events    the LoggedEvents logged after the previous checkpoint, in order
// =============================================================================

class CheckpointFold {
    static next(previous, header, events) {
        var from = WorkspaceFold.start(header), before = 0;
        if (previous !== null) {
            if (previous.fold !== Checkpoint.FOLD) {
                throw new Error("[CheckpointFold] a checkpoint of rules " + previous.fold + " cannot be folded on by rules " + Checkpoint.FOLD);
            }
            var theirs = JSON.stringify(LogHeaderCodec.transformTo(previous.folded.header));
            if (theirs !== JSON.stringify(LogHeaderCodec.transformTo(header))) {
                throw new Error("[CheckpointFold] the checkpoint is of another log: " + theirs);
            }
            from = previous.folded;
            before = previous.events;
        }
        return new Checkpoint(WorkspaceFold.foldFrom(from, events), before + events.length, Checkpoint.FOLD);
    }
}
