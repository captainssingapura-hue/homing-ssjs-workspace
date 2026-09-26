// =============================================================================
// WorkspaceLogExport — a workspace log as a file: JSON lines, each ended by a
// line feed, the header first and an event a line after it, every one written
// by its generated codec and JSON.stringify. The Java side writes the same log
// as the same bytes; its validator reads this file and says whether it does.
//
//   WorkspaceLogExport.text(header, events)  → the file's text
//   WorkspaceLogExport.of(store)             → Promise<the text of what the store holds>
//   WorkspaceLogExport.fileName(header)      → "<kind>-<workspaceId>.workspace.log"
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
}
