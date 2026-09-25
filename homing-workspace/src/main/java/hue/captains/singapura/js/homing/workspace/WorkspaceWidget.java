package hue.captains.singapura.js.homing.workspace;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.ModuleNameResolver;
import hue.captains.singapura.js.homing.core.SelfContent;

import java.util.ArrayList;
import java.util.List;

/**
 * RFC 0025 — abstract base for widgets hosted by the workspace's
 * dynamic widget management system. Distinct from RFC 0024's
 * {@code Widget<P, M>}: this contract is shaped for runtime picker
 * instantiation rather than build-time composition.
 *
 * <h2>How it differs from RFC 0024 Widget</h2>
 *
 * <table>
 *   <caption>Two widget shapes</caption>
 *   <tr><th></th><th>RFC 0024 Widget</th><th>WorkspaceWidget</th></tr>
 *   <tr><td>JS function</td><td>{@code mountInto(branch, parent, params)} — returns nothing</td>
 *       <td>{@code construct(branch, params, host)} — returns {@code { root: Element }}</td></tr>
 *   <tr><td>Placement</td><td>Widget attaches to {@code parent} (host gives it the spot)</td>
 *       <td>Widget returns a controller; workspace chrome attaches the controller's root</td></tr>
 *   <tr><td>Load timing</td><td>Eager via static import graph at page load</td>
 *       <td>Lazy via dynamic {@code import()} on first user pick</td></tr>
 *   <tr><td>Lifecycle hint</td><td>n/a</td>
 *       <td>Declared via {@link #lifecycleHint()} — MULTI / SINGLETON / PINNED</td></tr>
 *   <tr><td>Active gating</td><td>n/a (single-instance)</td>
 *       <td>An offered {@code setActive(boolean)} hears whether its tab is the one
 *           showing — for pausing audio or animation. Keys never need gating: they
 *           come through the room, the focus party's member, only while it holds them.</td></tr>
 *   <tr><td>Params</td><td>Typed Java record; URL marshalling reads it</td>
 *       <td>Same — typed Params record; picker form derives from it; URL marshalling
 *           and (future) server-side state storage both use it</td></tr>
 * </table>
 *
 * <h2>The construct contract: a widget runs in a room</h2>
 *
 * <p>A tab is two parts: the TAB — its chip, its name, its icon, its place —
 * which is the workspace's; and the ROOM its widget runs in, the focus
 * party's member. The widget touches neither. It is handed three things:</p>
 *
 * <ul>
 *   <li>{@code branch} — its own, UNACTIVATED: the widget activates it, as
 *       every component does, and mints everything on it.</li>
 *   <li>{@code params} — the typed Params record, as plain JS.</li>
 *   <li>{@code host} — what the widget may say to the host it runs in, and
 *       all of it. The standard API, the same for every widget:
 *       <pre>{@code host.title("Report.md")   // the widget's name, now: its tab says so}</pre>
 *       A widget names itself when its name is its own to know — a document's
 *       file name, a count, a zoom — at construction and on every change. Until
 *       it does, the tab carries the kind's label; its ICON is always the
 *       kind's ({@link WidgetEntry#icon()}), like a site's favicon.</li>
 * </ul>
 *
 * <p>It returns {@code { root: Element }}, and may offer more — each optional,
 * each the widget's to have a use for:</p>
 *
 * <pre>{@code
 * {
 *     root      : Element,                      // what the room shows
 *     keyDown   : function (ev) -> boolean,     // a key the room holds, handed on
 *     activate  : function (),                  // the room holds the keys: put the focus where yours work
 *     setActive : function (showing: boolean),  // its tab is, or is no longer, the one showing
 *     dispose   : function ()                   // it is going
 * }
 * }</pre>
 *
 * <h2>Subclass template</h2>
 *
 * <pre>{@code
 * public final class MyWidget extends WorkspaceWidget<MyWidget.Params, MyWidget> {
 *     public static final MyWidget INSTANCE = new MyWidget();
 *     private MyWidget() {}
 *
 *     public record Params(String docId) implements WorkspaceWidget._Param {}
 *     private record construct() implements WorkspaceWidget._Construct<Params, MyWidget> {}
 *
 *     @Override protected _Construct<Params, MyWidget> construct() { return new construct(); }
 *     @Override public Class<Params> paramsType() { return Params.class; }
 *     @Override public String title() { return "My Widget"; }
 *
 *     @Override protected List<String> constructBodyJs() {
 *         return List.of(
 *             "    var root = branch.createElement('root', 'div');",
 *             "    root.textContent = 'doc ' + params.docId;",
 *             "    var keyHandler = function (e) {",
 *             "        if (e.key === 'r') root.textContent = 'reloaded ' + params.docId;",
 *             "    };",
 *             "    return {",
 *             "        root: root,",
 *             "        setActive: function (active) {",
 *             "            if (active) document.addEventListener('keydown', keyHandler);",
 *             "            else        document.removeEventListener('keydown', keyHandler);",
 *             "        }",
 *             "    };"
 *         );
 *     }
 * }
 * }</pre>
 *
 * <h2>Hosting beyond workspaces</h2>
 *
 * <p>A {@code WorkspaceWidget} can be hosted by a non-workspace AppModule
 * via a degenerate mini-workspace wrapper — one widget, no picker, no
 * layout management. The widget's contract stays the same; only the
 * surrounding chrome changes. This is what makes WorkspaceWidget the
 * more general shape: it composes upward into the workspace and
 * downward into single-widget contexts.</p>
 *
 * @param <P> typed Params record this widget accepts
 * @param <W> self-type (CRTP)
 * @since RFC 0025 Ext1a — Mechanism 1 (Widget Type Registry)
 */
