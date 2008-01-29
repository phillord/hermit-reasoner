package org.semanticweb.HermiT.tableau;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

import org.semanticweb.HermiT.model.*;
import org.semanticweb.HermiT.monitor.*;

public class DLClauseEvaluator implements Serializable {
    private static final long serialVersionUID=4639844159658590456L;
    protected static final String CRLF=System.getProperty("line.separator");

    protected final ExtensionManager m_extensionManager;
    protected final Object[] m_valuesBuffer;
    protected final UnionDependencySet m_unionDependencySet;
    protected final ExtensionTable.Retrieval[] m_retrievals;
    protected final Worker[] m_workers;
    
    public DLClauseEvaluator(ExtensionManager extensionManager,DLClause dlClause,ExtensionTable.Retrieval firstAtomRetrieval) {
        m_extensionManager=extensionManager;
        Compiler compiler=new Compiler(this,m_extensionManager,dlClause,firstAtomRetrieval);
        m_valuesBuffer=compiler.m_valuesBuffer;
        m_unionDependencySet=compiler.m_unionDependencySet;
        m_retrievals=new ExtensionTable.Retrieval[compiler.m_retrievals.size()];
        compiler.m_retrievals.toArray(m_retrievals);
        m_workers=new Worker[compiler.m_workers.size()];
        compiler.m_workers.toArray(m_workers);
    }
    public void applyDLClause() {
        int programCounter=0;
        while (programCounter<m_workers.length && !m_extensionManager.containsClash())
            programCounter=m_workers[programCounter].execute(programCounter);
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

    protected static interface Worker {
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
    
    protected static final class BranchIfNotNodeIDLessThan implements BranchingWorker,Serializable {
        private static final long serialVersionUID=2484359261424674914L;

        protected int m_notLessProgramCounter;
        protected final Object[] m_buffer;
        protected final int m_index1;
        protected final int m_index2;
        
        public BranchIfNotNodeIDLessThan(int notLessProgramCounter,Object[] buffer,int index1,int index2) {
            m_notLessProgramCounter=notLessProgramCounter;
            m_buffer=buffer;
            m_index1=index1;
            m_index2=index2;
        }
        public int execute(int programCounter) {
            if (((Node)m_buffer[m_index1]).getNodeID()<((Node)m_buffer[m_index2]).getNodeID())
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
            return "Branch to "+m_notLessProgramCounter+" if "+m_index1+".ID >= "+m_index2+".ID";
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
        
        public CallMatchStartedOnMonitor(TableauMonitor tableauMonitor,DLClauseEvaluator dlClauseEvaluator) {
            m_tableauMonitor=tableauMonitor;
            m_dlClauseEvaluator=dlClauseEvaluator;
        }
        public int execute(int programCounter) {
            m_tableauMonitor.dlClauseMatchedStarted(m_dlClauseEvaluator);
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
        
        public CallMatchFinishedOnMonitor(TableauMonitor tableauMonitor,DLClauseEvaluator dlClauseEvaluator) {
            m_tableauMonitor=tableauMonitor;
            m_dlClauseEvaluator=dlClauseEvaluator;
        }
        public int execute(int programCounter) {
            m_tableauMonitor.dlClauseMatchedFinished(m_dlClauseEvaluator);
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
        protected final DependencySet m_dependencySet;
        protected final DLPredicate m_dlPredicate;
        protected final int m_argumentIndex;

        public DeriveUnaryFact(ExtensionManager extensionManager,Object[] valuesBuffer,DependencySet dependencySet,DLPredicate dlPredicate,int argumentIndex) {
            m_extensionManager=extensionManager;
            m_valuesBuffer=valuesBuffer;
            m_dependencySet=dependencySet;
            m_argumentIndex=argumentIndex;
            m_dlPredicate=dlPredicate;
        }
        public int execute(int programCounter) {
            Node argument=(Node)m_valuesBuffer[m_argumentIndex];
            m_extensionManager.addAssertion(m_dlPredicate,argument,m_dependencySet);
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
            m_extensionManager.addAssertion(m_dlPredicate,argument1,argument2,m_dependencySet);
            return programCounter+1;
        }
        public String toString() {
            return "Derive binary fact";
        }
    }
    
    protected static final class DeriveDisjunction implements Worker,Serializable {
        private static final long serialVersionUID=-3546622575743138887L;

        protected final Tableau m_tableau;
        protected final Object[] m_valuesBuffer;
        protected final DependencySet m_dependencySet;
        protected final DLPredicate[] m_headDLPredicates;
        protected final int[] m_disjunctStart;
        protected final int[] m_copyValuesToArguments;
        
        public DeriveDisjunction(Object[] valuesBuffer,DependencySet dependencySet,Tableau tableau,DLPredicate[] headDLPredicates,int[] copyValuesToArguments) {
            m_valuesBuffer=valuesBuffer;
            m_dependencySet=dependencySet;
            m_tableau=tableau;
            m_headDLPredicates=headDLPredicates;
            m_disjunctStart=new int[m_headDLPredicates.length];
            int argumentsSize=0;
            for (int disjunctIndex=0;disjunctIndex<m_headDLPredicates.length;disjunctIndex++) {
                m_disjunctStart[disjunctIndex]=argumentsSize;
                argumentsSize+=m_headDLPredicates[disjunctIndex].getArity();
            }
            m_copyValuesToArguments=copyValuesToArguments;
        }
        public int execute(int programCounter) {
            Node[] arguments=new Node[m_copyValuesToArguments.length];
            for (int argumentIndex=m_copyValuesToArguments.length-1;argumentIndex>=0;--argumentIndex)
                arguments[argumentIndex]=(Node)m_valuesBuffer[m_copyValuesToArguments[argumentIndex]];
            GroundDisjunction groundDisjunction=new GroundDisjunction(m_tableau,m_headDLPredicates,m_disjunctStart,arguments,m_tableau.m_dependencySetFactory.getPermanent(m_dependencySet));
            if (!groundDisjunction.isSatisfied(m_tableau))
                m_tableau.addGroundDisjunction(groundDisjunction);
            return programCounter+1;
        }
        public String toString() {
            return "Derive disjunction";
        }
    }
    
    protected static class Compiler {
        protected final DLClauseEvaluator m_dlClauseEvalautor;
        protected final ExtensionManager m_extensionManager;
        protected final DLClause m_dlClause;
        protected final List<Variable> m_variables;
        protected final Set<Variable> m_boundSoFar;
        protected final Object[] m_valuesBuffer;
        protected final UnionDependencySet m_unionDependencySet;
        protected final List<ExtensionTable.Retrieval> m_retrievals;
        protected final List<Worker> m_workers;
        protected final List<Integer> m_labels;

        public Compiler(DLClauseEvaluator dlClauseEvalautor,ExtensionManager extensionManager,DLClause dlClause,ExtensionTable.Retrieval firstAtomRetrieval) {
            m_dlClauseEvalautor=dlClauseEvalautor;
            m_extensionManager=extensionManager;
            m_dlClause=dlClause;
            m_variables=new ArrayList<Variable>();
            int numberOfRealAtoms=0;
            for (int bodyIndex=0;bodyIndex<dlClause.getBodyLength();bodyIndex++) {
                Atom atom=dlClause.getBodyAtom(bodyIndex);
                for (int argumentIndex=0;argumentIndex<atom.getArity();argumentIndex++) {
                    Variable variable=atom.getArgumentVariable(argumentIndex);
                    if (variable!=null && !m_variables.contains(variable) && occursInAtomsAfter(variable,dlClause,bodyIndex+1))
                        m_variables.add(variable);
                }
                if (!atom.getDLPredicate().equals(NodeIDLessThan.INSTANCE))
                    numberOfRealAtoms++;
            }
            for (int disjunctionIndex=0;disjunctionIndex<m_dlClause.getHeadLength();disjunctionIndex++) {
                for (int conjunctIndex=0;conjunctIndex<m_dlClause.getHeadConjunctionLength(disjunctionIndex);conjunctIndex++) {
                    Atom atom=m_dlClause.getHeadAtom(disjunctionIndex,conjunctIndex);
                    for (int argumentIndex=0;argumentIndex<atom.getArity();argumentIndex++) {
                        Variable variable=atom.getArgumentVariable(argumentIndex);
                        if (variable!=null && !m_variables.contains(variable))
                            m_variables.add(variable);
                    }
                }
            }
            m_boundSoFar=new HashSet<Variable>();
            m_valuesBuffer=new Object[m_variables.size()+dlClause.getBodyLength()];
            for (int bodyIndex=0;bodyIndex<dlClause.getBodyLength();bodyIndex++)
                m_valuesBuffer[m_variables.size()+bodyIndex]=m_dlClause.getBodyAtom(bodyIndex).getDLPredicate();
            m_unionDependencySet=new UnionDependencySet(numberOfRealAtoms);
            m_retrievals=new ArrayList<ExtensionTable.Retrieval>();
            m_workers=new ArrayList<Worker>();
            m_labels=new ArrayList<Integer>();
            m_labels.add(null);
            m_retrievals.add(firstAtomRetrieval);
            int afterRule=addLabel();
            compileCheckUnboundVariableMatches(dlClause.getBodyAtom(0),firstAtomRetrieval,afterRule);
            compileGenerateBindings(firstAtomRetrieval,dlClause.getBodyAtom(0));
            m_workers.add(new CopyDependencySet(firstAtomRetrieval,m_unionDependencySet.getConstituents(),0));
            compileBodyAtom(1,afterRule);
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
        protected boolean occursInAtomsAfter(Variable variable,DLClause dlClause,int startIndex) {
            for (int argumentIndex=startIndex;argumentIndex<dlClause.getBodyLength();argumentIndex++)
                if (dlClause.getBodyAtom(argumentIndex).containsVariable(variable))
                    return true;
            return false;
        }
        protected void compileBodyAtom(int bodyAtomIndex,int lastAtomNextElement) {
            if (bodyAtomIndex==m_dlClause.getBodyLength())
                compileHead();
            else if (m_dlClause.getBodyAtom(bodyAtomIndex).getDLPredicate().equals(NodeIDLessThan.INSTANCE)) {
                Atom atom=m_dlClause.getBodyAtom(bodyAtomIndex);
                int variable1Index=m_variables.indexOf(atom.getArgumentVariable(0));
                int variable2Index=m_variables.indexOf(atom.getArgumentVariable(1));
                assert variable1Index!=-1;
                assert variable2Index!=-1;
                m_workers.add(new BranchIfNotNodeIDLessThan(lastAtomNextElement,m_valuesBuffer,variable1Index,variable2Index));
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
                // NodeIDLessThan atoms are compiled in a way that makes them immediately jump to the next element of the previous regular atom.
                
                int afterLoop=addLabel();
                int nextElement=addLabel();
                Atom atom=m_dlClause.getBodyAtom(bodyAtomIndex);
                int[] bindingPositions=new int[atom.getArity()+1];
                bindingPositions[0]=m_variables.size()+bodyAtomIndex;
                for (int argumentIndex=0;argumentIndex<atom.getArity();argumentIndex++) {
                    Variable variable=atom.getArgumentVariable(argumentIndex);
                    if (variable!=null && m_boundSoFar.contains(variable))
                        bindingPositions[argumentIndex+1]=m_variables.indexOf(variable);
                    else
                        bindingPositions[argumentIndex+1]=-1;
                }
                ExtensionTable.Retrieval retrieval=m_extensionManager.getExtensionTable(atom.getArity()+1).createRetrieval(bindingPositions,m_valuesBuffer,ExtensionTable.View.EXTENSION_THIS);
                m_retrievals.add(retrieval);
                m_workers.add(new OpenRetrieval(retrieval));
                int loopStart=m_workers.size();
                m_workers.add(new HasMoreRetrieval(afterLoop,retrieval));
                compileCheckUnboundVariableMatches(atom,retrieval,nextElement);
                compileGenerateBindings(retrieval,atom);
                m_workers.add(new CopyDependencySet(retrieval,m_unionDependencySet.getConstituents(),m_retrievals.size()-1));
                compileBodyAtom(bodyAtomIndex+1,nextElement);
                setLabelProgramCounter(nextElement);
                m_workers.add(new NextRetrieval(retrieval));
                m_workers.add(new JumpTo(loopStart));
                setLabelProgramCounter(afterLoop);
            }
        }
        protected void compileHead() {
            if (m_extensionManager.m_tableauMonitor!=null)
                m_workers.add(new CallMatchStartedOnMonitor(m_extensionManager.m_tableauMonitor,m_dlClauseEvalautor));
            if (m_dlClause.getHeadLength()==0)
                m_workers.add(new SetClash(m_extensionManager,m_unionDependencySet));
            else if (m_dlClause.getHeadLength()==1) {
                for (int conjunctIndex=0;conjunctIndex<m_dlClause.getHeadConjunctionLength(0);conjunctIndex++) {
                    Atom atom=m_dlClause.getHeadAtom(0,conjunctIndex);
                    switch (atom.getArity()) {
                    case 1:
                        m_workers.add(new DeriveUnaryFact(m_extensionManager,m_valuesBuffer,m_unionDependencySet,atom.getDLPredicate(),m_variables.indexOf(atom.getArgumentVariable(0))));
                        break;
                    case 2:
                        m_workers.add(new DeriveBinaryFact(m_extensionManager,m_valuesBuffer,m_unionDependencySet,atom.getDLPredicate(),m_variables.indexOf(atom.getArgumentVariable(0)),m_variables.indexOf(atom.getArgumentVariable(1))));
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported atom arity.");
                    }
                }
            }
            else {
                int totalNumberOfArguments=0;
                for (int disjunctionIndex=0;disjunctionIndex<m_dlClause.getHeadLength();disjunctionIndex++) {
                    assert m_dlClause.getHeadConjunctionLength(disjunctionIndex)==1;
                    totalNumberOfArguments+=m_dlClause.getHeadAtom(disjunctionIndex,0).getArity();
                }
                DLPredicate[] headDLPredicates=new DLPredicate[m_dlClause.getHeadLength()];
                int[] copyValuesToArguments=new int[totalNumberOfArguments];
                int index=0;
                for (int disjunctionIndex=0;disjunctionIndex<m_dlClause.getHeadLength();disjunctionIndex++) {
                    Atom atom=m_dlClause.getHeadAtom(disjunctionIndex,0);
                    headDLPredicates[disjunctionIndex]=atom.getDLPredicate();
                    for (int argumentIndex=0;argumentIndex<atom.getArity();argumentIndex++) {
                        Variable variable=atom.getArgumentVariable(argumentIndex);
                        int variableIndex=m_variables.indexOf(variable);
                        assert variableIndex!=-1;
                        copyValuesToArguments[index++]=variableIndex;
                    }
                }
                m_workers.add(new DeriveDisjunction(m_valuesBuffer,m_unionDependencySet,m_extensionManager.m_tableau,headDLPredicates,copyValuesToArguments));
            }
            if (m_extensionManager.m_tableauMonitor!=null)
                m_workers.add(new CallMatchFinishedOnMonitor(m_extensionManager.m_tableauMonitor,m_dlClauseEvalautor));
        }
        protected void compileCheckUnboundVariableMatches(Atom atom,ExtensionTable.Retrieval retrieval,int jumpIndex) {
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
        protected void compileGenerateBindings(ExtensionTable.Retrieval retrieval,Atom atom) {
            for (int argumentIndex=0;argumentIndex<atom.getArity();argumentIndex++) {
                Variable variable=atom.getArgumentVariable(argumentIndex);
                if (variable!=null && !m_boundSoFar.contains(variable)) {
                    int variableIndex=m_variables.indexOf(variable);
                    if (variableIndex!=-1) {
                        m_workers.add(new CopyValues(retrieval.getTupleBuffer(),argumentIndex+1,m_valuesBuffer,variableIndex));
                        m_boundSoFar.add(variable);
                    }
                }
            }
        }
        protected int addLabel() {
            int labelIndex=m_labels.size();
            m_labels.add(null);
            return -labelIndex;
        }
        protected void setLabelProgramCounter(int labelID) {
            m_labels.set(-labelID,Integer.valueOf(m_workers.size()));
        }
    }
}
