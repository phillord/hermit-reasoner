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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.existentials.ExistentialExpansionStrategy;
import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLPredicate;
import org.semanticweb.HermiT.model.NodeIDLessEqualThan;
import org.semanticweb.HermiT.model.NodeIDsAscendingOrEqual;
import org.semanticweb.HermiT.model.Term;
import org.semanticweb.HermiT.model.Variable;
import org.semanticweb.HermiT.monitor.TableauMonitor;

public class DLClauseEvaluator implements Serializable {
    private static final long serialVersionUID=4639844159658590456L;
    protected static final String CRLF=System.getProperty("line.separator");

    protected final InterruptFlag m_interruptFlag;
    protected final ExtensionManager m_extensionManager;
    protected final ExtensionTable.Retrieval[] m_retrievals;
    protected final Worker[] m_workers;
    protected final DLClause m_bodyDLClause;
    protected final List<DLClause> m_headDLClauses;

    public DLClauseEvaluator(Tableau tableau,DLClause bodyDLClause,List<DLClause> headDLClauses,ExtensionTable.Retrieval firstAtomRetrieval,BufferSupply bufferSupply,ValuesBufferManager valuesBufferManager,GroundDisjunctionHeaderManager groundDisjunctionHeaderManager,Map<Integer,UnionDependencySet> unionDependencySetsBySize) {
        m_interruptFlag=tableau.m_interruptFlag;
        m_extensionManager=tableau.m_extensionManager;
        DLClauseCompiler compiler=new DLClauseCompiler(bufferSupply,valuesBufferManager,groundDisjunctionHeaderManager,unionDependencySetsBySize,this,m_extensionManager,tableau.getExistentialsExpansionStrategy(),bodyDLClause,headDLClauses,firstAtomRetrieval);
        m_retrievals=new ExtensionTable.Retrieval[compiler.m_retrievals.size()];
        compiler.m_retrievals.toArray(m_retrievals);
        m_workers=new Worker[compiler.m_workers.size()];
        compiler.m_workers.toArray(m_workers);
        m_bodyDLClause=bodyDLClause;
        m_headDLClauses=headDLClauses;
    }
    public int getBodyLength() {
        return m_bodyDLClause.getBodyLength();
    }
    public Atom getBodyAtom(int atomIndex) {
        return m_bodyDLClause.getBodyAtom(atomIndex);
    }
    public int getNumberOfDLClauses() {
        return m_headDLClauses.size();
    }
    public DLClause getDLClause(int dlClauseIndex) {
        return m_headDLClauses.get(dlClauseIndex);
    }
    public int getHeadLength(int dlClauseIndex) {
        return m_headDLClauses.get(dlClauseIndex).getHeadLength();
    }
    public Atom getHeadAtom(int dlClauseIndex,int atomIndex) {
        return m_headDLClauses.get(dlClauseIndex).getHeadAtom(atomIndex);
    }
    public Object[] getTupleMatchedToBody(int atomIndex) {
        return m_retrievals[atomIndex].getTupleBuffer();
    }
    public void evaluate() {
        int programCounter=0;
        while (programCounter<m_workers.length && !m_extensionManager.containsClash()) {
            m_interruptFlag.checkInterrupt();
            programCounter=m_workers[programCounter].execute(programCounter);
        }
    }
    public String toString() {
        StringBuffer buffer=new StringBuffer();
        int maximalPCLength=String.valueOf(m_workers.length-1).length();
        for (int programCounter=0;programCounter<m_workers.length;programCounter++) {
            String programCounterString=String.valueOf(programCounter);
            for (int count=maximalPCLength-programCounterString.length();count>0;--count)
                buffer.append(' ');
            buffer.append(programCounterString);
            buffer.append(": ");
            buffer.append(m_workers[programCounter].toString());
            buffer.append(CRLF);
        }
        return buffer.toString();
    }

    public static class BufferSupply {
        protected final List<Object[]> m_allBuffers;
        protected final Map<Integer,List<Object[]>> m_availableBuffersByArity;

        public BufferSupply() {
            m_allBuffers=new ArrayList<Object[]>();
            m_availableBuffersByArity=new HashMap<Integer,List<Object[]>>();
        }
        public void reuseBuffers() {
            m_availableBuffersByArity.clear();
            for (Object[] buffer : m_allBuffers) {
                Integer arityInteger=Integer.valueOf(buffer.length);
                List<Object[]> buffers=m_availableBuffersByArity.get(arityInteger);
                if (buffers==null) {
                    buffers=new ArrayList<Object[]>();
                    m_availableBuffersByArity.put(arityInteger,buffers);
                }
                buffers.add(buffer);
            }
        }
        public Object[] getBuffer(int arity) {
            Object[] buffer;
            Integer arityInteger=Integer.valueOf(arity);
            List<Object[]> buffers=m_availableBuffersByArity.get(arityInteger);
            if (buffers==null || buffers.isEmpty()) {
                buffer=new Object[arity];
                m_allBuffers.add(buffer);
            }
            else
                buffer=buffers.remove(buffers.size()-1);
            return buffer;
        }
        public Object[][] getAllBuffers() {
            Object[][] result=new Object[m_allBuffers.size()][];
            m_allBuffers.toArray(result);
            return result;
        }
    }

    public static class ValuesBufferManager {
        public final Object[] m_valuesBuffer;
        public final Map<DLPredicate,Integer> m_bodyDLPredicatesToIndexes;
        public final int m_maxNumberOfVariables;
        public final Map<Term,Integer> m_bodyNonvariableTermsToIndexes;

