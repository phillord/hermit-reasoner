/*
 * Copyright 2008 by Oxford University; see license.txt for details
 */

package org.semanticweb.HermiT.datatypes;

import org.semanticweb.HermiT.datatypes.DataConstant.Impl;

import dk.brics.automaton.Datatypes;

/**
 * An implementation for the AnyURI datatype restriction that is realised as an 
 * extension of the String datatype restriction.
 *  
 * @author BGlimm
 */
public class DatatypeRestrictionAnyURI extends DatatypeRestrictionString {
    
    private static final long serialVersionUID = -6415629172600854818L;

    /**
     * Should use DT.ANYURI. 
     * @param datatype the datatype for this restriction (DT.ANYURI)
     */
    public DatatypeRestrictionAnyURI(DT datatype) {
        super(datatype);
        patternMatcher = Datatypes.get("URI");
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.DatatypeRestrictionString#datatypeAccepts(org.semanticweb.HermiT.model.dataranges.DataConstant)
     */
    public boolean datatypeAccepts(DataConstant constant) {
        return constant.getImplementation() == Impl.IAnyURI;
    }
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.dataranges.DatatypeRestrictionString#canHandle(org.semanticweb.HermiT.model.dataranges.DatatypeRestriction.DT)
     */
    public boolean canHandle(DT datatype) {
        return DT.getSubTreeFor(DT.ANYURI).contains(datatype);
    }
}
