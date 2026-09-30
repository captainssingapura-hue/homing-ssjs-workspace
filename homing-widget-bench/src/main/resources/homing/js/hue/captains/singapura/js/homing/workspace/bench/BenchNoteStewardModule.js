// =============================================================================
// BenchNoteSteward — the steward of the content bench's notes: a
// BenchContentSteward of the type "note", hired by the root note party of the
// page, and by nothing linked below it.
//
//   new BenchNoteSteward(tell)
// =============================================================================

class BenchNoteSteward extends BenchContentSteward {
    constructor(tell) { super(tell, "note"); }
}
