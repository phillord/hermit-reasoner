package org.semanticweb.HermiT.datatypes;

import java.math.BigInteger;

import org.semanticweb.HermiT.model.dataranges.CanonicalDataRange;
import org.semanticweb.HermiT.model.dataranges.DataConstant;
import org.semanticweb.HermiT.model.dataranges.DataRange;
import org.semanticweb.HermiT.model.dataranges.DatatypeRestrictionDouble;
import org.semanticweb.HermiT.model.dataranges.DatatypeRestrictionOWLRealPlus;
import org.semanticweb.HermiT.model.dataranges.DoubleInterval;
import org.semanticweb.HermiT.model.dataranges.DatatypeRestriction.DT;
import org.semanticweb.HermiT.model.dataranges.DatatypeRestriction.Facets;
import org.semanticweb.HermiT.model.dataranges.DatatypeRestriction.Impl;
import org.semanticweb.HermiT.reasoner.AbstractReasonerTest;

public class DoublesTest extends AbstractReasonerTest {
    
    public DoublesTest(String name) {
        super(name);
    }
    
    public void testNaN() throws Exception {
        DataConstant constant1 = new DataConstant(Impl.IDouble, DT.DOUBLE, "" + Double.NaN);
        DataConstant constant2 = new DataConstant(Impl.IDouble, DT.DOUBLE, "" + new Double("NaN"));
        Double d1 = new Double(constant1.getValue());
        Double d2 = new Double(constant2.getValue());
        assertTrue(DatatypeRestrictionDouble.isNaN(d1));
        assertTrue(DatatypeRestrictionDouble.isNaN(d2));
    }
    
    public void testDatatypeRestrictionDouble() throws Exception {
        DatatypeRestrictionDouble drDouble = new DatatypeRestrictionDouble(DT.DOUBLE);
        double maxEx = 3.25;
        drDouble.addFacet(Facets.MAX_EXCLUSIVE, Double.toString(maxEx));
        assertTrue(drDouble.getDoubleIntervals().size() == 1);
        assertTrue(drDouble.isFinite());
        for (DoubleInterval i : drDouble.getDoubleIntervals()) {
            assertTrue(i.contains(DatatypeRestrictionDouble.previousDouble(maxEx)));
            assertFalse(i.contains(maxEx));
        }
        maxEx = 1.0;
        drDouble.addFacet(Facets.MAX_EXCLUSIVE, Double.toString(maxEx));
        assertTrue(drDouble.getDoubleIntervals().size() == 1);
        assertTrue(drDouble.isFinite());
        for (DoubleInterval i : drDouble.getDoubleIntervals()) {
            assertTrue(i.contains(DatatypeRestrictionDouble.previousDouble(maxEx)));
            assertFalse(i.contains(maxEx));
        }
        maxEx = 5.0;
        drDouble.addFacet(Facets.MAX_EXCLUSIVE, Double.toString(maxEx));
        assertTrue(drDouble.getDoubleIntervals().size() == 1);
        assertTrue(drDouble.isFinite());
        for (DoubleInterval i : drDouble.getDoubleIntervals()) {
            assertTrue(i.contains(DatatypeRestrictionDouble.previousDouble(1.0)));
            assertFalse(i.contains(1.0));
            assertFalse(i.contains(maxEx));
        }
        maxEx = +0.0;
        drDouble.addFacet(Facets.MAX_INCLUSIVE, Double.toString(maxEx));
        assertTrue(drDouble.getDoubleIntervals().size() == 1);
        assertTrue(drDouble.isFinite());
        for (DoubleInterval i : drDouble.getDoubleIntervals()) {
            assertTrue(i.contains(+0.0));
            assertTrue(i.contains(-0.0));
        }
        maxEx = -0.0;
        drDouble.addFacet(Facets.MAX_INCLUSIVE, Double.toString(maxEx));
        assertTrue(drDouble.getDoubleIntervals().size() == 1);
        assertTrue(drDouble.isFinite());
        for (DoubleInterval i : drDouble.getDoubleIntervals()) {
            assertFalse(i.contains(+0.0));
            assertTrue(i.contains(-0.0));
        }
        double min = -12.0;
        drDouble.addFacet(Facets.MIN_EXCLUSIVE, Double.toString(min));
        assertTrue(drDouble.getDoubleIntervals().size() == 1);
        assertTrue(drDouble.isFinite());
        for (DoubleInterval i : drDouble.getDoubleIntervals()) {
            assertTrue(i.contains(DatatypeRestrictionDouble.nextDouble(-12.0)));
            assertFalse(i.contains(-12.0));
            assertFalse(i.contains(+0.0));
            assertTrue(i.contains(-0.0));
        }
        min = -10.0;
        drDouble.addFacet(Facets.MIN_INCLUSIVE, Double.toString(min));
        assertTrue(drDouble.getDoubleIntervals().size() == 1);
        assertTrue(drDouble.isFinite());
        for (DoubleInterval i : drDouble.getDoubleIntervals()) {
            assertTrue(i.contains(-10.0));
            assertFalse(i.contains(DatatypeRestrictionDouble.previousDouble(-10.0)));
            assertFalse(i.contains(+0.0));
            assertTrue(i.contains(-0.0));
        }
        min = -14.25;
        drDouble.addFacet(Facets.MIN_INCLUSIVE, Double.toString(min));
        assertTrue(drDouble.getDoubleIntervals().size() == 1);
        assertTrue(drDouble.isFinite());
        for (DoubleInterval i : drDouble.getDoubleIntervals()) {
            assertTrue(i.contains(-10.0));
            assertFalse(i.contains(DatatypeRestrictionDouble.previousDouble(-10.0)));
            assertFalse(i.contains(+0.0));
            assertTrue(i.contains(-0.0));
        }
        min = 14.25;
        drDouble.addFacet(Facets.MIN_INCLUSIVE, Double.toString(min));
        assertTrue(drDouble.getDoubleIntervals().size() == 1);
        for (DoubleInterval i : drDouble.getDoubleIntervals()) {
            assertTrue(i.isEmpty());
        }
        assertTrue(drDouble.isBottom());
    }
    
