// =============================================================================
// LayoutAlgebra — the grid's algebra on the log's Layout: SplitGridTree's, move
// for move — the same subdivision, the same removal with its room going the
// same way, the same splits giving way — with each share computed exactly
// (ExactShare) and every split brought back to whole millionths by one rule.
// Java's LayoutAlgebra, transcribed; the two agree to the millionth.
//
//   LayoutAlgebra.subdivide(layout, regionId, side, newRegionId)  → a Layout
//   LayoutAlgebra.remove(layout, regionId, towardOrNull)           → a Layout
//   LayoutAlgebra.tracks(layout, path, [Scaled])                   → a Layout
//     a refusal throws, saying why
// =============================================================================

function _refuse(why) { throw new Error("[LayoutAlgebra] " + why); }

class LayoutAlgebra {
    static _work(l) {
        if (l instanceof Cell) return { cell: l.region.value };
        return { axis: l.axis, kids: l.tracks.map(function (t) { return { node: LayoutAlgebra._work(t.node), share: ExactShare.millionthsOf(t.share.units) }; }) };
    }

    static _done(n) {
        if (n.cell != null) return new Cell(new RegionId(n.cell));
        var units = ExactShare.millionths(n.kids.map(function (k) { return k.share; }));
        return new Split(n.axis, n.kids.map(function (k, i) { return new Track(LayoutAlgebra._done(k.node), new Scaled(units[i], Track.SCALE)); }));
    }

    static _cells(n) {
        if (n.cell != null) return [n.cell];
        var out = [];
        for (var i = 0; i < n.kids.length; i++) out = out.concat(LayoutAlgebra._cells(n.kids[i].node));
        return out;
    }

    static _find(n, id, parent, index) {
        if (n.cell != null) return n.cell === id ? { node: n, parent: parent, index: index } : null;
        for (var i = 0; i < n.kids.length; i++) {
            var h = LayoutAlgebra._find(n.kids[i].node, id, n, i);
            if (h) return h;
        }
        return null;
    }

    static _chain(n, id, acc) {
        if (n.cell != null) return n.cell === id ? acc : null;
        for (var i = 0; i < n.kids.length; i++) {
            var hit = LayoutAlgebra._chain(n.kids[i].node, id, acc.concat([{ split: n, index: i }]));
            if (hit) return hit;
        }
        return null;
    }

    static _parentOf(n, target, parent, index) {
        if (n === target) return parent ? { split: parent, index: index } : null;
        if (n.cell != null) return null;
        for (var i = 0; i < n.kids.length; i++) {
            var hit = LayoutAlgebra._parentOf(n.kids[i].node, target, n, i);
            if (hit) return hit;
        }
        return null;
    }

    static _giveWay(root, split) {
        var only = split.kids[0].node, above = LayoutAlgebra._parentOf(root, split, null, -1);
        if (!above) return only;
        above.split.kids[above.index].node = only;
        return root;
    }

    static subdivide(layout, id, side, newId) {
        var root = LayoutAlgebra._work(layout);
        if (LayoutAlgebra._cells(root).indexOf(newId.value) >= 0) _refuse("the region " + newId.value + " already exists");
        var hit = LayoutAlgebra._find(root, id.value, null, -1);
        if (!hit) _refuse("no region " + id.value);
        var axis = side === Side.LEFT || side === Side.RIGHT ? Axis.HORIZONTAL : Axis.VERTICAL;
        var before = side === Side.LEFT || side === Side.TOP, fresh = { cell: newId.value };
        if (hit.parent && hit.parent.axis === axis) {
            var k = hit.parent.kids[hit.index], half = k.share.times(ExactShare.half());
            k.share = half;
            hit.parent.kids.splice(before ? hit.index : hit.index + 1, 0, { node: fresh, share: half });
            return LayoutAlgebra._done(root);
        }
        var pair = before ? [{ node: fresh, share: ExactShare.half() }, { node: hit.node, share: ExactShare.half() }]
                          : [{ node: hit.node, share: ExactShare.half() }, { node: fresh, share: ExactShare.half() }];
        var split = { axis: axis, kids: pair };
        if (!hit.parent) return LayoutAlgebra._done(split);
        hit.parent.kids[hit.index].node = split;
        return LayoutAlgebra._done(root);
    }

    static remove(layout, id, toward) {
        var root = LayoutAlgebra._work(layout);
        var hit = LayoutAlgebra._find(root, id.value, null, -1);
        if (!hit) _refuse("no region " + id.value);
        if (!hit.parent) _refuse("the last region cannot be removed");
        var to = toward ? toward.value : null;
        if (to !== null && to !== id.value) {
            var overs = LayoutAlgebra._splitters(root, id.value);
            for (var o = 0; o < overs.length; o++) if (overs[o].id === to) return LayoutAlgebra._done(LayoutAlgebra._across(root, id.value, overs[o]));
        }
        var siblings = hit.parent.kids, gone = siblings[hit.index], lean = LayoutAlgebra._lean(hit, to);
        siblings.splice(hit.index, 1);
        var heir = siblings[lean > hit.index ? hit.index : lean];
        heir.share = heir.share.plus(gone.share);
        if (siblings.length > 1) return LayoutAlgebra._done(root);
        return LayoutAlgebra._done(LayoutAlgebra._giveWay(root, hit.parent));
    }

