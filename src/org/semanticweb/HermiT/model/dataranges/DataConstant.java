package org.semanticweb.HermiT.model.dataranges;

import java.net.URI;

import org.semanticweb.HermiT.Namespaces;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.Datatypes;

public class DataConstant implements Comparable<DataConstant> {
 
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
        return (datatypeURI == null ? datatypeURI == constant.getDatatypeURI() : datatypeURI.equals(constant.getDatatypeURI())) 
                &&  (value == null ? value == constant.getValue() : value.equals(constant.getValue()));
    }
    
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
