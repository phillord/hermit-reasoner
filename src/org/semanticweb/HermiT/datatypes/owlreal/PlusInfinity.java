package org.semanticweb.HermiT.datatypes.owlreal;

public final class PlusInfinity extends Number {
    private static final long serialVersionUID=-205551124673073593L;
    public static final PlusInfinity INSTANCE=new PlusInfinity();

    private PlusInfinity() {
    }
    public boolean equals(Object that) {
        return this==that;
    }
    public String toString() {
        return "+INF";
    }
    public double doubleValue() {
        throw new UnsupportedOperationException();
    }
    public float floatValue() {
        throw new UnsupportedOperationException();
    }
    public int intValue() {
        throw new UnsupportedOperationException();
    }
    public long longValue() {
        throw new UnsupportedOperationException();
    }
    protected Object readResolve() {
        return INSTANCE;
    }
}
