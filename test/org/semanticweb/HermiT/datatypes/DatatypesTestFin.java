package org.semanticweb.HermiT.datatypes;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.HermiT.model.dataranges.CanonicalDataRange;
import org.semanticweb.HermiT.model.dataranges.DataConstant;
import org.semanticweb.HermiT.model.dataranges.DataRange;
import org.semanticweb.HermiT.model.dataranges.DatatypeRestrictionIntegerFin;
import org.semanticweb.HermiT.model.dataranges.DatatypeRestriction.DT;
import org.semanticweb.HermiT.model.dataranges.DatatypeRestriction.Facets;
import org.semanticweb.HermiT.reasoner.AbstractReasonerTest;

public class DatatypesTestFin extends AbstractReasonerTest {
    
    public DatatypesTestFin(String name) {
        super(name);
    }
    
    public void testDataConstant() throws Exception {
        DataConstant constant1 = new DataConstant(DT.STRING, "abc");
        DataConstant constant2 = new DataConstant(DT.STRING, "abc");
        DataConstant constant3 = new DataConstant(DT.INTEGER, "3");
        DataConstant constant4 = new DataConstant(DT.INTEGER, "4");
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
    
    public void testAddDataConstants() throws Exception {
        Set<DataConstant> constants = new HashSet<DataConstant>();
        constants.add(new DataConstant(DT.INTEGER, "4"));
        assertTrue(constants.size() == 1);
        try {
            BigDecimal dec = new BigDecimal("4.0");
            try {
                constants.add(new DataConstant(DT.INTEGER, dec.toBigIntegerExact().toString()));
            } catch (ArithmeticException e) {
                constants.add(new DataConstant(DT.DECIMAL, dec.toString()));
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } 
        assertTrue(constants.size() == 1);
    }
    
    public void testDatatypeRestrictionInteger() throws Exception {
        DatatypeRestrictionIntegerFin drInteger = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        drInteger.addFacet(Facets.MAX_EXCLUSIVE, "3");
        assertTrue(drInteger.getIntervals().size() == 1);
        for (DatatypeRestrictionIntegerFin.Interval i : drInteger.getIntervals()) {
            assertTrue(i.contains(2));
            assertFalse(i.contains(3));
        }
        drInteger.addFacet(Facets.MAX_EXCLUSIVE, "1");
        assertTrue(drInteger.getIntervals().size() == 1);
        for (DatatypeRestrictionIntegerFin.Interval i : drInteger.getIntervals()) {
            assertTrue(i.contains(0));
            assertFalse(i.contains(1));
        }
        drInteger.addFacet(Facets.MAX_EXCLUSIVE, "5");
        assertTrue(drInteger.getIntervals().size() == 1);
        for (DatatypeRestrictionIntegerFin.Interval i : drInteger.getIntervals()) {
            assertTrue(i.contains(0));
            assertFalse(i.contains(1));
        }
        drInteger.addFacet(Facets.MAX_INCLUSIVE, "-2");
        assertTrue(drInteger.getIntervals().size() == 1);
        for (DatatypeRestrictionIntegerFin.Interval i : drInteger.getIntervals()) {
            assertTrue(i.contains(-2));
            assertFalse(i.contains(-1));
        }
        drInteger.addFacet(Facets.MAX_INCLUSIVE, "2");
        assertTrue(drInteger.getIntervals().size() == 1);
        for (DatatypeRestrictionIntegerFin.Interval i : drInteger.getIntervals()) {
            assertTrue(i.contains(-2));
            assertFalse(i.contains(-1));
        }
        drInteger.addFacet(Facets.MIN_EXCLUSIVE, "-15");
        assertTrue(drInteger.getIntervals().size() == 1);
        assertTrue(drInteger.isFinite());
        for (DatatypeRestrictionIntegerFin.Interval i : drInteger.getIntervals()) {
            assertTrue(i.contains(-14));
            assertFalse(i.contains(-15));
            assertFalse(i.contains(-1));
            assertTrue(i.contains(-2));
        }
        drInteger.addFacet(Facets.MIN_EXCLUSIVE, "-10");
        assertTrue(drInteger.getIntervals().size() == 1);
        assertTrue(drInteger.isFinite());
        for (DatatypeRestrictionIntegerFin.Interval i : drInteger.getIntervals()) {
            assertTrue(i.contains(-9));
            assertFalse(i.contains(-10));
            assertFalse(i.contains(-1));
            assertTrue(i.contains(-2));
        }
        drInteger.addFacet(Facets.MIN_INCLUSIVE, "-2");
        assertTrue(drInteger.getIntervals().size() == 1);
        assertTrue(drInteger.isFinite());
        for (DatatypeRestrictionIntegerFin.Interval i : drInteger.getIntervals()) {
            assertTrue(i.contains(-2));
            assertFalse(i.contains(-3));
            assertFalse(i.contains(-1));
        }
    }

    public void testDatatypeRestrictionIntegerNegated() throws Exception {
        DatatypeRestrictionIntegerFin drInteger = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        drInteger.negate();
        // not smaller 3 = not smaller or equal 2
        drInteger.addFacet(Facets.MAX_EXCLUSIVE, "3");
        assertTrue(drInteger.getIntervals().size() == 1);
        assertFalse(drInteger.isFinite());
        for (DatatypeRestrictionIntegerFin.Interval i : drInteger.getIntervals()) {
            assertFalse(i.contains(3));
            assertTrue(i.contains(2));
        }
        // and not smaller 1 = and not smaller or equal 0
        drInteger.addFacet(Facets.MAX_EXCLUSIVE, "1");
        assertTrue(drInteger.getIntervals().size() == 1);
        assertFalse(drInteger.isFinite());
        for (DatatypeRestrictionIntegerFin.Interval i : drInteger.getIntervals()) {
            assertFalse(i.contains(1));
            assertTrue(i.contains(0));
        }
        // and not smaller 5 = and not smaller or equal 4
        drInteger.addFacet(Facets.MAX_EXCLUSIVE, "5");
        assertTrue(drInteger.getIntervals().size() == 1);
        assertFalse(drInteger.isFinite());
        for (DatatypeRestrictionIntegerFin.Interval i : drInteger.getIntervals()) {
            assertFalse(i.contains(1));
            assertTrue(i.contains(0));
        }
        // and not smaller or equal -2
        drInteger.addFacet(Facets.MAX_INCLUSIVE, "-2");
        assertTrue(drInteger.getIntervals().size() == 1);
        assertFalse(drInteger.isFinite());
        for (DatatypeRestrictionIntegerFin.Interval i : drInteger.getIntervals()) {
            assertFalse(i.contains(-1));
            assertTrue(i.contains(-2));
        }
        // and not smaller or equal 2 = smaller or equal 1
        drInteger.addFacet(Facets.MAX_INCLUSIVE, "2");
        assertTrue(drInteger.getIntervals().size() == 1);
        assertFalse(drInteger.isFinite());
        for (DatatypeRestrictionIntegerFin.Interval i : drInteger.getIntervals()) {
            assertFalse(i.contains(-1));
            assertTrue(i.contains(-2));
        }
        // and not greater -5 = and not greater or equal -4
        drInteger.addFacet(Facets.MIN_EXCLUSIVE, "-5");
        assertTrue(drInteger.getIntervals().size() == 1);
        assertFalse(drInteger.isFinite());
        for (DatatypeRestrictionIntegerFin.Interval i : drInteger.getIntervals()) {
            assertTrue(i.getMin() == -4);
            assertTrue(i.getMax() == -2);
        }
        // and not greater -7 = and not greater or equal -6
        drInteger.addFacet(Facets.MIN_EXCLUSIVE, "-7");
        assertTrue(drInteger.getIntervals().size() == 1);
        assertFalse(drInteger.isFinite());
        for (DatatypeRestrictionIntegerFin.Interval i : drInteger.getIntervals()) {
            assertTrue(i.getMin() == -4);
            assertTrue(i.getMax() == -2);
        }
        // and not greater or equal 5
        drInteger.addFacet(Facets.MIN_INCLUSIVE, "5");
        assertTrue(drInteger.getIntervals().size() == 1);
        assertTrue(drInteger.isFinite());
        assertTrue(drInteger.isBottom());
        for (DatatypeRestrictionIntegerFin.Interval i : drInteger.getIntervals()) {
            assertTrue(i.getMin() == 5);
            assertTrue(i.getMax() == -2);
        }
    }
    
    public void testConjoinRanges1() {
        // conjoin     |-----...
        // to             |------...
        // results in     |------...
        DataRange dr1 = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        dr1.addFacet(Facets.MIN_INCLUSIVE, "10");
        DataRange dr2 = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        dr2.addFacet(Facets.MIN_INCLUSIVE, "9");
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "9")));
        assertTrue(canonical.accepts(new DataConstant(dr1.getDatatype(), "10")));
        assertTrue(canonical.accepts(new DataConstant(dr1.getDatatype(), "10000")));
        assertTrue(((DatatypeRestrictionIntegerFin)canonical).getIntervals().size() == 1);
    }
    
    public void testConjoinRanges2() {
        // conjoin           |-----...
        // to             |------...
        // results in        |-----...
        DataRange dr1 = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        dr1.addFacet(Facets.MIN_INCLUSIVE, "10");
        DataRange dr2 = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        dr2.addFacet(Facets.MIN_INCLUSIVE, "15");
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "14")));
        assertTrue(canonical.accepts(new DataConstant(dr1.getDatatype(), "15")));
        assertTrue(canonical.accepts(new DataConstant(dr1.getDatatype(), "10000")));
        assertTrue(((DatatypeRestrictionIntegerFin)canonical).getIntervals().size() == 1);
    }
    
    public void testConjoinRanges3() {
        // conjoin     ...---|
        // to                   |---...
        // results in      empty
        DataRange dr1 = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        dr1.addFacet(Facets.MIN_INCLUSIVE, "10");
        DataRange dr2 = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        dr2.addFacet(Facets.MAX_INCLUSIVE, "5");
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertTrue(canonical.isFinite());
        assertTrue(canonical.isBottom());
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "10")));
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "5")));
        assertTrue(canonical.getEnumerationSize().equals(BigInteger.ZERO));
    }
    
    public void testConjoinRanges4() {
        // conjoin     ...----|
        // to              |----...
        // results in      |--|
        DataRange dr1 = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        dr1.addFacet(Facets.MIN_INCLUSIVE, "10");
        DataRange dr2 = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        dr2.addFacet(Facets.MAX_INCLUSIVE, "15");
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertTrue(canonical.isFinite());
        assertFalse(canonical.isBottom());
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "9")));
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "16")));
        assertTrue(canonical.accepts(new DataConstant(dr1.getDatatype(), "10")));
        assertTrue(canonical.accepts(new DataConstant(dr1.getDatatype(), "15")));
        assertTrue(((DatatypeRestrictionIntegerFin)canonical).getIntervals().size() == 1);
        assertTrue(canonical.getEnumerationSize().equals(new BigInteger("6")));
    }
    
    public void testConjoinRanges5() {
        // conjoin     |---|
        // to                 |-------...
        // results in     empty
        DataRange dr1 = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        dr1.addFacet(Facets.MIN_INCLUSIVE, "10");
        DataRange dr2 = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        dr2.addFacet(Facets.MIN_INCLUSIVE, "4");
        dr2.addFacet(Facets.MAX_INCLUSIVE, "8");
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertTrue(canonical.isFinite());
        assertTrue(canonical.isBottom());
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "3")));
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "4")));
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "8")));
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "10")));
        assertTrue(((DatatypeRestrictionIntegerFin)canonical).getIntervals().size() == 1);
        assertTrue(canonical.getEnumerationSize().equals(BigInteger.ZERO));
    }
    
    public void testConjoinRanges6() {
        // conjoin     |-----|
        // to             |------...
        // results in     |--|
        DataRange dr1 = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        dr1.addFacet(Facets.MIN_INCLUSIVE, "10");
        DataRange dr2 = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        dr2.addFacet(Facets.MIN_INCLUSIVE, "5");
        dr2.addFacet(Facets.MAX_INCLUSIVE, "15");
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertTrue(canonical.isFinite());
        assertFalse(canonical.isBottom());
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "9")));
        assertTrue(canonical.accepts(new DataConstant(dr1.getDatatype(), "10")));
        assertTrue(canonical.accepts(new DataConstant(dr1.getDatatype(), "15")));
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "16")));
        assertTrue(((DatatypeRestrictionIntegerFin)canonical).getIntervals().size() == 1);
        assertTrue(canonical.getEnumerationSize().equals(new BigInteger("6")));
    }
    
    public void testConjoinRanges7() {
        // conjoin          |---|
        // to             |------...
        // results in       |---|
        DataRange dr1 = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        dr1.addFacet(Facets.MIN_INCLUSIVE, "10");
        DataRange dr2 = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        dr2.addFacet(Facets.MIN_INCLUSIVE, "15");
        dr2.addFacet(Facets.MAX_INCLUSIVE, "20");
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertTrue(canonical.isFinite());
        assertFalse(canonical.isBottom());
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "14")));
        assertTrue(canonical.accepts(new DataConstant(dr1.getDatatype(), "15")));
        assertTrue(canonical.accepts(new DataConstant(dr1.getDatatype(), "20")));
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "21")));
        assertTrue(((DatatypeRestrictionIntegerFin)canonical).getIntervals().size() == 1);
        assertTrue(canonical.getEnumerationSize().equals(new BigInteger("6")));
    }
    
    public void testConjoinRangesB1() {
        // conjoin      ...-----|
        // to          ...----|
        // results in  ...----|
        DataRange dr1 = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        dr1.addFacet(Facets.MAX_INCLUSIVE, "10");
        DataRange dr2 = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        dr2.addFacet(Facets.MAX_INCLUSIVE, "15");
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "11")));
        assertTrue(canonical.accepts(new DataConstant(dr1.getDatatype(), "10")));
        assertTrue(canonical.accepts(new DataConstant(dr1.getDatatype(), "-10000")));
        assertTrue(((DatatypeRestrictionIntegerFin)canonical).getIntervals().size() == 1);
    }
    
    public void testConjoinRangesB2() {
        // conjoin      ...----|
        // to              ...----|
        // results in   ...----|
        DataRange dr1 = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        dr1.addFacet(Facets.MAX_INCLUSIVE, "10");
        DataRange dr2 = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        dr2.addFacet(Facets.MAX_INCLUSIVE, "15");
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "11")));
        assertTrue(canonical.accepts(new DataConstant(dr1.getDatatype(), "10")));
        assertTrue(canonical.accepts(new DataConstant(dr1.getDatatype(), "-10000")));
        assertTrue(((DatatypeRestrictionIntegerFin)canonical).getIntervals().size() == 1);
    }
    
    public void testConjoinRangesB3() {
        // conjoin             |---...
        // to          ...---|
        // results in      empty
        DataRange dr1 = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        dr1.addFacet(Facets.MAX_INCLUSIVE, "10");
        DataRange dr2 = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        dr2.addFacet(Facets.MIN_INCLUSIVE, "15");
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertTrue(canonical.isFinite());
        assertTrue(canonical.isBottom());
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "10")));
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "5")));
        assertTrue(canonical.getEnumerationSize().equals(BigInteger.ZERO));
    }
    
    public void testConjoinRangesB4() {
        // conjoin         |---...
        // to          ...----|
        // results in      |--|
        DataRange dr1 = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        dr1.addFacet(Facets.MAX_INCLUSIVE, "10");
        DataRange dr2 = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        dr2.addFacet(Facets.MIN_INCLUSIVE, "5");
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertTrue(canonical.isFinite());
        assertFalse(canonical.isBottom());
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "4")));
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "11")));
        assertTrue(canonical.accepts(new DataConstant(dr1.getDatatype(), "5")));
        assertTrue(canonical.accepts(new DataConstant(dr1.getDatatype(), "10")));
        assertTrue(((DatatypeRestrictionIntegerFin)canonical).getIntervals().size() == 1);
        assertTrue(canonical.getEnumerationSize().equals(new BigInteger("6")));
    }
    
    public void testConjoinRangesB5() {
        // conjoin               |---|
        // to          ...----|
        // results in     empty
        DataRange dr1 = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        dr1.addFacet(Facets.MAX_INCLUSIVE, "10");
        DataRange dr2 = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        dr2.addFacet(Facets.MIN_INCLUSIVE, "15");
        dr2.addFacet(Facets.MAX_INCLUSIVE, "20");
        assertTrue(((CanonicalDataRange) dr2).isFinite());
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertTrue(canonical.isFinite());
        assertTrue(canonical.isBottom());
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "10")));
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "15")));
        assertTrue(((DatatypeRestrictionIntegerFin)canonical).getIntervals().size() == 1);
        assertTrue(canonical.getEnumerationSize().equals(BigInteger.ZERO));
    }
    
    public void testConjoinRangesB6() {
        // conjoin         |-----|
        // to          ...----|
        // results in      |--|
        DataRange dr1 = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        dr1.addFacet(Facets.MAX_INCLUSIVE, "10");
        DataRange dr2 = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        dr2.addFacet(Facets.MIN_INCLUSIVE, "5");
        dr2.addFacet(Facets.MAX_INCLUSIVE, "15");
        assertTrue(((CanonicalDataRange) dr2).isFinite());
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertTrue(canonical.isFinite());
        assertFalse(canonical.isBottom());
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "4")));
        assertTrue(canonical.accepts(new DataConstant(dr1.getDatatype(), "5")));
        assertTrue(canonical.accepts(new DataConstant(dr1.getDatatype(), "10")));
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "11")));
        assertTrue(((DatatypeRestrictionIntegerFin)canonical).getIntervals().size() == 1);
        assertTrue(canonical.getEnumerationSize().equals(new BigInteger("6")));
    }
    
    public void testConjoinRangesB7() {
        // conjoin         |---|
        // to           ...------|
        // results in      |---|
        DataRange dr1 = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        dr1.addFacet(Facets.MAX_INCLUSIVE, "10");
        DataRange dr2 = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        dr2.addFacet(Facets.MIN_INCLUSIVE, "0");
        dr2.addFacet(Facets.MAX_INCLUSIVE, "5");
        assertTrue(((CanonicalDataRange) dr2).isFinite());
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertTrue(canonical.isFinite());
        assertFalse(canonical.isBottom());
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "-1")));
        assertTrue(canonical.accepts(new DataConstant(dr1.getDatatype(), "0")));
        assertTrue(canonical.accepts(new DataConstant(dr1.getDatatype(), "5")));
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "6")));
        assertTrue(((DatatypeRestrictionIntegerFin)canonical).getIntervals().size() == 1);
        assertTrue(canonical.getEnumerationSize().equals(new BigInteger("6")));
    }
    
    public void testConjoinRangesC1() {
        // conjoin     ...---|
        // to                   |---|
        // results in      empty
        DataRange dr1 = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        dr1.addFacet(Facets.MIN_INCLUSIVE, "10");
        dr1.addFacet(Facets.MAX_INCLUSIVE, "15");
        DataRange dr2 = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        dr2.addFacet(Facets.MAX_INCLUSIVE, "5");
        assertTrue(((CanonicalDataRange) dr1).isFinite());
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertTrue(canonical.isFinite());
        assertTrue(canonical.isBottom());
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "10")));
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "5")));
        assertTrue(canonical.getEnumerationSize().equals(BigInteger.ZERO));
    }
    
    public void testConjoinRangesC2() {
        // conjoin     ...----|
        // to              |----|
        // results in      |--|
        DataRange dr1 = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        dr1.addFacet(Facets.MIN_INCLUSIVE, "10");
        dr1.addFacet(Facets.MAX_INCLUSIVE, "15");
        DataRange dr2 = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        dr2.addFacet(Facets.MAX_INCLUSIVE, "12");
        assertTrue(((CanonicalDataRange) dr1).isFinite());
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertTrue(canonical.isFinite());
        assertFalse(canonical.isBottom());
        assertTrue(canonical.accepts(new DataConstant(dr1.getDatatype(), "10")));
        assertTrue(canonical.accepts(new DataConstant(dr1.getDatatype(), "12")));
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "9")));
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "13")));
        assertTrue(canonical.getEnumerationSize().equals(new BigInteger("3")));
    }
    
    public void testConjoinRangesC3() {
        // conjoin     ...---------|
        // to              |----|
        // results in      |----|
        DataRange dr1 = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        dr1.addFacet(Facets.MIN_INCLUSIVE, "10");
        dr1.addFacet(Facets.MAX_INCLUSIVE, "15");
        DataRange dr2 = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        dr2.addFacet(Facets.MAX_INCLUSIVE, "20");
        assertTrue(((CanonicalDataRange) dr1).isFinite());
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertTrue(canonical.isFinite());
        assertFalse(canonical.isBottom());
        assertTrue(canonical.accepts(new DataConstant(dr1.getDatatype(), "10")));
        assertTrue(canonical.accepts(new DataConstant(dr1.getDatatype(), "15")));
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "9")));
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "16")));
        assertTrue(canonical.getEnumerationSize().equals(new BigInteger("6")));
    }
    
    public void testConjoinRangesC4() {
        // conjoin             |---...
        // to          |---|
        // results in      empty
        DataRange dr1 = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        dr1.addFacet(Facets.MIN_INCLUSIVE, "10");
        dr1.addFacet(Facets.MAX_INCLUSIVE, "15");
        DataRange dr2 = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        dr2.addFacet(Facets.MIN_INCLUSIVE, "20");
        assertTrue(((CanonicalDataRange) dr1).isFinite());
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertTrue(canonical.isFinite());
        assertTrue(canonical.isBottom());
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "10")));
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "20")));
        assertTrue(canonical.getEnumerationSize().equals(BigInteger.ZERO));
    }
    
    public void testConjoinRangesC5() {
        // conjoin         |----...
        // to          |------|
        // results in      |--|
        DataRange dr1 = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        dr1.addFacet(Facets.MIN_INCLUSIVE, "10");
        dr1.addFacet(Facets.MAX_INCLUSIVE, "15");
        DataRange dr2 = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        dr2.addFacet(Facets.MIN_INCLUSIVE, "12");
        assertTrue(((CanonicalDataRange) dr1).isFinite());
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertTrue(canonical.isFinite());
        assertFalse(canonical.isBottom());
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "11")));
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "16")));
        assertTrue(canonical.accepts(new DataConstant(dr1.getDatatype(), "12")));
        assertTrue(canonical.accepts(new DataConstant(dr1.getDatatype(), "15")));
        assertTrue(((DatatypeRestrictionIntegerFin)canonical).getIntervals().size() == 1);
        assertTrue(canonical.getEnumerationSize().equals(new BigInteger("4")));
    }
    
    public void testConjoinRangesC6() {
        // conjoin         |----...
        // to          |------|
        // results in      |--|
        DataRange dr1 = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        dr1.addFacet(Facets.MIN_INCLUSIVE, "10");
        dr1.addFacet(Facets.MAX_INCLUSIVE, "15");
        DataRange dr2 = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        dr2.addFacet(Facets.MIN_INCLUSIVE, "5");
        assertTrue(((CanonicalDataRange) dr1).isFinite());
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertTrue(canonical.isFinite());
        assertFalse(canonical.isBottom());
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "9")));
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "16")));
        assertTrue(canonical.accepts(new DataConstant(dr1.getDatatype(), "10")));
        assertTrue(canonical.accepts(new DataConstant(dr1.getDatatype(), "15")));
        assertTrue(((DatatypeRestrictionIntegerFin)canonical).getIntervals().size() == 1);
        assertTrue(canonical.getEnumerationSize().equals(new BigInteger("6")));
    }
    
    public void testConjoinRangesC7() {
        // conjoin            |---|
        // to          |---| 
        // results in      empty
        DataRange dr1 = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        dr1.addFacet(Facets.MIN_INCLUSIVE, "5");
        dr1.addFacet(Facets.MAX_INCLUSIVE, "9");
        DataRange dr2 = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        dr2.addFacet(Facets.MIN_INCLUSIVE, "10");
        dr2.addFacet(Facets.MAX_INCLUSIVE, "15");
        assertTrue(((CanonicalDataRange) dr1).isFinite());
        assertTrue(((CanonicalDataRange) dr2).isFinite());
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertTrue(canonical.isFinite());
        assertTrue(canonical.isBottom());
        assertTrue(((DatatypeRestrictionIntegerFin)canonical).getIntervals().size() == 1);
        assertTrue(canonical.getEnumerationSize().equals(BigInteger.ZERO));
    }
    
    public void testConjoinRangesC8() {
        // conjoin        |-----|
        // to          |-----| 
        // results in     |--|
        DataRange dr1 = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        dr1.addFacet(Facets.MIN_INCLUSIVE, "5");
        dr1.addFacet(Facets.MAX_INCLUSIVE, "10");
        DataRange dr2 = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        dr2.addFacet(Facets.MIN_INCLUSIVE, "8");
        dr2.addFacet(Facets.MAX_INCLUSIVE, "15");
        assertTrue(((CanonicalDataRange) dr1).isFinite());
        assertTrue(((CanonicalDataRange) dr2).isFinite());
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertTrue(canonical.isFinite());
        assertFalse(canonical.isBottom());
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "7")));
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "11")));
        assertTrue(canonical.accepts(new DataConstant(dr1.getDatatype(), "8")));
        assertTrue(canonical.accepts(new DataConstant(dr1.getDatatype(), "10")));
        assertTrue(((DatatypeRestrictionIntegerFin)canonical).getIntervals().size() == 1);
        assertTrue(canonical.getEnumerationSize().equals(new BigInteger("3")));
    }
    
    public void testConjoinRangesC9() {
        // conjoin     |-------|
        // to            |---| 
        // results in    |---|
        DataRange dr1 = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        dr1.addFacet(Facets.MIN_INCLUSIVE, "5");
        dr1.addFacet(Facets.MAX_INCLUSIVE, "20");
        DataRange dr2 = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        dr2.addFacet(Facets.MIN_INCLUSIVE, "10");
        dr2.addFacet(Facets.MAX_INCLUSIVE, "15");
        assertTrue(((CanonicalDataRange) dr1).isFinite());
        assertTrue(((CanonicalDataRange) dr2).isFinite());
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertTrue(canonical.isFinite());
        assertFalse(canonical.isBottom());
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "9")));
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "16")));
        assertTrue(canonical.accepts(new DataConstant(dr1.getDatatype(), "10")));
        assertTrue(canonical.accepts(new DataConstant(dr1.getDatatype(), "15")));
        assertTrue(((DatatypeRestrictionIntegerFin)canonical).getIntervals().size() == 1);
        assertTrue(canonical.getEnumerationSize().equals(new BigInteger("6")));
    }
    
    public void testConjoinRangesC10() {
        // conjoin        |---|
        // to          |--------| 
        // results in     |---|
        DataRange dr1 = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        dr1.addFacet(Facets.MIN_INCLUSIVE, "12");
        dr1.addFacet(Facets.MAX_INCLUSIVE, "13");
        DataRange dr2 = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        dr2.addFacet(Facets.MIN_INCLUSIVE, "10");
        dr2.addFacet(Facets.MAX_INCLUSIVE, "15");
        assertTrue(((CanonicalDataRange) dr1).isFinite());
        assertTrue(((CanonicalDataRange) dr2).isFinite());
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertTrue(canonical.isFinite());
        assertFalse(canonical.isBottom());
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "11")));
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "14")));
        assertTrue(canonical.accepts(new DataConstant(dr1.getDatatype(), "12")));
        assertTrue(canonical.accepts(new DataConstant(dr1.getDatatype(), "13")));
        assertTrue(((DatatypeRestrictionIntegerFin)canonical).getIntervals().size() == 1);
        assertTrue(canonical.getEnumerationSize().equals(new BigInteger("2")));
    }
    
    public void testConjoinRangesC11() {
        // conjoin        |-----|
        // to          |-----| 
        // results in     |--|
        DataRange dr1 = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        dr1.addFacet(Facets.MIN_INCLUSIVE, "5");
        dr1.addFacet(Facets.MAX_INCLUSIVE, "15");
        DataRange dr2 = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        dr2.addFacet(Facets.MIN_INCLUSIVE, "10");
        dr2.addFacet(Facets.MAX_INCLUSIVE, "20");
        assertTrue(((CanonicalDataRange) dr1).isFinite());
        assertTrue(((CanonicalDataRange) dr2).isFinite());
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertTrue(canonical.isFinite());
        assertFalse(canonical.isBottom());
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "9")));
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "16")));
        assertTrue(canonical.accepts(new DataConstant(dr1.getDatatype(), "10")));
        assertTrue(canonical.accepts(new DataConstant(dr1.getDatatype(), "15")));
        assertTrue(((DatatypeRestrictionIntegerFin)canonical).getIntervals().size() == 1);
        assertTrue(canonical.getEnumerationSize().equals(new BigInteger("6")));
    }
    
    public void testConjoinRangesC12() {
        // conjoin           |---|
        // to          |---| 
        // results in     empty
        DataRange dr1 = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        dr1.addFacet(Facets.MIN_INCLUSIVE, "5");
        dr1.addFacet(Facets.MAX_INCLUSIVE, "15");
        DataRange dr2 = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        dr2.addFacet(Facets.MIN_INCLUSIVE, "20");
        dr2.addFacet(Facets.MAX_INCLUSIVE, "30");
        assertTrue(((CanonicalDataRange) dr1).isFinite());
        assertTrue(((CanonicalDataRange) dr2).isFinite());
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertTrue(canonical.isFinite());
        assertTrue(canonical.isBottom());
        assertTrue(((DatatypeRestrictionIntegerFin)canonical).getIntervals().size() == 1);
        assertTrue(canonical.getEnumerationSize().equals(BigInteger.ZERO));
    }
    
    public void testConjoinNegRangesA1() {
        // conjoin      ...----|
        // to            |---| 
        // results in    |---|
        DataRange dr1 = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        dr1.addFacet(Facets.MIN_INCLUSIVE, "5");
        dr1.addFacet(Facets.MAX_INCLUSIVE, "15");
        DataRange dr2 = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        dr2.negate();
        dr2.addFacet(Facets.MIN_INCLUSIVE, "20");
        assertTrue(((CanonicalDataRange) dr1).isFinite());
        assertFalse(((CanonicalDataRange) dr2).isFinite());
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertTrue(canonical.isFinite());
        assertFalse(canonical.isBottom());
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "4")));
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "16")));
        assertTrue(canonical.accepts(new DataConstant(dr1.getDatatype(), "10")));
        assertTrue(canonical.accepts(new DataConstant(dr1.getDatatype(), "15")));
        assertTrue(((DatatypeRestrictionIntegerFin)canonical).getIntervals().size() == 1);
        assertTrue(canonical.getEnumerationSize().equals(new BigInteger("11")));
    }
    
    public void testConjoinNegRangesA2() {
        // conjoin           |-----...
        // to            |-------| 
        // results in        |---|
        DataRange dr1 = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        dr1.addFacet(Facets.MIN_INCLUSIVE, "5");
        dr1.addFacet(Facets.MAX_INCLUSIVE, "15");
        DataRange dr2 = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        dr2.negate();
        dr2.addFacet(Facets.MAX_INCLUSIVE, "10");
        assertTrue(((CanonicalDataRange) dr1).isFinite());
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertTrue(canonical.isFinite());
        assertFalse(canonical.isBottom());
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "10")));
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "16")));
        assertTrue(canonical.accepts(new DataConstant(dr1.getDatatype(), "11")));
        assertTrue(canonical.accepts(new DataConstant(dr1.getDatatype(), "15")));
        assertTrue(((DatatypeRestrictionIntegerFin)canonical).getIntervals().size() == 1);
        assertTrue(canonical.getEnumerationSize().equals(new BigInteger("5")));
    }
    
    public void testConjoinNegRangesA3() {
        // conjoin     ...---|
        // to            |-------| 
        // results in    |---|
        DataRange dr1 = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        dr1.addFacet(Facets.MIN_INCLUSIVE, "5");
        dr1.addFacet(Facets.MAX_INCLUSIVE, "15");
        DataRange dr2 = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        dr2.negate();
        dr2.addFacet(Facets.MIN_INCLUSIVE, "10");
        assertTrue(((CanonicalDataRange) dr1).isFinite());
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertTrue(canonical.isFinite());
        assertFalse(canonical.isBottom());
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "4")));
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "10")));
        assertTrue(canonical.accepts(new DataConstant(dr1.getDatatype(), "5")));
        assertTrue(canonical.accepts(new DataConstant(dr1.getDatatype(), "9")));
        assertTrue(((DatatypeRestrictionIntegerFin)canonical).getIntervals().size() == 1);
        assertTrue(canonical.getEnumerationSize().equals(new BigInteger("5")));
    }
    
    public void testConjoinNegRangesA4() {
        // conjoin     ...-----|
        // or               |-------...
        // to           |----------| 
        // results in   |----------| since the conjoined is top
        DataRange dr1 = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        dr1.addFacet(Facets.MIN_INCLUSIVE, "0");
        dr1.addFacet(Facets.MAX_INCLUSIVE, "15");
        DataRange dr2 = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        dr2.negate();
        dr2.addFacet(Facets.MIN_INCLUSIVE, "10"); // <= 9
        dr2.addFacet(Facets.MAX_INCLUSIVE, "5"); // or >= 6 (trivially true)
        assertTrue(((CanonicalDataRange) dr1).isFinite());
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertTrue(canonical.isFinite());
        assertFalse(canonical.isBottom());
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "-1")));
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "16")));
        assertTrue(canonical.accepts(new DataConstant(dr1.getDatatype(), "0")));
        assertTrue(canonical.accepts(new DataConstant(dr1.getDatatype(), "15")));
        assertTrue(((DatatypeRestrictionIntegerFin)canonical).getIntervals().size() == 1);
        assertTrue(canonical.getEnumerationSize().equals(new BigInteger("16")));
    }
    
    public void testConjoinNegRangesA5() {
        // conjoin    ...---|
        // or                   |---...
        // to           |-----------| 
        // results in   |---|  |---| 
        DataRange dr1 = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        dr1.addFacet(Facets.MIN_INCLUSIVE, "0");
        dr1.addFacet(Facets.MAX_INCLUSIVE, "15");
        DataRange dr2 = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        dr2.negate();
        dr2.addFacet(Facets.MIN_INCLUSIVE, "5"); // <= 4
        dr2.addFacet(Facets.MAX_INCLUSIVE, "10"); // or >= 11
        assertTrue(((CanonicalDataRange) dr1).isFinite());
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertTrue(canonical.isFinite());
        assertFalse(canonical.isBottom());
        // ranges 0-4 and 11-15
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "-1")));
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "5")));
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "10")));
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "16")));
        assertTrue(canonical.accepts(new DataConstant(dr1.getDatatype(), "0")));
        assertTrue(canonical.accepts(new DataConstant(dr1.getDatatype(), "4")));
        assertTrue(canonical.accepts(new DataConstant(dr1.getDatatype(), "11")));
        assertTrue(canonical.accepts(new DataConstant(dr1.getDatatype(), "15")));
        assertTrue(((DatatypeRestrictionIntegerFin)canonical).getIntervals().size() == 2);
        assertTrue(canonical.getEnumerationSize().equals(new BigInteger("10")));
    }
    
    public void testConjoinTwoNegRanges1() {
        // conjoin    ...-----|
        // or                   |---...
        // and 
        //               ...-------|
        // or                         |---...
        // to           |----------------| 
        // results in   |-----| |--|  |--| 
        DataRange dr1 = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        dr1.addFacet(Facets.MIN_INCLUSIVE, "0");
        dr1.addFacet(Facets.MAX_INCLUSIVE, "100");
        DataRange dr2 = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        dr2.negate();
        dr2.addFacet(Facets.MIN_INCLUSIVE, "10"); // <= 9
        dr2.addFacet(Facets.MAX_INCLUSIVE, "20"); // or >= 21
        DataRange dr3 = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        dr3.negate();
        dr3.addFacet(Facets.MIN_INCLUSIVE, "30"); // <= 29
        dr3.addFacet(Facets.MAX_INCLUSIVE, "40"); // or >= 41
        assertTrue(((CanonicalDataRange) dr1).isFinite());
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertTrue(canonical.isFinite());
        assertFalse(canonical.isBottom());
        // ranges 0-9 and 21-100
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "-1")));
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "10")));
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "20")));
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "101")));
        assertTrue(canonical.accepts(new DataConstant(dr1.getDatatype(), "0")));
        assertTrue(canonical.accepts(new DataConstant(dr1.getDatatype(), "9")));
        assertTrue(canonical.accepts(new DataConstant(dr1.getDatatype(), "21")));
        assertTrue(canonical.accepts(new DataConstant(dr1.getDatatype(), "100")));
        assertTrue(((DatatypeRestrictionIntegerFin)canonical).getIntervals().size() == 2);
        assertTrue(canonical.getEnumerationSize().equals(new BigInteger("90")));
        canonical.conjoinFacetsFrom(dr3);
        assertTrue(canonical.isFinite());
        assertFalse(canonical.isBottom());
        // ranges 0-9 and 21-29 and 41-100
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "-1")));
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "10")));
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "20")));
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "30")));
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "40")));
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "101")));
        assertTrue(canonical.accepts(new DataConstant(dr1.getDatatype(), "0")));
        assertTrue(canonical.accepts(new DataConstant(dr1.getDatatype(), "9")));
        assertTrue(canonical.accepts(new DataConstant(dr1.getDatatype(), "21")));
        assertTrue(canonical.accepts(new DataConstant(dr1.getDatatype(), "29")));
        assertTrue(canonical.accepts(new DataConstant(dr1.getDatatype(), "41")));
        assertTrue(canonical.accepts(new DataConstant(dr1.getDatatype(), "100")));
        assertTrue(((DatatypeRestrictionIntegerFin)canonical).getIntervals().size() == 3);
        assertTrue(canonical.getEnumerationSize().equals(new BigInteger("79")));
    }
    
    public void testConjoinTwoNegRangesAndPositive() {
        // conjoin    ...----|
        // or                   |---...
        // and 
        //               ...-------|
        // or                         |---...
        // and                   |---------|
        // to           |----------------| 
        // results in           |--|  |--| 
        DataRange dr1 = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        dr1.addFacet(Facets.MIN_INCLUSIVE, "0");
        dr1.addFacet(Facets.MAX_INCLUSIVE, "100");
        DataRange dr2 = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        dr2.negate();
        dr2.addFacet(Facets.MIN_INCLUSIVE, "10"); // <= 9
        dr2.addFacet(Facets.MAX_INCLUSIVE, "20"); // or >= 21
        DataRange dr3 = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        dr3.negate();
        dr3.addFacet(Facets.MIN_INCLUSIVE, "30"); // <= 29
        dr3.addFacet(Facets.MAX_INCLUSIVE, "40"); // or >= 41
        DataRange dr4 = new DatatypeRestrictionIntegerFin(DT.INTEGER);
        dr4.addFacet(Facets.MIN_INCLUSIVE, "25"); // >= 25
        dr4.addFacet(Facets.MAX_INCLUSIVE, "110"); // and >= 110
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        canonical.conjoinFacetsFrom(dr3);
        canonical.conjoinFacetsFrom(dr4);
        assertTrue(((DatatypeRestrictionIntegerFin)canonical).getIntervals().size() == 2);
        assertTrue(canonical.isFinite());
        // ranges 25-29 and 41-100
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "24")));
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "30")));
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "40")));
        assertFalse(canonical.accepts(new DataConstant(dr1.getDatatype(), "101")));
        assertTrue(canonical.accepts(new DataConstant(dr1.getDatatype(), "25")));
        assertTrue(canonical.accepts(new DataConstant(dr1.getDatatype(), "29")));
        assertTrue(canonical.accepts(new DataConstant(dr1.getDatatype(), "41")));
        assertTrue(canonical.accepts(new DataConstant(dr1.getDatatype(), "100")));
        assertTrue(canonical.getEnumerationSize().equals(new BigInteger("65")));
    }
}
