package org.semanticweb.HermiT.tableau;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.io.Serializable;

import org.semanticweb.HermiT.model.*;

public class HyperresolutionManager implements Serializable {
    private static final long serialVersionUID=-4880817508962130189L;

    protected static final Node[] EMPTY_TUPLE=new Node[0];
    
    protected final Tableau m_tableau;
    protected final ExtensionTable.Retrieval[] m_deltaOldRetrievals;
    protected final Map<DLPredicate,CompiledDLClauseInfo> m_tupleConsumersByDeltaPredicate;
    
    public HyperresolutionManager(Tableau tableau) {
        m_tableau=tableau;
        ExtensionManager extensionManager=m_tableau.getExtensionManager();
        m_deltaOldRetrievals=new ExtensionTable.Retrieval[extensionManager.m_allExtensionTablesArray.length];
        for (int index=0;index<extensionManager.m_allExtensionTablesArray.length;index++) {
            ExtensionTable extensionTable=extensionManager.m_allExtensionTablesArray[index];
            m_deltaOldRetrievals[index]=extensionTable.createRetrieval(new boolean[extensionTable.getArity()],ExtensionTable.View.DELTA_OLD);
        }
        DLClauseCompiler compiler=new DLClauseCompiler(m_tableau);
        m_tupleConsumersByDeltaPredicate=new HashMap<DLPredicate,CompiledDLClauseInfo>();
        for (DLClause dlClause : m_tableau.m_dlOntology.getDLClauses()) {
            BodyAtomsSwapper bodyAtomsSwapper=new BodyAtomsSwapper(dlClause);
            ExtensionTable.View[] extensionViews=new ExtensionTable.View[dlClause.getBodyLength()];
            for (int bodyAtomIndex=0;bodyAtomIndex<dlClause.getBodyLength();bodyAtomIndex++) {
                DLClause swappedDLClause=bodyAtomsSwapper.getSwappedDLClause(bodyAtomIndex,extensionViews);
                TupleConsumer tupleConsumer=compiler.compile(swappedDLClause,extensionViews);
                addTupleConsumer(swappedDLClause.getBodyAtom(0).getDLPredicate(),tupleConsumer,swappedDLClause);
            }
        }
    }
    protected void addTupleConsumer(DLPredicate deltaDLPredicate,TupleConsumer tupleConsumer,DLClause dlClause) {
        CompiledDLClauseInfo nextTupleConsumer=new CompiledDLClauseInfo(tupleConsumer,m_tupleConsumersByDeltaPredicate.get(deltaDLPredicate));
        m_tupleConsumersByDeltaPredicate.put(deltaDLPredicate,nextTupleConsumer);
    }
    public boolean evaluate() {
        m_tableau.m_nominalIntroductionManager.processTargets();
        boolean hasChange=false;
        while (propagateDeltaNew()) {
            for (int index=0;index<m_deltaOldRetrievals.length;index++)
                processDeltaOld(m_deltaOldRetrievals[index]);
            hasChange=true;
            m_tableau.m_nominalIntroductionManager.processTargets();
        }
        return hasChange;
    }
    protected void processDeltaOld(ExtensionTable.Retrieval retrieval) {
        retrieval.open();
        Object[] tupleBuffer=retrieval.getTupleBuffer();
        while (!retrieval.afterLast()) {
            DependencySet dependencySet=retrieval.getDependencySet();
            CompiledDLClauseInfo compiledDLClauseInfo=m_tupleConsumersByDeltaPredicate.get(tupleBuffer[0]);
            while (compiledDLClauseInfo!=null) {
                compiledDLClauseInfo.m_tupleConsumer.consumeTuple(tupleBuffer,dependencySet);
                compiledDLClauseInfo=compiledDLClauseInfo.m_next;
            }
            retrieval.next();
        }
    }
    protected boolean propagateDeltaNew() {
        boolean hasChange=false;
        for (int index=0;index<m_deltaOldRetrievals.length;index++)
            if (m_deltaOldRetrievals[index].getExtensionTable().propagateDeltaNew())
                hasChange=true;
        return hasChange;
    }
    
    protected static final class CompiledDLClauseInfo implements Serializable {
        private static final long serialVersionUID=1927165256091855708L;

        protected final TupleConsumer m_tupleConsumer;
        protected final CompiledDLClauseInfo m_next;
        
        public CompiledDLClauseInfo(TupleConsumer tupleConsumer,CompiledDLClauseInfo next) {
            m_tupleConsumer=tupleConsumer;
            m_next=next;
        }
    }
    
    protected static final class BodyAtomsSwapper {
        protected final DLClause m_dlClause;
        protected final boolean[] m_usedAtoms;
        protected final List<Atom> m_reorderedAtoms;
        protected final Set<Variable> m_boundVariables;
        
        public BodyAtomsSwapper(DLClause dlClause) {
            m_dlClause=dlClause;
            m_usedAtoms=new boolean[m_dlClause.getBodyLength()];
            m_reorderedAtoms=new ArrayList<Atom>(m_dlClause.getBodyLength());
            m_boundVariables=new HashSet<Variable>();
        }
        public DLClause getSwappedDLClause(int bodyIndex,ExtensionTable.View[] extensionViews) {
            for (int index=m_usedAtoms.length-1;index>=0;--index)
                m_usedAtoms[index]=false;
            m_reorderedAtoms.clear();
            m_boundVariables.clear();
            Atom atom=m_dlClause.getBodyAtom(bodyIndex);
            atom.getVariables(m_boundVariables);
            m_reorderedAtoms.add(atom);
            m_usedAtoms[bodyIndex]=true;
            extensionViews[m_reorderedAtoms.size()-1]=ExtensionTable.View.DELTA_OLD;
            while (m_reorderedAtoms.size()!=m_usedAtoms.length) {
                Atom bestAtom=null;
                int bestAtomIndex=-1;
                int numberOfBoundVariablesInBestAtom=0;
                int numberOfUnboundVariablesInBestAtom=0;
                for (int index=m_usedAtoms.length-1;index>=0;--index)
                    if (!m_usedAtoms[index]) {
                        atom=m_dlClause.getBodyAtom(index);
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
                        if ((numberOfUnboundVariables==0 || !NodeIDLessThan.INSTANCE.equals(atom.getDLPredicate())) &&
                            (bestAtom==null ||
                             numberOfBoundVariables>numberOfBoundVariablesInBestAtom ||
                             (numberOfBoundVariables>=numberOfBoundVariablesInBestAtom && numberOfUnboundVariables<numberOfUnboundVariablesInBestAtom))) {
                            bestAtom=atom;
                            numberOfBoundVariablesInBestAtom=numberOfBoundVariables;
                            numberOfUnboundVariablesInBestAtom=numberOfUnboundVariables;
                            bestAtomIndex=index;
                        }
                    }
                m_reorderedAtoms.add(bestAtom);
                extensionViews[m_reorderedAtoms.size()-1]=ExtensionTable.View.EXTENSION_THIS;
                m_usedAtoms[bestAtomIndex]=true;
                bestAtom.getVariables(m_boundVariables);
            }
            Atom[] bodyAtoms=new Atom[m_reorderedAtoms.size()];
            m_reorderedAtoms.toArray(bodyAtoms);
            return m_dlClause.getChangedDLClause(bodyAtoms,null);
        }
    }
}
