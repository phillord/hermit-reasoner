// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.datatypes;

import java.util.Map;
import java.util.HashMap;

import org.semanticweb.HermiT.Prefixes;
import org.semanticweb.HermiT.model.DatatypeRestriction;
import org.semanticweb.HermiT.datatypes.bool.BooleanDatatypeHandler;
import org.semanticweb.HermiT.datatypes.rdftext.RDFTextDatatypeHandler;
import org.semanticweb.HermiT.datatypes.owlreal.OWLRealDatatypeHandler;
import org.semanticweb.HermiT.datatypes.doublenum.DoubleDatatypeHandler;
import org.semanticweb.HermiT.datatypes.floatnum.FloatDatatypeHandler;
import org.semanticweb.HermiT.datatypes.datetime.DateTimeDatatypeHandler;
import org.semanticweb.HermiT.datatypes.binarydata.BinaryDataDatatypeHandler;
import org.semanticweb.HermiT.datatypes.anyuri.AnyURIDatatypeHandler;

/**
 * A registry for all available datatype handlers.
 */
public class DatatypeRegistry {
    protected static final Map<String,DatatypeHandler> s_handlersByDatatypeURI=new HashMap<String,DatatypeHandler>();
    protected static final Map<Class<?>,DatatypeHandler> s_handlersByDataValueClass=new HashMap<Class<?>,DatatypeHandler>();
    static {
        registerDatatypeHandler(new BooleanDatatypeHandler());
        registerDatatypeHandler(new RDFTextDatatypeHandler());
        registerDatatypeHandler(new OWLRealDatatypeHandler());
        registerDatatypeHandler(new DoubleDatatypeHandler());
        registerDatatypeHandler(new FloatDatatypeHandler());
        registerDatatypeHandler(new DateTimeDatatypeHandler());
        registerDatatypeHandler(new BinaryDataDatatypeHandler());
        registerDatatypeHandler(new AnyURIDatatypeHandler());
    }
    
    public static void registerDatatypeHandler(DatatypeHandler datatypeHandler) {
        synchronized (s_handlersByDatatypeURI) {
            for (String datatypeURI : datatypeHandler.getManagedDatatypeURIs())
                if (s_handlersByDatatypeURI.containsKey(datatypeURI))
                    throw new IllegalArgumentException("Datatype handler for datatype '"+datatypeURI+"' has already been registed.");
            for (Class<?> dataValueClass: datatypeHandler.getManagedDataValueClasses())
                if (s_handlersByDataValueClass.containsKey(dataValueClass))
                    throw new IllegalArgumentException("Datatype handler for data value class '"+dataValueClass+"' has already been registed.");
            for (String datatypeURI : datatypeHandler.getManagedDatatypeURIs())
                s_handlersByDatatypeURI.put(datatypeURI,datatypeHandler);
            for (Class<?> dataValueClass: datatypeHandler.getManagedDataValueClasses())
                s_handlersByDataValueClass.put(dataValueClass,datatypeHandler);
        }
    }
    protected static DatatypeHandler getDatatypeHandlerFor(String datatypeURI) throws UnsupportedDatatypeException {
        DatatypeHandler datatypeHandler;
        synchronized (s_handlersByDatatypeURI) {
            datatypeHandler=s_handlersByDatatypeURI.get(datatypeURI);
        }
        if (datatypeHandler==null)
            throw new UnsupportedDatatypeException("Datatype handler for datatype '"+datatypeURI+"' has not been registed.");
        else
            return datatypeHandler;
    }
    protected static DatatypeHandler getDatatypeHandlerFor(DatatypeRestriction datatypeRestriction) throws UnsupportedDatatypeException {
        return getDatatypeHandlerFor(datatypeRestriction.getDatatypeURI());
    }
    public static String toString(Prefixes prefixes,Object dataValue) {
        DatatypeHandler datatypeHandler;
        Class<?> dataValueClass=dataValue.getClass();
        synchronized (s_handlersByDatatypeURI) {
            datatypeHandler=s_handlersByDataValueClass.get(dataValueClass);
        }
        if (datatypeHandler==null)
            throw new IllegalArgumentException("Datatype handler for data value of class '"+dataValueClass+"' has not been registed.");
        else
            return datatypeHandler.toString(prefixes,dataValue);
    }
    public static Object parseLiteral(String lexicalForm,String datatypeURI) throws MalformedLiteralException,UnsupportedDatatypeException {
        return getDatatypeHandlerFor(datatypeURI).parseLiteral(lexicalForm,datatypeURI);
    }
    public static void validateDatatypeRestriction(DatatypeRestriction datatypeRestriction) throws UnsupportedDatatypeException,UnsupportedFacetException {
        getDatatypeHandlerFor(datatypeRestriction).validateDatatypeRestriction(datatypeRestriction);
    }
    public static ValueSpaceSubset createValueSpaceSubset(DatatypeRestriction datatypeRestriction) {
        return getDatatypeHandlerFor(datatypeRestriction).createValueSpaceSubset(datatypeRestriction);
    }
    public static ValueSpaceSubset conjoinWithDR(ValueSpaceSubset valueSpaceSubset,DatatypeRestriction datatypeRestriction) {
        return getDatatypeHandlerFor(datatypeRestriction).conjoinWithDR(valueSpaceSubset,datatypeRestriction);
    }
    public static ValueSpaceSubset conjoinWithDRNegation(ValueSpaceSubset valueSpaceSubset,DatatypeRestriction datatypeRestriction) {
        return getDatatypeHandlerFor(datatypeRestriction).conjoinWithDRNegation(valueSpaceSubset,datatypeRestriction);
    }
    public static boolean isSubsetOf(String subsetDatatypeURI,String supersetDatatypeURI) {
        DatatypeHandler datatypeHandler=getDatatypeHandlerFor(subsetDatatypeURI);
        if (datatypeHandler.getManagedDatatypeURIs().contains(supersetDatatypeURI))
            return datatypeHandler.isSubsetOf(subsetDatatypeURI,supersetDatatypeURI);
        else
            return false;
    }
    public static boolean isDisjointWith(String datatypeURI1,String datatypeURI2) {
        DatatypeHandler datatypeHandler=getDatatypeHandlerFor(datatypeURI1);
        if (datatypeHandler.getManagedDatatypeURIs().contains(datatypeURI2))
            return datatypeHandler.isDisjointWith(datatypeURI1,datatypeURI2);
        else
            return true;
    }
}
