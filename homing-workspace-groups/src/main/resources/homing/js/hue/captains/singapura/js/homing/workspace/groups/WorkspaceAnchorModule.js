// =============================================================================
// WorkspaceAnchor — where a kind of workspace is in its group, as the page's
// anchor names it: "ws/<section>/<kind>", the group's own tree after the "#"
// (RFC 0058). The group is the page, reached from outside by its address;
// everything after "#ws/" is the group's, and never reaches the server. Which
// workspace of the kind is not here: that is a parameter, not a position - the
// query's ws_id, or ws_name.
//
// A group is as the directory has it (WorkspaceDirectory): its sections' slugs
// and its workspaces' paths, "<section slug>/<kind>", generated in Java.
//
//   WorkspaceAnchor.PREFIX              "ws/": the workspace's anchors apart from any
//                                       other on the page - a heading's, say
//   WorkspaceAnchor.of(group, kind)     → "ws/<section>/<kind>", or null for a kind the
//                                         group does not file
//   WorkspaceAnchor.isWorkspace(anchor) → whether the anchor is a workspace's: "ws", "ws/..."
//   WorkspaceAnchor.read(group, anchor) → { kind, anchor, said, asked, section, workspace }:
//       kind       the kind the page shows
//       anchor     the kind's true anchor: what the address says
//       said       "exact"    the kind's true anchor
//                  "moved"    a kind of the group, under another path - the kind, at
//                             its true one: its segment is its identity, the rest is not
//                  "none"     no anchor, or another's than the workspace's: the group's default
//                  "unknown"  a workspace's anchor naming no kind of the group: the group's
//                             default - and the page says so
//       asked      the anchor as it came
//       section    the section it is filed under, { title, slug, ... }
//       workspace  the workspace filed, { kind, title, path }
//
// Pure: no DOM, and no address read - the page reads its anchor and hands it in.
// =============================================================================

class WorkspaceAnchor {

    static PREFIX = "ws/";

    static of(group, kind) {
        var filed = WorkspaceAnchor._filed(group, kind);
        return filed ? WorkspaceAnchor.PREFIX + filed.workspace.path : null;
    }

    static isWorkspace(anchor) {
        return typeof anchor === "string" && (anchor === "ws" || anchor.indexOf(WorkspaceAnchor.PREFIX) === 0);
    }

    static read(group, anchor) {
        if (!group || !Array.isArray(group.sections)) throw new TypeError("[WorkspaceAnchor] read takes a group as the directory has it, got " + JSON.stringify(group));
        var asked = typeof anchor === "string" ? anchor : "";
        if (!WorkspaceAnchor.isWorkspace(asked)) return WorkspaceAnchor._at(group, group.defaultKind, "none", asked);
        var segments = asked.slice(WorkspaceAnchor.PREFIX.length).split("/").filter(function (s) { return s; }).map(WorkspaceAnchor._decoded);
        var kind = segments.length ? segments[segments.length - 1] : null;
        if (kind === null || !WorkspaceAnchor._filed(group, kind)) return WorkspaceAnchor._at(group, group.defaultKind, "unknown", asked);
        var truth = WorkspaceAnchor.of(group, kind);
        return WorkspaceAnchor._at(group, kind, truth === asked ? "exact" : "moved", asked);
    }

    static _at(group, kind, said, asked) {
        var filed = WorkspaceAnchor._filed(group, kind);
        if (!filed) throw new TypeError("[WorkspaceAnchor] " + JSON.stringify(kind) + " is not filed in the group " + group.id);
        return Object.freeze({ kind: kind, anchor: WorkspaceAnchor.PREFIX + filed.workspace.path, said: said, asked: asked,
                               section: filed.section, workspace: filed.workspace });
    }

    /** Where the group files the kind: its section and its workspace, or null. */
    static _filed(group, kind) {
        for (var i = 0; i < group.sections.length; i++) {
            var s = group.sections[i], w = s.workspaces.filter(function (x) { return x.kind === kind; })[0];
            if (w) return { section: s, workspace: w };
        }
        return null;
    }

    /** A segment as it was meant: decoded, or as it came when it does not decode. */
    static _decoded(segment) {
        try { return decodeURIComponent(segment); } catch (e) { return segment; }
    }
}
