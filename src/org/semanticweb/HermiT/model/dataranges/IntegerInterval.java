/*
 * Copyright 2008 by Oxford University; see license.txt for details
 */

package org.semanticweb.HermiT.model.dataranges;

import java.math.BigInteger;

/**
 * An interface for integer intervals. We use an interface, since integer 
 * intervals come in two flavours: one that uses longs and one that uses 
 * BigInteger to represent arbitrary integer ranges. We can switch automatically 
 * between the ranges as soon as the (min/max) values are getting too big/small.
 *  
 * @author BGlimm
 */
public interface IntegerInterval {
   
    /**
     * Creates a copy of this interval. 
     * @return an interval object that has the same min and max values as this 
     *         one.
     */
    public IntegerInterval getCopy();
    
    /**
     * Creates an instance of the required interval (either one that internally 
     * uses BigInteger or one that uses longs). 
     * @param min the lower bound
     * @param max the upper bound
     * @return an IntegerInterval with the given bounds
     */
    public IntegerInterval getInstance(Number min, Number max);
    
    /**
     * Intersects this interval with the given one. The returned interval is the 
     * intersection.
     * @param i an IntegerInterval
     * @return an IntegerInterval (with BigInteger or long internally as 
     * required) that represents the intersection
     */
    public IntegerInterval intersectWith(IntegerInterval i);
    
    /**
     * Tests for emptyness.
     * @return true if the interval cannot contain values and false otherwise.  
     */
    public boolean isEmpty();
    
    /**
     * Tests for finiteness. 
     * @return true if the interval is finite (has min and max values not equal 
     * to null) and false otherwise. 
     */
    public boolean isFinite();
    
    /**
     * Tests containment. 
     * @param integer an integer (Long or BigInteger)
     * @return true if the interval contains this integer and false otherwise. 
     */
    public boolean contains(Number integer);

    /**
     * Computes the size of the interval if it is finite. 
     * @return the number of integer values that are contained in this interval 
     * or null if the interval is not finite  
     */
    public BigInteger getCardinality();

    /**
     * @return the minimal value for this interval
     */
    public Number getMin();

    /**
     * @return the maximal value for this interval
     */
    public Number getMax();
    
    /**
     * @return lower bound - 1 (internally Long or BigInteger as required)
     */ 
    public Number decreasedMin();

    /**
     * @return upper bound + 1 (internally Long or BigInteger as required)
     */
    public Number increasedMax();
    
    /**
     * @return zero in the implementation of the currently used range (Long or 
     * BigInteger)
     */
    public Number getZero();
}