        public ValuesBufferManager(Set<DLClause> dlClauses,Map<Term,Node> termsToNodes) {
            Set<DLPredicate> bodyDLPredicates=new HashSet<DLPredicate>();
            Set<Variable> variables=new HashSet<Variable>();
            m_bodyNonvariableTermsToIndexes=new HashMap<Term,Integer>();
            int maxNumberOfVariables=0;
            for (DLClause dlClause : dlClauses) {
                variables.clear();
                for (int bodyIndex=dlClause.getBodyLength()-1;bodyIndex>=0;--bodyIndex) {
                    Atom atom=dlClause.getBodyAtom(bodyIndex);
                    bodyDLPredicates.add(atom.getDLPredicate());
                    for (int argumentIndex=0;argumentIndex<atom.getArity();argumentIndex++) {
                        Term term=atom.getArgument(argumentIndex);
                        if (term instanceof Variable)
                            variables.add((Variable)term);
                        else
                            m_bodyNonvariableTermsToIndexes.put(term,-1);
                    }
                }
                if (variables.size()>maxNumberOfVariables)
                    maxNumberOfVariables=variables.size();
            }
            m_valuesBuffer=new Object[maxNumberOfVariables+bodyDLPredicates.size()+m_bodyNonvariableTermsToIndexes.size()];
            m_bodyDLPredicatesToIndexes=new HashMap<DLPredicate,Integer>();
            int bindingIndex=maxNumberOfVariables;
            for (DLPredicate bodyDLPredicate : bodyDLPredicates) {
                m_bodyDLPredicatesToIndexes.put(bodyDLPredicate,Integer.valueOf(bindingIndex));
                m_valuesBuffer[bindingIndex]=bodyDLPredicate;
                bindingIndex++;
            }
            for (Map.Entry<Term,Integer> entry : m_bodyNonvariableTermsToIndexes.entrySet()) {
                Node termNode=termsToNodes.get(entry.getKey());
                if (termNode==null)
                    throw new IllegalArgumentException("Term '"+entry.getValue()+"' is unknown to the reasoner.");
                entry.setValue(bindingIndex);
                m_valuesBuffer[bindingIndex]=termNode.getCanonicalNode();
                bindingIndex++;
            }
            m_maxNumberOfVariables=maxNumberOfVariables;
        }
    }

    public static class GroundDisjunctionHeaderManager {
        protected GroundDisjunctionHeader[] m_buckets;
        protected int m_numberOfElements;
        protected int m_threshold;

        public GroundDisjunctionHeaderManager() {
            m_buckets=new GroundDisjunctionHeader[1024];
            m_threshold=(int)(m_buckets.length*0.75);
            m_numberOfElements=0;
        }
        public GroundDisjunctionHeader get(DLPredicate[] dlPredicates) {
            int hashCode=0;
            for (int disjunctIndex=0;disjunctIndex<dlPredicates.length;disjunctIndex++)
                hashCode=hashCode*7+dlPredicates[disjunctIndex].hashCode();
            int bucketIndex=getIndexFor(hashCode,m_buckets.length);
            GroundDisjunctionHeader entry=m_buckets[bucketIndex];
            while (entry!=null) {
                if (hashCode==entry.m_hashCode && entry.isEqual(dlPredicates))
                    return entry;
                entry=entry.m_nextEntry;
            }
            entry=new GroundDisjunctionHeader(dlPredicates,hashCode,entry);
            m_buckets[bucketIndex]=entry;
            m_numberOfElements++;
            if (m_numberOfElements>=m_threshold)
                resize(m_buckets.length*2);
            return entry;
        }
        protected void resize(int newCapacity) {
            GroundDisjunctionHeader[] newBuckets=new GroundDisjunctionHeader[newCapacity];
            for (int i=0;i<m_buckets.length;i++) {
                GroundDisjunctionHeader entry=m_buckets[i];
                while (entry!=null) {
                    GroundDisjunctionHeader nextEntry=entry.m_nextEntry;
                    int newIndex=getIndexFor(entry.hashCode(),newCapacity);
                    entry.m_nextEntry=newBuckets[newIndex];
                    newBuckets[newIndex]=entry;
                    entry=nextEntry;
                }
            }
            m_buckets=newBuckets;
            m_threshold=(int)(newCapacity*0.75);
        }
        protected static int getIndexFor(int hashCode,int tableLength) {
            hashCode+=~(hashCode << 9);
            hashCode^=(hashCode >>> 14);
            hashCode+=(hashCode << 4);
            hashCode^=(hashCode >>> 10);
            return hashCode & (tableLength-1);
        }
    }

    public static interface Worker {
        int execute(int programCounter);
    }

    protected static interface BranchingWorker extends Worker {
        int getBranchingAddress();
        void setBranchingAddress(int branchingAddress);
    }

    protected static final class CopyValues implements Worker,Serializable {
        private static final long serialVersionUID=-4323769483485648756L;

        protected final Object[] m_fromBuffer;
        protected final int m_fromIndex;
        protected final Object[] m_toBuffer;
        protected final int m_toIndex;

        public CopyValues(Object[] fromBuffer,int fromIndex,Object[] toBuffer,int toIndex) {
            m_fromBuffer=fromBuffer;
            m_fromIndex=fromIndex;
            m_toBuffer=toBuffer;
            m_toIndex=toIndex;
        }
        public int execute(int programCounter) {
            m_toBuffer[m_toIndex]=m_fromBuffer[m_fromIndex];
            return programCounter+1;
        }
        public String toString() {
            return "Copy "+m_fromIndex+" --> "+m_toIndex;
        }
    }

