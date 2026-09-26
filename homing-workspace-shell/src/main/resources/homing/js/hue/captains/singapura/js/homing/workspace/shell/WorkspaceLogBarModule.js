// =============================================================================
// WorkspaceLogBar — the workspace log's own line along the foot of the floor:
// how many events it has recorded, and Export, which saves the log as a
// workspace log file — the file the Java validator reads. The gallery keeps
// no log, so this is the workspace's alone.
//
//   new WorkspaceLogBar(branch, { host, store })
//     host   where the bar goes: the workspace's floor, under its grid
//     store  the WorkspaceLogStore it exports
//   bar.count(n)     says how many are recorded
//   bar.export()     → Promise<the file's text>, and the file saved
//   bar.dispose()
// =============================================================================

const _logBarOwner = Object.freeze({ toString: () => "workspaceLogBar" });

class WorkspaceLogBar {
    constructor(branch, opts) {
        var o = opts || {};
        if (!branch) throw new Error("[WorkspaceLogBar] a branch of its own is required");
        if (!o.host || !o.store) throw new Error("[WorkspaceLogBar] opts.host and opts.store are required");
        branch.activate(_logBarOwner);
        this.branch = branch;
        this._store = o.store;
        this._url = null;
        var self = this;
        var bar = branch.createElement("bar", "div");
        css.addClass(bar, ws_logbar);
        this._counted = branch.createElement("count", "span");
        css.addClass(this._counted, ws_logbar_count);
        var button = branch.createElement("export", "button");
        button.type = "button";
        button.textContent = "Export log";
        button.addEventListener("click", function () { self.export(); });
        // the file is handed over by a link the bar keeps, never shown
        this._link = branch.createElement("link", "a");
        css.addClass(this._link, ws_logbar_link);
        bar.appendChild(this._counted);
        bar.appendChild(button);
        bar.appendChild(this._link);
        o.host.appendChild(bar);
        this.root = bar;
        this.count(0);
    }

    count(n) { this._counted.textContent = "Workspace log: " + n + (n === 1 ? " event" : " events") + " this visit"; }

    export() {
        var self = this;
        return WorkspaceLogExport.of(this._store).then(function (text) {
            if (self._url) URL.revokeObjectURL(self._url);
            self._url = URL.createObjectURL(new Blob([text], { type: "text/plain;charset=utf-8" }));
            HrefManagerInstance.set(self._link, self._url);
            self._link.download = WorkspaceLogExport.fileName(self._store.header);
            self._link.click();
            return text;
        });
    }

    dispose() {
        if (this._url) URL.revokeObjectURL(this._url);
        if (this.root.parentNode) this.root.parentNode.removeChild(this.root);
        try { this.branch.dissolve(); } catch (e) {}
    }
}
