// =============================================================================
// WidgetBenchApp - one widget on a page of its own. The address names a kind
// and the kind's params; the page makes that widget with the container it is
// lent and those params, and gives it nothing else: no branch, no focus
// membership, no steward, no tab. A widget that stands up here is
// self-contained - its DomOpsParty and FocusParty roots its own.
//
// The container is the MPA's slot, and the one facility the bench gives:
// resizable by its corner. It never scrolls. A widget fills it, at whatever
// size it is made, by itself - and whatever needs scrolling scrolls inside the
// widget. Since the container is the bench's, the bench watches it, and says
// when a widget misbehaves: anything in the container but the widget's one
// root, a root that does not fill it, anything overflowing it. Said on the
// console, and worn by the container, until the widget behaves again.
//
// The params reach the page as the address wrote them, strings, read and
// checked on the server by the kind's own WidgetQuery.
// =============================================================================

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
    // the widget's params: what the address said for it - not the bench's kind, not the page's steward
    var own = {};
    Object.keys(params).forEach(function (k) { if (k !== "widget" && k !== "keyboard") own[k] = params[k]; });
    new Widget(el, Object.freeze(own));
    _watch(el, params.widget);
}

/** What is wrong with the container as the widget keeps it, or nothing. */
function _misbehaviours(el) {
    var out = [];
    if (el.childElementCount !== 1) out.push(el.childElementCount + " elements in the container, not the widget's one root");
    var root = el.firstElementChild;
    if (root && (Math.abs(root.offsetWidth - el.clientWidth) > 1 || Math.abs(root.offsetHeight - el.clientHeight) > 1)) {
        out.push("its root is " + root.offsetWidth + "x" + root.offsetHeight + " in a container of " + el.clientWidth + "x" + el.clientHeight);
    }
    if (el.scrollWidth > el.clientWidth || el.scrollHeight > el.clientHeight) {
        out.push("it overflows the container: " + el.scrollWidth + "x" + el.scrollHeight + " in " + el.clientWidth + "x" + el.clientHeight);
    }
    return out;
}

/** The container watched: at every size it is made, and on every change inside it. */
function _watch(el, kind) {
    var said = "", pending = false;
    function check() {
        pending = false;
        var now = _misbehaviours(el).join("; ");
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
