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

import java.io.Serializable;
import java.util.Set;

import org.semanticweb.HermiT.Prefixes;
/**DescriptionGraph.*/
public class DescriptionGraph implements DLPredicate,Serializable {
    private static final long serialVersionUID=-6098910060520673164L;

    protected final String m_name;
    protected final AtomicConcept[] m_atomicConceptsByVertices;
    protected final Edge[] m_edges;
    protected final Set<AtomicConcept> m_startConcepts;

    /**
     * @param name name
     * @param atomicConceptsByVertices atomicConceptsByVertices
     * @param edges edges
     * @param startConcepts startConcepts
     */
    public DescriptionGraph(String name,AtomicConcept[] atomicConceptsByVertices,Edge[] edges,Set<AtomicConcept> startConcepts) {
        m_name=name;
        m_atomicConceptsByVertices=atomicConceptsByVertices;
        m_edges=edges;
        m_startConcepts=startConcepts;
    }
    /**
     * @return name
     */
    public String getName() {
        return m_name;
    }
    @Override
    public int getArity() {
        return m_atomicConceptsByVertices.length;
    }
    /**
     * @param vertex vertex
     * @return concept for vertex
     */
    public AtomicConcept getAtomicConceptForVertex(int vertex) {
        return m_atomicConceptsByVertices[vertex];
    }
    /**
     * @return number of vertices
     */
    public int getNumberOfVertices() {
        return m_atomicConceptsByVertices.length;
    }
    /**
     * @return number of edges
     */
    public int getNumberOfEdges() {
        return m_edges.length;
    }
    /**
     * @param edgeIndex edgeIndex
     * @return edge
     */
    public Edge getEdge(int edgeIndex) {
        return m_edges[edgeIndex];
    }
    /**
     * @return start concepts
     */
    public Set<AtomicConcept> getStartConcepts() {
        return m_startConcepts;
    }
    /**
     * @param resultingDLClauses resultingDLClauses
     */
    public void produceStartDLClauses(Set<DLClause> resultingDLClauses) {
        Variable X=Variable.create("X");
        for (AtomicConcept startAtomicConcept : m_startConcepts) {
            Atom[] antecedent=new Atom[] { Atom.create(startAtomicConcept,X) };
            int numberOfVerticesWithStartConcept=0;
            for (AtomicConcept vertexConcept : m_atomicConceptsByVertices)
                if (vertexConcept.equals(startAtomicConcept))
                    numberOfVerticesWithStartConcept++;
            int index=0;
            Atom[] consequent=new Atom[numberOfVerticesWithStartConcept];
            for (int vertex=0;vertex<m_atomicConceptsByVertices.length;vertex++)
                if (m_atomicConceptsByVertices[vertex].equals(startAtomicConcept))
                    consequent[index++]=Atom.create(ExistsDescriptionGraph.create(this,vertex),X);
            resultingDLClauses.add(DLClause.create(consequent,antecedent));
        }
    }
    @Override
    public String toString(Prefixes ns) {
        return ns.abbreviateIRI(m_name);
    }
    @Override
    public String toString() {
        return toString(Prefixes.STANDARD_PREFIXES);
    }
    /**
     * @return text representation
     */
    public String getTextRepresentation() {
        StringBuffer buffer=new StringBuffer();
        buffer.append('[');
        buffer.append('\n');
       for (int vertex=0;vertex<m_atomicConceptsByVertices.length;vertex++) {
            buffer.append("   ");
            buffer.append(vertex);
            buffer.append(" --> ");
            buffer.append(m_atomicConceptsByVertices[vertex].getIRI());
            buffer.append('\n');
        }
        buffer.append('\n');
        for (Edge edge : m_edges) {
            buffer.append("  ");
            buffer.append(edge.getFromVertex());
            buffer.append(" -- ");
            buffer.append(edge.getAtomicRole().getIRI());
            buffer.append(" --> ");
            buffer.append(edge.getToVertex());
            buffer.append('\n');
        }
        buffer.append('\n');
        for (AtomicConcept atomicConcept : m_startConcepts) {
            buffer.append("  ");
            buffer.append(atomicConcept.getIRI());
            buffer.append('\n');
        }
        buffer.append(']');
        return buffer.toString();
    }

    /**Edge.*/
    public static class Edge implements Serializable {
        private static final long serialVersionUID=-2407275128459101707L;

        protected final AtomicRole m_atomicRole;
        protected final int m_fromVertex;
        protected final int m_toVertex;

        /**
         * @param atomicRole atomicRole
         * @param fromVertex fromVertex
         * @param toVertex toVertex
         */
        public Edge(AtomicRole atomicRole,int fromVertex,int toVertex) {
            m_atomicRole=atomicRole;
            m_fromVertex=fromVertex;
            m_toVertex=toVertex;
        }
        /**
         * @return atomic role
         */
        public AtomicRole getAtomicRole() {
            return m_atomicRole;
        }
        /**
         * @return from vertex
         */
        public int getFromVertex() {
            return m_fromVertex;
        }
        /**
         * @return to vertex
         */
        public int getToVertex() {
            return m_toVertex;
        }
        @Override
        public int hashCode() {
            return m_fromVertex+7*m_toVertex+11*m_atomicRole.hashCode();
        }
        @Override
        public boolean equals(Object that) {
            if (this==that)
                return true;
            if (!(that instanceof Edge))
                return false;
            Edge thatEdge=(Edge)that;
            return m_atomicRole.equals(thatEdge.m_atomicRole) && m_fromVertex==thatEdge.m_fromVertex && m_toVertex==thatEdge.m_toVertex;
        }
    }
}
