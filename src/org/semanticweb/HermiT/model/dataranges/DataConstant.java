package org.semanticweb.HermiT.model.dataranges;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.HermiT.Namespaces;
import org.semanticweb.HermiT.model.dataranges.DatatypeRestriction.DT;
import org.semanticweb.HermiT.model.dataranges.DatatypeRestriction.Impl;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.Datatypes;

public class DataConstant implements Comparable<DataConstant> {
    
    public static Set<DataConstant> numericSpecials = new HashSet<DataConstant>();
    
    static {
        numericSpecials.add(new DataConstant(Impl.IDouble, DT.DOUBLE, "-0.0"));
        numericSpecials.add(new DataConstant(Impl.IDouble, DT.DOUBLE, "NaN"));
        numericSpecials.add(new DataConstant(Impl.IDouble, DT.DOUBLE, "+Infinity"));
        numericSpecials.add(new DataConstant(Impl.IDouble, DT.DOUBLE, "-Infinity"));
    }
    
    protected DT datatype;
    protected Impl implementation;
    protected String value;
    protected String lang = "";

    public DataConstant(Impl implementation, DT datatype, String value) {
        this.implementation = implementation;
        this.datatype = datatype;
        this.value = value;
    }
    
    public DataConstant(Impl implementation, DT datatype, String value, String lang) {
        this(implementation, datatype, value);
        this.lang = lang;
    }
    
    public Impl getImplementation() {
        return implementation;
    }
    
    public DT getDatatype() {
        return datatype;
    }
    
    public String getValue() {
        return value;
    }
    
    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DataConstant)) return false;
        DataConstant constant = (DataConstant) o;
        if (datatype != null 
                && value != null 
                && datatype.equals(DT.DOUBLE)
                && value.equalsIgnoreCase("NaN")) {
            return false;
        }
        return (implementation == null ? implementation == constant.getImplementation() 
                                    : implementation.equals(constant.getImplementation())) 
                && (value == null ? value == constant.getValue() 
                                   : value.equals(constant.getValue()))
                && lang.equals(constant.getLang());
    }
    
    public int hashCode() {
        int hashCode = 17;
        hashCode = 23 * hashCode + (implementation != null ? implementation.hashCode() : 0);
        hashCode = 13 * hashCode + (value != null ? value.hashCode() : 0);
        hashCode = 17 * hashCode + (lang.hashCode());
        return hashCode;
    }
    
    public String toString() {
        return toString(Namespaces.none);
    }
    
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

    public int compareTo(DataConstant constant) {
        int result = implementation.compareTo(constant.getImplementation());
        if (result != 0 ) return result;
        result = value.compareTo(constant.getValue());
        if (result != 0 ) return result;
        return lang.compareTo(constant.getLang());
    }
    
    public static boolean validate(String value, String type) {
        Automaton a = Datatypes.get(type);
        return a.run(value);
    }
}
