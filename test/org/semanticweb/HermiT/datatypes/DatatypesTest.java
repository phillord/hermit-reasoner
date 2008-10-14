package org.semanticweb.HermiT.datatypes;

import java.math.BigInteger;
import java.net.URI;

import org.semanticweb.HermiT.model.dataranges.DataConstant;
import org.semanticweb.HermiT.model.dataranges.DatatypeRestrictionInteger;
import org.semanticweb.HermiT.model.dataranges.DatatypeRestriction.Facets;
import org.semanticweb.HermiT.reasoner.AbstractReasonerTest;
import org.semanticweb.owl.vocab.XSDVocabulary;

public class DatatypesTest extends AbstractReasonerTest {

    public DatatypesTest(String name) {
        super(name);
    }
    
    public void testDataConstant() throws Exception {
        URI stringURI = XSDVocabulary.STRING.getURI();
        URI integerURI = XSDVocabulary.INTEGER.getURI();
        DataConstant constant1 = new DataConstant(stringURI, "abc");
        DataConstant constant2 = new DataConstant(stringURI, "abc");
        DataConstant constant3 = new DataConstant(integerURI, "3");
        DataConstant constant4 = new DataConstant(integerURI, "4");
        assertTrue(constant1.equals(constant2));
        assertTrue(constant2.equals(constant1));
        assertTrue(constant1.equals(constant1));
        assertTrue(constant2.equals(constant2));
        assertFalse(constant3.equals(constant4));
        assertFalse(constant4.equals(constant3));
        assertFalse(constant3.equals(constant2));
        assertTrue(constant3.equals(constant3));
        assertTrue(constant4.equals(constant4));
    }
    
    public void testDatatypeRestrictionInteger() throws Exception {
        DatatypeRestrictionInteger drInteger = new DatatypeRestrictionInteger();
        drInteger.addFacet(Facets.MAX_EXCLUSIVE, "3");
        assertTrue(drInteger.getIntervals().size() == 1);
        assertFalse(drInteger.isFinite());
        for (DatatypeRestrictionInteger.Interval i : drInteger.getIntervals()) {
            assertTrue(i.contains(new BigInteger("2")));
            assertFalse(i.contains(new BigInteger("3")));
        }
        drInteger.addFacet(Facets.MAX_EXCLUSIVE, "1");
        assertTrue(drInteger.getIntervals().size() == 1);
        assertFalse(drInteger.isFinite());
        for (DatatypeRestrictionInteger.Interval i : drInteger.getIntervals()) {
            assertTrue(i.contains(BigInteger.ZERO));
            assertFalse(i.contains(BigInteger.ONE));
        }
        drInteger.addFacet(Facets.MAX_EXCLUSIVE, "5");
        assertTrue(drInteger.getIntervals().size() == 1);
        assertFalse(drInteger.isFinite());
        for (DatatypeRestrictionInteger.Interval i : drInteger.getIntervals()) {
            assertTrue(i.contains(BigInteger.ZERO));
            assertFalse(i.contains(BigInteger.ONE));
        }
        drInteger.addFacet(Facets.MAX_INCLUSIVE, "-2");
        assertTrue(drInteger.getIntervals().size() == 1);
        assertFalse(drInteger.isFinite());
        for (DatatypeRestrictionInteger.Interval i : drInteger.getIntervals()) {
            assertTrue(i.contains(new BigInteger("-2")));
            assertFalse(i.contains(new BigInteger("-1")));
        }
        drInteger.addFacet(Facets.MAX_INCLUSIVE, "2");
        assertTrue(drInteger.getIntervals().size() == 1);
        assertFalse(drInteger.isFinite());
        for (DatatypeRestrictionInteger.Interval i : drInteger.getIntervals()) {
            assertTrue(i.contains(new BigInteger("-2")));
            assertFalse(i.contains(new BigInteger("-1")));
        }
        drInteger.addFacet(Facets.MIN_EXCLUSIVE, "-15");
        assertTrue(drInteger.getIntervals().size() == 1);
        assertTrue(drInteger.isFinite());
        for (DatatypeRestrictionInteger.Interval i : drInteger.getIntervals()) {
            assertTrue(i.contains(new BigInteger("-14")));
            assertFalse(i.contains(new BigInteger("-15")));
            assertFalse(i.contains(new BigInteger("-1")));
            assertTrue(i.contains(new BigInteger("-2")));
        }
        drInteger.addFacet(Facets.MIN_EXCLUSIVE, "-10");
        assertTrue(drInteger.getIntervals().size() == 1);
        assertTrue(drInteger.isFinite());
        for (DatatypeRestrictionInteger.Interval i : drInteger.getIntervals()) {
            assertTrue(i.contains(new BigInteger("-9")));
            assertFalse(i.contains(new BigInteger("-10")));
            assertFalse(i.contains(new BigInteger("-1")));
            assertTrue(i.contains(new BigInteger("-2")));
        }
        drInteger.addFacet(Facets.MIN_INCLUSIVE, "-2");
        assertTrue(drInteger.getIntervals().size() == 1);
        assertTrue(drInteger.isFinite());
        for (DatatypeRestrictionInteger.Interval i : drInteger.getIntervals()) {
            assertTrue(i.contains(new BigInteger("-2")));
            assertFalse(i.contains(new BigInteger("-3")));
            assertFalse(i.contains(new BigInteger("-1")));
        }
    }

