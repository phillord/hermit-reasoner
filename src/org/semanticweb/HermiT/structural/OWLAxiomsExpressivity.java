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

import java.util.List;

import org.semanticweb.owlapi.model.*;
/**OWLAxiomsExpressivity.*/
public class OWLAxiomsExpressivity implements OWLClassExpressionVisitor, OWLAxiomVisitor {
    /**has at most*/
    public boolean m_hasAtMostRestrictions;
    /**has inverse*/
    public boolean m_hasInverseRoles;
    /** has nominals*/
    public boolean m_hasNominals;
    /**has datatypes*/
    public boolean m_hasDatatypes;

    /**
     * @param axioms axioms
     */
    public OWLAxiomsExpressivity(OWLAxioms axioms) {
        axioms.m_conceptInclusions.forEach(c->c.forEach(d->d.accept(this)));
        for (List<OWLObjectPropertyExpression> inclusion : axioms.m_simpleObjectPropertyInclusions) {
            visitProperty(inclusion.get(0));
            visitProperty(inclusion.get(1));
        }
        for (OWLAxioms.ComplexObjectPropertyInclusion inclusion : axioms.m_complexObjectPropertyInclusions) {
            for (OWLObjectPropertyExpression subObjectProperty : inclusion.m_subObjectProperties)
                visitProperty(subObjectProperty);
            visitProperty(inclusion.m_superObjectProperty);
        }
        axioms.m_disjointObjectProperties.forEach(c->c.forEach(d->visitProperty(d)));
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
    }

    protected void visitProperty(OWLObjectPropertyExpression object) {
        if (object.isAnonymous())
            m_hasInverseRoles=true;
    }

    @Override
    public void visit(OWLClass desc) {
    }

    @Override
    public void visit(OWLObjectComplementOf object) {
        object.getOperand().accept(this);
    }

    @Override
    public void visit(OWLObjectIntersectionOf object) {
        object.operands().forEach(d->d.accept(this));
    }

    @Override
    public void visit(OWLObjectUnionOf object) {
        object.operands().forEach(d->d.accept(this));
    }

    @Override
    public void visit(OWLObjectOneOf object) {
        m_hasNominals=true;
    }

    @Override
    public void visit(OWLObjectSomeValuesFrom object) {
        visitProperty(object.getProperty());
        object.getFiller().accept(this);
    }

    @Override
    public void visit(OWLObjectHasValue object) {
        m_hasNominals=true;
        visitProperty(object.getProperty());
    }

    @Override
    public void visit(OWLObjectHasSelf object) {
        visitProperty(object.getProperty());
    }

    @Override
    public void visit(OWLObjectAllValuesFrom object) {
        visitProperty(object.getProperty());
        object.getFiller().accept(this);
    }

    @Override
    public void visit(OWLObjectMinCardinality object) {
        visitProperty(object.getProperty());
        object.getFiller().accept(this);
    }

    @Override
    public void visit(OWLObjectMaxCardinality object) {
        m_hasAtMostRestrictions=true;
        visitProperty(object.getProperty());
        object.getFiller().accept(this);
    }

    @Override
    public void visit(OWLObjectExactCardinality object) {
        m_hasAtMostRestrictions=true;
        visitProperty(object.getProperty());
        object.getFiller().accept(this);
    }

    @Override
    public void visit(OWLDataHasValue object) {
        m_hasDatatypes=true;
    }

    @Override
    public void visit(OWLDataSomeValuesFrom object) {
        m_hasDatatypes=true;
    }

    @Override
    public void visit(OWLDataAllValuesFrom object) {
        m_hasDatatypes=true;
    }

    @Override
    public void visit(OWLDataMinCardinality object) {
        m_hasDatatypes=true;
    }

    @Override
    public void visit(OWLDataMaxCardinality object) {
        m_hasDatatypes=true;
    }

    @Override
    public void visit(OWLDataExactCardinality object) {
        m_hasDatatypes=true;
    }

     @Override
    public void visit(OWLClassAssertionAxiom object) {
        object.getClassExpression().accept(OWLAxiomsExpressivity.this);
    }

    @Override
    public void visit(OWLObjectPropertyAssertionAxiom object) {
        visitProperty(object.getProperty());
    }

    @Override
    public void visit(OWLNegativeObjectPropertyAssertionAxiom object) {
        visitProperty(object.getProperty());
    }

    @Override
    public void visit(OWLDataPropertyAssertionAxiom object) {
        m_hasDatatypes=true;
    }
}
