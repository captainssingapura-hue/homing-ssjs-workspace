package hue.captains.singapura.js.homing.workspace.bench;

import hue.captains.singapura.js.homing.component.keyboard.FocusPartyModule;
import hue.captains.singapura.js.homing.component.keyboard.focusParty;
import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.ParamCodec;
import hue.captains.singapura.js.homing.core.js.DomOpsPartyModule;
import hue.captains.singapura.js.homing.core.js.domOpsParty;
import hue.captains.singapura.js.homing.ui.elements.Elements;
import hue.captains.singapura.js.homing.workspace.demowidgets.books.BookJumbotronModule;
import hue.captains.singapura.js.homing.workspace.demowidgets.books.BookSelectionModule;
import hue.captains.singapura.js.homing.workspace.demowidgets.books.BookSelectionSecretaryModule;
import hue.captains.singapura.js.homing.workspace.demowidgets.books.BooksGridModule;
import hue.captains.singapura.js.homing.workspace.parties.MessagingPartyModule;
import hue.captains.singapura.js.homing.workspace.tree.TreeLayoutModule;

import java.util.List;

/**
 * The tree placement, on the bench: {@code /tree} - {@link BenchTree}'s arrangement laid out
 * by the tree's engine, a bar of its sections above it to show one, and the section in view
 * marked there. The books meet in a book selection party the page holds.
 */
public record TreeBenchApp() implements AppModule<TreeBenchApp.Params, TreeBenchApp> {

    public static final TreeBenchApp INSTANCE = new TreeBenchApp();

    /** None: the page lays out the one tree it has. */
    public record Params() implements AppModule._Param {}

    record appMain() implements AppModule._AppMain<Params, TreeBenchApp> {}

    public static final ParamCodec<Params> CODEC = ParamCodec.ofEmpty(Params::new);

    @Override public String title()      { return "Tree bench"; }
    @Override public String simpleName() { return "tree-bench"; }
    @Override public Class<Params> paramsType() { return Params.class; }
    @Override public ParamCodec<Params> paramCodec() { return CODEC; }

    @Override
    public ImportsFor<TreeBenchApp> imports() {
        return ImportsFor.<TreeBenchApp>builder()
                // the tree and its engine
                .add(new ModuleImports<>(List.of(new BenchTreeModule.BENCH_TREE()), BenchTreeModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new TreeLayoutModule.TreeLayout()), TreeLayoutModule.INSTANCE))
                // the types it offers the tree
                .add(new ModuleImports<>(List.of(new ParamsCardModule.ParamsCard()), ParamsCardModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new BooksGridModule.BooksGrid()), BooksGridModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new BookJumbotronModule.BookJumbotron()), BookJumbotronModule.INSTANCE))
                // the party the books meet in, the page's
                .add(new ModuleImports<>(List.of(new MessagingPartyModule.MessagingParty()), MessagingPartyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new BookSelectionModule.BOOK_SELECTION()), BookSelectionModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new BookSelectionSecretaryModule.BookSelectionSecretary()), BookSelectionSecretaryModule.INSTANCE))
                // the page's parties, where the tree is grafted; the bar's buttons
                .add(new ModuleImports<>(List.of(new domOpsParty()), DomOpsPartyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new focusParty()), FocusPartyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new Elements.ButtonBuilder()), Elements.INSTANCE))
                .add(new ModuleImports<>(List.of(new TreeBenchStyles.tb_page(), new TreeBenchStyles.tb_bar(), new TreeBenchStyles.tb_box()),
                        TreeBenchStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<TreeBenchApp> exports() { return new ExportsOf<>(INSTANCE, List.of(new appMain())); }
}
