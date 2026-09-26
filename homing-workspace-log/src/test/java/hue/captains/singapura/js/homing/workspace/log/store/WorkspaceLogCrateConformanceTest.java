package hue.captains.singapura.js.homing.workspace.log.store;

import hue.captains.singapura.js.homing.conformance.rules.CrateDependencyRule;
import hue.captains.singapura.js.homing.conformance.rules.OrphanCheck;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Crate conformance for {@code homing-workspace-log}. */
class WorkspaceLogCrateConformanceTest {

    @Test
    void everyServedModuleIsCrated() {
        assertEquals(List.of(), OrphanCheck.check(WorkspaceLogCrate.INSTANCE),
                "every served JS module in homing-workspace-log must be declared in its crate");
    }

    @Test
    void importsRespectCrateBoundaries() {
        assertEquals(List.of(), CrateDependencyRule.check(WorkspaceLogCrate.INSTANCE),
                "every JS import must resolve to the crate itself or one it directly requires");
    }
}
