package org.semanticweb.HermiT.datatypes;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.HermiT.datatypes.old.DataConstant;
import org.semanticweb.HermiT.datatypes.old.DataConstant.Impl;
import org.semanticweb.HermiT.datatypes.old.InternalDatatypeRestriction.DT;
import org.semanticweb.HermiT.reasoner.AbstractReasonerTest;

public class DataConstantTest extends AbstractReasonerTest {

    public DataConstantTest(String name) {
        super(name);
    }
    
    public void testDataConstant() throws Exception {
        DataConstant constant1 = new DataConstant(Impl.IString, DT.STRING, "abc");
        DataConstant constant2 = new DataConstant(Impl.IString, DT.STRING, "abc");
        DataConstant constant3 = new DataConstant(Impl.IInteger, DT.INTEGER, "3");
        DataConstant constant4 = new DataConstant(Impl.IInteger, DT.INTEGER, "4");
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
        constants.add(new DataConstant(Impl.IInteger, DT.INTEGER, "4"));
        assertTrue(constants.size() == 1);
        try {
            BigDecimal dec = new BigDecimal("4.0");
            try {
                constants.add(new DataConstant(Impl.IInteger, DT.INTEGER, dec.toBigIntegerExact().toString()));
            } catch (ArithmeticException e) {
                constants.add(new DataConstant(Impl.IDecimal, DT.DECIMAL, dec.toString()));
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } 
        assertTrue(constants.size() == 1);
    }
    
    public void testDataConstant2() throws Exception {
        DataConstant constant1 = new DataConstant(Impl.IDouble, DT.DOUBLE, "+0");
        DataConstant constant2 = new DataConstant(Impl.IDouble, DT.DOUBLE, "-0");
        assertFalse(constant1.equals(constant2));
        DataConstant constant3 = new DataConstant(Impl.IDouble, DT.FLOAT, "+0");
        DataConstant constant4 = new DataConstant(Impl.IDouble, DT.FLOAT, "-0");
        assertFalse(constant3.equals(constant4));
        DataConstant constant5 = new DataConstant(Impl.IDouble, DT.DOUBLE, "NaN");
        DataConstant constant6 = new DataConstant(Impl.IDouble, DT.DOUBLE, "NaN");
        assertFalse(constant5.equals(constant6));
    }
    
    public void testNaN() throws Exception {
        DataConstant constant1 = new DataConstant(Impl.IDouble, DT.DOUBLE, "" + Double.NaN);
        DataConstant constant2 = new DataConstant(Impl.IDouble, DT.DOUBLE, "" + new Double("NaN"));
        DataConstant constant3 = new DataConstant(Impl.IDouble, DT.DOUBLE, "" + Double.NaN);
        assertFalse(constant1.equals(constant2));
        assertFalse(constant1.equals(constant3));
        assertFalse(constant2.equals(constant3));
    }
}
