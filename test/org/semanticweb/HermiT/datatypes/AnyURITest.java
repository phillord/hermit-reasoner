package org.semanticweb.HermiT.datatypes;

import org.semanticweb.HermiT.datatypes.DatatypeRestriction.DT;
import org.semanticweb.HermiT.datatypes.DatatypeRestriction.Facet;
import org.semanticweb.HermiT.reasoner.AbstractReasonerTest;

public class AnyURITest extends AbstractReasonerTest {
    
    public AnyURITest(String name) {
        super(name);
    }
        
    public void testAssignments() throws Exception {
        DataRange dr = new DatatypeRestrictionAnyURI(DT.ANYURI);
        dr.addFacet(Facet.MIN_LENGTH, "1");
        CanonicalDataRange cdr = (CanonicalDataRange) dr;
        assertTrue(!cdr.isFinite());
    }
}
