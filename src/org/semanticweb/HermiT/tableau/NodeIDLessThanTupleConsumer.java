package org.semanticweb.HermiT.tableau;

import java.io.Serializable;

public class NodeIDLessThanTupleConsumer implements TupleConsumer,Serializable {
    private static final long serialVersionUID=-7347034956107193899L;

    protected final TupleConsumer m_tupleConsumer;
    protected final int m_bindingIndexSmaller;
    protected final int m_bindingIndexLarger;
    
    public NodeIDLessThanTupleConsumer(TupleConsumer tupleConsumer,int bindingIndexSmaller,int bindingIndexLarger) {
        m_tupleConsumer=tupleConsumer;
        m_bindingIndexSmaller=bindingIndexSmaller;
        m_bindingIndexLarger=bindingIndexLarger;
    }
    public void consumeTuple(Object[] tuple,DependencySet[] dependencySets) {
        Node smaller=(Node)tuple[m_bindingIndexSmaller];
        Node larger=(Node)tuple[m_bindingIndexLarger];
        if (smaller.getNodeID()<larger.getNodeID())
            m_tupleConsumer.consumeTuple(tuple,dependencySets);
    }
}
