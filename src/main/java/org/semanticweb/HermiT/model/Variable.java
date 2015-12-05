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
        return toString(Prefixes.STANDARD_PREFIXES);
    }
    public String toString(Prefixes prefixes) {
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
