<!-- This file documents the reasoning architecture. It is hand-maintained;
     update it when the pipeline or calculus changes shape. -->

# HermiT — How reasoning works

HermiT is a conformant **OWL 2 DL** reasoner. Its inference engine is a
**hypertableau calculus** (Motik, Shearer & Horrocks). This document explains
the reasoning architecture end to end: how an `OWLOntology` becomes something the
calculus can run on, what the calculus actually does, and how the everyday
reasoning tasks (consistency, satisfiability, classification, realisation,
entailment) are all reduced to a single primitive — a **satisfiability test**.

Each subpackage that participates in reasoning has its own `CONTEXT.md`; this
file is the hub that ties them together. Read this first, then dive into the
package docs as needed.

## The one idea to hold onto

> **Almost everything HermiT computes is a model-existence question, and a model-existence
> question is answered by `Tableau.isSatisfiable(...)`.**

- *Is the ontology consistent?* → is the ABox satisfiable?
- *Is class `C` satisfiable?* → is `C(a)` satisfiable for a fresh `a`?
- *Does `C` subsume `D`?* → is `D(a) ∧ ¬C(a)` **un**satisfiable?
- *Is individual `a` of type `C`?* → is `¬C(a)` together with the ABox **un**satisfiable?
- *Does the ontology entail axiom `α`?* → is `ontology ∧ ¬α` **un**satisfiable?

Classification and realisation are just *disciplined sequences* of these tests,
arranged to do as few of them as possible. So the whole reasoner is a thin
orchestration layer wrapped around one expensive primitive.

## The reasoning pipeline

```
 OWLOntology (OWL API objects)
        │
        │  structural transformation + clausification
        │  ── package: structural/  (OWLClausification, OWLNormalization,
        │     ObjectPropertyInclusionManager, BuiltInPropertyManager …)
        ▼
 DLOntology  ── a set of DL-clauses + ABox facts + signature + feature flags
        │       ── package: model/  (DLClause, Atom, Concept, Role, Term …)
        │
        │  Tableau construction (blocking + existential strategies chosen
        │  from Configuration)
        ▼
 Tableau  ── the hypertableau engine
        │     ── package: tableau/   (the calculus loop)
        │     ── package: blocking/  (termination)
        │     ── package: existentials/ (how ∃ is satisfied)
        │     ── package: datatypes/  (concrete-domain reasoning)
        │
        │  isSatisfiable(...) → true / false
        ▼
 Reasoning tasks  ── consistency, satisfiability, classification, realisation,
                     entailment
                     ── package: hierarchy/ (classification & realisation)
                     ── Reasoner.java, EntailmentChecker.java (public API)
```

### Stage 1 — Structural transformation & clausification (`structural/`)

OWL axioms are rich and redundant; the calculus wants a small, uniform rule
form. The `structural` package normalises axioms into negation normal form,
introduces fresh names for nested subexpressions, compiles complex role axioms
(transitivity, role chains) into **finite automata** and rewrites them away, and
finally **clausifies** everything into **DL-clauses**. The product is a
`DLOntology`. Entry point: `OWLClausification.preprocessAndClausify(...)`.
See [`structural/CONTEXT.md`](structural/CONTEXT.md).

### Stage 2 — The internal logical model (`model/`)

A **DL-clause** is a rule

```
    B₁ ∧ B₂ ∧ … ∧ Bₘ  →  H₁ ∨ H₂ ∨ … ∨ Hₙ
    (body: conjunction of atoms)   (head: disjunction of atoms)
```

where atoms are built from predicates (`AtomicConcept`, `AtLeastConcept`,
`AtomicRole`, `Equality`, `Inequality`, …) over terms (`Variable`, `Individual`,
`Constant`). The ABox is a set of ground **facts**. The `DLOntology` also carries
feature flags (`isHorn`, `hasNominals`, `hasInverseRoles`, `hasAtMostRestrictions`,
…) that let the reasoner pick cheaper strategies when the ontology is simple.
See [`model/CONTEXT.md`](model/CONTEXT.md).

### Stage 3 — The hypertableau calculus (`tableau/`)

The calculus tries to build a model: a graph of **nodes** (individuals) labelled
with concepts, connected by role edges, stored in **extension tables**. Starting
from the ABox, it repeatedly:

1. fires DL-clauses by **hyperresolution** (`HyperresolutionManager`),
2. satisfies existential (`≥ n R.C`) restrictions by creating fresh successor
   nodes (`existentials/`), bounded by **blocking** (`blocking/`) to guarantee
   termination,
3. branches on **disjunctive** clause heads and on **at-most** merges
   (nondeterminism),
4. checks **datatype** constraints (`datatypes/`),
5. detects **clashes** (contradictions) and recovers by **dependency-directed
   backjumping**.

If saturation finishes with no clash, a model exists → **satisfiable**. If every
branch clashes → **unsatisfiable**. The loop lives in
`Tableau.runCalculus()` → `Tableau.doIteration()`.
See [`tableau/CONTEXT.md`](tableau/CONTEXT.md).

### Stage 4 — Tasks on top of satisfiability (`hierarchy/`, top-level files)

`Reasoner` (implements the OWL API `OWLReasoner`) turns user requests into
satisfiability tests. **Classification** — computing the subsumption hierarchy of
all named classes/properties — is the most test-heavy task, and the `hierarchy`
package minimises the number of tableau calls using told subsumers, known/possible
subsumptions and transitive reduction. **Realisation** computes the types of each
individual. `EntailmentChecker` reduces arbitrary axiom entailment to
(un)satisfiability. See [`hierarchy/CONTEXT.md`](hierarchy/CONTEXT.md).

