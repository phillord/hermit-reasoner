package org.semanticweb.HermiT.datatypes;

import java.math.BigInteger;

import org.semanticweb.HermiT.model.dataranges.CanonicalDataRange;
import org.semanticweb.HermiT.model.dataranges.DataConstant;
import org.semanticweb.HermiT.model.dataranges.DataRange;
import org.semanticweb.HermiT.model.dataranges.DatatypeRestrictionBase64Binary;
import org.semanticweb.HermiT.model.dataranges.DataConstant.Impl;
import org.semanticweb.HermiT.model.dataranges.DatatypeRestriction.DT;
import org.semanticweb.HermiT.model.dataranges.DatatypeRestriction.Facets;
import org.semanticweb.HermiT.reasoner.AbstractReasonerTest;

public class Base64BinaryTest extends AbstractReasonerTest {
    
    public Base64BinaryTest(String name) {
        super(name);
    }
    
    public void testSize1() throws Exception {
        DataRange dr = new DatatypeRestrictionBase64Binary(DT.BASE64BINARY);
        dr.addFacet(Facets.LENGTH, "1");
        //dr.addFacet(Facets.MAX_LENGTH, "3");
        CanonicalDataRange cdr = (CanonicalDataRange) dr;
        BigInteger num = cdr.getEnumerationSize();
        assertTrue(num.compareTo(new BigInteger("256")) == 0);
    }

    public void testSize2() throws Exception {
        DataRange dr = new DatatypeRestrictionBase64Binary(DT.BASE64BINARY);
        dr.addFacet(Facets.MIN_LENGTH, "2");
        dr.addFacet(Facets.MAX_LENGTH, "2");
        CanonicalDataRange cdr = (CanonicalDataRange) dr;
        BigInteger num = cdr.getEnumerationSize();
        assertTrue(num.compareTo(new BigInteger("65536")) == 0);
    }
    
    public void testSize3() throws Exception {
        DataRange dr = new DatatypeRestrictionBase64Binary(DT.BASE64BINARY);
        dr.addFacet(Facets.MIN_LENGTH, "1");
        dr.addFacet(Facets.MAX_LENGTH, "2");
        CanonicalDataRange cdr = (CanonicalDataRange) dr;
        BigInteger num = cdr.getEnumerationSize();
        assertTrue(num.compareTo(new BigInteger("" + (65536 + 256))) == 0);
    }
    
    public void testSize4() throws Exception {
        DataRange dr = new DatatypeRestrictionBase64Binary(DT.BASE64BINARY);
        dr.addFacet(Facets.MAX_LENGTH, "2");
        CanonicalDataRange cdr = (CanonicalDataRange) dr;
        BigInteger num = cdr.getEnumerationSize();
        assertTrue(num.compareTo(new BigInteger("" + (65536 + 256 + 1))) == 0);
    }
    
    public void testSize5() throws Exception {
        DataRange dr = new DatatypeRestrictionBase64Binary(DT.BASE64BINARY);
        dr.addFacet(Facets.MAX_LENGTH, "2");
        CanonicalDataRange cdr = (CanonicalDataRange) dr;
        BigInteger num = cdr.getEnumerationSize();
        assertTrue(num.compareTo(new BigInteger("" + (65536 + 256 + 1))) == 0);
    }
    
    public void testSize6() throws Exception {
        DataRange dr = new DatatypeRestrictionBase64Binary(DT.BASE64BINARY);
        dr.addFacet(Facets.MIN_LENGTH, "2");
        CanonicalDataRange cdr = (CanonicalDataRange) dr;
        BigInteger num = cdr.getEnumerationSize();
        assertTrue(num == null);
    }
    
    public void testFacets() throws Exception {
        DataRange dr = new DatatypeRestrictionBase64Binary(DT.BASE64BINARY);
        dr.addFacet(Facets.MIN_LENGTH, "1");
        dr.addFacet(Facets.MIN_LENGTH, "2");
        dr.addFacet(Facets.MIN_LENGTH, "0");
        dr.addFacet(Facets.MAX_LENGTH, "10");
        dr.addFacet(Facets.MAX_LENGTH, "2");
        dr.addFacet(Facets.MAX_LENGTH, "5");
        CanonicalDataRange cdr = (CanonicalDataRange) dr;
        BigInteger num = cdr.getEnumerationSize();
        assertTrue(num.compareTo(new BigInteger("65536")) == 0);
    }
    
    public void testAssignments() throws Exception {
        DataRange dr = new DatatypeRestrictionBase64Binary(DT.BASE64BINARY);
        dr.addFacet(Facets.LENGTH, "1");
        CanonicalDataRange cdr = (CanonicalDataRange) dr;
        DataConstant c = cdr.getSmallestAssignment();
        assertTrue(c.getValue().equals("+A=="));
        cdr.notOneOf(c);
        c = cdr.getSmallestAssignment();
        assertTrue(c.getValue().equals("+Q=="));
    }
    
    public void testAssignments2() throws Exception {
        DataRange dr = new DatatypeRestrictionBase64Binary(DT.BASE64BINARY);
        dr.addFacet(Facets.MIN_LENGTH, "1");
        dr.addFacet(Facets.MAX_LENGTH, "1");
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
        DataRange dr = new DatatypeRestrictionBase64Binary(DT.BASE64BINARY);
        dr.addFacet(Facets.MIN_LENGTH, "1");
        dr.addFacet(Facets.MAX_LENGTH, "5");
        CanonicalDataRange cdr = (CanonicalDataRange) dr;
        DataConstant c = cdr.getSmallestAssignment();
        assertTrue(c.getValue().equals("++0="));
        cdr.notOneOf(c);
        DataConstant not = new DataConstant(Impl.IBase64Binary, DT.BASE64BINARY, "++4=");
        cdr.notOneOf(not);
        c = cdr.getSmallestAssignment();
        assertTrue(c.getValue().equals("++8="));
    }
    
    public void testAssignments4() throws Exception {
        DataRange dr = new DatatypeRestrictionBase64Binary(DT.BASE64BINARY);
        dr.addFacet(Facets.MAX_LENGTH, "2");
        CanonicalDataRange cdr = (CanonicalDataRange) dr;
        BigInteger size = cdr.getEnumerationSize();
        assertTrue(size.compareTo(new BigInteger("65793")) == 0);
    }
}
