// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.structural;

import java.util.Set;
import java.util.HashSet;

import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLClassAssertionAxiom;
import org.semanticweb.owl.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owl.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLObjectPropertyInverse;
import org.semanticweb.owl.model.OWLObjectPropertyExpression;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataPropertyExpression;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLDataAllRestriction;
import org.semanticweb.owl.model.OWLDataExactCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataMaxCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataMinCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataSomeRestriction;
import org.semanticweb.owl.model.OWLDataValueRestriction;
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
import org.semanticweb.owl.model.OWLSameIndividualsAxiom;
import org.semanticweb.owl.util.OWLAxiomVisitorAdapter;

public class OWLAxiomsExpressivity implements OWLDescriptionVisitor {
    public final Set<OWLClass> m_classes;
    public final Set<OWLObjectProperty> m_objectProperties;
    public final Set<OWLDataProperty> m_dataProperties;
    public final Set<OWLIndividual> m_individuals;
    public boolean m_hasAtMostRestrictions;
    public boolean m_hasInverseRoles;
    public boolean m_hasNominals;
    public boolean m_hasDatatypes;

    public OWLAxiomsExpressivity(OWLAxioms axioms) {
        m_classes=new HashSet<OWLClass>();
        m_objectProperties=new HashSet<OWLObjectProperty>();
        m_dataProperties=new HashSet<OWLDataProperty>();
        m_individuals=new HashSet<OWLIndividual>();
        for (OWLDescription[] inclusion : axioms.m_conceptInclusions)
            for (OWLDescription description : inclusion)
                description.accept(this);
        for (OWLObjectPropertyExpression[] inclusion : axioms.m_objectPropertyInclusions) {
            visitProperty(inclusion[0]);
            visitProperty(inclusion[1]);
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
        for (OWLDataPropertyExpression[] inclusion : axioms.m_dataPropertyInclusions) {
            visitProperty(inclusion[0]);
            visitProperty(inclusion[1]);
        }
        for (OWLDataPropertyExpression[] disjoint : axioms.m_disjointDataProperties)
            for (int index=0;index<disjoint.length;index++)
                visitProperty(disjoint[index]);
        if (axioms.m_dataPropertyInclusions.size()>0 || axioms.m_disjointDataProperties.size()>0)
            m_hasDatatypes=true;
        FactVisitor factVisitor=new FactVisitor();
        for (OWLIndividualAxiom fact : axioms.m_facts)
            fact.accept(factVisitor);
    }
    
    protected void visitProperty(OWLObjectPropertyExpression object) {
        if (object instanceof OWLObjectPropertyInverse)
            m_hasInverseRoles=true;
        m_objectProperties.add(object.getNamedProperty());
    }

    protected void visitProperty(OWLDataPropertyExpression object) {
        m_dataProperties.add(object.asOWLDataProperty());
    }

    protected void visitIndividual(OWLIndividual object) {
        m_individuals.add(object);
    }

    public void visit(OWLClass object) {
        m_classes.add(object);
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
        for (OWLIndividual individual : object.getIndividuals())
            visitIndividual(individual);
    }

    public void visit(OWLObjectSomeRestriction object) {
        visitProperty(object.getProperty());
        object.getFiller().accept(this);
    }

    public void visit(OWLObjectValueRestriction object) {
        m_hasNominals=true;
        visitProperty(object.getProperty());
        visitIndividual(object.getValue());
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
        visitProperty(object.getProperty());
    }

    public void visit(OWLDataSomeRestriction object) {
        m_hasDatatypes=true;
        visitProperty(object.getProperty());
    }

    public void visit(OWLDataAllRestriction object) {
        m_hasDatatypes=true;
        visitProperty(object.getProperty());
    }

    public void visit(OWLDataMinCardinalityRestriction object) {
        m_hasDatatypes=true;
        visitProperty(object.getProperty());
    }

    public void visit(OWLDataMaxCardinalityRestriction object) {
        m_hasDatatypes=true;
        visitProperty(object.getProperty());
    }

    public void visit(OWLDataExactCardinalityRestriction object) {
        m_hasDatatypes=true;
        visitProperty(object.getProperty());
    }

    protected class FactVisitor extends OWLAxiomVisitorAdapter {

        public void visit(OWLSameIndividualsAxiom object) {
            for (OWLIndividual individual : object.getIndividuals())
                visitIndividual(individual);
        }

        public void visit(OWLDifferentIndividualsAxiom object) {
            for (OWLIndividual individual : object.getIndividuals())
                visitIndividual(individual);
        }

        public void visit(OWLClassAssertionAxiom object) {
            object.getDescription().accept(OWLAxiomsExpressivity.this);
            visitIndividual(object.getIndividual());
        }

        public void visit(OWLObjectPropertyAssertionAxiom object) {
            visitProperty(object.getProperty());
            visitIndividual(object.getSubject());
            visitIndividual(object.getObject());
        }

        public void visit(OWLNegativeObjectPropertyAssertionAxiom object) {
            visitProperty(object.getProperty());
            visitIndividual(object.getSubject());
            visitIndividual(object.getObject());
        }

        public void visit(OWLDataPropertyAssertionAxiom object) {
            visitProperty(object.getProperty());
            visitIndividual(object.getSubject());
        }

        public void visit(OWLNegativeDataPropertyAssertionAxiom object) {
            visitProperty(object.getProperty());
            visitIndividual(object.getSubject());
        }
    }
}