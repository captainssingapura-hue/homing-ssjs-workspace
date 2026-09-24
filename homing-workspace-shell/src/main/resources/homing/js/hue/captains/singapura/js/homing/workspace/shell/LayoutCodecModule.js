// =============================================================================
// LayoutCodec — pure converter between MultiTabPane's native layout shape and
// the typed LayoutNode record tree (RFC 0029). Both directions round-trip
// losslessly.
//
//   Pane native shape :  { kind: 'leaf', slotId }
//                     | { kind: 'split',
//                         orientation: 'horizontal' | 'vertical',
//                         children: [{ pane, ratio }, …] }        two or more
//
//   Typed LayoutNode   :  LayoutNode.Leaf(PaneId)
//                      |  LayoutNode.Split(Orientation, [LayoutNode.Child(node, ratio), …])
//
// Schema 2: both sides are N-ary now. The native shape always carried a
// children ARRAY and only ever put two in it; the typed side carried one
// ratio and two named halves, so a row of three arrived here as a split
// inside a split and went back out the same way. That is a different
// arrangement, not a different spelling - a flat row has a divider per pair,
// where the nested one has an outer divider that moves two panes together and
// refuses the merge across it. So nothing is folded in either direction now.
//
// Explicit Substrate doctrine: instance methods on a canonical INSTANCE
// singleton — never static. Mirrors Java Functional Objects faithfully:
// `LayoutCodec.INSTANCE.mtToTyped(node)`, not `LayoutCodec.mtToTyped(node)`.
//
// Stateless: zero instance fields. Behaviour is a pure function of input;
// testable in isolation without a DOM.
// =============================================================================

class LayoutCodec {

    /** MultiTabPane native layout → typed LayoutNode tree. */
    mtToTyped(node) {
        if (!node) return null;
        if (node.kind === 'leaf') {
            return new LayoutNode.Leaf(new PaneId(node.slotId));
        }
        // 'split'
        const orient = (node.orientation === 'vertical')
            ? Orientation.VERTICAL
            : Orientation.HORIZONTAL;
        const kids = [];
        for (let i = 0; i < node.children.length; i++) {
            // A track with no share of its own takes an even one; the typed
            // record normalises the lot, so an even share is whatever is left
            // over the tracks that named theirs.
            const r = (typeof node.children[i].ratio === 'number' && node.children[i].ratio > 0)
                    ? node.children[i].ratio
                    : 1 / node.children.length;
            kids.push(new LayoutNode.Child(this.mtToTyped(node.children[i].pane), r));
        }
        return new LayoutNode.Split(orient, kids);
    }

    /** Typed LayoutNode tree → MultiTabPane native shape. */
    typedToMt(typed) {
        if (!typed) return null;
        if (typed instanceof LayoutNode.Leaf) {
            return { kind: 'leaf', slotId: typed.paneId.value };
        }
        // LayoutNode.Split
        const orientStr = (typed.orientation === Orientation.VERTICAL)
            ? 'vertical' : 'horizontal';
        const children = [];
        for (let i = 0; i < typed.children.length; i++) {
            children.push({
                pane:  this.typedToMt(typed.children[i].node),
                ratio: typed.children[i].ratio
            });
        }
        return { kind: 'split', orientation: orientStr, children: children };
    }
}

LayoutCodec.INSTANCE = new LayoutCodec();
