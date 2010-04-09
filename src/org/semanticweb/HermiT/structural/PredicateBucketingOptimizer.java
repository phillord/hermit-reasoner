package org.semanticweb.HermiT.structural;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLPredicate;
import org.semanticweb.HermiT.model.NodeIDLessEqualThan;
import org.semanticweb.HermiT.model.NodeIDsAscendingOrEqual;
import org.semanticweb.HermiT.model.Term;
import org.semanticweb.HermiT.model.Variable;

public class PredicateBucketingOptimizer {

    public static void rewriteDLClauses(Set<DLClause> dlClauses,int occurrenceTrigger) {
        boolean changed=true;
        while (changed) {
            changed=false;
            Map<DLPredicate,Set<DLClause>> index=buildDLClauseIndex(dlClauses);
            for (Map.Entry<DLPredicate,Set<DLClause>> entry : index.entrySet()) {
                Set<DLClause> dlClausesForPredicate=entry.getValue();
                dlClausesForPredicate.retainAll(dlClauses);
                if (dlClausesForPredicate.size()>=occurrenceTrigger) {
                    int sizeAtStart=dlClauses.size();
                    dlClauses.removeAll(dlClausesForPredicate);
                    Set<DLClause> rewrittenDLClauses=rewriteDLPredicate(dlClausesForPredicate,entry.getKey());
                    dlClauses.addAll(rewrittenDLClauses);
                    if (sizeAtStart!=dlClauses.size())
                        changed=true;
                }
            }
        }
    }
    protected static Map<DLPredicate,Set<DLClause>> buildDLClauseIndex(Collection<DLClause> dlClauses) {
        Map<DLPredicate,Set<DLClause>> index=new HashMap<DLPredicate,Set<DLClause>>();
        for (DLClause dlClause : dlClauses) {
            if (dlClause.getBodyLength()>1) {
                for (int bodyIndex=0;bodyIndex<dlClause.getBodyLength();bodyIndex++) {
                    DLPredicate dlPredicate=dlClause.getBodyAtom(bodyIndex).getDLPredicate();
                    if (!isSpecialDLPredicate(dlPredicate)) {
                        Set<DLClause> dlClausesForDLPredicate=index.get(dlPredicate);
                        if (dlClausesForDLPredicate==null) {
                            dlClausesForDLPredicate=new HashSet<DLClause>();
                            index.put(dlPredicate,dlClausesForDLPredicate);
                        }
                        dlClausesForDLPredicate.add(dlClause);
                    }
                }
            }
        }
        return index;
    }
    protected static Set<DLClause> rewriteDLPredicate(Set<DLClause> dlClauses,DLPredicate dlPredicate) {
        Set<DLClause> result=new HashSet<DLClause>();
        Map<Set<ProvidedPositions>,List<RewritingInfo>> rewritingInfosByType=new HashMap<Set<ProvidedPositions>,List<RewritingInfo>>();
        for (DLClause dlClause : dlClauses) {
            RewritingInfo rewritingInfo=new RewritingInfo(dlClause,dlPredicate);
            if (rewritingInfo.m_rewrittenAtomIndex==-1 || rewritingInfo.m_bodyPartsByProvidedPositions.isEmpty())
                result.add(dlClause);
            else {
                Set<ProvidedPositions> providedPositionsSet=rewritingInfo.getProvidedPositionsSet();
                List<RewritingInfo> rewiritingInfos=rewritingInfosByType.get(providedPositionsSet);
                if (rewiritingInfos==null) {
                    rewiritingInfos=new ArrayList<RewritingInfo>();
                    rewritingInfosByType.put(providedPositionsSet,rewiritingInfos);
                }
                rewiritingInfos.add(rewritingInfo);
            }
        }
        Term[] variables=new Term[dlPredicate.getArity()];
        for (int variableIndex=0;variableIndex<dlPredicate.getArity();variableIndex++)
            variables[variableIndex]=Variable.create("X"+variableIndex);
        int bucketIndex=0;
        for (Map.Entry<Set<ProvidedPositions>,List<RewritingInfo>> entry : rewritingInfosByType.entrySet()) {
            if (entry.getValue().size()==1) {
                RewritingInfo rewritingInfo=entry.getValue().get(0);
                result.add(rewritingInfo.m_dlClause);
            }
            else {
                Set<ProvidedPositions> providedPositionsSet=entry.getKey();
                Atom[] bucketingBody=new Atom[1+providedPositionsSet.size()];
                bucketingBody[0]=Atom.create(dlPredicate,variables);
                // Create the guard predicates
                DLPredicate[] guardPredicates=new DLPredicate[providedPositionsSet.size()];
                int guardIndex=0;
                for (ProvidedPositions providedPositions : providedPositionsSet) {
                    guardPredicates[guardIndex]=providedPositions.getGuardDLPredicate(dlPredicate);
                    bucketingBody[guardIndex+1]=Atom.create(guardPredicates[guardIndex],providedPositions.selectProvidedTerms(variables));
                    guardIndex++;
                }
                // Create the bucketing clause
                DLPredicate bucketDLPredicate=getBucketDLPredicate(dlPredicate,bucketIndex);
                DLClause bucketingDLClause=DLClause.create(new Atom[] { Atom.create(bucketDLPredicate,variables) },bucketingBody,DLClause.ClauseType.OTHER);
                result.add(bucketingDLClause);
                // Now process the individual rewritings
                for (RewritingInfo rewritingInfo : entry.getValue()) {
                    Atom rewrittenAtom=rewritingInfo.getRewrittenAtom();
                    // Generate the modified clause
                    Atom[] newBodyAtoms=rewritingInfo.m_dlClause.getBodyAtoms();
                    newBodyAtoms[rewritingInfo.m_rewrittenAtomIndex]=rewrittenAtom.replaceDLPredicate(bucketDLPredicate);
                    DLClause modifiedDLClause=rewritingInfo.m_dlClause.getChangedDLClause(null,newBodyAtoms);
                    result.add(modifiedDLClause);
                    // Generate the guard clauses
                    guardIndex=0;
                    for (ProvidedPositions providedPositions : providedPositionsSet) {
                        Atom[] bodyPart=rewritingInfo.m_bodyPartsByProvidedPositions.get(providedPositions);
                        Term[] guardArguments=providedPositions.selectProvidedArguments(rewrittenAtom);
                        Atom guardHead=Atom.create(guardPredicates[guardIndex],guardArguments);
                        DLClause guardDLClause=DLClause.create(new Atom[] { guardHead },bodyPart,DLClause.ClauseType.OTHER);
                        result.add(guardDLClause);
                        guardIndex++;
                    }
                }
                bucketIndex++;
            }
        }
        return result;
    }
    protected static DLPredicate getBucketDLPredicate(DLPredicate baseDLPredicate,int bucketIndex) {
        if (baseDLPredicate instanceof AtomicConcept)
            return AtomicConcept.create("internal:bucket#"+bucketIndex+"#"+((AtomicConcept)baseDLPredicate).getIRI());
        else if (baseDLPredicate instanceof AtomicRole)
            return AtomicRole.create("internal:bucket#"+bucketIndex+"#"+((AtomicRole)baseDLPredicate).getIRI());
        else
            throw new IllegalArgumentException("Internal error: unsupported type of base predicate.");
    }
    protected static boolean isSpecialDLPredicate(DLPredicate dlPredicate) {
        return dlPredicate instanceof NodeIDLessEqualThan || dlPredicate instanceof NodeIDsAscendingOrEqual || dlPredicate.toString().startsWith("<internal:bucket#") || dlPredicate.toString().startsWith("<internal:guard#");
    }

