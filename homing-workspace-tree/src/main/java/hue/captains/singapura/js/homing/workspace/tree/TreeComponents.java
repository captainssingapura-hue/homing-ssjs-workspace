package hue.captains.singapura.js.homing.workspace.tree;

import hue.captains.singapura.js.homing.component.taxonomy.Component;
import hue.captains.singapura.js.homing.component.taxonomy.StateType;
import hue.captains.singapura.js.homing.ui.taxonomy.HouseBranches;
import hue.captains.singapura.js.homing.ui.taxonomy.HouseStates;

import java.util.List;

/**
 * What a document laid out as a tree draws that the house's components do not (RFC 0067, Two Classes per Leaf, D17): a
 * component of its own for each, in the house's tree wherever an instance registers the tree's module
 * ({@link TreeComponentModule}), its look in Base with it ({@link TreeBaseModule}).
 */
public final class TreeComponents {

    private TreeComponents() {}

    /** A section of a document laid out as a tree: its own part - its heading and its leaf - shown while the reader is in it. */
    public record TreeSection() implements Component<HouseBranches.Region> {
        public static final TreeSection INSTANCE = new TreeSection();
        @Override public HouseBranches.Region parent() { return HouseBranches.Region.INSTANCE; }
        /** Whether the reader is in it - the layout's to say. */
        @Override public List<StateType<?>> states() { return List.of(HouseStates.Current.INSTANCE); }
    }
}
