package hue.captains.singapura.js.homing.workspace.tree;

import hue.captains.singapura.js.homing.component.taxonomy.Component;
import hue.captains.singapura.js.homing.component.taxonomy.ComponentModule;

import java.util.List;

/** The tree's components as a module (RFC 0067, D46): what an instance that lays documents out as trees registers. */
public record TreeComponentModule() implements ComponentModule {

    public static final TreeComponentModule INSTANCE = new TreeComponentModule();

    @Override public List<? extends Component<?>> components() { return List.of(TreeComponents.TreeSection.INSTANCE); }
}