    public void testEnumerationSize() {
        DataRange dr1 = new DatatypeRestrictionDouble(DT.DOUBLE);
        double dMin = 1.0;
        double d = dMin;
        for (int i = 1; i < 5; i++) {
            d = DatatypeRestrictionDouble.nextDouble(d);
        }
        double dMax = d;
        dr1.addFacet(Facets.MIN_INCLUSIVE, "" + dMin);
        dr1.addFacet(Facets.MAX_INCLUSIVE, "" + dMax);
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        assertTrue(canonical.isFinite());
        assertFalse(canonical.isBottom());
        assertTrue(canonical.getEnumerationSize().equals(new BigInteger("5")));
    }
    
    public void testConjoinRanges1() {
        // conjoin         |------|
        // to             |-------|
        // results in      |------|
        DataRange dr1 = new DatatypeRestrictionDouble(DT.DOUBLE);
        dr1.addFacet(Facets.MIN_INCLUSIVE, "10.0");
        DataRange dr2 = new DatatypeRestrictionDouble(DT.INTEGER);
        dr2.addFacet(Facets.MIN_INCLUSIVE, "9.25");
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertTrue(canonical.isFinite());
        assertFalse(canonical.accepts(new DataConstant(Impl.IDouble, dr1.getDatatype(), "9.25")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IDouble, dr1.getDatatype(), "10.0")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IDouble, dr1.getDatatype(), "10000.0")));
        assertTrue(((DatatypeRestrictionDouble)canonical).getDoubleIntervals().size() == 1);
    }
    
    public void testConjoinRanges2() {
        // conjoin      |-----|
        // to             |---|
        // results in     |---|
        DataRange dr1 = new DatatypeRestrictionDouble(DT.DOUBLE);
        dr1.addFacet(Facets.MIN_INCLUSIVE, "10.25");
        DataRange dr2 = new DatatypeRestrictionDouble(DT.DOUBLE);
        dr2.addFacet(Facets.MIN_INCLUSIVE, "15.75");
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertTrue(canonical.isFinite());
        assertFalse(canonical.accepts(new DataConstant(Impl.IDouble, dr1.getDatatype(), "" + DatatypeRestrictionDouble.previousDouble(15.75))));
        assertTrue(canonical.accepts(new DataConstant(Impl.IDouble, dr1.getDatatype(), "15.75")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IDouble, dr1.getDatatype(), "10000.0")));
        assertTrue(((DatatypeRestrictionDouble)canonical).getDoubleIntervals().size() == 1);
    }
    
    public void testConjoinRanges3() {
        // conjoin     |---|
        // to                  |---|
        // results in      empty
        DataRange dr1 = new DatatypeRestrictionDouble(DT.DOUBLE);
        dr1.addFacet(Facets.MIN_INCLUSIVE, "10.01");
        DataRange dr2 = new DatatypeRestrictionDouble(DT.DOUBLE);
        dr2.addFacet(Facets.MAX_INCLUSIVE, "5.74");
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertTrue(canonical.isFinite());
        assertTrue(canonical.isBottom());
        assertFalse(canonical.accepts(new DataConstant(Impl.IDouble, dr1.getDatatype(), "10.01")));
        assertFalse(canonical.accepts(new DataConstant(Impl.IDouble, dr1.getDatatype(), "5.74")));
        assertTrue(canonical.getEnumerationSize().equals(BigInteger.ZERO));
    }
    
    public void testConjoinRanges6() {
        // conjoin     |-----|
        // to             |--------|
        // results in     |--|
        DataRange dr1 = new DatatypeRestrictionDouble(DT.DOUBLE);
        dr1.addFacet(Facets.MIN_INCLUSIVE, "10.0");
        DataRange dr2 = new DatatypeRestrictionDouble(DT.DOUBLE);
        dr2.addFacet(Facets.MIN_INCLUSIVE, "5.0");
        dr2.addFacet(Facets.MAX_INCLUSIVE, "15.0");
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertTrue(canonical.isFinite());
        assertFalse(canonical.isBottom());
        assertFalse(canonical.accepts(new DataConstant(Impl.IDouble, dr1.getDatatype(), "9.0")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IDouble, dr1.getDatatype(), "10.0")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IDouble, dr1.getDatatype(), "15.0")));
        assertFalse(canonical.accepts(new DataConstant(Impl.IDouble, dr1.getDatatype(), "16.0")));
        assertTrue(((DatatypeRestrictionDouble)canonical).getDoubleIntervals().size() == 1);
        assertTrue(canonical.getEnumerationSize() != null);
    }
    
    public void testConjoinNegRangesA1() {
        // conjoin      ...----|
        // to            |---| 
        // results in    |---|
        DataRange dr1 = new DatatypeRestrictionDouble(DT.DOUBLE);
        dr1.addFacet(Facets.MIN_INCLUSIVE, "5");
        dr1.addFacet(Facets.MAX_INCLUSIVE, "15");
        DataRange dr2 = new DatatypeRestrictionDouble(DT.DOUBLE);
        dr2.negate();
        dr2.addFacet(Facets.MIN_INCLUSIVE, "10.0");
        dr2.addFacet(Facets.MAX_INCLUSIVE, "20.0");
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertTrue(canonical.isFinite());
        assertFalse(canonical.isBottom());
        assertTrue(((DatatypeRestrictionDouble)canonical).getDoubleIntervals().size() == 1);
        assertTrue(canonical.accepts(new DataConstant(Impl.IDouble, dr1.getDatatype(), "" + DatatypeRestrictionDouble.previousDouble(10.0))));
        assertFalse(canonical.accepts(new DataConstant(Impl.IDouble, dr1.getDatatype(), "10.0")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IDouble, dr1.getDatatype(), "5.0")));
        assertFalse(canonical.accepts(new DataConstant(Impl.IDouble, dr1.getDatatype(), "" + DatatypeRestrictionDouble.previousDouble(5.0))));
    }

    public void testConjoinNegRangesA5() {
        // conjoin    ...---|
        // or                   |---...
        // to           |-----------| 
        // results in   |---|   |---| 
        DataRange dr1 = new DatatypeRestrictionDouble(DT.DOUBLE);
        dr1.addFacet(Facets.MIN_INCLUSIVE, "-4.5");
        dr1.addFacet(Facets.MAX_INCLUSIVE, "15.52");
        DataRange dr2 = new DatatypeRestrictionDouble(DT.DOUBLE);
        dr2.negate();
        dr2.addFacet(Facets.MIN_EXCLUSIVE, "-2.24");
        dr2.addFacet(Facets.MAX_EXCLUSIVE, "10.47854");
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertTrue(canonical.isFinite());
        assertFalse(canonical.isBottom());
        // ranges -4.5 - -2.22 and 10.47854 - 15.52
        assertFalse(canonical.accepts(new DataConstant(Impl.IDouble, dr1.getDatatype(), "-4.6")));
        assertFalse(canonical.accepts(new DataConstant(Impl.IDouble, dr1.getDatatype(), "-2.2")));
        assertFalse(canonical.accepts(new DataConstant(Impl.IDouble, dr1.getDatatype(), "10.47851")));
        assertFalse(canonical.accepts(new DataConstant(Impl.IDouble, dr1.getDatatype(), "15.521")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IDouble, dr1.getDatatype(), "-4.5")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IDouble, dr1.getDatatype(), "-2.24")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IDouble, dr1.getDatatype(), "10.47854")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IDouble, dr1.getDatatype(), "15.52")));
        assertTrue(((DatatypeRestrictionDouble)canonical).getDoubleIntervals().size() == 2);
        assertTrue(canonical.getEnumerationSize() != null);
    }
    
    public void testConjoinDecimal1() {
        // conjoin     |-----|
        // to         |---------...
        // results in  |-----|
        DataRange dr1 = new DatatypeRestrictionDouble(DT.DOUBLE);
        dr1.addFacet(Facets.MIN_INCLUSIVE, "0.04");
        dr1.addFacet(Facets.MAX_INCLUSIVE, "5.48");
        DataRange dr2 = new DatatypeRestrictionOWLRealPlus(DT.DECIMAL);
        dr2.addFacet(Facets.MIN_EXCLUSIVE, "0.1");
        dr2.addFacet(Facets.MAX_EXCLUSIVE, "4.1");
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2); 
        assertTrue(canonical.isFinite());
        assertFalse(canonical.accepts(new DataConstant(Impl.IDouble, dr1.getDatatype(), "0.1")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IDouble, dr1.getDatatype(), "" + DatatypeRestrictionDouble.nextDouble(0.1))));
        assertFalse(canonical.accepts(new DataConstant(Impl.IDouble, dr1.getDatatype(), "4.1")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IDouble, dr1.getDatatype(), "" + DatatypeRestrictionDouble.previousDouble(4.1))));
        assertTrue(((DatatypeRestrictionDouble)canonical).getDoubleIntervals().size() == 1);
    }
    
    public void testConjoinDecimal2() {
        // conjoin     |-----|
        // to         |---------...
        // results in  |-----|
        DataRange dr1 = new DatatypeRestrictionDouble(DT.DOUBLE);
        dr1.addFacet(Facets.MIN_INCLUSIVE, "0.04");
        dr1.addFacet(Facets.MAX_INCLUSIVE, "5.48");
        DataRange dr2 = new DatatypeRestrictionOWLRealPlus(DT.DECIMAL);
        dr2.addFacet(Facets.MIN_INCLUSIVE, "0.1");
        dr2.addFacet(Facets.MAX_INCLUSIVE, "4.1");
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2); 
        assertTrue(canonical.isFinite());
        assertTrue(canonical.accepts(new DataConstant(Impl.IDouble, dr1.getDatatype(), "0.1")));
        assertFalse(canonical.accepts(new DataConstant(Impl.IDouble, dr1.getDatatype(), "" + DatatypeRestrictionDouble.previousDouble(0.1))));
        assertTrue(canonical.accepts(new DataConstant(Impl.IDouble, dr1.getDatatype(), "4.1")));
        assertFalse(canonical.accepts(new DataConstant(Impl.IDouble, dr1.getDatatype(), "" + DatatypeRestrictionDouble.nextDouble(4.1))));
        assertTrue(((DatatypeRestrictionDouble)canonical).getDoubleIntervals().size() == 1);
    }
}
