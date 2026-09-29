// =============================================================================
// WorkspaceDirectory — the page's directory of its site's workspace groups:
// which kinds of workspace there are, filed in sections, the sections in
// groups. Its shape is generated in Java from the groups' core definitions
// (WorkspaceGroupsJs) and provided once by the page, at boot; whatever shows
// the groups — a switcher — reads it here, the page's as the steward is, and
// never writes it. Provided again, everything that subscribed is told.
//
// A group, as provided - frozen, and never changed:
//   { id, title, defaultKind, defaultPath,
//     sections: [{ title, slug, workspaces: [{ kind, title, path }] }] }
// a path "<section slug>/<kind>": where the kind is filed in its group.
//
//   WorkspaceDirectory.provide(groups)   groups: an array of groups; checked - each a group,
//                                        ids unique, a kind filed once over them all -
//                                        and every subscriber told
//   WorkspaceDirectory.groups()          → the groups provided, or none
//   WorkspaceDirectory.group(id?)        → that group, or the first; null when there is none
//   WorkspaceDirectory.find(kind)        → { group, section, workspace } where the kind is
//                                          filed, or null
//   WorkspaceDirectory.subscribe(fn)     fn(groups) at each provide; → off()
//
// Pure: no DOM; one directory a page, this module's.
// =============================================================================

var _workspaceDirectory = { groups: Object.freeze([]), listeners: [] };

class WorkspaceDirectory {

    static provide(groups) {
        if (!Array.isArray(groups)) throw new TypeError("[WorkspaceDirectory] provide takes an array of groups, got " + JSON.stringify(groups));
        var ids = {}, kinds = {};
        groups.forEach(function (g, i) {
            WorkspaceDirectory._check(g, "groups[" + i + "]");
            if (ids[g.id]) throw new TypeError("[WorkspaceDirectory] two groups of one id: " + g.id);
            ids[g.id] = true;
            g.sections.forEach(function (s) {
                s.workspaces.forEach(function (w) {
                    if (kinds[w.kind]) throw new TypeError("[WorkspaceDirectory] " + w.kind + " is filed in " + kinds[w.kind] + " and in " + g.id + ": a kind is filed once");
                    kinds[w.kind] = g.id;
                });
            });
        });
        _workspaceDirectory.groups = WorkspaceDirectory._frozen(groups);
        _workspaceDirectory.listeners.slice().forEach(function (fn) {
            try { fn(_workspaceDirectory.groups); }
            catch (e) { console.error("[WorkspaceDirectory] a subscriber threw:", e); }
        });
    }

    static groups() { return _workspaceDirectory.groups; }

    static group(id) {
        var all = _workspaceDirectory.groups;
        if (id === undefined || id === null) return all.length ? all[0] : null;
        return all.filter(function (g) { return g.id === id; })[0] || null;
    }

    static find(kind) {
        var all = _workspaceDirectory.groups;
        for (var i = 0; i < all.length; i++) {
            for (var j = 0; j < all[i].sections.length; j++) {
                var s = all[i].sections[j], w = s.workspaces.filter(function (x) { return x.kind === kind; })[0];
                if (w) return Object.freeze({ group: all[i], section: s, workspace: w });
            }
        }
        return null;
    }

    static subscribe(fn) {
        if (typeof fn !== "function") throw new TypeError("[WorkspaceDirectory] subscribe takes a function");
        _workspaceDirectory.listeners.push(fn);
        return function () {
            var at = _workspaceDirectory.listeners.indexOf(fn);
            if (at >= 0) _workspaceDirectory.listeners.splice(at, 1);
        };
    }

    /** A group as the page has it: each part there, of its kind; refused whole otherwise. */
    static _check(g, where) {
        function no(what) { throw new TypeError("[WorkspaceDirectory] " + where + ": " + what); }
        if (!g || typeof g !== "object") no("not a group");
        if (typeof g.id !== "string" || !g.id) no("no id");
        if (typeof g.title !== "string") no("no title");
        if (!Array.isArray(g.sections) || !g.sections.length) no("no sections");
        g.sections.forEach(function (s, i) {
            if (!s || typeof s.title !== "string" || typeof s.slug !== "string" || !s.slug) no("sections[" + i + "] is not a section");
            if (!Array.isArray(s.workspaces) || !s.workspaces.length) no("sections[" + i + "] files no workspace");
            s.workspaces.forEach(function (w, j) {
                if (!w || typeof w.kind !== "string" || !w.kind || typeof w.title !== "string" || typeof w.path !== "string")
                    no("sections[" + i + "].workspaces[" + j + "] is not a workspace");
            });
        });
        if (typeof g.defaultKind !== "string" || !g.sections.some(function (s) { return s.workspaces.some(function (w) { return w.kind === g.defaultKind; }); }))
            no("its default, " + JSON.stringify(g.defaultKind) + ", is not one of its workspaces");
    }

    /** Frozen through: the page's, and never changed. */
    static _frozen(v) {
        if (v && typeof v === "object") {
            Object.keys(v).forEach(function (k) { WorkspaceDirectory._frozen(v[k]); });
            Object.freeze(v);
        }
        return v;
    }
}
