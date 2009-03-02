package org.semanticweb.HermiT.datatypes;

import java.math.BigInteger;

import org.semanticweb.HermiT.model.dataranges.CanonicalDataRange;
import org.semanticweb.HermiT.model.dataranges.DataConstant;
import org.semanticweb.HermiT.model.dataranges.DataRange;
import org.semanticweb.HermiT.model.dataranges.DatatypeRestrictionString;
import org.semanticweb.HermiT.model.dataranges.DataConstant.Impl;
import org.semanticweb.HermiT.model.dataranges.DatatypeRestriction.DT;
import org.semanticweb.HermiT.model.dataranges.DatatypeRestriction.Facet;
import org.semanticweb.HermiT.reasoner.AbstractReasonerTest;

public class StringTest extends AbstractReasonerTest {
    
    public StringTest(String name) {
        super(name);
    }
    
    public void testSize1() throws Exception {
        DataRange dr = new DatatypeRestrictionString(DT.STRING);
        dr.addFacet(Facet.LENGTH, "1");
        CanonicalDataRange cdr = (CanonicalDataRange) dr;
        BigInteger num = cdr.getEnumerationSize();
        assertTrue(num == null);
    }

    public void testSize2() throws Exception {
        DataRange dr = new DatatypeRestrictionString(DT.STRING);
        dr.addFacet(Facet.MIN_LENGTH, "2");
        dr.addFacet(Facet.MAX_LENGTH, "2");
        CanonicalDataRange cdr = (CanonicalDataRange) dr;
        BigInteger num = cdr.getEnumerationSize();
        assertTrue(num == null);
    }
    
    public void testSize3() throws Exception {
        DataRange dr = new DatatypeRestrictionString(DT.STRING);
        dr.addFacet(Facet.MIN_LENGTH, "1");
        dr.addFacet(Facet.MAX_LENGTH, "1");
        dr.addFacet(Facet.PATTERN, "[a-z]");
        CanonicalDataRange cdr = (CanonicalDataRange) dr;
        BigInteger num = cdr.getEnumerationSize();
        assertTrue(num.compareTo(new BigInteger("26")) == 0);
    }
    
    public void testFacets() throws Exception {
        DataRange dr = new DatatypeRestrictionString(DT.STRING);
        dr.addFacet(Facet.MIN_LENGTH, "1");
        dr.addFacet(Facet.MIN_LENGTH, "2");
        dr.addFacet(Facet.MIN_LENGTH, "0");
        dr.addFacet(Facet.MAX_LENGTH, "10");
        dr.addFacet(Facet.MAX_LENGTH, "2");
        dr.addFacet(Facet.MAX_LENGTH, "5");
        dr.addFacet(Facet.PATTERN, "[a-z]*");
        CanonicalDataRange cdr = (CanonicalDataRange) dr;
        BigInteger num = cdr.getEnumerationSize();
        assertTrue(num.compareTo(new BigInteger("" + (26*26))) == 0);
    }
    
    public void testAssignments() throws Exception {
        DataRange dr = new DatatypeRestrictionString(DT.STRING);
        dr.addFacet(Facet.LENGTH, "1");
        dr.addFacet(Facet.PATTERN, "[a-z]*");
        CanonicalDataRange cdr = (CanonicalDataRange) dr;
        DataConstant c = cdr.getSmallestAssignment();
        assertTrue(c.getValue().equals("a"));
        cdr.notOneOf(c);
        c = cdr.getSmallestAssignment();
        assertTrue(c.getValue().equals("b"));
    }
    
    public void testAssignments2() throws Exception {
        DataRange dr = new DatatypeRestrictionString(DT.STRING);
        dr.addFacet(Facet.MIN_LENGTH, "1");
        dr.addFacet(Facet.MAX_LENGTH, "1");
        dr.addFacet(Facet.PATTERN, "([a-z])([A-D])*");
        CanonicalDataRange cdr = (CanonicalDataRange) dr;
        int i = 0;
        DataConstant c = cdr.getSmallestAssignment();
        while (c != null) {
            cdr.notOneOf(c);
            c = cdr.getSmallestAssignment();
            i++;
        }
        assertTrue(i == 26);
    }

    public void testAssignments3() throws Exception {
        DataRange dr = new DatatypeRestrictionString(DT.STRING);
        dr.addFacet(Facet.MIN_LENGTH, "1");
        dr.addFacet(Facet.MAX_LENGTH, "5");
        CanonicalDataRange cdr = (CanonicalDataRange) dr;
        DataConstant c = cdr.getSmallestAssignment();
        assertTrue(c == null);
    }
    
    public void testAssignments4() throws Exception {
        DataRange dr = new DatatypeRestrictionString(DT.STRING);
        dr.addFacet(Facet.MIN_LENGTH, "1");
        dr.addFacet(Facet.MAX_LENGTH, "3");
        CanonicalDataRange cdr = (CanonicalDataRange) dr;
        DataConstant c = cdr.getSmallestAssignment();
        assertTrue(c == null);
        dr.addFacet(Facet.PATTERN, "[a-z]*");
        c = cdr.getSmallestAssignment();
        assertTrue(c.getValue().equals("a"));
        cdr.notOneOf(c);
        DataConstant not = new DataConstant(Impl.IString, DT.STRING, "b");
        cdr.notOneOf(not);
        c = cdr.getSmallestAssignment();
        assertTrue(c.getValue().equals("c"));
    }
    
    public void testInvalidString() throws Exception {
        CanonicalDataRange cdr = new DatatypeRestrictionString(DT.STRING);
        ((DataRange) cdr).addFacet(Facet.MAX_LENGTH, "5");
        assertFalse(cdr.accepts(new DataConstant(Impl.IString, DT.STRING, "aaaaaa")));
        assertFalse(cdr.accepts(new DataConstant(Impl.IBase64Binary, DT.STRING, "a")));
    }
}
