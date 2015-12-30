/* Copyright 2008, 2009, 2010 by the Oxford University Computing Laboratory

   This file is part of HermiT.

   HermiT is free software: you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   HermiT is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public License
   along with HermiT.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.semanticweb.HermiT.datatypes;

import java.util.Set;

import org.semanticweb.HermiT.model.DatatypeRestriction;

/**
 * Implements the functions needed for a particular datatype.
 */
public interface DatatypeHandler {
    /**
     * @return managed datatypes
     */
    Set<String> getManagedDatatypeURIs();
    /**
     * @param lexicalForm lexicalForm
     * @param datatypeURI datatypeURI
     * @return literal
     * @throws MalformedLiteralException if the input is malformed
     */
    Object parseLiteral(String lexicalForm,String datatypeURI) throws MalformedLiteralException;
    /**
     * @param datatypeRestriction datatypeRestriction
     * @throws UnsupportedFacetException if facet unsupported
     */
    void validateDatatypeRestriction(DatatypeRestriction datatypeRestriction) throws UnsupportedFacetException;
    /**
     * @param datatypeRestriction datatypeRestriction
     * @return value space
     */
    ValueSpaceSubset createValueSpaceSubset(DatatypeRestriction datatypeRestriction);
    /**
     * @param valueSpaceSubset valueSpaceSubset
     * @param datatypeRestriction datatypeRestriction
     * @return intersection
     */
    ValueSpaceSubset conjoinWithDR(ValueSpaceSubset valueSpaceSubset,DatatypeRestriction datatypeRestriction);
    /**
     * @param valueSpaceSubset valueSpaceSubset
     * @param datatypeRestriction datatypeRestriction
     * @return intersection
     */
    ValueSpaceSubset conjoinWithDRNegation(ValueSpaceSubset valueSpaceSubset,DatatypeRestriction datatypeRestriction);
    /**
     * @param subsetDatatypeURI subsetDatatypeURI
     * @param supersetDatatypeURI supersetDatatypeURI
     * @return true if subset
     */
    boolean isSubsetOf(String subsetDatatypeURI,String supersetDatatypeURI);
    /**
     * @param datatypeURI1 datatypeURI1
     * @param datatypeURI2 datatypeURI2
     * @return true if disjointwith
     */
    boolean isDisjointWith(String datatypeURI1,String datatypeURI2);
}
