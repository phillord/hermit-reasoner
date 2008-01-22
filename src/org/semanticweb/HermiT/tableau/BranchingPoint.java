package org.semanticweb.HermiT.tableau;

import java.io.Serializable;

public class BranchingPoint implements Serializable {
    private static final long serialVersionUID=7306881534568051692L;

    protected final int m_level;
    protected final Node m_lastChangedNode;
    protected final Node.NodeEvent m_lastChangedNodeEvent;
    protected final GroundDisjunction m_lastUnprocessedGroundDisjunction;
    protected final GroundDisjunction m_lastProcessedGroundDisjunction;

    public BranchingPoint(Tableau tableau) {
        m_level=tableau.m_currentBranchingPoint+1;
        m_lastChangedNode=tableau.m_lastChangedNode;
        m_lastChangedNodeEvent=tableau.m_lastChangedNodeEvent;
        m_lastUnprocessedGroundDisjunction=tableau.m_lastUnprocessedGroundDisjunction;
        m_lastProcessedGroundDisjunction=tableau.m_lastProcessedGroundDisjunction;
    }
    public int getLevel() {
        return m_level;
    }
    public void startNextChoice(Tableau tableau,DependencySet clashDepdendencySet) {
    }
}