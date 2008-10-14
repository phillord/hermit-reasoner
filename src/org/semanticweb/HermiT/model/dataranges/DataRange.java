package org.semanticweb.HermiT.model.dataranges;

import java.net.URI;
import java.util.Set;

import org.semanticweb.HermiT.Namespaces;
import org.semanticweb.HermiT.model.DLPredicate;
import org.semanticweb.HermiT.model.dataranges.DatatypeRestriction.Facets;


public interface DataRange extends DLPredicate {
        
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.DLPredicate#getArity()
     */
    public int getArity();
    
    /**
     * The URI of the datatype that implements this DataRange instance 
     * @return The URI for the type of the concrete implementation for this 
     *         DataRange.   
     */
    public URI getDatatypeURI();
    
    /**
     * Constants that are representing the allowed assignments for this datatype 
     * restriction. The strings are String representations of the datatype that 
     * concretely implements the DataRange, e.g., if the concrete implementation 
     * has URI for integers, the returned strings have to be interpreted as 
     * integers.  
     * @return A set of constants that represent the current datatype 
     *         restrictions and are to be interpreted according to the datatype 
     *         URI of the concrete implementation of the DataRange. 
     */
    public Set<DataConstant> getOneOf();
    
    /**
     * Adds constant to the values that represent this data range. 
     * @param constant A constants that is a String representation of a value 
     *        that is to be interpreted according to the datatype URI of the 
     *        concrete implementation of the DataRange 
     * @return true if the oneOf values did not already contain the given 
     *         constant and false otherwise
     */
    public boolean addOneOf(DataConstant constant);
    
    /**
     * Checks whether this data range is negated. 
     * @return true if negated and false otherwise.
     */
    public boolean isNegated();
    
    /**
     * Negate this data range, i.e., if the range was negated it is no longer 
     * negated afterwards and if it was not negated it will be negated afterwards. 
     */
    public void negate();
    
    /**
     * Checks whether this data range cannot contain values
     * @return true if the restrictions on this data range cause the 
     *         interpretation of it to be empty and false otherwise. 
     */
    public boolean isBottom();
    
    /**
     * Adds a facet, if it is supported by the implementation that implements 
     * the data range. 
     * @param facet A facet. 
     * @param value A String representation of the facet value. 
     * @throws IllegalArgumentException if the facet is not supported by the 
     *         concrete realisation of this data range.  
     */
    public void addFacet(Facets facet, String value) throws IllegalArgumentException;
    
    /**
     * Checks whether the given facet is supported by the concrete 
     * implementation of this data range. 
     * @param facet a facet.
     * @return true if the facet is supported by the concrete implementation of 
     *         this data range and false otherwise.
     */
    public boolean supports(Facets facet);
    
    /* (non-Javadoc)
     * @see org.semanticweb.HermiT.model.DLPredicate#toString(org.semanticweb.HermiT.Namespaces)
     */
    public String toString(Namespaces namespaces); 
}
