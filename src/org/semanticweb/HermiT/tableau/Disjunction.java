/* Copyright 2008, 2009 by the Oxford University Computing Laboratory
   
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
import org.semanticweb.HermiT.model.InterningManager;

/**
 * This class is used to create sets of various types. It ensures that each distinct set exists only once,
 * thus allowing sets to be compared with ==. 
 */
public class Disjunction {
    
    protected final DLPredicate[] m_dlPredicates;
    protected final IndexWithPunishFactor[] m_indexesWithPunishFactor;
    
    protected Disjunction(DLPredicate[] dlPredicates) {
        m_dlPredicates=dlPredicates;
        m_indexesWithPunishFactor=new IndexWithPunishFactor[dlPredicates.length];
        for (int i=0;i<dlPredicates.length;i++) {
            m_indexesWithPunishFactor[i]=new IndexWithPunishFactor(i);
        }
    }
    public DLPredicate[] getDisjuncts() {
        return m_dlPredicates;
    }
    public int getNumberOfDisjuncts() {
        return m_dlPredicates.length;
    }
    public String toString(Prefixes prefixes) {
        String s="";
        for (int i=0;i<m_dlPredicates.length;i++) {
            s+=m_dlPredicates[i].toString(prefixes)+" ("+m_indexesWithPunishFactor[i].m_punishFactor+")";
        }
        return s;
    }
    public String toString() {
        String s="";
        for (int i=0;i<m_dlPredicates.length;i++) {
            s+=m_dlPredicates[i]+" ("+m_indexesWithPunishFactor[i].m_punishFactor+")";
            if (i<m_dlPredicates.length-1) s+=" \\/ ";
        }
        return s;
    }
    protected static InterningManager<Disjunction> s_interningManager=new InterningManager<Disjunction>() {
        protected boolean equal(Disjunction disjunction1,Disjunction disjunction2) {
            if (disjunction1.m_dlPredicates.length!=disjunction2.m_dlPredicates.length)
                return false;
            for (int index=disjunction1.m_dlPredicates.length-1;index>=0;--index)
                if (disjunction1.m_dlPredicates[index]!=disjunction2.m_dlPredicates[index]) return false;
            return true;
        }
        protected int getHashCode(Disjunction disjunction) {
            int hashCode=0;
            for (int i=0;i<disjunction.m_dlPredicates.length;i++) {
                hashCode+=disjunction.m_dlPredicates[i].hashCode();
            }
            return hashCode;
        }
    };
    
    public static Disjunction create(DLPredicate[] dlPredicates) {
        return s_interningManager.intern(new Disjunction(dlPredicates));
    }
    
    public static class IndexWithPunishFactor implements Comparable<IndexWithPunishFactor> {
        protected int m_punishFactor;
        protected int m_index;
        
        public IndexWithPunishFactor(int index) {
            m_index=index;
            m_punishFactor=0;
        }
        public void increasePunishment() {
            m_punishFactor++;
        }
        public int compareTo(IndexWithPunishFactor indexWithPunishFactor) {
            if (indexWithPunishFactor==this) return 0;
            int result=this.m_punishFactor-indexWithPunishFactor.m_punishFactor;
            if (result!=0) return result;
            return (this.m_index-indexWithPunishFactor.m_index);
        }
        public String toString() {
            return m_index+" ("+m_punishFactor+")";
        }
    }  
}
