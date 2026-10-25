package hue.captains.singapura.js.homing.workspace.tree;

import hue.captains.singapura.js.homing.component.taxonomy.ComponentModule;
import hue.captains.singapura.js.homing.design.styling.BaseModule;
import hue.captains.singapura.js.homing.design.styling.StyleBundle;
import hue.captains.singapura.js.homing.ui.styles.Base;
import hue.captains.singapura.js.homing.ui.styles.BaseScheme;
import hue.captains.singapura.js.homing.design.styles.base.TreeSection;

import java.util.List;

/**
 * The tree's components as an instance of the house registers them (RFC 0067, D50, D51): its component module, each
 * component with its look in Base. A site that lays documents out as trees hands it to the house's styling
 * ({@code HouseStyles.INSTANCE.styling(List.of(..., TreeBaseModule.INSTANCE))}), so the tree section is in its taxonomy
 * and drawn in every style it serves.
 */
public record TreeBaseModule() implements BaseModule<BaseScheme, Base> {

    public static final TreeBaseModule INSTANCE = new TreeBaseModule();

    @Override public Base style() { return Base.INSTANCE; }

    @Override public ComponentModule components() { return TreeComponentModule.INSTANCE; }

    @Override public List<StyleBundle<BaseScheme, Base>> bundles() {
        return List.of(new StyleBundle<>(TreeComponents.TreeSection.INSTANCE, List.of(new TreeSection())));
    }
}
