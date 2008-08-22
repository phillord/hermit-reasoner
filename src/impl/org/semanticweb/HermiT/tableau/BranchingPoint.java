// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.tableau;

import java.io.Serializable;

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
