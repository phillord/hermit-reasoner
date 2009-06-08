// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.model;

import org.semanticweb.HermiT.Prefixes;

public class ExistsDescriptionGraph extends ExistentialConcept implements DLPredicate {
    private static final long serialVersionUID=7433430510725260994L;

    protected final DescriptionGraph m_descriptionGraph;
    protected final int m_vertex;
    
    protected ExistsDescriptionGraph(DescriptionGraph descriptionGraph,int vertex) {
        m_descriptionGraph=descriptionGraph;
        m_vertex=vertex;
    }
    public DescriptionGraph getDescriptionGraph() {
        return m_descriptionGraph;
    }
    public int getVertex() {
        return m_vertex;
    }
    public int getArity() {
        return 1;
    }
    public boolean isAlwaysTrue() {
        return false;
    }
    public boolean isAlwaysFalse() {
        return false;
    }
    public String toString(Prefixes prefixes) {
        return "exists("+prefixes.abbreviateIRI(m_descriptionGraph.getName())+'|'+m_vertex+')';
    }
    public String toString() {
        return toString(Prefixes.STANDARD_PREFIXES);
    }
    protected Object readResolve() {
        return s_interningManager.intern(this);
    }

    protected static InterningManager<ExistsDescriptionGraph> s_interningManager=new InterningManager<ExistsDescriptionGraph>() {
        protected boolean equal(ExistsDescriptionGraph object1,ExistsDescriptionGraph object2) {
            return object1.m_descriptionGraph.equals(object2.m_descriptionGraph) && object1.m_vertex==object2.m_vertex;
        }
        protected int getHashCode(ExistsDescriptionGraph object) {
            return object.m_descriptionGraph.hashCode()+7*object.m_vertex;
        }
    };
    
    public static ExistsDescriptionGraph create(DescriptionGraph descriptionGraph,int vertex) {
        return s_interningManager.intern(new ExistsDescriptionGraph(descriptionGraph,vertex));
    }
}
