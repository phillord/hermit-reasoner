// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.datatypes.datetime;

public enum BoundType {
    INCLUSIVE,EXCLUSIVE;

    public BoundType getComplement() {
        return values()[1-ordinal()];
    }
    public static BoundType getMoreRestrictive(BoundType boundType1,BoundType boundType2) {
        int maxOrdinal=Math.max(boundType1.ordinal(),boundType1.ordinal());
        return values()[maxOrdinal];
    }
}