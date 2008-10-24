package org.semanticweb.HermiT.model.dataranges;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.HermiT.Namespaces;
import org.semanticweb.HermiT.model.dataranges.DatatypeRestriction.DT;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.Datatypes;

public class DataConstant implements Comparable<DataConstant> {
    
    public static Set<DataConstant> numericSpecials = new HashSet<DataConstant>();
    
    static {
        numericSpecials.add(new DataConstant(DT.DOUBLE, "-0"));
        numericSpecials.add(new DataConstant(DT.DOUBLE, "NaN"));
        numericSpecials.add(new DataConstant(DT.DOUBLE, "+INF"));
        numericSpecials.add(new DataConstant(DT.DOUBLE, "-INF"));
    }
    
    protected DT datatype;
    protected String value;

    public DataConstant() {
        super();
    }
    
    public DataConstant(DT datatype) {
        this.datatype = datatype;
    }
    
    public DataConstant(DT datatype, String value) {
        this.datatype = datatype;
        this.value = value;
    }
    
    public DT getDatatype() {
        return datatype;
    }
    
    public void setDatatype(DT datatype) {
        this.datatype = datatype;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
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
        return (datatype == null ? datatype == constant.getDatatype() 
                                    : datatype.equals(constant.getDatatype())) 
                &&  (value == null ? value == constant.getValue() 
                                   : value.equals(constant.getValue()));
    }
    
    public int hashCode() {
        int hashCode = 17;
        hashCode = 23 * hashCode + (datatype != null ? datatype.getURI().hashCode() : 0);
        hashCode = 13 * hashCode + (value != null ? value.hashCode() : 0);
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
        buffer.append(")");
        return buffer.toString();
    }

    public int compareTo(DataConstant constant) {
        int result = datatype.compareTo(constant.getDatatype());
        if (result != 0 ) return result;
        return value.compareToIgnoreCase(constant.getValue());
    }
    
    public static boolean validate(String value, String type) {
        Automaton a = Datatypes.get(type);
        return a.run(value);
    }
}
