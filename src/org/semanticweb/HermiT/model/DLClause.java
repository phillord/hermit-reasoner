// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.HermiT.Namespaces;

/**
 * Represents a DL clause. The body is a conjunction of atoms and the head is a disjunction of atoms.
 */
public class DLClause implements Serializable {
    private static final long serialVersionUID=-4513910129515151732L;

    public static final Atom[][] EMPTY_HEAD=new Atom[0][];

    protected final boolean m_isKnownToBeAdmissible;
    protected final Atom[] m_headAtoms;
    protected final Atom[] m_bodyAtoms;

    protected DLClause(boolean isKnownToBeAdmissible,Atom[] headAtoms,Atom[] bodyAtoms) {
        m_isKnownToBeAdmissible=isKnownToBeAdmissible;
        m_headAtoms=headAtoms;
        m_bodyAtoms=bodyAtoms;
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
    public int getBodyLength() {
        return m_bodyAtoms.length;
    }
    public Atom getBodyAtom(int atomIndex) {
        return m_bodyAtoms[atomIndex];
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
            for (Variable variable : variables)
                newBodyAtoms[index++]=Atom.create(AtomicConcept.THING,variable);
            return DLClause.createEx(m_isKnownToBeAdmissible,m_headAtoms,newBodyAtoms);
        }
    }
    public DLClause getChangedDLClause(Atom[] headAtoms,Atom[] bodyAtoms) {
        if (headAtoms==null)
            headAtoms=m_headAtoms;
        if (bodyAtoms==null)
            bodyAtoms=m_bodyAtoms;
        return DLClause.createEx(m_isKnownToBeAdmissible,headAtoms,bodyAtoms);
    }
    public boolean isConceptInclusion() {
        if (getBodyLength()==1 && getHeadLength()==1) {
            if (getBodyAtom(0).getDLPredicate() instanceof AtomicConcept && getHeadAtom(0).getDLPredicate() instanceof Concept) {
                Variable x=getBodyAtom(0).getArgumentVariable(0);
                Variable headX=getHeadAtom(0).getArgumentVariable(0);
                if (x!=null && x.equals(headX))
                    return true;
            }
        }
        return false;
    }
    public boolean isFunctionalityAxiom() {
        if (getBodyLength()==2 && getHeadLength()==1) {
            DLPredicate atomicRole=getBodyAtom(0).getDLPredicate();
            if (atomicRole instanceof AtomicRole) {
                if (getBodyAtom(1).getDLPredicate().equals(atomicRole) && getHeadAtom(0).getDLPredicate().equals(Equality.INSTANCE)) {
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
    public boolean isGuardedFunctionalityAxiom() {
        if (getBodyLength()==1 && getHeadLength()==1) {
            DLPredicate headDLPredicate=getHeadAtom(0).getDLPredicate();
            if (headDLPredicate instanceof AtMostGuard) {
                AtMostGuard atMostRoleGuard=(AtMostGuard)headDLPredicate;
                if (atMostRoleGuard.getCaridnality()==1 && atMostRoleGuard.getToAtomicConcept().equals(AtomicConcept.THING) && atMostRoleGuard.getOnRole() instanceof AtomicRole) {
                    AtomicRole atomicRole=(AtomicRole)atMostRoleGuard.getOnRole();
                    Variable x=getHeadAtom(0).getArgumentVariable(0);
                    Atom bodyAtom=getBodyAtom(0);
                    if (x!=null && bodyAtom.getDLPredicate().equals(atomicRole) && bodyAtom.getArgument(0).equals(x) && bodyAtom.getArgument(1) instanceof Variable && !bodyAtom.getArgument(1).equals(x))
                        return true;
                }
            }
        }
        return false;
    }
    public boolean isInverseFunctionalityAxiom() {
        if (getBodyLength()==2 && getHeadLength()==1) {
            DLPredicate atomicRole=getBodyAtom(0).getDLPredicate();
            if (atomicRole instanceof AtomicRole) {
                if (getBodyAtom(1).getDLPredicate().equals(atomicRole) && getHeadAtom(0).getDLPredicate().equals(Equality.INSTANCE)) {
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
    public boolean isGuardedInverseFunctionalityAxiom() {
        if (getBodyLength()==1 && getHeadLength()==1) {
            DLPredicate headDLPredicate=getHeadAtom(0).getDLPredicate();
            if (headDLPredicate instanceof AtMostGuard) {
                AtMostGuard atMostRoleGuard=(AtMostGuard)headDLPredicate;
                if (atMostRoleGuard.getCaridnality()==1 && atMostRoleGuard.getToAtomicConcept().equals(AtomicConcept.THING) && atMostRoleGuard.getOnRole() instanceof InverseRole) {
                    AtomicRole atomicRole=((InverseRole)atMostRoleGuard.getOnRole()).getInverseOf();
                    Variable x=getHeadAtom(0).getArgumentVariable(0);
                    Atom bodyAtom=getBodyAtom(0);
                    if (x!=null && bodyAtom.getDLPredicate().equals(atomicRole) && bodyAtom.getArgument(1).equals(x) && bodyAtom.getArgument(0) instanceof Variable && !bodyAtom.getArgument(0).equals(x))
                        return true;
                }
            }
        }
        return false;
    }
    public boolean isRoleInclusion() {
        if (getBodyLength()==1 && getHeadLength()==1) {
            if (getBodyAtom(0).getDLPredicate() instanceof AtomicRole && getHeadAtom(0).getDLPredicate() instanceof AtomicRole) {
                Variable x=getBodyAtom(0).getArgumentVariable(0);
                Variable y=getBodyAtom(0).getArgumentVariable(1);
                Variable headX=getHeadAtom(0).getArgumentVariable(0);
                Variable headY=getHeadAtom(0).getArgumentVariable(1);
                if (x!=null && y!=null && !x.equals(y) && x.equals(headX) && y.equals(headY))
                    return true;
            }
        }
        return false;
    }
    public boolean isRoleInverseInclusion() {
        if (getBodyLength()==1 && getHeadLength()==1) {
            if (getBodyAtom(0).getDLPredicate() instanceof AtomicRole && getHeadAtom(0).getDLPredicate() instanceof AtomicRole) {
                Variable x=getBodyAtom(0).getArgumentVariable(0);
                Variable y=getBodyAtom(0).getArgumentVariable(1);
                Variable headX=getHeadAtom(0).getArgumentVariable(0);
                Variable headY=getHeadAtom(0).getArgumentVariable(1);
                if (x!=null && y!=null && !x.equals(y) && x.equals(headY) && y.equals(headX))
                    return true;
            }
        }
        return false;
    }
    public String toString(Namespaces namespaces) {
        StringBuffer buffer=new StringBuffer();
        for (int headIndex=0;headIndex<m_headAtoms.length;headIndex++) {
            if (headIndex!=0)
                buffer.append(" v ");
            buffer.append(m_headAtoms[headIndex].toString(namespaces));
        }
        buffer.append(" :- ");
        for (int bodyIndex=0;bodyIndex<m_bodyAtoms.length;bodyIndex++) {
            if (bodyIndex!=0)
                buffer.append(", ");
            buffer.append(m_bodyAtoms[bodyIndex].toString(namespaces));
        }
        return buffer.toString();
    }
    public String toString() {
        return toString(Namespaces.EMPTY);
    }

    public static DLClause create(Atom[] headAtoms,Atom[] bodyAtoms) {
        return createEx(false,headAtoms,bodyAtoms);
    }
    public static DLClause createEx(boolean isKnownToBeAdmissible,Atom[] headAtoms,Atom[] bodyAtoms) {
        return new DLClause(isKnownToBeAdmissible,headAtoms,bodyAtoms);
    }
}