    protected static class RewritingInfo {
        public final DLClause m_dlClause;
        public final int m_rewrittenAtomIndex;
        public final Map<ProvidedPositions,Atom[]> m_bodyPartsByProvidedPositions;

        public RewritingInfo(DLClause dlClause,DLPredicate dlPredicate) {
            m_dlClause=dlClause;
            m_rewrittenAtomIndex=getRewrittenAtomIndex(dlPredicate);
            if (m_rewrittenAtomIndex==-1)
                m_bodyPartsByProvidedPositions=null;
            else {
                Map<Variable,Set<Atom>> atomsByVariablesIndex=getAtomsByVariablesIndex();
                Atom rewrittenAtom=getRewrittenAtom();
                m_bodyPartsByProvidedPositions=new HashMap<ProvidedPositions,Atom[]>();
                Set<Atom> usedAtoms=new HashSet<Atom>();
                usedAtoms.add(rewrittenAtom);
                Set<Variable> providedVariables=new HashSet<Variable>();
                for (int argumentIndex=0;argumentIndex<rewrittenAtom.getArity();argumentIndex++) {
                    Variable variable=rewrittenAtom.getArgumentVariable(argumentIndex);
                    if (variable!=null && providedVariables.add(variable)) {
                        Atom[] subclause=getUnusedAtomsContainingVariable(variable,usedAtoms,atomsByVariablesIndex,providedVariables);
                        if (subclause.length>0) {
                            ProvidedPositions providedPositions=getProvidedPositions(subclause);
                            m_bodyPartsByProvidedPositions.put(providedPositions,subclause);
                        }
                    }
                }
            }
        }
        public Atom getRewrittenAtom() {
            return m_dlClause.getBodyAtom(m_rewrittenAtomIndex);
        }
        protected ProvidedPositions getProvidedPositions(Atom[] subclause) {
            Atom rewrittenAtom=getRewrittenAtom();
            Set<Variable> variablesInSubclause=new HashSet<Variable>();
            for (Atom atom : subclause)
                atom.getVariables(variablesInSubclause);
            boolean[] providedPositionsArray=new boolean[rewrittenAtom.getArity()];
            for (int argumentIndex=rewrittenAtom.getArity()-1;argumentIndex>=0;--argumentIndex) {
                Variable variable=rewrittenAtom.getArgumentVariable(argumentIndex);
                if (variable!=null && variablesInSubclause.contains(variable))
                    providedPositionsArray[argumentIndex]=true;
            }
            return new ProvidedPositions(providedPositionsArray);
        }
        protected Atom[] getUnusedAtomsContainingVariable(Variable startVariable,Set<Atom> usedAtoms,Map<Variable,Set<Atom>> atomsByVariablesIndex,Set<Variable> providedVariables) {
            // Compute the set of atoms reachable from 'startVariable'
            Set<Atom> result=new HashSet<Atom>();
            List<Variable> toProcess=new ArrayList<Variable>();
            toProcess.add(startVariable);
            while (!toProcess.isEmpty()) {
                Variable variable=toProcess.remove(toProcess.size()-1);
                Set<Atom> atoms=atomsByVariablesIndex.get(variable);
                for (Atom atom : atoms) {
                    if (!isSpecialDLPredicate(atom.getDLPredicate()) && usedAtoms.add(atom)) {
                        result.add(atom);
                        for (int argumentIndex=0;argumentIndex<atom.getArity();argumentIndex++) {
                            Variable argumentVariable=atom.getArgumentVariable(argumentIndex);
                            if (argumentVariable!=null && providedVariables.add(argumentVariable))
                                toProcess.add(argumentVariable);
                        }
                    }
                }
            }
            // We produce the atoms in the order in which they occur in the DL-clause
            Atom[] resultArray=new Atom[result.size()];
            int resultIndex=0;
            for (int bodyIndex=0;bodyIndex<m_dlClause.getBodyLength();bodyIndex++) {
                Atom bodyAtom=m_dlClause.getBodyAtom(bodyIndex);
                if (result.contains(bodyAtom))
                    resultArray[resultIndex++]=bodyAtom;
            }
            return resultArray;
        }
        protected int getRewrittenAtomIndex(DLPredicate dlPredicate) {
            for (int bodyIndex=0;bodyIndex<m_dlClause.getBodyLength();bodyIndex++)
                if (m_dlClause.getBodyAtom(bodyIndex).getDLPredicate().equals(dlPredicate))
                    return bodyIndex;
            return -1;
        }
        protected Map<Variable,Set<Atom>> getAtomsByVariablesIndex() {
            Map<Variable,Set<Atom>> atomsByVariablesIndex=new HashMap<Variable,Set<Atom>>();
            for (int bodyIndex=0;bodyIndex<m_dlClause.getBodyLength();bodyIndex++) {
                Atom bodyAtom=m_dlClause.getBodyAtom(bodyIndex);
                for (int argumentIndex=0;argumentIndex<bodyAtom.getArity();argumentIndex++) {
                    Variable variable=bodyAtom.getArgumentVariable(argumentIndex);
                    if (variable!=null) {
                        Set<Atom> atoms=atomsByVariablesIndex.get(variable);
                        if (atoms==null) {
                            atoms=new HashSet<Atom>();
                            atomsByVariablesIndex.put(variable,atoms);
                        }
                        atoms.add(bodyAtom);
                    }
                }
            }
            return atomsByVariablesIndex;
        }
        public Set<ProvidedPositions> getProvidedPositionsSet() {
            return m_bodyPartsByProvidedPositions.keySet();
        }
    }

