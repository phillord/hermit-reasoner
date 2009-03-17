// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.structural;

import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;

import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLDataPropertyExpression;
import org.semanticweb.owl.model.OWLObjectPropertyExpression;
import org.semanticweb.owl.model.OWLIndividualAxiom;

public class OWLAxioms {
    public final Collection<OWLDescription[]> m_conceptInclusions;
    public final Collection<OWLObjectPropertyExpression[]> m_objectPropertyInclusions;
    public final Collection<OWLObjectPropertyExpression[]> m_disjointObjectProperties;
    public final Set<OWLObjectPropertyExpression> m_reflexiveObjectProperties;
    public final Set<OWLObjectPropertyExpression> m_irreflexiveObjectProperties;
    public final Set<OWLObjectPropertyExpression> m_asymmetricObjectProperties;
    public final Set<OWLObjectPropertyExpression> m_transitiveObjectProperties;
    public final Collection<OWLDataPropertyExpression[]> m_dataPropertyInclusions;
    public final Collection<OWLDataPropertyExpression[]> m_disjointDataProperties;
    public final Collection<OWLIndividualAxiom> m_facts;
    public final Set<OWLHasKeyDummy> m_hasKeys;

    public OWLAxioms() {
        m_conceptInclusions=new ArrayList<OWLDescription[]>();
        m_objectPropertyInclusions=new ArrayList<OWLObjectPropertyExpression[]>();
        m_disjointObjectProperties=new ArrayList<OWLObjectPropertyExpression[]>();
        m_reflexiveObjectProperties=new HashSet<OWLObjectPropertyExpression>();
        m_irreflexiveObjectProperties=new HashSet<OWLObjectPropertyExpression>();
        m_asymmetricObjectProperties=new HashSet<OWLObjectPropertyExpression>();
        m_transitiveObjectProperties=new HashSet<OWLObjectPropertyExpression>();
        m_disjointDataProperties=new ArrayList<OWLDataPropertyExpression[]>();
        m_dataPropertyInclusions=new ArrayList<OWLDataPropertyExpression[]>();
        m_facts=new HashSet<OWLIndividualAxiom>();
        m_hasKeys=new HashSet<OWLHasKeyDummy>();
    }
}
