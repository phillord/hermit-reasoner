package org.semanticweb.HermiT.existentials;

import java.io.Serializable;

import org.semanticweb.HermiT.model.*;
import org.semanticweb.HermiT.tableau.*;
import org.semanticweb.HermiT.blocking.*;

public class CreationOrderStrategy implements ExistentialsExpansionStrategy,Serializable {
    private static final long serialVersionUID=-64673639237063636L;

    protected final BlockingStrategy m_blockingStrategy;
    protected Tableau m_tableau;
    protected ExtensionManager m_extensionManager;
    protected ExistentialExpansionManager m_existentialExpansionManager;
    protected DescriptionGraphManager m_descriptionGraphManager;

    public CreationOrderStrategy(BlockingStrategy blockingStrategy) {
        m_blockingStrategy=blockingStrategy;
    }
    public void intialize(Tableau tableau) {
        m_tableau=tableau;
        m_extensionManager=m_tableau.getExtensionManager();
        m_existentialExpansionManager=m_tableau.getExistentialExpansionManager();
        m_descriptionGraphManager=m_tableau.getDescriptionGraphManager();
        m_blockingStrategy.initialize(m_tableau);
    }
    public void clear() {
        m_blockingStrategy.clear();
    }
    public boolean expandExistentials() {
        m_blockingStrategy.computeBlocking();
        Node node=m_tableau.getFirstTableauNode();
        while (node!=null) {
            if (!node.isBlocked() && node.hasUnprocessedExistentials()) {
                while (node.hasUnprocessedExistentials()) {
                    ExistentialConcept existentialConcept=node.getSomeUnprocessedExistential();
                    if (existentialConcept instanceof AtLeastAbstractRoleConcept) {
                        AtLeastAbstractRoleConcept atLeastAbstractRoleConcept=(AtLeastAbstractRoleConcept)existentialConcept;
                        if (!m_existentialExpansionManager.isSatisfied(atLeastAbstractRoleConcept,node))
                            m_existentialExpansionManager.expand(atLeastAbstractRoleConcept,node);
                        else {
                            if (m_tableau.getTableauMonitor()!=null)
                                m_tableau.getTableauMonitor().existentialSatisfied(atLeastAbstractRoleConcept,node);
                        }
                    }
                    else if (existentialConcept instanceof ExistsDescriptionGraph) {
                        ExistsDescriptionGraph existsDescriptionGraph=(ExistsDescriptionGraph)existentialConcept;
                        if (!m_descriptionGraphManager.isSatisfied(existsDescriptionGraph,node))
                            m_descriptionGraphManager.expand(existsDescriptionGraph,node);
                        else {
                            if (m_tableau.getTableauMonitor()!=null)
                                m_tableau.getTableauMonitor().existentialSatisfied(existsDescriptionGraph,node);
                        }
                    }
                    else
                        throw new IllegalStateException("Unsupported type of existential concept in CreationOrderStrategy.");
                    m_existentialExpansionManager.markExistentialProcessed(existentialConcept,node);
                }
                return true;
            }
            node=node.getNextTableauNode();
        }
        return false;
    }
    public void nodeWillChange(Node node) {
        m_blockingStrategy.nodeWillChange(node);
    }
    public void branchingPointPushed() {
    }
    public void backtrack() {
    }
    public void modelFound() {
        m_blockingStrategy.modelFound();
    }
    public boolean isDeterministic() {
        return true;
    }
}