    protected static class ProvidedPositions {
        public final boolean[] m_positionsArray;
        protected final int m_hashCode;

        public ProvidedPositions(boolean[] positionsArray) {
            m_positionsArray=positionsArray;
            int hashCode=0;
            for (int index=0;index<m_positionsArray.length;index++)
                hashCode=hashCode*3+(m_positionsArray[index] ? 7 : 0);
            m_hashCode=hashCode;
        }
        public int getNumberOfProvidedPositions() {
            int number=0;
            for (int index=0;index<m_positionsArray.length;index++)
                if (m_positionsArray[index])
                    number++;
            return number;
        }
        public Term[] selectProvidedTerms(Term[] terms) {
            Term[] result=new Term[getNumberOfProvidedPositions()];
            int number=0;
            for (int index=0;index<m_positionsArray.length;index++)
                if (m_positionsArray[index])
                    result[number++]=terms[index];
            return result;
        }
        public DLPredicate getGuardDLPredicate(DLPredicate baseDLPredicate) {
            StringBuffer guardIRI=new StringBuffer("internal:guard#");
            int numberOfProvidedPositions=0;
            for (int argumentIndex=0;argumentIndex<m_positionsArray.length;argumentIndex++) {
                if (m_positionsArray[argumentIndex]) {
                    guardIRI.append('p');
                    numberOfProvidedPositions++;
                }
                else
                    guardIRI.append('n');
            }
            guardIRI.append('#');
            if (baseDLPredicate instanceof AtomicConcept)
                guardIRI.append(((AtomicConcept)baseDLPredicate).getIRI());
            else if (baseDLPredicate instanceof AtomicRole)
                guardIRI.append(((AtomicRole)baseDLPredicate).getIRI());
            else
                throw new IllegalArgumentException("Internal error: unsupported type of base predicate.");
            if (numberOfProvidedPositions==1)
                return AtomicConcept.create(guardIRI.toString());
            else if (numberOfProvidedPositions==2)
                return AtomicRole.create(guardIRI.toString());
            else
                throw new IllegalArgumentException("Internal error: unsupported bindings in the guard.");
        }
        public Term[] selectProvidedArguments(Atom atom) {
            Term[] result=new Term[getNumberOfProvidedPositions()];
            int number=0;
            for (int index=0;index<m_positionsArray.length;index++)
                if (m_positionsArray[index])
                    result[number++]=atom.getArgument(index);
            return result;
        }
        public int hashCode() {
            return m_hashCode;
        }
        public boolean equals(Object that) {
            if (this==that)
                return true;
            if (!(that instanceof ProvidedPositions))
                return false;
            ProvidedPositions thatProvidedPositions=(ProvidedPositions)that;
            if (m_hashCode!=thatProvidedPositions.m_hashCode)
                return false;
            boolean[] thatPositionsArray=thatProvidedPositions.m_positionsArray;
            if (m_positionsArray.length!=thatPositionsArray.length)
                return false;
            for (int index=m_positionsArray.length-1;index>=0;--index)
                if (m_positionsArray[index]!=thatPositionsArray[index])
                    return false;
            return true;
        }
    }
}
