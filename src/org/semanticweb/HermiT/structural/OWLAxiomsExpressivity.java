// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.structural;

import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLClassAssertionAxiom;
import org.semanticweb.owl.model.OWLClassExpression;
import org.semanticweb.owl.model.OWLClassExpressionVisitor;
import org.semanticweb.owl.model.OWLDataAllValuesFrom;
import org.semanticweb.owl.model.OWLDataExactCardinality;
import org.semanticweb.owl.model.OWLDataHasValue;
import org.semanticweb.owl.model.OWLDataMaxCardinality;
import org.semanticweb.owl.model.OWLDataMinCardinality;
import org.semanticweb.owl.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLDataSomeValuesFrom;
import org.semanticweb.owl.model.OWLIndividualAxiom;
import org.semanticweb.owl.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLObjectAllValuesFrom;
import org.semanticweb.owl.model.OWLObjectComplementOf;
import org.semanticweb.owl.model.OWLObjectExactCardinality;
import org.semanticweb.owl.model.OWLObjectHasSelf;
import org.semanticweb.owl.model.OWLObjectHasValue;
import org.semanticweb.owl.model.OWLObjectIntersectionOf;
import org.semanticweb.owl.model.OWLObjectMaxCardinality;
import org.semanticweb.owl.model.OWLObjectMinCardinality;
import org.semanticweb.owl.model.OWLObjectOneOf;
import org.semanticweb.owl.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLObjectPropertyExpression;
import org.semanticweb.owl.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owl.model.OWLObjectUnionOf;
import org.semanticweb.owl.util.OWLAxiomVisitorAdapter;

public class OWLAxiomsExpressivity extends OWLAxiomVisitorAdapter implements OWLClassExpressionVisitor {
    public boolean m_hasAtMostRestrictions;
    public boolean m_hasInverseRoles;
    public boolean m_hasNominals;
    public boolean m_hasDatatypes;

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
            visitProperty(inclusion.m_superObjectProperties);
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
        if (axioms.m_dataPropertyInclusions.size()>0 || axioms.m_disjointDataProperties.size()>0)
            m_hasDatatypes=true;
        for (OWLIndividualAxiom fact : axioms.m_facts)
            fact.accept(this);
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
