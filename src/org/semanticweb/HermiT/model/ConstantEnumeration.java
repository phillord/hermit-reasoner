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
 * A data range that consists of a given set of constants.
 */
public class ConstantEnumeration extends AtomicDataRange {
    private static final long serialVersionUID=4663162424764302912L;

    protected final Constant[] m_constants;

    protected ConstantEnumeration(Constant[] constants) {
        m_constants=constants;
    }
    public int getNumberOfConstants() {
        return m_constants.length;
    }
    public Constant getConstant(int index) {
        return m_constants[index];
    }
    public LiteralDataRange getNegation() {
        return AtomicNegationDataRange.create(this);
    }
    public boolean isAlwaysTrue() {
        return false;
    }
    public boolean isAlwaysFalse() {
        return m_constants.length==0;
    }
    public String toString(Prefixes prefixes) {
        StringBuffer buffer=new StringBuffer();
        buffer.append("{ ");
        for (int index=0;index<m_constants.length;index++) {
            if (index>0)
                buffer.append(' ');
            buffer.append(m_constants[index].toString(prefixes));
        }
        buffer.append(" }");
        return buffer.toString();
    }
    protected Object readResolve() {
        return s_interningManager.intern(this);
    }

    protected static InterningManager<ConstantEnumeration> s_interningManager=new InterningManager<ConstantEnumeration>() {
        protected boolean equal(ConstantEnumeration object1,ConstantEnumeration object2) {
            if (object1.m_constants.length!=object2.m_constants.length)
                return false;
            for (int index=object1.m_constants.length-1;index>=0;--index)
                if (!contains(object1.m_constants[index],object2.m_constants))
                    return false;
            return true;
        }
        protected boolean contains(Constant constant,Constant[] constants) {
            for (int i=constants.length-1;i>=0;--i)
                if (constants[i].equals(constant))
                    return true;
            return false;
        }
        protected int getHashCode(ConstantEnumeration object) {
            int hashCode=0;
            for (int index=object.m_constants.length-1;index>=0;--index)
                hashCode+=object.m_constants[index].hashCode();
            return hashCode;
        }
    };

    public static ConstantEnumeration create(Constant[] constants) {
        return s_interningManager.intern(new ConstantEnumeration(constants));
    }
}
