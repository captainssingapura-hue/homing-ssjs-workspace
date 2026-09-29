// =============================================================================
// WorkspaceSwitcher — the kinds of workspace and the workspaces of the chosen
// one: a composed widget, an umbrella over two of its own — the kinds, as a
// tree (WorkspaceKinds), and the chosen kind's workspaces, as a table
// (WorkspaceInstances) — side by side, or one above the other when its box is
// narrow.
//
// It is their host (A Widget Is an Operational Unit: host is a role). Each is
// lent a box of the switcher's own, and grafted: its DomOps party into the
// switcher's, its focus party into the switcher's — which the switcher offers
// its own host, whole. And it hands the keys on between them: → past a kind
// gives them to the table, ← past the table's left edge back to the tree.
//
// Where they meet is a workspace choice party of the switcher's own: a scope
// (Messaging Parties Are Joined Top-Down). The tree tells it a kind is chosen,
// and the table shows that kind's workspaces. The scope is linked to the party
// the switcher is given, and its secretary (WorkspaceSwitcherSecretary) keeps
// the edge: a kind chosen in the scope goes up, as the switcher's own word
// there, and so does every workspace asked to open — opening is not the
// switcher's to do, but whoever hears the party above say so; a question is
// answered in the scope; and what the party above says is chosen, both are
// told. Joining is top-down: the switcher finishes its own — the scope made and
// linked, and the party above asked what is chosen — before its subordinates
// join, the tree first; leaving is the other way round. Joined with nothing
// given, the scope stands alone, and the two still meet in it; not joined at
// all, each works alone.
//
//   new WorkspaceSwitcher(container, params)   params: none
//   switcher.root  switcher.roots   { dom, focus }: its own, its subordinates' grafted in them
//   (its kind declares, in Java, the union of its subordinates' types: workspace-choice)
//   switcher.join(given)   given: { [type name]: party }; a second join without a leave is refused
//   switcher.leave()
//   switcher.scope()   its own workspace choice party, once joined: kept across a leave
//   switcher.selection()  what is chosen: { workspaceKind, workspaceId } - the kind its scope
//                         has, and the workspace at the table's cursor when it is of that kind,
//                         else "", the kind's own - fresh on the new row; null while nothing is chosen
//   switcher.prefer(workspaceId)   the workspace the table's cursor lands on whenever its kind is shown
//   switcher.rename()  switcher.remove()   the table's F2 and Delete, as a host's verbs
//   switcher.activate()   the tree is asked for the keys
//   switcher.dispose()
// =============================================================================

const _workspaceSwitcherOwner = Object.freeze({ toString: () => "workspaceSwitcher" });
var _workspaceSwitchersMade = 0;

class WorkspaceSwitcher {
    constructor(container, params) {
        if (!container || typeof container.appendChild !== "function") throw new Error("[WorkspaceSwitcher] a container is required: the one its page lends it");
        var name = "workspaceSwitcher-" + (++_workspaceSwitchersMade), self = this;
        this._dom = domOpsParties.mobile(name);
        this._dom.activate(_workspaceSwitcherOwner);
        var root = this._dom.createElement("root", "div");
        css.addClass(root, sw_split);
        root.setAttribute("role", "region");
        root.setAttribute("aria-label", "Workspaces: the kinds, and the chosen kind's");
        var kindsBox = this._dom.createElement("kindsBox", "div");
        css.addClass(kindsBox, sw_kinds);
        var instancesBox = this._dom.createElement("instancesBox", "div");
        css.addClass(instancesBox, sw_instances);
        root.appendChild(kindsBox);
        root.appendChild(instancesBox);
        container.appendChild(root);
        this.root = root;
        // its subordinates, each lent a box of its own, their roots grafted into the switcher's
        this._focusParty = focusParties.mobile(name);
        this._kinds = new WorkspaceKinds(kindsBox, {});
        this._instances = new WorkspaceInstances(instancesBox, {});
        this._dom.graft("kinds", this._kinds.roots.dom);
        this._dom.graft("instances", this._instances.roots.dom);
        this._focusParty.root.graft("kinds", this._kinds.roots.focus);
        this._focusParty.root.graft("instances", this._instances.roots.focus);
        // the keys handed on between them, past their edges
        this._kinds.edge(function (direction) { if (direction === "right") self._instances.activate(); });
        this._instances.edge(function (direction) { if (direction === "left") self._kinds.activate(); });
        this.roots = Object.freeze({ dom: this._dom, focus: this._focusParty });
        this._scope = null;
        this._joined = false;
    }

    /** Joined: its own first - the scope made, linked above, and the party above asked - then its subordinates, in order. */
    join(given) {
        if (this._joined) throw new Error("[WorkspaceSwitcher] joined already: leave first");
        this._joined = true;
        if (!this._scope) this._scope = new MessagingParty(WORKSPACE_CHOICE, WorkspaceSwitcherSecretary);
        var above = given && given[WORKSPACE_CHOICE.name];
        if (above) this._scope.link(above, "workspaceSwitcher").tell({ kind: "CurrentRequested" });
        var mine = {};
        mine[WORKSPACE_CHOICE.name] = this._scope;
        this._kinds.join(mine);
        this._instances.join(mine);
    }

    /** Left the other way round: its subordinates, the last first, then its own link above. The scope's memory is kept. */
    leave() {
        if (!this._joined) return;
        this._instances.leave();
        this._kinds.leave();
        this._scope.unlink();
        this._joined = false;
    }

    scope() { return this._scope; }

    selection() {
        var chosen = this._scope ? this._scope.state().choice.chosen : null;
        if (!chosen) return null;
        var at = this._instances.cursor();
        var ours = at && at.workspaceKind === chosen;
        return Object.freeze({ workspaceKind: chosen, workspaceId: ours ? at.workspaceId : "", fresh: !!(ours && at.fresh) });
    }

    prefer(workspaceId) { this._instances.prefer(workspaceId); }

    /** The workspace at the table's cursor, its name taken - a host's Rename; false when there is none. */
    rename() { return this._instances.rename(); }

    /** The workspace at the table's cursor, asked to be deleted - a host's Delete; false when there is none. */
    remove() { return this._instances.remove(); }

    /** Asked for the keys: its tree is asked. A host never claims for what it holds. */
    activate() { this._kinds.activate(); }

    dispose() {
        this.leave();
        this._instances.dispose();
        this._kinds.dispose();
        this._focusParty.dissolve();
        this._dom.dissolve();
    }
}
