package org.semanticweb.HermiT.model;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.semanticweb.HermiT.Namespaces;

/**
 * Represents a datatype and a list of facet-value pairs. 
 */
public class DatatypeRestriction extends DataRange {
    
    protected URI datatypeURI;
    protected List<String> equalsValues = new ArrayList<String>();
    
    public DatatypeRestriction(URI datatypeURI) {
        this.datatypeURI = datatypeURI;
    }
    public DatatypeRestriction(URI datatypeURI, List<String> equalsValues) {
        this.datatypeURI = datatypeURI;
        this.equalsValues = equalsValues;
    }
    public void setDatatypeURI(URI datatypeURI) {
        this.datatypeURI = datatypeURI;
    }
    public URI getDatatypeURI() {
        return datatypeURI;
    }
    public List<String> getEqualsValues() {
        return equalsValues;
    }
    public void setEqualsValues(List<String> equalsValues) {
        this.equalsValues = equalsValues;
    }
    public boolean addequalsValue(String equalsValue) {
        return this.equalsValues.add(equalsValue);
    }
    public boolean isTop() {
        return this.getDatatypeURI().toString().equals(AtomicConcept.RDFS_LITERAL.getURI()); 
    }
    public String toString(Namespaces namespaces) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("(");
        buffer.append(namespaces.idFromUri(datatypeURI.toString()));
        for (String value : equalsValues) {
            buffer.append(" " + value);
        }
        buffer.append(")");
        return buffer.toString();        
    }
}
