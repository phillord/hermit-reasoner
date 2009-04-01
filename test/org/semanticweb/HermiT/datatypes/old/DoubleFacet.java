/*
 * Copyright 2008 by Oxford University; see license.txt for details
 */

package org.semanticweb.HermiT.datatypes.old;

import java.util.Set;

/**
 * An interface that allows to retrieve double intervals. A decimal datatype 
 * restriction with decimal intervals can, for example, also return a double 
 * interval such that all double values in that range are also in the decimal 
 * range. We use this when merging numerical ranges in the DatatypeManager since 
 * the numerical ranges in OWL are not disjoint. 
 * @author BGlimm
 */
public interface DoubleFacet {
    
    /**
     * @return a set of double intervals
     */
    public Set<DoubleInterval> getDoubleIntervals();
    
    /**
     * @return true if the range is negated and false otherwise
     */
    public boolean isNegated();
    
    /**
     * @return true if an explicit max value has been set and false otherwise
     */
    public boolean hasExplicitMax();
    
    /**
     * @return true if an explicit min value has been set and false otherwise
     */
    public boolean hasExplicitMin();
}