    protected static final class CopyDependencySet implements Worker,Serializable {
        private static final long serialVersionUID=705172386083123813L;

        protected final ExtensionTable.Retrieval m_retrieval;
        protected final DependencySet[] m_targetDependencySets;
        protected final int m_targetIndex;

        public CopyDependencySet(ExtensionTable.Retrieval retrieval,DependencySet[] targetDependencySets,int targetIndex) {
            m_retrieval=retrieval;
            m_targetDependencySets=targetDependencySets;
            m_targetIndex=targetIndex;
        }
        public int execute(int programCounter) {
            m_targetDependencySets[m_targetIndex]=m_retrieval.getDependencySet();
            return programCounter+1;
        }
        public String toString() {
            return "Copy dependency set to "+m_targetIndex;
        }
    }

    protected static final class BranchIfNotEqual implements BranchingWorker,Serializable {
        private static final long serialVersionUID=-1880147431680856293L;

        protected int m_notEqualProgramCounter;
        protected final Object[] m_buffer;
        protected final int m_index1;
        protected final int m_index2;

        public BranchIfNotEqual(int notEqualProgramCounter,Object[] buffer,int index1,int index2) {
            m_notEqualProgramCounter=notEqualProgramCounter;
            m_buffer=buffer;
            m_index1=index1;
            m_index2=index2;
        }
        public int execute(int programCounter) {
            if (m_buffer[m_index1].equals(m_buffer[m_index2]))
                return programCounter+1;
            else
                return m_notEqualProgramCounter;
        }
        public int getBranchingAddress() {
            return m_notEqualProgramCounter;
        }
        public void setBranchingAddress(int branchingAddress) {
            m_notEqualProgramCounter=branchingAddress;
        }
        public String toString() {
            return "Branch to "+m_notEqualProgramCounter+" if "+m_index1+" != "+m_index2;
        }
    }

    protected static final class BranchIfNotNodeIDLessEqualThan implements BranchingWorker,Serializable {
        private static final long serialVersionUID=2484359261424674914L;

        protected int m_notLessProgramCounter;
        protected final Object[] m_buffer;
        protected final int m_index1;
        protected final int m_index2;

        public BranchIfNotNodeIDLessEqualThan(int notLessProgramCounter,Object[] buffer,int index1,int index2) {
            m_notLessProgramCounter=notLessProgramCounter;
            m_buffer=buffer;
            m_index1=index1;
            m_index2=index2;
        }
        public int execute(int programCounter) {
            if (((Node)m_buffer[m_index1]).getNodeID()<=((Node)m_buffer[m_index2]).getNodeID())
                return programCounter+1;
            else
                return m_notLessProgramCounter;
        }
        public int getBranchingAddress() {
            return m_notLessProgramCounter;
        }
        public void setBranchingAddress(int branchingAddress) {
            m_notLessProgramCounter=branchingAddress;
        }
        public String toString() {
            return "Branch to "+m_notLessProgramCounter+" if "+m_index1+".ID > "+m_index2+".ID";
        }
    }

    protected static final class BranchIfNotNodeIDsAscendingOrEqual implements BranchingWorker,Serializable {
        private static final long serialVersionUID=8053779312249250349L;

        protected int m_branchProgramCounter;
        protected final Object[] m_buffer;
        protected final int[] m_nodeIndexes;

        public BranchIfNotNodeIDsAscendingOrEqual(int branchProgramCounter,Object[] buffer,int[] nodeIndexes) {
            m_branchProgramCounter=branchProgramCounter;
            m_buffer=buffer;
            m_nodeIndexes=nodeIndexes;
        }
        public int execute(int programCounter) {
            boolean strictlyAscending=true;
            boolean allEqual=true;
            int lastNodeID=((Node)m_buffer[m_nodeIndexes[0]]).getNodeID();
            for (int index=1;index<m_nodeIndexes.length;index++) {
                int nodeID=((Node)m_buffer[m_nodeIndexes[index]]).getNodeID();
                if (lastNodeID>=nodeID)
                    strictlyAscending=false;
                if (nodeID!=lastNodeID)
                    allEqual=false;
                lastNodeID=nodeID;
            }
            if ((!strictlyAscending && allEqual) || (strictlyAscending && !allEqual))
                return programCounter+1;
            else
                return m_branchProgramCounter;
        }
        public int getBranchingAddress() {
            return m_branchProgramCounter;
        }
        public void setBranchingAddress(int branchingAddress) {
            m_branchProgramCounter=branchingAddress;
        }
        public String toString() {
            return "Branch to "+m_branchProgramCounter+" if node IDs are not ascending or equal";
        }
    }

    protected static final class OpenRetrieval implements Worker,Serializable {
        private static final long serialVersionUID=8246610603084803950L;

        protected final ExtensionTable.Retrieval m_retrieval;

        public OpenRetrieval(ExtensionTable.Retrieval retrieval) {
            m_retrieval=retrieval;
        }
        public int execute(int programCounter) {
            m_retrieval.open();
            return programCounter+1;
        }
        public String toString() {
            return "Open "+m_retrieval.getBindingsBuffer()[m_retrieval.getBindingPositions()[0]];
        }
    }

    protected static final class NextRetrieval implements Worker,Serializable {
        private static final long serialVersionUID=-2787897558147109082L;

        protected final ExtensionTable.Retrieval m_retrieval;

