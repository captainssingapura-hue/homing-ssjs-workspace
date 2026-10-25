package hue.captains.singapura.js.homing.workspace.tree;

import hue.captains.singapura.js.homing.design.styles.CastingStyle;
import hue.captains.singapura.js.homing.design.styles.StyleDeclaration;
import hue.captains.singapura.js.homing.design.styling.StyleBundle;
import hue.captains.singapura.js.homing.ui.styles.Base;
import hue.captains.singapura.js.homing.ui.styles.BaseScheme;

import java.util.ArrayList;
import java.util.List;

/**
 * The tree's looks as this jar declares them (RFC 0067, appendix The Style Jars): its components' in Base, read from its
 * base module's bundles - the one list - so the per-jar check holds them as it holds a house jar's.
 */
public record WorkspaceTreeStyleDeclaration() implements StyleDeclaration {

    public static final WorkspaceTreeStyleDeclaration INSTANCE = new WorkspaceTreeStyleDeclaration();

    @Override public List<Provided<?, ?>> provided() {
        var looks = new ArrayList<CastingStyle<BaseScheme, Base, ?, ?>>();
        for (StyleBundle<BaseScheme, Base> b : TreeBaseModule.INSTANCE.bundles()) looks.addAll(b.looks());
        return List.of(new Provided<>(Base.INSTANCE, looks));
    }
}
