package org.semanticweb.HermiT.tableau;

import java.io.Serializable;

import org.semanticweb.HermiT.model.*;

public class HornHeadTupleConsumer implements TupleConsumer,Serializable {
    private static final long serialVersionUID=5970479851820607299L;

    protected final ExtensionManager m_extensionManager;
    protected final int[] m_argumentIndices;
    protected final Object[] m_tuple;

    public HornHeadTupleConsumer(Tableau tableau,DLPredicate dlPredicate,int[] argumentIndices) {
        m_extensionManager=tableau.getExtensionManager();
        m_argumentIndices=argumentIndices;
        m_tuple=new Object[m_argumentIndices.length+1];
        m_tuple[0]=dlPredicate;
    }
    public void consumeTuple(Object[] tuple,DependencySet dependencySet) {
        for (int argumentIndex=0;argumentIndex<m_argumentIndices.length;argumentIndex++)
            m_tuple[argumentIndex+1]=tuple[m_argumentIndices[argumentIndex]];
        m_extensionManager.addTuple(m_tuple,dependencySet);
    }
}
