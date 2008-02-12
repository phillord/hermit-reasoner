package org.semanticweb.HermiT.hierarchy;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.io.Serializable;

import org.semanticweb.HermiT.model.*;
import org.semanticweb.HermiT.tableau.*;

/**
 * A subsumption checker that uses the tableau to perform the check.
 */
public class TableauSubsumptionChecker implements SubsumptionHierarchy.SubsumptionChecker,Serializable {
    private static final long serialVersionUID=-2023801882010561315L;

    protected final Tableau m_tableau;
    protected final Map<AtomicConcept,AtomicConceptInfo> m_atomicConceptInfos;

    public TableauSubsumptionChecker(Tableau tableau) {
        m_tableau=tableau;
        m_atomicConceptInfos=new HashMap<AtomicConcept,AtomicConceptInfo>();
    }
    public Set<AtomicConcept> getAllAtomicConcepts() {
        return m_tableau.getDLOntology().getAllAtomicConcepts();
    }
    public boolean isSatisfiable(AtomicConcept concept) {
        AtomicConceptInfo atomicConceptInfo=getAtomicConceptInfo(concept);
        if (atomicConceptInfo.m_isSatisfiable==null) {
            boolean isSatisfiable=m_tableau.isSatisfiable(concept);
            atomicConceptInfo.m_isSatisfiable=(isSatisfiable ? Boolean.TRUE : Boolean.FALSE);
            if (isSatisfiable) {
                updateKnownSubsumers(concept);
                updatePossibleSubsumers();
            }
        }
        return atomicConceptInfo.m_isSatisfiable;
    }
    public boolean isSubsumedBy(AtomicConcept subconcept,AtomicConcept superconcept) {
        if (AtomicConcept.THING.equals(superconcept) || AtomicConcept.NOTHING.equals(subconcept))
            return true;
        else if (AtomicConcept.NOTHING.equals(superconcept))
            return !isSatisfiable(subconcept);
        AtomicConceptInfo subconceptInfo=getAtomicConceptInfo(subconcept);
        if (subconceptInfo.isKnownSubsumer(superconcept))
            return true;
        else if (subconceptInfo.isKnownNotSubsumer(superconcept))
            return false;
        else if (!m_tableau.isDeterministic()) {
            boolean isSubsumedBy=m_tableau.isSubsumedBy(subconcept,superconcept);
            if (!isSubsumedBy) {
                subconceptInfo.m_isSatisfiable=Boolean.TRUE;
                updateKnownSubsumers(subconcept);
                updatePossibleSubsumers();
            }
            else {
                subconceptInfo.addKnownSubsumer(superconcept);
            }
            return isSubsumedBy;
        }
        else {
            isSatisfiable(subconcept);
            assert subconceptInfo.m_allSubsumersKnown;
            return subconceptInfo.isKnownSubsumer(superconcept);
        }
    }
    protected void updateKnownSubsumers(AtomicConcept subconcept) {
        Node checkedNode=m_tableau.getCheckedNode().getCanonicalNode();
        AtomicConceptInfo subconceptInfo=getAtomicConceptInfo(subconcept);
        ExtensionTable.Retrieval retrieval=m_tableau.getExtensionManager().getBinaryExtensionTable().createRetrieval(new boolean[] { false,true },ExtensionTable.View.TOTAL);
        retrieval.getBindingsBuffer()[1]=checkedNode;
        retrieval.open();
        while (!retrieval.afterLast()) {
            Object concept=retrieval.getTupleBuffer()[0];
            if (concept instanceof AtomicConcept && retrieval.getDependencySet().isEmpty())
                subconceptInfo.addKnownSubsumer((AtomicConcept)concept);
            retrieval.next();
        }
        if (m_tableau.isCurrentModelDeterministic())
            subconceptInfo.setAllSubsumersKnown();
    }
    protected void updatePossibleSubsumers() {
        ExtensionTable.Retrieval retrieval=m_tableau.getExtensionManager().getBinaryExtensionTable().createRetrieval(new boolean[] { false,false },ExtensionTable.View.TOTAL);
        retrieval.open();
        Object[] tupleBuffer=retrieval.getTupleBuffer();
        while (!retrieval.afterLast()) {
            Object conceptObject=tupleBuffer[0];
            if (conceptObject instanceof AtomicConcept) {
                AtomicConcept atomicConcept=(AtomicConcept)conceptObject;
                Node node=(Node)tupleBuffer[1];
                if (node.isActive() && !node.isBlocked())
                    getAtomicConceptInfo(atomicConcept).updatePossibleSubsumers(m_tableau,node);
            }
            retrieval.next();
        }
    }
    protected AtomicConceptInfo getAtomicConceptInfo(AtomicConcept atomicConcept) {
        AtomicConceptInfo result=m_atomicConceptInfos.get(atomicConcept);
        if (result==null) {
            result=new AtomicConceptInfo();
            m_atomicConceptInfos.put(atomicConcept,result);
        }
        return result;
    }
    
    protected static final class AtomicConceptInfo implements Serializable {
        private static final long serialVersionUID=2370809671193223969L;

        protected Boolean m_isSatisfiable;
        protected Set<AtomicConcept> m_knownSubsumers;
        protected Set<AtomicConcept> m_possibleSubsumers;
        protected boolean m_allSubsumersKnown;
        
        public boolean isKnownSubsumer(AtomicConcept potentialSubsumer) {
            return m_knownSubsumers!=null && m_knownSubsumers.contains(potentialSubsumer);
        }
        public void addKnownSubsumer(AtomicConcept atomicConcept) {
            if (m_knownSubsumers==null)
                m_knownSubsumers=new HashSet<AtomicConcept>();
            m_knownSubsumers.add(atomicConcept);
        }
        public void setAllSubsumersKnown() {
            m_allSubsumersKnown=true;
            m_possibleSubsumers=m_knownSubsumers;
        }
        public boolean isKnownNotSubsumer(AtomicConcept potentialSubsumer) {
            return (!isKnownSubsumer(potentialSubsumer) && m_allSubsumersKnown) || (m_possibleSubsumers!=null && !m_possibleSubsumers.contains(potentialSubsumer));
        }
        public void updatePossibleSubsumers(Tableau tableau,Node node) {
            if (!m_allSubsumersKnown) {
                if (m_possibleSubsumers==null) {
                    m_possibleSubsumers=new HashSet<AtomicConcept>();
                    ExtensionTable.Retrieval retrieval=tableau.getExtensionManager().getBinaryExtensionTable().createRetrieval(new boolean[] { false,true },ExtensionTable.View.TOTAL);
                    retrieval.getBindingsBuffer()[1]=node;
                    retrieval.open();
                    while (!retrieval.afterLast()) {
                        Object concept=retrieval.getTupleBuffer()[0];
                        if (concept instanceof AtomicConcept)
                            m_possibleSubsumers.add((AtomicConcept)concept);
                        retrieval.next();
                    }
                }
                else {
                    Iterator<AtomicConcept> iterator=m_possibleSubsumers.iterator();
                    while (iterator.hasNext()) {
                        AtomicConcept atomicConcept=iterator.next();
                        if (!tableau.getExtensionManager().containsConceptAssertion(atomicConcept,node))
                            iterator.remove();
                    }
                }
            }
        }
    }
}
