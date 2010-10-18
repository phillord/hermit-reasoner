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

/**
 * A data range consisting of a datatype URI and a number of facet restrictions.
 * NOTE: RDFS_LITERAL is treated as InternalDatatype due to implemetnation side-effects.
 */
public class DatatypeRestriction extends AtomicDataRange {
    private static final long serialVersionUID=524235536504588458L;
    public static final String[] NO_FACET_URIs=new String[0];
    public static final Constant[] NO_FACET_VALUES=new Constant[0];

    protected final String m_datatypeURI;
    protected final String[] m_facetURIs;
    protected final Constant[] m_facetValues;

    public DatatypeRestriction(String datatypeURI,String[] facetURIs,Constant[] facetValues) {
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
    public Constant getFacetValue(int index) {
        return m_facetValues[index];
    }
    public LiteralDataRange getNegation() {
        return AtomicNegationDataRange.create(this);
    }
    public boolean isAlwaysTrue() {
        return false;
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
                buffer.append(m_facetValues[index].toString(prefixes));
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

    public static DatatypeRestriction create(String datatypeURI,String[] facetURIs,Constant[] facetValues) {
        return s_interningManager.intern(new DatatypeRestriction(datatypeURI,facetURIs,facetValues));
    }
}
