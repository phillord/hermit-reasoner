package org.semanticweb.HermiT.disjunction;

import java.io.Serializable;

import org.semanticweb.HermiT.tableau.*;

public class MostRecentDisjunctionProcessingStrategy implements DisjunctionProcessingStrategy,Serializable {
    private static final long serialVersionUID=-4709094613340416326L;

    public GroundDisjunction pickUnprocessedGroundDisjunction(Tableau tableau) {
        return tableau.getLastUnprocessedGroundDisjunction();
    }
}
