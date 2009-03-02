package org.semanticweb.HermiT.datatypes;

import java.math.BigInteger;

import org.semanticweb.HermiT.model.dataranges.CanonicalDataRange;
import org.semanticweb.HermiT.model.dataranges.DataConstant;
import org.semanticweb.HermiT.model.dataranges.DataRange;
import org.semanticweb.HermiT.model.dataranges.DatatypeRestrictionDouble;
import org.semanticweb.HermiT.model.dataranges.DatatypeRestrictionInteger;
import org.semanticweb.HermiT.model.dataranges.DatatypeRestrictionOWLRealPlus;
import org.semanticweb.HermiT.model.dataranges.IntegerInterval;
import org.semanticweb.HermiT.model.dataranges.IntegerIntervalBig;
import org.semanticweb.HermiT.model.dataranges.IntegerIntervalFin;
import org.semanticweb.HermiT.model.dataranges.DataConstant.Impl;
import org.semanticweb.HermiT.model.dataranges.DatatypeRestriction.DT;
import org.semanticweb.HermiT.model.dataranges.DatatypeRestriction.Facet;
import org.semanticweb.HermiT.reasoner.AbstractReasonerTest;

public class IntegersTest extends AbstractReasonerTest {

    public IntegersTest(String name) {
        super(name);
    }
    
    public void testDatatypeRestrictionInteger() throws Exception {
        DatatypeRestrictionInteger drInteger = new DatatypeRestrictionInteger(DT.INTEGER);
        drInteger.addFacet(Facet.MAX_EXCLUSIVE, "2.735");
        assertTrue(drInteger.getIntegerIntervals().size() == 1);
        assertFalse(drInteger.isFinite());
        for (IntegerInterval i : drInteger.getIntegerIntervals()) {
            assertTrue(i.contains(new BigInteger("2")));
            assertFalse(i.contains(new BigInteger("3")));
        }
        drInteger.addFacet(Facet.MAX_EXCLUSIVE, "1.1");
        assertTrue(drInteger.getIntegerIntervals().size() == 1);
        assertFalse(drInteger.isFinite());
        for (IntegerInterval i : drInteger.getIntegerIntervals()) {
            assertTrue(i.contains(new Integer("1")));
            assertFalse(i.contains(new Integer("2")));
        }
        drInteger.addFacet(Facet.MAX_EXCLUSIVE, "1");
        assertTrue(drInteger.getIntegerIntervals().size() == 1);
        assertFalse(drInteger.isFinite());
        for (IntegerInterval i : drInteger.getIntegerIntervals()) {
            assertTrue(i.contains(BigInteger.ZERO));
            assertFalse(i.contains(BigInteger.ONE));
        }
        drInteger.addFacet(Facet.MAX_EXCLUSIVE, "5");
        assertTrue(drInteger.getIntegerIntervals().size() == 1);
        assertFalse(drInteger.isFinite());
        for (IntegerInterval i : drInteger.getIntegerIntervals()) {
            assertTrue(i.contains(BigInteger.ZERO));
            assertFalse(i.contains(BigInteger.ONE));
        }
        drInteger.addFacet(Facet.MAX_INCLUSIVE, "-2");
        assertTrue(drInteger.getIntegerIntervals().size() == 1);
        assertFalse(drInteger.isFinite());
        for (IntegerInterval i : drInteger.getIntegerIntervals()) {
            assertTrue(i.contains(new BigInteger("-2")));
            assertFalse(i.contains(new BigInteger("-1")));
        }
        drInteger.addFacet(Facet.MAX_INCLUSIVE, "2");
        assertTrue(drInteger.getIntegerIntervals().size() == 1);
        assertFalse(drInteger.isFinite());
        for (IntegerInterval i : drInteger.getIntegerIntervals()) {
            assertTrue(i.contains(new BigInteger("-2")));
            assertFalse(i.contains(new BigInteger("-1")));
        }
        drInteger.addFacet(Facet.MIN_EXCLUSIVE, "-14.7");
        assertTrue(drInteger.getIntegerIntervals().size() == 1);
        assertTrue(drInteger.isFinite());
        for (IntegerInterval i : drInteger.getIntegerIntervals()) {
            assertTrue(i.contains(new BigInteger("-14")));
            assertFalse(i.contains(new BigInteger("-15")));
            assertFalse(i.contains(new BigInteger("-1")));
            assertTrue(i.contains(new BigInteger("-2")));
        }
        drInteger.addFacet(Facet.MIN_EXCLUSIVE, "-10");
        assertTrue(drInteger.getIntegerIntervals().size() == 1);
        assertTrue(drInteger.isFinite());
        for (IntegerInterval i : drInteger.getIntegerIntervals()) {
            assertTrue(i.contains(new BigInteger("-9")));
            assertFalse(i.contains(new BigInteger("-10")));
            assertFalse(i.contains(new BigInteger("-1")));
            assertTrue(i.contains(new BigInteger("-2")));
        }
        drInteger.addFacet(Facet.MIN_INCLUSIVE, "-2");
        assertTrue(drInteger.getIntegerIntervals().size() == 1);
        assertTrue(drInteger.isFinite());
        for (IntegerInterval i : drInteger.getIntegerIntervals()) {
            assertTrue(i.contains(new BigInteger("-2")));
            assertFalse(i.contains(new BigInteger("-3")));
            assertFalse(i.contains(new BigInteger("-1")));
        }
    }

