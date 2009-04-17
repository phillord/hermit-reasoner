// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.datatypes;

import java.util.Collection;

public interface ValueSpaceSubset {
    String getDatatypeURI();
    boolean hasCardinalityAtLeast(int number);
    boolean containsDataValue(Object dataValue);
    void enumerateDataValues(Collection<Object> dataValues);
}
