package hue.captains.singapura.js.homing.workspace.log.store;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/** Where a workspace log's rows are kept in the browser: the IndexedDB database homing.workspace.log. */
public record IndexedDbLogModule() implements DomModule<IndexedDbLogModule> {

    public record IndexedDbLog() implements Exportable._Class<IndexedDbLogModule> {}

    public static final IndexedDbLogModule INSTANCE = new IndexedDbLogModule();

    @Override
    public ImportsFor<IndexedDbLogModule> imports() {
        return ImportsFor.noImports();
    }

    @Override
    public ExportsOf<IndexedDbLogModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new IndexedDbLog())); }
}
