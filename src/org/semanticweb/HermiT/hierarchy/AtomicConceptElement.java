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

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.HermiT.model.Individual;
/**AtomicConceptElement.*/
public class AtomicConceptElement {
    
    protected final Set<Individual> m_knownInstances;
    protected final Set<Individual> m_possibleInstances;
    
    /**
     * @param known known
     * @param possible possible
     */
    public AtomicConceptElement(Set<Individual> known, Set<Individual> possible) {
        if (known==null)
            m_knownInstances=new HashSet<>();
        else 
            m_knownInstances=known;
        if (possible==null)
            m_possibleInstances=new HashSet<>();
        else 
            m_possibleInstances=possible;
    }
    /**
     * @param individual individual
     * @return true if known
     */
    public boolean isKnown(Individual individual) {
        return m_knownInstances.contains(individual);
    }
    /**
     * @param individual individual
     * @return true if possible
     */
    public boolean isPossible(Individual individual) {
        return m_possibleInstances.contains(individual);
    }
    /**
     * @return known instances
     */
    public Set<Individual> getKnownInstances() {
        return m_knownInstances;
    }
    /**
     * @return possible instances
     */
    public Set<Individual> getPossibleInstances() {
        return m_possibleInstances;
    }
    /**
     * @return true if has possibles
     */
    public boolean hasPossibles() {
        return !m_possibleInstances.isEmpty();
    }
    /**
     * @param individual individual
     */
    public void setToKnown(Individual individual) {
        m_possibleInstances.remove(individual);
        m_knownInstances.add(individual);
    }
    /**
     * @param individual individual
     * @return true if added
     */
    public boolean addPossible(Individual individual) {
        return m_possibleInstances.add(individual);
    }
    /**
     * @param individuals individuals
     * @return true if added
     */
    public boolean addPossibles(Set<Individual> individuals) {
        return m_possibleInstances.addAll(individuals);
    }
    @Override
    public String toString() {
        StringBuilder buffer=new StringBuilder(" (known instances: ");
        boolean notfirst=false;
        for (Individual individual : m_knownInstances) {
            if (notfirst) 
                buffer.append(", ");
            notfirst=true;
            buffer.append(individual);
        }
        buffer.append(" | possible instances: ");
        notfirst=false;
        for (Individual individual : m_possibleInstances) {
            if (notfirst) 
                buffer.append(", ");
            notfirst=true;
            buffer.append(individual);
        }
        buffer.append(") ");
        return buffer.toString();
    }
}

