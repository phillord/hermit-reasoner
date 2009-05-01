// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.model;

import java.io.Serializable;

import org.semanticweb.HermiT.Prefixes;
import org.semanticweb.HermiT.datatypes.DatatypeRegistry;

/**
 * Represents a constants.
 */
public class Constant extends Term {
    private static final long serialVersionUID=-8143911431654640690L;

    protected final Object m_dataValue;
    
    protected Constant(Object dataValue) {
        m_dataValue=dataValue;
    }
    public Object getDataValue() {
        return m_dataValue;
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
        return s_interningManager.intern(new Constant(dataValue));
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
