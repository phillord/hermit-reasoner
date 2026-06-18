# `model/` — HermiT's internal logical representation

This package is the **vocabulary the calculus reasons in**. It is *not* the OWL
API object model — it is the small, canonical, interned representation that the
`structural` package produces and the `tableau` engine consumes. If `tableau/` is
the engine, this package is the fuel specification.

> Read this together with the top-level [`../CONTEXT.md`](../CONTEXT.md) (pipeline
> overview) and [`../structural/CONTEXT.md`](../structural/CONTEXT.md) (how these
> objects are built).

## The shape of the representation

A clausified ontology is a `DLOntology`: a set of **DL-clauses** (the TBox/RBox
as rules), a set of ground **facts** (the ABox), the **signature** (every atomic
concept, role and individual that occurs), and **feature flags**.

```
 DLOntology
   ├─ Set<DLClause>  m_dlClauses        the rules
   ├─ Set<Atom>      m_positiveFacts    ABox: things that hold
   ├─ Set<Atom>      m_negativeFacts    ABox: things that must not hold
   ├─ signature      all atomic concepts / object roles / data roles / individuals
   └─ flags          isHorn, hasNominals, hasInverseRoles,
                     hasAtMostRestrictions, hasDatatypes, …
```

The flags matter: they let `Reasoner`/`Tableau` pick cheaper strategies. `isHorn`
(every clause head has ≤ 1 atom ⇒ no disjunctive branching) enables the
deterministic classification path; `hasNominals` forces validated core blocking;
`hasInverseRoles` forces pairwise blocking; and so on.

## The DL-clause: the single most important type

`DLClause` (`DLClause.java`) is the rule form the whole calculus runs on:

```
    body₁ ∧ body₂ ∧ … ∧ bodyₘ   →   head₁ ∨ head₂ ∨ … ∨ headₙ
```

stored as two arrays, `m_headAtoms` (a **disjunction**) and `m_bodyAtoms` (a
**conjunction**). Reading direction: *if every body atom holds (under some
variable binding), then at least one head atom must hold.*

- `n = 0` (empty head) ⇒ the body is unsatisfiable — firing it produces a clash.
- `n = 1` ⇒ a deterministic (Horn) consequence — no branching.
- `n > 1` ⇒ a disjunction — firing it creates a `GroundDisjunction` the tableau
  must branch on.

Typical clause families (recognised by `isAtomicConceptInclusion()`,
`isRoleInclusion()`, `isFunctionalityAxiom()`, … on `DLClause`):

| OWL axiom | DL-clause |
|---|---|
| `SubClassOf(A,B)` | `A(X) → B(X)` |
| `SubClassOf(A, B ⊔ C)` | `A(X) → B(X) ∨ C(X)` |
| `SubObjectPropertyOf(R,S)` | `R(X,Y) → S(X,Y)` |
| `SubClassOf(A, ∃R.C)` | `A(X) → (≥1 R.C)(X)` |
| `DisjointClasses(A,B)` | `A(X) ∧ B(X) →` (empty head) |
| `FunctionalObjectProperty(R)` | `R(X,Y₁) ∧ R(X,Y₂) → Y₁ ≈ Y₂` |

`getSafeVersion()` enforces **DL-safety** of clauses: every head variable must
also occur in the body (unsafe head variables get a guarding `Thing`/`Top` body
atom). This is what keeps rule application grounded and the calculus terminating.

## Atoms and predicates

An `Atom` (`Atom.java`) is `predicate(term, term, …)` — a `DLPredicate` applied to
a `Term[]` whose length equals the predicate's arity. The `DLPredicate` interface
(arity + `toString`) has these implementations:

**Unary (concept-like) predicates**
- `AtomicConcept` — a named class (`Person`); `THING` and `NOTHING` are the
  distinguished top/bottom.
- `AtomicNegationConcept` — `¬A`.
- `AtLeastConcept` — `≥ n R.C` over an object role (the existential workhorse).
- `AtLeastDataRange` — `≥ n R.DR` over a data role.
- `ExistsDescriptionGraph` — "satisfies graph `G` at vertex `v`".
- `LiteralDataRange` and friends (`DatatypeRestriction`, `InternalDatatype`,
  `ConstantEnumeration`, `AtomicNegationDataRange`) — datatype membership tests.

