package org.semanticweb.HermiT;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.owl.inference.OWLReasonerException;
import org.semanticweb.owl.model.IRI;
import org.semanticweb.owl.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owl.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owl.model.OWLAnnotationPropertyRangeAxiom;
import org.semanticweb.owl.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLAxiomVisitorEx;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLClassAssertionAxiom;
import org.semanticweb.owl.model.OWLClassExpression;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDataMaxCardinality;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owl.model.OWLDataPropertyExpression;
import org.semanticweb.owl.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owl.model.OWLDataSomeValuesFrom;
import org.semanticweb.owl.model.OWLDatatypeDefinitionAxiom;
import org.semanticweb.owl.model.OWLDeclarationAxiom;
import org.semanticweb.owl.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owl.model.OWLDisjointClassesAxiom;
import org.semanticweb.owl.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owl.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owl.model.OWLDisjointUnionAxiom;
import org.semanticweb.owl.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owl.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owl.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owl.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owl.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLHasKeyAxiom;
import org.semanticweb.owl.model.OWLImportsDeclaration;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owl.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLObjectMaxCardinality;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owl.model.OWLObjectPropertyExpression;
import org.semanticweb.owl.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owl.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owl.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLSameIndividualAxiom;
import org.semanticweb.owl.model.OWLSubAnnotationPropertyOfAxiom;
import org.semanticweb.owl.model.OWLSubClassOfAxiom;
import org.semanticweb.owl.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owl.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owl.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owl.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owl.model.SWRLRule;

public class EntailmentChecker implements OWLAxiomVisitorEx<Boolean> {
    private final OWLDataFactory factory;
    private final Reasoner reasoner;

    public EntailmentChecker(Reasoner reasoner,OWLDataFactory factory) {
        this.reasoner=reasoner;
        this.factory=factory;
    }

    public boolean isEntailed(OWLAxiom axiom) throws OWLReasonerException {
        return axiom.accept(this);
    }

    public Boolean visit(OWLAsymmetricObjectPropertyAxiom axiom) {
        return reasoner.isAsymmetric((OWLObjectProperty)axiom.getProperty());
    }
    public Boolean visit(OWLAnnotationAssertionAxiom axiom) {
        return Boolean.TRUE;
    }
    public Boolean visit(OWLSubAnnotationPropertyOfAxiom axiom) {
        return Boolean.TRUE;
    }
    public Boolean visit(OWLAnnotationPropertyDomainAxiom axiom) {
        return Boolean.TRUE;
    }
    public Boolean visit(OWLAnnotationPropertyRangeAxiom axiom) {
        return Boolean.TRUE;
    }
    public Boolean visit(OWLClassAssertionAxiom axiom) {
        OWLIndividual ind=axiom.getIndividual();
        OWLClassExpression c=axiom.getClassExpression();
        if (ind.isAnonymous())
            return reasoner.isSatisfiable(c);
        else
            return reasoner.hasType(ind,c,false);
    }

    public Boolean visit(OWLDataPropertyAssertionAxiom axiom) {
        return reasoner.hasDataPropertyRelationship(axiom.getSubject(),axiom.getProperty(),axiom.getObject());
    }

    public Boolean visit(OWLDataPropertyDomainAxiom axiom) {
        return reasoner.isSubClassOf(factory.getOWLDataSomeValuesFrom(axiom.getProperty(),factory.getTopDatatype()),axiom.getDomain());
    }

    public Boolean visit(OWLDataPropertyRangeAxiom axiom) {
        return reasoner.isSubClassOf(factory.getOWLThing(),factory.getOWLDataAllValuesFrom(axiom.getProperty(),axiom.getRange()));
    }

    public Boolean visit(OWLSubDataPropertyOfAxiom axiom) {
        OWLDataPropertyExpression sub=axiom.getSubProperty();
        OWLDataPropertyExpression sup=axiom.getSuperProperty();
        return isSubDataProperty(sub,sup);
    }

    protected boolean isSubDataProperty(OWLDataPropertyExpression sub,OWLDataPropertyExpression sup) {
        Set<Set<OWLDataProperty>> ancestors=reasoner.getAncestorProperties(sub.asOWLDataProperty());
        for (Set<OWLDataProperty> ancestorSet : ancestors)
            if (ancestorSet.contains(sup))
                return true;
        return false;
    }

    public Boolean visit(OWLDeclarationAxiom axiom) {
        return Boolean.TRUE;
    }

