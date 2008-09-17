// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.debugger;

import java.io.Serializable;

import java.util.Map;
import java.util.HashMap;
import java.util.Stack;

import org.semanticweb.HermiT.*;
import org.semanticweb.HermiT.model.*;
import org.semanticweb.HermiT.monitor.*;
import org.semanticweb.HermiT.tableau.*;

public class DerivationHistory extends TableauMonitorAdapter {
    private static final long serialVersionUID=-3963478091986772947L;

    protected static final Object[] EMPTY_TUPLE=new Object[0];
    
    protected final Map<AtomKey,Atom> m_derivedAtoms;
    protected final Map<GroundDisjunction,Disjunction> m_derivedDisjunctions;
    protected final Stack<Derivation> m_derivations;
    protected final Stack<Atom> m_mergeAtoms;

    public DerivationHistory() {
        m_derivedAtoms=new HashMap<AtomKey,Atom>();
        m_derivedDisjunctions=new HashMap<GroundDisjunction,Disjunction>();
        m_derivations=new Stack<Derivation>();
        m_mergeAtoms=new Stack<Atom>();
    }
    public void tableauCleared() {
        m_derivedAtoms.clear();
        m_derivedDisjunctions.clear();
        m_derivations.clear();
        m_derivations.push(BaseFact.INSTANCE);
        m_mergeAtoms.clear();
    }
    public void dlClauseMatchedStarted(DLClauseEvaluator dlClauseEvaluator,int dlClauseIndex) {
        DerivationPremise[] premises=new DerivationPremise[dlClauseEvaluator.getBodyLength()];
        for (int index=0;index<premises.length;index++)
            premises[index]=new DerivationPremise(getAtom(dlClauseEvaluator.getTupleMatchedToBody(index)));
        m_derivations.push(new DLClauseApplication(dlClauseEvaluator.getDLClause(dlClauseIndex),premises));
    }
    public void dlClauseMatchedFinished(DLClauseEvaluator dlClauseEvaluator) {
        m_derivations.pop();
    }
    public void addFactFinished(Object[] tuple,boolean factAdded) {
        if (factAdded)
            addAtom(tuple);
    }
    public void mergeStarted(Node nodeFrom,Node nodeInto) {
        Atom equalityAtom=addAtom(new Object[] { Equality.INSTANCE,nodeFrom,nodeInto });
        m_mergeAtoms.add(equalityAtom);
    }
    public void mergeFactStarted(Node mergeFrom,Node mergeInto,Object[] sourceTuple,Object[] targetTuple) {
        m_derivations.push(new Merging(m_mergeAtoms.peek(),getAtom(sourceTuple)));
    }
    public void mergeFactFinished(Node mergeFrom,Node mergeInto,Object[] sourceTuple,Object[] targetTuple) {
        m_derivations.pop();
    }
    public void mergeFinished(Node nodeFrom,Node nodeInto) {
        m_mergeAtoms.pop();
    }
    public void mergeGraphsStarted(Object[] graph1,Object[] graph2,int position) {
        Atom graphAtom1=getAtom(graph1);
        Atom graphAtom2=getAtom(graph2);
        m_derivations.add(new GraphMerging(graphAtom1,graphAtom2,position));
    }
    public void mergeGraphsFinished(Object[] graph1,Object[] graph2,int position) {
        m_derivations.pop();
    }
    public void clashDetected(Object[]... causes) {
        DerivationPremise[] atoms=new DerivationPremise[causes.length];
        for (int index=0;index<causes.length;index++)
            atoms[index]=new DerivationPremise(getAtom(causes[index]));
        m_derivations.push(new ClashDetection(atoms));
        addAtom(EMPTY_TUPLE);
        m_derivations.pop();
    }
    public void tupleRemoved(Object[] tuple) {
        m_derivedAtoms.remove(new AtomKey(tuple));
    }
    public void backtrackToFinished(BranchingPoint newCurrentBrancingPoint) {
        m_derivedAtoms.remove(new AtomKey(EMPTY_TUPLE));
    }
    public void groundDisjunctionDerived(GroundDisjunction groundDisjunction) {
        Disjunction disjunction=new Disjunction(groundDisjunction,m_derivations.peek());
        m_derivedDisjunctions.put(groundDisjunction,disjunction);
    }
    public void disjunctProcessingStarted(GroundDisjunction groundDisjunction,int disjunct) {
        Disjunction disjunction=getDisjunction(groundDisjunction);
        m_derivations.push(new DisjunctApplication(disjunction,disjunct));
    }
    public void disjunctProcessingFinished(GroundDisjunction groundDisjunction,int disjunct) {
        m_derivations.pop();
    }
    public void existentialExpansionStarted(ExistentialConcept existentialConcept,Node forNode) {
        Atom existentialAtom=getAtom(new Object[] { existentialConcept,forNode });
        m_derivations.push(new ExistentialExpansion(existentialAtom));
    }
    public void existentialExpansionFinished(ExistentialConcept existentialConcept,Node forNode) {
        m_derivations.pop();
    }
    public Atom getAtom(Object[] tuple) {
        return m_derivedAtoms.get(new AtomKey(tuple));
    }
    public Disjunction getDisjunction(GroundDisjunction groundDisjunction) {
        return m_derivedDisjunctions.get(groundDisjunction);
    }
    protected Atom addAtom(Object[] tuple) {
        Object[] clonedTuple=tuple.clone();
        Atom newAtom=new Atom(clonedTuple,m_derivations.peek());
        m_derivedAtoms.put(new AtomKey(clonedTuple),newAtom);
        return newAtom;
    }
    