        public NextRetrieval(ExtensionTable.Retrieval retrieval) {
            m_retrieval=retrieval;
        }
        public int execute(int programCounter) {
            m_retrieval.next();
            return programCounter+1;
        }
        public String toString() {
            return "Next "+m_retrieval.getBindingsBuffer()[m_retrieval.getBindingPositions()[0]];
        }
    }

    protected static final class HasMoreRetrieval implements BranchingWorker,Serializable {
        private static final long serialVersionUID=-2415094151423166585L;

        protected int m_eofProgramCounter;
        protected final ExtensionTable.Retrieval m_retrieval;

        public HasMoreRetrieval(int eofProgramCounter,ExtensionTable.Retrieval retrieval) {
            m_eofProgramCounter=eofProgramCounter;
            m_retrieval=retrieval;
        }
        public int execute(int programCounter) {
            if (m_retrieval.afterLast())
                return m_eofProgramCounter;
            else
                return programCounter+1;
        }
        public int getBranchingAddress() {
            return m_eofProgramCounter;
        }
        public void setBranchingAddress(int branchingAddress) {
            m_eofProgramCounter=branchingAddress;
        }
        public String toString() {
            return "Branch to "+m_eofProgramCounter+" if "+m_retrieval.getBindingsBuffer()[m_retrieval.getBindingPositions()[0]]+" is empty";
        }
    }

    protected static final class JumpTo implements BranchingWorker,Serializable {
        private static final long serialVersionUID=-6957866973028474739L;

        protected int m_jumpTo;

        public JumpTo(int jumpTo) {
            m_jumpTo=jumpTo;
        }
        public int execute(int programCounter) {
            return m_jumpTo;
        }
        public int getBranchingAddress() {
            return m_jumpTo;
        }
        public void setBranchingAddress(int branchingAddress) {
            m_jumpTo=branchingAddress;
        }
        public String toString() {
            return "Jump to "+m_jumpTo;
        }
    }

    protected static final class CallMatchStartedOnMonitor implements Worker,Serializable {
        private static final long serialVersionUID=8736659573939242252L;

        protected final TableauMonitor m_tableauMonitor;
        protected final DLClauseEvaluator m_dlClauseEvaluator;
        protected final int m_dlClauseIndex;

        public CallMatchStartedOnMonitor(TableauMonitor tableauMonitor,DLClauseEvaluator dlClauseEvaluator,int dlClauseIndex) {
            m_tableauMonitor=tableauMonitor;
            m_dlClauseEvaluator=dlClauseEvaluator;
            m_dlClauseIndex=dlClauseIndex;
        }
        public int execute(int programCounter) {
            m_tableauMonitor.dlClauseMatchedStarted(m_dlClauseEvaluator,m_dlClauseIndex);
            return programCounter+1;
        }
        public String toString() {
            return "Monitor -> Match started";
        }
    }

    protected static final class CallMatchFinishedOnMonitor implements Worker,Serializable {
        private static final long serialVersionUID=1046400921858176361L;

        protected final TableauMonitor m_tableauMonitor;
        protected final DLClauseEvaluator m_dlClauseEvaluator;
        protected final int m_dlClauseIndex;

        public CallMatchFinishedOnMonitor(TableauMonitor tableauMonitor,DLClauseEvaluator dlClauseEvaluator,int dlClauseIndex) {
            m_tableauMonitor=tableauMonitor;
            m_dlClauseEvaluator=dlClauseEvaluator;
            m_dlClauseIndex=dlClauseIndex;
        }
        public int execute(int programCounter) {
            m_tableauMonitor.dlClauseMatchedFinished(m_dlClauseEvaluator,m_dlClauseIndex);
            return programCounter+1;
        }
        public String toString() {
            return "Monitor -> Match finished";
        }
    }

    protected static final class SetClash implements Worker,Serializable {
        private static final long serialVersionUID=-4981087765064918953L;

        protected final ExtensionManager m_extensionManager;
        protected final DependencySet m_dependencySet;

        public SetClash(ExtensionManager extensionManager,DependencySet dependencySet) {
            m_extensionManager=extensionManager;
            m_dependencySet=dependencySet;
        }
        public int execute(int programCounter) {
            m_extensionManager.setClash(m_dependencySet);
            return programCounter+1;
        }
        public String toString() {
            return "Set clash";
        }
    }

    protected static final class DeriveUnaryFact implements Worker,Serializable {
        private static final long serialVersionUID=7883620022252842010L;

        protected final ExtensionManager m_extensionManager;
        protected final Object[] m_valuesBuffer;
        protected final boolean[] m_coreVariables;
        protected final DependencySet m_dependencySet;
        protected final DLPredicate m_dlPredicate;
        protected final int m_argumentIndex;

        public DeriveUnaryFact(ExtensionManager extensionManager,Object[] valuesBuffer,boolean[] coreVariables,DependencySet dependencySet,DLPredicate dlPredicate,int argumentIndex) {
            m_extensionManager=extensionManager;
            m_valuesBuffer=valuesBuffer;
            m_coreVariables=coreVariables;
            m_dependencySet=dependencySet;
            m_argumentIndex=argumentIndex;
            m_dlPredicate=dlPredicate;
        }
        public int execute(int programCounter) {
            Node argument=(Node)m_valuesBuffer[m_argumentIndex];
            boolean isCore=m_coreVariables[m_argumentIndex];
            m_extensionManager.addAssertion(m_dlPredicate,argument,m_dependencySet,isCore);
            return programCounter+1;
        }
        public String toString() {
            return "Derive unary fact";
        }
    }

    protected static final class DeriveBinaryFact implements Worker,Serializable {
        private static final long serialVersionUID=1823363493615682288L;

