/* Copyright 2008, 2009, 2010 by the Oxford University Computing Laboratory

   This file is part of HermiT.

   HermiT is free software: you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   HermiT is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public License
   along with HermiT.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.semanticweb.HermiT.tableau;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicRole;
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

    protected final ExtensionManager m_extensionManager;
    protected final ExtensionTable.Retrieval[] m_deltaOldRetrievals;
    protected final ExtensionTable.Retrieval m_binaryTableRetrieval;
    protected final Map<DLPredicate,CompiledDLClauseInfo> m_tupleConsumersByDeltaPredicate;
    protected final Map<AtomicRole,CompiledDLClauseInfo> m_atomicRoleTupleConsumersUnguarded;
    protected final HashMap<AtomicRole,Map<AtomicConcept,CompiledDLClauseInfo>> m_atomicRoleTupleConsumersByGuardConcept1;
    protected final HashMap<AtomicRole,Map<AtomicConcept,CompiledDLClauseInfo>> m_atomicRoleTupleConsumersByGuardConcept2;
    protected final Object[][] m_buffersToClear;
    protected final UnionDependencySet[] m_unionDependencySetsToClear;
    protected final Object[] m_valuesBuffer;
    protected final int m_maxNumberOfVariables;

    public HyperresolutionManager(Tableau tableau,Set<DLClause> dlClauses) {
        InterruptFlag interruptFlag=tableau.m_interruptFlag;
        m_extensionManager=tableau.m_extensionManager;
        m_tupleConsumersByDeltaPredicate=new HashMap<DLPredicate,CompiledDLClauseInfo>();
        m_atomicRoleTupleConsumersUnguarded=new HashMap<AtomicRole,CompiledDLClauseInfo>();
        m_atomicRoleTupleConsumersByGuardConcept1=new HashMap<AtomicRole,Map<AtomicConcept,CompiledDLClauseInfo>>();
        m_atomicRoleTupleConsumersByGuardConcept2=new HashMap<AtomicRole,Map<AtomicConcept,CompiledDLClauseInfo>>();
        // Index DL clauses by body
        Map<DLClauseBodyKey,List<DLClause>> dlClausesByBody=new HashMap<DLClauseBodyKey,List<DLClause>>();
        for (DLClause dlClause : dlClauses) {
            DLClauseBodyKey key=new DLClauseBodyKey(dlClause);
            List<DLClause> dlClausesForKey=dlClausesByBody.get(key);
            if (dlClausesForKey==null) {
                dlClausesForKey=new ArrayList<DLClause>();
                dlClausesByBody.put(key,dlClausesForKey);
            }
            dlClausesForKey.add(dlClause);
            interruptFlag.checkInterrupt();
        }
        // Compile the DL clauses
        Map<Integer,ExtensionTable.Retrieval> retrievalsByArity=new HashMap<Integer,ExtensionTable.Retrieval>();
        DLClauseEvaluator.BufferSupply bufferSupply=new DLClauseEvaluator.BufferSupply();
        Map<Term,Node> noTermsToNodes=Collections.emptyMap();
        DLClauseEvaluator.ValuesBufferManager valuesBufferManager=new DLClauseEvaluator.ValuesBufferManager(dlClauses,noTermsToNodes);
        DLClauseEvaluator.GroundDisjunctionHeaderManager groundDisjunctionHeaderManager=new DLClauseEvaluator.GroundDisjunctionHeaderManager();
        Map<Integer,UnionDependencySet> unionDependencySetsBySize=new HashMap<Integer,UnionDependencySet>();
        ArrayList<Atom> guardingAtomicConceptAtoms1=new ArrayList<Atom>();
        ArrayList<Atom> guardingAtomicConceptAtoms2=new ArrayList<Atom>();
        for (Map.Entry<DLClauseBodyKey,List<DLClause>> entry : dlClausesByBody.entrySet()) {
            DLClause bodyDLClause=entry.getKey().m_dlClause;
            BodyAtomsSwapper bodyAtomsSwapper=new BodyAtomsSwapper(bodyDLClause);
            for (int bodyAtomIndex=0;bodyAtomIndex<bodyDLClause.getBodyLength();++bodyAtomIndex)
                if (isPredicateWithExtension(bodyDLClause.getBodyAtom(bodyAtomIndex).getDLPredicate())) {
                    DLClause swappedDLClause=bodyAtomsSwapper.getSwappedDLClause(bodyAtomIndex);
                    Atom deltaAtom=swappedDLClause.getBodyAtom(0);
                    DLPredicate deltaDLPredicate=deltaAtom.getDLPredicate();
                    Integer arity=Integer.valueOf(deltaDLPredicate.getArity()+1);
                    ExtensionTable.Retrieval firstTableRetrieval=retrievalsByArity.get(arity);
                    if (firstTableRetrieval==null) {
                        ExtensionTable extensionTable=m_extensionManager.getExtensionTable(arity.intValue());
                        firstTableRetrieval=extensionTable.createRetrieval(new boolean[extensionTable.getArity()],ExtensionTable.View.DELTA_OLD);
                        retrievalsByArity.put(arity,firstTableRetrieval);
                    }
                    DLClauseEvaluator evaluator=new DLClauseEvaluator(tableau,swappedDLClause,entry.getValue(),firstTableRetrieval,bufferSupply,valuesBufferManager,groundDisjunctionHeaderManager,unionDependencySetsBySize);
                    CompiledDLClauseInfo normalTupleConsumer=new CompiledDLClauseInfo(evaluator,m_tupleConsumersByDeltaPredicate.get(deltaDLPredicate));
                    m_tupleConsumersByDeltaPredicate.put(deltaDLPredicate,normalTupleConsumer);
                    if (deltaDLPredicate instanceof AtomicRole && deltaAtom.getArgument(0) instanceof Variable && deltaAtom.getArgument(1) instanceof Variable) {
                        AtomicRole deltaAtomicRole=(AtomicRole)deltaDLPredicate;
                        getAtomicRoleClauseGuards(swappedDLClause,guardingAtomicConceptAtoms1,guardingAtomicConceptAtoms2);
                        if (!guardingAtomicConceptAtoms1.isEmpty()) {
                            Map<AtomicConcept,CompiledDLClauseInfo> compiledDLClauseInfos=m_atomicRoleTupleConsumersByGuardConcept1.get(deltaAtomicRole);
                            if (compiledDLClauseInfos==null) {
                                compiledDLClauseInfos=new HashMap<AtomicConcept,CompiledDLClauseInfo>();
                                m_atomicRoleTupleConsumersByGuardConcept1.put(deltaAtomicRole,compiledDLClauseInfos);
                            }
                            for (Atom guardingAtom : guardingAtomicConceptAtoms1) {
                                AtomicConcept atomicConcept=(AtomicConcept)guardingAtom.getDLPredicate();
                                CompiledDLClauseInfo optimizedTupleConsumer=new CompiledDLClauseInfo(evaluator,compiledDLClauseInfos.get(atomicConcept));
                                compiledDLClauseInfos.put(atomicConcept,optimizedTupleConsumer);
                            }
                        }
                        if (!guardingAtomicConceptAtoms2.isEmpty()) {
                            Map<AtomicConcept,CompiledDLClauseInfo> compiledDLClauseInfos=m_atomicRoleTupleConsumersByGuardConcept2.get(deltaAtomicRole);
                            if (compiledDLClauseInfos==null) {
                                compiledDLClauseInfos=new HashMap<AtomicConcept,CompiledDLClauseInfo>();
                                m_atomicRoleTupleConsumersByGuardConcept2.put(deltaAtomicRole,compiledDLClauseInfos);
                            }
                            for (Atom guardingAtom : guardingAtomicConceptAtoms2) {
                                AtomicConcept atomicConcept=(AtomicConcept)guardingAtom.getDLPredicate();
                                CompiledDLClauseInfo optimizedTupleConsumer=new CompiledDLClauseInfo(evaluator,compiledDLClauseInfos.get(atomicConcept));
                                compiledDLClauseInfos.put(atomicConcept,optimizedTupleConsumer);
                            }
                        }
                        if (guardingAtomicConceptAtoms1.isEmpty() && guardingAtomicConceptAtoms2.isEmpty()) {
                            CompiledDLClauseInfo unguardedTupleConsumer=new CompiledDLClauseInfo(evaluator,m_atomicRoleTupleConsumersUnguarded.get(deltaAtomicRole));
                            m_atomicRoleTupleConsumersUnguarded.put(deltaAtomicRole,unguardedTupleConsumer);
                        }
                    }
                    bufferSupply.reuseBuffers();
                    interruptFlag.checkInterrupt();
                }
        }
        m_deltaOldRetrievals=new ExtensionTable.Retrieval[retrievalsByArity.size()];
        retrievalsByArity.values().toArray(m_deltaOldRetrievals);
        m_binaryTableRetrieval=m_extensionManager.getExtensionTable(2).createRetrieval(new boolean[] { false,true },ExtensionTable.View.EXTENSION_THIS);
        m_buffersToClear=bufferSupply.getAllBuffers();
        m_unionDependencySetsToClear=new UnionDependencySet[unionDependencySetsBySize.size()];
        unionDependencySetsBySize.values().toArray(m_unionDependencySetsToClear);
        m_valuesBuffer=valuesBufferManager.m_valuesBuffer;
        m_maxNumberOfVariables=valuesBufferManager.m_maxNumberOfVariables;
    }
    protected void getAtomicRoleClauseGuards(DLClause swappedDLClause,List<Atom> guardingAtomicConceptAtoms1,List<Atom> guardingAtomicConceptAtoms2) {
        guardingAtomicConceptAtoms1.clear();
        guardingAtomicConceptAtoms2.clear();
        Atom deltaOldAtom=swappedDLClause.getBodyAtom(0);
        Variable X=deltaOldAtom.getArgumentVariable(0);
        Variable Y=deltaOldAtom.getArgumentVariable(1);
        for (int bodyIndex=1;bodyIndex<swappedDLClause.getBodyLength();bodyIndex++) {
            Atom atom=swappedDLClause.getBodyAtom(bodyIndex);
            if (atom.getDLPredicate() instanceof AtomicConcept) {
                Variable variable=atom.getArgumentVariable(0);
                if (variable!=null) {
                    if (X.equals(variable))
                        guardingAtomicConceptAtoms1.add(atom);
                    if (Y.equals(variable))
                        guardingAtomicConceptAtoms2.add(atom);
                }
            }
            bodyIndex++;
        }
    }
    protected boolean isPredicateWithExtension(DLPredicate dlPredicate) {
        return !NodeIDLessEqualThan.INSTANCE.equals(dlPredicate) && !(dlPredicate instanceof NodeIDsAscendingOrEqual);
    }
    public void clear() {
        for (int retrievalIndex=m_deltaOldRetrievals.length-1;retrievalIndex>=0;--retrievalIndex)
            m_deltaOldRetrievals[retrievalIndex].clear();
        m_binaryTableRetrieval.clear();
        for (int bufferIndex=m_buffersToClear.length-1;bufferIndex>=0;--bufferIndex) {
            Object[] buffer=m_buffersToClear[bufferIndex];
            for (int index=buffer.length-1;index>=0;--index)
                buffer[index]=null;
        }
        for (int unionDependencySetIndex=m_unionDependencySetsToClear.length-1;unionDependencySetIndex>=0;--unionDependencySetIndex) {
            DependencySet[] dependencySets=m_unionDependencySetsToClear[unionDependencySetIndex].m_dependencySets;
            for (int dependencySetIndex=dependencySets.length-1;dependencySetIndex>=0;--dependencySetIndex)
                dependencySets[dependencySetIndex]=null;
        }
        for (int variableIndex=0;variableIndex<m_maxNumberOfVariables;variableIndex++)
            m_valuesBuffer[variableIndex]=null;
    }
    public void applyDLClauses() {
        for (int index=0;index<m_deltaOldRetrievals.length;index++) {
            ExtensionTable.Retrieval deltaOldRetrieval=m_deltaOldRetrievals[index];
            deltaOldRetrieval.open();
            Object[] deltaOldTupleBuffer=deltaOldRetrieval.getTupleBuffer();
            while (!deltaOldRetrieval.afterLast() && !m_extensionManager.containsClash()) {
                Object deltaOldPredicate=deltaOldTupleBuffer[0];
                CompiledDLClauseInfo unoptimizedCompiledDLClauseInfo=m_tupleConsumersByDeltaPredicate.get(deltaOldPredicate);
                boolean applyUnoptimized=true;
                if (unoptimizedCompiledDLClauseInfo!=null && deltaOldTupleBuffer[0] instanceof AtomicRole) {
                    CompiledDLClauseInfo unguardedCompiledDLClauseInfo=m_atomicRoleTupleConsumersUnguarded.get(deltaOldPredicate);
                    if (unoptimizedCompiledDLClauseInfo.m_indexInList>((Node)deltaOldTupleBuffer[1]).getNumberOfPositiveAtomicConcepts()+((Node)deltaOldTupleBuffer[2]).getNumberOfPositiveAtomicConcepts()+(unguardedCompiledDLClauseInfo==null ? 0 : unguardedCompiledDLClauseInfo.m_indexInList)) {
                        applyUnoptimized=false;
                        while (unguardedCompiledDLClauseInfo!=null && !m_extensionManager.containsClash()) {
                            unguardedCompiledDLClauseInfo.m_evaluator.evaluate();
                            unguardedCompiledDLClauseInfo=unguardedCompiledDLClauseInfo.m_next;
                        }
                        if (!m_extensionManager.containsClash()) {
                            Map<AtomicConcept,CompiledDLClauseInfo> compiledDLClauseInfos=m_atomicRoleTupleConsumersByGuardConcept1.get(deltaOldPredicate);
                            if (compiledDLClauseInfos!=null) {
                                m_binaryTableRetrieval.getBindingsBuffer()[1]=deltaOldTupleBuffer[1];
                                m_binaryTableRetrieval.open();
                                Object[] binaryTableTupleBuffer=m_binaryTableRetrieval.getTupleBuffer();
                                while (!m_binaryTableRetrieval.afterLast() && !m_extensionManager.containsClash()) {
                                    Object atomicConceptObject=binaryTableTupleBuffer[0];
                                    if (atomicConceptObject instanceof AtomicConcept) {
                                        CompiledDLClauseInfo optimizedCompiledDLClauseInfo=compiledDLClauseInfos.get(atomicConceptObject);
                                        while (optimizedCompiledDLClauseInfo!=null && !m_extensionManager.containsClash()) {
                                            optimizedCompiledDLClauseInfo.m_evaluator.evaluate();
                                            optimizedCompiledDLClauseInfo=optimizedCompiledDLClauseInfo.m_next;
                                        }
                                    }
                                    m_binaryTableRetrieval.next();
                                }
                            }
                        }
                        if (!m_extensionManager.containsClash()) {
                            Map<AtomicConcept,CompiledDLClauseInfo> compiledDLClauseInfos=m_atomicRoleTupleConsumersByGuardConcept2.get(deltaOldPredicate);
                            if (compiledDLClauseInfos!=null) {
                                m_binaryTableRetrieval.getBindingsBuffer()[1]=deltaOldTupleBuffer[2];
                                m_binaryTableRetrieval.open();
                                Object[] binaryTableTupleBuffer=m_binaryTableRetrieval.getTupleBuffer();
                                while (!m_binaryTableRetrieval.afterLast() && !m_extensionManager.containsClash()) {
                                    Object atomicConceptObject=binaryTableTupleBuffer[0];
                                    if (atomicConceptObject instanceof AtomicConcept) {
                                        CompiledDLClauseInfo optimizedCompiledDLClauseInfo=compiledDLClauseInfos.get(atomicConceptObject);
                                        while (optimizedCompiledDLClauseInfo!=null && !m_extensionManager.containsClash()) {
                                            optimizedCompiledDLClauseInfo.m_evaluator.evaluate();
                                            optimizedCompiledDLClauseInfo=optimizedCompiledDLClauseInfo.m_next;
                                        }
                                    }
                                    m_binaryTableRetrieval.next();
                                }
                            }
                        }
                    }
                }
                if (applyUnoptimized) {
                    while (unoptimizedCompiledDLClauseInfo!=null && !m_extensionManager.containsClash()) {
                        unoptimizedCompiledDLClauseInfo.m_evaluator.evaluate();
                        unoptimizedCompiledDLClauseInfo=unoptimizedCompiledDLClauseInfo.m_next;
                    }
                }
                deltaOldRetrieval.next();
            }
        }
    }

    protected static final class CompiledDLClauseInfo {
        protected final DLClauseEvaluator m_evaluator;
        protected final CompiledDLClauseInfo m_next;
        protected final int m_indexInList;

        public CompiledDLClauseInfo(DLClauseEvaluator evaluator,CompiledDLClauseInfo next) {
            m_evaluator=evaluator;
            m_next=next;
            if (m_next==null)
                m_indexInList=1;
            else
                m_indexInList=m_next.m_indexInList+1;
        }
    }

    public static final class BodyAtomsSwapper {
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
