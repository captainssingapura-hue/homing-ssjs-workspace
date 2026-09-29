// =============================================================================
// WorkspaceAnchor — where a workspace is in its group, as the page's anchor
// names it: "ws/<section>/<kind>", the group's own tree after the "#" (RFC
// 0058), and, at its end, which workspace of the kind — "?ws_name=<name>", or
// "?ws_id=<id>" — unless it is the kind's own. The group is the page, reached
// from outside by its address; everything after "#ws/" is the group's, and
// never reaches the server. The kind is a position; which of it is a
// parameter, never a segment - the anchor's query.
//
// A group is as the directory has it (WorkspaceDirectory): its sections' slugs
// and its workspaces' paths, "<section slug>/<kind>", generated in Java. A
// name is written as a form writes it - a space a "+", the rest escaped - and
// read back so.
//
//   WorkspaceAnchor.PREFIX              "ws/": the workspace's anchors apart from any
//                                       other on the page - a heading's, say
//   WorkspaceAnchor.of(group, kind, which?) → "ws/<section>/<kind>", and "?ws_name=…" for
//                                         { name }, "?ws_id=…" for { id } - none for the
//                                         kind's own; null for a kind the group does not file
//   WorkspaceAnchor.isWorkspace(anchor) → whether the anchor is a workspace's: "ws", "ws/..."
//   WorkspaceAnchor.read(group, anchor) → { kind, anchor, said, asked, query, workspaceId,
//                                           workspaceName, section, workspace }:
//       kind       the kind the page shows
//       anchor     the kind's true anchor, its path alone: what the address says of it
//       said       "exact"    the kind's true path
//                  "moved"    a kind of the group, under another path - the kind, at
//                             its true one: its segment is its identity, the rest is not
//                  "none"     no anchor, or another's than the workspace's: the group's default
//                  "unknown"  a workspace's anchor naming no kind of the group: the group's
//                             default - and the page says so
//       asked      the anchor as it came
//       query      its query as it came, "?…", or ""
//       workspaceId, workspaceName   which workspace of the kind it names, by id or by name -
//                  each or both null: the kind's own
//       section    the section it is filed under, { title, slug, ... }
//       workspace  the workspace filed, { kind, title, path }
//
// Pure: no DOM, and no address read - the page reads its anchor and hands it in.
// =============================================================================

class WorkspaceAnchor {

    static PREFIX = "ws/";

    static of(group, kind, which) {
        var filed = WorkspaceAnchor._filed(group, kind);
        if (!filed) return null;
        var path = WorkspaceAnchor.PREFIX + filed.workspace.path, w = which || {};
        if (w.name) return path + "?ws_name=" + WorkspaceAnchor._written(w.name);
        if (w.id) return path + "?ws_id=" + WorkspaceAnchor._written(w.id);
        return path;
    }

    static isWorkspace(anchor) {
        return typeof anchor === "string" && (anchor === "ws" || anchor.indexOf("ws?") === 0 || anchor.indexOf(WorkspaceAnchor.PREFIX) === 0);
    }

    static read(group, anchor) {
        if (!group || !Array.isArray(group.sections)) throw new TypeError("[WorkspaceAnchor] read takes a group as the directory has it, got " + JSON.stringify(group));
        var asked = typeof anchor === "string" ? anchor : "";
        if (!WorkspaceAnchor.isWorkspace(asked)) return WorkspaceAnchor._at(group, group.defaultKind, "none", asked, "");
        var q = asked.indexOf("?"), path = q < 0 ? asked : asked.slice(0, q), query = q < 0 ? "" : asked.slice(q);
        var segments = path.slice(WorkspaceAnchor.PREFIX.length).split("/").filter(function (s) { return s; }).map(WorkspaceAnchor._decoded);
        var kind = segments.length ? segments[segments.length - 1] : null;
        if (kind === null || !WorkspaceAnchor._filed(group, kind)) return WorkspaceAnchor._at(group, group.defaultKind, "unknown", asked, query);
        return WorkspaceAnchor._at(group, kind, WorkspaceAnchor.of(group, kind) === path ? "exact" : "moved", asked, query);
    }

    static _at(group, kind, said, asked, query) {
        var filed = WorkspaceAnchor._filed(group, kind);
        if (!filed) throw new TypeError("[WorkspaceAnchor] " + JSON.stringify(kind) + " is not filed in the group " + group.id);
        var params = WorkspaceAnchor._params(query);
        return Object.freeze({ kind: kind, anchor: WorkspaceAnchor.PREFIX + filed.workspace.path, said: said, asked: asked, query: query,
                               workspaceId: params.ws_id || null, workspaceName: params.ws_name || null,
                               section: filed.section, workspace: filed.workspace });
    }

    /** An anchor's query, read as a form writes it: "?a=1&b=two+words". The first of each name; a blank, none. */
    static _params(query) {
        var out = {};
        query.replace(/^\?/, "").split("&").forEach(function (pair) {
            if (!pair) return;
            var at = pair.indexOf("="), name = WorkspaceAnchor._read(at < 0 ? pair : pair.slice(0, at)), value = at < 0 ? "" : WorkspaceAnchor._read(pair.slice(at + 1));
            if (!Object.prototype.hasOwnProperty.call(out, name) && value.trim()) out[name] = value.trim();
        });
        return out;
    }

    /** A value as a form writes it: escaped, a space a "+". */
    static _written(value) { return encodeURIComponent(String(value)).replace(/%20/g, "+"); }

    static _read(value) { return WorkspaceAnchor._decoded(value.replace(/\+/g, " ")); }

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
