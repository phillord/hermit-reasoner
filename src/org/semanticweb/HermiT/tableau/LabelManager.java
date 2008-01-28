package org.semanticweb.HermiT.tableau;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

import org.semanticweb.HermiT.model.*;

public final class LabelManager implements Serializable {
    private static final long serialVersionUID=2628318450352626514L;

    protected final Tableau m_tableau;
    protected final SetFactory<Concept> m_conceptSetFactory;
    protected final SetFactory<AtomicAbstractRole> m_atomicAbstractRoleSetFactory;
    protected final ExtensionTable.Retrieval m_binaryTableSearch1Bound;
    protected final ExtensionTable.Retrieval m_ternaryTableSearch12Bound;
    protected final List<Concept> m_conceptBuffer;
    protected final List<AtomicAbstractRole> m_atomicAbstractRoleBuffer;
    
    public LabelManager(Tableau tableau) {
        m_tableau=tableau;
        m_conceptSetFactory=new SetFactory<Concept>();
        m_atomicAbstractRoleSetFactory=new SetFactory<AtomicAbstractRole>();
        m_binaryTableSearch1Bound=m_tableau.getExtensionManager().getBinaryExtensionTable().createRetrieval(new boolean[] { false,true },ExtensionTable.View.TOTAL);
        m_ternaryTableSearch12Bound=m_tableau.getExtensionManager().getTernaryExtensionTable().createRetrieval(new boolean[] { false,true,true },ExtensionTable.View.TOTAL);
        m_conceptBuffer=new ArrayList<Concept>();
        m_atomicAbstractRoleBuffer=new ArrayList<AtomicAbstractRole>(); 
    }
    public SetFactory<Concept> getConceptSetFactory() {
        return m_conceptSetFactory;
    }
    public SetFactory<AtomicAbstractRole> getAtomicAbstractRoleSetFactory() {
        return m_atomicAbstractRoleSetFactory;
    }
    public Set<Concept> getPositiveLabel(Node node) {
        m_conceptBuffer.clear();
        m_binaryTableSearch1Bound.getBindingsBuffer()[1]=node;
        m_binaryTableSearch1Bound.open();
        Object[] tupleBuffer=m_binaryTableSearch1Bound.getTupleBuffer();
        while (!m_binaryTableSearch1Bound.afterLast()) {
            Object concept=tupleBuffer[0];
            if (!(concept instanceof AtomicNegationConcept))
                m_conceptBuffer.add((Concept)concept);
            m_binaryTableSearch1Bound.next();
        }
        Set<Concept> result=m_conceptSetFactory.getSet(m_conceptBuffer);
        m_conceptBuffer.clear();
        return result;
    }
    public Set<AtomicAbstractRole> getEdgeLabel(Node nodeFrom,Node nodeTo) {
        m_atomicAbstractRoleBuffer.clear();
        m_ternaryTableSearch12Bound.getBindingsBuffer()[1]=nodeFrom;
        m_ternaryTableSearch12Bound.getBindingsBuffer()[2]=nodeTo;
        m_ternaryTableSearch12Bound.open();
        Object[] tupleBuffer=m_ternaryTableSearch12Bound.getTupleBuffer();
        while (!m_ternaryTableSearch12Bound.afterLast()) {
            Object atomicAbstractRole=tupleBuffer[0];
            if (atomicAbstractRole instanceof AtomicAbstractRole)
                m_atomicAbstractRoleBuffer.add((AtomicAbstractRole)atomicAbstractRole);
            m_ternaryTableSearch12Bound.next();
        }
        Set<AtomicAbstractRole> result=m_atomicAbstractRoleSetFactory.getSet(m_atomicAbstractRoleBuffer);
        m_atomicAbstractRoleBuffer.clear();
        return result;
    }
    public void addConceptSetReference(Set<Concept> set) {
        m_conceptSetFactory.addReference(set);
    }
    public void removeConceptSetReference(Set<Concept> set) {
        m_conceptSetFactory.removeReference(set);
    }
    public void addAtomicAbstractRoleSetReference(Set<AtomicAbstractRole> set) {
        m_atomicAbstractRoleSetFactory.addReference(set);
    }
    public void removeAtomicAbstractRoleSetReference(Set<AtomicAbstractRole> set) {
        m_atomicAbstractRoleSetFactory.removeReference(set);
    }
}
