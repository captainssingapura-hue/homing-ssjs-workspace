package hue.captains.singapura.js.homing.workspace.log.store;

import hue.captains.singapura.js.homing.workspace.log.json.Json;
import hue.captains.singapura.js.homing.workspace.log.json.JsonText;

import java.util.ArrayList;

/** Where two wire values first part: a path into them — {@code state.regions[1].tabs[0]} — and what each has there. */
final class JsonDiff {

    private JsonDiff() {}

    /** The first place the two differ, said, or null when they are the same. */
    static String first(Json a, Json b, String path) {
        if (a instanceof Json.Obj oa && b instanceof Json.Obj ob) {
            var ka = new ArrayList<>(oa.members().keySet());
            var kb = new ArrayList<>(ob.members().keySet());
            if (!ka.equals(kb)) return at(path) + ": members " + ka + " / " + kb;
            for (String k : ka) {
                String d = first(oa.members().get(k), ob.members().get(k), path.isEmpty() ? k : path + "." + k);
                if (d != null) return d;
            }
            return null;
        }
        if (a instanceof Json.Arr xa && b instanceof Json.Arr xb) {
            for (int i = 0; i < Math.min(xa.items().size(), xb.items().size()); i++) {
                String d = first(xa.items().get(i), xb.items().get(i), path + "[" + i + "]");
                if (d != null) return d;
            }
            if (xa.items().size() != xb.items().size()) return at(path) + ": " + xa.items().size() + " items / " + xb.items().size();
            return null;
        }
        return a.equals(b) ? null : at(path) + ": " + JsonText.write(a) + " / " + JsonText.write(b);
    }

    private static String at(String path) { return path.isEmpty() ? "(the whole)" : path; }
}
