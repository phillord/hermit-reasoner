package org.semanticweb.HermiT.tableau;

import java.io.Serializable;

public class NestedLoopTupleConsumer implements TupleConsumer,Serializable {
    private static final long serialVersionUID=6165049562696864961L;

    protected final Tableau m_tableau;
    protected final TupleConsumer m_tupleConsumer;
    protected final ExtensionTable.Retrieval m_extensionTableRetrieval;
    protected final int[][] m_copyInputToOutput;
    protected final int[][] m_copyInputToBindings;
    protected final int[][] m_checkEqualUnboundInRetrieval;
    protected final int[][] m_copyRetrievedToOutput;
    protected final int m_dependencySetIndex;
    protected final Object[] m_outputTuple;
    
    public NestedLoopTupleConsumer(Tableau tableau,TupleConsumer tupleConsumer,ExtensionTable.Retrieval extensionTableRetrieval,int[][] copyInputToOutput,int[][] copyInputToBindings,int[][] checkEqualUnboundInRetrieval,int[][] copyRetrievedToOutput,int dependencySetIndex) {
        m_tableau=tableau;
        m_tupleConsumer=tupleConsumer;
        m_extensionTableRetrieval=extensionTableRetrieval;
        m_copyInputToOutput=copyInputToOutput;
        m_copyInputToBindings=copyInputToBindings;
        m_checkEqualUnboundInRetrieval=checkEqualUnboundInRetrieval;
        m_copyRetrievedToOutput=copyRetrievedToOutput;
        m_dependencySetIndex=dependencySetIndex;
        m_outputTuple=new Object[m_copyInputToOutput.length+m_copyRetrievedToOutput.length];
    }
    public void consumeTuple(Object[] tuple,DependencySet[] dependencySets) {
        for (int index=m_copyInputToOutput.length-1;index>=0;--index)
            m_outputTuple[m_copyInputToOutput[index][1]]=tuple[m_copyInputToOutput[index][0]];
        Object[] bindingsBuffer=m_extensionTableRetrieval.getBindingsBuffer();
        for (int index=m_copyInputToBindings.length-1;index>=0;--index)
            bindingsBuffer[m_copyInputToBindings[index][1]]=tuple[m_copyInputToBindings[index][0]];
        m_extensionTableRetrieval.open();
        Object[] tupleBuffer=m_extensionTableRetrieval.getTupleBuffer();
        while (!m_extensionTableRetrieval.afterLast() && !m_tableau.getExtensionManager().containsClash()) {
            boolean tupleCorrect=true;
            for (int setIndex=m_checkEqualUnboundInRetrieval.length-1;setIndex>=0;--setIndex) {
                int[] checkEqualUnboundInRetrieval=m_checkEqualUnboundInRetrieval[setIndex];
                if (!tupleBuffer[checkEqualUnboundInRetrieval[0]].equals(tupleBuffer[checkEqualUnboundInRetrieval[1]])) {
                    tupleCorrect=false;
                    break;
                }
            }
            if (tupleCorrect) {
                for (int index=m_copyRetrievedToOutput.length-1;index>=0;--index)
                    m_outputTuple[m_copyRetrievedToOutput[index][1]]=tupleBuffer[m_copyRetrievedToOutput[index][0]];
                dependencySets[m_dependencySetIndex]=m_extensionTableRetrieval.getDependencySet();
                m_tupleConsumer.consumeTuple(m_outputTuple,dependencySets);
                if (m_copyRetrievedToOutput.length==0)
                    break;
            }
            m_extensionTableRetrieval.next();
        }
    }
}
