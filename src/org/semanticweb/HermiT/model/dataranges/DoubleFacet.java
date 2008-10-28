package org.semanticweb.HermiT.model.dataranges;

import java.util.Set;

public interface DoubleFacet {
    public Set<DoubleInterval> getDoubleIntervals();
    
    public boolean isNegated();
}
