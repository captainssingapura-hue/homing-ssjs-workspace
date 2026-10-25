package hue.captains.singapura.js.homing.design.styles.base;

import hue.captains.singapura.js.homing.component.taxonomy.RoleRoot;
import hue.captains.singapura.js.homing.design.styles.CastingStyle;
import hue.captains.singapura.js.homing.design.styles.StylingRes;
import hue.captains.singapura.js.homing.ui.styles.Base;
import hue.captains.singapura.js.homing.ui.styles.BaseColours;
import hue.captains.singapura.js.homing.ui.styles.BaseScheme;
import hue.captains.singapura.js.homing.workspace.tree.TreeComponents;

/**
 * The tree section in the base layer: clear on the page, and washed in the accent while the reader is in it; its room
 * the layout's. Whole: every property its look has (D9).
 */
public record TreeSection() implements CastingStyle<BaseScheme, Base, TreeComponents.TreeSection, RoleRoot> {
    @Override public StylingRes<BaseScheme, TreeComponents.TreeSection> style() {
        return new StylingRes.CssContent<>(new BaseColours.AccentWashLight());
    }
}
