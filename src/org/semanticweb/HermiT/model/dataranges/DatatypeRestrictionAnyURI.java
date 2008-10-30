package org.semanticweb.HermiT.model.dataranges;

import dk.brics.automaton.Datatypes;

public class DatatypeRestrictionAnyURI extends DatatypeRestrictionString {
    
    public DatatypeRestrictionAnyURI(DT datatype) {
        super(datatype);
        patternMatcher = Datatypes.get("URI");
    }
    
    public boolean datatypeAccepts(DataConstant constant) {
        return DT.getSubTreeFor(DT.ANYURI).contains(constant.getDatatype());
    }
    
    public boolean canHandle(DT datatype) {
        return DT.getSubTreeFor(DT.ANYURI).contains(datatype);
    }
}
