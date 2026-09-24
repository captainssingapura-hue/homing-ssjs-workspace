// =============================================================================
// WidgetPickerModule — the widget picker: a grid of tiles, then a params form.
//
//   new WidgetPicker(branch, { entries, disabledIds?, onPick, onCancel?,
//                              keyboard?, keyboardId? })
//     .mountInto(hostEl)   .dispose()
//
//   onPick(entry, params)   params {} for a tile with no fields, the filled
//                           object for one with, and NULL for a tile that is
//                           disabled because its widget is already open —
//                           "I want that one" rather than "make another".
//
// A TILE IS A DESIGN WORD. Box.Control.Tile: one BOX of a grid, picked, where
// an Option is one ROW of a list. The word carries the box — extent and
// proportion, so every tile is the same size however long its label; inset,
// gap, corner, rule — and the grid's own columns are minmax'd off the tile's
// extent, so the DESIGN decides how many fit across and this file cannot
// disagree with it.
//
// BOTH DIRECTIONS, AND BY GEOMETRY. Left and right step along the row; up and
// down move a row, and the row is MEASURED rather than assumed: the tiles wrap
// at whatever width the pane happens to be, so the columns are read back from
// where the boxes actually landed (offsetTop groups a row) and the cursor keeps
// its column when it changes row. A grid that guessed four across would jump
// sideways the moment a pane were narrowed. Home and End go to the ends of the
// grid, Enter or Space picks, Escape cancels.
//
// The cursor is a CLASS the picker puts on and takes off, wearing the selected
// and focus words — the same mark for the keyboard and the pointer, and no
// colour chosen here. Not a selector on the resting class: a worn token is put
// on the element whenever its class is added, so a state written as a selector
// marks every tile it is declared on.
//
// The keys come THROUGH THE PARTY: the picker joins the page's steward while it
// is up and leaves when it goes. Without a steward it still picks by pointer;
// only the keys are missing, which is the honest degradation for a host that
// made none.
// =============================================================================

const _pickerOwner = Object.freeze({ toString: () => "widgetPicker" });
var _pickerSeq = 0;

class WidgetPicker {

    constructor(branch, opts) {
        if (!branch) throw new Error("[WidgetPicker] a branch of its own is required");
        opts = opts || {};
        if (!Array.isArray(opts.entries)) throw new Error("[WidgetPicker] opts.entries must be an array");
        if (typeof opts.onPick !== "function") throw new Error("[WidgetPicker] opts.onPick must be a function");
        branch.activate(_pickerOwner);
        this._branch      = branch;
        this._seq         = ++_pickerSeq;
        this._entries     = opts.entries;
        this._disabledIds = opts.disabledIds || {};
        this._onPick      = opts.onPick;
        this._onCancel    = opts.onCancel || null;
        this._kb          = opts.keyboard || null;
        this._kbId        = null;
        this._tiles       = [];      // { el, entry, disabled }
        this._at          = -1;      // the cursor
        this._delivered   = false;
        this._gridEl      = null;

        if (this._kb) {
            var self = this;
            var id = (opts.keyboardId ? String(opts.keyboardId) : branch.name) + "/picker";
            this._kbId = this._kb.join(id, { keyDown: function (ev) { return self.keyDown(ev); } });
            this._kb.claim(this._kbId);
        }
    }

    // ── Building ────────────────────────────────────────────────────────────

    mountInto(hostEl) {
        if (!hostEl) throw new Error("[WidgetPicker] mountInto needs a host");
        this._host = hostEl;
        hostEl.appendChild(this._buildGrid());
        if (this._tiles.length) this._moveTo(this._firstEnabled());
        return this;
    }

    _buildGrid() {
        var grid = this._branch.createElement("grid" + this._seq, "div");
        css.addClass(grid, hwp_grid);
        grid.setAttribute("role", "listbox");
        for (var i = 0; i < this._entries.length; i++) grid.appendChild(this._buildTile(this._entries[i], i));
        this._gridEl = grid;
        return grid;
    }

    _buildTile(entry, i) {
        var self = this;
        var disabled = !!this._disabledIds[entry.simpleName];
        var tile = this._branch.createElement("tile" + this._seq + "_" + i, "div");
        css.addClass(tile, hwp_tile);
        if (disabled) css.addClass(tile, hwp_tile_disabled);
        tile.setAttribute("role", "option");
        tile.setAttribute("aria-selected", "false");
        if (disabled) tile.setAttribute("aria-disabled", "true");

        var icon = this._branch.createElement("icon" + this._seq + "_" + i, "div");
        css.addClass(icon, hwp_tile_icon);
        icon.textContent = entry.icon && entry.icon.kind === "emoji" ? entry.icon.value : "📦";
        tile.appendChild(icon);

        var label = this._branch.createElement("label" + this._seq + "_" + i, "div");
        css.addClass(label, hwp_tile_label);
        label.textContent = entry.label;
        tile.appendChild(label);

        var at = this._tiles.length;
        tile.addEventListener("mouseenter", function () { self._moveTo(at); });
        tile.addEventListener("click", function () { self._moveTo(at); self._pickAt(at); });
        this._tiles.push({ el: tile, entry: entry, disabled: disabled });
        return tile;
    }

    // ── The cursor ──────────────────────────────────────────────────────────

    _moveTo(i) {
        if (i < 0 || i >= this._tiles.length || i === this._at) return;
        if (this._at >= 0) {
            css.removeClass(this._tiles[this._at].el, hwp_tile_on);
            this._tiles[this._at].el.setAttribute("aria-selected", "false");
        }
        this._at = i;
        css.addClass(this._tiles[i].el, hwp_tile_on);
        this._tiles[i].el.setAttribute("aria-selected", "true");
        if (this._tiles[i].el.scrollIntoView) {
            this._tiles[i].el.scrollIntoView({ block: "nearest", inline: "nearest" });
        }
    }

