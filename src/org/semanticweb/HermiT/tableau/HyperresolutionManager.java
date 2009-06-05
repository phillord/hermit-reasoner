// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.tableau;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLPredicate;
import org.semanticweb.HermiT.model.NodeIDLessEqualThan;
import org.semanticweb.HermiT.model.NodeIDsAscendingOrEqual;
import org.semanticweb.HermiT.model.Term;
import org.semanticweb.HermiT.model.Variable;

/**
 * Applies the rules during the expansion of a tableau.
 */
public final class HyperresolutionManager implements Serializable {
    private static final long serialVersionUID=-4880817508962130189L;

    protected final Tableau m_tableau;
    protected final ExtensionManager m_extensionManager;
    protected final ExtensionTable.Retrieval[] m_deltaOldRetrievals;
    protected final Map<DLPredicate,CompiledDLClauseInfo> m_tupleConsumersByDeltaPredicate;

    public HyperresolutionManager(Tableau tableau) {
        m_tableau=tableau;
        InterruptFlag interruptFlag=m_tableau.m_interruptFlag;
        m_extensionManager=m_tableau.getExtensionManager();
        m_tupleConsumersByDeltaPredicate=new HashMap<DLPredicate,CompiledDLClauseInfo>();
        Map<Integer,ExtensionTable.Retrieval> retrievalsByArity=new HashMap<Integer,ExtensionTable.Retrieval>();
        Map<DLClauseBodyKey,List<DLClause>> dlClausesByBody=new HashMap<DLClauseBodyKey,List<DLClause>>();
        for (DLClause dlClause : m_tableau.m_dlOntology.getDLClauses()) {
            DLClauseBodyKey key=new DLClauseBodyKey(dlClause);
            List<DLClause> dlClauses=dlClausesByBody.get(key);
            if (dlClauses==null) {
                dlClauses=new ArrayList<DLClause>();
                dlClausesByBody.put(key,dlClauses);
            }
            dlClauses.add(dlClause);
            interruptFlag.checkInterrupt();
        }
        for (Map.Entry<DLClauseBodyKey,List<DLClause>> entry : dlClausesByBody.entrySet()) {
            DLClause bodyDLClause=entry.getKey().m_dlClause;
            BodyAtomsSwapper bodyAtomsSwapper=new BodyAtomsSwapper(bodyDLClause);
            for (int bodyAtomIndex=0;bodyAtomIndex<bodyDLClause.getBodyLength();++bodyAtomIndex)
                if (isPredicateWithExtension(bodyDLClause.getBodyAtom(bodyAtomIndex).getDLPredicate())) {
                    DLClause swappedDLClause=bodyAtomsSwapper.getSwappedDLClause(bodyAtomIndex);
                    DLPredicate deltaDLPredicate=swappedDLClause.getBodyAtom(0).getDLPredicate();
                    Integer arity=Integer.valueOf(deltaDLPredicate.getArity()+1);
                    ExtensionTable.Retrieval firstTableRetrieval=retrievalsByArity.get(arity);
                    if (firstTableRetrieval==null) {
                        ExtensionTable extensionTable=m_extensionManager.getExtensionTable(arity.intValue());
                        firstTableRetrieval=extensionTable.createRetrieval(new boolean[extensionTable.getArity()],ExtensionTable.View.DELTA_OLD);
                        retrievalsByArity.put(arity,firstTableRetrieval);
                    }
                    CompiledDLClauseInfo nextTupleConsumer=new CompiledDLClauseInfo(m_tableau,swappedDLClause,entry.getValue(),firstTableRetrieval,m_tupleConsumersByDeltaPredicate.get(deltaDLPredicate));
                    m_tupleConsumersByDeltaPredicate.put(deltaDLPredicate,nextTupleConsumer);
                    interruptFlag.checkInterrupt();
                }
        }
        m_deltaOldRetrievals=new ExtensionTable.Retrieval[retrievalsByArity.size()];
        retrievalsByArity.values().toArray(m_deltaOldRetrievals);
    }
    protected boolean isPredicateWithExtension(DLPredicate dlPredicate) {
        return !NodeIDLessEqualThan.INSTANCE.equals(dlPredicate) && !(dlPredicate instanceof NodeIDsAscendingOrEqual);
    }
    public void applyDLClauses() {
        for (int index=0;index<m_deltaOldRetrievals.length;index++)
            processDeltaOld(m_deltaOldRetrievals[index]);
    }
    protected void processDeltaOld(ExtensionTable.Retrieval retrieval) {
        retrieval.open();
        Object[] tupleBuffer=retrieval.getTupleBuffer();
        while (!retrieval.afterLast() && !m_extensionManager.containsClash()) {
            CompiledDLClauseInfo compiledDLClauseInfo=m_tupleConsumersByDeltaPredicate.get(tupleBuffer[0]);
            while (compiledDLClauseInfo!=null) {
                compiledDLClauseInfo.evaluate();
                compiledDLClauseInfo=compiledDLClauseInfo.m_next;
            }
            retrieval.next();
        }
    }

