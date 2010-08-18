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

package org.semanticweb.HermiT.hierarchy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.Individual;

public class AtomicConceptElementManager {
    
    public static final String LB=System.getProperty("line.separator");
    
    protected final Map<AtomicConcept,AtomicConceptElement> m_conceptToElement;

    
    protected AtomicConceptElementManager() {
        m_conceptToElement=new HashMap<AtomicConcept, AtomicConceptElement>();
    }
    public AtomicConceptElement getAtomicConceptElement(AtomicConcept atomicConcept) {
        if (m_conceptToElement.containsKey(atomicConcept)) 
            return m_conceptToElement.get(atomicConcept);
        else {
            AtomicConceptElement element=new AtomicConceptElement(atomicConcept);
            m_conceptToElement.put(atomicConcept, element);
            return element;
        }
    }
    public String toString() {
        StringBuffer buffer=new StringBuffer();
        for (AtomicConcept atomicConcept : m_conceptToElement.keySet()) {
            buffer.append(atomicConcept);
            buffer.append(" -> ");
            buffer.append(m_conceptToElement.get(atomicConcept).toString());
            buffer.append(LB);
        }
        return buffer.toString();
    }
    
    public class AtomicConceptElement {
        protected final AtomicConcept m_atomicConcept;
        protected final Set<Individual> m_knownInstances;
        protected final Set<Individual> m_possibleInstances;
        
        protected AtomicConceptElement(AtomicConcept atomicConcept) {
            m_atomicConcept=atomicConcept;
            m_knownInstances=new HashSet<Individual>();
            m_possibleInstances=new HashSet<Individual>();
        }
        public AtomicConcept getAtomicConcept() {
            return m_atomicConcept;
        }
        public boolean isKnown(Individual individual) {
            return m_knownInstances.contains(individual);
        }
        public boolean isPossible(Individual individual) {
            return m_possibleInstances.contains(individual);
        }
        public Set<Individual> getKnownInstances() {
            return m_knownInstances;
        }
        public Set<Individual> getPossibleInstances() {
            return m_possibleInstances;
        }
        public boolean hasPossibles() {
            return !m_possibleInstances.isEmpty();
        }
        public void setToKnown(Individual individual) {
            m_possibleInstances.remove(individual);
            m_knownInstances.add(individual);
        }
        public boolean addPossible(Individual individual) {
            return m_possibleInstances.add(individual);
        }
        public boolean addPossibles(Set<Individual> individuals) {
            return m_possibleInstances.addAll(individuals);
        }
        public String toString() {
            StringBuffer buffer=new StringBuffer();
            buffer.append(m_atomicConcept);
            buffer.append(" (known instances: ");
            boolean notfirst=false;
            for (Individual individual : m_knownInstances) {
                if (notfirst) 
                    buffer.append(", ");
                notfirst=true;
                buffer.append(individual.toString());
            }
            buffer.append(" | possible instances: ");
            notfirst=false;
            for (Individual individual : m_possibleInstances) {
                if (notfirst) 
                    buffer.append(", ");
                notfirst=true;
                buffer.append(individual.toString());
            }
            buffer.append(") ");
            return buffer.toString();
        }
    }
}

