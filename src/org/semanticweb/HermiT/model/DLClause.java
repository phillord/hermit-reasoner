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
 * Represents a DL clause. The body is a conjunction of atoms and the head is a disjunction of atoms.
 */
public class DLClause implements Serializable {
    private static final long serialVersionUID=-4513910129515151732L;

    protected final Atom[] m_headAtoms;
    protected final Atom[] m_bodyAtoms;

    protected DLClause(Atom[] headAtoms,Atom[] bodyAtoms) {
        m_headAtoms=headAtoms;
        m_bodyAtoms=bodyAtoms;
    }
    public int getHeadLength() {
        return m_headAtoms.length;
    }
    public Atom getHeadAtom(int atomIndex) {
        return m_headAtoms[atomIndex];
    }
    public Atom[] getHeadAtoms() {
        return m_headAtoms.clone();
    }
    public int getBodyLength() {
        return m_bodyAtoms.length;
    }
    public Atom getBodyAtom(int atomIndex) {
        return m_bodyAtoms[atomIndex];
    }
    public Atom[] getBodyAtoms() {
        return m_bodyAtoms.clone();
    }
    public DLClause getSafeVersion(DLPredicate safeMakingPredicate) {
        Set<Variable> variables=new HashSet<Variable>();
        // collect all the variables that occur in the head into the set variables
        for (int headIndex=0;headIndex<m_headAtoms.length;headIndex++) {
            Atom atom=m_headAtoms[headIndex];
            for (int argumentIndex=0;argumentIndex<atom.getArity();argumentIndex++) {
                Variable variable=atom.getArgumentVariable(argumentIndex);
                if (variable!=null)
                    variables.add(variable);
            }
        }
        // remove all those variables that occur in the body, so we get a set
        // with the unsafe variables
        for (int bodyIndex=0;bodyIndex<m_bodyAtoms.length;bodyIndex++) {
            Atom atom=m_bodyAtoms[bodyIndex];
            for (int argumentIndex=0;argumentIndex<atom.getArity();argumentIndex++) {
                Variable variable=atom.getArgumentVariable(argumentIndex);
                if (variable!=null)
                    variables.remove(variable);
            }
        }
        if (m_headAtoms.length==0 && m_bodyAtoms.length==0)
            variables.add(Variable.create("X"));
        if (variables.isEmpty())
            return this;
        else {
            // we need to add a concept atom with the top concept for each of
            // the unsafe variables
            Atom[] newBodyAtoms=new Atom[m_bodyAtoms.length+variables.size()];
            System.arraycopy(m_bodyAtoms,0,newBodyAtoms,0,m_bodyAtoms.length);
            int index=m_bodyAtoms.length;
            for (Variable variable : variables)
                newBodyAtoms[index++]=Atom.create(safeMakingPredicate,variable);
            return DLClause.create(m_headAtoms,newBodyAtoms);
        }
    }
    public DLClause getChangedDLClause(Atom[] headAtoms,Atom[] bodyAtoms) {
        if (headAtoms==null)
            headAtoms=m_headAtoms;
        if (bodyAtoms==null)
            bodyAtoms=m_bodyAtoms;
        return DLClause.create(headAtoms,bodyAtoms);
    }
    public boolean isGeneralConceptInclusion() {
        if (m_headAtoms.length==0) {
            // not a GCI if all body atoms are data ranges
            // could also be an asymmetry axiom of the form r(x, y) and r(y, x) -> bottom
            if (m_bodyAtoms.length==2 && m_bodyAtoms[0].getArity()==2 && m_bodyAtoms[1].getArity()==2)
                return false;
            for (Atom bodyAtom : m_bodyAtoms)
                if (bodyAtom.getArity()!=1 || !(bodyAtom.getDLPredicate() instanceof DataRange)) 
                    return true;                
        }
        for (Atom headAtom : m_headAtoms) {
            DLPredicate predicate=headAtom.getDLPredicate(); 
            if (predicate instanceof AtLeast 
                    || predicate instanceof LiteralConcept 
                    || predicate instanceof AnnotatedEquality
                    || predicate instanceof NodeIDLessEqualThan
                    || predicate instanceof NodeIDsAscendingOrEqual) 
                return true;
            if (predicate instanceof Equality) {
                // could be a key, which we do not count as GCI
                // check if body uses the special named concept
                for (Atom bodyAtom : m_bodyAtoms) {
                    DLPredicate bodyPredicate=bodyAtom.getDLPredicate();
                    if (bodyAtom.getArity()==1 
                            && bodyPredicate instanceof AtomicConcept 
                            && ((AtomicConcept)bodyPredicate).equals(AtomicConcept.INTERNAL_NAMED))
                        return false;
                }
            }
            if (predicate instanceof DataRange) {
                // could be a data range inclusion or a universal, e.g., A -> for all dp.DR
                for (Atom bodyAtom : m_bodyAtoms) {
                    if (bodyAtom.getArity()==2)
                        return true;
                }
                return false;
            }
            if (predicate instanceof Role) // role inclusion
                return false;
        }
        return false;
    }
    public boolean isAtomicConceptInclusion() {
        if (m_bodyAtoms.length==1 && m_headAtoms.length==1) {
            Atom bodyAtom=m_bodyAtoms[0];
            Atom headAtom=m_headAtoms[0];
            if (bodyAtom.getArity()==1 && headAtom.getArity()==1 && bodyAtom.getDLPredicate() instanceof AtomicConcept && headAtom.getDLPredicate() instanceof AtomicConcept) {
                Term argument=bodyAtom.getArgument(0);
                return argument instanceof Variable && argument.equals(headAtom.getArgument(0));
            }
        }
        return false;
    }
    public boolean isAtomicRoleInclusion() {
        if (m_bodyAtoms.length==1 && m_headAtoms.length==1) {
            Atom bodyAtom=m_bodyAtoms[0];
            Atom headAtom=m_headAtoms[0];
            if (bodyAtom.getArity()==2 && headAtom.getArity()==2 && bodyAtom.getDLPredicate() instanceof AtomicRole && headAtom.getDLPredicate() instanceof AtomicRole) {
                Term argument0=bodyAtom.getArgument(0);
                Term argument1=bodyAtom.getArgument(1);
                return argument0 instanceof Variable && argument1 instanceof Variable && !argument0.equals(argument1) && argument0.equals(headAtom.getArgument(0))  && argument1.equals(headAtom.getArgument(1));
            }
        }
        return false;
    }
    public boolean isAtomicRoleInverseInclusion() {
        if (m_bodyAtoms.length==1 && m_headAtoms.length==1) {
            Atom bodyAtom=m_bodyAtoms[0];
            Atom headAtom=m_headAtoms[0];
            if (bodyAtom.getArity()==2 && headAtom.getArity()==2 && bodyAtom.getDLPredicate() instanceof AtomicRole && headAtom.getDLPredicate() instanceof AtomicRole) {
                Term argument0=bodyAtom.getArgument(0);
                Term argument1=bodyAtom.getArgument(1);
                return argument0 instanceof Variable && argument1 instanceof Variable && !argument0.equals(argument1) && argument0.equals(headAtom.getArgument(1))  && argument1.equals(headAtom.getArgument(0));
            }
        }
        return false;
    }
    public boolean isFunctionalityAxiom() {
        if (m_bodyAtoms.length==2 && m_headAtoms.length==1) {
            DLPredicate atomicRole=getBodyAtom(0).getDLPredicate();
            if (atomicRole instanceof AtomicRole) {
                if (getBodyAtom(1).getDLPredicate().equals(atomicRole) && (getHeadAtom(0).getDLPredicate() instanceof AnnotatedEquality)) {
                    Variable x=getBodyAtom(0).getArgumentVariable(0);
                    if (x!=null && x.equals(getBodyAtom(1).getArgument(0))) {
                        Variable y1=getBodyAtom(0).getArgumentVariable(1);
                        Variable y2=getBodyAtom(1).getArgumentVariable(1);
                        Variable headY1=getHeadAtom(0).getArgumentVariable(0);
                        Variable headY2=getHeadAtom(0).getArgumentVariable(1);
                        if (y1!=null && y2!=null && !y1.equals(y2) && headY1!=null && headY2!=null && ((y1.equals(headY1) && y2.equals(headY2)) || (y1.equals(headY2) && y2.equals(headY1))))
                            return true;
                    }
                }
            }
        }
        return false;
    }
    public boolean isInverseFunctionalityAxiom() {
        if (getBodyLength()==2 && getHeadLength()==1) {
            DLPredicate atomicRole=getBodyAtom(0).getDLPredicate();
            if (atomicRole instanceof AtomicRole) {
                if (getBodyAtom(1).getDLPredicate().equals(atomicRole) && (getHeadAtom(0).getDLPredicate() instanceof AnnotatedEquality)) {
                    Variable x=getBodyAtom(0).getArgumentVariable(1);
                    if (x!=null && x.equals(getBodyAtom(1).getArgument(1))) {
                        Variable y1=getBodyAtom(0).getArgumentVariable(0);
                        Variable y2=getBodyAtom(1).getArgumentVariable(0);
                        Variable headY1=getHeadAtom(0).getArgumentVariable(0);
                        Variable headY2=getHeadAtom(0).getArgumentVariable(1);
                        if (y1!=null && y2!=null && !y1.equals(y2) && headY1!=null && headY2!=null && ((y1.equals(headY1) && y2.equals(headY2)) || (y1.equals(headY2) && y2.equals(headY1))))
                            return true;
                    }
                }
            }
        }
        return false;
    }
    public String toString(Prefixes prefixes) {
        StringBuffer buffer=new StringBuffer();
        for (int headIndex=0;headIndex<m_headAtoms.length;headIndex++) {
            if (headIndex!=0)
                buffer.append(" v ");
            buffer.append(m_headAtoms[headIndex].toString(prefixes));
        }
        buffer.append(" :- ");
        for (int bodyIndex=0;bodyIndex<m_bodyAtoms.length;bodyIndex++) {
            if (bodyIndex!=0)
                buffer.append(", ");
            buffer.append(m_bodyAtoms[bodyIndex].toString(prefixes));
        }
        return buffer.toString();
    }
    public String toString() {
        return toString(Prefixes.STANDARD_PREFIXES);
    }

    protected static InterningManager<DLClause> s_interningManager=new InterningManager<DLClause>() {
        protected boolean equal(DLClause object1,DLClause object2) {
            if (object1.m_headAtoms.length!=object2.m_headAtoms.length || object1.m_bodyAtoms.length!=object2.m_bodyAtoms.length)
                return false;
            for (int index=object1.m_headAtoms.length-1;index>=0;--index)
                if (object1.m_headAtoms[index]!=object2.m_headAtoms[index])
                    return false;
            for (int index=object1.m_bodyAtoms.length-1;index>=0;--index)
                if (object1.m_bodyAtoms[index]!=object2.m_bodyAtoms[index])
                    return false;
            return true;
        }
        protected int getHashCode(DLClause object) {
            int hashCode=0;
            for (int index=object.m_bodyAtoms.length-1;index>=0;--index)
                hashCode+=object.m_bodyAtoms[index].hashCode();
            for (int index=object.m_headAtoms.length-1;index>=0;--index)
                hashCode+=object.m_headAtoms[index].hashCode();
            return hashCode;
        }
    };

    public static DLClause create(Atom[] headAtoms,Atom[] bodyAtoms) {
        return s_interningManager.intern(new DLClause(headAtoms,bodyAtoms));
    }
}
