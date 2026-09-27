// =============================================================================
// WidgetBenchApp - one widget on a page of its own. The address names a kind
// and the kind's params; the page makes that widget with the container it is
// lent and those params, and gives it nothing else: no branch, no focus
// membership, no steward, no tab. A widget that stands up here is
// self-contained - its roots its own.
//
// The bench is the widget's host, and does what a host does with its roots:
// the widget's DomOps party - a mobile party, offered as roots.dom - is
// grafted into the page's tree at the bench's place in it, "widgetBench", as
// "widget"; its focus party - roots.focus - into the page's focus party, at
// the root, as "widget", where the page's steward reaches it. The page's
// snapshot reads the widget's DomOps party through its proxy, and the steward
// learns the widget's members through the other.
//
// The container is the MPA's slot, and the one facility the bench gives:
// resizable by its corner. It never scrolls. A widget fills it, at whatever
// size it is made, by itself - and whatever needs scrolling scrolls inside the
// widget. Since the container is the bench's, the bench watches it, and says
// when a widget misbehaves: anything in the container but the widget's one
// root, a root that does not fill it, anything overflowing it - and, in the
// page's parties, a branch or a member the widget made there rather than in a
// party of its own, or a mobile party no one has grafted. Said on the console,
// and worn by the container, until the widget behaves again.
//
// The params reach the page as the address wrote them, strings, read and
// checked on the server by the kind's own WidgetQuery.
// =============================================================================

const _benchOwner = Object.freeze({ toString: () => "widgetBench" });

function appMain(el, params) {
    var Widget = params && BENCH_WIDGETS[params.widget];
    if (!Widget) {
        // The MPA's flat /app hands the app no params when the codec refused the
        // address's: no kind, a kind the bench does not know, params that do not read.
        el.textContent = "No widget: the address names none, or none the bench knows, or params that do not read.";
        console.error("[widgetBench] no widget kind in the page's params");
        return;
    }
    css.addClass(el, wb_bench);
    // the bench's place in the page's party, where the widget's party is grafted
    var place = domOpsParty.createBranch("widgetBench");
    place.activate(_benchOwner);
    var pageBranches = domOpsParty.listBranches();
    var pageMembers = focusParty.root.members.map(function (m) { return m.name; });
    // the widget's params: what the address said for it - not the bench's kind, not the page's steward
    var own = {};
    Object.keys(params).forEach(function (k) { if (k !== "widget" && k !== "keyboard") own[k] = params[k]; });
    var widget = new Widget(el, Object.freeze(own));
    var dom = widget.roots && widget.roots.dom;
    if (dom instanceof MobileDomOpsParty) place.graft("widget", dom);
    var focus = widget.roots && widget.roots.focus;
    if (focus instanceof MobileFocusParty) focusParty.root.graft("widget", focus);
    _watch(el, params.widget, { widget: widget, pageBranches: pageBranches, pageMembers: pageMembers });
}

/** What is wrong with the container, and the page's party, as the widget keeps them - or nothing. */
function _misbehaviours(el, seen) {
    var out = [];
    if (el.childElementCount !== 1) out.push(el.childElementCount + " elements in the container, not the widget's one root");
    var root = el.firstElementChild;
    if (root && (Math.abs(root.offsetWidth - el.clientWidth) > 1 || Math.abs(root.offsetHeight - el.clientHeight) > 1)) {
        out.push("its root is " + root.offsetWidth + "x" + root.offsetHeight + " in a container of " + el.clientWidth + "x" + el.clientHeight);
    }
    if (el.scrollWidth > el.clientWidth || el.scrollHeight > el.clientHeight) {
        out.push("it overflows the container: " + el.scrollWidth + "x" + el.scrollHeight + " in " + el.clientWidth + "x" + el.clientHeight);
    }
    var dom = seen.widget.roots && seen.widget.roots.dom;
    if (!(dom instanceof MobileDomOpsParty)) out.push("it offers no DomOps party of its own for its host to graft (roots.dom)");
    var theirs = domOpsParty.listBranches().filter(function (n) { return seen.pageBranches.indexOf(n) < 0; });
    if (theirs.length) out.push("it made " + theirs.join(", ") + " in the page's party, not in a party of its own");
    var strays = domOpsParties.strays();
    if (strays.length) out.push("DomOps parties no one has grafted: " + strays.map(function (s) { return s.name; }).join(", "));
    var focus = seen.widget.roots && seen.widget.roots.focus;
    if (!(focus instanceof MobileFocusParty)) out.push("it offers no focus party of its own for its host to graft (roots.focus)");
    var joined = focusParty.root.members.map(function (m) { return m.name; }).filter(function (n) { return n !== "widget" && seen.pageMembers.indexOf(n) < 0; });
    if (joined.length) out.push("it joined " + joined.join(", ") + " to the page's focus party, not to a party of its own");
    var focusStrays = focusParties.strays();
    if (focusStrays.length) out.push("focus parties no one has grafted: " + focusStrays.map(function (s) { return s.name; }).join(", "));
    return out;
}

/** The container watched: at every size it is made, and on every change inside it. */
function _watch(el, kind, seen) {
    var said = "", pending = false;
    function check() {
        pending = false;
        var now = _misbehaviours(el, seen).join("; ");
        if (now === said) return;
        if (now) console.error("[widgetBench] " + kind + " misbehaves: " + now);
        else console.info("[widgetBench] " + kind + " behaves again");
        said = now;
        css.toggleClass(el, wb_misbehaves, !!now);
    }
    function soon() { if (!pending) { pending = true; requestAnimationFrame(check); } }
    new ResizeObserver(soon).observe(el);
    new MutationObserver(soon).observe(el, { childList: true, subtree: true, attributes: true, attributeFilter: ["class", "style"] });
    soon();
}