## Why a *hyper*tableau (the design bet)

A naïve DL tableau branches on every disjunction and builds large
"or"-expansions; it is swamped by nondeterminism. HermiT's two central bets:

1. **Push reasoning into clauses, not into the tree.** Axioms become DL-clauses
   applied by hyperresolution. A clause only fires when *all* its body atoms are
   already present, so deterministic consequences are derived deterministically —
   no speculative branching. Branching is reserved for genuinely disjunctive
   heads.
2. **Make the model as small and tree-shaped as possible.** Existentials build a
   tree of anonymous successor nodes; **blocking** halts the tree the moment a
   node repeats an existing node's label, which is what makes reasoning over
   cyclic ontologies terminate.

Together these keep the search space far smaller than classical tableaux on the
nondeterministic, GCI-heavy ontologies that occur in practice.

## Cross-cutting concerns

- **Nondeterminism & backtracking.** Each choice (a disjunct, an at-most merge,
  an individual reuse) pushes a `BranchingPoint`. Every derived fact carries a
  `DependencySet` recording exactly which branching points it depends on. On a
  clash, the engine jumps straight back to the most recent branching point in the
  clash's dependency set (**backjumping**, not chronological backtracking) and
  tries the next alternative. Optional **disjunction learning** remembers
  conflicts. (`tableau/`: `DependencySet`, `BranchingPoint`, `GroundDisjunction`.)
- **Termination = blocking.** Without blocking, cyclic existentials would build an
  infinite tree. Strategies (`blocking/`) range from cheap single/ancestor
  blocking to pairwise blocking (needed for inverse roles) to validated *core*
  blocking (needed for nominals). The right one is chosen from `Configuration`.
- **Nominals.** `{a}` (one-of) couples the tree back to named individuals.
  Handled by the **nominal introduction (NN) rule**
  (`tableau/NominalIntroductionManager`).
- **Datatypes / concrete domains.** Data values, facets and datatype clashes are
  checked by `tableau/DatatypeManager`, backed by the `datatypes/` package
  (integers, reals, floats, doubles, dateTime, strings/`rdf:PlainLiteral`, binary
  data, anyURI, XML literals, booleans).
- **Configuration** (`Configuration.java`) selects blocking type
  (`SINGLE`/`PAIR_WISE`/`OPTIMAL`), blocking strategy
  (`ANCESTOR`/`ANYWHERE`/`SIMPLE_CORE`/`COMPLEX_CORE`), existential strategy
  (`CREATION_ORDER`/`INDIVIDUAL_REUSE`/`EL`), signature caching, disjunction
  learning, monitors, timeouts and fresh-entity policy.
- **Incrementality.** A second, "additional" `DLOntology` can be layered onto a
  prepared `Tableau` for per-query ABox facts without re-clausifying the TBox
  (`ReducedABoxOnlyClausification`, `Tableau.m_additionalDLOntology`).
- **Monitoring & debugging.** `monitor/` (timing, counting, progress) and
  `debugger/` (an interactive command shell) observe a run without changing it.

## Map of the codebase (reasoning-relevant packages)

| Package | Role in reasoning | Doc |
|---|---|---|
| `structural/` | OWL axioms → DL-clauses (clausification) | [link](structural/CONTEXT.md) |
| `model/` | the DL-clause / atom / concept / role representation | [link](model/CONTEXT.md) |
| `tableau/` | the hypertableau calculus engine | [link](tableau/CONTEXT.md) |
| `blocking/` | termination via blocking | [link](blocking/CONTEXT.md) |
| `existentials/` | how `≥ n R.C` restrictions are satisfied | [link](existentials/CONTEXT.md) |
| `hierarchy/` | classification & instance realisation | [link](hierarchy/CONTEXT.md) |
| `datatypes/` | concrete-domain (datatype) reasoning | — |
| `datalog/` | datalog-style materialisation engine over the DLOntology | — |
| `graph/` | generic graph utilities (e.g. SCCs for classification) | — |
| `monitor/`, `debugger/` | observation & interactive debugging | — |
| `cli/` | command-line front end | — |

Top-level files: `Reasoner.java` (the `OWLReasoner` implementation and task
orchestrator), `Configuration.java` (reasoning knobs), `EntailmentChecker.java`
(axiom entailment → satisfiability), `Prefixes.java`,
`ProtegeReasonerFactory.java`.

## A worked trace: "is class `C` satisfiable?"

1. `Reasoner.isSatisfiable(C)` → if `C` is named and the class hierarchy is
   already computed, just check it isn't the bottom node; otherwise fall through.
2. Build the test: assert `C(a)` for a fresh individual `a`.
3. `Tableau.isSatisfiable(...)` loads the permanent ABox plus `C(a)`.
4. `runCalculus()` saturates: hyperresolution fires the clauses that define `C`,
   existentials spawn successor nodes, disjunctions branch, blocking caps the
   tree, the datatype/nominal rules run.
5. Some branch closes without a clash → a model exists → **`C` is satisfiable**.
   If every branch clashes and there's no earlier branching point to retry →
   **`C` is unsatisfiable** (equivalent to `owl:Nothing`).

Every other task is a variation on this trace.
