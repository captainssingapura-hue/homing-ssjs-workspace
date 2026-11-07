package hue.captains.singapura.js.homing.workspace.tree;

import hue.captains.singapura.js.homing.component.taxonomy.ComponentModule;
import hue.captains.singapura.js.homing.component.taxonomy.MeaningsCheck;
import hue.captains.singapura.js.homing.ui.taxonomy.HouseTaxonomy;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The tree's nodes held to the taxonomy's checks at build, as a house jar's are: over the house's taxonomy with the
 * tree's components in it. The meanings first - a taxonomy read no longer checks them, the jar that declares the
 * nodes does.
 */
class TreeTaxonomyTest {

    @Test
    void everyNode_meansSomething_everySectionANodes() {
        var modules = new ArrayList<ComponentModule>(HouseTaxonomy.INSTANCE.modules());
        modules.add(TreeComponentModule.INSTANCE);
        var problems = MeaningsCheck.INSTANCE.problems(HouseTaxonomy.INSTANCE.read(modules));
        assertEquals(List.of(), problems.stream().map(Object::toString).toList(),
                "every node means something, every section is a node's, none twice, no words astray");
    }
}
