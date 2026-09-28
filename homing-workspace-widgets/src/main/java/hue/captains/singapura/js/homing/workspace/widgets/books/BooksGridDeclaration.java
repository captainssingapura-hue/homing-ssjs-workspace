package hue.captains.singapura.js.homing.workspace.widgets.books;

import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.QueryString;
import hue.captains.singapura.js.homing.workspace.parties.PartyType;
import hue.captains.singapura.js.homing.workspace.widgets.WidgetDeclaration;
import hue.captains.singapura.js.homing.workspace.widgets.WidgetParams;
import hue.captains.singapura.js.homing.workspace.widgets.WidgetQuery;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** The books grid, declared: {@code books-grid}, and its params on an address - {@code columns}, {@code numbers}. */
public record BooksGridDeclaration() implements WidgetDeclaration<BooksGridDeclaration.Params> {

    public static final BooksGridDeclaration INSTANCE = new BooksGridDeclaration();

    /** The books' columns, as the relation has them. */
    public static final List<String> COLUMNS = List.of("title", "author", "year", "rating");

    /**
     * What the grid shows: which columns, in the order given - each one of
     * {@link #COLUMNS}, each once - and whether a gutter of row numbers leads.
     */
    public record Params(List<String> columns, boolean rowNumbers) implements WidgetParams {

        /** Every column, in the relation's order, and no gutter. */
        public static final Params DEFAULT = new Params(COLUMNS, false);

        public Params {
            columns = List.copyOf(Objects.requireNonNull(columns, "Params.columns"));
            if (columns.isEmpty()) throw new IllegalArgumentException("Params.columns: at least one");
            if (!COLUMNS.containsAll(columns)) throw new IllegalArgumentException("Params.columns " + columns + ": each one of " + COLUMNS);
            if (new HashSet<>(columns).size() != columns.size()) throw new IllegalArgumentException("Params.columns " + columns + ": each once");
        }
    }

    /** {@code columns=title,rating} and {@code numbers=on}; each absent at its default. */
    public record Query() implements WidgetQuery<Params> {

        @Override
        public Read<Params> from(Map<String, List<String>> query) {
            List<String> columns = COLUMNS;
            String said = QueryString.first(query, "columns");
            if (said != null) {
                try { columns = new Params(List.of(said.split(",", -1)), false).columns(); }
                catch (IllegalArgumentException e) {
                    return Read.refused("columns", said, "a comma list of " + String.join(", ", COLUMNS) + ", each once");
                }
            }
            String numbers = QueryString.first(query, "numbers");
            if (numbers != null && !numbers.equals("on")) return Read.refused("numbers", numbers, "on, or absent");
            return Read.ok(new Params(columns, numbers != null));
        }

        @Override
        public Map<String, List<String>> to(Params params) {
            var q = QueryString.params();
            if (!params.columns().equals(COLUMNS)) QueryString.put(q, "columns", String.join(",", params.columns()));
            if (params.rowNumbers()) QueryString.put(q, "numbers", "on");
            return q;
        }
    }

    @Override public String kind() { return "books-grid"; }
    @Override public Class<Params> paramsType() { return Params.class; }
    @Override public WidgetQuery<Params> query() { return new Query(); }
    @Override public List<PartyType<?>> parties() { return List.of(BookSelection.TYPE); }

    @Override
    public ModuleImports<?> constructs() {
        return new ModuleImports<>(List.of(new BooksGridModule.BooksGrid()), BooksGridModule.INSTANCE);
    }
}
