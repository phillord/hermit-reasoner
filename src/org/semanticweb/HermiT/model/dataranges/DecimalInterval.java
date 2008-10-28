package org.semanticweb.HermiT.model.dataranges;

import java.math.BigDecimal;

public class DecimalInterval {
    BigDecimal min = null;
    BigDecimal max = null;
    boolean openMin = true;
    boolean openMax = true;
    
    public DecimalInterval(BigDecimal min, BigDecimal max, 
            boolean openMin, boolean openMax) {
        this.min = min;
        this.max = max;
        this.openMin = openMin;
        this.openMax = openMax;
    }
    
    public DecimalInterval getCopy() {
        return new DecimalInterval(min, max, openMin, openMax);
    }
    
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
    
    public boolean isEmpty() {
        return (min != null && max != null && min.compareTo(max) > 0);
    }
    
    protected boolean isEmpty(BigDecimal lower, BigDecimal upper) {
        return (lower != null && upper != null && lower.compareTo(upper) > 0);
    }
    
    public boolean isFinite() {
        return isEmpty();
    }
    
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
    
    public boolean contains(DecimalInterval interval) {
        return contains(interval.getMin()) && contains(interval.getMax());
    }
    
    public boolean disjointWith(DecimalInterval interval) {
        return (max != null && interval.getMin() != null 
                && (max.compareTo(interval.getMin()) < 0 
                        || (max.compareTo(interval.getMin()) == 0 && (!openMax || !interval.isOpenMin())))) 
        || (min != null && interval.getMax() != null 
                && (min.compareTo(interval.getMax()) > 0 
                        || (min.compareTo(interval.getMax()) == 0 && (!openMin || !interval.isOpenMax()))));
    }
    
    public BigDecimal getCardinality() {
        if (isEmpty()) return BigDecimal.ZERO;
        return null;
    }

    public BigDecimal getMin() {
        return min;
    }

    public void setMin(BigDecimal min) {
        this.min = min;
    }

    public BigDecimal getMax() {
        return max;
    }

    public void setMax(BigDecimal max) {
        this.max = max;
    }
    
    public boolean isOpenMin() {
        return openMin;
    }

    public void setOpenMin(boolean openMin) {
        this.openMin = openMin;
    }

    public boolean isOpenMax() {
        return openMax;
    }

    public void setOpenMax(boolean openMax) {
        this.openMax = openMax;
    }

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
