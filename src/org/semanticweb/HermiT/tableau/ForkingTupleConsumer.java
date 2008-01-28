package org.semanticweb.HermiT.tableau;

import java.io.Serializable;

public class ForkingTupleConsumer implements TupleConsumer,Serializable {
    private static final long serialVersionUID=839739846607677203L;

    protected final TupleConsumer[] m_tupleConsumers;
    
    public ForkingTupleConsumer(TupleConsumer[] tupleConsumers) {
        m_tupleConsumers=tupleConsumers;
    }
    public void consumeTuple(Object[] tuple,DependencySet dependencySet) {
        for (int tupleConsumerIndex=m_tupleConsumers.length-1;tupleConsumerIndex>=0;--tupleConsumerIndex)
            m_tupleConsumers[tupleConsumerIndex].consumeTuple(tuple,dependencySet);
    }
}