    public void testDatatypeRestrictionIntegerNegated() throws Exception {
        DatatypeRestrictionInteger drInteger = new DatatypeRestrictionInteger(DT.INTEGER);
        drInteger.negate();
        // not smaller 3 = not smaller or equal 2
        drInteger.addFacet(Facet.MAX_EXCLUSIVE, "3");
        assertTrue(drInteger.getIntegerIntervals().size() == 1);
        assertFalse(drInteger.isFinite());
        for (IntegerInterval i : drInteger.getIntegerIntervals()) {
            assertFalse(i.contains(new BigInteger("3")));
            assertTrue(i.contains(new BigInteger("2")));
        }
        // and not smaller 1 = and not smaller or equal 0
        drInteger.addFacet(Facet.MAX_EXCLUSIVE, "1");
        assertTrue(drInteger.getIntegerIntervals().size() == 1);
        assertFalse(drInteger.isFinite());
        for (IntegerInterval i : drInteger.getIntegerIntervals()) {
            assertFalse(i.contains(new BigInteger("1")));
            assertTrue(i.contains(new BigInteger("0")));
        }
        // and not smaller 5 = and not smaller or equal 4
        drInteger.addFacet(Facet.MAX_EXCLUSIVE, "5");
        assertTrue(drInteger.getIntegerIntervals().size() == 1);
        assertFalse(drInteger.isFinite());
        for (IntegerInterval i : drInteger.getIntegerIntervals()) {
            assertFalse(i.contains(new BigInteger("1")));
            assertTrue(i.contains(new BigInteger("0")));
        }
        // and not smaller or equal -2
        drInteger.addFacet(Facet.MAX_INCLUSIVE, "-2");
        assertTrue(drInteger.getIntegerIntervals().size() == 1);
        assertFalse(drInteger.isFinite());
        for (IntegerInterval i : drInteger.getIntegerIntervals()) {
            assertFalse(i.contains(new BigInteger("-1")));
            assertTrue(i.contains(new BigInteger("-2")));
        }
        // and not smaller or equal 2 = smaller or equal 1
        drInteger.addFacet(Facet.MAX_INCLUSIVE, "2");
        assertTrue(drInteger.getIntegerIntervals().size() == 1);
        assertFalse(drInteger.isFinite());
        for (IntegerInterval i : drInteger.getIntegerIntervals()) {
            assertFalse(i.contains(new BigInteger("-1")));
            assertTrue(i.contains(new BigInteger("-2")));
        }
        // and not greater -5 = and not greater or equal -4
        drInteger.addFacet(Facet.MIN_EXCLUSIVE, "-5");
        assertTrue(drInteger.getIntegerIntervals().size() == 1);
        assertFalse(drInteger.isFinite());
        for (IntegerInterval i : drInteger.getIntegerIntervals()) {
            assertFalse(i.contains(-5));
            assertTrue(i.contains(-4));
            assertTrue(i.contains(-2));
            assertFalse(i.contains(-1));
        }
        // and not greater -7 = and not greater or equal -6
        drInteger.addFacet(Facet.MIN_EXCLUSIVE, "-7");
        assertTrue(drInteger.getIntegerIntervals().size() == 1);
        assertFalse(drInteger.isFinite());
        for (IntegerInterval i : drInteger.getIntegerIntervals()) {
            assertFalse(i.contains(-5));
            assertTrue(i.contains(-4));
            assertTrue(i.contains(-2));
            assertFalse(i.contains(-1));
        }
        // and not greater or equal 5
        drInteger.addFacet(Facet.MIN_INCLUSIVE, "5");
        assertTrue(drInteger.getIntegerIntervals().size() == 1);
        assertTrue(drInteger.isFinite());
        assertTrue(drInteger.isBottom());
    }
    