    protected static class AtomKey implements Serializable {
        private static final long serialVersionUID=1409033744982881556L;

        protected final Object[] m_tuple;
        protected final int m_hashCode;
        
        public AtomKey(Object[] tuple) {
            m_tuple=tuple;
            int hashCode=0;
            for (int index=0;index<tuple.length;index++)
                hashCode+=tuple[index].hashCode();
            m_hashCode=hashCode;
        }
        public int hashCode() {
            return m_hashCode;
        }
        public boolean equals(Object that) {
            if (this==that)
                return true;
            if (!(that instanceof AtomKey))
                return false;
            AtomKey thatAtomKey=(AtomKey)that;
            if (m_tuple.length!=thatAtomKey.m_tuple.length)
                return false;
            for (int index=0;index<m_tuple.length;index++)
                if (!m_tuple[index].equals(thatAtomKey.m_tuple[index]))
                    return false;
            return true;
        }
    }
    
    protected static interface Fact extends Serializable {
        String toString(Namespaces namespaces);
        Derivation getDerivation();
    }
    
    public static class Atom implements Fact {
        private static final long serialVersionUID=-6136317748590721560L;

        protected final Object[] m_tuple;
        protected final Derivation m_derivedBy;
        
        public Atom(Object[] tuple,Derivation derivedBy) {
            m_tuple=tuple;
            m_derivedBy=derivedBy;
        }
        public Object getDLPredicate() {
            return m_tuple[0];
        }
        public int getArity() {
            return m_tuple.length-1;
        }
        public Node getArgument(int index) {
            return (Node)m_tuple[index+1];
        }
        public Derivation getDerivation() {
            return m_derivedBy;
        }
        public String toString(Namespaces namespaces) {
            if (m_tuple.length==0)
                return "[ ]";
            else {
                StringBuffer buffer=new StringBuffer();
                Object dlPredicate=getDLPredicate();
                if (org.semanticweb.HermiT.model.Atom.s_infixPredicates.contains(dlPredicate)) {
                    buffer.append(getArgument(0).getNodeID());
                    buffer.append(' ');
                    buffer.append(((DLPredicate)dlPredicate).toString(namespaces));
                    buffer.append(' ');
                    buffer.append(getArgument(1).getNodeID());
                }
                else {
                    if (dlPredicate instanceof DLPredicate)
                        buffer.append(((DLPredicate)dlPredicate).toString(namespaces));
                    else if (dlPredicate instanceof Concept)
                        buffer.append(((Concept)dlPredicate).toString(namespaces));
                    else
                        throw new IllegalStateException("Internal error: invalid DL-predicate.");
                    buffer.append('(');
                    for (int argumentIndex=0;argumentIndex<getArity();argumentIndex++) {
                        if (argumentIndex!=0)
                            buffer.append(',');
                        buffer.append(getArgument(argumentIndex).getNodeID());
                    }
                    buffer.append(')');
                }
                return buffer.toString();
            }
        }
        public String toString() {
            return toString(Namespaces.none);
        }
    }
    
