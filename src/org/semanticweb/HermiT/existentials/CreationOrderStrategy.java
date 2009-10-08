// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.existentials;

import java.io.Serializable;

import org.semanticweb.HermiT.blocking.BlockingStrategy;
import org.semanticweb.HermiT.model.AtLeastConcept;
import org.semanticweb.HermiT.tableau.Node;

/**
 * Strategy for expanding all existentials on the oldest node in the tableau with unexpanded existentials.
 * This usually closely approximates a breadth-first expansion. (Existentials introduced onto parent nodes
 * as a result of constraints on their children can produce newer nodes of lower depth than older nodes,
 * which could result in slight non-breadth-first behavior.)
 */
public class CreationOrderStrategy extends AbstractExpansionStrategy implements Serializable {
    private static final long serialVersionUID=-64673639237063636L;
    
    public CreationOrderStrategy(BlockingStrategy strategy) {
        super(strategy,true);
    }
    public boolean isDeterministic() {
        return true;
    }
    protected void expandExistential(AtLeastConcept atLeastConcept,Node forNode) {
        m_existentialExpansionManager.expand(atLeastConcept,forNode);
        m_existentialExpansionManager.markExistentialProcessed(atLeastConcept,forNode);
    }
}
