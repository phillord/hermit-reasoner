# `blocking/` — Termination

Blocking is **why the calculus terminates**. Existential restrictions
(`≥ n R.C`) create fresh successor nodes; on a cyclic ontology that would build an
infinite tree forever. Blocking detects when a new node is "the same as" an
existing one and tells the existential-expansion machinery *not to expand it*. The
already-existing node stands in as a witness for the blocked node's successors.

> Called from the tableau loop: `ExistentialExpansionStrategy.expandExistentials`
> recomputes blocking before expanding, and skips blocked nodes
> ([`../existentials/CONTEXT.md`](../existentials/CONTEXT.md),
> [`../tableau/CONTEXT.md`](../tableau/CONTEXT.md)).

## The core idea

A tree node `y` is **directly blocked** by an earlier node `x` when their
*relevant labels* match — so the subtree below `y` could only repeat what is
already guaranteed below `x`. Once `y` is blocked, its existentials are not
expanded, the tree stops growing there, and termination follows from the fact
that there are only finitely many possible labels.

"Relevant label" is exactly what distinguishes the strategies, and it is a
soundness/completeness knob:

- **Single blocking** (`SingleDirectBlockingChecker`): the label is just the
  node's set of atomic concepts. Cheapest; sound only when there are no inverse
  roles.
- **Pairwise blocking** (`PairWiseDirectBlockingChecker`): the label also includes
  the parent's atomic concepts and the role labels on the edge to/from the parent
  (`FromParent`/`ToParent`). Required once **inverse roles** are present, because
  an inverse role lets a successor constrain its predecessor.
- **Validated (core) blocking** (`Validated{Single,Pairwise}DirectBlockingChecker`
  + `BlockingValidator`): distinguishes **core** from non-core assertions and
  *checks* that a proposed block does not violate any DL-clause constraint before
  trusting it. Required for **nominals**, where label-equality alone is not enough
  to guarantee a real model exists.

Atomic-concept and role **sets are interned** via `SetFactory`, so label
comparison is reference equality (`==`) rather than set comparison — the same
performance trick used throughout `model/`.

## Where blockers come from: anywhere vs. ancestor

- `AncestorBlocking` — a node may only be blocked by one of its **proper
  ancestors** (walk up the parent chain). Simple; can build larger models.
- `AnywhereBlocking` — a node may be blocked by **any** earlier suitable node,
  located in O(1) via a `BlockersCache` (a hash table of active, non-blocked
  candidate blockers keyed by blocking hash). Produces smaller models and is the
  usual default.

Beyond *direct* blocking, a directly-blocked node also **indirectly** blocks its
own descendants (a blocked node's subtree is never expanded).

## How a blocking pass works

`computeBlocking(finalChance)` is incremental. It tracks the first node whose
label changed since the last pass (`m_firstChangedNode`), removes affected nodes
from the cache, then for each changed node decides its blocker:

1. if the parent is blocked → the node is blocked (inherited);
2. else if a `BlockingSignatureCache` hit → blocked by a cached signature;
3. else ask the `BlockersCache` for a matching earlier node;
4. if still unblocked and it can be a blocker → add it to the cache.

The `finalChance` flag is used by `runCalculus`: when a non-exact strategy (core
blocking) thinks it is done, it gets one last pass to *validate* every block;
invalid blocks are broken and expansion resumes. This is what
`BlockingStrategy.isExact()` reports — exact strategies (single/pairwise) never
need this; validated ones do.

## The signature cache

`BlockingSignatureCache` (+ `BlockingSignature`) remembers the labels of
unblocked nodes from a *previous* satisfiability run so that, in a later run, a
node can be blocked immediately by a cached signature without re-deriving it. This
accelerates the many tests classification performs. It is only safe when there are
**no nominals and no core blocking** (those make labels run-specific), so the
tableau disables it in those cases.

## Validated blocking in one paragraph

`BlockingValidator` answers: *if I block `y` by `x`, is the resulting model still
a model?* It checks the DL-clauses that would have fired on `y`'s
parent/successors (matching clause variables `X`/`Y`/`Z` against the blocker, the
blocked node and its parent) and flags at-least/clause violations. If blocking `y`
would violate a constraint that expanding `y` would have satisfied, the block is
**invalid** and `y` must be expanded after all. Caching of "already checked"
parents (`hasAlreadyBeenChecked`) keeps this affordable.

## File map

| File | Role |
|---|---|
| `BlockingStrategy` | strategy interface: `computeBlocking`, assertion/node lifecycle hooks, `isExact` |
| `AnywhereBlocking` / `AncestorBlocking` | the two blocker-location strategies (+ `BlockersCache` inner class) |
| `AnywhereValidatedBlocking` | anywhere blocking with constraint validation (core blocking) |
| `DirectBlockingChecker` | interface: is `x` a valid direct blocker of `y`? hashing & candidacy |
| `SingleDirectBlockingChecker` / `PairWiseDirectBlockingChecker` | the two label definitions |
| `Validated{Single,Pairwise}DirectBlockingChecker` | the core/non-core variants |
| `BlockingValidator` | validates that a block yields a real model |
| `BlockingSignature` / `BlockingSignatureCache` | cross-run cache of blocker labels |
| `SetFactory` | interns concept/role sets for `==` comparison |

## Choosing a strategy

`Configuration` exposes `BlockingStrategyType`
(`ANCESTOR`/`ANYWHERE`/`SIMPLE_CORE`/`COMPLEX_CORE`/`OPTIMAL`) and
`DirectBlockingType` (`SINGLE`/`PAIR_WISE`/`OPTIMAL`); `Reasoner` translates these
plus the `DLOntology` feature flags into the concrete objects above — pairwise
when inverse roles are present, validated core blocking when nominals are present,
plain anywhere/single otherwise.
