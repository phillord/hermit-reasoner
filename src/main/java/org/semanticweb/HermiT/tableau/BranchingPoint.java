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

import java.io.Serializable;

/**
 * Represents a branching point for the tableau given to the constructor. 
 */
public class BranchingPoint implements Serializable {
    private static final long serialVersionUID=7306881534568051692L;

    protected final int m_level;
    protected final Node m_lastTableauNode;
    protected final Node m_lastMergedOrPrunedNode;
    protected final GroundDisjunction m_firstGroundDisjunction;
    protected final GroundDisjunction m_firstUnprocessedGroundDisjunction;

    public BranchingPoint(Tableau tableau) {
        m_level=tableau.m_currentBranchingPoint+1;
        m_lastTableauNode=tableau.m_lastTableauNode;
        m_lastMergedOrPrunedNode=tableau.m_lastMergedOrPrunedNode;
        m_firstGroundDisjunction=tableau.m_firstGroundDisjunction;
        m_firstUnprocessedGroundDisjunction=tableau.m_firstUnprocessedGroundDisjunction;
    }
    public int getLevel() {
        return m_level;
    }
    public void startNextChoice(Tableau tableau,DependencySet clashDepdendencySet) {
    }
}
