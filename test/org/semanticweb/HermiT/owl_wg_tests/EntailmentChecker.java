package org.semanticweb.HermiT.owl_wg_tests;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.owl.inference.OWLReasonerException;
import org.semanticweb.owl.model.OWLAntiSymmetricObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLAxiomAnnotationAxiom;
import org.semanticweb.owl.model.OWLAxiomVisitor;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLClassAssertionAxiom;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDataMaxCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owl.model.OWLDataPropertyExpression;
import org.semanticweb.owl.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owl.model.OWLDataSomeRestriction;
import org.semanticweb.owl.model.OWLDataSubPropertyAxiom;
import org.semanticweb.owl.model.OWLDeclarationAxiom;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owl.model.OWLDisjointClassesAxiom;
import org.semanticweb.owl.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owl.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owl.model.OWLDisjointUnionAxiom;
import org.semanticweb.owl.model.OWLEntityAnnotationAxiom;
import org.semanticweb.owl.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owl.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owl.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owl.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owl.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLImportsDeclaration;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owl.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLObjectMaxCardinalityRestriction;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLObjectPropertyChainSubPropertyAxiom;
import org.semanticweb.owl.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owl.model.OWLObjectPropertyExpression;
import org.semanticweb.owl.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owl.model.OWLObjectSomeRestriction;
import org.semanticweb.owl.model.OWLObjectSubPropertyAxiom;
import org.semanticweb.owl.model.OWLOntologyAnnotationAxiom;
import org.semanticweb.owl.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLSameIndividualsAxiom;
import org.semanticweb.owl.model.OWLSubClassAxiom;
import org.semanticweb.owl.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owl.model.SWRLRule;

public class EntailmentChecker implements OWLAxiomVisitor {
    public static Logger log = Logger.getLogger(EntailmentChecker.class.getName());

    private OWLReasonerException exception = null;
    private OWLDataFactory factory;
    private boolean isEntailed = false;
    private Reasoner reasoner;

    public EntailmentChecker(Reasoner reasoner, OWLDataFactory factory) {
        this.reasoner = reasoner;
        this.factory = factory;
    }

    public boolean isEntailed(OWLAxiom axiom) throws OWLReasonerException {
        isEntailed = false;
        exception = null;

        axiom.accept(this);

        if (exception != null)
            throw exception;

        return isEntailed;
    }

    public void visit(OWLAntiSymmetricObjectPropertyAxiom axiom) {
        isEntailed = reasoner.isAsymmetric((OWLObjectProperty) axiom.getProperty());
    }

    public void visit(OWLAxiomAnnotationAxiom axiom) {
        isEntailed = true;
        if (log.isLoggable(Level.FINE))
            log.fine("Ignoring axiom annotation " + axiom);
    }

    public void visit(OWLClassAssertionAxiom axiom) {
        OWLIndividual ind = axiom.getIndividual();
        OWLDescription c = axiom.getDescription();
        if (ind.isAnonymous()) {
            isEntailed = reasoner.isSatisfiable(c);
        } else{
            isEntailed = reasoner.hasType(ind, c, false);
        }
    }

    public void visit(OWLDataPropertyAssertionAxiom axiom) {
        isEntailed = reasoner.hasDataPropertyRelationship(
                axiom.getSubject(), axiom.getProperty(), axiom.getObject());
    }

    public void visit(OWLDataPropertyDomainAxiom axiom) {
        isEntailed = reasoner.isSubClassOf(
                factory.getOWLDataSomeRestriction(axiom.getProperty(), factory.getTopDataType()), 
                axiom.getDomain()
        );
    }

    public void visit(OWLDataPropertyRangeAxiom axiom) {
        isEntailed = reasoner.isSubClassOf(
                factory.getOWLThing(), 
                factory.getOWLDataAllRestriction(axiom.getProperty(), axiom.getRange())
        );
    }

    public void visit(OWLDataSubPropertyAxiom axiom) {
        OWLDataPropertyExpression sub = axiom.getSubProperty();
        OWLDataPropertyExpression sup = axiom.getSuperProperty();
        isEntailed = isSubDataProperty(sub, sup);
    }
    
