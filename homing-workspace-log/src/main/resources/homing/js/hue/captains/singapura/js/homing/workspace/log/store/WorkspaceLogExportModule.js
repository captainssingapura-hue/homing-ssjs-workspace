// =============================================================================
// WorkspaceLogExport — a workspace log as a file: JSON lines, each ended by a
// line feed, the header first and an event a line after it, every one written
// by its generated codec and JSON.stringify. The Java side writes the same log
// as the same bytes; its validator reads this file and says whether it does.
//
//   WorkspaceLogExport.text(header, events)  → the file's text
//   WorkspaceLogExport.of(store)             → Promise<the text of what the store holds>
//   WorkspaceLogExport.fileName(header)      → "<kind>-<workspaceId>.workspace.log"
//   WorkspaceLogExport.stateText(header, events) → the log's meaning: one line, the
//                                              FoldedState the browser's fold makes of it
//   WorkspaceLogExport.state(store)          → Promise<that, for what the store holds>
//   WorkspaceLogExport.stateFileName(header) → "<kind>-<workspaceId>.workspace.state"
//   WorkspaceLogExport.asideText(aside)      → a set-aside log as a log file: its
//                                              header, then its lines as they were kept
//   WorkspaceLogExport.asideFileName(aside)  → "<kind>-<workspaceId>.aside-<at>.workspace.log"
// =============================================================================

class WorkspaceLogExport {
    static text(header, events) {
        var out = JSON.stringify(LogHeaderCodec.transformTo(header)) + "\n";
        for (var i = 0; i < events.length; i++) out += JSON.stringify(LoggedEventCodec.transformTo(events[i])) + "\n";
        return out;
    }

    static of(store) {
        return store.events().then(function (events) { return WorkspaceLogExport.text(store.header, events); });
    }

    static fileName(header) {
        return header.kind.value + "-" + header.workspaceId.id + ".workspace.log";
    }

    static stateText(header, events) {
        return JSON.stringify(FoldedStateCodec.transformTo(WorkspaceFold.fold(header, events))) + "\n";
    }

    static state(store) {
        return store.events().then(function (events) { return WorkspaceLogExport.stateText(store.header, events); });
    }

    static stateFileName(header) {
        return header.kind.value + "-" + header.workspaceId.id + ".workspace.state";
    }

    static asideText(aside) {
        var out = JSON.stringify(LogHeaderCodec.transformTo(aside.header)) + "\n";
        for (var i = 0; i < aside.lines.length; i++) out += aside.lines[i] + "\n";
        return out;
    }

    static asideFileName(aside) {
        return aside.header.kind.value + "-" + aside.header.workspaceId.id + ".aside-" + aside.at + ".workspace.log";
    }
}
