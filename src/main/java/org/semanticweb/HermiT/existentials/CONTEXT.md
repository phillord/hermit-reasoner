# `existentials/` — Satisfying existential restrictions

When a node carries an unsatisfied `≥ n R.C` (an `AtLeast` concept), the model
must provide `n` distinct `R`-successors that are instances of `C`. This package
decides **whether** the restriction is already satisfied and, if not, **how** to
satisfy it. It is the rule that *grows* the model — and the place where the
tableau cooperates with `blocking/` to *stop* growing it.

> Driven from the tableau loop's step 3
> ([`../tableau/CONTEXT.md`](../tableau/CONTEXT.md)); termination comes from
> [`../blocking/CONTEXT.md`](../blocking/CONTEXT.md).

## The contract

`ExistentialExpansionStrategy` is the interface the `Tableau` holds. Its key
method is `expandExistentials(finalChance)`, which the calculus calls every
iteration. The strategy also answers two characterising questions:

- `isDeterministic()` — does expansion ever introduce a branching point?
- `isExact()` — does the strategy (together with its blocking) only ever produce
  *valid* blocks? If not, `runCalculus` will request a final validation pass
  (`finalChance == true`).

## The shared expansion loop

`AbstractExpansionStrategy` implements the loop all strategies share:

1. **Recompute blocking first** — `m_blockingStrategy.computeBlocking(finalChance)`.
   This is the coupling point: expansion is only ever done on **active,
   non-blocked** nodes.
2. Walk the nodes; for each non-blocked node with unprocessed existentials,
   classify each `AtLeast` by `isSatisfied(...)`:
   - **`PERMANENTLY_SATISFIED`** — witnesses exist via permanent assertions
     (parent/child/root) that later rules cannot retract → mark the existential
     processed, done.
   - **`CURRENTLY_SATISFIED`** — witnesses exist but could be invalidated by a
     later nominal/merge step → leave it; re-check next time.
   - **`NOT_SATISFIED`** — no witnesses → call `expandExistential(...)`, which the
     concrete strategy implements.
3. `ExistsDescriptionGraph` existentials are handled on a separate path.

`isSatisfied` is careful about cardinality: for `n = 1` it looks for any
`R`-successor that is a `C`; for `n > 1` it must find `n` *distinct* successors
each satisfying `C`. "Permanent vs. current" is the subtle part — a witness that
depends on a choice or a mergeable node is only *currently* satisfying.

## The strategies

| Strategy | Behaviour | Determinism |
|---|---|---|
| `CreationOrderStrategy` | Breadth-first. Process the oldest node's existentials fully before moving on; **always create fresh successor nodes** (no reuse). | deterministic, exact |
| `IndividualReuseStrategy` | Try to **reuse** an existing node before creating one: reuse the parent (for `≥ 1`), or reuse a node created for the same target concept by an earlier expansion; fall back to normal creation. Smaller models, more bookkeeping. | may branch (non-deterministic), via `IndividualReuseBranchingPoint` |

`CreationOrderStrategy` is the straightforward, predictable default.
`IndividualReuseStrategy` trades determinism for a smaller model: when it reuses a
node it pushes a branching point, so that if the reuse leads to a clash the engine
can backtrack and create a fresh node instead. The **EL** configuration uses
`IndividualReuseStrategy` in a deterministic mode tuned for the OWL 2 EL profile.

Actual node creation/reuse and functionality bookkeeping are delegated to the
tableau's `ExistentialExpansionManager` (`expand`, `doNormalExpansion`,
`tryFunctionalExpansion`, `markExistentialProcessed`); the strategy decides
*policy*, the manager performs the *mechanism*.

## Why this is a strategy at all

The size and shape of the constructed model dominate reasoning cost. Breadth-first
fresh creation is simple and always correct; individual reuse can collapse a large
model into a much smaller one but introduces nondeterminism and extra checks. Making
this pluggable lets `Configuration.ExistentialStrategyType`
(`CREATION_ORDER`/`INDIVIDUAL_REUSE`/`EL`) pick the right trade-off per ontology
without touching the calculus.

## File map

| File | Role |
|---|---|
| `ExistentialExpansionStrategy` | the interface the `Tableau` depends on |
| `AbstractExpansionStrategy` | shared loop: blocking-first, `isSatisfied` classification, dispatch |
| `CreationOrderStrategy` | breadth-first, always-create |
| `IndividualReuseStrategy` | parent/model reuse with branching fallback (+ EL mode) |

## Connects to

- **Calls into** `blocking/` (recompute before expanding) and the tableau's
  `ExistentialExpansionManager` (perform the expansion).
- **Operates on** `model/` `AtLeast` concepts and `tableau/` `Node`s.
- **Selected by** `Reasoner` from `Configuration`.
