package hue.captains.singapura.js.homing.workspace.log;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

/** What a host's tabs must be, said once: each once, and the one shown among them. */
final class HostTabs {
    private HostTabs() {}

    static void check(String host, List<TabId> tabs, Optional<TabId> shown) {
        var seen = new HashSet<TabId>();
        for (TabId t : tabs) if (!seen.add(t)) throw new IllegalArgumentException("the " + host + " holds " + t + " twice");
        shown.ifPresent(s -> {
            if (!seen.contains(s)) throw new IllegalArgumentException("the " + host + " shows " + s + ", which it does not hold");
        });
    }
}