    _firstEnabled() {
        for (var i = 0; i < this._tiles.length; i++) if (!this._tiles[i].disabled) return i;
        return this._tiles.length ? 0 : -1;
    }

    /**
     * The rows as they actually laid out: tiles grouped by the top they landed
     * on. Measured on every move, because the pane can be resized under the
     * picker and a remembered column count would be wrong the moment it was.
     */
    _rows() {
        var rows = [], last = null, cur = null;
        for (var i = 0; i < this._tiles.length; i++) {
            var top = this._tiles[i].el.offsetTop;
            if (last === null || Math.abs(top - last) > 1) { cur = []; rows.push(cur); last = top; }
            cur.push(i);
        }
        return rows;
    }

    /** Where the cursor is, as a row and a place in it. */
    _where(rows) {
        for (var r = 0; r < rows.length; r++) {
            var c = rows[r].indexOf(this._at);
            if (c >= 0) return { row: r, col: c };
        }
        return { row: 0, col: 0 };
    }

    _step(by) { this._moveTo(Math.max(0, Math.min(this._tiles.length - 1, this._at + by))); }

    /** A row up or down, keeping the column — or the end of a shorter row. */
    _rowStep(by) {
        var rows = this._rows();
        var at = this._where(rows);
        var r = at.row + by;
        if (r < 0 || r >= rows.length) return;
        var row = rows[r];
        this._moveTo(row[Math.min(at.col, row.length - 1)]);
    }

    // ── Keys ────────────────────────────────────────────────────────────────

    /** The member's door: the steward routed a key here while the picker holds them. */
    keyDown(ev) {
        if (ev.altKey || ev.ctrlKey || ev.metaKey) return false;   // a chord is somebody else's
        switch (ev.key) {
            case "ArrowRight": this._step(1);      return true;
            case "ArrowLeft":  this._step(-1);     return true;
            case "ArrowDown":  this._rowStep(1);   return true;
            case "ArrowUp":    this._rowStep(-1);  return true;
            case "Home":       this._moveTo(0);    return true;
            case "End":        this._moveTo(this._tiles.length - 1); return true;
            case "Enter":
            case " ":          this._pickAt(this._at); return true;
            case "Escape":     if (this._onCancel) this._onCancel(); return true;
            default:           return false;
        }
    }

    // ── Picking ─────────────────────────────────────────────────────────────

    _pickAt(i) {
        if (i < 0 || i >= this._tiles.length) return;
        var t = this._tiles[i];
        // A disabled tile means "that one is already open": the caller focuses
        // the live instance rather than making a second.
        if (t.disabled) { this._deliver(t.entry, null); return; }
        var fields = t.entry.paramsFields;
        if (fields && fields.length) this._showForm(t.entry);
        else this._deliver(t.entry, {});
    }

    _deliver(entry, params) {
        if (this._delivered) return;
        this._delivered = true;
        this._release();
        this._onPick(entry, params);
    }

    /** The keys go back to whoever had them; the picker is done with them either way. */
    _release() {
        if (!this._kb || !this._kbId) return;
        try { this._kb.leave(this._kbId); } catch (e) {}
        this._kb = null;
        this._kbId = null;
    }

    dispose() {
        this._release();
        try { this._branch.dissolve(); } catch (e) {}
    }

    // ── The params form ─────────────────────────────────────────────────────

    _showForm(entry) {
        var self = this;
        var host = this._host;
        while (host.firstChild) host.removeChild(host.firstChild);

        var form = this._branch.createElement("form" + this._seq, "div");
        css.addClass(form, hwp_form);
        var inputs = {};

        for (var i = 0; i < entry.paramsFields.length; i++) {
            var f = entry.paramsFields[i];
            var row = this._branch.createElement("row" + this._seq + "_" + i, "div");
            css.addClass(row, hwp_form_row);
            var label = this._branch.createElement("flab" + this._seq + "_" + i, "label");
            css.addClass(label, hwp_form_label);
            label.textContent = f.name;
            var input = this._branch.createElement("fin" + this._seq + "_" + i, "input");
            css.addClass(input, hwp_form_input);
            input.type = "text";
            input.value = (entry.defaults && entry.defaults[f.name]) || "";
            inputs[f.name] = input;
            row.appendChild(label);
            row.appendChild(input);
            form.appendChild(row);
        }

        var actions = this._branch.createElement("acts" + this._seq, "div");
        css.addClass(actions, hwp_form_actions);
        var cancel = this._branch.createElement("cancel" + this._seq, "button");
        css.addClass(cancel, hwp_form_btn);
        cancel.textContent = "Cancel";
        cancel.addEventListener("click", function () { if (self._onCancel) self._onCancel(); });
        var ok = this._branch.createElement("ok" + this._seq, "button");
        css.addClass(ok, hwp_form_btn, hwp_form_btn_primary);
        ok.textContent = "Add";
        ok.addEventListener("click", function () {
            var params = {};
            for (var k in inputs) if (Object.prototype.hasOwnProperty.call(inputs, k)) params[k] = inputs[k].value;
            self._deliver(entry, params);
        });
        actions.appendChild(cancel);
        actions.appendChild(ok);
        form.appendChild(actions);
        host.appendChild(form);

        var first = entry.paramsFields[0];
        if (first && inputs[first.name] && inputs[first.name].focus) inputs[first.name].focus();
    }
}
