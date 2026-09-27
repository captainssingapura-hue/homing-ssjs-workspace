// =============================================================================
// WidgetBenchApp - one widget on a page of its own. The address names a kind
// and the kind's params; the page makes that widget with the container it is
// lent and those params, and gives it nothing else: no branch, no focus
// membership, no steward, no tab. A widget that stands up here is
// self-contained - its DomOpsParty and FocusParty roots its own.
//
// The container is the MPA's slot, and the one facility the bench gives:
// resizable by its corner, so the widget is seen to fill whatever it is lent.
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
}
