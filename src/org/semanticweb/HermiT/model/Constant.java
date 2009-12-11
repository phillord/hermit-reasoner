/* Copyright 2008, 2009 by the Oxford University Computing Laboratory
   
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
import java.net.URI;

import org.semanticweb.HermiT.Prefixes;
import org.semanticweb.HermiT.datatypes.DatatypeRegistry;

/**
 * Represents a constants.
 */
public class Constant extends Term {
    private static final long serialVersionUID=-8143911431654640690L;

    protected final Object m_dataValue;
    protected final URI m_datatypeURI;
    

    protected Constant(Object dataValue) {
        this(dataValue,null);
    }
    protected Constant(Object dataValue,URI datatypeURI) {
        m_dataValue=dataValue;
        m_datatypeURI=datatypeURI;
    }
    public Object getDataValue() {
        return m_dataValue;
    }
    public URI getDatatypeURI() {
        return m_datatypeURI;
    }
    public String toString() {
        return toString(Prefixes.STANDARD_PREFIXES);
    }
    public String toString(Prefixes prefixes) {
        return DatatypeRegistry.toString(prefixes,m_dataValue);
    }
    protected Object readResolve() {
        return s_interningManager.intern(this);
    }

    protected static InterningManager<Constant> s_interningManager=new InterningManager<Constant>() {
        protected boolean equal(Constant object1,Constant object2) {
            return object1.m_dataValue.equals(object2.m_dataValue);
        }
        protected int getHashCode(Constant object) {
            return object.m_dataValue.hashCode();
        }
    };
    
    public static Constant create(Object dataValue) {
        return create(dataValue,null);
    }
    public static Constant create(Object dataValue,URI datatypeURI) {
        return s_interningManager.intern(new Constant(dataValue,datatypeURI));
    }
    
    public static class AnonymousConstantValue implements Serializable {
        private static final long serialVersionUID=-6507581477324043034L;

        protected final String m_name;
        
        public AnonymousConstantValue(String name) {
            m_name=name;
        }
        public String getName() {
            return m_name;
        }
        public int hashCode() {
            return m_name.hashCode();
        }
        public boolean equals(Object that) {
            if (this==that)
                return true;
            if (!(that instanceof AnonymousConstantValue))
                return false;
            return ((AnonymousConstantValue)that).m_name.equals(m_name);
        }
        public static AnonymousConstantValue create(String name) {
            return new AnonymousConstantValue(name);
        }
    }
}
