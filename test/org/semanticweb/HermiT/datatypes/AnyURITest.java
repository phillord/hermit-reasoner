package org.semanticweb.HermiT.datatypes;

import org.semanticweb.HermiT.datatypes.old.CanonicalDataRange;
import org.semanticweb.HermiT.datatypes.old.DatatypeRestrictionAnyURI;
import org.semanticweb.HermiT.datatypes.old.InternalDataRange;
import org.semanticweb.HermiT.datatypes.old.InternalDatatypeRestriction.DT;
import org.semanticweb.HermiT.datatypes.old.InternalDatatypeRestriction.Facet;
import org.semanticweb.HermiT.reasoner.AbstractReasonerTest;

public class AnyURITest extends AbstractReasonerTest {
    
    public AnyURITest(String name) {
        super(name);
    }
        
    public void testAssignments() throws Exception {
        InternalDataRange dr = new DatatypeRestrictionAnyURI(DT.ANYURI);
        dr.addFacet(Facet.MIN_LENGTH, "1");
        CanonicalDataRange cdr = (CanonicalDataRange) dr;
        assertTrue(!cdr.isFinite());
    }
}
