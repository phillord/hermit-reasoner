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
/**OWLAxioms.*/
public class OWLAxioms {
    final Set<OWLClass> m_classes =new HashSet<>();
    final Set<OWLObjectProperty> m_objectProperties =new HashSet<>();
    final Set<OWLObjectProperty> m_objectPropertiesOccurringInOWLAxioms =new HashSet<>();
    final Set<OWLObjectPropertyExpression> m_complexObjectPropertyExpressions =new HashSet<>();
    final Set<OWLDataProperty> m_dataProperties =new HashSet<>();
    final Set<OWLNamedIndividual> m_namedIndividuals =new HashSet<>();
    final Collection<OWLClassExpression[]> m_conceptInclusions =new ArrayList<>();
    final Collection<OWLDataRange[]> m_dataRangeInclusions =new ArrayList<>();
    final Collection<OWLObjectPropertyExpression[]> m_simpleObjectPropertyInclusions =new ArrayList<>();
    final Collection<ComplexObjectPropertyInclusion> m_complexObjectPropertyInclusions =new ArrayList<>();
    final Collection<OWLObjectPropertyExpression[]> m_disjointObjectProperties =new ArrayList<>();
    final Set<OWLObjectPropertyExpression> m_reflexiveObjectProperties =new HashSet<>();
    final Set<OWLObjectPropertyExpression> m_irreflexiveObjectProperties =new HashSet<>();
    final Set<OWLObjectPropertyExpression> m_asymmetricObjectProperties =new HashSet<>();
    final Collection<OWLDataPropertyExpression[]> m_dataPropertyInclusions =new ArrayList<>();
    final Collection<OWLDataPropertyExpression[]> m_disjointDataProperties =new ArrayList<>();
    final Collection<OWLIndividualAxiom> m_facts =new HashSet<>();
    final Set<OWLHasKeyAxiom> m_hasKeys =new HashSet<>();
    /** contains custom datatypes from DatatypeDefinition axioms*/
    public final Set<String> m_definedDatatypesIRIs =new HashSet<>();
    final Collection<DisjunctiveRule> m_rules =new HashSet<>();

    static class ComplexObjectPropertyInclusion {
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

    static class DisjunctiveRule {
        public final SWRLAtom[] m_body;
        public final SWRLAtom[] m_head;

        public DisjunctiveRule(SWRLAtom[] body,SWRLAtom[] head) {
            m_body=body;
            m_head=head;
        }
        @Override
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
