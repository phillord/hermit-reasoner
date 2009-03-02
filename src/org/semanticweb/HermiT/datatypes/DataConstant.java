/*
 * Copyright 2008 by Oxford University; see license.txt for details
 */

package org.semanticweb.HermiT.datatypes;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.HermiT.Namespaces;
import org.semanticweb.HermiT.datatypes.DatatypeRestriction.DT;

/**
 * An instance of DataConstant represents a concrete datatype value. Its value 
 * is a String representation that has to be interpreted according to the given 
 * URI in DT. All values are assumed to be normalised and accordingly rounded 
 * (e.g., for doubles). The equals function of the DataConstant takes the 
 * requirements of the OWL2 spec into account regarding the inequality of NaN to 
 * itself. The datatype (DT) and the URI given there is not relevant, when 
 * determining equality, whereas the implementation is. The values of +0 and -0 
 * are to be handled by the datatype restrictions as equality here depends on 
 * whether we add -0 to an integer or to a double datatype restriction. 
 * 
 * @author BGlimm
 */
public class DataConstant implements Comparable<DataConstant>, Serializable {
    
    private static final long serialVersionUID = -4549371681595417404L;
    
    /**
     * A set containing data constants for the special values -0.0, NaN, 
     * +Infinity, and -Infinity. 
     */
    public static Set<DataConstant> numericSpecials = new HashSet<DataConstant>();
    
    static {
        // also the specials are doubles here, they count also as floats, since 
        // the equals methods handles this
        numericSpecials.add(new DataConstant(Impl.IDouble, DT.DOUBLE, "-0.0")); 
        numericSpecials.add(new DataConstant(Impl.IDouble, DT.DOUBLE, "NaN"));
        numericSpecials.add(new DataConstant(Impl.IDouble, DT.DOUBLE, "Infinity"));
        numericSpecials.add(new DataConstant(Impl.IDouble, DT.DOUBLE, "-Infinity"));
    }
    
    /**
     * The available implementations. A data constant of a datatype, say double, 
     * does not necessarily have to use the class DatatypeRestrictionDouble. In 
     * fact, since the numeric ranges are not disjoint, doubles that are also 
     * integers (such as 3.0) use the datatype URI for doubles (xsd:double) and 
     * the implementation for integers (DatatypeRestrictionInteger). Datatype 
     * instances for integer restrictions such as short use the URI for shorts 
     * (xsd:short via DT.SHORT) and the implementation of integers 
     * (Impl.IInteger).
     * 
     * @author BGlimm
     */
    public enum Impl {
        IInteger, 
        IDouble,
        IFloat, 
        IDecimal,
        IRational, 
        IDateTime,
        IString,
        IBoolean, 
        ILiteral, 
        IAnyURI,
        IBase64Binary, 
        IHexBinary,
        IUnkown;
    }
    
    protected DT datatype;
    protected Impl implementation;
    protected String value;
    protected String lang = "";

    /**
     * Create a data constant. 
     * @param implementation The implementation to be used for interpretating 
     * the value (e.g., integer). 
     * @param datatype The datatype of the constant (e.g., unsignedByte). 
     * @param value The actual value in its String representation (normalised if 
     * that is required, e.g., INF should be Infinity so that 
     * Double.parse(value) can be used to get the real value.)
     */
    public DataConstant(Impl implementation, DT datatype, String value) {
        this.implementation = implementation;
        this.datatype = datatype;
        this.value = value;
    }
    
    /**
     * As DataConstant(Impl implementation, DT datatype, String value), but for 
     * Strings that have a language tag. 
     * @param implementation The implementation to be used for interpretating 
     * the value (e.g., integer). 
     * @param datatype The datatype of the constant (e.g., unsignedByte). 
     * @param value The actual value in its String representation (normalised if 
     * that is required, e.g., INF should be Infinity so that 
     * Double.parse(value) can be used to get the real value.)
     * @param lang A string representing the language tag. 
     */
    public DataConstant(Impl implementation, DT datatype, String value, 
            String lang) {
        this(implementation, datatype, value);
        this.lang = lang;
    }
    
    /**
     * The implementation that should be used when interpreting the value of 
     * this constant. 
     * @return an implementation
     */
    public Impl getImplementation() {
        return implementation;
    }
    
    /**
     * The original datatype of this constant. 
     * @return a datatype (DT)
     */
    public DT getDatatype() {
        return datatype;
    }
    
    /**
     * A String representation of the value for this data constant that has to 
     * be interpreted according to the used implementation.  
     * @return a string representation of the value for this constant
     */
    public String getValue() {
        return value;
    }
    
    /**
     * Either empty if no language tag is used or a language tag. 
     * @return The language tag for this constant as String. 
     */
    public String getLang() {
        return lang;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DataConstant)) return false;
        DataConstant constant = (DataConstant) o;
        if (datatype != null 
                && value != null 
                && (datatype.equals(DT.DOUBLE) || datatype.equals(DT.FLOAT))
                && value.equalsIgnoreCase("NaN")) {
            return false;
        }
        if (datatype != null && value != null 
                && constant.getDatatype() != null && constant.getValue() != null
                && (datatype.equals(DT.DOUBLE) || datatype.equals(DT.FLOAT))
                && (constant.getDatatype().equals(DT.DOUBLE) || constant.getDatatype().equals(DT.FLOAT))
                && ((constant.getValue().equals("Infinity") && value.equals("Infinity"))
                || (constant.getValue().equals("-Infinity") && value.equals("-Infinity"))
                || (constant.getValue().equals("-0.0") && value.equals("-0.0")))) {
            return true;
        }
        return (implementation == null ? implementation == constant.getImplementation() 
                                    : implementation.equals(constant.getImplementation())) 
                && (value == null ? value == constant.getValue() 
                                   : value.equals(constant.getValue()))
                && lang.equals(constant.getLang());
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        int hashCode = 17;
        int impl;
        // although Infinity and -Infinity can use different implementations 
        // (float and double) they are the same
        if (implementation != null 
                && implementation == Impl.IFloat 
                && value != null 
                && (value.equals("-0.0") 
                        || value.equals("Infinity") 
                        || value.equals("-Infinity"))) {
            impl = Impl.IDouble.hashCode();
        } else {
            impl = (implementation != null ? implementation.hashCode() : 0);
        }
        hashCode = 23 * hashCode + impl;
        hashCode = 13 * hashCode + (value != null ? value.hashCode() : 0);
        hashCode = 17 * hashCode + (lang.hashCode());
        return hashCode;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return toString(Namespaces.none);
    }
    
    /**
     * Returns a String representation of this constant and uses the given 
     * namespaces to abbreviate long names. 
     * @param namespaces namespaces with prefixes
     * @return a String representation of this data constant
     */
    public String toString(Namespaces namespaces) {
        if (datatype == null) return "";
        StringBuffer buffer = new StringBuffer();
        buffer.append("(");
        buffer.append(namespaces.idFromUri(datatype.getURIAsString()));
        buffer.append(" " + value);
        if (lang != "") buffer.append("@" + lang);
        buffer.append(")");
        return buffer.toString();
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(DataConstant constant) {
        int result = implementation.compareTo(constant.getImplementation());
        if (result != 0 ) return result;
        result = value.compareTo(constant.getValue());
        if (result != 0 ) return result;
        return lang.compareTo(constant.getLang());
    }
}
