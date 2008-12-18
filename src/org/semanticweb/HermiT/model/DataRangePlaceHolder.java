package org.semanticweb.HermiT.model;

import org.semanticweb.HermiT.model.dataranges.AbstractDataRange;

public class DataRangePlaceHolder extends AtomicConcept implements
        AbstractDataRange {

    private static final long serialVersionUID = 5032329670184644924L;
    
    protected DataRangePlaceHolder(String uri) {
        super(uri);
    }
}
