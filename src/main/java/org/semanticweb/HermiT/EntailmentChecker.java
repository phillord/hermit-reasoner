/* Copyright 2009 by the Oxford University Computing Laboratory

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
package org.semanticweb.HermiT;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.InternalDatatype;
import org.semanticweb.HermiT.tableau.ReasoningTaskDescription;
import org.semanticweb.HermiT.tableau.Tableau;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLAxiomVisitorEx;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataIntersectionOf;
import org.semanticweb.owlapi.model.OWLDataMaxCardinality;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLDataUnionOf;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDatatypeDefinitionAxiom;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLHasKeyAxiom;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.OWLSubAnnotationPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.SWRLRule;

public class EntailmentChecker implements OWLAxiomVisitorEx<Boolean> {
    private final OWLDataFactory factory;
    private final Reasoner reasoner;
    protected Set<OWLAxiom> anonymousIndividualAxioms=new HashSet<OWLAxiom>();

    public EntailmentChecker(Reasoner reasoner,OWLDataFactory factory) {
        this.reasoner=reasoner;
        this.factory=factory;
    }

    /**
     * Checks entailment of a set of axioms (an ontology) against the loaded ontology.
     *
     * @param axioms
     *            the axioms that should be checked for enailment
     * @return true if all axioms follow from the loaded ontology and false otherwise.
     */
    public boolean entails(Set<? extends OWLAxiom> axioms) {
        anonymousIndividualAxioms.clear();
        for (OWLAxiom axiom : axioms) {
            if (axiom.isLogicalAxiom())
                if (!axiom.accept(this))
                    return false;
        }
        return checkAnonymousIndividuals();
    }

    /**
     * Use this method only if you really want to check just one axiom or if the axioms you want to check do not contain blind nodes/anonymous individuals. Otherwise use entails(Set<OWLAxiom> axioms) because only then concepts for the anonymous individuals can be obtained by rolling-up as required.
     *
     * @param axiom
     *            an axiom for which entailment is to be checked
     * @return true if the loaded ontology entails the axiom and false otherwise
     */
    public boolean entails(OWLAxiom axiom) {
        if (!axiom.accept(this))
            return false;
        return checkAnonymousIndividuals();
    }
    /**
     * @return true if there are no individual axioms or if all rolled-up concepts for the anonymous individuals are entailed and false otherwise
     */
    protected boolean checkAnonymousIndividuals() {
        if (anonymousIndividualAxioms.isEmpty())
            return true;
        // go through the axioms and build the rolling-up concepts for them
        AnonymousIndividualForestBuilder anonIndChecker=new AnonymousIndividualForestBuilder();
        anonIndChecker.constructConceptsForAnonymousIndividuals(factory,anonymousIndividualAxioms);
        for (OWLAxiom ax : anonIndChecker.getAnonIndAxioms())
            if (!ax.accept(this))
                return false;
        for (OWLAxiom ax : anonIndChecker.getAnonNoNamedIndAxioms()) {
            Tableau t=reasoner.getTableau(ax);
            if (t.isSatisfiable(true,true,null,null,null,null,null,new ReasoningTaskDescription(false,"Anonymous individual check: "+ax.toString())))
                return false;
        }
        return true;
    }

    // ************ non-logical axioms ****************************

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
    public Boolean visit(OWLDeclarationAxiom axiom) {
        return Boolean.TRUE;
    }
    public Boolean visit(OWLImportsDeclaration axiom) {
        return Boolean.TRUE;
    }

    // ************ assertions ****************************

    public Boolean visit(OWLDifferentIndividualsAxiom axiom) {
        // see OWL 2 Syntax, Sec 11.2
        // No axiom in Ax of the following form contains anonymous individuals:
        // SameIndividual, DifferentIndividuals, NegativeObjectPropertyAssertion, and NegativeDataPropertyAssertion.
        ArrayList<OWLIndividual> list=new ArrayList<OWLIndividual>(axiom.getIndividuals());
        for (OWLIndividual i : list) {
            if (i.isAnonymous()) {
                throw new IllegalArgumentException("OWLDifferentIndividualsAxiom axioms are not allowed to be used "+"with anonymous individuals (see OWL 2 Syntax Sec 11.2) but the axiom "+axiom+" cotains an anonymous individual. ");
            }
        }
        for (int i=0;i<list.size()-1;i++) {
            OWLNamedIndividual head=list.get(i).asOWLNamedIndividual();
            for (int j=i+1;j<list.size();j++) {
                OWLNamedIndividual next=list.get(j).asOWLNamedIndividual();
                if (!reasoner.hasType(head,factory.getOWLObjectComplementOf(factory.getOWLObjectOneOf(next)),false))
                    return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }
    public Boolean visit(OWLSameIndividualAxiom axiom) {
        // see OWL 2 Syntax, Sec 11.2
        // No axiom in Ax of the following form contains anonymous individuals:
        // SameIndividual, DifferentIndividuals, NegativeObjectPropertyAssertion, and NegativeDataPropertyAssertion.
        for (OWLIndividual i : axiom.getIndividuals()) {
            if (i.isAnonymous()) {
                throw new IllegalArgumentException("OWLSameIndividualAxiom axioms are not allowed to be used "+"with anonymous individuals (see OWL 2 Syntax Sec 11.2) but the axiom "+axiom+" cotains an anonymous individual. ");
            }
        }
        Iterator<OWLIndividual> i=axiom.getIndividuals().iterator();
        if (i.hasNext()) {
            OWLNamedIndividual first=i.next().asOWLNamedIndividual();
            while (i.hasNext()) {
                OWLNamedIndividual next=i.next().asOWLNamedIndividual();
                if (!reasoner.isSameIndividual(first, next))
                    return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }
    public Boolean visit(OWLClassAssertionAxiom axiom) {
        OWLIndividual ind=axiom.getIndividual();
        if (ind.isAnonymous()) {
            anonymousIndividualAxioms.add(axiom);
            return true; // will be checked afterwards by rolling-up
        }
        OWLClassExpression c=axiom.getClassExpression();
        return reasoner.hasType(ind.asOWLNamedIndividual(),c,false);
    }
    public Boolean visit(OWLObjectPropertyAssertionAxiom axiom) {
        OWLIndividual sub=axiom.getSubject();
        OWLIndividual obj=axiom.getObject();
        if (sub.isAnonymous()||obj.isAnonymous()) {
            anonymousIndividualAxioms.add(axiom);
            return true; // will be checked afterwards by rolling-up
        }
        return reasoner.hasObjectPropertyRelationship(sub.asOWLNamedIndividual(),axiom.getProperty(),obj.asOWLNamedIndividual());
    }
    public Boolean visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
        // see OWL 2 Syntax, Sec 11.2
        // No axiom in Ax of the following form contains anonymous individuals:
        // SameIndividual, DifferentIndividuals, NegativeObjectPropertyAssertion, and NegativeDataPropertyAssertion.
        if (axiom.getSubject().isAnonymous()||axiom.getObject().isAnonymous()) {
            throw new IllegalArgumentException("NegativeObjectPropertyAssertion axioms are not allowed to be used "+"with anonymous individuals (see OWL 2 Syntax Sec 11.2) but the axiom "+axiom+" cotains an anonymous subject or object. ");
        }
        OWLClassExpression hasValue=factory.getOWLObjectHasValue(axiom.getProperty(),axiom.getObject());
        OWLClassExpression doesNotHaveValue=factory.getOWLObjectComplementOf(hasValue);
        return reasoner.hasType(axiom.getSubject().asOWLNamedIndividual(),doesNotHaveValue,false);
    }
    public Boolean visit(OWLDataPropertyAssertionAxiom axiom) {
        OWLIndividual sub=axiom.getSubject();
        if (sub.isAnonymous()) {
            anonymousIndividualAxioms.add(axiom);
            return true; // will be checked afterwards by rolling-up
        }
        OWLClassExpression hasValue=factory.getOWLDataHasValue(axiom.getProperty(),axiom.getObject());
        return reasoner.hasType(axiom.getSubject().asOWLNamedIndividual(),hasValue,false);
    }
    public Boolean visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
        // see OWL 2 Syntax, Sec 11.2
        // No axiom in Ax of the following form contains anonymous individuals:
        // SameIndividual, DifferentIndividuals, NegativeObjectPropertyAssertion, and NegativeDataPropertyAssertion.
        if (axiom.getSubject().isAnonymous()) {
            throw new IllegalArgumentException("NegativeDataPropertyAssertion axioms are not allowed to be used "+"with anonymous individuals (see OWL 2 Syntax Sec 11.2) and the subject "+axiom.getSubject()+" of the axiom "+axiom+" is anonymous. ");
        }
        OWLClassExpression hasValue=factory.getOWLDataHasValue(axiom.getProperty(),axiom.getObject());
        OWLClassExpression doesNotHaveValue=factory.getOWLObjectComplementOf(hasValue);
        return reasoner.hasType(axiom.getSubject().asOWLNamedIndividual(),doesNotHaveValue,false);
    }

    // ************ object properties ****************************

    public Boolean visit(OWLObjectPropertyDomainAxiom axiom) {
        return reasoner.isSubClassOf(factory.getOWLObjectSomeValuesFrom(axiom.getProperty(),factory.getOWLThing()),axiom.getDomain());
    }
    public Boolean visit(OWLObjectPropertyRangeAxiom axiom) {
        return reasoner.isSubClassOf(factory.getOWLThing(),factory.getOWLObjectAllValuesFrom(axiom.getProperty(),axiom.getRange()));
    }
    public Boolean visit(OWLInverseObjectPropertiesAxiom axiom) {
        OWLObjectPropertyExpression objectPropertyExpression1=axiom.getFirstProperty().getInverseProperty();
        OWLObjectPropertyExpression objectPropertyExpression2=axiom.getSecondProperty();
        return reasoner.isSubObjectPropertyExpressionOf(objectPropertyExpression1,objectPropertyExpression2) && reasoner.isSubObjectPropertyExpressionOf(objectPropertyExpression2,objectPropertyExpression1);
    }
    public Boolean visit(OWLSymmetricObjectPropertyAxiom axiom) {
        return reasoner.isSymmetric(axiom.getProperty());
    }
    public Boolean visit(OWLTransitiveObjectPropertyAxiom axiom) {
        return reasoner.isTransitive(axiom.getProperty());
    }
    public Boolean visit(OWLReflexiveObjectPropertyAxiom axiom) {
        return reasoner.isReflexive(axiom.getProperty());
    }
    public Boolean visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
        return reasoner.isIrreflexive(axiom.getProperty());
    }
    public Boolean visit(OWLAsymmetricObjectPropertyAxiom axiom) {
        return reasoner.isAsymmetric(axiom.getProperty());
    }
    public Boolean visit(OWLEquivalentObjectPropertiesAxiom axiom) {
        Set<OWLObjectPropertyExpression> props=axiom.getProperties();
        Iterator<OWLObjectPropertyExpression> it=props.iterator();
        if (it.hasNext()) {
            OWLObjectPropertyExpression objectPropertyExpression1=it.next();
            while (it.hasNext()) {
                OWLObjectPropertyExpression objectPropertyExpression2=it.next();
                if (!reasoner.isSubObjectPropertyExpressionOf(objectPropertyExpression1,objectPropertyExpression2) || !reasoner.isSubObjectPropertyExpressionOf(objectPropertyExpression2,objectPropertyExpression1))
                    return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }
    public Boolean visit(OWLSubObjectPropertyOfAxiom axiom) {
        return reasoner.isSubObjectPropertyExpressionOf(axiom.getSubProperty(),axiom.getSuperProperty());
    }
    public Boolean visit(OWLSubPropertyChainOfAxiom axiom) {
        return reasoner.isSubObjectPropertyExpressionOf(axiom.getPropertyChain(),axiom.getSuperProperty());
    }
    public Boolean visit(OWLDisjointObjectPropertiesAxiom axiom) {
        int n=axiom.getProperties().size();
        OWLObjectPropertyExpression[] props=axiom.getProperties().toArray(new OWLObjectPropertyExpression[n]);
        for (int i=0;i<n-1;i++)
            for (int j=i+1;j<n;j++)
                if (!reasoner.isDisjointObjectProperty(props[i],props[j])) return Boolean.FALSE;
        return Boolean.TRUE;
    }
    public Boolean visit(OWLFunctionalObjectPropertyAxiom axiom) {
        return reasoner.isFunctional(axiom.getProperty());
    }
    public Boolean visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
        return reasoner.isInverseFunctional(axiom.getProperty());
    }

    // ************ data properties ****************************

    public Boolean visit(OWLDataPropertyDomainAxiom axiom) {
        return reasoner.isSubClassOf(factory.getOWLDataSomeValuesFrom(axiom.getProperty(),factory.getTopDatatype()),axiom.getDomain());
    }
    public Boolean visit(OWLDataPropertyRangeAxiom axiom) {
        return reasoner.isSubClassOf(factory.getOWLThing(),factory.getOWLDataAllValuesFrom(axiom.getProperty(),axiom.getRange()));
    }
    public Boolean visit(OWLEquivalentDataPropertiesAxiom axiom) {
        Set<OWLDataPropertyExpression> props=axiom.getProperties();
        Iterator<OWLDataPropertyExpression> it=props.iterator();
        if (it.hasNext()) {
            OWLDataProperty prop1=it.next().asOWLDataProperty();
            while (it.hasNext()) {
                OWLDataProperty dataProperty1=prop1.asOWLDataProperty();
                OWLDataProperty dataProperty2=it.next().asOWLDataProperty();
                if (!reasoner.isSubDataPropertyOf(dataProperty1,dataProperty2) || !reasoner.isSubDataPropertyOf(dataProperty2,dataProperty1))
                    return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }
    public Boolean visit(OWLSubDataPropertyOfAxiom axiom) {
        return reasoner.isSubDataPropertyOf(axiom.getSubProperty().asOWLDataProperty(),axiom.getSuperProperty().asOWLDataProperty());
    }
    public Boolean visit(OWLDisjointDataPropertiesAxiom axiom) {
        int n=axiom.getProperties().size();
        OWLDataPropertyExpression[] props=axiom.getProperties().toArray(new OWLDataPropertyExpression[n]);
        for (int i=0;i<n-1;i++) {
            for (int j=i+1;j<n;j++) {
                OWLDataSomeValuesFrom some_i=factory.getOWLDataSomeValuesFrom(props[i],factory.getOWLDatatype(IRI.create(InternalDatatype.RDFS_LITERAL.getIRI())));
                OWLDataSomeValuesFrom some_j=factory.getOWLDataSomeValuesFrom(props[j],factory.getOWLDatatype(IRI.create(InternalDatatype.RDFS_LITERAL.getIRI())));
                OWLDataMaxCardinality max1=factory.getOWLDataMaxCardinality(1,factory.getOWLDataProperty(IRI.create(AtomicRole.TOP_DATA_ROLE.getIRI())));
                OWLClassExpression desc=factory.getOWLObjectIntersectionOf(some_i,some_j,max1);
                if (reasoner.isSatisfiable(desc))
                    return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }
    public Boolean visit(OWLFunctionalDataPropertyAxiom axiom) {
        return reasoner.isFunctional(axiom.getProperty().asOWLDataProperty());
    }

    // ************ class axioms ****************************

    public Boolean visit(OWLSubClassOfAxiom axiom) {
        return reasoner.isSubClassOf(axiom.getSubClass(),axiom.getSuperClass());
    }
    public Boolean visit(OWLEquivalentClassesAxiom axiom) {
        boolean isEntailed=true;
        Iterator<OWLClassExpression> i=axiom.getClassExpressions().iterator();
        if (i.hasNext()) {
            OWLClassExpression first=i.next();
            while (i.hasNext()&&isEntailed) {
                OWLClassExpression next=i.next();
                isEntailed=reasoner.isSubClassOf(first,next) && reasoner.isSubClassOf(next,first);
            }
        }
        return isEntailed;
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

    // ************ datatype definitions ****************************

    public Boolean visit(OWLDatatypeDefinitionAxiom axiom) {
        reasoner.throwInconsistentOntologyExceptionIfNecessary();
        if (!reasoner.isConsistent())
            return true;
        if (reasoner.m_dlOntology.hasDatatypes()) {
            OWLDataFactory factory=reasoner.getDataFactory();
            OWLIndividual freshIndividual=factory.getOWLAnonymousIndividual("fresh-individual");
            OWLDataProperty freshDataProperty=factory.getOWLDataProperty(IRI.create("fresh-data-property"));
            OWLDataRange dataRange=axiom.getDataRange();
            OWLDatatype dt=axiom.getDatatype();
            OWLDataIntersectionOf dr1=factory.getOWLDataIntersectionOf(factory.getOWLDataComplementOf(dataRange),dt);
            OWLDataIntersectionOf dr2=factory.getOWLDataIntersectionOf(factory.getOWLDataComplementOf(dt),dataRange);
            OWLDataUnionOf union=factory.getOWLDataUnionOf(dr1,dr2);
            OWLClassExpression c=factory.getOWLDataSomeValuesFrom(freshDataProperty,union);
            OWLClassAssertionAxiom ax=factory.getOWLClassAssertionAxiom(c,freshIndividual);
            Tableau tableau=reasoner.getTableau(ax);
            return !tableau.isSatisfiable(true,true,null,null,null,null,null,ReasoningTaskDescription.isAxiomEntailed(axiom));
        }
        else
            return false;
    }

    // ************ rules ****************************

    public Boolean visit(SWRLRule rule) {
        throw new UnsupportedOperationException();
    }

    // ************ keys****************************

    public Boolean visit(OWLHasKeyAxiom axiom) {
        reasoner.throwFreshEntityExceptionIfNecessary(axiom);
        reasoner.throwInconsistentOntologyExceptionIfNecessary();
        if (!reasoner.isConsistent())
            return true;
        OWLOntologyManager ontologyManager=OWLManager.createOWLOntologyManager();
        OWLDataFactory factory=ontologyManager.getOWLDataFactory();
        OWLIndividual individualA=factory.getOWLNamedIndividual(IRI.create("internal:named-fresh-individual-A"));
        OWLIndividual individualB=factory.getOWLNamedIndividual(IRI.create("internal:named-fresh-individual-B"));
        Set<OWLAxiom> axioms=new HashSet<OWLAxiom>();
        axioms.add(factory.getOWLClassAssertionAxiom(axiom.getClassExpression(),individualA));
        axioms.add(factory.getOWLClassAssertionAxiom(axiom.getClassExpression(),individualB));
        int i=0;
        for (OWLObjectPropertyExpression p : axiom.getObjectPropertyExpressions()) {
            OWLIndividual tmp=factory.getOWLNamedIndividual(IRI.create("internal:named-fresh-individual-"+i));
            axioms.add(factory.getOWLObjectPropertyAssertionAxiom(p,individualA,tmp));
            axioms.add(factory.getOWLObjectPropertyAssertionAxiom(p,individualB,tmp));
            i++;
        }
        for (OWLDataPropertyExpression p : axiom.getDataPropertyExpressions()) {
            OWLDatatype anonymousConstantsDatatype=factory.getOWLDatatype(IRI.create("internal:anonymous-constants"));
            OWLLiteral constant=factory.getOWLLiteral("internal:constant-"+i,anonymousConstantsDatatype);
            axioms.add(factory.getOWLDataPropertyAssertionAxiom(p,individualA,constant));
            axioms.add(factory.getOWLDataPropertyAssertionAxiom(p,individualB,constant));
            i++;
        }
        axioms.add(factory.getOWLDifferentIndividualsAxiom(individualA,individualB));
        Tableau tableau=reasoner.getTableau(axioms.toArray(new OWLAxiom[axioms.size()]));
        return !tableau.isSatisfiable(true,true,null,null,null,null,null,ReasoningTaskDescription.isAxiomEntailed(axiom));
    }

    protected class AnonymousIndividualForestBuilder implements OWLAxiomVisitor {
        // No axiom in Ax of the following form contains anonymous individuals:
        // - SameIndividual, DifferentIndividuals, NegativeObjectPropertyAssertion, and NegativeDataPropertyAssertion.
        // A forest F over the anonymous individuals in Ax exists such that the following conditions are satisfied,
        // for OPE an object property expression, _:x and _:y anonymous individuals, and a a named individual:
        // - for each assertion in Ax of the form ObjectPropertyAssertion( OPE _:x _:y ), either _:x is a child of _:y
        // or _:y is a child of _:x in F;
        // - for each pair of anonymous individuals _:x and _:y such that _:y is a child of _:x in F, the set Ax contains
        // at most one assertion of the form ObjectPropertyAssertion( OPE _:x _:y ) or
        // ObjectPropertyAssertion( OPE _:y _:x ); and
        // - for each anonymous individual _:x that is a root in F, the set Ax contains at most one assertion of the
        // form ObjectPropertyAssertion( OPE _:x a ) or ObjectPropertyAssertion( OPE a _:x ).

        protected final Set<OWLNamedIndividual> namedNodes=new HashSet<OWLNamedIndividual>();
        protected final Set<OWLAnonymousIndividual> nodes=new HashSet<OWLAnonymousIndividual>();
        // that is the forest we are trying to construct
        protected final Map<OWLAnonymousIndividual,Set<OWLAnonymousIndividual>> edges=new HashMap<OWLAnonymousIndividual,Set<OWLAnonymousIndividual>>();
        // the following map determines which anonymous individual nodes are reachable from the named individuals
        protected final Map<OWLAnonymousIndividual,Map<OWLNamedIndividual,Set<OWLObjectPropertyExpression>>> specialOPEdges=new HashMap<OWLAnonymousIndividual,Map<OWLNamedIndividual,Set<OWLObjectPropertyExpression>>>();
        // node labels for the anonymous individual forest
        protected final Map<OWLAnonymousIndividual,Set<OWLClassExpression>> nodelLabels=new HashMap<OWLAnonymousIndividual,Set<OWLClassExpression>>();
        // edge labels for the anonymous individual forest
        protected final Map<Edge,OWLObjectProperty> edgeOPLabels=new HashMap<Edge,OWLObjectProperty>();

        protected final Set<OWLAxiom> anonIndAxioms=new HashSet<OWLAxiom>();
        protected final Set<OWLAxiom> anonNoNamedIndAxioms=new HashSet<OWLAxiom>();

        /**
         * @param factory
         *            The data factory to be used when creating new concepts in the elimination of anonymous individuals.
         * @param axioms
         *            The axioms from the conclusion ontology. After executing this method, the axioms that contain anonymous individuals can be discarded. Instead the methods getAssertions() and getConcepts() can be used to check entailment of the conclusion ontology.
         */
        public void constructConceptsForAnonymousIndividuals(OWLDataFactory factory,Set<OWLAxiom> axioms) {
            // The anonymous individuals together with the object property assertions
            // induce a labelled forest with anonymous individuals as nodes and edges labelled
            // with an object property and nodes labelled with a set of concepts.
            // While visiting all axioms, we construct the induced forest. Data property assertions
            // are rolled-up during the traversal and result in node labels, e.g.,
            // DataPropertyAssertion(:dp _:x <someLiteral>) results in the concept
            // DataHasValue(:dp <someLiteral>) being added to the label of concepts of the node
            // for _:x.
            // In case the result is not a forest, an error is thrown.
            for (OWLAxiom ax : axioms) {
                ax.accept(this);
            }

            // We now find the components among the anonymous individuals. It must be possible to
            // arrange each component into a tree such that the root satisfies the above mentioned
            // conditions on root. If that is not possible, an error is thrown.
            Set<Set<OWLAnonymousIndividual>> components=getComponents();
            Map<Set<OWLAnonymousIndividual>,OWLAnonymousIndividual> componentsToRoots=findSuitableRoots(components);
            // It seems the forest is valid, so we can read off the concepts.
            for (Set<OWLAnonymousIndividual> component : componentsToRoots.keySet()) {
                OWLAnonymousIndividual root=componentsToRoots.get(component);
                if (!specialOPEdges.containsKey(root)) {
                    // It was not possible to find a root that has exactly one relationship with a named individual,
                    // otherwise findSuitableRoots() had given preference to that root.
                    // We have to roll-up into a concept.
                    OWLClassExpression c=getClassExpressionFor(factory,root,null);
                    anonNoNamedIndAxioms.add(factory.getOWLSubClassOfAxiom(factory.getOWLThing(),factory.getOWLObjectComplementOf(c)));
                }
                else {
                    // We can roll-up into a class assertion.
                    Map<OWLNamedIndividual,Set<OWLObjectPropertyExpression>> ind2OP=specialOPEdges.get(root);
                    // We now that the set of object properties is a singleton set since otherwise this would
                    // not be a valid root and the map should have one single entry, but lets double-check.
                    if (ind2OP.size()!=1) {
                        throw new RuntimeException("Internal error: HermiT decided that the anonymous individuals form a valid forest, but actually they do not. ");
                    }
                    OWLNamedIndividual subject=ind2OP.keySet().iterator().next();
                    Set<OWLObjectPropertyExpression> ops=ind2OP.get(subject);
                    if (ops.size()!=1) {
                        throw new RuntimeException("Internal error: HermiT decided that the anonymous individuals form a valid forest, but actually they do not. ");
                    }
                    OWLObjectPropertyExpression op=ops.iterator().next().getInverseProperty().getSimplified();
                    OWLClassExpression c=getClassExpressionFor(factory,root,null);
                    anonIndAxioms.add(factory.getOWLClassAssertionAxiom(factory.getOWLObjectSomeValuesFrom(op,c),subject));
                }
            }
        }
        /**
         * After calling constructConceptsForAnonymousIndividuals(), the method return a set of assertions that have to be entailed in order to satisfy the axioms that contain anonymous individuals. E.g., if the conclusion ontology contains axioms ObjectPropertyAssertion(:r :fred _:x), ObjectPropertyAssertion(:r _:x _:y), we get the assertion ClassAssertion(:fred ObjectSomeValuesFrom(:r ObjectSomeValuesFrom(:r owl:Thing))).
         *
         * @return a set of assertions without anonymous individuals that have to be entailed by the premise ontology
         */
        public Set<OWLAxiom> getAnonIndAxioms() {
            return anonIndAxioms;
        }
        /**
         * After calling constructConceptsForAnonymousIndividuals(), the method return a set of subclass axioms that, when added to the premise ontology should result in an inconsistency for the entailment to hold. E.g., if the conclusion ontology contains axiom ObjectPropertyAssertion(:r _:x _:y), we get the axiom SubClassOf(owl:Thing ObjectAllValuesFrom(:r owl:Nothing)) and if the premise ontology plus this axioms is inconsistent, then the entailment holds since each model of the premise contains some element that is an r-successor of something.
         *
         * @return a set of axioms without anonymous individuals that have make the premise ontology inconsistent for the entailment to hold
         */
        public Set<OWLAxiom> getAnonNoNamedIndAxioms() {
            return anonNoNamedIndAxioms;
        }
        // /**
        // * After calling constructConceptsForAnonymousIndividuals(), the method return a set of concepts that
        // * have to be entailed in order to satisfy the axioms that contain anonymous individuals. E.g., if the conclusion
        // * ontology contains axioms ObjectPropertyAssertion(:r _:x _:y), ClassAssertion(:C _:y) and there is no named
        // * individual that is related to _:x, then we get the concept ObjectSomeValuesFrom(:r :C) and the entailment holds
        // * if the premise ontology plus the axiom SubClassOf(owl:Thing ObjectcomplementOf(ObjectSomeValuesFrom(:r :C))) is
        // * inconsistent.
        // * @return a set of concepts that have to be non-empty in each model of the premise ontlogy in order for
        // * the entailment to hold
        // */
        // public Set<OWLClassExpression> getConcepts() {
        // return concepts;
        // }
        protected OWLClassExpression getClassExpressionFor(OWLDataFactory factory,OWLAnonymousIndividual node,OWLAnonymousIndividual predecessor) {
            Set<OWLAnonymousIndividual> successors=edges.get(node);
            if (successors==null||(successors.size()==1&&successors.iterator().next()==predecessor)) {
                // the tree consists of a single node
                if (!nodelLabels.containsKey(node)) {
                    return factory.getOWLThing();
                }
                else if (nodelLabels.get(node).size()==1) {
                    return nodelLabels.get(node).iterator().next();
                }
                else {
                    return factory.getOWLObjectIntersectionOf(nodelLabels.get(node));
                }
            }
            Set<OWLClassExpression> concepts=new HashSet<OWLClassExpression>();
            for (OWLAnonymousIndividual successor : successors) {
                OWLObjectProperty op;
                Edge pair=new Edge(node,successor);
                if (edgeOPLabels.containsKey(pair)) {
                    op=edgeOPLabels.get(pair);
                }
                else {
                    pair=new Edge(successor,node);
                    if (!edgeOPLabels.containsKey(pair)) {
                        throw new RuntimeException("Internal error: some edge in the forest of anonymous individuals has no edge label although it should. ");
                    }
                    else {
                        op=edgeOPLabels.get(pair);
                    }
                }
                concepts.add(factory.getOWLObjectSomeValuesFrom(op,getClassExpressionFor(factory,successor,node)));
            }
            return concepts.size()==1 ? concepts.iterator().next() : factory.getOWLObjectIntersectionOf(concepts);
        }

        protected Map<Set<OWLAnonymousIndividual>,OWLAnonymousIndividual> findSuitableRoots(Set<Set<OWLAnonymousIndividual>> components) {
            Map<Set<OWLAnonymousIndividual>,OWLAnonymousIndividual> componentsToRoots=new HashMap<Set<OWLAnonymousIndividual>,OWLAnonymousIndividual>();
            for (Set<OWLAnonymousIndividual> component : components) {
                // We have to find a node with at most one relation to the named individuals
                // if there is one with exactly one relation that is a bit nicer for the rolling-up
                // so we try to find that
                OWLAnonymousIndividual root=null;
                OWLAnonymousIndividual rootWithOneNamedRelation=null;
                for (OWLAnonymousIndividual ind : component) {
                    if (specialOPEdges.containsKey(ind)) {
                        if (specialOPEdges.get(ind).size()<2) {
                            rootWithOneNamedRelation=ind;
                        }
                    }
                    else {
                        root=ind;
                    }
                }
                if (root==null&&rootWithOneNamedRelation==null) {
                    throw new IllegalArgumentException("Invalid input ontology: One of the trees in the forst of anomnymous individuals has no root that satisfies the criteria on roots (cf. OWL 2 Structural Specification and Functional-Style Syntax, Sec. 11.2).");
                }
                else if (rootWithOneNamedRelation!=null) {
                    componentsToRoots.put(component,rootWithOneNamedRelation);
                }
                else {
                    componentsToRoots.put(component,root);
                }
            }
            return componentsToRoots;
        }
        protected Set<Set<OWLAnonymousIndividual>> getComponents() {
            Set<Set<OWLAnonymousIndividual>> components=new HashSet<Set<OWLAnonymousIndividual>>();
            if (nodes.isEmpty())
                return components;

            Set<OWLAnonymousIndividual> toProcess=nodes;
            Set<OWLAnonymousIndividual> currentComponent;
            List<Edge> workQueue=new ArrayList<Edge>();
            Edge nodePlusPredecessor;

            while (!toProcess.isEmpty()) {
                currentComponent=new HashSet<OWLAnonymousIndividual>();
                nodePlusPredecessor=new Edge(toProcess.iterator().next(),null);
                workQueue.add(nodePlusPredecessor);
                while (!workQueue.isEmpty()) {
                    nodePlusPredecessor=workQueue.remove(0);
                    currentComponent.add(nodePlusPredecessor.first);
                    // see whether there are any successors that we have to check
                    if (edges.containsKey(nodePlusPredecessor.first)) {
                        // add successors to the workQueue
                        for (OWLAnonymousIndividual ind : edges.get(nodePlusPredecessor.first)) {
                            if (nodePlusPredecessor.second==null||!(ind.getID().equals(nodePlusPredecessor.second.getID()))) {
                                // check for cycle
                                for (Edge pair : workQueue) {
                                    if (pair.first==ind) {
                                        throw new IllegalArgumentException("Invalid input ontology: The anonymous individuals cannot be arranged into a forest as required (cf. OWL 2 Structural Specification and Functional-Style Syntax, Sec. 11.2) because there is a cycle. ");
                                    }
                                }
                                workQueue.add(new Edge(ind,nodePlusPredecessor.first));
                            }
                        }
                    }
                }
                components.add(currentComponent);
                toProcess.removeAll(currentComponent);
            }
            return components;
        }
        public void visit(OWLClassAssertionAxiom axiom) {
            if (axiom.getClassExpression().isOWLThing())
                return;
            OWLIndividual node=axiom.getIndividual();
            if (!node.isAnonymous()) {
                namedNodes.add(node.asOWLNamedIndividual());
            }
            else {
                nodes.add(node.asOWLAnonymousIndividual());
                if (nodelLabels.containsKey(node)) {
                    nodelLabels.get(node).add(axiom.getClassExpression());
                }
                else {
                    Set<OWLClassExpression> label=new HashSet<OWLClassExpression>();
                    label.add(axiom.getClassExpression());
                    nodelLabels.put(node.asOWLAnonymousIndividual(),label);
                }
            }
        }
        public void visit(OWLObjectPropertyAssertionAxiom axiom) {
            OWLIndividual sub=axiom.getSubject();
            OWLIndividual obj=axiom.getObject();
            OWLObjectPropertyExpression ope=axiom.getProperty().getSimplified();
            if (!sub.isAnonymous()&&!obj.isAnonymous()) {
                return; // not interesting for the forest
            }
            else if ((!sub.isAnonymous()&&obj.isAnonymous())||(sub.isAnonymous()&&!obj.isAnonymous())) {
                if (!sub.isAnonymous()&&obj.isAnonymous()) {
                    OWLIndividual tmp=sub;
                    sub=obj;
                    obj=tmp;
                    ope=ope.getInverseProperty().getSimplified();
                }
                OWLNamedIndividual named=obj.asOWLNamedIndividual();
                OWLAnonymousIndividual unnamed=sub.asOWLAnonymousIndividual();
                namedNodes.add(named);
                nodes.add(unnamed);
                if (specialOPEdges.containsKey(unnamed)) {
                    Map<OWLNamedIndividual,Set<OWLObjectPropertyExpression>> specialEdges=specialOPEdges.get(unnamed);
                    if (specialEdges.containsKey(named)) {
                        specialEdges.get(named).add(ope);
                    }
                    else {
                        specialEdges=new HashMap<OWLNamedIndividual,Set<OWLObjectPropertyExpression>>();
                        Set<OWLObjectPropertyExpression> label=new HashSet<OWLObjectPropertyExpression>();
                        label.add(ope);
                        specialEdges.put(named,label);
                        specialOPEdges.put(unnamed,specialEdges);
                    }
                }
                else {
                    Map<OWLNamedIndividual,Set<OWLObjectPropertyExpression>> specialEdge=new HashMap<OWLNamedIndividual,Set<OWLObjectPropertyExpression>>();
                    Set<OWLObjectPropertyExpression> label=new HashSet<OWLObjectPropertyExpression>();
                    label.add(ope);
                    specialEdge.put(named,label);
                    specialOPEdges.put(unnamed,specialEdge);
                }
            }
            else {
                // both sub and obj anonymous
                OWLObjectProperty op;
                if (ope.isAnonymous()) {
                    // inverse role
                    op=ope.getNamedProperty();
                    OWLIndividual tmp=sub;
                    sub=obj;
                    obj=tmp;
                }
                else {
                    op=ope.asOWLObjectProperty();
                }
                OWLAnonymousIndividual subAnon=sub.asOWLAnonymousIndividual();
                OWLAnonymousIndividual objAnon=obj.asOWLAnonymousIndividual();
                nodes.add(subAnon);
                nodes.add(objAnon);
                if ((edges.containsKey(subAnon)&&edges.get(subAnon).contains(objAnon))||((edges.containsKey(objAnon)&&edges.get(objAnon).contains(subAnon)))) {
                    throw new IllegalArgumentException("Invalid input ontology: There are two object property assertions for the same anonymous individuals, "+"which is not allowed (see OWL 2 Syntax Sec 11.2). ");
                }
                if (edges.containsKey(subAnon)) {
                    edges.get(subAnon).add(objAnon);
                }
                else {
                    Set<OWLAnonymousIndividual> successors=new HashSet<OWLAnonymousIndividual>();
                    successors.add(objAnon);
                    edges.put(subAnon,successors);
                }
                if (edges.containsKey(objAnon)) {
                    edges.get(objAnon).add(subAnon);
                }
                else {
                    Set<OWLAnonymousIndividual> successors=new HashSet<OWLAnonymousIndividual>();
                    successors.add(subAnon);
                    edges.put(objAnon,successors);
                }
                edgeOPLabels.put(new Edge(subAnon,objAnon),op);
            }
        }
        public void visit(OWLDataPropertyAssertionAxiom axiom) {
            if (!axiom.getSubject().isAnonymous()) {
                return; // not interesting for the anonymous individual forest
            }
            OWLAnonymousIndividual sub=axiom.getSubject().asOWLAnonymousIndividual();
            nodes.add(sub);
            OWLClassExpression c=factory.getOWLDataHasValue(axiom.getProperty(),axiom.getObject());
            if (nodelLabels.containsKey(sub)) {
                nodelLabels.get(sub).add(c);
            }
            else {
                Set<OWLClassExpression> labels=new HashSet<OWLClassExpression>();
                labels.add(c);
                nodelLabels.put(sub,labels);
            }
        }
        public void visit(OWLDeclarationAxiom axiom) {
        }
        public void visit(OWLSubClassOfAxiom axiom) {
        }
        public void visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
        }
        public void visit(OWLAsymmetricObjectPropertyAxiom axiom) {
        }
        public void visit(OWLReflexiveObjectPropertyAxiom axiom) {
        }
        public void visit(OWLDisjointClassesAxiom axiom) {
        }
        public void visit(OWLDataPropertyDomainAxiom axiom) {
        }
        public void visit(OWLObjectPropertyDomainAxiom axiom) {
        }
        public void visit(OWLEquivalentObjectPropertiesAxiom axiom) {
        }
        public void visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
        }
        public void visit(OWLDifferentIndividualsAxiom axiom) {
        }
        public void visit(OWLDisjointDataPropertiesAxiom axiom) {
        }
        public void visit(OWLDisjointObjectPropertiesAxiom axiom) {
        }
        public void visit(OWLObjectPropertyRangeAxiom axiom) {
        }
        public void visit(OWLFunctionalObjectPropertyAxiom axiom) {
        }
        public void visit(OWLSubObjectPropertyOfAxiom axiom) {
        }
        public void visit(OWLDisjointUnionAxiom axiom) {
        }
        public void visit(OWLSymmetricObjectPropertyAxiom axiom) {
        }
        public void visit(OWLDataPropertyRangeAxiom axiom) {
        }
        public void visit(OWLFunctionalDataPropertyAxiom axiom) {
        }
        public void visit(OWLEquivalentDataPropertiesAxiom axiom) {
        }
        public void visit(OWLEquivalentClassesAxiom axiom) {
        }
        public void visit(OWLTransitiveObjectPropertyAxiom axiom) {
        }
        public void visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
        }
        public void visit(OWLSubDataPropertyOfAxiom axiom) {
        }
        public void visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
        }
        public void visit(OWLSameIndividualAxiom axiom) {
        }
        public void visit(OWLSubPropertyChainOfAxiom axiom) {
        }
        public void visit(OWLInverseObjectPropertiesAxiom axiom) {
        }
        public void visit(OWLHasKeyAxiom axiom) {
        }
        public void visit(OWLDatatypeDefinitionAxiom axiom) {
        }
        public void visit(SWRLRule rule) {
        }
        public void visit(OWLAnnotationAssertionAxiom axiom) {
        }
        public void visit(OWLSubAnnotationPropertyOfAxiom axiom) {
        }
        public void visit(OWLAnnotationPropertyDomainAxiom axiom) {
        }
        public void visit(OWLAnnotationPropertyRangeAxiom axiom) {
        }
    }

    protected class Edge {
        public final OWLAnonymousIndividual first;
        public final OWLAnonymousIndividual second;

        public Edge(OWLAnonymousIndividual first,OWLAnonymousIndividual second) {
            this.first=first;
            this.second=second;
        }
        public int hashCode() {
            return 13+(3*(first!=null ? first.hashCode() : 0))+(7*(second!=null ? second.hashCode() : 0));
        }
        public boolean equals(Object o) {
            if (o==this)
                return true;
            if (o==null||getClass()!=o.getClass())
                return false;
            Edge other=(Edge)o;
            return this.first.equals(other.first)&&this.second.equals(other.second);
        }
        public String toString() {
            return "("+first+", "+second+")";
        }
    }
}
