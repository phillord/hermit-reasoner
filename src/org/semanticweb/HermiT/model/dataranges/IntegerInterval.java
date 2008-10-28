package org.semanticweb.HermiT.model.dataranges;

import java.math.BigInteger;

public interface IntegerInterval {
   
    public IntegerInterval getCopy();
    
    public IntegerInterval getInstance(Number min, Number max);
    
    public IntegerInterval intersectWith(IntegerInterval i);
    
    public boolean isEmpty();
    
    public boolean isFinite();
    
    public boolean contains(Number integer);

    public BigInteger getCardinality();

    public Number getMin();

    public Number getMax();
    
    public Number increasedMin();
    
    public Number decreasedMin();
    
    public Number increasedMax();
    
    public Number decreasedMax();
}
