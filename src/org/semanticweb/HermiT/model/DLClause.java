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
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.semanticweb.HermiT.Prefixes;

/**
 * Represents a DL clause. The body is a conjunction of atoms and the head is a disjunction of atoms.
 */
public class DLClause implements Serializable {
    public static enum ClauseType {
        CONCEPT_INCLUSION, DATA_RANGE_INCLUSION, INVERSE_OBJECT_PROPERTY_INCLUSION, OBJECT_PROPERTY_INCLUSION, DATA_PROPERTY_INCLUSION,
        ASYMMETRY, REFLEXIVITY, IRREFLEXIVITY, DISJOINT_OBJECT_PROPERTIES, DISJOINT_DATA_PROPERTIES,
        HAS_KEY, SWRL_RULE, GRAPH_RULE, GRAPH_START_CLAUSE, OTHER
    }

    private static final long serialVersionUID=-4513910129515151732L;

    public static final Atom[][] EMPTY_HEAD=new Atom[0][];

    protected final boolean m_isKnownToBeAdmissible;
    protected final Atom[] m_headAtoms;
    protected final Atom[] m_bodyAtoms;
    protected final ClauseType m_clauseType;

    protected DLClause(boolean isKnownToBeAdmissible,Atom[] headAtoms,Atom[] bodyAtoms,ClauseType clauseType) {
        m_isKnownToBeAdmissible=isKnownToBeAdmissible;
        m_headAtoms=headAtoms;
        m_bodyAtoms=bodyAtoms;
        m_clauseType=clauseType;
    }
    public boolean isKnownToBeAdmissible() {
        return m_isKnownToBeAdmissible;
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
    public DLClause getSafeVersion() {
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
            DLPredicate thingConcept=(m_clauseType==ClauseType.DATA_RANGE_INCLUSION ? InternalDatatype.RDFS_LITERAL : AtomicConcept.THING);
            for (Variable variable : variables)
                newBodyAtoms[index++]=Atom.create(thingConcept,variable);
            return DLClause.createEx(m_isKnownToBeAdmissible,m_headAtoms,newBodyAtoms,m_clauseType);
        }
    }
    public DLClause getChangedDLClause(Atom[] headAtoms,Atom[] bodyAtoms) {
        if (headAtoms==null)
            headAtoms=m_headAtoms;
        if (bodyAtoms==null)
            bodyAtoms=m_bodyAtoms;
        return DLClause.createEx(m_isKnownToBeAdmissible,headAtoms,bodyAtoms,m_clauseType);
    }
    public boolean isFunctionalityAxiom() {
        if (getBodyLength()==2 && getHeadLength()==1) {
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
    public ClauseType getClauseType() {
        return m_clauseType;
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
    public String toOrderedString(Prefixes prefixes) {
        StringBuffer buffer=new StringBuffer();
        SortedSet<Atom> headAtoms = new TreeSet<Atom>(AtomLexicalComparator.INSTANCE);
        headAtoms.addAll(Arrays.asList(m_headAtoms));
        boolean isFirstAtom = true;
        for (Atom a : headAtoms) {
            if (isFirstAtom) isFirstAtom = false;
            else buffer.append(" v ");
            buffer.append(a.toString(prefixes));
        }
        buffer.append(" :- ");
        SortedSet<Atom> bodyAtoms = new TreeSet<Atom>(AtomLexicalComparator.INSTANCE);
        bodyAtoms.addAll(Arrays.asList(m_bodyAtoms));
        isFirstAtom = true;
        for (Atom a : bodyAtoms) {
            if (isFirstAtom) isFirstAtom = false;
            else buffer.append(", ");
            buffer.append(a.toString(prefixes));
        }
        return buffer.toString();
    }
    public String toString() {
        return toString(Prefixes.STANDARD_PREFIXES);
    }

    public static DLClause create(Atom[] headAtoms,Atom[] bodyAtoms,ClauseType clauseType) {
        return createEx(false,headAtoms,bodyAtoms,clauseType);
    }
    public static DLClause createEx(boolean isKnownToBeAdmissible,Atom[] headAtoms,Atom[] bodyAtoms, ClauseType clauseType) {
        return new DLClause(isKnownToBeAdmissible,headAtoms,bodyAtoms,clauseType);
    }
}
class AtomLexicalComparator implements Serializable, Comparator<Atom> {
    private static final long serialVersionUID = 1734767518260417510L;
    public static final Comparator<Atom> INSTANCE = new AtomLexicalComparator();

    public int compare(Atom a1, Atom a2) {
        if (a1 == a2) return 0;
        int diff = a1.getDLPredicate().toString().compareTo(a2.getDLPredicate().toString());
        if (diff != 0) return diff;
        diff = a1.getArity() - a2.getArity();
        if (diff != 0) return diff;
        for (int i=0;i<a1.getArity();i++) {
            diff = a1.getArgument(i).toString().compareTo(a2.getArgument(i).toString());
            if (diff != 0) return diff;
        }
        return 0;
    }
}
