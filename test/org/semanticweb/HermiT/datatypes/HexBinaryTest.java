package org.semanticweb.HermiT.datatypes;

import java.math.BigInteger;

import org.semanticweb.HermiT.datatypes.old.CanonicalDataRange;
import org.semanticweb.HermiT.datatypes.old.DataConstant;
import org.semanticweb.HermiT.datatypes.old.DatatypeRestrictionHexBinary;
import org.semanticweb.HermiT.datatypes.old.InternalDataRange;
import org.semanticweb.HermiT.datatypes.old.DataConstant.Impl;
import org.semanticweb.HermiT.datatypes.old.InternalDatatypeRestriction.DT;
import org.semanticweb.HermiT.datatypes.old.InternalDatatypeRestriction.Facet;
import org.semanticweb.HermiT.reasoner.AbstractReasonerTest;

public class HexBinaryTest extends AbstractReasonerTest {
    
    public HexBinaryTest(String name) {
        super(name);
    }
    
    public void testSize1() throws Exception {
        InternalDataRange dr = new DatatypeRestrictionHexBinary(DT.HEXBINARY);
        dr.addFacet(Facet.LENGTH, "1");
        CanonicalDataRange cdr = (CanonicalDataRange) dr;
        BigInteger num = cdr.getEnumerationSize();
        assertTrue(num.compareTo(new BigInteger("256")) == 0);
    }

    public void testSize2() throws Exception {
        InternalDataRange dr = new DatatypeRestrictionHexBinary(DT.HEXBINARY);
        dr.addFacet(Facet.MIN_LENGTH, "2");
        dr.addFacet(Facet.MAX_LENGTH, "2");
        CanonicalDataRange cdr = (CanonicalDataRange) dr;
        BigInteger num = cdr.getEnumerationSize();
        assertTrue(num.compareTo(new BigInteger("65536")) == 0);
    }
    
    public void testSize3() throws Exception {
        InternalDataRange dr = new DatatypeRestrictionHexBinary(DT.HEXBINARY);
        dr.addFacet(Facet.MIN_LENGTH, "1");
        dr.addFacet(Facet.MAX_LENGTH, "2");
        CanonicalDataRange cdr = (CanonicalDataRange) dr;
        BigInteger num = cdr.getEnumerationSize();
        assertTrue(num.compareTo(new BigInteger("" + (65536 + 256))) == 0);
    }
    
    public void testSize4() throws Exception {
        InternalDataRange dr = new DatatypeRestrictionHexBinary(DT.HEXBINARY);
        dr.addFacet(Facet.MAX_LENGTH, "2");
        CanonicalDataRange cdr = (CanonicalDataRange) dr;
        BigInteger num = cdr.getEnumerationSize();
        assertTrue(num.compareTo(new BigInteger("" + (65536 + 256 + 1))) == 0);
    }
    
    public void testSize5() throws Exception {
        InternalDataRange dr = new DatatypeRestrictionHexBinary(DT.HEXBINARY);
        dr.addFacet(Facet.MAX_LENGTH, "2");
        CanonicalDataRange cdr = (CanonicalDataRange) dr;
        BigInteger num = cdr.getEnumerationSize();
        assertTrue(num.compareTo(new BigInteger("" + (65536 + 256 + 1))) == 0);
    }
    
    public void testSize6() throws Exception {
        InternalDataRange dr = new DatatypeRestrictionHexBinary(DT.HEXBINARY);
        dr.addFacet(Facet.MIN_LENGTH, "2");
        CanonicalDataRange cdr = (CanonicalDataRange) dr;
        BigInteger num = cdr.getEnumerationSize();
        assertTrue(num == null);
    }
    
    public void testFacets() throws Exception {
        InternalDataRange dr = new DatatypeRestrictionHexBinary(DT.HEXBINARY);
        dr.addFacet(Facet.MIN_LENGTH, "1");
        dr.addFacet(Facet.MIN_LENGTH, "2");
        dr.addFacet(Facet.MIN_LENGTH, "0");
        dr.addFacet(Facet.MAX_LENGTH, "10");
        dr.addFacet(Facet.MAX_LENGTH, "2");
        dr.addFacet(Facet.MAX_LENGTH, "5");
        CanonicalDataRange cdr = (CanonicalDataRange) dr;
        BigInteger num = cdr.getEnumerationSize();
        assertTrue(num.compareTo(new BigInteger("65536")) == 0);
    }
    
    public void testAssignments() throws Exception {
        InternalDataRange dr = new DatatypeRestrictionHexBinary(DT.HEXBINARY);
        dr.addFacet(Facet.LENGTH, "1");
        CanonicalDataRange cdr = (CanonicalDataRange) dr;
        DataConstant c = cdr.getSmallestAssignment();
        assertTrue(c.getValue().equals("00"));
        cdr.notOneOf(c);
        c = cdr.getSmallestAssignment();
        assertTrue(c.getValue().equals("01"));
    }
    
    public void testAssignments2() throws Exception {
        InternalDataRange dr = new DatatypeRestrictionHexBinary(DT.HEXBINARY);
        dr.addFacet(Facet.MIN_LENGTH, "1");
        dr.addFacet(Facet.MAX_LENGTH, "1");
        CanonicalDataRange cdr = (CanonicalDataRange) dr;
        int i = 0;
        DataConstant c = cdr.getSmallestAssignment();
        while (c != null) {
            cdr.notOneOf(c);
            c = cdr.getSmallestAssignment();
            i++;
        }
        assertTrue(i == 256);
    }
    
    public void testAssignments3() throws Exception {
        InternalDataRange dr = new DatatypeRestrictionHexBinary(DT.HEXBINARY);
        dr.addFacet(Facet.MIN_LENGTH, "1");
        dr.addFacet(Facet.MAX_LENGTH, "5");
        CanonicalDataRange cdr = (CanonicalDataRange) dr;
        DataConstant c = cdr.getSmallestAssignment();
        assertTrue(c.getValue().equals("00"));
        cdr.notOneOf(c);
        DataConstant not = new DataConstant(Impl.IHexBinary, DT.HEXBINARY, "01");
        cdr.notOneOf(not);
        c = cdr.getSmallestAssignment();
        assertTrue(c.getValue().equals("02"));
    }
    
    public void testInvalidHex() throws Exception {
        CanonicalDataRange cdr = new DatatypeRestrictionHexBinary(DT.HEXBINARY);
        ((InternalDataRange) cdr).addFacet(Facet.MAX_LENGTH, "5");
        assertFalse(cdr.accepts(new DataConstant(Impl.IHexBinary, DT.HEXBINARY, "01a")));
        assertFalse(cdr.accepts(new DataConstant(Impl.IBase64Binary, DT.HEXBINARY, "00")));
    }
}
