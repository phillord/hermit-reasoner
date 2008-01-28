package org.semanticweb.HermiT.tableau;

import java.io.Serializable;

public class FirstAtomTupleConsumer implements TupleConsumer,Serializable {
    private static final long serialVersionUID=7438878326213648428L;

    protected final TupleConsumer m_tupleConsumer;
    protected final int[][] m_checkEqualInInput;
    protected final int[] m_outputTupleCopy;
    protected final Object[] m_inputTuple;
    protected final Object[] m_outputTuple;
    
    public FirstAtomTupleConsumer(int inputArity,TupleConsumer tupleConsumer,int[][] checkEqualInInput,int[] outputTupleCopy) {
        m_tupleConsumer=tupleConsumer;
        m_checkEqualInInput=checkEqualInInput;
        m_outputTupleCopy=outputTupleCopy;
        m_inputTuple=new Object[inputArity];
        m_outputTuple=new Object[m_outputTupleCopy.length];
    }
    public void consumeTuple(Object[] tuple,DependencySet dependencySet) {
        System.arraycopy(tuple,0,m_inputTuple,0,tuple.length);
        for (int setIndex=m_checkEqualInInput.length-1;setIndex>=0;--setIndex) {
            int[] checkEqualUnboundInRetrieval=m_checkEqualInInput[setIndex];
            if (!tuple[checkEqualUnboundInRetrieval[0]].equals(tuple[checkEqualUnboundInRetrieval[1]]))
                return;
        }
        for (int index=m_outputTupleCopy.length-1;index>=0;--index)
            m_outputTuple[index]=tuple[m_outputTupleCopy[index]];
        m_tupleConsumer.consumeTuple(m_outputTuple,dependencySet);
    }
}
