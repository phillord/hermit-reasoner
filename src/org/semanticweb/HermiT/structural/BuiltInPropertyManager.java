// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.structural;

import java.net.URI;

import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLClassAssertionAxiom;
import org.semanticweb.owl.model.OWLDataAllRestriction;
import org.semanticweb.owl.model.OWLDataExactCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDataMaxCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataMinCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLDataPropertyExpression;
import org.semanticweb.owl.model.OWLDataSomeRestriction;
import org.semanticweb.owl.model.OWLDataValueRestriction;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLDescriptionVisitor;
import org.semanticweb.owl.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLIndividualAxiom;
import org.semanticweb.owl.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLObjectAllRestriction;
import org.semanticweb.owl.model.OWLObjectComplementOf;
import org.semanticweb.owl.model.OWLObjectExactCardinalityRestriction;
import org.semanticweb.owl.model.OWLObjectIntersectionOf;
import org.semanticweb.owl.model.OWLObjectMaxCardinalityRestriction;
import org.semanticweb.owl.model.OWLObjectMinCardinalityRestriction;
import org.semanticweb.owl.model.OWLObjectOneOf;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLObjectPropertyExpression;
import org.semanticweb.owl.model.OWLObjectSelfRestriction;
import org.semanticweb.owl.model.OWLObjectSomeRestriction;
import org.semanticweb.owl.model.OWLObjectUnionOf;
import org.semanticweb.owl.model.OWLObjectValueRestriction;
import org.semanticweb.owl.model.OWLSameIndividualsAxiom;
import org.semanticweb.owl.util.OWLAxiomVisitorAdapter;

public class BuiltInPropertyManager {
    protected final OWLDataFactory m_factory;
    protected final OWLObjectProperty m_topObjectProperty;
    protected final OWLObjectProperty m_bottomObjectProperty;
    protected final OWLDataProperty m_topDataProperty;
    protected final OWLDataProperty m_bottomDataProperty;

    public BuiltInPropertyManager(OWLDataFactory factory) {
        m_factory=factory;
        m_topObjectProperty=m_factory.getOWLObjectProperty(URI.create(AtomicRole.TOP_OBJECT_ROLE.getURI()));
        m_bottomObjectProperty=m_factory.getOWLObjectProperty(URI.create(AtomicRole.BOTTOM_OBJECT_ROLE.getURI()));
        m_topDataProperty=m_factory.getOWLDataProperty(URI.create(AtomicRole.TOP_DATA_ROLE.getURI()));
        m_bottomDataProperty=m_factory.getOWLDataProperty(URI.create(AtomicRole.BOTTOM_DATA_ROLE.getURI()));
    }
    public void axiomatizeBuiltInPropertiesAsNeeded(OWLAxioms axioms,boolean skipTopObjectProperty,boolean skipBottomObjectProperty,boolean skipTopDataProperty,boolean skipBottomDataProperty) {
        Checker checker=new Checker(axioms);
        if (checker.m_usesTopObjectProperty && !skipTopObjectProperty)
            axiomatizeTopObjectProperty(axioms);
        if (checker.m_usesBottomObjectProperty && !skipBottomObjectProperty)
            axiomatizeBottomObjectProperty(axioms);
        if (checker.m_usesTopDataProperty && !skipTopDataProperty)
            axiomatizeTopDataProperty(axioms);
        if (checker.m_usesBottomDataProperty && !skipBottomDataProperty)
            axiomatizeBottomDataProperty(axioms);
    }
    public void axiomatizeBuiltInPropertiesAsNeeded(OWLAxioms axioms) {
        axiomatizeBuiltInPropertiesAsNeeded(axioms,false,false,false,false);
    }
    protected void axiomatizeTopObjectProperty(OWLAxioms axioms) {
        OWLObjectProperty topObjectProperty=m_factory.getOWLObjectProperty(URI.create(AtomicRole.TOP_OBJECT_ROLE.getURI()));
        axioms.m_complexObjectPropertyInclusions.add(new OWLAxioms.ComplexObjectPropertyInclusion(topObjectProperty));
        axioms.m_simpleObjectPropertyInclusions.add(new OWLObjectPropertyExpression[] { topObjectProperty,topObjectProperty.getInverseProperty() });
        OWLIndividual newIndividual=m_factory.getOWLIndividual(URI.create("internal:nam#topIndividual"));
        OWLObjectOneOf oneOfNewIndividual=m_factory.getOWLObjectOneOf(newIndividual);
        OWLObjectSomeRestriction hasTopNewIndividual=m_factory.getOWLObjectSomeRestriction(topObjectProperty,oneOfNewIndividual);
        axioms.m_conceptInclusions.add(new OWLDescription[] { hasTopNewIndividual });
    }
    protected void axiomatizeBottomObjectProperty(OWLAxioms axioms) {
        axioms.m_unsatisfiableObjectProperties.add(m_bottomObjectProperty);
    }
    protected void axiomatizeTopDataProperty(OWLAxioms axioms) {
        throw new IllegalArgumentException("The axioms use owl:topDataProperty in an inappropriate way.");
    }
    protected void axiomatizeBottomDataProperty(OWLAxioms axioms) {
        axioms.m_unsatisfiableDataProperties.add(m_bottomDataProperty);
    }
    
