package org.semanticweb.HermiT.model.dataranges;

import java.util.Set;

public interface IntegerFacet {
    public Set<IntegerInterval> getIntegerIntervals();
    
    public boolean isNegated();
}
