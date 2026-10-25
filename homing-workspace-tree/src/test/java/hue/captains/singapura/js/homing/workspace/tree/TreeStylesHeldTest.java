package hue.captains.singapura.js.homing.workspace.tree;

import hue.captains.singapura.js.homing.component.taxonomy.ComponentModule;
import hue.captains.singapura.js.homing.design.styling.CheckDeclaration;
import hue.captains.singapura.js.homing.ui.styles.BaseColours;
import hue.captains.singapura.js.homing.ui.taxonomy.HouseTaxonomy;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The tree's looks held to the house's per-jar check, as a house jar's are: over the house's taxonomy with the tree's
 * components in it, against the house's colours.
 */
class TreeStylesHeldTest {

    @TestFactory
    Stream<DynamicTest> theJar_andEachImplementation_heldAlone() {
        var modules = new ArrayList<ComponentModule>(HouseTaxonomy.MODULES);
        modules.add(TreeComponentModule.INSTANCE);
        CheckDeclaration.Checked checked = CheckDeclaration.INSTANCE.check(
                WorkspaceTreeStyleDeclaration.INSTANCE, HouseTaxonomy.INSTANCE.read(modules), BaseColours.TREE);
        return Stream.concat(
                Stream.of(DynamicTest.dynamicTest("the jar", () -> assertEquals(List.of(), checked.problems()))),
                checked.implementations().stream()
                        .map(i -> DynamicTest.dynamicTest(i.name(), () -> assertEquals(List.of(), i.problems()))));
    }
}
