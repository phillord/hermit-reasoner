package org.semanticweb.HermiT.model.dataranges;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.HermiT.Namespaces;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.Datatypes;

public class DataConstant implements Comparable<DataConstant> {
    
    public static Set<DataConstant> numericSpecials = new HashSet<DataConstant>();
    
    static {
        numericSpecials.add(new DataConstant(URI.create(org.semanticweb.owl.vocab.Namespaces.XSD + "double"), "-0"));
        numericSpecials.add(new DataConstant(URI.create(org.semanticweb.owl.vocab.Namespaces.XSD + "double"), "NaN"));
        numericSpecials.add(new DataConstant(URI.create(org.semanticweb.owl.vocab.Namespaces.XSD + "double"), "+INF"));
        numericSpecials.add(new DataConstant(URI.create(org.semanticweb.owl.vocab.Namespaces.XSD + "double"), "-INF"));
    }
    
    protected URI datatypeURI;
    protected String value;

    public DataConstant() {
        super();
    }
    
    public DataConstant(URI datatypeURI) {
        this.datatypeURI = datatypeURI;
    }
    
    public DataConstant(URI datatypeURI, String value) {
        this.datatypeURI = datatypeURI;
        this.value = value;
    }
    
    public URI getDatatypeURI() {
        return datatypeURI;
    }
    
    public void setDatatypeURI(URI datatypeURI) {
        this.datatypeURI = datatypeURI;
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
        return (datatypeURI == null ? datatypeURI == constant.getDatatypeURI() 
                                    : datatypeURI.equals(constant.getDatatypeURI())) 
                &&  (value == null ? value == constant.getValue() 
                                   : value.equals(constant.getValue()));
    }
    
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (!(o instanceof DataConstant)) return false;
//        DataConstant constant = (DataConstant) o;
//        if (datatypeURI == null) {
//            return datatypeURI == constant.getDatatypeURI();
//        } else {
//            if (constant.getDatatypeURI() == null) return false;
//            Set<URI> uris = new HashSet<URI>();
//            uris.add(datatypeURI);
//            uris.add(constant.getDatatypeURI());
//            if (datatypeURI.equals(constant.getDatatypeURI())) {
//                if (value != null 
//                        && DatatypeRestrictionInteger.canHandleAll(uris) 
//                        && value.equalsIgnoreCase("NaN")) {
//                    return false;
//                } 
//                return value == null ? value == constant.getValue() 
//                                     : value.equals(constant.getValue());
//            } else if (DatatypeRestrictionOWLRealPlus.canHandleAll(uris)) { 
//                if (value != null && value.equalsIgnoreCase("NaN")) {
//                    return false;
//                }
//                if ("-0".equals(value) 
//                        && ("+0".equals(constant.getValue()) 
//                                || "0".equals(constant.getValue()))) {
//                    return false;
//                }
//                if ("+INF".equalsIgnoreCase(value) 
//                        || "-INF".equalsIgnoreCase(value)) {
//                    return value.equals(constant.getValue());
//                }
//                BigDecimal thisNum = new BigDecimal(value);
//                BigDecimal thatNum = new BigDecimal(constant.getValue());
//                return thisNum.compareTo(thatNum) == 0;
//            } else if (DatatypeRestrictionInteger.canHandleAll(uris)) {
//                BigInteger thisNum = new BigInteger(value);
//                BigInteger thatNum = new BigInteger(constant.getValue());
//                return thisNum.compareTo(thatNum) == 0;
//            } else if (DatatypeRestrictionBoolean.canHandleAll(uris) 
//                    || DatatypeRestrictionDateTime.canHandleAll(uris)
//                    || DatatypeRestrictionString.canHandleAll(uris)
//                    || DatatypeRestrictionLiteral.canHandleAll(uris)) {
//                return value.equals(constant.getValue());
//            } else {
//                return false;
//            }
//        }
//    }
    
    public int hashCode() {
        int hashCode = 17;
        hashCode = 23 * hashCode + (datatypeURI != null ? datatypeURI.hashCode() : 0);
        hashCode = 13 * hashCode + (value != null ? value.hashCode() : 0);
        return hashCode;
    }
    
    public String toString() {
        return toString(Namespaces.none);
    }
    
    public String toString(Namespaces namespaces) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("(");
        buffer.append(namespaces.idFromUri(datatypeURI.toString()));
        buffer.append(" " + value);
        buffer.append(")");
        return buffer.toString();        
    }

    public int compareTo(DataConstant constant) {
        int result = datatypeURI.compareTo(constant.getDatatypeURI());
        if (result != 0 ) return result;
        return value.compareToIgnoreCase(constant.getValue());
    }
    
    public static boolean validate(String value, String type) {
        Automaton a = Datatypes.get(type);
        return a.run(value);
    }
}