    public Boolean visit(OWLDifferentIndividualsAxiom axiom) {
        ArrayList<OWLIndividual> list=new ArrayList<OWLIndividual>(axiom.getIndividuals());
        for (int i=0;i<list.size()-1;i++) {
            OWLIndividual head=list.get(i);
            for (int j=i+1;j<list.size();j++) {
                OWLIndividual next=list.get(j);
                if (!reasoner.hasType(head,factory.getOWLObjectComplementOf(factory.getOWLObjectOneOf(next)),false))
                    return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }

    public Boolean visit(OWLDisjointClassesAxiom axiom) {
        int n=axiom.getClassExpressions().size();
        OWLClassExpression[] classes=axiom.getClassExpressions().toArray(new OWLClassExpression[n]);
        for (int i=0;i<n-1;i++) {
            for (int j=i+1;j<n;j++) {
                OWLClassExpression notj=factory.getOWLObjectComplementOf(classes[j]);
                if (!reasoner.isSubClassOf(classes[i],notj))
                    return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }

    public Boolean visit(OWLDisjointDataPropertiesAxiom axiom) {
        int n=axiom.getProperties().size();
        OWLDataPropertyExpression[] props=axiom.getProperties().toArray(new OWLDataPropertyExpression[n]);
        for (int i=0;i<n-1;i++) {
            for (int j=i+1;j<n;j++) {
                OWLDataSomeValuesFrom some_i=factory.getOWLDataSomeValuesFrom(props[i],factory.getOWLDatatype(IRI.create(AtomicConcept.RDFS_LITERAL.getIRI())));
                OWLDataSomeValuesFrom some_j=factory.getOWLDataSomeValuesFrom(props[j],factory.getOWLDatatype(IRI.create(AtomicConcept.RDFS_LITERAL.getIRI())));
                OWLDataMaxCardinality max1=factory.getOWLDataMaxCardinality(factory.getOWLDataProperty(IRI.create(AtomicRole.TOP_DATA_ROLE.getIRI())),1);
                OWLClassExpression desc=factory.getOWLObjectIntersectionOf(some_i,some_j,max1);
                if (reasoner.isSatisfiable(desc))
                    return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }

    public Boolean visit(OWLDisjointObjectPropertiesAxiom axiom) {
        int n=axiom.getProperties().size();
        OWLObjectPropertyExpression[] props=axiom.getProperties().toArray(new OWLObjectPropertyExpression[n]);
        for (int i=0;i<n-1;i++) {
            for (int j=i+1;j<n;j++) {
                OWLObjectSomeValuesFrom some_i=factory.getOWLObjectSomeValuesFrom(props[i],factory.getOWLThing());
                OWLObjectSomeValuesFrom some_j=factory.getOWLObjectSomeValuesFrom(props[j],factory.getOWLThing());
                OWLObjectMaxCardinality max1=factory.getOWLObjectMaxCardinality(factory.getOWLObjectProperty(IRI.create(AtomicRole.TOP_OBJECT_ROLE.getIRI())),1);
                OWLClassExpression desc=factory.getOWLObjectIntersectionOf(some_i,some_j,max1);
                if (reasoner.isSatisfiable(desc))
                    return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }

    public Boolean visit(OWLDisjointUnionAxiom axiom) {
        // C = C1 or ... or Cn, for 1 <= i < j <= n: Ci and Cj -> bottom
        OWLClass c=axiom.getOWLClass();
        Set<OWLClassExpression> cs=new HashSet<OWLClassExpression>(axiom.getClassExpressions());
        cs.add(factory.getOWLObjectComplementOf(c));
        OWLClassExpression incl1=factory.getOWLObjectUnionOf(cs);
        OWLClassExpression incl2=factory.getOWLObjectUnionOf(factory.getOWLObjectComplementOf(factory.getOWLObjectUnionOf(axiom.getClassExpressions())),c);
        // incl1: not C or C1 or ... or Cn
        // incl2: not(C1 or ... or Cn) or C
        Set<OWLClassExpression> conjuncts=new HashSet<OWLClassExpression>();
        conjuncts.add(incl1);
        conjuncts.add(incl2);
        int n=axiom.getClassExpressions().size();
        OWLClassExpression[] descs=axiom.getClassExpressions().toArray(new OWLClassExpression[n]);
        for (int i=0;i<n-1;i++) {
            for (int j=i+1;j<n;j++) {
                conjuncts.add(factory.getOWLObjectUnionOf(factory.getOWLObjectComplementOf(descs[i]),factory.getOWLObjectComplementOf(descs[j])));
            }
        }
        OWLClassExpression entailmentDesc=factory.getOWLObjectIntersectionOf(conjuncts);
        return !reasoner.isSatisfiable(factory.getOWLObjectComplementOf(entailmentDesc));
    }

    public Boolean visit(OWLEquivalentClassesAxiom axiom) {
        boolean isEntailed=true;
        Iterator<OWLClassExpression> i=axiom.getClassExpressions().iterator();
        if (i.hasNext()) {
            OWLClassExpression first=i.next();
            while (i.hasNext() && isEntailed) {
                OWLClassExpression next=i.next();
                isEntailed=reasoner.isEquivalentClass(first,next);
            }
        }
        return isEntailed;
    }

    public Boolean visit(OWLEquivalentDataPropertiesAxiom axiom) {
        Set<OWLDataPropertyExpression> props=axiom.getProperties();
        Iterator<OWLDataPropertyExpression> it=props.iterator();
        if (it.hasNext()) {
            OWLDataPropertyExpression prop1=it.next();
            while (it.hasNext()) {
                OWLDataPropertyExpression prop2=it.next();
                if (!reasoner.getEquivalentProperties(prop1.asOWLDataProperty()).contains(prop2.asOWLDataProperty()))
                    return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }

    public Boolean visit(OWLEquivalentObjectPropertiesAxiom axiom) {
        Set<OWLObjectPropertyExpression> props=axiom.getProperties();
        Iterator<OWLObjectPropertyExpression> it=props.iterator();
        if (it.hasNext()) {
            OWLObjectPropertyExpression prop1=it.next();
            while (it.hasNext()) {
                OWLObjectPropertyExpression prop2=it.next();
                if (!reasoner.getEquivalentProperties(prop1).contains(prop2))
                    return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }

    public Boolean visit(OWLFunctionalDataPropertyAxiom axiom) {
        return reasoner.isFunctional(axiom.getProperty().asOWLDataProperty());
    }

    public Boolean visit(OWLFunctionalObjectPropertyAxiom axiom) {
        return reasoner.isFunctional(axiom.getProperty().asOWLObjectProperty());
    }

    public Boolean visit(OWLImportsDeclaration axiom) {
        return Boolean.TRUE;
    }

    public Boolean visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
        return reasoner.isInverseFunctional(axiom.getProperty().asOWLObjectProperty());
    }

    public Boolean visit(OWLInverseObjectPropertiesAxiom axiom) {
        OWLObjectPropertyExpression prop1=axiom.getFirstProperty();
        OWLObjectPropertyExpression prop2=axiom.getSecondProperty();
        return reasoner.getInverseProperties(prop1).contains(prop2);
    }

    public Boolean visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
        return reasoner.isIrreflexive((OWLObjectProperty)axiom.getProperty());
    }

    public Boolean visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
        OWLClassExpression hasValue=factory.getOWLDataHasValue(axiom.getProperty(),axiom.getObject());
        OWLClassExpression doesNotHaveValue=factory.getOWLObjectComplementOf(hasValue);
        return reasoner.hasType(axiom.getSubject(),doesNotHaveValue,false);
    }

    public Boolean visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
        OWLClassExpression hasValue=factory.getOWLObjectHasValue(axiom.getProperty(),axiom.getObject());
        OWLClassExpression doesNotHaveValue=factory.getOWLObjectComplementOf(hasValue);
        return reasoner.hasType(axiom.getSubject(),doesNotHaveValue,false);
    }

    public Boolean visit(OWLObjectPropertyAssertionAxiom axiom) {
        return reasoner.hasObjectPropertyRelationship(axiom.getSubject(),axiom.getProperty(),axiom.getObject());
    }

    public Boolean visit(OWLSubPropertyChainOfAxiom axiom) {
        throw new UnsupportedOperationException();
    }

    public Boolean visit(OWLObjectPropertyDomainAxiom axiom) {
        return reasoner.isSubClassOf(factory.getOWLObjectSomeValuesFrom(axiom.getProperty(),factory.getOWLThing()),axiom.getDomain());
    }

    public Boolean visit(OWLObjectPropertyRangeAxiom axiom) {
        return reasoner.isSubClassOf(factory.getOWLThing(),factory.getOWLObjectAllValuesFrom(axiom.getProperty(),axiom.getRange()));
    }

    protected boolean isSubObjectProperty(OWLObjectPropertyExpression sub,OWLObjectPropertyExpression sup) {
        Set<Set<OWLObjectPropertyExpression>> ancestors=reasoner.getAncestorProperties(sub);
        for (Set<OWLObjectPropertyExpression> ancestorSet : ancestors)
            if (ancestorSet.contains(sup))
                return true;
        return false;
    }

    public Boolean visit(OWLSubObjectPropertyOfAxiom axiom) {
        OWLObjectPropertyExpression sub=axiom.getSubProperty();
        OWLObjectPropertyExpression sup=axiom.getSuperProperty();
        return isSubObjectProperty(sub,sup);
    }
    public Boolean visit(OWLReflexiveObjectPropertyAxiom axiom) {
        return reasoner.isReflexive((OWLObjectProperty)axiom.getProperty());
    }

    public Boolean visit(OWLSameIndividualAxiom axiom) {
        Iterator<OWLIndividual> i=axiom.getIndividuals().iterator();
        if (i.hasNext()) {
            OWLIndividual first=i.next();
            while (i.hasNext()) {
                OWLIndividual next=i.next();
                if (!reasoner.hasType(first,factory.getOWLObjectOneOf(next),false))
                    return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }
    public Boolean visit(OWLSubClassOfAxiom axiom) {
        return reasoner.isSubClassOf(axiom.getSubClass(),axiom.getSuperClass());
    }
    public Boolean visit(OWLSymmetricObjectPropertyAxiom axiom) {
        return reasoner.isSymmetric((OWLObjectProperty)axiom.getProperty());
    }
    public Boolean visit(OWLTransitiveObjectPropertyAxiom axiom) {
        return reasoner.isTransitive((OWLObjectProperty)axiom.getProperty());
    }
    public Boolean visit(SWRLRule rule) {
        throw new UnsupportedOperationException();
    }


    public Boolean visit(OWLHasKeyAxiom axiom) {
        // TODO Auto-generated method stub
        return null;
    }
    public Boolean visit(OWLDatatypeDefinitionAxiom axiom) {
        // TODO Auto-generated method stub
        return null;
    }    
}
