# `structural/` — Structural transformation & clausification

This package is the **front of the reasoning pipeline**: it turns an
`OWLOntology` (OWL API objects) into a `DLOntology` of **DL-clauses** and ABox
facts that the hypertableau engine can run on. Nothing here reasons; it
*prepares* the problem so that reasoning is cheap and uniform.

> See the pipeline overview in [`../CONTEXT.md`](../CONTEXT.md) and the target
> representation in [`../model/CONTEXT.md`](../model/CONTEXT.md).

## Why a transformation step exists

OWL axioms are expressive and redundant: deeply nested class expressions,
complex role hierarchies, transitivity, role chains, nominals, keys, datatypes,
SWRL rules. The calculus, by contrast, wants one thing: small **DL-clauses**
(`body conjunction → head disjunction`). The job of this package is to bridge that
gap **without changing the set of models** — the `DLOntology` is satisfiable iff
the original ontology is, and it entails the same things over the original
signature.

Two transformations do the heavy lifting:

1. **Structural transformation (a.k.a. normalisation).** Rewrite every axiom into
   negation normal form and break nested subexpressions apart by introducing
   *fresh* atomic concepts (`X ≡ subexpression`). This keeps clauses shallow and
   bounds their number linearly, instead of the exponential blow-up a naïve CNF
   would cause.
2. **Clausification.** Translate the normalised, shallow axioms into `DLClause`
   objects.

## The entry point and the flow

`OWLClausification.preprocessAndClausify(rootOntology, descriptionGraphs)` drives
the whole pipeline and returns the `ObjectPropertyInclusionManager` plus the built
`DLOntology`. The stages, in order:

1. **Normalise** — `OWLNormalization` walks every axiom with an `AxiomVisitor`,
   collects the signature, converts class/data expressions to NNF
   (`ExpressionManager`/`getNNF`, `getSimplified`), and rewrites complex
   inclusions into disjunctions of (mostly) literal concepts, minting fresh names
   for the parts that are still complex. Results land in an `OWLAxioms` container,
   partitioned into concept inclusions, object/data property inclusions, facts,
   reflexive/irreflexive/asymmetric declarations, has-key axioms and SWRL rules.
   A few special forms (`∃R.{a…}`, `∀R.{a…}`, `∀R.¬{a}`) are deliberately *kept*
   intact because they clausify more efficiently directly.

2. **Axiomatise built-in properties** — `BuiltInPropertyManager` adds the axioms
   defining `owl:topObjectProperty` / `owl:bottomObjectProperty` (and the data
   counterparts) *only if* they actually occur
   (`axiomatizeBuiltInPropertiesAsNeeded`).

3. **Compile complex roles into automata** — `ObjectPropertyInclusionManager` is
   where transitivity and **role chains** (`SubPropertyChainOf`) are handled. It
   classifies each object property as **simple** (not involved in any chain) or
   **non-simple/complex**, and builds a **finite automaton** per complex property
   (`createAutomata`, using the rationals/automata library) that captures the
   regular language of role compositions implied by the chains.

4. **Rewrite complex-role axioms away** — `rewriteAxioms` replaces `∀R.C` over a
   complex `R` with internal concepts generated from `R`'s automaton, so that
   transitivity/chains are simulated by ordinary concept inclusions the clause
   engine understands. `rewriteNegativeObjectPropertyAssertions` first turns
   negative assertions over complex roles into concept assertions, so the
   rewriting stays sound.

5. **Detect expressivity** — `OWLAxiomsExpressivity` scans the normalised axioms
   for the features (inverse roles, at-most restrictions, nominals, datatypes,
   SWRL) that become the `DLOntology` flags driving strategy selection downstream.

6. **Clausify** — `OWLClausification.clausify(...)` produces the `DLClause`s:
   - role inclusions → role-atom clauses;
   - asymmetry/reflexivity/irreflexivity/disjointness → (mostly empty-headed)
     constraint clauses;
   - concept inclusions → clauses, via the nested **`NormalizedAxiomClausifier`**
     visitor (turns a disjunction of class literals into head/body atoms);
   - data-range inclusions → via `NormalizedDataRangeAxiomClausifier`;
   - `HasKey` → `clausifyKey(...)`;
   - ABox assertions (class/property/(in)equality) → ground facts, via the nested
     **`FactClausifier`**;
   - SWRL rules → via the nested **`NormalizedRuleClausifier`** (Lloyd–Topor for
     disjunctive heads).

7. **Assemble** — collect the signature (atomic concepts, atomic/complex object
   roles, data roles, individuals, description graphs, data-property assertions)
   and construct the `DLOntology`.

## File-by-file

| File | Responsibility |
|---|---|
| `OWLClausification` | Orchestrator. `preprocessAndClausify` + `clausify`; hosts the nested clausifier visitors (`NormalizedAxiomClausifier`, `NormalizedDataRangeAxiomClausifier`, `FactClausifier`, `NormalizedRuleClausifier`). |
| `OWLNormalization` | Structural transformation: NNF + fresh-name introduction; `AxiomVisitor`, `ClassExpressionNormalizer`, `DataRangeNormalizer`. |
| `OWLAxioms` | Container for normalised axioms, partitioned by kind; defines `ComplexObjectPropertyInclusion` and `DisjunctiveRule`. |
| `OWLAxiomsExpressivity` | Computes the feature flags (inverses, at-most, nominals, datatypes, rules). |
| `ExpressionManager` | NNF + simplification utilities for class expressions and data ranges. |
| `BuiltInPropertyManager` | Axiomatises top/bottom object & data properties on demand. |
| `ObjectPropertyInclusionManager` | Simple-vs-complex role analysis; automata for transitivity/role chains; rewrites complex-role axioms and negative assertions. |
| `ReducedABoxOnlyClausification` | Incremental path: clausify *only* ABox facts, leaving the TBox/RBox untouched (used for per-query/incremental ABox updates). |

## Key ideas worth remembering

- **Fresh concepts everywhere.** Most internal names (`internal:…`) you see in a
  tableau trace were introduced here to flatten nested expressions. They are
  filtered out of user-visible results (`Prefixes.isInternalIRI`).
- **Roles are compiled, not interpreted.** Transitivity and role chains never
  reach the tableau as such — they are pre-compiled into automata and rewritten
  into concept inclusions. This is why the calculus only needs to handle *simple*
  role atoms at runtime.
- **The output is immutable and interned.** Everything produced lands as interned
  `model/` objects (see that package's note on `InterningManager`).
- **Incremental reuse.** When only the ABox changes, `ReducedABoxOnlyClausification`
  avoids re-running the expensive TBox/RBox normalisation and automata
  construction; the new facts are layered onto a prepared `Tableau` as an
  "additional" ontology.

## Connects to

- **Produces** the `DLOntology` and `DLClause`s in [`../model/`](../model/CONTEXT.md).
- **Invoked by** `Reasoner.loadOntology()`.
- **Feeds** the strategy choices in `Configuration` via the expressivity flags it
  computes, which the [`../tableau/`](../tableau/CONTEXT.md) construction reads.
