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
package org.semanticweb.HermiT.tableau;

import org.semanticweb.HermiT.Prefixes;
import org.semanticweb.HermiT.model.AtLeastConcept;
import org.semanticweb.HermiT.model.AtomicNegationConcept;
import org.semanticweb.HermiT.model.DLPredicate;

public final class GroundDisjunctionHeader {
    protected final DLPredicate[] m_dlPredicates;
    protected final int[] m_disjunctStart;
    protected final int m_hashCode;
    protected final DisjunctIndexWithBacktrackings[] m_disjunctIndexesWithBacktrackings;
    protected final int m_firstAtLeastPositiveIndex;
    protected final int m_firstAtLeastNegativeIndex;
    protected GroundDisjunctionHeader m_nextEntry;

    protected GroundDisjunctionHeader(DLPredicate[] dlPredicates,int hashCode,GroundDisjunctionHeader nextEntry) {
        m_dlPredicates=dlPredicates;
        m_disjunctStart=new int[m_dlPredicates.length];
        int argumentsSize=0;
        for (int disjunctIndex=0;disjunctIndex<m_dlPredicates.length;disjunctIndex++) {
            m_disjunctStart[disjunctIndex]=argumentsSize;
            argumentsSize+=m_dlPredicates[disjunctIndex].getArity();
        }
        m_hashCode=hashCode;
        m_nextEntry=nextEntry;
        m_disjunctIndexesWithBacktrackings=new DisjunctIndexWithBacktrackings[dlPredicates.length];
        // The disjuncts are arranged in a particular order that seems to work well in practice
        // Thus we initialize m_disjunctIndexesWithBacktrackings in the following order:
        // First, we have the disjuncts that are at least concepts but over a negated atomic concept
        // Next, we have the atomic concept disjuncts
        // Finally, we have the at least concepts that are not over a negated atomic concept
        // Later on we will ensure that disjunction learning (if enabled) does not move the disjuncts
        // out of their partition.
        int numberOfAtLeastPositiveDisjuncts=0;
        int numberOfAtLeastNegativeDisjuncts=0;
        for (int index=0;index<dlPredicates.length;index++)
            if (m_dlPredicates[index] instanceof AtLeastConcept) {
                AtLeastConcept atLeast=(AtLeastConcept)m_dlPredicates[index];
                if (atLeast.getToConcept() instanceof AtomicNegationConcept)
                    numberOfAtLeastNegativeDisjuncts++;
                else
                    numberOfAtLeastPositiveDisjuncts++;
            }
        m_firstAtLeastNegativeIndex=m_disjunctIndexesWithBacktrackings.length-numberOfAtLeastPositiveDisjuncts-numberOfAtLeastNegativeDisjuncts;
        m_firstAtLeastPositiveIndex=m_disjunctIndexesWithBacktrackings.length-numberOfAtLeastPositiveDisjuncts;
        int nextAtomicDisjunct=0;
        int nextAtLeastNegativeDisjunct=m_firstAtLeastNegativeIndex;
        int nextAtLeastPositiveDisjunct=m_firstAtLeastPositiveIndex;
        for (int index=0;index<dlPredicates.length;index++)
            if (m_dlPredicates[index] instanceof AtLeastConcept) {
                AtLeastConcept atLeast=(AtLeastConcept)m_dlPredicates[index];
                if (atLeast.getToConcept() instanceof AtomicNegationConcept)
                    m_disjunctIndexesWithBacktrackings[nextAtLeastNegativeDisjunct++]=new DisjunctIndexWithBacktrackings(index);
                else
                    m_disjunctIndexesWithBacktrackings[nextAtLeastPositiveDisjunct++]=new DisjunctIndexWithBacktrackings(index);
            }
            else
                m_disjunctIndexesWithBacktrackings[nextAtomicDisjunct++]=new DisjunctIndexWithBacktrackings(index);
    }
    protected boolean isEqual(DLPredicate[] dlPredicates) {
        if (m_dlPredicates.length!=dlPredicates.length)
            return false;
        for (int index=m_dlPredicates.length-1;index>=0;--index)
            if (!m_dlPredicates[index].equals(dlPredicates[index]))
                return false;
        return true;
    }
    public int[] getSortedDisjunctIndexes() {
        int[] sortedDisjunctIndexes=new int[m_disjunctIndexesWithBacktrackings.length];
        for (int index=m_disjunctIndexesWithBacktrackings.length-1;index>=0;--index)
            sortedDisjunctIndexes[index]=m_disjunctIndexesWithBacktrackings[index].m_disjunctIndex;
        return sortedDisjunctIndexes;
    }
    public void increaseNumberOfBacktrackings(int disjunctIndex) {
        for (int index=0;index<m_disjunctIndexesWithBacktrackings.length;index++) {
            DisjunctIndexWithBacktrackings disjunctIndexWithBacktrackings=m_disjunctIndexesWithBacktrackings[index];
            if (disjunctIndexWithBacktrackings.m_disjunctIndex==disjunctIndex) {
                disjunctIndexWithBacktrackings.m_numberOfBacktrackings++;
                // find the partition end, swapping of disjuncts stops when the number of backtrackings for the
                // current disjunct is lower than the one for the next disjunct or when the partition end is reached
                int partitionEnd;
                if (index<m_firstAtLeastNegativeIndex) partitionEnd=m_firstAtLeastNegativeIndex;
                else if (index>=m_firstAtLeastNegativeIndex && index<m_firstAtLeastPositiveIndex) partitionEnd=m_firstAtLeastPositiveIndex;
                else partitionEnd=m_disjunctIndexesWithBacktrackings.length;
                int currentIndex=index;
                int nextIndex=currentIndex+1;
                while (nextIndex<partitionEnd && disjunctIndexWithBacktrackings.m_numberOfBacktrackings>m_disjunctIndexesWithBacktrackings[nextIndex].m_numberOfBacktrackings) {
                    m_disjunctIndexesWithBacktrackings[currentIndex]=m_disjunctIndexesWithBacktrackings[nextIndex];
                    m_disjunctIndexesWithBacktrackings[nextIndex]=disjunctIndexWithBacktrackings;
                    currentIndex=nextIndex;
                    nextIndex++;
                }
                break;
            }
        }
    }
    public String toString(Prefixes prefixes) {
        StringBuffer buffer=new StringBuffer();
        for (int disjunctIndex=0;disjunctIndex<m_dlPredicates.length;disjunctIndex++) {
        	if (disjunctIndex>0)
        	    buffer.append(" \\/ ");
    	    buffer.append(m_dlPredicates[disjunctIndex].toString(prefixes));
    	    buffer.append(" (");
            for (DisjunctIndexWithBacktrackings disjunctIndexWithBacktrackings : m_disjunctIndexesWithBacktrackings) {
            	if (disjunctIndexWithBacktrackings.m_disjunctIndex==disjunctIndex) {
            	    buffer.append(disjunctIndexWithBacktrackings.m_numberOfBacktrackings);
            		break;
            	}
            }
            buffer.append(")");
        }
        return buffer.toString();
    }
    public String toString() {
        return toString(Prefixes.STANDARD_PREFIXES);
    }

    protected static class DisjunctIndexWithBacktrackings {
        protected final int m_disjunctIndex;
        protected int m_numberOfBacktrackings;

        public DisjunctIndexWithBacktrackings(int index) {
            m_disjunctIndex=index;
        }
    }
}
