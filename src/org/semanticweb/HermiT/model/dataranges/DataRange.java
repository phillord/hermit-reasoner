/*
 * Copyright 2008 by Oxford University; see license.txt for details
 */

package org.semanticweb.HermiT.model.dataranges;

import java.net.URI;
import java.util.Set;

import org.semanticweb.HermiT.model.DLPredicate;
import org.semanticweb.HermiT.model.dataranges.DatatypeRestriction.DT;
import org.semanticweb.HermiT.model.dataranges.DatatypeRestriction.Facets;


/**
 * The DataRange interface should be used when creating data ranges during 
 * parsing of the ontology. It supports all methods that are required at the 
 * time, e.g., adding facets and negation. It further allows for simple tests 
 * that are performed by the DatatypeManager before the real satisfiability 
 * testing, e.g., checking if the range is empty/bottom. Later in the 
 * DatatypeManager, the interface CanonicalDataRange is used, which provides 
 * methods for conjoining the restrictions of several data ranges into one 
 * canonical representation. The split into two interfaces is deliberate since 
 * some functions should not be used in arbitrary order, e.g., after conjoining 
 * facets, adding facets might result in strange results and adding forbidden 
 * assignments (notOneOf) should only be done by the DatatypeManager during 
 * satisfiability checking on the canonical ranges as their values might 
 * otherwise not be correctly handled, e.g., oneOf and notOneOf are not checked 
 * for disjointness. Thus, using DataRange during loading and CanonicalDataRange 
 * in the DatatypeManager makes sure that these assumptions hold as required. 
 * 
 * @author BGlimm
 */
public interface DataRange extends DLPredicate {
    
    /**
     * @return an new instance of the concrete implementation on which the 
     *         method is called
     */
    public CanonicalDataRange getNewInstance();

    /**
     * The URI of the datatype that implements this DataRange instance 
     * @return The URI for the type of the concrete implementation for this 
     *         DataRange.   
     */
    public URI getDatatypeURI();
    
    /**
     * The datatype implements this DataRange instance 
     * @return The datatype for the type of the concrete implementation for this 
     *         DataRange.   
     */
    public DT getDatatype();
    
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
     * Constants that are representing the assignments that are not allowed for 
     * this datatype restriction. The strings are String representations of the 
     * datatype that concretely implements the DataRange, e.g., if the concrete 
     * implementation has URI for integers, the returned strings have to be 
     * interpreted as integers.  
     * @return A set of constants that are not allowed as assignment for the 
     *         current datatype restrictions and are to be interpreted according 
     *         to the datatype URI of the concrete implementation of the 
     *         DataRange. 
     */
    public Set<DataConstant> getNotOneOf();

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
}
