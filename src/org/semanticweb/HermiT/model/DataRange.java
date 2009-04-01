/*
 * Copyright 2008 by Oxford University; see license.txt for details
 */

package org.semanticweb.HermiT.model;

import org.semanticweb.HermiT.model.LiteralConcept;
import org.semanticweb.HermiT.model.DLPredicate;

/**
 * Represents a data range in a DL clause.
 */
public abstract class DataRange extends LiteralConcept implements DLPredicate {
    private static final long serialVersionUID=352467050584766830L;

    public int getArity() {
        return 1;
    }
}
