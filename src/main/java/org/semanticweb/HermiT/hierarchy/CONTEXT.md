# `hierarchy/` — Classification & instance realisation

This package answers the *aggregate* questions — the ones that need many
satisfiability tests rather than one:

- **Classification**: compute the subsumption hierarchy of all named classes (and,
  analogously, object/data properties).
- **Realisation**: compute, for every individual, the classes it belongs to (and
  the property relationships it participates in).

Both are built entirely on top of the tableau's `isSatisfiable(...)` primitive.
The whole point of this package is to **do as few of those tests as possible**,
because each one can be expensive.

> Background: every test reduces to model existence
> ([`../CONTEXT.md`](../CONTEXT.md), [`../tableau/CONTEXT.md`](../tableau/CONTEXT.md)).
> The public task methods live in `Reasoner.java`; this package supplies the
> algorithms they call.

## What a hierarchy is

- `HierarchyNode<E>` — one **equivalence class** of mutually-subsuming elements:
  a representative, the full set of equivalent elements, and its direct
  parent/child nodes (with cached ancestor/descendant access).
- `Hierarchy<E>` — the DAG of those nodes, with a distinguished top (`owl:Thing`)
  and bottom (`owl:Nothing`) node and a `getNodeForElement` index. It is generic
  in `E` and can be **transformed** (`transform(...)`) from one element type to
  another — which is exactly how a hierarchy of auxiliary *concepts* is turned
  into a hierarchy of *roles* (see below).

## Classification — two algorithms

The fundamental fact: `C ⊑ D` iff `C(a) ∧ ¬D(a)` is unsatisfiable. The naïve
algorithm tests every ordered pair of named classes — O(*n*²) tableau calls. Both
algorithms here avoid most of that.

### `DeterministicClassification`

Used when the ontology is **Horn / deterministic** (`DLOntology.isHorn()` — no
disjunctive branching). It exploits a powerful shortcut: run **one** satisfiability
test with `C(a)` asserted and then simply **read off the node's label** from the
extension table — every named concept that ended up on `a` is a subsumer of `C`.
So one test yields *all* of `C`'s subsumers at once. From the resulting
subsumption graph it builds the hierarchy with **Tarjan's SCC algorithm** (SCCs =
equivalence classes) and keeps only minimal parents (transitive reduction).

### `QuasiOrderClassification`

Used for **non-deterministic** ontologies (where reading off the label is not
complete, because a subsumption might hold only across all disjunctive branches).
It maintains two relations as it goes:

- **known** subsumptions — definitely hold (seeded from *told subsumers*, see
  below);
- **possible** subsumptions — might hold, still to be tested.

It then drives the number of expensive tableau tests down by processing the
hierarchy **leaf-first**, testing a node only against its candidate parents, and
resolving only the genuinely *unknown* possible subsumptions — often within a
small sub-hierarchy of the unknown candidates. A final transitive reduction yields
the published DAG. `Reasoner.forceQuasiOrderClassification` can force this path.

### Told subsumers — the cheap seed

Before any tableau call, subsumptions that are *syntactically obvious* are read
straight off the DL-clauses (e.g. `A(X) → B(X)` tells us `A ⊑ B`). These **told
subsumers** seed the "known" relation and slash the number of tests both
algorithms must perform. `HierarchySearch.findPosition` (with `findParents` /
`findChildren`) is the generic routine for slotting a *single* new element
(e.g. an ad-hoc complex class expression) into an existing hierarchy via a
`Relation<E>` subsumption oracle that calls the tableau.

## Property classification — by reduction to concepts

There is no separate role calculus. To classify object properties, `Reasoner`
introduces, for each role `R`, an auxiliary concept `C_R ≡ ∃R.fresh`, classifies
those auxiliary concepts with the machinery above, and then **transforms** the
resulting concept hierarchy back into a role hierarchy
(`Hierarchy.transform(...)`). `R ⊑ S` iff `C_R ⊑ C_S`. Data properties are handled
the same way. This reuse is why the package is generic in `E`.

## Realisation & the `InstanceManager`

`InstanceManager` computes individual types and property instances, again on top
of satisfiability tests but with heavy caching and **incremental, chunked**
processing so large ABoxes don't blow up memory:

- It holds the (transitively reduced) class and role hierarchies and a set of
  cached `ExtensionTable.Retrieval`s (concept-bound, individual-bound,
  role-bound) for fast fact lookup.
- It separates **known** instances (definitely hold) from **possible** ones
  (need a confirming/refuting test), so it only spends tableau calls on the
  genuinely uncertain memberships.
- `realize(...)` assigns each individual its (direct and indirect) types;
  `realizeObjectRoles(...)` does the same for role instances;
  `getTypes`/`getInstances` serve the queries.
- It processes individuals in chunks: add "read-off" axioms, saturate, read the
  extension tables, then clear and move on.

`getTypes(individual, direct=true)` needs the classified hierarchy to compute
*direct* types (most-specific classes); indirect types just gather ancestors.

## File map

| File | Role |
|---|---|
| `Hierarchy` / `HierarchyNode` | the hierarchy DAG and its equivalence-class nodes; `transform`, traversal |
| `DeterministicClassification` | one-test-per-class read-off + Tarjan SCC; for Horn ontologies |
| `QuasiOrderClassification` | known/possible subsumptions, leaf-first testing; for non-deterministic ontologies |
| `HierarchyBuilder` | shared hierarchy-construction support |
| `HierarchySearch` | place a single element via `findParents`/`findChildren` over a subsumption oracle |
| `InstanceManager` | individual realisation: known/possible instances, chunked, cached retrievals |
| (others) | progress monitors and supporting structures |

## Connects to

- **Calls** `tableau/` `isSatisfiable(...)` for every subsumption / instance test.
- **Reads** `model/` clauses to extract told subsumers, and the signature to know
  which elements to classify.
- **Invoked by** `Reasoner` (`classifyClasses`, `classifyObjectProperties`,
  `realise`, `getTypes`, `getInstances`, …) and indirectly by
  `EntailmentChecker`.