    public static class Disjunction implements Fact {
        private static final long serialVersionUID=-6645342875287836609L;

        protected final Object[][] m_atoms;
        protected final Derivation m_derivedBy;
        
        public Disjunction(GroundDisjunction groundDisjunction,Derivation derivedBy) {
            m_atoms=new Object[groundDisjunction.getNumberOfDisjuncts()][];
            for (int disjunctIndex=0;disjunctIndex<groundDisjunction.getNumberOfDisjuncts();disjunctIndex++) {
                DLPredicate dlPredicate=groundDisjunction.getDLPredicate(disjunctIndex);
                Object[] tuple=new Object[dlPredicate.getArity()+1];
                tuple[0]=dlPredicate;
                for (int argumentIndex=0;argumentIndex<dlPredicate.getArity();argumentIndex++)
                    tuple[argumentIndex+1]=groundDisjunction.getArgument(disjunctIndex,argumentIndex);
                m_atoms[disjunctIndex]=tuple;
            }
            m_derivedBy=derivedBy;
        }
        public int getNumberOfDisjuncts() {
            return m_atoms.length;
        }
        public Object getDLPredicate(int disjunctIndex) {
            return m_atoms[disjunctIndex][0];
        }
        public Node getArgument(int disjunctIndex,int argumentIndex) {
            return (Node)m_atoms[disjunctIndex][argumentIndex+1];
        }
        public Derivation getDerivation() {
            return m_derivedBy;
        }
        public String toString(Namespaces namespaces) {
            StringBuffer buffer=new StringBuffer();
            for (int disjunctIndex=0;disjunctIndex<m_atoms.length;disjunctIndex++) {
                if (disjunctIndex!=0)
                    buffer.append(" v ");
                Object[] tuple=m_atoms[disjunctIndex];
                if (tuple[0] instanceof DLPredicate)
                    buffer.append(((DLPredicate)tuple[0]).toString(namespaces));
                else if (tuple[0] instanceof Concept)
                    buffer.append(((Concept)tuple[0]).toString(namespaces));
                else
                    throw new IllegalStateException("Internal error: invalid DL-predicate.");
                buffer.append('(');
                for (int argumentIndex=1;argumentIndex<tuple.length;argumentIndex++) {
                    if (argumentIndex!=1)
                        buffer.append(',');
                    buffer.append(((Node)tuple[argumentIndex]).getNodeID());
                }
                buffer.append(')');
            }
            return buffer.toString();
        }
        public String toString() {
            return toString(Namespaces.none);
        }
    }
    
    @SuppressWarnings("serial")
    public abstract static class Derivation implements Serializable {
        public abstract String toString(Namespaces namespaces);
        public String toString() {
            return toString(Namespaces.none);
        }
        public abstract int getNumberOfPremises();
        public abstract DerivationPremise getPremise(int premiseIndex);
    }

    public static class DerivationPremise implements Serializable {
        private static final long serialVersionUID=387225349849122349L;

        protected final Fact m_fact;
        
        public DerivationPremise(Fact fact) {
            m_fact=fact;
        }
        public Fact getFact() {
            return m_fact;
        }
    }
    
    public static class DLClauseApplication extends Derivation {
        private static final long serialVersionUID=5841561027229354512L;

        protected final DLClause m_dlClause;
        protected final DerivationPremise[] m_premises;
        
        public DLClauseApplication(DLClause dlClause,DerivationPremise[] premises) {
            m_dlClause=dlClause;
            m_premises=premises;
        }
        public DLClause getDLClause() {
            return m_dlClause;
        }
        public int getNumberOfPremises() {
            return m_premises.length;
        }
        public DerivationPremise getPremise(int premiseIndex) {
            return m_premises[premiseIndex];
        }
        public Atom getPremiseAtom(int premiseIndex) {
            return (Atom)m_premises[premiseIndex].getFact();
        }
        public String toString(Namespaces namespaces) {
            return "  <--  "+m_dlClause.toString(namespaces);
        }
    }

    public static class DisjunctApplication extends Derivation {
        private static final long serialVersionUID=6657356873675430986L;

        protected final DerivationPremise m_disjunction;
        protected final int m_disjunctIndex;
        
