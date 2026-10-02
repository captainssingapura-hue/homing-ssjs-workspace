package hue.captains.singapura.js.homing.workspace.monitors;

import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.QueryString;
import hue.captains.singapura.js.homing.workspace.widgets.WidgetDeclaration;
import hue.captains.singapura.js.homing.workspace.widgets.WidgetParams;
import hue.captains.singapura.js.homing.workspace.widgets.WidgetQuery;

import java.util.List;
import java.util.Map;

/** The party log, declared: {@code party-log}, and its one param on an address - {@code keep}. */
public record PartyLogDeclaration() implements WidgetDeclaration<PartyLogDeclaration.Params> {

    public static final PartyLogDeclaration INSTANCE = new PartyLogDeclaration();

    /** The fewest and the most lines a log keeps. */
    public static final int FEWEST = 10, MOST = 1000;

    /** How many lines the log keeps, the newest: from {@link #FEWEST} to {@link #MOST}. */
    public record Params(int keep) implements WidgetParams {

        /** 200 lines: the widget's own default, PartyLog.KEEP. */
        public static final Params DEFAULT = new Params(200);

        public Params {
            if (keep < FEWEST || keep > MOST) throw new IllegalArgumentException("Params.keep " + keep + ": from " + FEWEST + " to " + MOST);
        }
    }

    /** {@code keep=50}; absent at its default. */
    public record Query() implements WidgetQuery<Params> {

        @Override
        public Read<Params> from(Map<String, List<String>> query) {
            String said = QueryString.first(query, "keep");
            if (said == null) return Read.ok(Params.DEFAULT);
            try { return Read.ok(new Params(Integer.parseInt(said))); }
            catch (IllegalArgumentException e) {   // NumberFormatException is one
                return Read.refused("keep", said, "a whole number from " + FEWEST + " to " + MOST);
            }
        }

        @Override
        public Map<String, List<String>> to(Params params) {
            var q = QueryString.params();
            if (params.keep() != Params.DEFAULT.keep()) QueryString.put(q, "keep", String.valueOf(params.keep()));
            return q;
        }
    }

    @Override public String kind() { return "party-log"; }
    @Override public Class<Params> paramsType() { return Params.class; }
    @Override public WidgetQuery<Params> query() { return new Query(); }

    @Override
    public ModuleImports<?> constructs() {
        return new ModuleImports<>(List.of(new PartyLogModule.PartyLog()), PartyLogModule.INSTANCE);
    }
}
