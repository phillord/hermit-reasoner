package org.semanticweb.HermiT.model;

import org.semanticweb.HermiT.*;

/**
 * Represents a variable.
 */
public class Variable extends Term {
    private static final long serialVersionUID=-1943457771102512887L;

    protected final String m_name;
    
    protected Variable(String name) {
        m_name=name;
    }
    public String getName() {
        return m_name;
    }
    public String toString() {
        return m_name;
    }
    public String toString(Namespaces namespaces) {
        return m_name;
    }
    protected Object readResolve() {
        return s_interningManager.intern(this);
    }

    protected static InterningManager<Variable> s_interningManager=new InterningManager<Variable>() {
        protected boolean equal(Variable object1,Variable object2) {
            return object1.m_name.equals(object2.m_name);
        }
        protected int getHashCode(Variable object) {
            return object.m_name.hashCode();
        }
    };
    
    public static Variable create(String name) {
        return s_interningManager.intern(new Variable(name));
    }
}
