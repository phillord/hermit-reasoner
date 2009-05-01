// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.structural;

import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;

import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLDataPropertyExpression;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLObjectPropertyExpression;
import org.semanticweb.owl.model.OWLIndividualAxiom;

public class OWLAxioms {
    public final Set<OWLClass> m_classes;
    public final Set<OWLObjectProperty> m_objectProperties;
    public final Set<OWLDataProperty> m_dataProperties;
    public final Set<OWLIndividual> m_individuals;
    public final Collection<OWLDescription[]> m_conceptInclusions;
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
    public final Set<OWLHasKeyDummy> m_hasKeys;

    public OWLAxioms() {
        m_classes=new HashSet<OWLClass>();
        m_objectProperties=new HashSet<OWLObjectProperty>();
        m_dataProperties=new HashSet<OWLDataProperty>();
        m_individuals=new HashSet<OWLIndividual>();
        m_conceptInclusions=new ArrayList<OWLDescription[]>();
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
        m_hasKeys=new HashSet<OWLHasKeyDummy>();
    }
    
    public static class ComplexObjectPropertyInclusion {
        public final OWLObjectPropertyExpression[] m_subObjectProperties;
        public final OWLObjectPropertyExpression m_superObjectProperties;
        
        public ComplexObjectPropertyInclusion(OWLObjectPropertyExpression[] subObjectProperties,OWLObjectPropertyExpression superObjectProperties) {
            m_subObjectProperties=subObjectProperties;
            m_superObjectProperties=superObjectProperties;
        }
        public ComplexObjectPropertyInclusion(OWLObjectPropertyExpression trasnitiveObjectProperty) {
            m_subObjectProperties=new OWLObjectPropertyExpression[] { trasnitiveObjectProperty,trasnitiveObjectProperty };
            m_superObjectProperties=trasnitiveObjectProperty;
        }
    }
}
