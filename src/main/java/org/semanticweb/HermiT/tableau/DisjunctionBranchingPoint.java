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

import org.semanticweb.HermiT.model.AnnotatedEquality;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.DLPredicate;
import org.semanticweb.HermiT.model.Equality;
import org.semanticweb.HermiT.model.Inequality;

public final class DisjunctionBranchingPoint extends BranchingPoint {
    private static final long serialVersionUID=-8855083430836162354L;

    protected final GroundDisjunction m_groundDisjunction;
    protected final int[] m_sortedDisjunctIndexes;
    protected int m_currentIndex;

    public DisjunctionBranchingPoint(Tableau tableau,GroundDisjunction groundDisjunction,int[] sortedDisjunctIndexes) {
        super(tableau);
        m_groundDisjunction=groundDisjunction;
        m_sortedDisjunctIndexes=sortedDisjunctIndexes;
    }
    public void startNextChoice(Tableau tableau,DependencySet clashDependencySet) {
        if (tableau.m_useDisjunctionLearning)
            m_groundDisjunction.getGroundDisjunctionHeader().increaseNumberOfBacktrackings(m_sortedDisjunctIndexes[m_currentIndex]);
        m_currentIndex++;
        assert m_currentIndex<m_groundDisjunction.getNumberOfDisjuncts();
        int currentDisjunctIndex=m_sortedDisjunctIndexes[m_currentIndex];
        if (tableau.m_tableauMonitor!=null)
            tableau.m_tableauMonitor.disjunctProcessingStarted(m_groundDisjunction,currentDisjunctIndex);
        PermanentDependencySet dependencySet=tableau.getDependencySetFactory().getPermanent(clashDependencySet);
        if (m_currentIndex+1==m_groundDisjunction.getNumberOfDisjuncts())
            dependencySet=tableau.getDependencySetFactory().removeBranchingPoint(dependencySet,m_level);
        for (int previousIndex=0;previousIndex<m_currentIndex;previousIndex++) {
            int previousDisjunctIndex=m_sortedDisjunctIndexes[previousIndex];
            DLPredicate dlPredicate=m_groundDisjunction.getDLPredicate(previousDisjunctIndex);
            if (Equality.INSTANCE.equals(dlPredicate) || (dlPredicate instanceof AnnotatedEquality))
                tableau.m_extensionManager.addAssertion(Inequality.INSTANCE,m_groundDisjunction.getArgument(previousDisjunctIndex,0),m_groundDisjunction.getArgument(previousDisjunctIndex,1),dependencySet,false);
            else if (dlPredicate instanceof AtomicConcept)
                tableau.m_extensionManager.addConceptAssertion(((AtomicConcept)dlPredicate).getNegation(),m_groundDisjunction.getArgument(previousDisjunctIndex,0),dependencySet,false);
        }
        m_groundDisjunction.addDisjunctToTableau(tableau,currentDisjunctIndex,dependencySet);
        if (tableau.m_tableauMonitor!=null)
            tableau.m_tableauMonitor.disjunctProcessingFinished(m_groundDisjunction,currentDisjunctIndex);
    }
}