    public void testConjoinRanges1() {
        // conjoin     |-----...
        // to             |------...
        // results in     |------...
        DataRange dr1 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr1.addFacet(Facet.MIN_INCLUSIVE, "10");
        DataRange dr2 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr2.addFacet(Facet.MIN_INCLUSIVE, "9");
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertFalse(canonical.isFinite());
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "9")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "10")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "10000")));
        assertTrue(((DatatypeRestrictionInteger)canonical).getIntegerIntervals().size() == 1);
        assertTrue(canonical.getEnumerationSize() == null);
    }
    
    public void testConjoinRanges2() {
        // conjoin           |-----...
        // to             |------...
        // results in        |-----...
        DataRange dr1 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr1.addFacet(Facet.MIN_INCLUSIVE, "10");
        DataRange dr2 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr2.addFacet(Facet.MIN_INCLUSIVE, "15");
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertFalse(canonical.isFinite());
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "14")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "15")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "10000")));
        assertTrue(((DatatypeRestrictionInteger)canonical).getIntegerIntervals().size() == 1);
        assertTrue(canonical.getEnumerationSize() == null);
    }
    
    public void testConjoinRanges3() {
        // conjoin     ...---|
        // to                   |---...
        // results in      empty
        DataRange dr1 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr1.addFacet(Facet.MIN_INCLUSIVE, "10");
        DataRange dr2 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr2.addFacet(Facet.MAX_INCLUSIVE, "5");
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertTrue(canonical.isFinite());
        assertTrue(canonical.isBottom());
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "10")));
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "5")));
        assertTrue(canonical.getEnumerationSize().equals(BigInteger.ZERO));
    }
    
    public void testConjoinRanges4() {
        // conjoin     ...----|
        // to              |----...
        // results in      |--|
        DataRange dr1 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr1.addFacet(Facet.MIN_INCLUSIVE, "10");
        DataRange dr2 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr2.addFacet(Facet.MAX_INCLUSIVE, "15");
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertTrue(canonical.isFinite());
        assertFalse(canonical.isBottom());
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "9")));
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "16")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "10")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "15")));
        assertTrue(((DatatypeRestrictionInteger)canonical).getIntegerIntervals().size() == 1);
        assertTrue(canonical.getEnumerationSize().equals(new BigInteger("6")));
    }
    
    public void testConjoinRanges5() {
        // conjoin     |---|
        // to                 |-------...
        // results in     empty
        DataRange dr1 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr1.addFacet(Facet.MIN_INCLUSIVE, "10");
        DataRange dr2 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr2.addFacet(Facet.MIN_INCLUSIVE, "4");
        dr2.addFacet(Facet.MAX_INCLUSIVE, "8");
        assertTrue(((CanonicalDataRange) dr2).isFinite());
        assertFalse(((CanonicalDataRange) dr1).isFinite());
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertTrue(canonical.isFinite());
        assertTrue(canonical.isBottom());
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "3")));
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "4")));
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "8")));
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "10")));
        assertTrue(((DatatypeRestrictionInteger)canonical).getIntegerIntervals().size() == 1);
        assertTrue(canonical.getEnumerationSize().equals(new BigInteger("0")));
    }
    
    public void testConjoinRanges6() {
        // conjoin     |-----|
        // to             |------...
        // results in     |--|
        DataRange dr1 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr1.addFacet(Facet.MIN_INCLUSIVE, "10");
        DataRange dr2 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr2.addFacet(Facet.MIN_INCLUSIVE, "5");
        dr2.addFacet(Facet.MAX_INCLUSIVE, "15");
        assertTrue(((CanonicalDataRange) dr2).isFinite());
        assertFalse(((CanonicalDataRange) dr1).isFinite());
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertTrue(canonical.isFinite());
        assertFalse(canonical.isBottom());
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "9")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "10")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "15")));
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "16")));
        assertTrue(((DatatypeRestrictionInteger)canonical).getIntegerIntervals().size() == 1);
        assertTrue(canonical.getEnumerationSize().equals(new BigInteger("6")));
    }
    
    public void testConjoinRanges7() {
        // conjoin          |---|
        // to             |------...
        // results in       |---|
        DataRange dr1 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr1.addFacet(Facet.MIN_INCLUSIVE, "10");
        DataRange dr2 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr2.addFacet(Facet.MIN_INCLUSIVE, "15");
        dr2.addFacet(Facet.MAX_INCLUSIVE, "20");
        assertTrue(((CanonicalDataRange) dr2).isFinite());
        assertFalse(((CanonicalDataRange) dr1).isFinite());
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertTrue(canonical.isFinite());
        assertFalse(canonical.isBottom());
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "14")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "15")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "20")));
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "21")));
        assertTrue(((DatatypeRestrictionInteger)canonical).getIntegerIntervals().size() == 1);
        assertTrue(canonical.getEnumerationSize().equals(new BigInteger("6")));
    }
    
    public void testConjoinRangesB1() {
        // conjoin      ...-----|
        // to          ...----|
        // results in  ...----|
        DataRange dr1 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr1.addFacet(Facet.MAX_INCLUSIVE, "10");
        DataRange dr2 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr2.addFacet(Facet.MAX_INCLUSIVE, "15");
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertFalse(canonical.isFinite());
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "11")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "10")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "-10000")));
        assertTrue(((DatatypeRestrictionInteger)canonical).getIntegerIntervals().size() == 1);
        assertTrue(canonical.getEnumerationSize() == null);
    }
    
    public void testConjoinRangesB2() {
        // conjoin      ...----|
        // to              ...----|
        // results in   ...----|
        DataRange dr1 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr1.addFacet(Facet.MAX_INCLUSIVE, "10");
        DataRange dr2 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr2.addFacet(Facet.MAX_INCLUSIVE, "15");
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertFalse(canonical.isFinite());
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "11")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "10")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "-10000")));
        assertTrue(((DatatypeRestrictionInteger)canonical).getIntegerIntervals().size() == 1);
        assertTrue(canonical.getEnumerationSize() == null);
    }
    
    public void testConjoinRangesB3() {
        // conjoin             |---...
        // to          ...---|
        // results in      empty
        DataRange dr1 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr1.addFacet(Facet.MAX_INCLUSIVE, "10");
        DataRange dr2 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr2.addFacet(Facet.MIN_INCLUSIVE, "15");
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertTrue(canonical.isFinite());
        assertTrue(canonical.isBottom());
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "10")));
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "5")));
        assertTrue(canonical.getEnumerationSize().equals(BigInteger.ZERO));
    }
    
    public void testConjoinRangesB4() {
        // conjoin         |---...
        // to          ...----|
        // results in      |--|
        DataRange dr1 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr1.addFacet(Facet.MAX_INCLUSIVE, "10");
        DataRange dr2 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr2.addFacet(Facet.MIN_INCLUSIVE, "5");
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertTrue(canonical.isFinite());
        assertFalse(canonical.isBottom());
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "4")));
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "11")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "5")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "10")));
        assertTrue(((DatatypeRestrictionInteger)canonical).getIntegerIntervals().size() == 1);
        assertTrue(canonical.getEnumerationSize().equals(new BigInteger("6")));
    }
    
    public void testConjoinRangesB5() {
        // conjoin               |---|
        // to          ...----|
        // results in     empty
        DataRange dr1 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr1.addFacet(Facet.MAX_INCLUSIVE, "10");
        DataRange dr2 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr2.addFacet(Facet.MIN_INCLUSIVE, "15");
        dr2.addFacet(Facet.MAX_INCLUSIVE, "20");
        assertTrue(((CanonicalDataRange) dr2).isFinite());
        assertFalse(((CanonicalDataRange) dr1).isFinite());
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertTrue(canonical.isFinite());
        assertTrue(canonical.isBottom());
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "10")));
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "15")));
        assertTrue(((DatatypeRestrictionInteger)canonical).getIntegerIntervals().size() == 1);
        assertTrue(canonical.getEnumerationSize().equals(new BigInteger("0")));
    }
    
    public void testConjoinRangesB6() {
        // conjoin         |-----|
        // to          ...----|
        // results in      |--|
        DataRange dr1 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr1.addFacet(Facet.MAX_INCLUSIVE, "10");
        DataRange dr2 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr2.addFacet(Facet.MIN_INCLUSIVE, "5");
        dr2.addFacet(Facet.MAX_INCLUSIVE, "15");
        assertTrue(((CanonicalDataRange) dr2).isFinite());
        assertFalse(((CanonicalDataRange) dr1).isFinite());
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertTrue(canonical.isFinite());
        assertFalse(canonical.isBottom());
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "4")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "5")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "10")));
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "11")));
        assertTrue(((DatatypeRestrictionInteger)canonical).getIntegerIntervals().size() == 1);
        assertTrue(canonical.getEnumerationSize().equals(new BigInteger("6")));
    }
    
    public void testConjoinRangesB7() {
        // conjoin         |---|
        // to           ...------|
        // results in      |---|
        DataRange dr1 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr1.addFacet(Facet.MAX_INCLUSIVE, "10");
        DataRange dr2 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr2.addFacet(Facet.MIN_INCLUSIVE, "0");
        dr2.addFacet(Facet.MAX_INCLUSIVE, "5");
        assertTrue(((CanonicalDataRange) dr2).isFinite());
        assertFalse(((CanonicalDataRange) dr1).isFinite());
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertTrue(canonical.isFinite());
        assertFalse(canonical.isBottom());
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "-1")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "0")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "5")));
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "6")));
        assertTrue(((DatatypeRestrictionInteger)canonical).getIntegerIntervals().size() == 1);
        assertTrue(canonical.getEnumerationSize().equals(new BigInteger("6")));
    }
    
    public void testConjoinRangesC1() {
        // conjoin     ...---|
        // to                   |---|
        // results in      empty
        DataRange dr1 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr1.addFacet(Facet.MIN_INCLUSIVE, "10");
        dr1.addFacet(Facet.MAX_INCLUSIVE, "15");
        DataRange dr2 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr2.addFacet(Facet.MAX_INCLUSIVE, "5");
        assertTrue(((CanonicalDataRange) dr1).isFinite());
        assertFalse(((CanonicalDataRange) dr2).isFinite());
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertTrue(canonical.isFinite());
        assertTrue(canonical.isBottom());
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "10")));
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "5")));
        assertTrue(canonical.getEnumerationSize().equals(BigInteger.ZERO));
    }
    
    public void testConjoinRangesC2() {
        // conjoin     ...----|
        // to              |----|
        // results in      |--|
        DataRange dr1 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr1.addFacet(Facet.MIN_INCLUSIVE, "10");
        dr1.addFacet(Facet.MAX_INCLUSIVE, "15");
        DataRange dr2 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr2.addFacet(Facet.MAX_INCLUSIVE, "12");
        assertTrue(((CanonicalDataRange) dr1).isFinite());
        assertFalse(((CanonicalDataRange) dr2).isFinite());
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertTrue(canonical.isFinite());
        assertFalse(canonical.isBottom());
        assertTrue(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "10")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "12")));
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "9")));
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "13")));
        assertTrue(canonical.getEnumerationSize().equals(new BigInteger("3")));
    }
    
    public void testConjoinRangesC3() {
        // conjoin     ...---------|
        // to              |----|
        // results in      |----|
        DataRange dr1 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr1.addFacet(Facet.MIN_INCLUSIVE, "10");
        dr1.addFacet(Facet.MAX_INCLUSIVE, "15");
        DataRange dr2 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr2.addFacet(Facet.MAX_INCLUSIVE, "20");
        assertTrue(((CanonicalDataRange) dr1).isFinite());
        assertFalse(((CanonicalDataRange) dr2).isFinite());
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertTrue(canonical.isFinite());
        assertFalse(canonical.isBottom());
        assertTrue(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "10")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "15")));
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "9")));
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "16")));
        assertTrue(canonical.getEnumerationSize().equals(new BigInteger("6")));
    }
    
    public void testConjoinRangesC4() {
        // conjoin             |---...
        // to          |---|
        // results in      empty
        DataRange dr1 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr1.addFacet(Facet.MIN_INCLUSIVE, "10");
        dr1.addFacet(Facet.MAX_INCLUSIVE, "15");
        DataRange dr2 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr2.addFacet(Facet.MIN_INCLUSIVE, "20");
        assertTrue(((CanonicalDataRange) dr1).isFinite());
        assertFalse(((CanonicalDataRange) dr2).isFinite());
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertTrue(canonical.isFinite());
        assertTrue(canonical.isBottom());
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "10")));
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "20")));
        assertTrue(canonical.getEnumerationSize().equals(BigInteger.ZERO));
    }
    
    public void testConjoinRangesC5() {
        // conjoin         |----...
        // to          |------|
        // results in      |--|
        DataRange dr1 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr1.addFacet(Facet.MIN_INCLUSIVE, "10");
        dr1.addFacet(Facet.MAX_INCLUSIVE, "15");
        DataRange dr2 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr2.addFacet(Facet.MIN_INCLUSIVE, "12");
        assertTrue(((CanonicalDataRange) dr1).isFinite());
        assertFalse(((CanonicalDataRange) dr2).isFinite());
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertTrue(canonical.isFinite());
        assertFalse(canonical.isBottom());
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "11")));
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "16")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "12")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "15")));
        assertTrue(((DatatypeRestrictionInteger)canonical).getIntegerIntervals().size() == 1);
        assertTrue(canonical.getEnumerationSize().equals(new BigInteger("4")));
    }
    
    public void testConjoinRangesC6() {
        // conjoin         |----...
        // to          |------|
        // results in      |--|
        DataRange dr1 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr1.addFacet(Facet.MIN_INCLUSIVE, "10");
        dr1.addFacet(Facet.MAX_INCLUSIVE, "15");
        DataRange dr2 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr2.addFacet(Facet.MIN_INCLUSIVE, "5");
        assertTrue(((CanonicalDataRange) dr1).isFinite());
        assertFalse(((CanonicalDataRange) dr2).isFinite());
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertTrue(canonical.isFinite());
        assertFalse(canonical.isBottom());
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "9")));
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "16")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "10")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "15")));
        assertTrue(((DatatypeRestrictionInteger)canonical).getIntegerIntervals().size() == 1);
        assertTrue(canonical.getEnumerationSize().equals(new BigInteger("6")));
    }
    
    public void testConjoinRangesC7() {
        // conjoin            |---|
        // to          |---| 
        // results in      empty
        DataRange dr1 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr1.addFacet(Facet.MIN_INCLUSIVE, "5");
        dr1.addFacet(Facet.MAX_INCLUSIVE, "9");
        DataRange dr2 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr2.addFacet(Facet.MIN_INCLUSIVE, "10");
        dr2.addFacet(Facet.MAX_INCLUSIVE, "15");
        assertTrue(((CanonicalDataRange) dr1).isFinite());
        assertTrue(((CanonicalDataRange) dr2).isFinite());
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertTrue(canonical.isFinite());
        assertTrue(canonical.isBottom());
        assertTrue(((DatatypeRestrictionInteger)canonical).getIntegerIntervals().size() == 1);
        assertTrue(canonical.getEnumerationSize().equals(new BigInteger("0")));
    }
    
    public void testConjoinRangesC8() {
        // conjoin        |-----|
        // to          |-----| 
        // results in     |--|
        DataRange dr1 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr1.addFacet(Facet.MIN_INCLUSIVE, "5");
        dr1.addFacet(Facet.MAX_INCLUSIVE, "10");
        DataRange dr2 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr2.addFacet(Facet.MIN_INCLUSIVE, "8");
        dr2.addFacet(Facet.MAX_INCLUSIVE, "15");
        assertTrue(((CanonicalDataRange) dr1).isFinite());
        assertTrue(((CanonicalDataRange) dr2).isFinite());
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertTrue(canonical.isFinite());
        assertFalse(canonical.isBottom());
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "7")));
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "11")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "8")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "10")));
        assertTrue(((DatatypeRestrictionInteger)canonical).getIntegerIntervals().size() == 1);
        assertTrue(canonical.getEnumerationSize().equals(new BigInteger("3")));
    }
    
    public void testConjoinRangesC9() {
        // conjoin     |-------|
        // to            |---| 
        // results in    |---|
        DataRange dr1 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr1.addFacet(Facet.MIN_INCLUSIVE, "5");
        dr1.addFacet(Facet.MAX_INCLUSIVE, "20");
        DataRange dr2 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr2.addFacet(Facet.MIN_INCLUSIVE, "10");
        dr2.addFacet(Facet.MAX_INCLUSIVE, "15");
        assertTrue(((CanonicalDataRange) dr1).isFinite());
        assertTrue(((CanonicalDataRange) dr2).isFinite());
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertTrue(canonical.isFinite());
        assertFalse(canonical.isBottom());
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "9")));
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "16")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "10")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "15")));
        assertTrue(((DatatypeRestrictionInteger)canonical).getIntegerIntervals().size() == 1);
        assertTrue(canonical.getEnumerationSize().equals(new BigInteger("6")));
    }
    
    public void testConjoinRangesC10() {
        // conjoin        |---|
        // to          |--------| 
        // results in     |---|
        DataRange dr1 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr1.addFacet(Facet.MIN_INCLUSIVE, "12");
        dr1.addFacet(Facet.MAX_INCLUSIVE, "13");
        DataRange dr2 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr2.addFacet(Facet.MIN_INCLUSIVE, "10");
        dr2.addFacet(Facet.MAX_INCLUSIVE, "15");
        assertTrue(((CanonicalDataRange) dr1).isFinite());
        assertTrue(((CanonicalDataRange) dr2).isFinite());
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertTrue(canonical.isFinite());
        assertFalse(canonical.isBottom());
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "11")));
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "14")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "12")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "13")));
        assertTrue(((DatatypeRestrictionInteger)canonical).getIntegerIntervals().size() == 1);
        assertTrue(canonical.getEnumerationSize().equals(new BigInteger("2")));
    }
    
    public void testConjoinRangesC11() {
        // conjoin        |-----|
        // to          |-----| 
        // results in     |--|
        DataRange dr1 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr1.addFacet(Facet.MIN_INCLUSIVE, "5");
        dr1.addFacet(Facet.MAX_INCLUSIVE, "15");
        DataRange dr2 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr2.addFacet(Facet.MIN_INCLUSIVE, "10");
        dr2.addFacet(Facet.MAX_INCLUSIVE, "20");
        assertTrue(((CanonicalDataRange) dr1).isFinite());
        assertTrue(((CanonicalDataRange) dr2).isFinite());
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertTrue(canonical.isFinite());
        assertFalse(canonical.isBottom());
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "9")));
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "16")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "10")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "15")));
        assertTrue(((DatatypeRestrictionInteger)canonical).getIntegerIntervals().size() == 1);
        assertTrue(canonical.getEnumerationSize().equals(new BigInteger("6")));
    }
    
    public void testConjoinRangesC12() {
        // conjoin           |---|
        // to          |---| 
        // results in     empty
        DataRange dr1 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr1.addFacet(Facet.MIN_INCLUSIVE, "5");
        dr1.addFacet(Facet.MAX_INCLUSIVE, "15");
        DataRange dr2 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr2.addFacet(Facet.MIN_INCLUSIVE, "20");
        dr2.addFacet(Facet.MAX_INCLUSIVE, "30");
        assertTrue(((CanonicalDataRange) dr1).isFinite());
        assertTrue(((CanonicalDataRange) dr2).isFinite());
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertTrue(canonical.isFinite());
        assertTrue(canonical.isBottom());
        assertTrue(((DatatypeRestrictionInteger)canonical).getIntegerIntervals().size() == 1);
        assertTrue(canonical.getEnumerationSize().equals(new BigInteger("0")));
    }
    
    public void testConjoinNegRangesA1() {
        // conjoin      ...----|
        // to            |---| 
        // results in    |---|
        DataRange dr1 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr1.addFacet(Facet.MIN_INCLUSIVE, "5");
        dr1.addFacet(Facet.MAX_INCLUSIVE, "15");
        DataRange dr2 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr2.negate();
        dr2.addFacet(Facet.MIN_INCLUSIVE, "20");
        assertTrue(((CanonicalDataRange) dr1).isFinite());
        assertFalse(((CanonicalDataRange) dr2).isFinite());
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertTrue(canonical.isFinite());
        assertFalse(canonical.isBottom());
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "4")));
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "16")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "10")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "15")));
        assertTrue(((DatatypeRestrictionInteger)canonical).getIntegerIntervals().size() == 1);
        assertTrue(canonical.getEnumerationSize().equals(new BigInteger("11")));
    }
    
    public void testConjoinNegRangesA2() {
        // conjoin           |-----...
        // to            |-------| 
        // results in        |---|
        DataRange dr1 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr1.addFacet(Facet.MIN_INCLUSIVE, "5");
        dr1.addFacet(Facet.MAX_INCLUSIVE, "15");
        DataRange dr2 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr2.negate();
        dr2.addFacet(Facet.MAX_INCLUSIVE, "10");
        assertTrue(((CanonicalDataRange) dr1).isFinite());
        assertFalse(((CanonicalDataRange) dr2).isFinite());
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertTrue(canonical.isFinite());
        assertFalse(canonical.isBottom());
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "10")));
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "16")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "11")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "15")));
        assertTrue(((DatatypeRestrictionInteger)canonical).getIntegerIntervals().size() == 1);
        assertTrue(canonical.getEnumerationSize().equals(new BigInteger("5")));
    }
    
    public void testConjoinNegRangesA3() {
        // conjoin     ...---|
        // to            |-------| 
        // results in    |---|
        DataRange dr1 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr1.addFacet(Facet.MIN_INCLUSIVE, "5");
        dr1.addFacet(Facet.MAX_INCLUSIVE, "15");
        DataRange dr2 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr2.negate();
        dr2.addFacet(Facet.MIN_INCLUSIVE, "10");
        assertTrue(((CanonicalDataRange) dr1).isFinite());
        assertFalse(((CanonicalDataRange) dr2).isFinite());
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertTrue(canonical.isFinite());
        assertFalse(canonical.isBottom());
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "4")));
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "10")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "5")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "9")));
        assertTrue(((DatatypeRestrictionInteger)canonical).getIntegerIntervals().size() == 1);
        assertTrue(canonical.getEnumerationSize().equals(new BigInteger("5")));
    }
    
    public void testConjoinNegRangesA4() {
        // conjoin     ...-----|
        // or               |-------...
        // to           |----------| 
        // results in   |----------| since the conjoined is top
        DataRange dr1 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr1.addFacet(Facet.MIN_INCLUSIVE, "0");
        dr1.addFacet(Facet.MAX_INCLUSIVE, "15");
        DataRange dr2 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr2.negate();
        dr2.addFacet(Facet.MIN_INCLUSIVE, "10"); // <= 9
        dr2.addFacet(Facet.MAX_INCLUSIVE, "5"); // or >= 6 (trivially true)
        assertTrue(((CanonicalDataRange) dr1).isFinite());
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertTrue(canonical.isFinite());
        assertFalse(canonical.isBottom());
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "-1")));
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "16")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "0")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "15")));
        assertTrue(((DatatypeRestrictionInteger)canonical).getIntegerIntervals().size() == 1);
        assertTrue(canonical.getEnumerationSize().equals(new BigInteger("16")));
    }
    
    public void testConjoinNegRangesA5() {
        // conjoin    ...---|
        // or                   |---...
        // to           |-----------| 
        // results in   |---|  |---| 
        DataRange dr1 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr1.addFacet(Facet.MIN_INCLUSIVE, "0");
        dr1.addFacet(Facet.MAX_INCLUSIVE, "15");
        DataRange dr2 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr2.negate();
        dr2.addFacet(Facet.MIN_INCLUSIVE, "5"); // <= 4
        dr2.addFacet(Facet.MAX_INCLUSIVE, "10"); // or >= 11
        assertTrue(((CanonicalDataRange) dr1).isFinite());
        assertFalse(((CanonicalDataRange) dr2).isFinite());
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertTrue(canonical.isFinite());
        assertFalse(canonical.isBottom());
        // ranges 0-4 and 11-15
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "-1")));
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "5")));
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "10")));
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "16")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "0")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "4")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "11")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "15")));
        assertTrue(((DatatypeRestrictionInteger)canonical).getIntegerIntervals().size() == 2);
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
        DataRange dr1 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr1.addFacet(Facet.MIN_INCLUSIVE, "0");
        dr1.addFacet(Facet.MAX_INCLUSIVE, "100");
        DataRange dr2 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr2.negate();
        dr2.addFacet(Facet.MIN_INCLUSIVE, "10"); // <= 9
        dr2.addFacet(Facet.MAX_INCLUSIVE, "20"); // or >= 21
        DataRange dr3 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr3.negate();
        dr3.addFacet(Facet.MIN_INCLUSIVE, "30"); // <= 29
        dr3.addFacet(Facet.MAX_INCLUSIVE, "40"); // or >= 41
        assertTrue(((CanonicalDataRange) dr1).isFinite());
        assertFalse(((CanonicalDataRange) dr2).isFinite());
        assertFalse(((CanonicalDataRange) dr3).isFinite());
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertTrue(canonical.isFinite());
        assertFalse(canonical.isBottom());
        // ranges 0-9 and 21-100
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "-1")));
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "10")));
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "20")));
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "101")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "0")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "9")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "21")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "100")));
        assertTrue(((DatatypeRestrictionInteger)canonical).getIntegerIntervals().size() == 2);
        assertTrue(canonical.getEnumerationSize().equals(new BigInteger("90")));
        canonical.conjoinFacetsFrom(dr3);
        assertTrue(canonical.isFinite());
        assertFalse(canonical.isBottom());
        // ranges 0-9 and 21-29 and 41-100
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "-1")));
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "10")));
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "20")));
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "30")));
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "40")));
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "101")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "0")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "9")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "21")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "29")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "41")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "100")));
        assertTrue(((DatatypeRestrictionInteger)canonical).getIntegerIntervals().size() == 3);
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
        DataRange dr1 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr1.addFacet(Facet.MIN_INCLUSIVE, "0");
        dr1.addFacet(Facet.MAX_INCLUSIVE, "100");
        DataRange dr2 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr2.negate();
        dr2.addFacet(Facet.MIN_INCLUSIVE, "10"); // <= 9
        dr2.addFacet(Facet.MAX_INCLUSIVE, "20"); // or >= 21
        DataRange dr3 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr3.negate();
        dr3.addFacet(Facet.MIN_INCLUSIVE, "30"); // <= 29
        dr3.addFacet(Facet.MAX_INCLUSIVE, "40"); // or >= 41
        DataRange dr4 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr4.addFacet(Facet.MIN_INCLUSIVE, "25"); // >= 25
        dr4.addFacet(Facet.MAX_INCLUSIVE, "110"); // and >= 110
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        canonical.conjoinFacetsFrom(dr3);
        canonical.conjoinFacetsFrom(dr4);
        assertTrue(((DatatypeRestrictionInteger)canonical).getIntegerIntervals().size() == 2);
        assertTrue(canonical.isFinite());
        assertFalse(canonical.isBottom());
        // ranges 25-29 and 41-100
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "24")));
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "30")));
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "40")));
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "101")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "25")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "29")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "41")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "100")));
        assertTrue(canonical.getEnumerationSize().equals(new BigInteger("65")));
    }
    
    public void testFinToBig() {
        DatatypeRestrictionInteger dr1 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr1.addFacet(Facet.MIN_INCLUSIVE, "" + Long.MAX_VALUE);
        assertFalse(dr1.accepts(new DataConstant(Impl.IInteger, DT.INTEGER, "" + (Long.MAX_VALUE - 1))));
        assertTrue(dr1.accepts(new DataConstant(Impl.IInteger, DT.INTEGER, "" + Long.MAX_VALUE)));
        for (IntegerInterval i : dr1.getIntegerIntervals()) {
            assertTrue(i instanceof IntegerIntervalFin);
        }
        dr1.addFacet(Facet.MIN_EXCLUSIVE, "" + Long.MAX_VALUE);
        assertFalse(dr1.accepts(new DataConstant(Impl.IInteger, DT.INTEGER, "" + Long.MAX_VALUE)));
        assertTrue(dr1.accepts(new DataConstant(Impl.IInteger, DT.INTEGER, "" + new BigInteger("" + Long.MAX_VALUE).add(BigInteger.ONE))));
        for (IntegerInterval i : dr1.getIntegerIntervals()) {
            assertTrue(i instanceof IntegerIntervalBig);
        }
    }
    
    public void testFinToBig2() {
        DatatypeRestrictionInteger dr1 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr1.addFacet(Facet.MAX_INCLUSIVE, "" + Long.MIN_VALUE);
        assertFalse(dr1.accepts(new DataConstant(Impl.IInteger, DT.INTEGER, "" + (Long.MIN_VALUE + 1))));
        assertTrue(dr1.accepts(new DataConstant(Impl.IInteger, DT.INTEGER, "" + Long.MIN_VALUE)));
        for (IntegerInterval i : dr1.getIntegerIntervals()) {
            assertTrue(i instanceof IntegerIntervalFin);
        }
        dr1.addFacet(Facet.MAX_EXCLUSIVE, "" + Long.MIN_VALUE);
        assertFalse(dr1.accepts(new DataConstant(Impl.IInteger, DT.INTEGER, "" + Long.MIN_VALUE)));
        assertTrue(dr1.accepts(new DataConstant(Impl.IInteger, DT.INTEGER, "" + new BigInteger("" + Long.MIN_VALUE).subtract(BigInteger.ONE))));
        for (IntegerInterval i : dr1.getIntegerIntervals()) {
            assertTrue(i instanceof IntegerIntervalBig);
        }
    }
    
    public void testConjoinDouble() {
        // conjoin     |-----...
        // to             |------...
        // results in     |------...
        DataRange dr1 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr1.addFacet(Facet.MIN_INCLUSIVE, "10.0");
        DataRange dr2 = new DatatypeRestrictionDouble(DT.DOUBLE);
        dr2.addFacet(Facet.MIN_INCLUSIVE, "11.11");
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2);
        assertTrue(canonical.isFinite());
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "11")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "12")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "10000")));
        assertTrue(((DatatypeRestrictionInteger)canonical).getIntegerIntervals().size() == 1);
        assertTrue(canonical.getEnumerationSize() != null);
    }
    
    public void testConjoinDouble2() {
        // conjoin     |-----|
        // to         |---------...
        // results in  |-----|
        DataRange dr1 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr1.addFacet(Facet.MIN_INCLUSIVE, "10.0");
        DataRange dr2 = new DatatypeRestrictionDouble(DT.DOUBLE);
        dr2.addFacet(Facet.MIN_INCLUSIVE, "11.11"); // >= 12
        dr2.addFacet(Facet.MAX_EXCLUSIVE, "15.1");  // <= 15
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2); // >= 12 and <= 15
        assertTrue(canonical.isFinite());
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "11")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "12")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "15")));
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "16")));
        assertTrue(((DatatypeRestrictionInteger)canonical).getIntegerIntervals().size() == 1);
        assertTrue(canonical.getEnumerationSize().equals(new BigInteger("4")));
    }
    
    public void testConjoinDecimal2() {
        // conjoin     |-----|
        // to         |---------...
        // results in  |-----|
        DataRange dr1 = new DatatypeRestrictionInteger(DT.INTEGER);
        dr1.addFacet(Facet.MIN_INCLUSIVE, "0");
        dr1.addFacet(Facet.MAX_INCLUSIVE, "5");
        DataRange dr2 = new DatatypeRestrictionOWLRealPlus(DT.DECIMAL);
        dr2.addFacet(Facet.MIN_INCLUSIVE, "0.1"); // >= 1
        dr2.addFacet(Facet.MAX_INCLUSIVE, "2.0");  // <= 2
        CanonicalDataRange canonical = (CanonicalDataRange) dr1;
        canonical.conjoinFacetsFrom(dr2); // >= 1 and <= 2
        assertTrue(canonical.isFinite());
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "0")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "1")));
        assertTrue(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "2")));
        assertFalse(canonical.accepts(new DataConstant(Impl.IInteger, dr1.getDatatype(), "3")));
        assertTrue(((DatatypeRestrictionInteger)canonical).getIntegerIntervals().size() == 1);
        assertTrue(canonical.getEnumerationSize().equals(new BigInteger("2")));
    }
    
    public void testInvalidInts() throws Exception {
        CanonicalDataRange cdr = new DatatypeRestrictionInteger(DT.INTEGER);
        assertFalse(cdr.accepts(new DataConstant(Impl.IDouble, DT.DECIMAL, "1.2")));
        assertFalse(cdr.accepts(new DataConstant(Impl.IBase64Binary, DT.BASE64BINARY, "00==")));
        assertTrue(cdr.accepts(new DataConstant(Impl.IInteger, DT.DOUBLE, "4.0")));
        assertTrue(cdr.accepts(new DataConstant(Impl.IInteger, DT.DECIMAL, "4544556468748765468484654549876546548784654648784546547445545555545454542175418454")));
    }
}