        protected final ExtensionManager m_extensionManager;
        protected final Object[] m_valuesBuffer;
        protected final DependencySet m_dependencySet;
        protected final DLPredicate m_dlPredicate;
        protected final int m_argumentIndex1;
        protected final int m_argumentIndex2;

        public DeriveBinaryFact(ExtensionManager extensionManager,Object[] valuesBuffer,DependencySet dependencySet,DLPredicate dlPredicate,int argumentIndex1,int argumentIndex2) {
            m_extensionManager=extensionManager;
            m_valuesBuffer=valuesBuffer;
            m_dependencySet=dependencySet;
            m_dlPredicate=dlPredicate;
            m_argumentIndex1=argumentIndex1;
            m_argumentIndex2=argumentIndex2;
        }
        public int execute(int programCounter) {
            Node argument1=(Node)m_valuesBuffer[m_argumentIndex1];
            Node argument2=(Node)m_valuesBuffer[m_argumentIndex2];
            m_extensionManager.addAssertion(m_dlPredicate,argument1,argument2,m_dependencySet,true);
            return programCounter+1;
        }
        public String toString() {
            return "Derive binary fact";
        }
    }

    protected static final class DeriveTernaryFact implements Worker,Serializable {
        private static final long serialVersionUID=1823363493615682288L;

        protected final ExtensionManager m_extensionManager;
        protected final Object[] m_valuesBuffer;
        protected final DependencySet m_dependencySet;
        protected final DLPredicate m_dlPredicate;
        protected final int m_argumentIndex1;
        protected final int m_argumentIndex2;
        protected final int m_argumentIndex3;

        public DeriveTernaryFact(ExtensionManager extensionManager,Object[] valuesBuffer,DependencySet dependencySet,DLPredicate dlPredicate,int argumentIndex1,int argumentIndex2,int argumentIndex3) {
            m_extensionManager=extensionManager;
            m_valuesBuffer=valuesBuffer;
            m_dependencySet=dependencySet;
            m_dlPredicate=dlPredicate;
            m_argumentIndex1=argumentIndex1;
            m_argumentIndex2=argumentIndex2;
            m_argumentIndex3=argumentIndex3;
        }
        public int execute(int programCounter) {
            Node argument1=(Node)m_valuesBuffer[m_argumentIndex1];
            Node argument2=(Node)m_valuesBuffer[m_argumentIndex2];
            Node argument3=(Node)m_valuesBuffer[m_argumentIndex3];
            m_extensionManager.addAssertion(m_dlPredicate,argument1,argument2,argument3,m_dependencySet,true);
            return programCounter+1;
        }
        public String toString() {
            return "Derive ternary fact";
        }
    }

    protected static final class DeriveDisjunction implements Worker,Serializable {
        private static final long serialVersionUID=-3546622575743138887L;

        protected final Tableau m_tableau;
        protected final Object[] m_valuesBuffer;
        protected final boolean[] m_coreVariables;
        protected final DependencySet m_dependencySet;
        protected final GroundDisjunctionHeader m_groundDisjunctionHeader;
        protected final int[] m_copyIsCore;
        protected final int[] m_copyValuesToArguments;

        public DeriveDisjunction(Object[] valuesBuffer,boolean[] coreVariables,DependencySet dependencySet,Tableau tableau,GroundDisjunctionHeader groundDisjunctionHeader,int[] copyIsCore,int[] copyValuesToArguments) {
            m_valuesBuffer=valuesBuffer;
            m_coreVariables=coreVariables;
            m_dependencySet=dependencySet;
            m_tableau=tableau;
            m_groundDisjunctionHeader=groundDisjunctionHeader;
            m_copyIsCore=copyIsCore;
            m_copyValuesToArguments=copyValuesToArguments;
        }
        public void clear() {
        }
        public int execute(int programCounter) {
            Node[] arguments=new Node[m_copyValuesToArguments.length];
            for (int argumentIndex=m_copyValuesToArguments.length-1;argumentIndex>=0;--argumentIndex)
                arguments[argumentIndex]=(Node)m_valuesBuffer[m_copyValuesToArguments[argumentIndex]];
            boolean[] isCore=new boolean[m_copyIsCore.length];
            for (int copyIndex=m_copyIsCore.length-1;copyIndex>=0;--copyIndex) {
                int copyFrom=m_copyIsCore[copyIndex];
                if (copyFrom==-1)
                    isCore[copyIndex]=true;
                else
                    isCore[copyIndex]=m_coreVariables[copyFrom];
            }
            GroundDisjunction groundDisjunction=new GroundDisjunction(m_tableau,m_groundDisjunctionHeader,arguments,isCore,m_tableau.m_dependencySetFactory.getPermanent(m_dependencySet));
            if (!groundDisjunction.isSatisfied(m_tableau))
                m_tableau.addGroundDisjunction(groundDisjunction);
            return programCounter+1;
        }
        public String toString() {
            return "Derive disjunction";
        }
    }

    protected static final class DLClauseCompiler extends ConjunctionCompiler {
        protected final DLClauseEvaluator m_dlClauseEvalautor;
        protected final GroundDisjunctionHeaderManager m_groundDisjunctionHeaderManager;
        protected final ExistentialExpansionStrategy m_existentialExpansionStrategy;
        protected final DLClause m_bodyDLClause;
        protected final List<DLClause> m_headDLClauses;
        protected final boolean[] m_coreVariables;

