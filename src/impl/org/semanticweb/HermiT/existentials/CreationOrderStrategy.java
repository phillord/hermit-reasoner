// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.existentials;

import java.io.Serializable;

import org.semanticweb.HermiT.model.*;
import org.semanticweb.HermiT.tableau.*;
import org.semanticweb.HermiT.blocking.*;

public class CreationOrderStrategy extends StrategyBase implements Serializable {
    private static final long serialVersionUID=-64673639237063636L;
    StrategyBase.Expander expander;
    
    public CreationOrderStrategy(BlockingStrategy strategy) {
        super(strategy);
        expander = new StrategyBase.Expander() {
            Node expanded;
            public boolean expand(AtLeastAbstractRoleConcept c, Node n) {
                if (expanded == null) expanded = n;
                else if (expanded != n) return true;
                existentialExpansionManager.expand(c, n);
                existentialExpansionManager.markExistentialProcessed(c, n);
                return false;
            }
            public boolean completeExpansion() {
                if (expanded != null) {
                    expanded = null;
                    return true;
                } else {
                    return false;
                }
            }
        };
    }

    /** Expand all existentials on the oldest node with relevant existentials.
    
        This usually approximates a breadth-first expansion.
    */
    public boolean expandExistentials() {
        return super.expandExistentials(expander);
    }
    
}
