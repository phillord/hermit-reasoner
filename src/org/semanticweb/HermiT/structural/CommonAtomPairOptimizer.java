package org.semanticweb.HermiT.structural;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLPredicate;
import org.semanticweb.HermiT.model.Variable;

public class CommonAtomPairOptimizer {
    protected final Map<AtomPair,AtomPairOccurrences> m_pairOccurrences;
    protected final Map<DLClause,Set<AtomPairOccurrences>> m_dlClausesToOccurrences;
    protected int m_replacementNumber;

    public CommonAtomPairOptimizer(Collection<DLClause> dlClauses) {
        m_pairOccurrences=new HashMap<AtomPair,AtomPairOccurrences>();
        m_dlClausesToOccurrences=new HashMap<DLClause,Set<AtomPairOccurrences>>();
        for (DLClause dlClause : dlClauses)
            addDLClause(dlClause);
    }
    protected void addDLClause(DLClause dlClause) {
        if (!m_dlClausesToOccurrences.containsKey(dlClause))
            m_dlClausesToOccurrences.put(dlClause,new HashSet<AtomPairOccurrences>());
        for (int firstIndex=0;firstIndex<dlClause.getBodyLength();firstIndex++) {
            Atom first=dlClause.getBodyAtom(firstIndex);
            for (int secondIndex=firstIndex+1;secondIndex<dlClause.getBodyLength();secondIndex++) {
                Atom second=dlClause.getBodyAtom(secondIndex);
                if (atomsCompatible(first,second))
                    addCounter(dlClause,first,second);
                else if (atomsCompatible(second,first))
                    addCounter(dlClause,second,first);
            }
        }
    }
    protected void addCounter(DLClause dlClause,Atom first,Atom second) {
        AtomPair pair=new AtomPair(first,second);
        AtomPairOccurrences occurrences=m_pairOccurrences.get(pair);
        if (occurrences==null) {
            occurrences=new AtomPairOccurrences(pair);
            m_pairOccurrences.put(pair,occurrences);
        }
        occurrences.m_dlClauses.add(dlClause);
        m_dlClausesToOccurrences.get(dlClause).add(occurrences);
    }
    protected boolean atomsCompatible(Atom first,Atom second) {
        DLPredicate firstDLPredicate=first.getDLPredicate();
        DLPredicate secondDLPredicate=second.getDLPredicate();
        if (!(firstDLPredicate instanceof AtomicConcept || firstDLPredicate instanceof AtomicRole) || !(secondDLPredicate instanceof AtomicConcept || secondDLPredicate instanceof AtomicRole))
            return false;
        for (int secondArgumentIndex=0;secondArgumentIndex<second.getArity();secondArgumentIndex++) {
            Variable variable=second.getArgumentVariable(secondArgumentIndex);
            if (variable!=null && !first.containsVariable(variable))
                return false;
        }
        return true;
    }
    protected void removeDLClause(DLClause dlClause) {
        Set<AtomPairOccurrences> setOfOccurrences=m_dlClausesToOccurrences.remove(dlClause);
        for (AtomPairOccurrences occurrences : setOfOccurrences) {
            occurrences.m_dlClauses.remove(dlClause);
            if (occurrences.m_dlClauses.isEmpty())
                m_pairOccurrences.remove(occurrences.m_atomPair);
        }
    }
    public void optimizePairs(int pairFrequencyThreshold) {
        List<DLClause> dlClausesCopy=new ArrayList<DLClause>();
        AtomPairOccurrences occurrences=getMostFrequent();
        while (occurrences!=null && occurrences.m_dlClauses.size()>=pairFrequencyThreshold) {
            dlClausesCopy.clear();
            dlClausesCopy.addAll(occurrences.m_dlClauses);
            Atom first=occurrences.m_atomPair.m_first;
            Atom second=occurrences.m_atomPair.m_second;
            Atom replacementAtom=getReplacementAtom(first,second);
            for (DLClause dlClause : dlClausesCopy) {
                removeDLClause(dlClause);
                Atom[] newBodyAtoms=new Atom[dlClause.getBodyLength()-1];
                int newBodyIndex=0;
                for (int bodyIndex=0;bodyIndex<dlClause.getBodyLength();bodyIndex++) {
                    Atom bodyAtom=dlClause.getBodyAtom(bodyIndex);
                    if (bodyAtom.equals(first))
                        newBodyAtoms[newBodyIndex++]=replacementAtom;
                    else if (!bodyAtom.equals(second))
                        newBodyAtoms[newBodyIndex++]=bodyAtom;
                }
                DLClause newDLClause=dlClause.getChangedDLClause(null,newBodyAtoms);
                addDLClause(newDLClause);
            }
            occurrences=getMostFrequent();
        }
    }
    public Set<DLClause> getDLClauses() {
        return m_dlClausesToOccurrences.keySet();
    }
    protected AtomPairOccurrences getMostFrequent() {
        Iterator<AtomPairOccurrences> iterator=m_pairOccurrences.values().iterator();
        if (!iterator.hasNext())
            return null;
        AtomPairOccurrences mostFrequent=iterator.next();
        int mostFrequentFrequency=mostFrequent.m_dlClauses.size();
        while (iterator.hasNext()) {
            AtomPairOccurrences occurrences=iterator.next();
            int occurrencesFrequency=occurrences.m_dlClauses.size();
            if (occurrencesFrequency>mostFrequentFrequency) {
                mostFrequent=occurrences;
                mostFrequentFrequency=occurrencesFrequency;
            }
        }
        return mostFrequent;
    }
    protected Atom getReplacementAtom(Atom first,Atom second) {
        DLPredicate replacementDLPredicate;
        if (first.getDLPredicate() instanceof AtomicConcept)
            replacementDLPredicate=AtomicConcept.create("internal:pair#"+(m_replacementNumber++));
        else
            replacementDLPredicate=AtomicRole.create("internal:pair#"+(m_replacementNumber++));
        Atom replacementAtom=first.replaceDLPredicate(replacementDLPredicate);
        DLClause definitionDLClause=DLClause.create(new Atom[] { replacementAtom },new Atom[] { first,second },DLClause.ClauseType.OTHER);
        addDLClause(definitionDLClause);
        return replacementAtom;
    }

    protected static class AtomPair {
        protected final Atom m_first;
        protected final Atom m_second;

        public AtomPair(Atom first,Atom second) {
            m_first=first;
            m_second=second;
        }
        public int hashCode() {
            return m_first.hashCode()+m_second.hashCode();
        }
        public boolean equals(Object that) {
            if (this==that)
                return true;
            AtomPair thatPair=(AtomPair)that;
            return m_first.equals(thatPair.m_first) && m_second.equals(thatPair.m_second);
        }
    }

    protected static class AtomPairOccurrences {
        protected final AtomPair m_atomPair;
        protected final Set<DLClause> m_dlClauses;

        public AtomPairOccurrences(AtomPair atomPair) {
            m_atomPair=atomPair;
            m_dlClauses=new HashSet<DLClause>();
        }
    }
}
