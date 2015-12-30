package org.semanticweb.HermiT.datalog;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.existentials.ExistentialExpansionStrategy;
import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.Concept;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLOntology;
import org.semanticweb.HermiT.model.Term;
import org.semanticweb.HermiT.model.Variable;
import org.semanticweb.HermiT.tableau.DLClauseEvaluator;
import org.semanticweb.HermiT.tableau.ExtensionManager;
import org.semanticweb.HermiT.tableau.InterruptFlag;
import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.HermiT.tableau.Tableau;

/**
 * Datalog engine.
 */
public final class DatalogEngine {
    protected final InterruptFlag m_interruptFlag;
    protected final DLOntology m_dlOntology;
    protected final Map<Term,Node> m_termsToNodes;
    protected final Map<Node,Term> m_nodesToTerms;
    protected final Map<Term,Set<Term>> m_termsToEquivalenceClasses;
    protected final Map<Term,Term> m_termsToRepresentatives;
    protected ExtensionManager m_extensionManager;
    
    /**
     * @param dlOntology dlOntology
     */
    public DatalogEngine(DLOntology dlOntology) {
        for (DLClause dlClause : dlOntology.getDLClauses())
            if (dlClause.getHeadLength()>1)
                throw new IllegalArgumentException("The supplied DL ontology contains rules with disjunctive heads.");
        m_interruptFlag=new InterruptFlag(0);
        m_dlOntology=dlOntology;
        m_termsToNodes=new HashMap<>();
        m_nodesToTerms=new HashMap<>();
        m_termsToEquivalenceClasses=new HashMap<>();
        m_termsToRepresentatives=new HashMap<>();
    }
    /**
     * @return true if no clashes
     */
    public boolean materialize() {
        if (m_extensionManager==null) {
            m_termsToNodes.clear();
            m_nodesToTerms.clear();
            m_termsToEquivalenceClasses.clear();
            m_termsToRepresentatives.clear();
            Tableau tableau=new Tableau(m_interruptFlag,null,NullExistentialExpansionStrategy.INSTANCE,false,m_dlOntology,null,new HashMap<String,Object>());
            Set<Atom> noAtoms=Collections.emptySet();
            tableau.isSatisfiable(true,false,noAtoms,noAtoms,noAtoms,noAtoms,m_termsToNodes,null,null);
            for (Map.Entry<Term,Node> entry : m_termsToNodes.entrySet())
                m_nodesToTerms.put(entry.getValue(),entry.getKey());
            m_extensionManager=tableau.getExtensionManager();
            Node node=tableau.getFirstTableauNode();
            while (node!=null) {
                Term term=m_nodesToTerms.get(node);
                Term canonicalTerm=m_nodesToTerms.get(node.getCanonicalNode());
                Set<Term> equivalenceClass=m_termsToEquivalenceClasses.get(canonicalTerm);
                if (equivalenceClass==null) {
                    equivalenceClass=new HashSet<>();
                    m_termsToEquivalenceClasses.put(canonicalTerm,equivalenceClass);
                }
                if (!term.equals(canonicalTerm))
                    m_termsToEquivalenceClasses.put(term,equivalenceClass);
                equivalenceClass.add(term);
                m_termsToRepresentatives.put(term,canonicalTerm);
                node=node.getNextTableauNode();
            }
        }
        return !m_extensionManager.containsClash();
    }
    /**
     * @return ontology
     */
    public DLOntology getDLOntology() {
        return m_dlOntology;
    }
    /**
     * @param term term
     * @return equivalence classes
     */
    public Set<Term> getEquivalenceClass(Term term) {
        return m_termsToEquivalenceClasses.get(term);
    }
    /**
     * @param term term
     * @return representative
     */
    public Term getRepresentative(Term term) {
        return m_termsToRepresentatives.get(term);
    }
    
    protected static class NullExistentialExpansionStrategy implements ExistentialExpansionStrategy {
        public static final ExistentialExpansionStrategy INSTANCE=new NullExistentialExpansionStrategy();

        @Override
        public void initialize(Tableau tableau) {
        }
        @Override
        public void additionalDLOntologySet(DLOntology additionalDLOntology) {
        }
        @Override
        public void additionalDLOntologyCleared() {
        }
        @Override
        public void clear() {
        }
        @Override
        public boolean expandExistentials(boolean finalChance) {
            return false;
        }
        @Override
        public void assertionAdded(Concept concept,Node node,boolean isCore) {
        }
        @Override
        public void assertionCoreSet(Concept concept,Node node) {
        }
        @Override
        public void assertionRemoved(Concept concept,Node node,boolean isCore) {
        }
        @Override
        public void assertionAdded(AtomicRole atomicRole,Node nodeFrom,Node nodeTo,boolean isCore) {
        }
        @Override
        public void assertionCoreSet(AtomicRole atomicRole,Node nodeFrom,Node nodeTo) {
        }
        @Override
        public void assertionRemoved(AtomicRole atomicRole,Node nodeFrom,Node nodeTo,boolean isCore) {
        }
        @Override
        public void nodesMerged(Node mergeFrom,Node mergeInto) {
        }
        @Override
        public void nodesUnmerged(Node mergeFrom,Node mergeInto) {
        }
        @Override
        public void nodeStatusChanged(Node node) {
        }
        @Override
        public void nodeInitialized(Node node) {
        }
        @Override
        public void nodeDestroyed(Node node) {
        }
        @Override
        public void branchingPointPushed() {
        }
        @Override
        public void backtrack() {
        }
        @Override
        public void modelFound() {
        }
        @Override
        public boolean isDeterministic() {
            return true;
        }
        @Override
        public boolean isExact() {
            return true;
        }
        @Override
        public void dlClauseBodyCompiled(List<DLClauseEvaluator.Worker> workers,DLClause dlClause,List<Variable> variables,Object[] valuesBuffer,boolean[] coreVariables) {
        }
    }
}
