# `tableau/` — The hypertableau calculus engine

This is the heart of HermiT. Everything else in the reasoner exists to feed this
package a `DLOntology` and to interpret its single answer: **does a model exist?**
The engine implements the **hypertableau calculus** — it attempts to build a
model of the clausified ontology as a labelled graph of nodes, and reports
satisfiable iff it succeeds.

> Prerequisites: the representation it runs on ([`../model/CONTEXT.md`](../model/CONTEXT.md))
> and the pipeline that produced it ([`../CONTEXT.md`](../CONTEXT.md)). Termination
> and existential handling are split out into
> [`../blocking/CONTEXT.md`](../blocking/CONTEXT.md) and
> [`../existentials/CONTEXT.md`](../existentials/CONTEXT.md).

## The model being built

A candidate model is a set of **`Node`s** (individuals — named, anonymous tree,
nominal, concrete/data, or graph nodes; see `NodeType`) carrying labels, related
by role edges. Both labels and edges are stored uniformly as **tuples** in
**extension tables**:

- binary tuples `⟨concept, node⟩` — concept assertions (the node's label);
- ternary tuples `⟨role, node, node⟩` — role assertions (edges);
- *n*-ary tuples `⟨graph, node₁, …⟩` — description-graph instances.

`ExtensionManager` is the façade over these tables; `ClashManager` watches every
tuple insertion for a contradiction. The `Tableau` object owns the whole mutable
run state.

## The calculus loop

Entry: `Tableau.isSatisfiable(...)` (line 258) loads the ABox (the permanent
facts, plus any per-test/additional facts) and calls `runCalculus()` (line 372),
which simply iterates `doIteration()` (line 410) until there is no more work, then
returns *satisfiable = no clash*. (For non-exact blocking it does a final
validation pass before declaring success.)

`doIteration()` applies the rules in a fixed priority order — cheap/deterministic
work first, expensive/nondeterministic work last:

```
 doIteration():
   1. nominal introduction        m_nominalIntroductionManager.processAnnotatedEqualities()
   2. saturate deterministically  while propagateDeltaNew() && no clash:
          • description-graph constraints   (if any graphs)
          • HYPERRESOLUTION                 m_permanentHyperresolutionManager.applyDLClauses()
          •   "  for the additional ontology (if present)
          • unknown-datatype semantics + datatype constraint checks
          • nominal introduction again
        → if anything changed, return true (re-enter)
   3. existential expansion        m_existentialExpansionStrategy.expandExistentials(false)
        → if it changed anything, return true
   4. branch on a ground disjunction (see below)
        → if one was processed, return true
   5. if there is a CLASH → backjump (see below); else return false (done)
```

The `propagateDeltaNew()` discipline is the efficiency core: rules fire only
against **newly added** tuples (the `DELTA_OLD` view), never re-deriving from
facts already processed. Saturation reaches a fixpoint of deterministic
consequences before any branching is even considered.

### 1. Hyperresolution — applying DL-clauses

`HyperresolutionManager` compiles every `DLClause` once, up front, into an
executable `DLClauseEvaluator` indexed by its body predicates (with
**guard-concept** optimisations so a clause is only evaluated for nodes that
already carry the right concept). `applyDLClauses()` (line 176) streams over the
delta tuples and fires matching clauses. A clause fires only when **all** body
atoms are simultaneously satisfied by a variable binding — that is what makes
hyperresolution derive deterministic consequences without speculative branching.
A fired clause:

- with a single head atom → adds that atom (deterministic);
- with an empty head → signals a clash;
- with a multi-atom head → registers a `GroundDisjunction` for step 4.

### 2. Existential expansion

When a node carries an unsatisfied `≥ n R.C`, it needs successors. The pluggable
`ExistentialExpansionStrategy` (package `existentials/`) decides whether the
restriction is already satisfied, and if not creates `n` fresh tree successors
(or reuses nodes). Crucially, `expandExistentials()` first asks the
`BlockingStrategy` to recompute blocking, and **skips blocked nodes** — that is
the termination mechanism (package `blocking/`).

### 3 & 4. Nondeterminism — disjunctions and merges

Two sources of choice:

- **Disjunctive clause heads** become `GroundDisjunction`s. Step 4 picks the next
  unprocessed one, and if it isn't already satisfied, pushes a
  `DisjunctionBranchingPoint` and asserts the first (heuristically sorted)
  disjunct. On backtracking, `startNextChoice` asserts the negation of the tried
  disjuncts and moves to the next.
- **At-most restrictions** (`≤ n R.C`) force **merges**: if a node has more than
  `n` successors, two of them must be the same. This surfaces as an
  `AnnotatedEquality`, processed via `MergingManager`, which is itself a
  nondeterministic choice of *which* pair to merge.

### 5. Clash detection and dependency-directed backjumping

`ClashManager.tupleAdded(...)` fires on every insertion and recognises:
`owl:Nothing` (or `¬rdfs:Literal`) on a node; `x ≉ x`; a concept and its negation
on the same node; a role and its negation on the same edge; datatype conflicts.
When it finds one it records the **clash dependency set** and sets the clash flag.

Every derived fact carries a **`DependencySet`**: the exact set of branching
points it transitively depends on. On a clash, `doIteration()` reads the clash's
dependency set, takes its **maximum branching point**, and:

```
   if that branching point ≤ the non-backtrackable point → return false   (UNSAT)
   else  backtrackTo(thatPoint)                                            (line 534)
         currentBranchingPoint.startNextChoice(clashDependencySet)
```

This is **backjumping**, not chronological backtracking: the engine leaps
directly past every choice the clash did *not* depend on, instead of undoing them
one at a time. `backtrackTo` restores everything to the checkpoint — truncates
the extension tables, un-merges/un-prunes nodes, destroys nodes created since,
rewinds the ground-disjunction queue — and the next alternative is tried.
Optional **disjunction learning** (`Configuration.useDisjunctionLearning`)
remembers clausal conflicts to prune future search.

## State management: how undo is made cheap

The engine is allocation- and undo-sensitive, so the data structures are built
for fast rollback:

- **Extension tables** (`ExtensionTable`, `ExtensionTableWithTupleIndexes`,
  `ExtensionTableWithFullIndex`) store tuples in flat `TupleTable` arrays with
  `TupleIndex` tries for retrieval. They expose **views** —
  `EXTENSION_THIS`/`EXTENSION_OLD`/`DELTA_OLD`/`TOTAL` — so saturation can target
  only the delta. Backtracking is just *truncating* the arrays to a saved length.
- **`Node`s** are pooled (a free-list, `m_firstFreeNode`) and reused; a node
  records its blocker, its merge target (`getCanonicalNode()` follows merge
  chains), its unprocessed existentials and label counts.
- **`DependencySet`** comes in a `PermanentDependencySet` (interned, ref-counted,
  shared) and a transient `UnionDependencySet` (for combining sets during a tuple
  op), built via `DependencySetFactory`.
- **`BranchingPoint`** snapshots exactly enough state (last node, last
  merged/pruned node, ground-disjunction queue position) to roll back to it.

## The managers (rule implementations)

| Manager | Rule / responsibility |
|---|---|
| `HyperresolutionManager` | compile & fire DL-clauses (the `→` rule) |
| `ExistentialExpansionManager` | create/reuse successors for `≥ n R.C`; track functionality |
| `MergingManager` | merge two nodes (the `≤ n R.C` / equality rule), by `NodeType` precedence |
| `NominalIntroductionManager` | the **NN rule**: introduce/merge nominal nodes from `AnnotatedEquality`s |
| `DescriptionGraphManager` | enforce description-graph constraints |
| `DatatypeManager` | datatype constraint checking + unknown-datatype semantics |
| `ClashManager` | detect contradictions on tuple insertion |
| `ExtensionManager` | the façade over all extension tables; `propagateDeltaNew()`, clash flag |

Supporting types: `GroundDisjunction`/`GroundDisjunctionHeader` (a pending
disjunctive choice and its disjunct ordering), `DLClauseEvaluator` (a compiled
clause), `InterruptFlag`/`InterruptCurrentTaskException` (cancellation/timeout),
`ReasoningTaskDescription` (what the current run is for, for monitoring).

## Permanent vs. additional ontology

A `Tableau` is built around a fixed **permanent** `DLOntology` (the TBox+RBox+ABox
from clausification) and can host an **additional** `DLOntology`
(`m_additionalDLOntology`, with its own `HyperresolutionManager`) carrying
per-query facts — e.g. the `C(a)` of a satisfiability test, or extra ABox
assertions for incremental reasoning. This is why most `Reasoner` tasks can reuse
one prepared tableau instead of re-clausifying.

## Reading a trace / debugging

`monitor/` provides timing/counting/progress observers; `debugger/` adds an
interactive command shell (set via `Configuration.tableauMonitorType`). A
`TableauMonitor` is notified at every interesting point — iteration start/finish,
clause firing, disjunct processing, clash, branching — without altering the run.

## Connects to

- **Consumes** `model/` (`DLClause`, `Atom`, `DLOntology`).
- **Delegates** termination to `blocking/` and successor creation to
  `existentials/`; datatype checks to `datatypes/`.
- **Is driven by** `Reasoner`/`hierarchy/`/`EntailmentChecker`, every one of which
  ultimately calls `isSatisfiable(...)`.
