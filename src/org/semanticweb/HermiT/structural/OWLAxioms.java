// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.structural;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLHasKeyAxiom;
import org.semanticweb.owlapi.model.OWLIndividualAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

public class OWLAxioms {
    public final Set<OWLClass> m_classes;
    public final Set<OWLObjectProperty> m_objectProperties;
    public final Set<OWLDataProperty> m_dataProperties;
    public final Set<OWLNamedIndividual> m_namedIndividuals;
    public final Collection<OWLClassExpression[]> m_conceptInclusions;
    public final Collection<OWLDataRange[]> m_dataRangeInclusions;
    public final Collection<OWLObjectPropertyExpression[]> m_simpleObjectPropertyInclusions;
    public final Collection<ComplexObjectPropertyInclusion> m_complexObjectPropertyInclusions;
    public final Collection<OWLObjectPropertyExpression[]> m_disjointObjectProperties;
    public final Set<OWLObjectPropertyExpression> m_reflexiveObjectProperties;
    public final Set<OWLObjectPropertyExpression> m_irreflexiveObjectProperties;
    public final Set<OWLObjectPropertyExpression> m_asymmetricObjectProperties;
    public final Set<OWLObjectPropertyExpression> m_unsatisfiableObjectProperties;
    public final Collection<OWLDataPropertyExpression[]> m_dataPropertyInclusions;
    public final Collection<OWLDataPropertyExpression[]> m_disjointDataProperties;
    public final Set<OWLDataPropertyExpression> m_unsatisfiableDataProperties;
    public final Collection<OWLIndividualAxiom> m_facts;
    public final Set<OWLHasKeyAxiom> m_hasKeys;
    public final Set<String> m_definedDatatypesIRIs; // contains custom datatypes from DatatypeDefinition axioms
    
    public OWLAxioms() {
        m_classes=new HashSet<OWLClass>();
        m_objectProperties=new HashSet<OWLObjectProperty>();
        m_dataProperties=new HashSet<OWLDataProperty>();
        m_namedIndividuals=new HashSet<OWLNamedIndividual>();
        m_conceptInclusions=new ArrayList<OWLClassExpression[]>();
        m_dataRangeInclusions=new ArrayList<OWLDataRange[]>();
        m_simpleObjectPropertyInclusions=new ArrayList<OWLObjectPropertyExpression[]>();
        m_complexObjectPropertyInclusions=new ArrayList<ComplexObjectPropertyInclusion>();
        m_disjointObjectProperties=new ArrayList<OWLObjectPropertyExpression[]>();
        m_reflexiveObjectProperties=new HashSet<OWLObjectPropertyExpression>();
        m_irreflexiveObjectProperties=new HashSet<OWLObjectPropertyExpression>();
        m_asymmetricObjectProperties=new HashSet<OWLObjectPropertyExpression>();
        m_unsatisfiableObjectProperties=new HashSet<OWLObjectPropertyExpression>();
        m_disjointDataProperties=new ArrayList<OWLDataPropertyExpression[]>();
        m_dataPropertyInclusions=new ArrayList<OWLDataPropertyExpression[]>();
        m_unsatisfiableDataProperties=new HashSet<OWLDataPropertyExpression>();
        m_facts=new HashSet<OWLIndividualAxiom>();
        m_hasKeys=new HashSet<OWLHasKeyAxiom>();
        m_definedDatatypesIRIs=new HashSet<String>();
    }
    
    public static class ComplexObjectPropertyInclusion {
        public final OWLObjectPropertyExpression[] m_subObjectProperties;
        public final OWLObjectPropertyExpression m_superObjectProperties;
        
        public ComplexObjectPropertyInclusion(OWLObjectPropertyExpression[] subObjectProperties,OWLObjectPropertyExpression superObjectProperties) {
            m_subObjectProperties=subObjectProperties;
            m_superObjectProperties=superObjectProperties;
        }
        public ComplexObjectPropertyInclusion(OWLObjectPropertyExpression transitiveObjectProperty) {
            m_subObjectProperties=new OWLObjectPropertyExpression[] { transitiveObjectProperty,transitiveObjectProperty };
            m_superObjectProperties=transitiveObjectProperty;
        }
    }
}
