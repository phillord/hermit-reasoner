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
package org.semanticweb.HermiT.model;

import org.semanticweb.HermiT.Prefixes;
/**ExistsDescriptionGraph.*/
public class ExistsDescriptionGraph extends ExistentialConcept implements DLPredicate {
    private static final long serialVersionUID=7433430510725260994L;

    protected final DescriptionGraph m_descriptionGraph;
    protected final int m_vertex;
    
    protected ExistsDescriptionGraph(DescriptionGraph descriptionGraph,int vertex) {
        m_descriptionGraph=descriptionGraph;
        m_vertex=vertex;
    }
    /**
     * @return description graph
     */
    public DescriptionGraph getDescriptionGraph() {
        return m_descriptionGraph;
    }
    /**
     * @return vertex
     */
    public int getVertex() {
        return m_vertex;
    }
    @Override
    public int getArity() {
        return 1;
    }
    @Override
    public boolean isAlwaysTrue() {
        return false;
    }
    @Override
    public boolean isAlwaysFalse() {
        return false;
    }
    @Override
    public String toString(Prefixes prefixes) {
        return "exists("+prefixes.abbreviateIRI(m_descriptionGraph.getName())+'|'+m_vertex+')';
    }
    @Override
    public String toString() {
        return toString(Prefixes.STANDARD_PREFIXES);
    }
    protected Object readResolve() {
        return s_interningManager.intern(this);
    }

    protected static final InterningManager<ExistsDescriptionGraph> s_interningManager=new InterningManager<ExistsDescriptionGraph>() {
        @Override
        protected boolean equal(ExistsDescriptionGraph object1,ExistsDescriptionGraph object2) {
            return object1.m_descriptionGraph.equals(object2.m_descriptionGraph) && object1.m_vertex==object2.m_vertex;
        }
        @Override
        protected int getHashCode(ExistsDescriptionGraph object) {
            return object.m_descriptionGraph.hashCode()+7*object.m_vertex;
        }
    };
    
    /**
     * @param descriptionGraph descriptionGraph
     * @param vertex vertex
     * @return description graph
     */
    public static ExistsDescriptionGraph create(DescriptionGraph descriptionGraph,int vertex) {
        return s_interningManager.intern(new ExistsDescriptionGraph(descriptionGraph,vertex));
    }
}
