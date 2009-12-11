/* Copyright 2009 by the Oxford University Computing Laboratory
   
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
import org.semanticweb.HermiT.datatypes.DatatypeRegistry;

/**
 * A data range consisting of a datatype URI and a number of facet restrictions.
 */
public class DatatypeRestriction extends DataRange {
    private static final long serialVersionUID=524235536504588458L;
    public static final String[] NO_FACET_URIs=new String[0];
    public static final Object[] NO_FACET_VALUES=new Object[0];

    protected final String m_datatypeURI;
    protected final String[] m_facetURIs;
    protected final Object[] m_facetValues;

    public DatatypeRestriction(String datatypeURI,String[] facetURIs,Object[] facetValues) {
        m_datatypeURI=datatypeURI;
        m_facetURIs=facetURIs;
        m_facetValues=facetValues;
    }
    public String getDatatypeURI() {
        return m_datatypeURI;
    }
    public int getNumberOfFacetRestrictions() {
        return m_facetURIs.length;
    }
    public String getFacetURI(int index) {
        return m_facetURIs[index];
    }
    public Object getFacetValue(int index) {
        return m_facetValues[index];
    }
    public LiteralConcept getNegation() {
        return NegationDataRange.create(this);
    }
    public boolean isAlwaysTrue() {
        return this==RDFS_LITERAL;
    }
    public boolean isAlwaysFalse() {
        return false;
    }
    public String toString(Prefixes prefixes) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(prefixes.abbreviateIRI(m_datatypeURI));
        if (m_facetURIs.length>0) {
            buffer.append('[');
            for (int index=0;index<m_facetURIs.length;index++) {
                if (index>0)
                    buffer.append(',');
                buffer.append(prefixes.abbreviateIRI(m_facetURIs[index]));
                buffer.append('=');
                buffer.append(DatatypeRegistry.toString(prefixes,m_facetValues[index]));
            }
            buffer.append(']');
        }
        return buffer.toString();
    }
    protected Object readResolve() {
        return s_interningManager.intern(this);
    }

    protected static InterningManager<DatatypeRestriction> s_interningManager=new InterningManager<DatatypeRestriction>() {
        protected boolean equal(DatatypeRestriction object1,DatatypeRestriction object2) {
            if (!object1.m_datatypeURI.equals(object2.m_datatypeURI) || object1.m_facetURIs.length!=object2.m_facetURIs.length)
                return false;
            for (int index=object1.m_facetURIs.length-1;index>=0;--index)
                if (!contains(object2,object1.m_facetURIs[index],object1.m_facetValues[index]))
                    return false;
            return true;
        }
        protected boolean contains(DatatypeRestriction datatypeRestriction,String facetURI,Object facetValue) {
            for (int i=datatypeRestriction.m_facetURIs.length-1;i>=0;--i)
                if (datatypeRestriction.m_facetURIs[i].equals(facetURI) && datatypeRestriction.m_facetValues[i].equals(facetValue))
                    return true;
            return false;
        }
        protected int getHashCode(DatatypeRestriction object) {
            int hashCode=object.m_datatypeURI.hashCode();
            for (int index=object.m_facetURIs.length-1;index>=0;--index)
                hashCode+=object.m_facetURIs[index].hashCode()+object.m_facetValues[index].hashCode();
            return hashCode;
        }
    };

    public static DatatypeRestriction create(String datatypeURI,String[] facetURIs,Object[] facetValues) {
        return s_interningManager.intern(new DatatypeRestriction(datatypeURI,facetURIs,facetValues));
    }
    
    public static final DatatypeRestriction RDFS_LITERAL=create("http://www.w3.org/2000/01/rdf-schema#Literal",NO_FACET_URIs, NO_FACET_VALUES);
}