    protected boolean isSubDataProperty(OWLDataPropertyExpression sub, OWLDataPropertyExpression sup) {
        Set<Set<OWLDataProperty>>supers = reasoner.getSuperProperties(sub.asOWLDataProperty()); 
        return supers.contains(sup);
    }

    protected boolean isEquivalentDataProperty(OWLDataPropertyExpression dp1, OWLDataPropertyExpression dp2) {
        return isSubDataProperty(dp1, dp2) && isSubDataProperty(dp2, dp1);
    }
    
    public void visit(OWLDeclarationAxiom axiom) {
        isEntailed = true;
        if (log.isLoggable(Level.FINE))
            log.fine("Ignoring declaration " + axiom);
    }

    public void visit(OWLDifferentIndividualsAxiom axiom) {
        isEntailed = true;
        ArrayList<OWLIndividual> list = new ArrayList<OWLIndividual>(axiom.getIndividuals());
        for (int i = 0; i < list.size() - 1; i++) {
            OWLIndividual head = list.get(i);
            for (int j = i + 1; j < list.size(); j++) {
                OWLIndividual next = list.get(j);
                if (!reasoner.hasType(head, factory.getOWLObjectComplementOf(factory.getOWLObjectOneOf(next)), false)) {
                    isEntailed = false;
                    return;
                }
            }
        }
    }