    protected static final class CompiledDLClauseInfo extends DLClauseEvaluator {
        private static final long serialVersionUID=2873489982404000730L;

        protected final CompiledDLClauseInfo m_next;

        public CompiledDLClauseInfo(Tableau tableau,DLClause bodyDLClause,List<DLClause> headDLClauses,ExtensionTable.Retrieval firstAtomRetrieval,CompiledDLClauseInfo next) {
            super(tableau,bodyDLClause,headDLClauses,firstAtomRetrieval);
            m_next=next;
        }
    }

    protected static final class BodyAtomsSwapper {
        protected final DLClause m_dlClause;
        protected final List<Atom> m_nodeIDComparisonAtoms;
        protected final boolean[] m_usedAtoms;
        protected final List<Atom> m_reorderedAtoms;
        protected final Set<Variable> m_boundVariables;

        public BodyAtomsSwapper(DLClause dlClause) {
            m_dlClause=dlClause;
            m_nodeIDComparisonAtoms=new ArrayList<Atom>(m_dlClause.getBodyLength());
            m_usedAtoms=new boolean[m_dlClause.getBodyLength()];
            m_reorderedAtoms=new ArrayList<Atom>(m_dlClause.getBodyLength());
            m_boundVariables=new HashSet<Variable>();
        }
        public DLClause getSwappedDLClause(int bodyIndex) {
            m_nodeIDComparisonAtoms.clear();
            for (int index=m_usedAtoms.length-1;index>=0;--index) {
                m_usedAtoms[index]=false;
                Atom atom=m_dlClause.getBodyAtom(index);
                if (NodeIDLessEqualThan.INSTANCE.equals(atom.getDLPredicate()))
                    m_nodeIDComparisonAtoms.add(atom);
            }
            m_reorderedAtoms.clear();
            m_boundVariables.clear();
            Atom atom=m_dlClause.getBodyAtom(bodyIndex);
            atom.getVariables(m_boundVariables);
            m_reorderedAtoms.add(atom);
            m_usedAtoms[bodyIndex]=true;
            while (m_reorderedAtoms.size()!=m_usedAtoms.length) {
                Atom bestAtom=null;
                int bestAtomIndex=-1;
                int bestAtomGoodness=-1000;
                for (int index=m_usedAtoms.length-1;index>=0;--index)
                    if (!m_usedAtoms[index]) {
                        atom=m_dlClause.getBodyAtom(index);
                        int atomGoodness=getAtomGoodness(atom);
                        if (atomGoodness>bestAtomGoodness) {
                            bestAtom=atom;
                            bestAtomGoodness=atomGoodness;
                            bestAtomIndex=index;
                        }
                    }
                m_reorderedAtoms.add(bestAtom);
                m_usedAtoms[bestAtomIndex]=true;
                bestAtom.getVariables(m_boundVariables);
                m_nodeIDComparisonAtoms.remove(bestAtom);
            }
            Atom[] bodyAtoms=new Atom[m_reorderedAtoms.size()];
            m_reorderedAtoms.toArray(bodyAtoms);
            return m_dlClause.getChangedDLClause(null,bodyAtoms);
        }
        protected int getAtomGoodness(Atom atom) {
            if (NodeIDLessEqualThan.INSTANCE.equals(atom.getDLPredicate())) {
                if (m_boundVariables.contains(atom.getArgumentVariable(0)) && m_boundVariables.contains(atom.getArgumentVariable(1)))
                    return 1000;
                else
                    return -2000;
            }
            else if (atom.getDLPredicate() instanceof NodeIDsAscendingOrEqual) {
                int numberOfUnboundVariables=0;
                for (int argumentIndex=atom.getArity()-1;argumentIndex>=0;--argumentIndex) {
                    Term argument=atom.getArgument(argumentIndex);
                    if (argument instanceof Variable) {
                        if (!m_boundVariables.contains(argument))
                            numberOfUnboundVariables++;
                    }
                }
                if (numberOfUnboundVariables>0)
                    return -5000;
                else
                    return 5000;
            }
            else {
                int numberOfBoundVariables=0;
                int numberOfUnboundVariables=0;
                for (int argumentIndex=atom.getArity()-1;argumentIndex>=0;--argumentIndex) {
                    Term argument=atom.getArgument(argumentIndex);
                    if (argument instanceof Variable) {
                        if (m_boundVariables.contains(argument))
                            numberOfBoundVariables++;
                        else
                            numberOfUnboundVariables++;
                    }
                }
                int goodness=numberOfBoundVariables*100-numberOfUnboundVariables*10;
                if (atom.getDLPredicate().getArity()==2 && numberOfUnboundVariables==1 && !m_nodeIDComparisonAtoms.isEmpty()) {
                    Variable unboundVariable=atom.getArgumentVariable(0);
                    if (m_boundVariables.contains(unboundVariable))
                        unboundVariable=atom.getArgumentVariable(1);
                    // At this point, unboundVariable must be really unbound because
                    // we have already established that numberOfUnboundVariables==1.
                    for (int compareAtomIndex=m_nodeIDComparisonAtoms.size()-1;compareAtomIndex>=0;--compareAtomIndex) {
                        Atom compareAtom=m_nodeIDComparisonAtoms.get(compareAtomIndex);
                        Variable argument0=compareAtom.getArgumentVariable(0);
                        Variable argument1=compareAtom.getArgumentVariable(1);
                        if ((m_boundVariables.contains(argument0) || unboundVariable.equals(argument0)) && (m_boundVariables.contains(argument1) || unboundVariable.equals(argument1))) {
                            goodness+=5;
                            break;
                        }
                    }
                }
                return goodness;
            }
        }
    }

    protected static final class DLClauseBodyKey {
        protected final DLClause m_dlClause;
        protected final int m_hashCode;

        public DLClauseBodyKey(DLClause dlClause) {
            m_dlClause=dlClause;
            int hashCode=0;
            for (int atomIndex=0;atomIndex<m_dlClause.getBodyLength();atomIndex++)
                hashCode+=m_dlClause.getBodyAtom(atomIndex).hashCode();
            m_hashCode=hashCode;
        }
        public boolean equals(Object that) {
            if (this==that)
                return true;
            DLClause thatDLClause=((DLClauseBodyKey)that).m_dlClause;
            if (m_dlClause.getBodyLength()!=thatDLClause.getBodyLength())
                return false;
            for (int atomIndex=0;atomIndex<m_dlClause.getBodyLength();atomIndex++)
                if (!m_dlClause.getBodyAtom(atomIndex).equals(thatDLClause.getBodyAtom(atomIndex)))
                    return false;
            return true;
        }
        public int hashCode() {
            return m_hashCode;
        }
    }
}
