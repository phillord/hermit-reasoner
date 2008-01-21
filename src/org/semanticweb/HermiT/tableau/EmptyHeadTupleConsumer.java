package org.semanticweb.HermiT.tableau;

import java.io.Serializable;

public class EmptyHeadTupleConsumer implements TupleConsumer,Serializable {
    private static final long serialVersionUID=6143804526909213698L;

    protected static final Object[] EMPTY_TUPLE=new Object[0];

    protected final ExtensionManager m_extensionManager;

    public EmptyHeadTupleConsumer(Tableau tableau) {
        m_extensionManager=tableau.getExtensionManager();
    }
    public void consumeTuple(Object[] tuple,DependencySet dependencySet) {
        m_extensionManager.addTuple(EMPTY_TUPLE,dependencySet);
    }
}