        public DisjunctApplication(Disjunction disjunction,int disjunctIndex) {
            m_disjunction=new DerivationPremise(disjunction);
            m_disjunctIndex=disjunctIndex;
        }
        public Disjunction getDisjunction() {
            return (Disjunction)m_disjunction.getFact();
        }
        public int getDisjunctIndex() {
            return m_disjunctIndex;
        }
        public int getNumberOfPremises() {
            return 1;
        }
        public DerivationPremise getPremise(int premiseIndex) {
            switch (premiseIndex) {
            case 0:
                return m_disjunction;
            default:
                throw new IndexOutOfBoundsException();
            }
        }
        public String toString(Namespaces namespaces) {
            return "  |  "+String.valueOf(m_disjunctIndex);
        }
    }
    
    public static class Merging extends Derivation {
        private static final long serialVersionUID=6815119442652251306L;

        protected final DerivationPremise m_equality;
        protected final DerivationPremise m_fromAtom;
        
        public Merging(Atom equality,Atom fromAtom) {
            m_equality=new DerivationPremise(equality);
            m_fromAtom=new DerivationPremise(fromAtom);
        }
        public Atom getEquality() {
            return (Atom)m_equality.getFact();
        }
        public Atom getFromAtom() {
            return (Atom)m_fromAtom.getFact();
        }
        public int getNumberOfPremises() {
            return 2;
        }
        public DerivationPremise getPremise(int premiseIndex) {
            switch (premiseIndex) {
            case 0:
                return m_equality;
            case 1:
                return m_fromAtom;
            default:
                throw new IndexOutOfBoundsException();
            }
        }
        public String toString(Namespaces namespaces) {
            return "   <--|   ";
        }
    }
    
    public static class GraphMerging extends Derivation {
        private static final long serialVersionUID=-3671522413313454739L;

        protected final DerivationPremise m_graph1;
        protected final DerivationPremise m_graph2;
        protected final int m_position;
        
        public GraphMerging(Atom graph1,Atom graph2,int position) {
            m_graph1=new DerivationPremise(graph1);
            m_graph2=new DerivationPremise(graph2);
            m_position=position;
        }
        public Atom getGraph1() {
            return (Atom)m_graph1.getFact();
        }
        public Atom getGraph2() {
            return (Atom)m_graph2.getFact();
        }
        public int getNumberOfPremises() {
            return 2;
        }
        public DerivationPremise getPremise(int premiseIndex) {
            switch (premiseIndex) {
            case 0:
                return m_graph1;
            case 1:
                return m_graph2;
            default:
                throw new IndexOutOfBoundsException();
            }
        }
        public String toString(Namespaces namespaces) {
            return "   <--G--| @ "+m_position;
        }
    }
    
    public static class ExistentialExpansion extends Derivation {
        private static final long serialVersionUID=-1266097745277870260L;

        protected final DerivationPremise m_existentialAtom;
        
        public ExistentialExpansion(Atom existentialAtom) {
            m_existentialAtom=new DerivationPremise(existentialAtom);
        }
        public int getNumberOfPremises() {
            return 1;
        }
        public Atom getExistentialAtom() {
            return (Atom)m_existentialAtom.getFact();
        }
        public DerivationPremise getPremise(int premiseIndex) {
            switch (premiseIndex) {
            case 0:
                return m_existentialAtom;
            default:
                throw new IndexOutOfBoundsException();
            }
        }
        public String toString(Namespaces namespaces) {
            return " <<  EXISTS";
        }
    }
    
    public static class ClashDetection extends Derivation {
        private static final long serialVersionUID=-1046733682276190587L;
        protected final DerivationPremise[] m_causes;
        
        public ClashDetection(DerivationPremise[] causes) {
            m_causes=causes;
        }
        public int getNumberOfPremises() {
            return m_causes.length;
        }
        public DerivationPremise getPremise(int premiseIndex) {
            return m_causes[premiseIndex];
        }
        public Atom getPremiseAtom(int premiseIndex) {
            return (Atom)m_causes[premiseIndex].getFact();
        }
        public String toString(Namespaces namespaces) {
            return "   << Clash!";
        }
    }

    public static class BaseFact extends Derivation {
        private static final long serialVersionUID=-5998349862414502218L;

        public static Derivation INSTANCE=new BaseFact();

        public int getNumberOfPremises() {
            return 0;
        }
        public DerivationPremise getPremise(int premiseIndex) {
            throw new IndexOutOfBoundsException();
        }
        public String toString(Namespaces namespaces) {
            return ".";
        }
    }
}