    protected class Checker implements OWLDescriptionVisitor {
        public boolean m_usesTopObjectProperty;
        public boolean m_usesBottomObjectProperty;
        public boolean m_usesTopDataProperty;
        public boolean m_usesBottomDataProperty;

        public Checker(OWLAxioms axioms) {
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
            for (OWLDataPropertyExpression[] inclusion : axioms.m_dataPropertyInclusions) {
                visitProperty(inclusion[0]);
                visitProperty(inclusion[1]);
            }
            for (OWLDataPropertyExpression[] disjoint : axioms.m_disjointDataProperties)
                for (int index=0;index<disjoint.length;index++)
                    visitProperty(disjoint[index]);
            FactVisitor factVisitor=new FactVisitor();
            for (OWLIndividualAxiom fact : axioms.m_facts)
                fact.accept(factVisitor);
        }
        protected void visitProperty(OWLObjectPropertyExpression object) {
            if (object.getNamedProperty().equals(m_topObjectProperty))
                m_usesTopObjectProperty=true;
            else if (object.getNamedProperty().equals(m_bottomObjectProperty))
                m_usesBottomObjectProperty=true;
        }

        protected void visitProperty(OWLDataPropertyExpression object) {
            if (object.asOWLDataProperty().equals(m_topDataProperty))
                m_usesTopDataProperty=true;
            else if (object.asOWLDataProperty().equals(m_bottomDataProperty))
                m_usesBottomDataProperty=true;
        }

        public void visit(OWLClass object) {
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
        }

        public void visit(OWLObjectSomeRestriction object) {
            visitProperty(object.getProperty());
            object.getFiller().accept(this);
        }

        public void visit(OWLObjectValueRestriction object) {
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
            visitProperty(object.getProperty());
            object.getFiller().accept(this);
        }

        public void visit(OWLObjectExactCardinalityRestriction object) {
            visitProperty(object.getProperty());
            object.getFiller().accept(this);
        }

        public void visit(OWLDataValueRestriction object) {
            visitProperty(object.getProperty());
        }

        public void visit(OWLDataSomeRestriction object) {
            visitProperty(object.getProperty());
        }

        public void visit(OWLDataAllRestriction object) {
            visitProperty(object.getProperty());
        }

        public void visit(OWLDataMinCardinalityRestriction object) {
            visitProperty(object.getProperty());
        }

        public void visit(OWLDataMaxCardinalityRestriction object) {
            visitProperty(object.getProperty());
        }

        public void visit(OWLDataExactCardinalityRestriction object) {
            visitProperty(object.getProperty());
        }
    
        protected class FactVisitor extends OWLAxiomVisitorAdapter {
    
            public void visit(OWLSameIndividualsAxiom object) {
            }
    
            public void visit(OWLDifferentIndividualsAxiom object) {
            }
    
            public void visit(OWLClassAssertionAxiom object) {
                object.getDescription().accept(Checker.this);
            }
    
            public void visit(OWLObjectPropertyAssertionAxiom object) {
                visitProperty(object.getProperty());
            }
    
            public void visit(OWLNegativeObjectPropertyAssertionAxiom object) {
                visitProperty(object.getProperty());
            }
    
            public void visit(OWLDataPropertyAssertionAxiom object) {
                visitProperty(object.getProperty());
            }
    
            public void visit(OWLNegativeDataPropertyAssertionAxiom object) {
                visitProperty(object.getProperty());
            }
        }
    }
}
