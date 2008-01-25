package org.semanticweb.HermiT.model;

import java.io.Serializable;
import java.util.Set;
import java.util.HashSet;

import org.semanticweb.HermiT.*;

/**
 * Represents a DL clause. The body is a conjunction of atoms and the head is a disjunction of conjunctions.
 */
public class DLClause implements Serializable {
    private static final long serialVersionUID=-4513910129515151732L;

    public static final Atom[][] EMPTY_HEAD=new Atom[0][];

    protected final Atom[][] m_headConjunctions;
    protected final Atom[] m_bodyAtoms;
    
    protected DLClause(Atom[][] headConjunctions,Atom[] bodyAtoms) {
        m_headConjunctions=headConjunctions;
        m_bodyAtoms=bodyAtoms;
    }
    public int getHeadLength() {
        return m_headConjunctions.length;
    }
    public int getHeadConjunctionLength(int disjunctionIndex) {
        return m_headConjunctions[disjunctionIndex].length;
    }
    public Atom[] getHeadConjunction(int disjunctionIndex) {
        return m_headConjunctions[disjunctionIndex];
    }
    public Atom getHeadAtom(int disjunctionIndex,int conjunctIndex) {
        return m_headConjunctions[disjunctionIndex][conjunctIndex];
    }
    public int getBodyLength() {
        return m_bodyAtoms.length;
    }
    public Atom getBodyAtom(int atomIndex) {
        return m_bodyAtoms[atomIndex];
    }
    public DLClause getSafeVersion() {
        Set<Variable> variables=new HashSet<Variable>();
        for (int disjunctionIndex=0;disjunctionIndex<m_headConjunctions.length;disjunctionIndex++) {
            Atom[] headConjunctions=m_headConjunctions[disjunctionIndex];
            for (int conjunctionIndex=0;conjunctionIndex<headConjunctions.length;conjunctionIndex++) {
                Atom atom=headConjunctions[conjunctionIndex];
                for (int argumentIndex=0;argumentIndex<atom.getArity();argumentIndex++) {
                    Variable variable=atom.getArgumentVariable(argumentIndex);
                    if (variable!=null)
                        variables.add(variable);
                }
            }
        }
        for (int bodyIndex=0;bodyIndex<m_bodyAtoms.length;bodyIndex++) {
            Atom atom=m_bodyAtoms[bodyIndex];
            for (int argumentIndex=0;argumentIndex<atom.getArity();argumentIndex++) {
                Variable variable=atom.getArgumentVariable(argumentIndex);
                if (variable!=null)
                    variables.remove(variable);
            }
        }
        if (variables.isEmpty())
            return this;
        else {
            Atom[] newBodyAtoms=new Atom[m_bodyAtoms.length+variables.size()];
            System.arraycopy(m_bodyAtoms,0,newBodyAtoms,0,m_bodyAtoms.length);
            int index=m_bodyAtoms.length;
            for (Variable variable : variables)
                newBodyAtoms[index++]=Atom.create(AtomicConcept.THING,variable);
            return DLClause.create(m_headConjunctions,newBodyAtoms);
        }
    }
    public DLClause getChangedDLClause(Atom[] bodyAtoms,Atom[][] headConjunctions) {
        if (bodyAtoms==null)
            bodyAtoms=m_bodyAtoms;
        if (headConjunctions==null)
            headConjunctions=m_headConjunctions;
        return DLClause.create(headConjunctions,bodyAtoms);
    }
    public boolean isConceptInclusion() {
        if (getBodyLength()==1 && getHeadLength()==1 && getHeadConjunctionLength(0)==1) {
            if (getBodyAtom(0).getDLPredicate() instanceof AtomicConcept && getHeadAtom(0,0).getDLPredicate() instanceof Concept) {
                Variable x=getBodyAtom(0).getArgumentVariable(0);
                Variable headX=getHeadAtom(0,0).getArgumentVariable(0);
                if (x!=null && x.equals(headX))
                    return true;
            }
        }
        return false;
    }
    public boolean isFunctionalityAxiom() {
        if (getBodyLength()==2 && getHeadLength()==1 && getHeadConjunctionLength(0)==1) {
            DLPredicate atomicAbstractRole=getBodyAtom(0).getDLPredicate();
            if (atomicAbstractRole instanceof AtomicAbstractRole) {
                if (getBodyAtom(1).getDLPredicate().equals(atomicAbstractRole) && getHeadAtom(0,0).getDLPredicate().equals(Equality.INSTANCE)) {
                    Variable x=getBodyAtom(0).getArgumentVariable(0);
                    if (x!=null && x.equals(getBodyAtom(1).getArgument(0))) {
                        Variable y1=getBodyAtom(0).getArgumentVariable(1);
                        Variable y2=getBodyAtom(1).getArgumentVariable(1);
                        Variable headY1=getHeadAtom(0,0).getArgumentVariable(0);
                        Variable headY2=getHeadAtom(0,0).getArgumentVariable(1);
                        if (y1!=null && y2!=null && !y1.equals(y2) && headY1!=null && headY2!=null && ((y1.equals(headY1) && y2.equals(headY2)) || (y1.equals(headY2) && y2.equals(headY1))))
                            return true;
                    }
                }
            }
        }
        return false;
    }
    public boolean isGuardedFunctionalityAxiom() {
        if (getBodyLength()==3 && getHeadLength()==1 && getHeadConjunctionLength(0)==1) {
            DLPredicate guard=getBodyAtom(0).getDLPredicate();
            if (guard instanceof AtMostAbstractRoleGuard) {
                AtMostAbstractRoleGuard atMostAbstractRoleGuard=(AtMostAbstractRoleGuard)guard;
                if (atMostAbstractRoleGuard.getToAtomicConcept().equals(AtomicConcept.THING)) {
                    Variable x=getBodyAtom(0).getArgumentVariable(0);
                    DLPredicate atomicAbstractRole=getBodyAtom(1).getDLPredicate();
                    if (atomicAbstractRole instanceof AtomicAbstractRole && atMostAbstractRoleGuard.getOnAbstractRole().equals(atomicAbstractRole)) {
                        if (getBodyAtom(2).getDLPredicate().equals(atomicAbstractRole) && getHeadAtom(0,0).getDLPredicate().equals(Equality.INSTANCE)) {
                            if (x!=null && x.equals(getBodyAtom(1).getArgument(0)) && x.equals(getBodyAtom(2).getArgument(0))) {
                                Variable y1=getBodyAtom(1).getArgumentVariable(1);
                                Variable y2=getBodyAtom(2).getArgumentVariable(1);
                                Variable headY1=getHeadAtom(0,0).getArgumentVariable(0);
                                Variable headY2=getHeadAtom(0,0).getArgumentVariable(1);
                                if (y1!=null && y2!=null && !y1.equals(y2) && headY1!=null && headY2!=null && ((y1.equals(headY1) && y2.equals(headY2)) || (y1.equals(headY2) && y2.equals(headY1))))
                                    return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
    public boolean isInverseFunctionalityAxiom() {
        if (getBodyLength()==2 && getHeadLength()==1 && getHeadConjunctionLength(0)==1) {
            DLPredicate atomicAbstractRole=getBodyAtom(0).getDLPredicate();
            if (atomicAbstractRole instanceof AtomicAbstractRole) {
                if (getBodyAtom(1).getDLPredicate().equals(atomicAbstractRole) && getHeadAtom(0,0).getDLPredicate().equals(Equality.INSTANCE)) {
                    Variable x=getBodyAtom(0).getArgumentVariable(1);
                    if (x!=null && x.equals(getBodyAtom(1).getArgument(1))) {
                        Variable y1=getBodyAtom(0).getArgumentVariable(0);
                        Variable y2=getBodyAtom(1).getArgumentVariable(0);
                        Variable headY1=getHeadAtom(0,0).getArgumentVariable(0);
                        Variable headY2=getHeadAtom(0,0).getArgumentVariable(1);
                        if (y1!=null && y2!=null && !y1.equals(y2) && headY1!=null && headY2!=null && ((y1.equals(headY1) && y2.equals(headY2)) || (y1.equals(headY2) && y2.equals(headY1))))
                            return true;
                    }
                }
            }
        }
        return false;
    }
    public boolean isGuardedInverseFunctionalityAxiom() {
        if (getBodyLength()==3 && getHeadLength()==1 && getHeadConjunctionLength(0)==1) {
            DLPredicate guard=getBodyAtom(0).getDLPredicate();
            if (guard instanceof AtMostAbstractRoleGuard) {
                AtMostAbstractRoleGuard atMostAbstractRoleGuard=(AtMostAbstractRoleGuard)guard;
                if (atMostAbstractRoleGuard.getToAtomicConcept().equals(AtomicConcept.THING)) {
                    Variable x=getBodyAtom(0).getArgumentVariable(0);
                    DLPredicate atomicAbstractRole=getBodyAtom(1).getDLPredicate();
                    if (atomicAbstractRole instanceof AtomicAbstractRole && atMostAbstractRoleGuard.getOnAbstractRole().getInverseRole().equals(atomicAbstractRole)) {
                        if (getBodyAtom(2).getDLPredicate().equals(atomicAbstractRole) && getHeadAtom(0,0).getDLPredicate().equals(Equality.INSTANCE)) {
                            if (x!=null && x.equals(getBodyAtom(1).getArgument(1)) && x.equals(getBodyAtom(2).getArgument(1))) {
                                Variable y1=getBodyAtom(1).getArgumentVariable(0);
                                Variable y2=getBodyAtom(2).getArgumentVariable(0);
                                Variable headY1=getHeadAtom(0,0).getArgumentVariable(0);
                                Variable headY2=getHeadAtom(0,0).getArgumentVariable(1);
                                if (y1!=null && y2!=null && !y1.equals(y2) && headY1!=null && headY2!=null && ((y1.equals(headY1) && y2.equals(headY2)) || (y1.equals(headY2) && y2.equals(headY1))))
                                    return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
    public boolean isRoleInclusion() {
        if (getBodyLength()==1 && getHeadLength()==1 && getHeadConjunctionLength(0)==1) {
            if (getBodyAtom(0).getDLPredicate() instanceof AtomicAbstractRole && getHeadAtom(0,0).getDLPredicate() instanceof AtomicAbstractRole) {
                Variable x=getBodyAtom(0).getArgumentVariable(0);
                Variable y=getBodyAtom(0).getArgumentVariable(1);
                Variable headX=getHeadAtom(0,0).getArgumentVariable(0);
                Variable headY=getHeadAtom(0,0).getArgumentVariable(1);
                if (x!=null && y!=null && !x.equals(y) && x.equals(headX) && y.equals(headY))
                    return true;
            }
        }
        return false;
    }
    public boolean isRoleInverseInclusion() {
        if (getBodyLength()==1 && getHeadLength()==1 && getHeadConjunctionLength(0)==1) {
            if (getBodyAtom(0).getDLPredicate() instanceof AtomicAbstractRole && getHeadAtom(0,0).getDLPredicate() instanceof AtomicAbstractRole) {
                Variable x=getBodyAtom(0).getArgumentVariable(0);
                Variable y=getBodyAtom(0).getArgumentVariable(1);
                Variable headX=getHeadAtom(0,0).getArgumentVariable(0);
                Variable headY=getHeadAtom(0,0).getArgumentVariable(1);
                if (x!=null && y!=null && !x.equals(y) && x.equals(headY) && y.equals(headX))
                    return true;
            }
        }
        return false;
    }
    public String toString(Namespaces namespaces) {
        StringBuffer buffer=new StringBuffer();
        for (int headConjunctionIndex=0;headConjunctionIndex<m_headConjunctions.length;headConjunctionIndex++) {
            if (headConjunctionIndex!=0)
                buffer.append(" v ");
            Atom[] headConjunction=m_headConjunctions[headConjunctionIndex];
            if (headConjunction.length==1)
                buffer.append(headConjunction[0].toString(namespaces));
            else {
                buffer.append('[');
                for (int conjunctIndex=0;conjunctIndex<headConjunction.length;conjunctIndex++) {
                    if (conjunctIndex!=0)
                        buffer.append(" & ");
                    buffer.append(headConjunction[conjunctIndex].toString(namespaces));
                }
                buffer.append(']');
            }
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
        return toString(Namespaces.INSTANCE);
    }
    protected Object readResolve() {
        return s_interningManager.intern(this);
    }

    protected static InterningManager<DLClause> s_interningManager=new InterningManager<DLClause>() {
        protected boolean equal(DLClause object1,DLClause object2) {
            if (object1.m_headConjunctions.length!=object2.m_headConjunctions.length || object1.m_bodyAtoms.length!=object2.m_bodyAtoms.length)
                return false;
            for (int headConjunctionIndex=object1.m_headConjunctions.length-1;headConjunctionIndex>=0;--headConjunctionIndex) {
                Atom[] headConjunction1=object1.m_headConjunctions[headConjunctionIndex];
                Atom[] headConjunction2=object2.m_headConjunctions[headConjunctionIndex];
                if (headConjunction1.length!=headConjunction2.length)
                    return false;
                for (int conjunctIndex=0;conjunctIndex<headConjunction1.length;conjunctIndex++)
                    if (headConjunction1[conjunctIndex]!=headConjunction2[conjunctIndex])
                        return false;
            }
            for (int bodyIndex=object1.m_bodyAtoms.length-1;bodyIndex>=0;--bodyIndex)
                if (object1.m_bodyAtoms[bodyIndex]!=object2.m_bodyAtoms[bodyIndex])
                    return false;
            return true;
        }
        protected int getHashCode(DLClause object) {
            int hashCode=0;
            for (int headConjunctionIndex=object.m_headConjunctions.length-1;headConjunctionIndex>=0;--headConjunctionIndex) {
                Atom[] headConjunction=object.m_headConjunctions[headConjunctionIndex];
                for (int conjunctIndex=0;conjunctIndex<headConjunction.length;conjunctIndex++) {
                    hashCode+=headConjunction[conjunctIndex].hashCode();
                }
            }
            for (int bodyIndex=object.m_bodyAtoms.length-1;bodyIndex>=0;--bodyIndex)
                hashCode+=object.m_bodyAtoms[bodyIndex].hashCode();
            return hashCode;
        }
    };
    
    public static DLClause create(Atom[][] headConjunctions,Atom[] bodyAtoms) {
        return s_interningManager.intern(new DLClause(headConjunctions,bodyAtoms));
    }
}
