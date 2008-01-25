package org.semanticweb.HermiT.tableau;

import org.semanticweb.HermiT.model.*;

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
        DependencySet dependencySet=clashDependencySet;
        if (m_currentDisjunctIndex==m_groundDisjunction.getNumberOfDisjuncts()-1)
            dependencySet=tableau.getDependencySetFactory().removeBranchingPoint(dependencySet,m_level);
        for (int previousDisjunctIndex=0;previousDisjunctIndex<m_currentDisjunctIndex;previousDisjunctIndex++) {
            DLPredicate dlPredicate=m_groundDisjunction.getDLPredicate(previousDisjunctIndex);
            if (Equality.INSTANCE.equals(dlPredicate))
                tableau.getExtensionManager().addAssertion(Inequality.INSTANCE,m_groundDisjunction.getArgument(previousDisjunctIndex,0),m_groundDisjunction.getArgument(previousDisjunctIndex,1),dependencySet);
            else if (dlPredicate instanceof AtomicConcept)
                tableau.getExtensionManager().addConceptAssertion(AtomicNegationConcept.create((AtomicConcept)dlPredicate),m_groundDisjunction.getArgument(previousDisjunctIndex,0),dependencySet);
        }
        m_groundDisjunction.addDisjunctToTableau(tableau,m_currentDisjunctIndex,dependencySet);
        if (tableau.m_tableauMonitor!=null)
            tableau.m_tableauMonitor.disjunctProcessingFinished(m_groundDisjunction,m_currentDisjunctIndex);
    }
}
