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
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.HermiT.Prefixes;

/**
 * Represents an atom in a DL clause.
 */
public class Atom implements Serializable {
    private static final long serialVersionUID=7884900540178779422L;
    public static final Set<DLPredicate> s_infixPredicates=new HashSet<DLPredicate>();
    static {
        s_infixPredicates.add(Equality.INSTANCE);
        s_infixPredicates.add(Inequality.INSTANCE);
        s_infixPredicates.add(NodeIDLessEqualThan.INSTANCE);
    }

    protected final DLPredicate m_dlPredicate;
    protected final Term[] m_arguments;

    protected Atom(DLPredicate dlPredicate,Term[] arguments) {
        m_dlPredicate=dlPredicate;
        m_arguments=arguments;
        if (m_dlPredicate.getArity()!=m_arguments.length)
            throw new IllegalArgumentException("The arity of the predicate must be equal to the number of arguments.");
    }
    public DLPredicate getDLPredicate() {
        return m_dlPredicate;
    }
    public int getArity() {
        return m_arguments.length;
    }
    public Term getArgument(int argumentIndex) {
        return m_arguments[argumentIndex];
    }
    public Variable getArgumentVariable(int argumentIndex) {
        if (m_arguments[argumentIndex] instanceof Variable)
            return (Variable)m_arguments[argumentIndex];
        else
            return null;
    }
    public void getVariables(Set<Variable> variables) {
        for (int argumentIndex=m_arguments.length-1;argumentIndex>=0;--argumentIndex) {
            Term argument=m_arguments[argumentIndex];
            if (argument instanceof Variable)
                variables.add((Variable)argument);
        }
    }
    public void getIndividuals(Set<Individual> individuals) {
        for (int argumentIndex=m_arguments.length-1;argumentIndex>=0;--argumentIndex) {
            Term argument=m_arguments[argumentIndex];
            if (argument instanceof Individual)
                individuals.add((Individual)argument);
        }
    }
    public boolean containsVariable(Variable variable) {
        for (int argumentIndex=m_arguments.length-1;argumentIndex>=0;--argumentIndex)
            if (m_arguments[argumentIndex].equals(variable))
                return true;
        return false;
    }
    public Atom replaceDLPredicate(DLPredicate newDLPredicate) {
        return create(newDLPredicate,m_arguments);
    }
    public String toString(Prefixes prefixes) {
        StringBuffer buffer=new StringBuffer();
        if (s_infixPredicates.contains(m_dlPredicate)) {
            buffer.append(m_arguments[0].toString(prefixes));
            buffer.append(' ');
            buffer.append(m_dlPredicate.toString(prefixes));
            buffer.append(' ');
            buffer.append(m_arguments[1].toString(prefixes));
        }
        else if (m_dlPredicate instanceof AnnotatedEquality) {
            AnnotatedEquality annotatedEquality=(AnnotatedEquality)m_dlPredicate;
            buffer.append('[');
            buffer.append(m_arguments[0].toString(prefixes));
            buffer.append(' ');
            buffer.append("==");
            buffer.append(' ');
            buffer.append(m_arguments[1].toString(prefixes));
            buffer.append("]@atMost(");
            buffer.append(annotatedEquality.getCaridnality());
            buffer.append(' ');
            buffer.append(annotatedEquality.getOnRole().toString(prefixes));
            buffer.append(' ');
            buffer.append(annotatedEquality.getToConcept().toString(prefixes));
            buffer.append(")(");
            buffer.append(m_arguments[2].toString(prefixes));
            buffer.append(')');
        }
        else {
            buffer.append(m_dlPredicate.toString(prefixes));
            buffer.append('(');
            for (int i=0;i<m_arguments.length;i++) {
                if (i!=0)
                    buffer.append(',');
                buffer.append(m_arguments[i].toString(prefixes));
            }
            buffer.append(')');
        }
        return buffer.toString();
    }
    public String toString() {
        return toString(Prefixes.STANDARD_PREFIXES);
    }
    protected Object readResolve() {
        return s_interningManager.intern(this);
    }

    protected static InterningManager<Atom> s_interningManager=new InterningManager<Atom>() {
        protected boolean equal(Atom object1,Atom object2) {
            if (object1.m_dlPredicate!=object2.m_dlPredicate)
                return false;
            for (int index=object1.m_arguments.length-1;index>=0;--index)
                if (object1.m_arguments[index]!=object2.m_arguments[index])
                    return false;
            return true;
        }
        protected int getHashCode(Atom object) {
            int hashCode=object.m_dlPredicate.hashCode();
            for (int index=object.m_arguments.length-1;index>=0;--index)
                hashCode+=object.m_arguments[index].hashCode();
            return hashCode;
        }
    };

    public static Atom create(DLPredicate dlPredicate,Term... arguments) {
        return s_interningManager.intern(new Atom(dlPredicate,arguments));
    }
}
