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
import org.semanticweb.HermiT.model.DLPredicate;

/**
 * This class is used to create sets of various types. It ensures that each distinct set exists only once,
 * thus allowing sets to be compared with ==.
 */
public final class Disjunction {
    protected final DLPredicate[] m_dlPredicates;
    protected final int m_hashCode;
    protected final DisjunctIndexWithBacktrackings[] m_disjunctIndexesWithBacktrackings;
    protected Disjunction m_nextEntry;

    protected Disjunction(DLPredicate[] dlPredicates,int hashCode,Disjunction nextEntry) {
        m_dlPredicates=dlPredicates;
        m_hashCode=hashCode;
        m_nextEntry=nextEntry;
        m_disjunctIndexesWithBacktrackings=new DisjunctIndexWithBacktrackings[dlPredicates.length];
        for (int i=0;i<dlPredicates.length;i++)
            m_disjunctIndexesWithBacktrackings[i]=new DisjunctIndexWithBacktrackings(i);
    }
    public DLPredicate[] getDisjuncts() {
        return m_dlPredicates;
    }
    public int getNumberOfDisjuncts() {
        return m_dlPredicates.length;
    }
    public DLPredicate getDisjunct(int disjunctIndex) {
        return m_dlPredicates[disjunctIndex];
    }
    public boolean isEqual(DLPredicate[] dlPredicates) {
        if (m_dlPredicates.length!=dlPredicates.length)
            return false;
        for (int index=m_dlPredicates.length-1;index>=0;--index)
            if (!m_dlPredicates[index].equals(dlPredicates[index]))
                return false;
        return true;
    }
    public int getDisjunctIndexWithLeastBacktrackings() {
        return m_disjunctIndexesWithBacktrackings[0].m_disjunctIndex;
    }
    public int getDisjunctIndexWithLeastBacktrackings(boolean[] triedDisjuncts) {
        assert triedDisjuncts.length==m_disjunctIndexesWithBacktrackings.length;
        int leastPunishedUntriedIndex;
        for (int i=0;i<m_disjunctIndexesWithBacktrackings.length;i++) {
            leastPunishedUntriedIndex=m_disjunctIndexesWithBacktrackings[i].m_disjunctIndex;
            if (!triedDisjuncts[leastPunishedUntriedIndex])
                return leastPunishedUntriedIndex;
        }
        throw new IllegalStateException("Internal error: invalid untried index.");
    }
    public void increaseNumberOfBacktrackings(int disjunctIndex) {
        for (int index=0;index<m_disjunctIndexesWithBacktrackings.length;index++) {
            DisjunctIndexWithBacktrackings disjunctIndexWithBacktrackings=m_disjunctIndexesWithBacktrackings[index];
            if (disjunctIndexWithBacktrackings.m_disjunctIndex==disjunctIndex) {
                disjunctIndexWithBacktrackings.m_numberOfBacktrackings++;
                int currentIndex=index;
                int nextIndex=currentIndex+1;
                while (nextIndex<m_disjunctIndexesWithBacktrackings.length && disjunctIndexWithBacktrackings.m_numberOfBacktrackings>m_disjunctIndexesWithBacktrackings[nextIndex].m_numberOfBacktrackings) {
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
