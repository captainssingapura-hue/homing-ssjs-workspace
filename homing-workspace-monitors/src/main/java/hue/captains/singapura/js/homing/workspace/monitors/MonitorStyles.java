package hue.captains.singapura.js.homing.workspace.monitors;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.Wearable;

import java.util.List;

import static hue.captains.singapura.js.homing.design.Box.Container;
import static hue.captains.singapura.js.homing.design.DesignClass.of;
import static hue.captains.singapura.js.homing.design.Target.Size;

/**
 * The monitors' own sheet. What a monitor shows wears the focus monitor's rows
 * (FocusStyles), so the trees and the log read alike; this is only the room
 * around them.
 */
public record MonitorStyles() implements CssGroup<MonitorStyles> {

    public static final MonitorStyles INSTANCE = new MonitorStyles();

    /** The monitor's box, where what it shows scrolls: inset as a pane's content is. */
    public record mn_pane() implements CssClass<MonitorStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Container.Pane.class, Size.Inset.class)); }
        @Override public List<? extends Wearable> sizes() { return List.of(of(Container.Pane.class, Size.Inset.class)); }
        @Override public String body() { return "box-sizing: border-box;"; }
    }

    @Override
    public List<CssClass<MonitorStyles>> cssClasses() { return List.of(new mn_pane()); }
}