public abstract class WorkspaceWidget<P extends WorkspaceWidget._Param, W extends WorkspaceWidget<P, W>>
        implements DomModule<W>, SelfContent {

    /**
     * Marker for the {@code Params} record a WorkspaceWidget accepts.
     * Every concrete params record implements this — distinct from
     * {@code AppModule._Param} so the type system separates the two
     * widget worlds even when the params shape happens to be identical.
     */
    public interface _Param {}

    /** Sentinel for paramless workspace widgets. */
    public record _None() implements _Param {
        public static final _None INSTANCE = new _None();
    }

    /**
     * Marker for the generated {@code construct} export. Each concrete
     * widget declares a nested {@code record construct()} implementing
     * this; the framework reads it via {@link #construct()} and emits
     * the corresponding {@code export {construct};} line.
     */
    public interface _Construct<P extends _Param, W extends WorkspaceWidget<P, W>>
            extends Exportable._Constant<W> {}

    // -----------------------------------------------------------------------
    // Subclass contract — abstract.
    // -----------------------------------------------------------------------

    /** The typed construct export record this widget declares. */
    protected abstract _Construct<P, W> construct();

    /**
     * The Params record class. Used by: picker form generation (reflects
     * on record components), URL marshalling, future server-side state
     * storage. Return {@link _None}{@code .class} for paramless widgets.
     */
    public abstract Class<P> paramsType();

    /** Display title for the widget — picker label fallback, page title hint. */
    public abstract String title();

    /**
     * Lifecycle policy. Default {@link LifecycleHint#MULTI} — each open
     * creates a fresh instance. Override for singleton / ephemeral
     * widgets.
     */
    public LifecycleHint lifecycleHint() { return LifecycleHint.MULTI; }

    /**
     * The body JS of the {@code construct(branch, params, host)} function.
     * Three things in scope:
     * <ul>
     *   <li>{@code branch} — DomOpsParty branch owned by the workspace's
     *       widgets sub-tree. Use {@code branch.createElement(name, tag)}
     *       to mint owned elements and {@code branch.createBranch(name)}
     *       for sub-branches. Branch dissolution drives all cleanup.</li>
     *   <li>{@code params} — plain JS object matching the typed Params
     *       record. The framework marshals from URL / picker submission
     *       into the typed shape before invoking construct.</li>
     *   <li>{@code host} — the widget's word to its host:
     *       {@code host.title(text)} names its tab.</li>
     * </ul>
     *
     * <p>The body MUST {@code return} a controller object with at least
     * {@code root}; see the class-level Javadoc for the rest, all optional.</p>
     */
    protected abstract List<String> constructBodyJs();

    /** Extra imports the body JS needs. Return empty when the body only
     *  uses framework primitives. */
    protected List<ModuleImports<? extends Importable>> bodyImports() {
        return List.of();
    }

    // -----------------------------------------------------------------------
    // Framework wiring — final. Subclasses cannot override.
    // -----------------------------------------------------------------------

    @Override
    public final ImportsFor<W> imports() {
        var b = ImportsFor.<W>builder();
        for (ModuleImports<? extends Importable> extra : bodyImports()) {
            b.add(extra);
        }
        return b.build();
    }

    @SuppressWarnings("unchecked")
    @Override
    public final ExportsOf<W> exports() {
        return new ExportsOf<>((W) this, List.of(construct()));
    }

    @Override
    public final List<String> selfContent(ModuleNameResolver resolver) {
        var lines = new ArrayList<String>();
        // The third arg is the widget's HOST: what it may say to the host it
        // runs in - host.title(text) names its tab. A body that has nothing to
        // say ignores it.
        lines.add("function construct(branch, params, host) {");
        lines.add("    try {");
        lines.addAll(constructBodyJs());
        lines.add("    } catch (e) {");
        lines.add("        console.error('WorkspaceWidget construct failed:', e);");
        // RFC 0045: the error card is served into EVERY widget's JS, so it must be
        // conformant itself — build it through the DomOpsParty branch, with no
        // inline style or literal colour (a class would need a CssGroup this base
        // doesn't own; the fallback stays unstyled, which is fine for an error).
        // The branch was handed unactivated; a widget that threw before
        // activating it has left it so, and the card needs it active.
        lines.add("        try { branch.activate({ toString: function () { return 'widgetError'; } }); } catch (_) {}");
        lines.add("        var err = branch.createElement('__widgetError', 'div');");
        lines.add("        err.textContent = '\\u26a0 Widget failed to construct: ' + (e && e.message ? e.message : String(e));");
        lines.add("        return { root: err };");
        lines.add("    }");
        lines.add("}");
        return lines;
    }
}
