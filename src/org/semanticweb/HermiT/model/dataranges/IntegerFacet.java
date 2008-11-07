/*
 * Copyright 2008 by Oxford University; see license.txt for details
 */

package org.semanticweb.HermiT.model.dataranges;

/**
 * An interface that allows to retrieve integer intervals. A decimal datatype 
 * restriction with decimal intervals can, for example, also return an integer 
 * interval such that all integer values in that range are also in the decimal 
 * range. We use this when merging numerical ranges in the DatatypeManager since 
 * the numerical ranges in OWL are not disjoint.
 *  
 * @author BGlimm
 */
import java.util.Set;

public interface IntegerFacet {
    
    /**
     * @return a set of integer intervals
     */
    public Set<IntegerInterval> getIntegerIntervals();
    
    /**
     * @return true if the range is negated and false otherwise
     */
    public boolean isNegated();
}
