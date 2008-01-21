package org.semanticweb.HermiT.disjunction;

import org.semanticweb.HermiT.tableau.*;

public interface DisjunctionProcessingStrategy {
    GroundDisjunction pickUnprocessedGroundDisjunction(Tableau tableau);
}
