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
import org.semanticweb.owlapi.model.SWRLAtom;

public class OWLAxioms {
    public final Set<OWLClass> m_classes;
    public final Set<OWLObjectProperty> m_objectProperties;
    public final Set<OWLObjectProperty> m_objectPropertiesOccurringInOWLAxioms;
    public final Set<OWLObjectPropertyExpression> m_complexObjectPropertyExpressions;
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
    public final Collection<OWLDataPropertyExpression[]> m_dataPropertyInclusions;
    public final Collection<OWLDataPropertyExpression[]> m_disjointDataProperties;
    public final Collection<OWLIndividualAxiom> m_facts;
    public final Set<OWLHasKeyAxiom> m_hasKeys;
    public final Set<String> m_definedDatatypesIRIs; // contains custom datatypes from DatatypeDefinition axioms
    public final Collection<DisjunctiveRule> m_rules;

    public OWLAxioms() {
        m_classes=new HashSet<OWLClass>();
        m_objectProperties=new HashSet<OWLObjectProperty>();
        m_objectPropertiesOccurringInOWLAxioms=new HashSet<OWLObjectProperty>();
        m_complexObjectPropertyExpressions=new HashSet<OWLObjectPropertyExpression>();
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
        m_disjointDataProperties=new ArrayList<OWLDataPropertyExpression[]>();
        m_dataPropertyInclusions=new ArrayList<OWLDataPropertyExpression[]>();
        m_facts=new HashSet<OWLIndividualAxiom>();
        m_hasKeys=new HashSet<OWLHasKeyAxiom>();
        m_definedDatatypesIRIs=new HashSet<String>();
        m_rules=new HashSet<DisjunctiveRule>();
    }

    public static class ComplexObjectPropertyInclusion {
        public final OWLObjectPropertyExpression[] m_subObjectProperties;
        public final OWLObjectPropertyExpression m_superObjectProperty;

        public ComplexObjectPropertyInclusion(OWLObjectPropertyExpression[] subObjectProperties,OWLObjectPropertyExpression superObjectPropery) {
            m_subObjectProperties=subObjectProperties;
            m_superObjectProperty=superObjectPropery;
        }
        public ComplexObjectPropertyInclusion(OWLObjectPropertyExpression transitiveObjectProperty) {
            m_subObjectProperties=new OWLObjectPropertyExpression[] { transitiveObjectProperty,transitiveObjectProperty };
            m_superObjectProperty=transitiveObjectProperty;
        }
    }

    public static class DisjunctiveRule {
        public final SWRLAtom[] m_body;
        public final SWRLAtom[] m_head;

        public DisjunctiveRule(SWRLAtom[] body,SWRLAtom[] head) {
            m_body=body;
            m_head=head;
        }
        public String toString() {
            StringBuffer buffer=new StringBuffer();
            boolean first=true;
            for (SWRLAtom atom : m_body) {
                if (first)
                    first=false;
                else
                    buffer.append(" /\\ ");
                buffer.append(atom.toString());
            }
            buffer.append(" -: ");
            first=true;
            for (SWRLAtom atom : m_head) {
                if (first)
                    first=false;
                else
                    buffer.append(" \\/ ");
                buffer.append(atom.toString());
            }
            return buffer.toString();
        }
    }
}