        public DLClauseCompiler(BufferSupply bufferSupply,ValuesBufferManager valuesBufferManager,GroundDisjunctionHeaderManager groundDisjunctionHeaderManager,Map<Integer,UnionDependencySet> unionDependencySetsBySize,DLClauseEvaluator dlClauseEvalautor,ExtensionManager extensionManager,ExistentialExpansionStrategy existentialExpansionStrategy,DLClause bodyDLClause,List<DLClause> headDLClauses,ExtensionTable.Retrieval firstAtomRetrieval) {
            super(bufferSupply,valuesBufferManager,unionDependencySetsBySize,extensionManager,bodyDLClause.getBodyAtoms(),getHeadVariables(headDLClauses));
            m_groundDisjunctionHeaderManager=groundDisjunctionHeaderManager;
            m_dlClauseEvalautor=dlClauseEvalautor;
            m_existentialExpansionStrategy=existentialExpansionStrategy;
            m_bodyDLClause=bodyDLClause;
            m_headDLClauses=headDLClauses;
            m_coreVariables=new boolean[m_variables.size()];
            generateCode(1,firstAtomRetrieval);
        }
        protected int getNumberOfHeads() {
            return m_headDLClauses.size();
        }
        protected int getHeadLength(int dlClauseIndex) {
            return m_headDLClauses.get(dlClauseIndex).getHeadLength();
        }
        protected Atom getHeadAtom(int dlClauseIndex,int atomIndex) {
            return m_headDLClauses.get(dlClauseIndex).getHeadAtom(atomIndex);
        }
        protected void compileHeads() {
            m_existentialExpansionStrategy.dlClauseBodyCompiled(m_workers,m_bodyDLClause,m_variables,m_valuesBufferManager.m_valuesBuffer,m_coreVariables);
            for (int dlClauseIndex=0;dlClauseIndex<getNumberOfHeads();dlClauseIndex++) {
                if (m_extensionManager.m_tableauMonitor!=null)
                    m_workers.add(new CallMatchStartedOnMonitor(m_extensionManager.m_tableauMonitor,m_dlClauseEvalautor,dlClauseIndex));
                if (getHeadLength(dlClauseIndex)==0)
                    m_workers.add(new SetClash(m_extensionManager,m_unionDependencySet));
                else if (getHeadLength(dlClauseIndex)==1) {
                    Atom atom=getHeadAtom(dlClauseIndex,0);
                    switch (atom.getArity()) {
                    case 1:
                        m_workers.add(new DeriveUnaryFact(m_extensionManager,m_valuesBufferManager.m_valuesBuffer,m_coreVariables,m_unionDependencySet,atom.getDLPredicate(),m_variables.indexOf(atom.getArgumentVariable(0))));
                        break;
                    case 2:
                        m_workers.add(new DeriveBinaryFact(m_extensionManager,m_valuesBufferManager.m_valuesBuffer,m_unionDependencySet,atom.getDLPredicate(),m_variables.indexOf(atom.getArgumentVariable(0)),m_variables.indexOf(atom.getArgumentVariable(1))));
                        break;
                    case 3:
                        m_workers.add(new DeriveTernaryFact(m_extensionManager,m_valuesBufferManager.m_valuesBuffer,m_unionDependencySet,atom.getDLPredicate(),m_variables.indexOf(atom.getArgumentVariable(0)),m_variables.indexOf(atom.getArgumentVariable(1)),m_variables.indexOf(atom.getArgumentVariable(2))));
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported atom arity.");
                    }
                }
                else {
                    int totalNumberOfArguments=0;
                    for (int headIndex=0;headIndex<getHeadLength(dlClauseIndex);headIndex++)
                        totalNumberOfArguments+=getHeadAtom(dlClauseIndex,headIndex).getArity();
                    DLPredicate[] headDLPredicates=new DLPredicate[getHeadLength(dlClauseIndex)];
                    int[] copyIsCore=new int[getHeadLength(dlClauseIndex)];
                    int[] copyValuesToArguments=new int[totalNumberOfArguments];
                    int index=0;
                    for (int headIndex=0;headIndex<getHeadLength(dlClauseIndex);headIndex++) {
                        Atom atom=getHeadAtom(dlClauseIndex,headIndex);
                        headDLPredicates[headIndex]=atom.getDLPredicate();
                        for (int argumentIndex=0;argumentIndex<atom.getArity();argumentIndex++) {
                            Variable variable=atom.getArgumentVariable(argumentIndex);
                            int variableIndex=m_variables.indexOf(variable);
                            assert variableIndex!=-1;
                            copyValuesToArguments[index++]=variableIndex;
                        }
                        if (headDLPredicates[headIndex].getArity()==1) {
                            Variable variable=atom.getArgumentVariable(0);
                            copyIsCore[headIndex]=m_variables.indexOf(variable);
                        }
                        else
                            copyIsCore[headIndex]=-1;
                    }
                    GroundDisjunctionHeader groundDisjunctionHeader=m_groundDisjunctionHeaderManager.get(headDLPredicates);
                    m_workers.add(new DeriveDisjunction(m_valuesBufferManager.m_valuesBuffer,m_coreVariables,m_unionDependencySet,m_extensionManager.m_tableau,groundDisjunctionHeader,copyIsCore,copyValuesToArguments));
                }
                if (m_extensionManager.m_tableauMonitor!=null)
                    m_workers.add(new CallMatchFinishedOnMonitor(m_extensionManager.m_tableauMonitor,m_dlClauseEvalautor,dlClauseIndex));
            }
        }
        protected static List<Variable> getHeadVariables(List<DLClause> headDLClauses) {
            List<Variable> result=new ArrayList<Variable>();
            for (DLClause dlClause : headDLClauses) {
                for (int headIndex=0;headIndex<dlClause.getHeadLength();headIndex++) {
                    Atom atom=dlClause.getHeadAtom(headIndex);
                    for (int argumentIndex=0;argumentIndex<atom.getArity();argumentIndex++) {
                        Variable variable=atom.getArgumentVariable(argumentIndex);
                        if (variable!=null && !result.contains(variable))
                            result.add(variable);
                    }
                }
            }
            return result;
        }
    }
    
