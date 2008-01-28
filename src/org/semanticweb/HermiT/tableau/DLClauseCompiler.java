package org.semanticweb.HermiT.tableau;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.io.Serializable;

import org.semanticweb.HermiT.model.*;
import org.semanticweb.HermiT.monitor.*;

public class DLClauseCompiler {
    protected static final int[][] NO_INDEXES=new int[0][];
    
    protected final Tableau m_tableau;
    
    public DLClauseCompiler(Tableau tableau) {
        m_tableau=tableau;
    }
    public TupleConsumer compile(DLClause dlClause,ExtensionTable.View[] extensionViews) {
        // Compute the variables that are produced by this DL-clause
        List<Variable> tupleLayout=new ArrayList<Variable>();
        for (int disjunctIndex=0;disjunctIndex<dlClause.getHeadLength();disjunctIndex++) {
            int headLength=dlClause.getHeadConjunctionLength(disjunctIndex);
            for (int conjunctionIndex=0;conjunctionIndex<headLength;conjunctionIndex++) {
                Atom atom=dlClause.getHeadAtom(disjunctIndex,conjunctionIndex);
                for (int argumentIndex=0;argumentIndex<atom.getArity();argumentIndex++) {
                    Variable variable=atom.getArgumentVariable(argumentIndex);
                    if (variable!=null) {
                        if (!tupleLayout.contains(variable))
                            tupleLayout.add(variable);
                    }
                    else
                        throw new IllegalArgumentException("Constants in DL-clauses are not supported.");
                }
            }
        }
        // Start creating the tuple consumers
        List<TupleConsumer> headTupleConsumers=new ArrayList<TupleConsumer>();
        // Create the tuple consumer for the head
        if (dlClause.getHeadLength()==0)
            headTupleConsumers.add(new EmptyHeadTupleConsumer(m_tableau));
        else if (dlClause.getHeadLength()==1) {
            Atom[] headConjuncts=dlClause.getHeadConjunction(0);
            for (int conjunctIndex=0;conjunctIndex<headConjuncts.length;conjunctIndex++) {
                Atom headAtom=headConjuncts[conjunctIndex];
                DLPredicate dlPredicate=headAtom.getDLPredicate();
                int[] copyTupleToHead=getCopyTupleToHeadArray(headAtom,tupleLayout);
                headTupleConsumers.add(new HornHeadTupleConsumer(m_tableau,dlPredicate,copyTupleToHead));
            }
        }
        else {
            DLPredicate[] dlPredicates=new DLPredicate[dlClause.getHeadLength()];
            int[][] copyTupleToHeads=new int[dlClause.getHeadLength()][];
            for (int disjunctionIndex=0;disjunctionIndex<dlClause.getHeadLength();disjunctionIndex++) {
                if (dlClause.getHeadConjunctionLength(disjunctionIndex)>1)
                    throw new IllegalStateException("Disjunctions of conjunctions are not supported yet.");
                Atom headAtom=dlClause.getHeadAtom(disjunctionIndex,0);
                dlPredicates[disjunctionIndex]=headAtom.getDLPredicate();
                copyTupleToHeads[disjunctionIndex]=getCopyTupleToHeadArray(headAtom,tupleLayout);
            }
            headTupleConsumers.add(new DisjunctiveHeadTupleConsumer(m_tableau,dlPredicates,copyTupleToHeads));
        }
        // Construct the tuple consumer
        TupleConsumer tupleConsumer;
        if (headTupleConsumers.size()==1)
            tupleConsumer=headTupleConsumers.get(0);
        else {
            TupleConsumer[] headTupleConsumersArray=new TupleConsumer[headTupleConsumers.size()];
            headTupleConsumers.toArray(headTupleConsumersArray);
            tupleConsumer=new ForkingTupleConsumer(headTupleConsumersArray);
        }
        // For each literal, compute the set of variables that precede it
        List<Set<Variable>> variablesBeforeAtom=new ArrayList<Set<Variable>>(dlClause.getBodyLength());
        variablesBeforeAtom.add(new HashSet<Variable>());
        for (int bodyIndex=0;bodyIndex<dlClause.getBodyLength();bodyIndex++) {
            Set<Variable> variables=new HashSet<Variable>(variablesBeforeAtom.get(bodyIndex));
            variables.addAll(variablesBeforeAtom.get(bodyIndex));
            Atom atom=dlClause.getBodyAtom(bodyIndex);
            for (int argumentIndex=0;argumentIndex<atom.getArity();argumentIndex++) {
                Variable variable=atom.getArgumentVariable(argumentIndex);
                if (variable!=null)
                    variables.add(variable);
                else
                    throw new IllegalArgumentException("Constants in DL-clauses are not supported.");
            }
            variablesBeforeAtom.add(variables);
        }
        // Now finally compile the DL-clause
        Object[][] matchedTuples=new Object[dlClause.getBodyLength()][];
        if (m_tableau.m_tableauMonitor!=null)
            tupleConsumer=new NotifyMatchTupleConsumer(m_tableau.m_tableauMonitor,tupleConsumer,dlClause,matchedTuples);
        List<Variable> tupleLayoutBeforeAtom=new ArrayList<Variable>();
        for (int bodyIndex=dlClause.getBodyLength()-1;bodyIndex>=1;--bodyIndex) {
            tupleLayoutBeforeAtom.clear();
            tupleLayoutBeforeAtom.addAll(tupleLayout);
            Atom bodyAtom=dlClause.getBodyAtom(bodyIndex);
            DLPredicate bodyAtomDLPredicate=bodyAtom.getDLPredicate();
            // Add all variables from the atom to the tuple before
            for (int argumentIndex=0;argumentIndex<bodyAtom.getArity();argumentIndex++) {
                Variable variable=bodyAtom.getArgumentVariable(argumentIndex);
                if (!tupleLayoutBeforeAtom.contains(variable))
                    tupleLayoutBeforeAtom.add(variable);
            }
            // Now remove all variables from the tuple before if they don't occur before the atom
            Set<Variable> beforeAtom=variablesBeforeAtom.get(bodyIndex);
            for (int variableIndex=tupleLayoutBeforeAtom.size()-1;variableIndex>=0;--variableIndex) {
                Variable variable=tupleLayoutBeforeAtom.get(variableIndex);
                if (!beforeAtom.contains(variable))
                    tupleLayoutBeforeAtom.remove(variableIndex);
            }
            // Tuples before and after are now ready -- we can compile the DL-clause.
            if (NodeIDLessThan.INSTANCE.equals(bodyAtomDLPredicate)) {
                assert tupleLayout.equals(tupleLayoutBeforeAtom) : "Internal error: NodeIDLessThan atom was cmopiled wrongly.";
                int bindingIndexSmaller=tupleLayoutBeforeAtom.indexOf(bodyAtom.getArgumentVariable(0));
                int bindingIndexLarger=tupleLayoutBeforeAtom.indexOf(bodyAtom.getArgumentVariable(1));
                tupleConsumer=new NodeIDLessThanTupleConsumer(tupleConsumer,bindingIndexSmaller,bindingIndexLarger);
            }
            else {
                // We first determine several counts:
                // - how many positions are bound in the current atom
                // - how many pairs of occurrences of some unbound variable there are 
                // We also compute the binding pattern. Remember that the binding pattern contains the predicate in the first position!
                int numberOfBoundPositions=0;
                int numberOfOccurrencesOfUnboundVariables=0;
                boolean[] bindingPattern=new boolean[bodyAtom.getArity()+1];
                bindingPattern[0]=true;
                for (int argumentIndex=0;argumentIndex<bodyAtom.getArity();argumentIndex++) {
                    Variable variable=bodyAtom.getArgumentVariable(argumentIndex);
                    if (tupleLayoutBeforeAtom.contains(variable)) {
                        bindingPattern[argumentIndex+1]=true;
                        numberOfBoundPositions++;
                    }
                    else {
                        bindingPattern[argumentIndex+1]=false;
                        if (getPreviousOccurrenceIndex(bodyAtom,variable,argumentIndex)!=-1)
                            numberOfOccurrencesOfUnboundVariables++;
                    }
                }
                // We now determine how many variables are copied from the input to output
                int numberOfInputToOutputCopyings=0;
                for (int variableIndex=tupleLayout.size()-1;variableIndex>=0;--variableIndex) {
                    Variable variable=tupleLayout.get(variableIndex);
                    if (tupleLayoutBeforeAtom.contains(variable))
                        numberOfInputToOutputCopyings++;
                }
                // We now generate the pairs of indices for copying input to output
                int number1=0;
                int[][] copyInputToOutput=allocateIndexArray(numberOfInputToOutputCopyings,2);
                for (int variableIndex=tupleLayout.size()-1;variableIndex>=0;--variableIndex) {
                    Variable variable=tupleLayout.get(variableIndex);
                    int beforeIndex=tupleLayoutBeforeAtom.indexOf(variable);
                    if (beforeIndex!=-1) {
                        copyInputToOutput[number1][0]=beforeIndex;
                        copyInputToOutput[number1][1]=variableIndex;
                        number1++;
                    }
                }
                // We now generate all the remaining indices
                int[][] copyInputToBindings=allocateIndexArray(numberOfBoundPositions,2);
                int[][] checkEqualUnboundInRetrieval=allocateIndexArray(numberOfOccurrencesOfUnboundVariables,2);
                int[][] copyRetrievedToOutput=allocateIndexArray(tupleLayout.size()-numberOfInputToOutputCopyings,2);
                number1=0;
                int number2=0;
                int number3=0;
                for (int argumentIndex=0;argumentIndex<bodyAtom.getArity();argumentIndex++) {
                    Variable variable=bodyAtom.getArgumentVariable(argumentIndex);
                    if (bindingPattern[argumentIndex+1]) {
                        copyInputToBindings[number1][0]=tupleLayoutBeforeAtom.indexOf(variable);
                        copyInputToBindings[number1][1]=argumentIndex+1;
                        number1++;
                    }
                    else {
                        int previousOccurrenceIndex=getPreviousOccurrenceIndex(bodyAtom,variable,argumentIndex);
                        if (previousOccurrenceIndex==-1) {
                            int variableInOutputIndex=tupleLayout.indexOf(variable);
                            if (variableInOutputIndex!=-1) {
                                copyRetrievedToOutput[number2][0]=argumentIndex+1;
                                copyRetrievedToOutput[number2][1]=variableInOutputIndex;
                                number2++;
                            }
                        }
                        else {
                            checkEqualUnboundInRetrieval[number3][0]=argumentIndex+1;
                            checkEqualUnboundInRetrieval[number3][1]=previousOccurrenceIndex+1;
                            number3++;
                        }
                    }
                }
                // Generate the tuple consumer
                ExtensionTable extensionTable=m_tableau.getExtensionManager().getExtensionTable(bodyAtom.getArity()+1);
                ExtensionTable.Retrieval extensionTableRetrieval=extensionTable.createRetrieval(bindingPattern,extensionViews[bodyIndex]);
                extensionTableRetrieval.getBindingsBuffer()[0]=bodyAtomDLPredicate;
                tupleConsumer=new NestedLoopTupleConsumer(m_tableau,tupleConsumer,extensionTableRetrieval,copyInputToOutput,copyInputToBindings,checkEqualUnboundInRetrieval,copyRetrievedToOutput);
                matchedTuples[bodyIndex]=extensionTableRetrieval.getTupleBuffer();
            }
            // We now swap the tuple arrays and go to the previous atom
            List<Variable> temp=tupleLayout;
            tupleLayout=tupleLayoutBeforeAtom;
            tupleLayoutBeforeAtom=temp;
        }
        // We now compile the first atom
        int[] outputTupleCopy=new int[tupleLayout.size()];
        Atom firstBodyAtom=dlClause.getBodyAtom(0);
        int numberOfOccurrencesOfEqualsVariables=0;
        for (int argumentIndex=0;argumentIndex<firstBodyAtom.getArity();argumentIndex++) {
            Variable variable=firstBodyAtom.getArgumentVariable(argumentIndex);
            if (getPreviousOccurrenceIndex(firstBodyAtom,variable,argumentIndex)!=-1)
                numberOfOccurrencesOfEqualsVariables++;
        }
        int[][] checkEqualInInput=allocateIndexArray(numberOfOccurrencesOfEqualsVariables,2);
        int number=0;
        for (int argumentIndex=0;argumentIndex<firstBodyAtom.getArity();argumentIndex++) {
            Variable variable=firstBodyAtom.getArgumentVariable(argumentIndex);
            int previousOccurrenceIndex=getPreviousOccurrenceIndex(firstBodyAtom,variable,argumentIndex);
            if (previousOccurrenceIndex!=-1) {
                checkEqualInInput[number][0]=argumentIndex+1;
                checkEqualInInput[number][1]=previousOccurrenceIndex+1;
                number++;
            }
        }
        for (int index=0;index<tupleLayout.size();index++)
            outputTupleCopy[index]=getPreviousOccurrenceIndex(firstBodyAtom,tupleLayout.get(index),firstBodyAtom.getArity())+1;
        tupleConsumer=new FirstAtomTupleConsumer(firstBodyAtom.getArity()+1,tupleConsumer,checkEqualInInput,outputTupleCopy);
        matchedTuples[0]=((FirstAtomTupleConsumer)tupleConsumer).m_inputTuple;
        return tupleConsumer;
    }
    protected int[] getCopyTupleToHeadArray(Atom atom,List<Variable> tupleLayout) {
        int[] result=new int[atom.getArity()];
        for (int argumentIndex=0;argumentIndex<atom.getArity();argumentIndex++) {
            Variable variable=atom.getArgumentVariable(argumentIndex);
            result[argumentIndex]=tupleLayout.indexOf(variable);
        }
        return result;
    }
    protected int getPreviousOccurrenceIndex(Atom atom,Variable variable,int before) {
        for (int index=before-1;index>=0;--index)
            if (variable.equals(atom.getArgumentVariable(index)))
                return index;
        return -1;
    }
    protected int[][] allocateIndexArray(int rows,int columns) {
        if (rows==0)
            return NO_INDEXES;
        else
            return new int[rows][columns];
    }
    
    protected static class NotifyMatchTupleConsumer implements TupleConsumer,Serializable {
        private static final long serialVersionUID=1766228363524694034L;

        protected final TableauMonitor m_tableauMonitor;
        protected final TupleConsumer m_headConsumer;
        protected final DLClause m_dlClause;
        protected final Object[][] m_matchedTuples;

        public NotifyMatchTupleConsumer(TableauMonitor tableauMonitor,TupleConsumer headConsumer,DLClause dlClause,Object[][] matchedTuples) {
            m_tableauMonitor=tableauMonitor;
            m_headConsumer=headConsumer;
            m_dlClause=dlClause;
            m_matchedTuples=matchedTuples;
        }
        public void consumeTuple(Object[] tuple,DependencySet dependencySet) {
            m_tableauMonitor.dlClauseMatchedStarted(m_dlClause,m_matchedTuples);
            m_headConsumer.consumeTuple(tuple,dependencySet);
            m_tableauMonitor.dlClauseMatchedFinished(m_dlClause,m_matchedTuples);
        }
    }
}
