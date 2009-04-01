/*
 * Copyright 2008 by Oxford University; see license.txt for details
 */

package org.semanticweb.HermiT.datatypes.old;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * The class can be used to capture decimal intervals. Such a range is always 
 * infinite since between each to decimal values there is another one. 
 * 
 * @author BGlimm
 */
public class DecimalInterval implements Serializable {

    private static final long serialVersionUID = 6886089002512160228L;
    
    protected BigDecimal min = null;
    protected BigDecimal max = null;
    protected boolean openMin = true;
    protected boolean openMax = true;
    
    /**
     * Creates a decimal interval. 
     * @param min the lower bound
     * @param max the upper bound
     * @param openMin if true then the interval excludes the lower bound 
     *                otherwise it includes the lower bound
     * @param openMax if true then the interval excludes the upper bound 
     *                otherwise it includes the upper bound
     */
    public DecimalInterval(BigDecimal min, BigDecimal max, 
            boolean openMin, boolean openMax) {
        this.min = min;
        this.max = max;
        this.openMin = openMin;
        this.openMax = openMax;
    }
    
    /**
     * Creates a copy of this interval. 
     * @return an interval object that has the same min/max/open/closed values 
     *         as this one.
     */
    public DecimalInterval getCopy() {
        return new DecimalInterval(min, max, openMin, openMax);
    }
    
    /**
     * Intersect with the given interval. After the intersection, this interval 
     * will have min and max values that are those of the intersection and the 
     * open/closed values are adapted accordingly. 
     * @param i an interval
     */
    public void intersectWith(DecimalInterval i) {
        if (max == null) {
            max = i.getMax();
            openMax = i.isOpenMax();
        } else {
            if (i.getMax() != null) {
                // both not null
                if (i.getMax().compareTo(max) == 0 && !i.isOpenMax()) {
                    openMax = false;
                } else if (i.getMax().compareTo(max) < 0) {
                    max = i.getMax();
                    openMax = i.isOpenMax();
                }
            }
        }
        if (min == null) {
            min = i.getMin();
            openMin = i.isOpenMin();
        } else {
            if (i.getMin() != null) {
                // both not null
                if (i.getMin().compareTo(min) == 0 && !i.isOpenMin()) {
                    openMin = false;
                } else if (i.getMin().compareTo(min) > 0) {
                    min = i.getMin();
                    openMin = i.isOpenMin();
                }
            }
        }
    }
    
    /**
     * Tests for emptyness.
     * @return true if the interval cannot contain values and false otherwise.  
     */
    public boolean isEmpty() {
        return (min != null && max != null && min.compareTo(max) > 0);
    }

    /**
     * Tests for finiteness. 
     * @return true if the interval is finite (has min and max values not equal 
     * to null) and false otherwise. 
     */
    public boolean isFinite() {
        return isEmpty();
    }
    
    /**
     * Tests containment. 
     * @param decimal a decimal
     * @return true if the interval contains this decimal and false otherwise. 
     */
    public boolean contains(BigDecimal decimal) {
        boolean contains = true;
        if (min != null) {
            contains = contains 
                    && (min.compareTo(decimal) <= 0 
                    && (!openMin || min.compareTo(decimal) != 0));
        }
        if (max != null) {
            contains = contains 
                    && (max.compareTo(decimal) >= 0 
                    && (!openMin || max.compareTo(decimal) != 0));
        }
        return contains;
    }

    /**
     * @return the minimal value for this interval
     */
    public BigDecimal getMin() {
        return min;
    }
    
    /**
     * @return the maximal value for this interval
     */
    public BigDecimal getMax() {
        return max;
    }
    
    /**
     * @return true if the interval is open w.r.t. the lower bound
     */
    public boolean isOpenMin() {
        return openMin;
    }
    
    /**
     * @return true if the interval is open w.r.t. the upper bound
     */
    public boolean isOpenMax() {
        return openMax;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        if (min != null) {
            buffer.append((isOpenMin() ? "> " : ">= ") + min);
        }
        if (max != null) {
            if (min != null) buffer.append(" ");
            buffer.append((isOpenMax() ? "< " : "<= ") + max);
        }
        return buffer.toString();
    }
}
