package hue.captains.singapura.js.homing.workspace.bench;

import hue.captains.singapura.js.homing.component.keyboard.FocusPartyModule;
import hue.captains.singapura.js.homing.component.keyboard.focusParties;
import hue.captains.singapura.js.homing.component.keyboard.focusParty;
import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.ParamCodec;
import hue.captains.singapura.js.homing.core.QueryString;
import hue.captains.singapura.js.homing.core.js.DomOpsPartyModule;
import hue.captains.singapura.js.homing.core.js.domOpsParties;
import hue.captains.singapura.js.homing.core.js.domOpsParty;
import hue.captains.singapura.js.homing.workspace.parties.MessagingPartyModule;
import hue.captains.singapura.js.homing.workspace.widgets.WidgetParams;
import hue.captains.singapura.js.homing.workspace.widgets.WidgetQuery;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * One widget on a page of its own: {@code /app?app=widget-bench&widget=<kind>&…},
 * or a route of the bench's site. The address names the kind; the rest of it
 * is the widget's params, read by the kind's own {@link WidgetQuery}. The page
 * makes the widget with the container it is lent - the bench's own, made
 * resizable - and its params, and gives it nothing else. The container is the
 * bench's, so the bench watches it: a widget that misbehaves in it is said.
 * The bench is the widget's host: it grafts the widget's own DomOps party into
 * the page's tree at its place there, and its own focus party into the page's.
 * Over the page float the monitors, each at a toggle of the bar above the
 * container, each a widget like any other, hosted in a tab-pane.
 */
public record WidgetBenchApp() implements AppModule<WidgetBenchApp.Params, WidgetBenchApp> {

    public static final WidgetBenchApp INSTANCE = new WidgetBenchApp();

    /** The kind to stand up, and its params, as that kind declares them. */
    public record Params(String widget, WidgetParams params) implements AppModule._Param {
        public Params {
            Objects.requireNonNull(widget, "Params.widget");
            Objects.requireNonNull(params, "Params.params");
            var kind = WidgetBench.kind(widget).orElseThrow(() -> new IllegalArgumentException("Params.widget '" + widget + "': one of " + WidgetBench.names()));
            if (!kind.paramsType().isInstance(params)) {
                throw new IllegalArgumentException("Params.params: a " + kind.paramsType().getSimpleName() + ", for " + widget);
            }
        }
    }

    record appMain() implements AppModule._AppMain<Params, WidgetBenchApp> {}

    /**
     * The kind is required, and one the bench knows; everything else on the
     * address is the kind's to read, and a refusal names the key it read.
     */
    public static final ParamCodec<Params> CODEC = new ParamCodec<>() {

        @Override public Decoded<Params> from(Map<String, List<String>> query) {
            String widget = QueryString.first(query, "widget");
            if (widget == null || widget.isBlank()) return Decoded.missing("widget");
            var kind = WidgetBench.kind(widget);
            if (kind.isEmpty()) return Decoded.malformed("widget", widget, "a widget kind: " + WidgetBench.names());
            var read = kind.get().query().from(query);
            if (read instanceof WidgetQuery.Read.Refused<?> r) return Decoded.malformed(r.key(), r.value(), r.expected());
            return Decoded.ok(new Params(widget, ((WidgetQuery.Read.Ok<?>) read).params()));
        }

        @Override public Map<String, List<String>> to(Params params) {
            var q = QueryString.params();
            QueryString.put(q, "widget", params.widget());
            WidgetBench.kind(params.widget()).orElseThrow().toQuery(params.params())
                    .forEach((k, vs) -> vs.forEach(v -> QueryString.put(q, k, v)));
            return q;
        }
    };

    @Override public String title()      { return "Widget bench"; }
    @Override public String simpleName() { return "widget-bench"; }
    @Override public Class<Params> paramsType() { return Params.class; }
    @Override public ParamCodec<Params> paramCodec() { return CODEC; }

    @Override
    public ImportsFor<WidgetBenchApp> imports() {
        return ImportsFor.<WidgetBenchApp>builder()
                .add(new ModuleImports<>(List.of(new BenchWidgetsModule.BENCH_WIDGETS(), new BenchWidgetsModule.BENCH_MONITORS(), new BenchWidgetsModule.BENCH_PARTIES()), BenchWidgetsModule.INSTANCE))
                // the monitors' bar, and the desk their floats lie on
                .add(new ModuleImports<>(List.of(new BenchMonitorsModule.BenchMonitors()), BenchMonitorsModule.INSTANCE))
                // the parties the widget joins, at the root: the runtime, the bench's manual secretary, and its simulator
                .add(new ModuleImports<>(List.of(new MessagingPartyModule.MessagingParty()), MessagingPartyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new BenchSecretaryModule.BenchSecretary()), BenchSecretaryModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new PartySimulatorModule.PartySimulator()), PartySimulatorModule.INSTANCE))
                // the page's party, where the bench grafts the widget's own; the party of parties, which says the strays
                .add(new ModuleImports<>(List.of(new domOpsParty(), new domOpsParties(), new DomOpsPartyModule.MobileDomOpsParty()), DomOpsPartyModule.INSTANCE))
                // the page's focus party, where the bench grafts the widget's own; its party of parties
                .add(new ModuleImports<>(List.of(new focusParty(), new focusParties(), new FocusPartyModule.MobileFocusParty()), FocusPartyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new WidgetBenchStyles.wb_page(), new WidgetBenchStyles.wb_bench(), new WidgetBenchStyles.wb_misbehaves()), WidgetBenchStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<WidgetBenchApp> exports() { return new ExportsOf<>(INSTANCE, List.of(new appMain())); }
}
