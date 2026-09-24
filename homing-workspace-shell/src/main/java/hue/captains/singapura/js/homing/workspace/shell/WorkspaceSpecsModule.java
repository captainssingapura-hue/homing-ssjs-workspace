package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.AppUrl;
import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleNameResolver;
import hue.captains.singapura.js.homing.core.QueryString;
import hue.captains.singapura.js.homing.core.SelfContent;
import hue.captains.singapura.js.homing.core.StampedParams;

import java.util.ArrayList;
import java.util.List;

/**
 * Every registered {@link WorkspaceSpec}, stamped into one JS module as
 * {@code SPECS}, keyed by kind.
 *
 * <p>The same object {@code GenericWorkspaceChrome} interpolates into its body
 * JS, in a module of its own instead. The difference matters: a body is the
 * studio's widget machinery writing a page, and only that machinery can carry
 * it; a module is imported by whoever wants it. {@link WorkspaceApp} wants it
 * to be a page of any standard MPA, and the workspace is then hosted by the
 * framework's own page model rather than the studio's.</p>
 *
 * <p>Each spec leaves here carrying two things the shell asks for and no
 * single spec can know: {@code availableKinds}, the whole catalogue the
 * switcher offers, and {@code switchBase}, the address a kind change goes to —
 * the app's own flat address, minted, not spelled.</p>
 */
public record WorkspaceSpecsModule() implements EsModule<WorkspaceSpecsModule>, SelfContent {

    public static final WorkspaceSpecsModule INSTANCE = new WorkspaceSpecsModule();

    /** The registry, keyed by {@code spec.kind}. */
    public record SPECS() implements Exportable._Constant<WorkspaceSpecsModule> {}

    @Override public ImportsFor<WorkspaceSpecsModule> imports() { return ImportsFor.noImports(); }

    @Override
    public ExportsOf<WorkspaceSpecsModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new SPECS()));
    }

    @Override
    public List<String> selfContent(ModuleNameResolver resolver) {
        var lines = new ArrayList<String>();
        lines.add("// Generated from WorkspaceSpecRegistry - do not hand-edit.");
        lines.add("const SPECS = " + WorkspaceSpecJson.allAsObject(WorkspaceSpecRegistry.INSTANCE.all()) + ";");
        lines.add("// What no single spec can know: every kind the switcher offers, and where");
        lines.add("// a change of kind goes. The registry here is the only thing that knows both.");
        lines.add("const KINDS = Object.keys(SPECS).map(function (k) {");
        lines.add("    return { kind: k, title: SPECS[k].title, section: SPECS[k].section };");
        lines.add("});");
        lines.add("const SWITCH_BASE = " + StampedParams.jsString(switchBase()) + ";");
        lines.add("Object.keys(SPECS).forEach(function (k) {");
        lines.add("    SPECS[k].availableKinds = KINDS;");
        lines.add("    SPECS[k].switchBase = SWITCH_BASE;");
        lines.add("});");
        return lines;
    }

    /** Where a change of kind navigates: the app's flat address, the kind added by the switcher. */
    static String switchBase() {
        return AppUrl.flat(WorkspaceApp.INSTANCE.simpleName(), QueryString.params());
    }
}