    public static abstract class ConjunctionCompiler {
        protected final BufferSupply m_bufferSupply;
        protected final ValuesBufferManager m_valuesBufferManager;
        protected final ExtensionManager m_extensionManager;
        protected final Atom[] m_bodyAtoms;
        protected final List<Variable> m_variables;
        protected final Set<Variable> m_boundSoFar;
        protected final UnionDependencySet m_unionDependencySet;
        protected final List<ExtensionTable.Retrieval> m_retrievals;
        public final List<Worker> m_workers;
        protected final List<Integer> m_labels;

        public ConjunctionCompiler(BufferSupply bufferSupply,ValuesBufferManager valuesBufferManager,Map<Integer,UnionDependencySet> unionDependencySetsBySize,ExtensionManager extensionManager,Atom[] bodyAtoms,List<Variable> headVariables) {
            m_bufferSupply=bufferSupply;
            m_valuesBufferManager=valuesBufferManager;
            m_extensionManager=extensionManager;
            m_bodyAtoms=bodyAtoms;
            m_variables=new ArrayList<Variable>();
            m_boundSoFar=new HashSet<Variable>();
            int numberOfRealAtoms=0;
            for (int bodyIndex=0;bodyIndex<getBodyLength();bodyIndex++) {
                Atom atom=getBodyAtom(bodyIndex);
                for (int argumentIndex=0;argumentIndex<atom.getArity();argumentIndex++) {
                    Variable variable=atom.getArgumentVariable(argumentIndex);
                    if (variable!=null && !m_variables.contains(variable) && occursInBodyAtomsAfter(variable,bodyIndex+1))
                        m_variables.add(variable);
                }
                if (!atom.getDLPredicate().equals(NodeIDLessEqualThan.INSTANCE) && !(atom.getDLPredicate() instanceof NodeIDsAscendingOrEqual))
                    numberOfRealAtoms++;
            }
            for (Variable variable : headVariables)
                if (!m_variables.contains(variable))
                    m_variables.add(variable);
            if (unionDependencySetsBySize!=null) {
                Integer numberOfRealAtomsInteger=Integer.valueOf(numberOfRealAtoms);
                UnionDependencySet unionDependencySet=unionDependencySetsBySize.get(numberOfRealAtomsInteger);
                if (unionDependencySet==null) {
                    unionDependencySet=new UnionDependencySet(numberOfRealAtoms);
                    unionDependencySetsBySize.put(numberOfRealAtomsInteger,unionDependencySet);
                }
                m_unionDependencySet=unionDependencySet;
            }
            else
                m_unionDependencySet=null;
            m_retrievals=new ArrayList<ExtensionTable.Retrieval>();
            m_workers=new ArrayList<Worker>();
            m_labels=new ArrayList<Integer>();
        }
        protected final void generateCode(int firstBodyAtomToCompile,ExtensionTable.Retrieval firstAtomRetrieval) {
            m_labels.add(null);
            m_retrievals.add(firstAtomRetrieval);
            int afterRule=addLabel();
            if (firstBodyAtomToCompile>0) {
                compileCheckUnboundVariableMatches(getBodyAtom(0),firstAtomRetrieval,afterRule);
                compileGenerateBindings(firstAtomRetrieval,getBodyAtom(0));
                if (m_unionDependencySet!=null)
                    m_workers.add(new CopyDependencySet(firstAtomRetrieval,m_unionDependencySet.m_dependencySets,0));
            }
            compileBodyAtom(firstBodyAtomToCompile,afterRule);
            setLabelProgramCounter(afterRule);
            for (Worker worker : m_workers)
                if (worker instanceof BranchingWorker) {
                    BranchingWorker branchingWorker=(BranchingWorker)worker;
                    int branchingAddress=branchingWorker.getBranchingAddress();
                    if (branchingAddress<0) {
                        int resolvedAddress=m_labels.get(-branchingAddress);
                        branchingWorker.setBranchingAddress(resolvedAddress);
                    }
                }
        }
        protected final boolean occursInBodyAtomsAfter(Variable variable,int startIndex) {
            for (int argumentIndex=startIndex;argumentIndex<getBodyLength();argumentIndex++)
                if (getBodyAtom(argumentIndex).containsVariable(variable))
                    return true;
            return false;
        }
        protected final void compileBodyAtom(int bodyAtomIndex,int lastAtomNextElement) {
            if (bodyAtomIndex==getBodyLength())
                compileHeads();
            else if (getBodyAtom(bodyAtomIndex).getDLPredicate().equals(NodeIDLessEqualThan.INSTANCE)) {
                Atom atom=getBodyAtom(bodyAtomIndex);
                int variable1Index=m_variables.indexOf(atom.getArgumentVariable(0));
                int variable2Index=m_variables.indexOf(atom.getArgumentVariable(1));
                assert variable1Index!=-1;
                assert variable2Index!=-1;
                m_workers.add(new BranchIfNotNodeIDLessEqualThan(lastAtomNextElement,m_valuesBufferManager.m_valuesBuffer,variable1Index,variable2Index));
                compileBodyAtom(bodyAtomIndex+1,lastAtomNextElement);
            }
            else if (getBodyAtom(bodyAtomIndex).getDLPredicate() instanceof NodeIDsAscendingOrEqual) {
                Atom atom=getBodyAtom(bodyAtomIndex);
                int[] nodeIndexes=new int[atom.getArity()];
                for (int index=0;index<atom.getArity();index++) {
                    nodeIndexes[index]=m_variables.indexOf(atom.getArgumentVariable(index));
                    assert nodeIndexes[index]!=-1;
                }
                m_workers.add(new BranchIfNotNodeIDsAscendingOrEqual(lastAtomNextElement,m_valuesBufferManager.m_valuesBuffer,nodeIndexes));
                compileBodyAtom(bodyAtomIndex+1,lastAtomNextElement);
            }
            else {
                // Each atom is compiled into the following structure:
                //
                //              retrieval.open()
                // loopStart:   if (!retrieval.hasMore) goto afterLoop
                //              if (!retrieval.unboundVariableMatches) goto nextElement
                //              generate bindings - copy bindings from the retrieval to the values buffer
                //              copy the dependency set from the retrieval into the union dependency set
                //                  < the code for the next atom >
                // nextElement: retrieval.next
                //              goto loopStart
                // afterLoop:
                //
                // NodeIDLessEqualThan and NodeIDsAscendingOrEqual atoms are compiled such that they
                // immediately jump to the next element of the previous regular atom.

                int afterLoop=addLabel();
                int nextElement=addLabel();
                Atom atom=getBodyAtom(bodyAtomIndex);
                int[] bindingPositions=new int[atom.getArity()+1];
                bindingPositions[0]=m_valuesBufferManager.m_bodyDLPredicatesToIndexes.get(atom.getDLPredicate()).intValue();
                for (int argumentIndex=0;argumentIndex<atom.getArity();argumentIndex++) {
                    Term term=atom.getArgument(argumentIndex);
                    if (term instanceof Variable) {
                        if (m_boundSoFar.contains(term))
                            bindingPositions[argumentIndex+1]=m_variables.indexOf((Variable)term);
                        else
                            bindingPositions[argumentIndex+1]=-1;
                    }
                    else
                        bindingPositions[argumentIndex+1]=m_valuesBufferManager.m_bodyNonvariableTermsToIndexes.get(term).intValue();
                }
                ExtensionTable.Retrieval retrieval=m_extensionManager.getExtensionTable(atom.getArity()+1).createRetrieval(bindingPositions,m_valuesBufferManager.m_valuesBuffer,m_bufferSupply.getBuffer(atom.getArity()+1),false,ExtensionTable.View.EXTENSION_THIS);
                m_retrievals.add(retrieval);
                m_workers.add(new OpenRetrieval(retrieval));
                int loopStart=m_workers.size();
                m_workers.add(new HasMoreRetrieval(afterLoop,retrieval));
                compileCheckUnboundVariableMatches(atom,retrieval,nextElement);
                compileGenerateBindings(retrieval,atom);
                if (m_unionDependencySet!=null)
                    m_workers.add(new CopyDependencySet(retrieval,m_unionDependencySet.m_dependencySets,m_retrievals.size()-1));
                compileBodyAtom(bodyAtomIndex+1,nextElement);
                setLabelProgramCounter(nextElement);
                m_workers.add(new NextRetrieval(retrieval));
                m_workers.add(new JumpTo(loopStart));
                setLabelProgramCounter(afterLoop);
            }
        }
        protected final int getBodyLength() {
            return m_bodyAtoms.length;
        }
        protected final Atom getBodyAtom(int atomIndex) {
            return m_bodyAtoms[atomIndex];
        }
        protected final void compileCheckUnboundVariableMatches(Atom atom,ExtensionTable.Retrieval retrieval,int jumpIndex) {
            for (int outerArgumentIndex=0;outerArgumentIndex<atom.getArity();outerArgumentIndex++) {
                Variable variable=atom.getArgumentVariable(outerArgumentIndex);
                if (variable!=null && !m_boundSoFar.contains(variable)) {
                    for (int innerArgumentIndex=outerArgumentIndex+1;innerArgumentIndex<atom.getArity();innerArgumentIndex++) {
                        if (variable.equals(atom.getArgument(innerArgumentIndex)))
                            m_workers.add(new BranchIfNotEqual(jumpIndex,retrieval.getTupleBuffer(),outerArgumentIndex+1,innerArgumentIndex+1));
                    }
                }
            }
        }
        protected final void compileGenerateBindings(ExtensionTable.Retrieval retrieval,Atom atom) {
            for (int argumentIndex=0;argumentIndex<atom.getArity();argumentIndex++) {
                Variable variable=atom.getArgumentVariable(argumentIndex);
                if (variable!=null && !m_boundSoFar.contains(variable)) {
                    int variableIndex=m_variables.indexOf(variable);
                    if (variableIndex!=-1) {
                        m_workers.add(new CopyValues(retrieval.getTupleBuffer(),argumentIndex+1,m_valuesBufferManager.m_valuesBuffer,variableIndex));
                        m_boundSoFar.add(variable);
                    }
                }
            }
        }
        protected final int addLabel() {
            int labelIndex=m_labels.size();
            m_labels.add(null);
            return -labelIndex;
        }
        protected final void setLabelProgramCounter(int labelID) {
            m_labels.set(-labelID,Integer.valueOf(m_workers.size()));
        }
        protected abstract void compileHeads();
   }
}