    public void testDatatypeRestrictionIntegerNegated() throws Exception {
        DatatypeRestrictionInteger drInteger = new DatatypeRestrictionInteger();
        drInteger.negate();
        // not smaller 3 = greater or equal 3
        drInteger.addFacet(Facets.MAX_EXCLUSIVE, "3");
        assertTrue(drInteger.getIntervals().size() == 1);
        assertFalse(drInteger.isFinite());
        for (DatatypeRestrictionInteger.Interval i : drInteger.getIntervals()) {
            assertTrue(i.contains(new BigInteger("3")));
            assertFalse(i.contains(new BigInteger("2")));
        }
        // or not smaller 1 = or greater or equal 1
        drInteger.addFacet(Facets.MAX_EXCLUSIVE, "1");
        assertTrue(drInteger.getIntervals().size() == 1);
        assertFalse(drInteger.isFinite());
        for (DatatypeRestrictionInteger.Interval i : drInteger.getIntervals()) {
            assertTrue(i.contains(new BigInteger("1")));
            assertFalse(i.contains(new BigInteger("0")));
        }
        // or not smaller 5 = or greater or equal 5
        drInteger.addFacet(Facets.MAX_EXCLUSIVE, "5");
        assertTrue(drInteger.getIntervals().size() == 1);
        assertFalse(drInteger.isFinite());
        for (DatatypeRestrictionInteger.Interval i : drInteger.getIntervals()) {
            assertTrue(i.contains(new BigInteger("1")));
            assertFalse(i.contains(new BigInteger("0")));
        }
        // or not smaller or equal -2 = greater or equal -1
        drInteger.addFacet(Facets.MAX_INCLUSIVE, "-2");
        assertTrue(drInteger.getIntervals().size() == 1);
        assertFalse(drInteger.isFinite());
        for (DatatypeRestrictionInteger.Interval i : drInteger.getIntervals()) {
            assertTrue(i.contains(new BigInteger("-1")));
            assertFalse(i.contains(new BigInteger("-2")));
        }
        // or not smaller or equal 2 = greater or equal 1
        drInteger.addFacet(Facets.MAX_INCLUSIVE, "2");
        assertTrue(drInteger.getIntervals().size() == 1);
        assertFalse(drInteger.isFinite());
        for (DatatypeRestrictionInteger.Interval i : drInteger.getIntervals()) {
            assertTrue(i.contains(new BigInteger("-1")));
            assertFalse(i.contains(new BigInteger("-2")));
        }
        // or not greater -5 = smaller or equal -5
        drInteger.addFacet(Facets.MIN_EXCLUSIVE, "-5");
        assertTrue(drInteger.getIntervals().size() == 2);
        assertFalse(drInteger.isFinite());
        for (DatatypeRestrictionInteger.Interval i : drInteger.getIntervals()) {
            assertTrue(i.contains(new BigInteger("-5")) || i.contains(new BigInteger("-1")));
            assertFalse(i.contains(new BigInteger("-2")));
            assertFalse(i.contains(new BigInteger("-3")));
            assertFalse(i.contains(new BigInteger("-4")));
        }
        // or not greater -7 = smaller or equal -7
        drInteger.addFacet(Facets.MIN_EXCLUSIVE, "-7");
        assertTrue(drInteger.getIntervals().size() == 2);
        assertFalse(drInteger.isFinite());
        for (DatatypeRestrictionInteger.Interval i : drInteger.getIntervals()) {
            assertTrue(i.contains(new BigInteger("-5")) || i.contains(new BigInteger("-1")));
            assertFalse(i.contains(new BigInteger("-2")));
            assertFalse(i.contains(new BigInteger("-3")));
            assertFalse(i.contains(new BigInteger("-4")));
        }
        // or not greater or equal 5 = smaller or equal 4
        drInteger.addFacet(Facets.MIN_INCLUSIVE, "5");
        assertTrue(drInteger.getIntervals().size() == 0);
        assertFalse(drInteger.isFinite());
        assertFalse(drInteger.isBottom());
    }
    
//    public void testDatatypeManager() {
//        DatatypeManager manager = new DatatypeManager();
//        manager.
//    }
}
