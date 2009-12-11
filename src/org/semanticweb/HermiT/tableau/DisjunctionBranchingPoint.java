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

import org.semanticweb.HermiT.model.AnnotatedEquality;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.DLPredicate;
import org.semanticweb.HermiT.model.Equality;
import org.semanticweb.HermiT.model.Inequality;

public class DisjunctionBranchingPoint extends BranchingPoint {
    private static final long serialVersionUID=-8855083430836162354L;

    protected final GroundDisjunction m_groundDisjunction;
    protected int m_currentDisjunctIndex;
    
    public DisjunctionBranchingPoint(Tableau tableau,GroundDisjunction groundDisjunction) {
        super(tableau);
        m_groundDisjunction=groundDisjunction;
        m_currentDisjunctIndex=0;
    }
    public void startNextChoice(Tableau tableau,DependencySet clashDependencySet) {
        m_currentDisjunctIndex++;
        assert m_currentDisjunctIndex<m_groundDisjunction.getNumberOfDisjuncts();
        if (tableau.m_tableauMonitor!=null)
            tableau.m_tableauMonitor.disjunctProcessingStarted(m_groundDisjunction,m_currentDisjunctIndex);
        PermanentDependencySet dependencySet=tableau.getDependencySetFactory().getPermanent(clashDependencySet);
        if (m_currentDisjunctIndex==m_groundDisjunction.getNumberOfDisjuncts()-1)
            dependencySet=tableau.getDependencySetFactory().removeBranchingPoint(dependencySet,m_level);
        for (int previousDisjunctIndex=0;previousDisjunctIndex<m_currentDisjunctIndex;previousDisjunctIndex++) {
            DLPredicate dlPredicate=m_groundDisjunction.getDLPredicate(previousDisjunctIndex);
            if (Equality.INSTANCE.equals(dlPredicate) || (dlPredicate instanceof AnnotatedEquality))
                tableau.getExtensionManager().addAssertion(Inequality.INSTANCE,m_groundDisjunction.getArgument(previousDisjunctIndex,0),m_groundDisjunction.getArgument(previousDisjunctIndex,1),dependencySet,false);
            else if (dlPredicate instanceof AtomicConcept)
                tableau.getExtensionManager().addConceptAssertion(((AtomicConcept)dlPredicate).getNegation(),m_groundDisjunction.getArgument(previousDisjunctIndex,0),dependencySet,false);
        }
        m_groundDisjunction.addDisjunctToTableau(tableau,m_currentDisjunctIndex,dependencySet);
        if (tableau.m_tableauMonitor!=null)
            tableau.m_tableauMonitor.disjunctProcessingFinished(m_groundDisjunction,m_currentDisjunctIndex);
    }
}
