// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.structural;

import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLClassAssertionAxiom;
import org.semanticweb.owl.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLObjectPropertyInverse;
import org.semanticweb.owl.model.OWLObjectPropertyExpression;
import org.semanticweb.owl.model.OWLDataAllRestriction;
import org.semanticweb.owl.model.OWLDataExactCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataMaxCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataMinCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataSomeRestriction;
import org.semanticweb.owl.model.OWLDataValueRestriction;
import org.semanticweb.owl.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLDescriptionVisitor;
import org.semanticweb.owl.model.OWLObjectAllRestriction;
import org.semanticweb.owl.model.OWLObjectComplementOf;
import org.semanticweb.owl.model.OWLObjectExactCardinalityRestriction;
import org.semanticweb.owl.model.OWLObjectIntersectionOf;
import org.semanticweb.owl.model.OWLObjectMaxCardinalityRestriction;
import org.semanticweb.owl.model.OWLObjectMinCardinalityRestriction;
import org.semanticweb.owl.model.OWLObjectOneOf;
import org.semanticweb.owl.model.OWLObjectSelfRestriction;
import org.semanticweb.owl.model.OWLObjectSomeRestriction;
import org.semanticweb.owl.model.OWLObjectUnionOf;
import org.semanticweb.owl.model.OWLObjectValueRestriction;
import org.semanticweb.owl.model.OWLIndividualAxiom;
import org.semanticweb.owl.util.OWLAxiomVisitorAdapter;

public class OWLAxiomsExpressivity extends OWLAxiomVisitorAdapter implements OWLDescriptionVisitor {
    public boolean m_hasAtMostRestrictions;
    public boolean m_hasInverseRoles;
    public boolean m_hasNominals;
    public boolean m_hasDatatypes;

    public OWLAxiomsExpressivity(OWLAxioms axioms) {
        for (OWLDescription[] inclusion : axioms.m_conceptInclusions)
            for (OWLDescription description : inclusion)
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
        for (OWLObjectPropertyExpression property : axioms.m_transitiveObjectProperties)
            visitProperty(property);
        if (axioms.m_dataPropertyInclusions.size()>0 || axioms.m_disjointDataProperties.size()>0)
            m_hasDatatypes=true;
        for (OWLIndividualAxiom fact : axioms.m_facts)
            fact.accept(this);
    }
    
    protected void visitProperty(OWLObjectPropertyExpression object) {
        if (object instanceof OWLObjectPropertyInverse)
            m_hasInverseRoles=true;
    }

    public void visit(OWLClass desc) {
    }

    public void visit(OWLObjectComplementOf object) {
        object.getOperand().accept(this);
    }

    public void visit(OWLObjectIntersectionOf object) {
        for (OWLDescription description : object.getOperands())
            description.accept(this);
    }

    public void visit(OWLObjectUnionOf object) {
        for (OWLDescription description : object.getOperands())
            description.accept(this);
    }

    public void visit(OWLObjectOneOf object) {
        m_hasNominals=true;
    }

    public void visit(OWLObjectSomeRestriction object) {
        visitProperty(object.getProperty());
        object.getFiller().accept(this);
    }

    public void visit(OWLObjectValueRestriction object) {
        m_hasNominals=true;
        visitProperty(object.getProperty());
    }

    public void visit(OWLObjectSelfRestriction object) {
        visitProperty(object.getProperty());
    }

    public void visit(OWLObjectAllRestriction object) {
        visitProperty(object.getProperty());
        object.getFiller().accept(this);
    }

    public void visit(OWLObjectMinCardinalityRestriction object) {
        visitProperty(object.getProperty());
        object.getFiller().accept(this);
    }

    public void visit(OWLObjectMaxCardinalityRestriction object) {
        m_hasAtMostRestrictions=true;
        visitProperty(object.getProperty());
        object.getFiller().accept(this);
    }

    public void visit(OWLObjectExactCardinalityRestriction object) {
        m_hasAtMostRestrictions=true;
        visitProperty(object.getProperty());
        object.getFiller().accept(this);
    }

    public void visit(OWLDataValueRestriction object) {
        m_hasDatatypes=true;
    }

    public void visit(OWLDataSomeRestriction object) {
        m_hasDatatypes=true;
    }

    public void visit(OWLDataAllRestriction object) {
        m_hasDatatypes=true;
    }

    public void visit(OWLDataMinCardinalityRestriction object) {
        m_hasDatatypes=true;
    }

    public void visit(OWLDataMaxCardinalityRestriction object) {
        m_hasDatatypes=true;
    }

    public void visit(OWLDataExactCardinalityRestriction object) {
        m_hasDatatypes=true;
    }

     public void visit(OWLClassAssertionAxiom object) {
        object.getDescription().accept(OWLAxiomsExpressivity.this);
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