    public void visit(OWLDisjointClassesAxiom axiom) {
        isEntailed = true;
        int n = axiom.getDescriptions().size();
        OWLDescription[] classes = axiom.getDescriptions().toArray(new OWLDescription[n]);
        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                OWLDescription notj = factory.getOWLObjectComplementOf(classes[j]);
                if (!reasoner.isSubClassOf(classes[i], notj)) {
                    isEntailed = false;
                    return;
                }
            }
        }
    }

    public void visit(OWLDisjointDataPropertiesAxiom axiom) {
        isEntailed = true;
        int n = axiom.getProperties().size();
        OWLDataPropertyExpression[] props = axiom.getProperties().toArray(new OWLDataPropertyExpression[n]);
        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                OWLDataSomeRestriction some_i =  factory.getOWLDataSomeRestriction(props[i], factory.getOWLDataType(URI.create(AtomicConcept.RDFS_LITERAL.getURI())));
                OWLDataSomeRestriction some_j =  factory.getOWLDataSomeRestriction(props[j], factory.getOWLDataType(URI.create(AtomicConcept.RDFS_LITERAL.getURI())));
                OWLDataMaxCardinalityRestriction max1 = factory.getOWLDataMaxCardinalityRestriction(factory.getOWLDataProperty(URI.create(AtomicRole.TOP_DATA_ROLE.getURI())), 1);
                OWLDescription desc = factory.getOWLObjectIntersectionOf(some_i, some_j, max1);
                if (reasoner.isSatisfiable(desc)) {
                    isEntailed = false;
                    return;
                }
            }
        }
    }

    public void visit(OWLDisjointObjectPropertiesAxiom axiom) {
        isEntailed = true;
        int n = axiom.getProperties().size();
        OWLObjectPropertyExpression[] props = axiom.getProperties().toArray(new OWLObjectPropertyExpression[n]);
        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                OWLObjectSomeRestriction some_i =  factory.getOWLObjectSomeRestriction(props[i], factory.getOWLThing());
                OWLObjectSomeRestriction some_j =  factory.getOWLObjectSomeRestriction(props[j], factory.getOWLThing());
                OWLObjectMaxCardinalityRestriction max1 = factory.getOWLObjectMaxCardinalityRestriction(factory.getOWLObjectProperty(URI.create(AtomicRole.TOP_OBJECT_ROLE.getURI())), 1);
                OWLDescription desc = factory.getOWLObjectIntersectionOf(some_i, some_j, max1);
                if (reasoner.isSatisfiable(desc)) {
                    isEntailed = false;
                    return;
                }
            }
        }
    }

    public void visit(OWLDisjointUnionAxiom axiom) {
        // C = C1 or ... or Cn, for 1 <= i < j <= n: Ci and Cj -> bottom
        OWLClass c = axiom.getOWLClass();
        Set<OWLDescription> cs = new HashSet<OWLDescription>(axiom.getDescriptions());
        cs.add(factory.getOWLObjectComplementOf(c));
        OWLDescription incl1 = factory.getOWLObjectUnionOf(cs);        
        OWLDescription incl2 = factory.getOWLObjectUnionOf(factory.getOWLObjectComplementOf(factory.getOWLObjectUnionOf(axiom.getDescriptions())), c); 
        // incl1: not C or C1 or ... or Cn
        // incl2: not(C1 or ... or Cn) or C 
        Set<OWLDescription> conjuncts = new HashSet<OWLDescription>();
        conjuncts.add(incl1);
        conjuncts.add(incl2);
        int n = axiom.getDescriptions().size();
        OWLDescription[] descs = axiom.getDescriptions().toArray(new OWLDescription[n]);
        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                conjuncts.add(factory.getOWLObjectUnionOf(factory.getOWLObjectComplementOf(descs[i]), factory.getOWLObjectComplementOf(descs[j])));
            }
        }
        OWLDescription entailmentDesc = factory.getOWLObjectIntersectionOf(conjuncts);
        isEntailed = !reasoner.isSatisfiable(factory.getOWLObjectComplementOf(entailmentDesc));
    }

    public void visit(OWLEntityAnnotationAxiom axiom) {
        isEntailed = true;
        if (log.isLoggable(Level.FINE))
            log.fine("Ignoring entity annotation " + axiom);
    }

    public void visit(OWLEquivalentClassesAxiom axiom) {
        isEntailed = true;
        Iterator<OWLDescription> i = axiom.getDescriptions().iterator();
        if (i.hasNext()) {
            OWLDescription first = i.next();
            while (i.hasNext() && isEntailed) {
                OWLDescription next = i.next();
                isEntailed = reasoner.isEquivalentClass(first, next);
            }
        }
    }

    public void visit(OWLEquivalentDataPropertiesAxiom axiom) {
        isEntailed = true;
        Set<OWLDataPropertyExpression> props = axiom.getProperties();
        Iterator<OWLDataPropertyExpression> it = props.iterator();
        if (it.hasNext()) {
            OWLDataPropertyExpression prop1 = it.next();
            while (it.hasNext()) {
                OWLDataPropertyExpression prop2 = it.next();
                if (!isEquivalentDataProperty(prop1,prop2)) {
                    isEntailed = false;
                    return;
                }
            }
        }
    }

    public void visit(OWLEquivalentObjectPropertiesAxiom axiom) {
        isEntailed = true;
        Set<OWLObjectPropertyExpression> props = axiom.getProperties();
        Iterator<OWLObjectPropertyExpression> it = props.iterator();
        if (it.hasNext()) {
            OWLObjectPropertyExpression prop1 = it.next();
            while (it.hasNext()) {
                OWLObjectPropertyExpression prop2 = it.next();
                if (!isEquivalentObjectProperty(prop1,prop2)) {
                    isEntailed = false;
                    return;
                }
            }
        }
    }

    public void visit(OWLFunctionalDataPropertyAxiom axiom) {
        isEntailed = reasoner.isFunctional(axiom.getProperty().asOWLDataProperty());
    }

    public void visit(OWLFunctionalObjectPropertyAxiom axiom) {
        isEntailed = reasoner.isFunctional(axiom.getProperty().asOWLObjectProperty());
    }

    public void visit(OWLImportsDeclaration axiom) {
        isEntailed = true;
        if (log.isLoggable(Level.FINE))
            log.fine("Ignoring imports declaration " + axiom);
    }

    public void visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
        isEntailed = reasoner.isInverseFunctional(axiom.getProperty().asOWLObjectProperty());
    }

    public void visit(OWLInverseObjectPropertiesAxiom axiom) {
        OWLObjectPropertyExpression prop1 = axiom.getFirstProperty();
        OWLObjectPropertyExpression prop2 = axiom.getSecondProperty();
        isEntailed = isEquivalentObjectProperty(prop1.getInverseProperty(), prop2);
    }

    public void visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
        isEntailed = reasoner.isIrreflexive((OWLObjectProperty) axiom.getProperty());
    }

    public void visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
        OWLDescription hasValue = factory.getOWLDataValueRestriction(axiom.getProperty(), axiom.getObject());
        OWLDescription doesNotHaveValue = factory.getOWLObjectComplementOf(hasValue);
        isEntailed = reasoner.hasType(axiom.getSubject(), doesNotHaveValue, false);
    }

    public void visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
        OWLDescription hasValue = factory.getOWLObjectValueRestriction(axiom.getProperty(), axiom.getObject());
        OWLDescription doesNotHaveValue = factory.getOWLObjectComplementOf(hasValue);
        isEntailed = reasoner.hasType(axiom.getSubject(), doesNotHaveValue, false);
    }

    public void visit(OWLObjectPropertyAssertionAxiom axiom) {
        isEntailed = reasoner.hasObjectPropertyRelationship(axiom.getSubject(), axiom.getProperty(), axiom.getObject());
    }

    public void visit(OWLObjectPropertyChainSubPropertyAxiom axiom) {
        throw new UnsupportedOperationException();
    }

    public void visit(OWLObjectPropertyDomainAxiom axiom) {
        isEntailed = reasoner.isSubClassOf(factory.getOWLObjectSomeRestriction(axiom.getProperty(), factory.getOWLThing()), axiom.getDomain());
    }

    public void visit(OWLObjectPropertyRangeAxiom axiom) {
        isEntailed = reasoner.isSubClassOf(factory.getOWLThing(), factory.getOWLObjectAllRestriction(axiom.getProperty(), axiom.getRange()));
    }

    protected boolean isSubObjectProperty(OWLObjectPropertyExpression sub, OWLObjectPropertyExpression sup) {
        Set<Set<OWLObjectPropertyExpression>>supers = reasoner.getSuperProperties(sup); 
        return supers.contains(sup);
    }

    protected boolean isEquivalentObjectProperty(OWLObjectPropertyExpression op1, OWLObjectPropertyExpression op2) {
        return isSubObjectProperty(op1, op2) && isSubObjectProperty(op2, op1);
    }
    
    public void visit(OWLObjectSubPropertyAxiom axiom) {
        OWLObjectPropertyExpression sub = axiom.getSubProperty();
        OWLObjectPropertyExpression sup = axiom.getSuperProperty();
        isEntailed = isSubObjectProperty(sub, sup);
    }

    public void visit(OWLOntologyAnnotationAxiom axiom) {
        isEntailed = true;
        if (log.isLoggable(Level.FINE))
            log.fine("Ignoring ontology annotation " + axiom);
    }

    public void visit(OWLReflexiveObjectPropertyAxiom axiom) {
        isEntailed = reasoner.isReflexive((OWLObjectProperty) axiom.getProperty());
    }

    public void visit(OWLSameIndividualsAxiom axiom) {
        isEntailed = true;
        Iterator<OWLIndividual> i = axiom.getIndividuals().iterator();
        if (i.hasNext()) {
            OWLIndividual first = i.next();
            while (i.hasNext()) {
                OWLIndividual next = i.next();
                if (!reasoner.hasType(first, factory.getOWLObjectOneOf(next), false)) {
                    isEntailed = false;
                    return;
                }
            }
        }
    }

    public void visit(OWLSubClassAxiom axiom) {
        isEntailed = reasoner.isSubClassOf(axiom.getSubClass(), axiom.getSuperClass());
    }

    public void visit(OWLSymmetricObjectPropertyAxiom axiom) {
        isEntailed = reasoner.isSymmetric((OWLObjectProperty) axiom.getProperty());
    }

    public void visit(OWLTransitiveObjectPropertyAxiom axiom) {
        isEntailed = reasoner.isTransitive((OWLObjectProperty) axiom.getProperty());
    }

    public void visit(SWRLRule rule) {
        throw new UnsupportedOperationException();
    }
}
