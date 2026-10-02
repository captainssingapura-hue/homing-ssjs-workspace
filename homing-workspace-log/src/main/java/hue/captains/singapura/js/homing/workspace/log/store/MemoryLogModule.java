package hue.captains.singapura.js.homing.workspace.log.store;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/** A workspace log's rows kept in memory: for a page that keeps nothing, and for tests. */
public record MemoryLogModule() implements DomModule<MemoryLogModule> {

    public record MemoryLog() implements Exportable._Class<MemoryLogModule> {}

    public static final MemoryLogModule INSTANCE = new MemoryLogModule();

    @Override
    public ImportsFor<MemoryLogModule> imports() {
        return ImportsFor.noImports();
    }

    @Override
    public ExportsOf<MemoryLogModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new MemoryLog())); }
}