**Binary (role-like) predicates**
- `AtomicRole` — a named object or data property; defines the built-in
  `TOP_/BOTTOM_OBJECT_ROLE` and `TOP_/BOTTOM_DATA_ROLE`.
- `Equality` (`≈`) / `Inequality` (`≉`) — the built-in (in)equality predicates;
  `owl:sameAs`/`owl:differentFrom` clausify to these. A merge or an `x ≉ x` is how
  equality reasoning and at-most clashes surface.
- `NodeIDLessEqualThan` — node-ordering predicate used when translating at-most
  restrictions.

**Special-arity predicates**
- `AnnotatedEquality` (arity 3) — an equality "annotated" with the at-most
  context (`m_cardinality`, `m_onRole`, `m_toConcept`) that produced it; this is
  what drives the merge step for `≤ n R.C` restrictions.
- `NodeIDsAscendingOrEqual` (variable arity) and `DescriptionGraph` (arity = number
  of vertices).

`InverseRole` and `NegatedAtomicRole` express `R⁻` and `¬R`; `InverseRole`
delegates assertions by flipping argument order (`inv(R)(Y,X) ≡ R(X,Y)`).

## Concepts vs. predicates — two overlapping hierarchies

There is a `Concept` hierarchy (the *syntax of class expressions* surviving
normalisation) and a `DLPredicate` hierarchy (*what can label a node or head an
atom*). They overlap because many concepts are also predicates.

```
 Concept (abstract; isAlwaysTrue / isAlwaysFalse)
   ├─ LiteralConcept                 (has getNegation())
   │    ├─ AtomicConcept             (also a DLPredicate)
   │    ├─ AtomicNegationConcept
   │    └─ LiteralDataRange …        (data ranges as concepts)
   └─ ExistentialConcept
        ├─ AtLeast (abstract: number + onRole)
        │    ├─ AtLeastConcept
        │    └─ AtLeastDataRange
        └─ ExistsDescriptionGraph
```

`isAlwaysTrue()`/`isAlwaysFalse()` (true for `Thing`/`rdfs:Literal` and
`Nothing`/empty enumerations respectively) let clausification and the tableau
short-circuit trivial cases.

## Terms

`Term` (abstract) has three concrete forms:
- `Variable` — `X`, `Y`, … in clause bodies/heads (interned, compared by `==`).
- `Individual` — a named ABox individual or an internal anonymous one
  (`internal:anonymous#…`).
- `Constant` — a data literal: lexical form + datatype URI + the parsed Java
  value (parsed via the `datatypes` registry).

## Description graphs

`DescriptionGraph` represents complex structural patterns (labelled vertices +
`Edge`s) that cannot be captured by ordinary clauses — used for guarded/graph-shaped
axioms. `produceStartDLClauses()` emits the clauses that instantiate a graph when a
start concept appears; the tableau then enforces graph membership via
`ExistsDescriptionGraph` atoms and `DescriptionGraphManager`.

## Why everything is interned

`InterningManager` is a weak-reference object pool used by *every* `create(...)`
factory in this package (clauses, atoms, concepts, roles, terms). Two
structurally-equal logical objects are guaranteed to be the **same Java object**,
so the hot path can use reference equality (`==`) and identity hashing instead of
structural `.equals()`. The tableau allocates and compares these objects
relentlessly, so this canonicalisation is a real performance lever, not a tidiness
nicety.

## How this package connects

- **Built by** `structural/` — `OWLClausification` constructs the `DLOntology`,
  its clauses, atoms and facts.
- **Consumed by** `tableau/` — `HyperresolutionManager` compiles `DLClause`s into
  executable form; `ExtensionManager` stores ground atoms; `ClashManager` reads
  predicates to spot contradictions.
- **Queried by** `hierarchy/` — told-subsumer extraction reads clause structure
  directly to seed classification.

Invariant to respect when changing this package: these objects are **immutable and
interned**. Never mutate a `DLClause`/`Atom`/`Concept` after creation; create a new
interned instance instead.
