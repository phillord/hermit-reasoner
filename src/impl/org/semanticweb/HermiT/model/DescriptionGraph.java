// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.model;

import java.io.Serializable;
import java.util.Set;

import org.semanticweb.HermiT.*;

public class DescriptionGraph implements DLPredicate,Serializable {
    private static final long serialVersionUID=-6098910060520673164L;

    protected final String m_name;
    protected final AtomicConcept[] m_atomicConceptsByVertices;
    protected final Edge[] m_edges;
    protected final Set<AtomicConcept> m_startConcepts;
    
    public DescriptionGraph(String name,AtomicConcept[] atomicConceptsByVertices,Edge[] edges,Set<AtomicConcept> startConcepts) {
        m_name=name;
        m_atomicConceptsByVertices=atomicConceptsByVertices;
        m_edges=edges;
        m_startConcepts=startConcepts;
    }
    public String getName() {
        return m_name;
    }
    public int getArity() {
        return m_atomicConceptsByVertices.length;
    }
    public AtomicConcept getAtomicConceptForVertex(int vertex) {
        return m_atomicConceptsByVertices[vertex];
    }
    public int getNumberOfVertices() {
        return m_atomicConceptsByVertices.length;
    }
    public int getNumberOfEdges() {
        return m_edges.length;
    }
    public Edge getEdge(int edgeIndex) {
        return m_edges[edgeIndex];
    }
    public Set<AtomicConcept> getStartConcepts() {
        return m_startConcepts;
    }
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
    public String toString(Namespaces namespaces) {
        return m_name;
    }
    public String toString() {
        return toString(Namespaces.INSTANCE);
    }
    public String getTextRepresentation() {
        StringBuffer buffer=new StringBuffer();
        String CRLF=System.getProperty("line.separator");
        buffer.append('[');
        buffer.append(CRLF);
        for (int vertex=0;vertex<m_atomicConceptsByVertices.length;vertex++) {
            buffer.append("   ");
            buffer.append(vertex);
            buffer.append(" --> ");
            buffer.append(m_atomicConceptsByVertices[vertex].getURI());
            buffer.append(CRLF);
        }
        buffer.append(CRLF);
        for (Edge edge : m_edges) {
            buffer.append("  ");
            buffer.append(edge.getFromVertex());
            buffer.append(" -- ");
            buffer.append(edge.getAtomicRole().getURI());
            buffer.append(" --> ");
            buffer.append(edge.getToVertex());
            buffer.append(CRLF);
        }
        buffer.append(CRLF);
        for (AtomicConcept atomicConcept : m_startConcepts) {
            buffer.append("  ");
            buffer.append(atomicConcept.getURI());
            buffer.append(CRLF);
        }
        buffer.append(']');
        return buffer.toString();
    }
    
    public static class Edge implements Serializable {
        private static final long serialVersionUID=-2407275128459101707L;

        protected final AtomicRole m_atomicRole;
        protected final int m_fromVertex;
        protected final int m_toVertex;

        public Edge(AtomicRole atomicRole,int fromVertex,int toVertex) {
            m_atomicRole=atomicRole;
            m_fromVertex=fromVertex;
            m_toVertex=toVertex;
        }
        public AtomicRole getAtomicRole() {
            return m_atomicRole;
        }
        public int getFromVertex() {
            return m_fromVertex;
        }
        public int getToVertex() {
            return m_toVertex;
        }
        public int hashCode() {
            return m_fromVertex+7*m_toVertex+11*m_atomicRole.hashCode();
        }
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