    static _lean(hit, toward) {
        var kids = hit.parent.kids, i = hit.index;
        if (toward !== null) for (var d = 0; d < 2; d++) {
            var j = d === 1 ? i + 1 : i - 1;
            if (j >= 0 && j < kids.length && LayoutAlgebra._cells(kids[j].node).indexOf(toward) >= 0) return j;
        }
        return i > 0 ? i - 1 : i + 1;
    }

    static _splitters(root, id) {
        var chain = LayoutAlgebra._chain(root, id, []), out = [], axis = null, before = true, after = true;
        for (var k = chain.length - 1; k >= 0 && (before || after); k--) {
            var s = chain[k].split, i = chain[k].index, last = s.kids.length - 1, far;
            if (axis === null) axis = s.axis;
            else if (s.axis !== axis) break;
            if (before && i > 0) { far = LayoutAlgebra._facing(s.kids[i - 1].node, axis, false); if (far !== null) out.push({ before: true, id: far, at: k }); }
            if (after && i < last) { far = LayoutAlgebra._facing(s.kids[i + 1].node, axis, true); if (far !== null) out.push({ before: false, id: far, at: k }); }
            if (i > 0) before = false;
            if (i < last) after = false;
        }
        return out;
    }

    static _facing(n, axis, first) {
        while (n.cell == null) {
            if (n.axis !== axis) return null;
            n = n.kids[first ? 0 : n.kids.length - 1].node;
        }
        return n.cell;
    }

    static _across(root, id, over) {
        var chain = LayoutAlgebra._chain(root, id, []);
        var s = chain[over.at].split, i = chain[over.at].index, j = over.before ? i - 1 : i + 1;
        var mine = s.kids[i], theirs = s.kids[j], share = mine.share;
        for (var k = over.at + 1; k < chain.length; k++) share = share.times(chain[k].split.kids[chain[k].index].share);
        LayoutAlgebra._gains(theirs.node, theirs.share, share, over.id);
        theirs.share = theirs.share.plus(share);
        var left = LayoutAlgebra._loses(mine.node, mine.share, share, id);
        if (left) { mine.node = left; mine.share = mine.share.minus(share); }
        else s.kids.splice(i, 1);
        if (s.kids.length > 1) return root;
        return LayoutAlgebra._giveWay(root, s);
    }

    static _gains(n, have, extra, id) {
        if (n.cell != null) return;
        var total = have.plus(extra), i = LayoutAlgebra._holding(n.kids, id), at = have.times(n.kids[i].share);
        for (var j = 0; j < n.kids.length; j++) n.kids[j].share = (j === i ? at.plus(extra) : have.times(n.kids[j].share)).over(total);
        LayoutAlgebra._gains(n.kids[i].node, at, extra, id);
    }

    static _loses(n, have, share, id) {
        if (n.cell != null) return null;
        var total = have.minus(share), i = LayoutAlgebra._holding(n.kids, id), at = have.times(n.kids[i].share);
        var inner = LayoutAlgebra._loses(n.kids[i].node, at, share, id), sizes = [];
        for (var j = 0; j < n.kids.length; j++) sizes.push(j === i ? at.minus(share) : have.times(n.kids[j].share));
        if (inner) n.kids[i].node = inner;
        else { n.kids.splice(i, 1); sizes.splice(i, 1); }
        for (var m = 0; m < n.kids.length; m++) n.kids[m].share = sizes[m].over(total);
        return n.kids.length > 1 ? n : n.kids[0].node;
    }

    static _holding(kids, id) {
        for (var i = 0; i < kids.length; i++) if (LayoutAlgebra._cells(kids[i].node).indexOf(id) >= 0) return i;
        return -1;
    }

    static tracks(layout, path, shares) {
        var root = LayoutAlgebra._work(layout), n = root;
        if (path !== "") path.split("/").forEach(function (step) {
            var k = parseInt(step, 10);
            if (n.cell != null || k >= n.kids.length) _refuse("no split at " + path);
            n = n.kids[k].node;
        });
        if (n.cell != null) _refuse("the node at '" + path + "' is a region, not a split");
        if (n.kids.length !== shares.length) _refuse("the split at '" + path + "' has " + n.kids.length + " tracks, not " + shares.length);
        for (var i = 0; i < shares.length; i++) {
            if (shares[i].scale !== Track.SCALE) _refuse("a share at scale " + shares[i].scale + ": tracks are in millionths");
            n.kids[i].share = ExactShare.millionthsOf(shares[i].units);
        }
        return LayoutAlgebra._done(root);
    }
}
