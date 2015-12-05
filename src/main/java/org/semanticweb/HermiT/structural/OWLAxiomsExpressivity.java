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

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitor;
import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;
import org.semanticweb.owlapi.model.OWLDataExactCardinality;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLDataMaxCardinality;
import org.semanticweb.owlapi.model.OWLDataMinCardinality;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLIndividualAxiom;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectExactCardinality;
import org.semanticweb.owlapi.model.OWLObjectHasSelf;
import org.semanticweb.owlapi.model.OWLObjectHasValue;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.util.OWLAxiomVisitorAdapter;

public class OWLAxiomsExpressivity extends OWLAxiomVisitorAdapter implements OWLClassExpressionVisitor {
    public boolean m_hasAtMostRestrictions;
    public boolean m_hasInverseRoles;
    public boolean m_hasNominals;
    public boolean m_hasDatatypes;
    public boolean m_hasSWRLRules;

    public OWLAxiomsExpressivity(OWLAxioms axioms) {
        for (OWLClassExpression[] inclusion : axioms.m_conceptInclusions)
            for (OWLClassExpression description : inclusion)
                description.accept(this);
        for (OWLObjectPropertyExpression[] inclusion : axioms.m_simpleObjectPropertyInclusions) {
            visitProperty(inclusion[0]);
            visitProperty(inclusion[1]);
        }
        for (OWLAxioms.ComplexObjectPropertyInclusion inclusion : axioms.m_complexObjectPropertyInclusions) {
            for (OWLObjectPropertyExpression subObjectProperty : inclusion.m_subObjectProperties)
                visitProperty(subObjectProperty);
            visitProperty(inclusion.m_superObjectProperty);
        }
        for (OWLObjectPropertyExpression[] disjoint : axioms.m_disjointObjectProperties)
            for (int index=0;index<disjoint.length;index++)
                visitProperty(disjoint[index]);
        for (OWLObjectPropertyExpression property : axioms.m_reflexiveObjectProperties)
            visitProperty(property);
        for (OWLObjectPropertyExpression property : axioms.m_irreflexiveObjectProperties)
            visitProperty(property);
        for (OWLObjectPropertyExpression property : axioms.m_asymmetricObjectProperties)
            visitProperty(property);
        if (!axioms.m_dataProperties.isEmpty()
        		|| !axioms.m_disjointDataProperties.isEmpty()
        		|| !axioms.m_dataPropertyInclusions.isEmpty()
        		|| !axioms.m_dataRangeInclusions.isEmpty()
        		|| !axioms.m_definedDatatypesIRIs.isEmpty())
            m_hasDatatypes=true;
        for (OWLIndividualAxiom fact : axioms.m_facts)
            fact.accept(this);
        m_hasSWRLRules=!axioms.m_rules.isEmpty();
    }

    protected void visitProperty(OWLObjectPropertyExpression object) {
        if (object.getSimplified().isAnonymous())
            m_hasInverseRoles=true;
    }

    public void visit(OWLClass desc) {
    }

    public void visit(OWLObjectComplementOf object) {
        object.getOperand().accept(this);
    }

    public void visit(OWLObjectIntersectionOf object) {
        for (OWLClassExpression description : object.getOperands())
            description.accept(this);
    }

    public void visit(OWLObjectUnionOf object) {
        for (OWLClassExpression description : object.getOperands())
            description.accept(this);
    }

    public void visit(OWLObjectOneOf object) {
        m_hasNominals=true;
    }

    public void visit(OWLObjectSomeValuesFrom object) {
        visitProperty(object.getProperty());
        object.getFiller().accept(this);
    }

    public void visit(OWLObjectHasValue object) {
        m_hasNominals=true;
        visitProperty(object.getProperty());
    }

    public void visit(OWLObjectHasSelf object) {
        visitProperty(object.getProperty());
    }

    public void visit(OWLObjectAllValuesFrom object) {
        visitProperty(object.getProperty());
        object.getFiller().accept(this);
    }

    public void visit(OWLObjectMinCardinality object) {
        visitProperty(object.getProperty());
        object.getFiller().accept(this);
    }

    public void visit(OWLObjectMaxCardinality object) {
        m_hasAtMostRestrictions=true;
        visitProperty(object.getProperty());
        object.getFiller().accept(this);
    }

    public void visit(OWLObjectExactCardinality object) {
        m_hasAtMostRestrictions=true;
        visitProperty(object.getProperty());
        object.getFiller().accept(this);
    }

    public void visit(OWLDataHasValue object) {
        m_hasDatatypes=true;
    }

    public void visit(OWLDataSomeValuesFrom object) {
        m_hasDatatypes=true;
    }

    public void visit(OWLDataAllValuesFrom object) {
        m_hasDatatypes=true;
    }

    public void visit(OWLDataMinCardinality object) {
        m_hasDatatypes=true;
    }

    public void visit(OWLDataMaxCardinality object) {
        m_hasDatatypes=true;
    }

    public void visit(OWLDataExactCardinality object) {
        m_hasDatatypes=true;
    }

     public void visit(OWLClassAssertionAxiom object) {
        object.getClassExpression().accept(OWLAxiomsExpressivity.this);
    }

    public void visit(OWLObjectPropertyAssertionAxiom object) {
        visitProperty(object.getProperty());
    }

    public void visit(OWLNegativeObjectPropertyAssertionAxiom object) {
        visitProperty(object.getProperty());
    }

    public void visit(OWLDataPropertyAssertionAxiom object) {
        m_hasDatatypes=true;
    }
}
