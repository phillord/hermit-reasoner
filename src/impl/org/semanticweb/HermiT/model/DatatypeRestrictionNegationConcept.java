package org.semanticweb.HermiT.model;

import org.semanticweb.HermiT.Namespaces;

public class DatatypeRestrictionNegationConcept extends DataRange {
    protected DatatypeRestriction negatedDatatypeRestriction;
    
    public DatatypeRestrictionNegationConcept(DatatypeRestriction datatypeRestriction) {
        this.negatedDatatypeRestriction = datatypeRestriction;
    }

    public DatatypeRestriction getNegatedDatatypeRestriction() {
        return negatedDatatypeRestriction;
    }

    public void setNegatedDatatypeRestriction(
            DatatypeRestriction negatedDatatypeRestriction) {
        this.negatedDatatypeRestriction = negatedDatatypeRestriction;
    }
    public boolean isBottom() {
        return negatedDatatypeRestriction.getDatatypeURI().toString().equals(AtomicConcept.RDFS_LITERAL.getURI()); 
    }
    public String toString(Namespaces namespaces) {
        return "(not" + negatedDatatypeRestriction.toString(namespaces).trim() + ")";      
    }
}
