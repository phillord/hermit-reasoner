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
package org.semanticweb.HermiT.debugger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.semanticweb.HermiT.Prefixes;
import org.semanticweb.HermiT.model.Concept;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLPredicate;
import org.semanticweb.HermiT.model.DataRange;
import org.semanticweb.HermiT.model.ConstantEnumeration;
import org.semanticweb.HermiT.model.DatatypeRestriction;
import org.semanticweb.HermiT.model.Equality;
import org.semanticweb.HermiT.model.ExistentialConcept;
import org.semanticweb.HermiT.model.Inequality;
import org.semanticweb.HermiT.model.NodeIDLessEqualThan;
import org.semanticweb.HermiT.model.NodeIDsAscendingOrEqual;
import org.semanticweb.HermiT.monitor.TableauMonitorAdapter;
import org.semanticweb.HermiT.tableau.BranchingPoint;
import org.semanticweb.HermiT.tableau.DLClauseEvaluator;
import org.semanticweb.HermiT.tableau.DatatypeManager;
import org.semanticweb.HermiT.tableau.GroundDisjunction;
import org.semanticweb.HermiT.tableau.Node;

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
        int regularBodyAtomsNumber=0;
        for (int index=0;index<dlClauseEvaluator.getBodyLength();index++) {
            DLPredicate dlPredicate=dlClauseEvaluator.getBodyAtom(index).getDLPredicate();
            if (!(dlPredicate instanceof NodeIDLessEqualThan) && !(dlPredicate instanceof NodeIDsAscendingOrEqual))
                regularBodyAtomsNumber++;
        }
        Atom[] premises=new Atom[regularBodyAtomsNumber];
        int atomIndex=0;
        for (int index=0;index<premises.length;index++) {
            DLPredicate dlPredicate=dlClauseEvaluator.getBodyAtom(index).getDLPredicate();
            if (!(dlPredicate instanceof NodeIDLessEqualThan) || !(dlPredicate instanceof NodeIDsAscendingOrEqual))
                premises[atomIndex++]=getAtom(dlClauseEvaluator.getTupleMatchedToBody(index));
        }
        m_derivations.push(new DLClauseApplication(dlClauseEvaluator.getDLClause(dlClauseIndex),premises));
    }
    public void dlClauseMatchedFinished(DLClauseEvaluator dlClauseEvaluator) {
        m_derivations.pop();
    }
    public void addFactFinished(Object[] tuple,boolean isCore,boolean factAdded) {
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
    public void clashDetectionStarted(Object[]... tuples) {
        Atom[] atoms=new Atom[tuples.length];
        for (int index=0;index<tuples.length;index++)
            atoms[index]=getAtom(tuples[index]);
        m_derivations.push(new ClashDetection(atoms));
    }
    public void clashDetectionFinished(Object[]... tuples) {
        m_derivations.pop();
    }
    public void clashDetected() {
        addAtom(EMPTY_TUPLE);
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
    public void descriptionGraphCheckingStarted(int graphIndex1,int tupleIndex1,int position1,int graphIndex2,int tupleIndex2,int position2) {
        Atom graph1=getAtom(m_tableau.getDescriptionGraphManager().getDescriptionGraphTuple(graphIndex1,tupleIndex1));
        Atom graph2=getAtom(m_tableau.getDescriptionGraphManager().getDescriptionGraphTuple(graphIndex2,tupleIndex2));
        m_derivations.push(new GraphChecking(graph1,position1,graph2,position2));
    }
    public void descriptionGraphCheckingFinished(int graphIndex1,int tupleIndex1,int position1,int graphIndex2,int tupleIndex2,int position2) {
        m_derivations.pop();
    }
    public void unknownDatatypeRestrictionDetectionStarted(DataRange dataRange1,Node node1,DataRange dataRange2,Node node2) {
        Atom atom1=getAtom(new Object[] { dataRange1,node1 });
        Atom atom2=getAtom(new Object[] { dataRange2,node2 });
        m_derivations.push(new UnknownDatatypeRestrictionDetection(new Atom[] { atom1,atom2 }));
    }
    public void unknownDatatypeRestrictionDetectionFinished(DataRange dataRange1,Node node1, DataRange dataRange2,Node node2) {
        m_derivations.pop();
    }
    public void datatypeConjunctionCheckingStarted(DatatypeManager.DConjunction conjunction) {
        List<Atom> atoms=new ArrayList<Atom>();
        for (DatatypeManager.DVariable variable : conjunction.getActiveVariables()) {
            Node node=variable.getNode();
            for (DatatypeRestriction datatypeRestriction : variable.getPositiveDatatypeRestrictions())
                atoms.add(getAtom(new Object[] { datatypeRestriction,node }));
            for (DatatypeRestriction datatypeRestriction : variable.getNegativeDatatypeRestrictions())
                atoms.add(getAtom(new Object[] { datatypeRestriction.getNegation(),node }));
            for (ConstantEnumeration dataValueEnumeration : variable.getPositiveDataValueEnumerations())
                atoms.add(getAtom(new Object[] { dataValueEnumeration,node }));
            for (ConstantEnumeration dataValueEnumeration : variable.getNegativeDataValueEnumerations())
                atoms.add(getAtom(new Object[] { dataValueEnumeration.getNegation(),node }));
            for (DatatypeManager.DVariable neighborVariable : variable.getUnequalToDirect())
                atoms.add(getAtom(new Object[] { Inequality.INSTANCE,node,neighborVariable.getNode() }));
        }
        Atom[] atomsArray=new Atom[atoms.size()];
        atoms.toArray(atomsArray);
        m_derivations.push(new DatatypeChecking(atomsArray));
    }
    public void datatypeConjunctionCheckingFinished(DatatypeManager.DConjunction conjunction,boolean result) {
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
        String toString(Prefixes prefixes);
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
        public String toString(Prefixes prefixes) {
            if (m_tuple.length==0)
                return "[ ]";
            else {
                StringBuffer buffer=new StringBuffer();
                Object dlPredicate=getDLPredicate();
                if (org.semanticweb.HermiT.model.Atom.s_infixPredicates.contains(dlPredicate)) {
                    buffer.append(getArgument(0).getNodeID());
                    buffer.append(' ');
                    buffer.append(((DLPredicate)dlPredicate).toString(prefixes));
                    buffer.append(' ');
                    buffer.append(getArgument(1).getNodeID());
                }
                else {
                    if (dlPredicate instanceof DLPredicate)
                        buffer.append(((DLPredicate)dlPredicate).toString(prefixes));
                    else if (dlPredicate instanceof Concept)
                        buffer.append(((Concept)dlPredicate).toString(prefixes));
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
            return toString(Prefixes.STANDARD_PREFIXES);
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
        public String toString(Prefixes prefixes) {
            StringBuffer buffer=new StringBuffer();
            for (int disjunctIndex=0;disjunctIndex<m_atoms.length;disjunctIndex++) {
                if (disjunctIndex!=0)
                    buffer.append(" v ");
                Object[] tuple=m_atoms[disjunctIndex];
                if (tuple[0] instanceof DLPredicate)
                    buffer.append(((DLPredicate)tuple[0]).toString(prefixes));
                else if (tuple[0] instanceof Concept)
                    buffer.append(((Concept)tuple[0]).toString(prefixes));
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
            return toString(Prefixes.STANDARD_PREFIXES);
        }
    }

    @SuppressWarnings("serial")
    public abstract static class Derivation implements Serializable {
        public abstract String toString(Prefixes prefixes);
        public String toString() {
            return toString(Prefixes.STANDARD_PREFIXES);
        }
        public abstract int getNumberOfPremises();
        public abstract Fact getPremise(int premiseIndex);
    }

    public static class DLClauseApplication extends Derivation {
        private static final long serialVersionUID=5841561027229354512L;

        protected final DLClause m_dlClause;
        protected final Atom[] m_premises;

        public DLClauseApplication(DLClause dlClause,Atom[] premises) {
            m_dlClause=dlClause;
            m_premises=premises;
        }
        public DLClause getDLClause() {
            return m_dlClause;
        }
        public int getNumberOfPremises() {
            return m_premises.length;
        }
        public Fact getPremise(int premiseIndex) {
            return m_premises[premiseIndex];
        }
        public String toString(Prefixes prefixes) {
            return "  <--  "+m_dlClause.toString(prefixes);
        }
    }

    public static class DisjunctApplication extends Derivation {
        private static final long serialVersionUID=6657356873675430986L;

        protected final Disjunction m_disjunction;
        protected final int m_disjunctIndex;

        public DisjunctApplication(Disjunction disjunction,int disjunctIndex) {
            m_disjunction=disjunction;
            m_disjunctIndex=disjunctIndex;
        }
        public int getDisjunctIndex() {
            return m_disjunctIndex;
        }
        public int getNumberOfPremises() {
            return 1;
        }
        public Fact getPremise(int premiseIndex) {
            switch (premiseIndex) {
            case 0:
                return m_disjunction;
            default:
                throw new IndexOutOfBoundsException();
            }
        }
        public String toString(Prefixes prefixes) {
            return "  |  "+String.valueOf(m_disjunctIndex);
        }
    }

    public static class Merging extends Derivation {
        private static final long serialVersionUID=6815119442652251306L;

        protected final Atom m_equality;
        protected final Atom m_fromAtom;

        public Merging(Atom equality,Atom fromAtom) {
            m_equality=equality;
            m_fromAtom=fromAtom;
        }
        public int getNumberOfPremises() {
            return 2;
        }
        public Fact getPremise(int premiseIndex) {
            switch (premiseIndex) {
            case 0:
                return m_equality;
            case 1:
                return m_fromAtom;
            default:
                throw new IndexOutOfBoundsException();
            }
        }
        public String toString(Prefixes prefixes) {
            return "   <--|";
        }
    }

    public static class GraphChecking extends Derivation {
        private static final long serialVersionUID=-3671522413313454739L;

        protected final Atom m_graph1;
        protected final int m_position1;
        protected final Atom m_graph2;
        protected final int m_position2;

        public GraphChecking(Atom graph1,int position1,Atom graph2,int position2) {
            m_graph1=graph1;
            m_position1=position1;
            m_graph2=graph2;
            m_position2=position2;
        }
        public int getNumberOfPremises() {
            return 2;
        }
        public Fact getPremise(int premiseIndex) {
            switch (premiseIndex) {
            case 0:
                return m_graph1;
            case 1:
                return m_graph2;
            default:
                throw new IndexOutOfBoundsException();
            }
        }
        public String toString(Prefixes prefixes) {
            return "   << DGRAPHS | "+m_position1+" and "+m_position2;
        }
    }

    public static class ExistentialExpansion extends Derivation {
        private static final long serialVersionUID=-1266097745277870260L;

        protected final Atom m_existentialAtom;

        public ExistentialExpansion(Atom existentialAtom) {
            m_existentialAtom=existentialAtom;
        }
        public int getNumberOfPremises() {
            return 1;
        }
        public Fact getPremise(int premiseIndex) {
            switch (premiseIndex) {
            case 0:
                return m_existentialAtom;
            default:
                throw new IndexOutOfBoundsException();
            }
        }
        public String toString(Prefixes prefixes) {
            return " <<  EXISTS";
        }
    }

    public static class ClashDetection extends Derivation {
        private static final long serialVersionUID=-1046733682276190587L;
        protected final Atom[] m_causes;

        public ClashDetection(Atom[] causes) {
            m_causes=causes;
        }
        public int getNumberOfPremises() {
            return m_causes.length;
        }
        public Fact getPremise(int premiseIndex) {
            return m_causes[premiseIndex];
        }
        public String toString(Prefixes prefixes) {
            return "   << CLASH";
        }
    }

    public static class DatatypeChecking extends Derivation {
        private static final long serialVersionUID=-7833124370362424190L;
        protected final Atom[] m_causes;

        public DatatypeChecking(Atom[] causes) {
            m_causes=causes;
        }
        public int getNumberOfPremises() {
            return m_causes.length;
        }
        public Fact getPremise(int premiseIndex) {
            return m_causes[premiseIndex];
        }
        public String toString(Prefixes prefixes) {
            return "   << DATATYPES";
        }
    }

    public static class UnknownDatatypeRestrictionDetection extends Derivation {
        private static final long serialVersionUID=-7824360133765453948L;
        protected final Atom[] m_causes;

        public UnknownDatatypeRestrictionDetection(Atom[] causes) {
            m_causes=causes;
        }
        public int getNumberOfPremises() {
            return m_causes.length;
        }
        public Fact getPremise(int premiseIndex) {
            return m_causes[premiseIndex];
        }
        public String toString(Prefixes prefixes) {
            return "   << UNKNOWN DATATYPE";
        }
    }

    public static class BaseFact extends Derivation {
        private static final long serialVersionUID=-5998349862414502218L;

        public static Derivation INSTANCE=new BaseFact();

        public int getNumberOfPremises() {
            return 0;
        }
        public Fact getPremise(int premiseIndex) {
            throw new IndexOutOfBoundsException();
        }
        public String toString(Prefixes prefixes) {
            return ".";
        }
    }
}
