// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.owlapi.structural;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.owl.model.OWLAntiSymmetricObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLAxiomAnnotationAxiom;
import org.semanticweb.owl.model.OWLAxiomVisitor;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLClassAssertionAxiom;
import org.semanticweb.owl.model.OWLConstant;
import org.semanticweb.owl.model.OWLDataAllRestriction;
import org.semanticweb.owl.model.OWLDataComplementOf;
import org.semanticweb.owl.model.OWLDataExactCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDataMaxCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataMinCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataOneOf;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owl.model.OWLDataPropertyExpression;
import org.semanticweb.owl.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owl.model.OWLDataRange;
import org.semanticweb.owl.model.OWLDataSomeRestriction;
import org.semanticweb.owl.model.OWLDataSubPropertyAxiom;
import org.semanticweb.owl.model.OWLDataValueRestriction;
import org.semanticweb.owl.model.OWLDeclarationAxiom;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLDescriptionVisitorEx;
import org.semanticweb.owl.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owl.model.OWLDisjointClassesAxiom;
import org.semanticweb.owl.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owl.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owl.model.OWLDisjointUnionAxiom;
import org.semanticweb.owl.model.OWLEntityAnnotationAxiom;
import org.semanticweb.owl.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owl.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owl.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owl.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLImportsDeclaration;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLIndividualAxiom;
import org.semanticweb.owl.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owl.model.OWLIrreflexiveObjectPropertyAxiom;
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
import org.semanticweb.owl.model.OWLObjectPropertyChainSubPropertyAxiom;
import org.semanticweb.owl.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owl.model.OWLObjectPropertyExpression;
import org.semanticweb.owl.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owl.model.OWLObjectSelfRestriction;
import org.semanticweb.owl.model.OWLObjectSomeRestriction;
import org.semanticweb.owl.model.OWLObjectSubPropertyAxiom;
import org.semanticweb.owl.model.OWLObjectUnionOf;
import org.semanticweb.owl.model.OWLObjectValueRestriction;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyAnnotationAxiom;
import org.semanticweb.owl.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLSameIndividualsAxiom;
import org.semanticweb.owl.model.OWLSubClassAxiom;
import org.semanticweb.owl.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owl.model.SWRLRule;

/**
 * This class implements the structural transformation from our new tableau paper. This transformation departs in the following way from the paper: it keeps the concepts of the form \exists R.{ a_1, ..., a_n }, \forall R.{ a_1, ..., a_n }, and \forall R.\neg { a } intact. These concepts are then clausified in a more efficient way.
 */
public class OWLNormalization implements Serializable {
    private static final long serialVersionUID=1696339403669197304L;

    protected final OWLDataFactory m_factory;
    protected final Map<OWLDescription,OWLDescription> m_definitions;
    protected final Map<OWLObjectOneOf,OWLClass> m_definitionsForNegativeNominals;
    protected final OWLAxioms m_axioms;

    public OWLNormalization(OWLDataFactory factory,OWLAxioms axioms) {
        m_factory=factory;
        m_definitions=new HashMap<OWLDescription,OWLDescription>();
        m_definitionsForNegativeNominals=new HashMap<OWLObjectOneOf,OWLClass>();
        m_axioms=axioms;
    }

    /**
     * Normalizes the ontology and performs the structural transformation. After executing this method, the getter methods of this class (e.g., getConceptInclusions, getAsymmetricObjectProperties, etc) can be used to retrieve the normalized axioms and assertions from the ontology.
     * 
     * @param inOntology
     *            the ontology to be normalized
     * @throws OWLException
     */
    public void processOntology(Configuration config,OWLOntology inOntology) {
        // Each entry in the inclusions list represents a disjunction of
        // concepts -- that is, each OWLDescription in an entry contributes a
        // disjunct. It is thus not really inclusions, but rather a disjunction
        // of concepts that represents an inclusion axiom.
        { // Approximate the top object and data roles
            // TODO: make this complete (efficiently)
            OWLObjectProperty topObjectProp=m_factory.getOWLObjectProperty(URI.create(AtomicRole.TOP_OBJECT_ROLE.getURI()));
            OWLDataProperty topDataProp=m_factory.getOWLDataProperty(URI.create(AtomicRole.TOP_DATA_ROLE.getURI()));
            for (OWLObjectProperty p : inOntology.getReferencedObjectProperties()) {
                addInclusion(p,topObjectProp);
            }
            for (OWLDataProperty p : inOntology.getReferencedDataProperties()) {
                addInclusion(p,topDataProp);
            }
            if (config.makeTopRoleUniversal) {
                makeTransitive(topObjectProp);
                makeReflexive(topObjectProp);
                addInclusion(topObjectProp,topObjectProp.getInverseProperty());
            }
        }
        AxiomVisitor axiomVisitor=new AxiomVisitor();
        for (OWLAxiom axiom : inOntology.getAxioms()) {
            // the visitor populates the member variables such as
            // m_reflexiveObjectProperties, m_disjointObjectProperties, etc,
            // collects the facts and turns equivalences and implications into
            // an internal set of disjunctions that is then normalized
            axiom.accept(axiomVisitor);
        }
        List<OWLDescription[]> inclusions=axiomVisitor.getInclusionsAsDisjunctions();
        normalizeInclusions(inclusions,m_axioms.m_conceptInclusions,m_axioms.m_facts);
    }
    protected void addFact(OWLIndividualAxiom axiom) {
        m_axioms.m_facts.add(axiom);
    }
    protected void addHasKey(OWLHasKeyDummy axiom) {
        m_axioms.m_hasKeys.add(axiom);
    }
    protected void addInclusion(OWLDataPropertyExpression subDataPropertyExpression,OWLDataPropertyExpression superDataPropertyExpression) {
        m_axioms.m_dataPropertyInclusions.add(new OWLDataPropertyExpression[] { subDataPropertyExpression,superDataPropertyExpression });
    }
    protected void addInclusion(OWLObjectPropertyExpression subObjectPropertyExpression,OWLObjectPropertyExpression superObjectPropertyExpression) {
        m_axioms.m_objectPropertyInclusions.add(new OWLObjectPropertyExpression[] { subObjectPropertyExpression.getSimplified(),superObjectPropertyExpression.getSimplified() });
    }
    protected void makeTransitive(OWLObjectPropertyExpression objectPropertyExpression) {
        m_axioms.m_transitiveObjectProperties.add(objectPropertyExpression.getSimplified());
    }
    protected void makeReflexive(OWLObjectPropertyExpression objectPropertyExpression) {
        m_axioms.m_reflexiveObjectProperties.add(objectPropertyExpression.getSimplified());
    }
    protected void makeIrreflexive(OWLObjectPropertyExpression objectPropertyExpression) {
        m_axioms.m_irreflexiveObjectProperties.add(objectPropertyExpression.getSimplified());
    }
    protected void makeAsymmetric(OWLObjectPropertyExpression objectPropertyExpression) {
        m_axioms.m_asymmetricObjectProperties.add(objectPropertyExpression.getSimplified());
    }
    public void processKeys(Configuration config,Set<OWLHasKeyDummy> keys) {
        AxiomVisitor axiomVisitor=new AxiomVisitor();
        for (OWLHasKeyDummy key : keys) {
            axiomVisitor.visit(key);
        }
    }

    protected class AxiomVisitor implements Serializable,OWLAxiomVisitor {
        private static final long serialVersionUID=1121574663177585806L;

        protected final List<OWLDescription[]> m_inclusionsAsDisjunctions=new ArrayList<OWLDescription[]>();
        protected final boolean[] m_alreadyExists=new boolean[1];
        protected final SimplificationVisitor m_simplificationVisitor=new SimplificationVisitor();

        public List<OWLDescription[]> getInclusionsAsDisjunctions() {
            return m_inclusionsAsDisjunctions;
        }

        protected OWLDescription simplify(OWLDescription d) {
            return d.accept(m_simplificationVisitor);
        }
        
        // Semantic-less axioms
        
        public void visit(OWLImportsDeclaration axiom) {
        }

        public void visit(OWLDeclarationAxiom axiom) {
        }

        public void visit(OWLOntologyAnnotationAxiom axiom) {
        }

        public void visit(OWLEntityAnnotationAxiom axiom) {
        }

        public void visit(OWLAxiomAnnotationAxiom axiom) {
        }

        // Object property axioms
        
        public void visit(OWLObjectSubPropertyAxiom axiom) {
            addInclusion(axiom.getSubProperty(),axiom.getSuperProperty());
        }

        public void visit(OWLObjectPropertyChainSubPropertyAxiom axiom) {
            List<OWLObjectPropertyExpression> subChain=axiom.getPropertyChain();
            OWLObjectPropertyExpression superObjectPropertyExpression=axiom.getSuperProperty();
            if (subChain.size()==1)
                addInclusion(subChain.get(0),superObjectPropertyExpression);
            else if (subChain.size()==2 && subChain.get(0).equals(superObjectPropertyExpression) && subChain.get(1).equals(superObjectPropertyExpression))
                makeTransitive(axiom.getSuperProperty());
            else
                throw new RuntimeException("General property chains are not supported yet.");
        }

        public void visit(OWLEquivalentObjectPropertiesAxiom axiom) {
            if (axiom.getProperties().size()>1) {
                Iterator<OWLObjectPropertyExpression> iterator=axiom.getProperties().iterator();
                OWLObjectPropertyExpression first=iterator.next();
                OWLObjectPropertyExpression last=first;
                while (iterator.hasNext()) {
                    OWLObjectPropertyExpression next=iterator.next();
                    addInclusion(last,next);
                    last=next;
                }
                addInclusion(last,first);
            }
        }

        public void visit(OWLDisjointObjectPropertiesAxiom axiom) {
            OWLObjectPropertyExpression[] objectProperties=new OWLObjectPropertyExpression[axiom.getProperties().size()];
            axiom.getProperties().toArray(objectProperties);
            for (int i=0;i<objectProperties.length;i++)
                objectProperties[i]=objectProperties[i].getSimplified();
            m_axioms.m_disjointObjectProperties.add(objectProperties);
        }

        public void visit(OWLObjectPropertyDomainAxiom axiom) {
            OWLObjectAllRestriction allPropertyNohting=m_factory.getOWLObjectAllRestriction(axiom.getProperty().getSimplified(),m_factory.getOWLNothing());
            m_inclusionsAsDisjunctions.add(new OWLDescription[] { axiom.getDomain(),allPropertyNohting });
        }

        public void visit(OWLObjectPropertyRangeAxiom axiom) {
            OWLObjectAllRestriction allPropertyRange=m_factory.getOWLObjectAllRestriction(axiom.getProperty().getSimplified(),axiom.getRange().getNNF());
            m_inclusionsAsDisjunctions.add(new OWLDescription[] { allPropertyRange });
        }

        public void visit(OWLInverseObjectPropertiesAxiom axiom) {
            OWLObjectPropertyExpression first=axiom.getFirstProperty();
            OWLObjectPropertyExpression second=axiom.getSecondProperty();
            addInclusion(first,second.getInverseProperty());
            addInclusion(second,first.getInverseProperty());
        }

        public void visit(OWLFunctionalObjectPropertyAxiom axiom) {
            m_inclusionsAsDisjunctions.add(new OWLDescription[] { m_factory.getOWLObjectMaxCardinalityRestriction(axiom.getProperty().getSimplified(),1) });
        }

        public void visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
            m_inclusionsAsDisjunctions.add(new OWLDescription[] { m_factory.getOWLObjectMaxCardinalityRestriction(axiom.getProperty().getSimplified().getInverseProperty(),1) });
        }

        public void visit(OWLReflexiveObjectPropertyAxiom axiom) {
            makeReflexive(axiom.getProperty());
        }

        public void visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
            makeIrreflexive(axiom.getProperty());
        }

        public void visit(OWLSymmetricObjectPropertyAxiom axiom) {
            OWLObjectPropertyExpression objectProperty=axiom.getProperty();
            addInclusion(objectProperty,objectProperty.getInverseProperty());
        }

        public void visit(OWLAntiSymmetricObjectPropertyAxiom axiom) {
            makeAsymmetric(axiom.getProperty());
        }

        public void visit(OWLTransitiveObjectPropertyAxiom axiom) {
            makeTransitive(axiom.getProperty());
        }

        // Data property axioms
        
        public void visit(OWLDataSubPropertyAxiom axiom) {
            OWLDataPropertyExpression subDataProperty=axiom.getSubProperty();
            OWLDataPropertyExpression superDataProperty=axiom.getSuperProperty();
            addInclusion(subDataProperty,superDataProperty);
        }

        public void visit(OWLEquivalentDataPropertiesAxiom axiom) {
            OWLDataPropertyExpression[] dataProperties=new OWLDataPropertyExpression[axiom.getProperties().size()];
            axiom.getProperties().toArray(dataProperties);
            for (int i=0;i<dataProperties.length-1;i++) {
                addInclusion(dataProperties[i],dataProperties[i+1]);
                addInclusion(dataProperties[i+1],dataProperties[i]);
            }
        }

        public void visit(OWLDisjointDataPropertiesAxiom axiom) {
            OWLDataPropertyExpression[] dataProperties=new OWLDataPropertyExpression[axiom.getProperties().size()];
            axiom.getProperties().toArray(dataProperties);
            m_axioms.m_disjointDataProperties.add(dataProperties);
        }

        public void visit(OWLDataPropertyDomainAxiom axiom) {
            OWLDataRange dataNothing=m_factory.getOWLDataComplementOf(m_factory.getOWLDataType(URI.create(AtomicConcept.RDFS_LITERAL.getURI())));
            OWLDataAllRestriction allPropertyNothing=m_factory.getOWLDataAllRestriction(axiom.getProperty(),dataNothing);
            m_inclusionsAsDisjunctions.add(new OWLDescription[] { axiom.getDomain(),allPropertyNothing });
        }

        public void visit(OWLDataPropertyRangeAxiom axiom) {
            OWLDataAllRestriction allPropertyRange=m_factory.getOWLDataAllRestriction(axiom.getProperty(),axiom.getRange());
            m_inclusionsAsDisjunctions.add(new OWLDescription[] { allPropertyRange });
        }

        public void visit(OWLFunctionalDataPropertyAxiom axiom) {
            m_inclusionsAsDisjunctions.add(new OWLDescription[] { m_factory.getOWLDataMaxCardinalityRestriction(axiom.getProperty(),1) });
        }

        // Class axioms
        
        public void visit(OWLSubClassAxiom axiom) {
            m_inclusionsAsDisjunctions.add(new OWLDescription[] { axiom.getSubClass().getComplementNNF(),axiom.getSuperClass().getNNF() });
        }

        public void visit(OWLEquivalentClassesAxiom axiom) {
            OWLDescription[] descriptions=new OWLDescription[axiom.getDescriptions().size()];
            axiom.getDescriptions().toArray(descriptions);
            for (int i=0;i<descriptions.length-1;i++) {
                m_inclusionsAsDisjunctions.add(new OWLDescription[] { descriptions[i].getComplementNNF(),descriptions[i+1].getNNF() });
                m_inclusionsAsDisjunctions.add(new OWLDescription[] { descriptions[i+1].getComplementNNF(),descriptions[i].getNNF() });
            }
        }

        public void visit(OWLDisjointClassesAxiom axiom) {
            OWLDescription[] descriptions=new OWLDescription[axiom.getDescriptions().size()];
            axiom.getDescriptions().toArray(descriptions);
            for (int i=0;i<descriptions.length;i++)
                descriptions[i]=descriptions[i].getComplementNNF();
            for (int i=0;i<descriptions.length;i++)
                for (int j=i+1;j<descriptions.length;j++)
                    m_inclusionsAsDisjunctions.add(new OWLDescription[] { descriptions[i],descriptions[j] });
        }

        public void visit(OWLDisjointUnionAxiom axiom) {
            // DisjointUnion(C CE1 ... CEn)
            // add C implies CE1 or ... or CEn (not C or CE1 or ... or CEn)
            Set<OWLDescription> inclusion=new HashSet<OWLDescription>(axiom.getDescriptions());
            inclusion.add(axiom.getOWLClass().getComplementNNF());
            OWLDescription[] inclusionArray=new OWLDescription[axiom.getDescriptions().size()+1];
            inclusion.toArray(inclusionArray);
            m_inclusionsAsDisjunctions.add(inclusionArray);
            // add CE1 or ... or CEn implies C ((not CE1 and ... and not CEn) or C)
            OWLDescription conjunction=m_factory.getOWLObjectUnionOf(axiom.getDescriptions());
            m_inclusionsAsDisjunctions.add(new OWLDescription[] { conjunction.getComplementNNF(),axiom.getOWLClass() });
            // add CEi and CEj implies bottom (not CEi or not CEj) for 1 <= i < j <= n
            OWLDescription[] descriptions=new OWLDescription[axiom.getDescriptions().size()];
            axiom.getDescriptions().toArray(descriptions);
            for (int i=0;i<descriptions.length;i++) {
                descriptions[i]=descriptions[i].getComplementNNF();
            }
            for (int i=0;i<descriptions.length;i++) {
                for (int j=i+1;j<descriptions.length;j++) {
                    m_inclusionsAsDisjunctions.add(new OWLDescription[] { descriptions[i],descriptions[j] });
                }
            }
        }

        // Assertions
        
        public void visit(OWLClassAssertionAxiom axiom) {
            OWLDescription desc=simplify(axiom.getDescription().getNNF());
            if (!isSimple(desc)) {
                OWLDescription definition=getDefinitionFor(desc,m_alreadyExists);
                if (!m_alreadyExists[0])
                    m_inclusionsAsDisjunctions.add(new OWLDescription[] { definition.getComplementNNF(),desc });
                desc=definition;
            }
            if (desc==axiom.getDescription())
                addFact(axiom);
            else
                addFact(m_factory.getOWLClassAssertionAxiom(axiom.getIndividual(),desc));
        }

        public void visit(OWLObjectPropertyAssertionAxiom axiom) {
            addFact(m_factory.getOWLObjectPropertyAssertionAxiom(axiom.getSubject(),axiom.getProperty().getSimplified(),axiom.getObject()));
        }

        public void visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
            OWLObjectOneOf nominal=m_factory.getOWLObjectOneOf(axiom.getObject());
            OWLDescription not_nominal=m_factory.getOWLObjectComplementOf(nominal);
            OWLDescription restriction=m_factory.getOWLObjectAllRestriction(axiom.getProperty().getSimplified(),not_nominal);
            OWLClassAssertionAxiom rewrittenAxiom=m_factory.getOWLClassAssertionAxiom(axiom.getSubject(),restriction);
            OWLDescription desc=simplify(rewrittenAxiom.getDescription().getNNF());
            if (!isSimple(desc)) {
                OWLDescription definition=getDefinitionFor(desc,m_alreadyExists);
                if (!m_alreadyExists[0])
                    m_inclusionsAsDisjunctions.add(new OWLDescription[] { definition.getComplementNNF(),desc });
                desc=definition;
            }
            if (desc==rewrittenAxiom.getDescription())
                addFact(axiom);
            else
                addFact(m_factory.getOWLClassAssertionAxiom(rewrittenAxiom.getIndividual(),desc));
        }

        public void visit(OWLDataPropertyAssertionAxiom axiom) {
            OWLDataRange filler=m_factory.getOWLDataOneOf(axiom.getObject());
            OWLDataSomeRestriction restriction=m_factory.getOWLDataSomeRestriction(axiom.getProperty(),filler);
            OWLDescription definition=getDefinitionFor(restriction,m_alreadyExists);
            if (!m_alreadyExists[0]) {
                m_inclusionsAsDisjunctions.add(new OWLDescription[] { definition.getComplementNNF(),restriction });
            }
            addFact(m_factory.getOWLClassAssertionAxiom(axiom.getSubject(),definition));
        }

        public void visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
            OWLDataRange dataRange=m_factory.getOWLDataOneOf(axiom.getObject());
            OWLDataRange notDataRange=m_factory.getOWLDataComplementOf(dataRange);
            OWLDescription restriction=m_factory.getOWLDataAllRestriction(axiom.getProperty(),notDataRange);
            OWLDescription definition=getDefinitionFor(restriction,m_alreadyExists);
            if (!m_alreadyExists[0]) {
                m_inclusionsAsDisjunctions.add(new OWLDescription[] { definition.getComplementNNF(),restriction });
            }
            addFact(m_factory.getOWLClassAssertionAxiom(axiom.getSubject(),definition));
        }

        public void visit(OWLSameIndividualsAxiom axiom) {
            addFact(axiom);
        }

        public void visit(OWLDifferentIndividualsAxiom axiom) {
            addFact(axiom);
        }

        // Keys, rules, and the rest
        
        public void visit(OWLHasKeyDummy axiom) {
            OWLDescription desc=simplify(axiom.getClassExpression());
            if (desc instanceof OWLDataRange) {
                throw new RuntimeException("Parsed a data range instead of a "+"concept expression, but only concept "+"expressions can be used in an HasKey axiom.");
            }
            if (!isSimple(desc)) {
                OWLDescription definition=getDefinitionFor(desc,m_alreadyExists);
                if (!m_alreadyExists[0])
                    m_inclusionsAsDisjunctions.add(new OWLDescription[] { definition.getComplementNNF(),desc });
                desc=definition;
            }
            if (desc==axiom.getClassExpression()) {
                addHasKey(axiom);
            }
            else {
                // rewrite: construct a new axiom that uses the concept
                // definition.
                OWLHasKeyDummy k=new OWLHasKeyDummy(m_factory);
                k.setClassExpression(desc);
                k.setObjectProperties(axiom.getObjectProperties());
                k.setDataProperties(axiom.getDataProperties());
                addHasKey(k);
            }
        }

        public void visit(SWRLRule rule) {
            throw new RuntimeException("Parsed a SWRL rule, but SWRL rules "+"are not supported by HermiT.");
        }
    }

    /**
     * @param description
     *            an OWL class description
     * @return true if description is a literal or a data range, false otherwise
     */
    protected static boolean isSimple(OWLDescription description) {
        return
            description instanceof OWLClass ||
            (description instanceof OWLObjectComplementOf && ((OWLObjectComplementOf)description).getOperand() instanceof OWLClass) ||
            // assuming that we do not further normalize data ranges for now
            (description instanceof OWLDataRange);
    }

    protected void normalizeInclusions(List<OWLDescription[]> inclusions,Collection<OWLDescription[]> outInclusions,Collection<OWLIndividualAxiom> ioFacts) {
        OWLDescriptionVisitorEx<OWLDescription> normalizer=new NormalizationVisitor(inclusions);
        SimplificationVisitor simplificationVisitor=new SimplificationVisitor();
        while (!inclusions.isEmpty()) {
            OWLDescription simplifiedDescription=m_factory.getOWLObjectUnionOf(inclusions.remove(inclusions.size()-1)).accept(simplificationVisitor);
            if (!simplifiedDescription.isOWLThing()) {
                if (simplifiedDescription instanceof OWLObjectUnionOf) {
                    OWLObjectUnionOf objectOr=(OWLObjectUnionOf)simplifiedDescription;
                    OWLDescription[] descriptions=new OWLDescription[objectOr.getOperands().size()];
                    objectOr.getOperands().toArray(descriptions);
                    if (!distributeUnionOverAnd(descriptions,inclusions) && !optimizedNegativeOneOfTranslation(descriptions,m_factory,ioFacts)) {
                        for (int index=0;index<descriptions.length;index++)
                            descriptions[index]=descriptions[index].accept(normalizer);
                        outInclusions.add(descriptions);
                    }
                }
                else if (simplifiedDescription instanceof OWLObjectIntersectionOf) {
                    OWLObjectIntersectionOf objectAnd=(OWLObjectIntersectionOf)simplifiedDescription;
                    for (OWLDescription conjunct : objectAnd.getOperands())
                        inclusions.add(new OWLDescription[] { conjunct });
                }
                else {
                    OWLDescription normalized=simplifiedDescription.accept(normalizer);
                    outInclusions.add(new OWLDescription[] { normalized });
                }
            }
        }

    }

    protected boolean optimizedNegativeOneOfTranslation(OWLDescription[] descriptions,OWLDataFactory factory,Collection<OWLIndividualAxiom> ioFacts) {
        if (descriptions.length==2) {
            OWLObjectOneOf nominal=null;
            OWLDescription other=null;
            if (descriptions[0] instanceof OWLObjectComplementOf && ((OWLObjectComplementOf)descriptions[0]).getOperand() instanceof OWLObjectOneOf) {
                nominal=(OWLObjectOneOf)((OWLObjectComplementOf)descriptions[0]).getOperand();
                other=descriptions[1];
            }
            else if (descriptions[1] instanceof OWLObjectComplementOf && ((OWLObjectComplementOf)descriptions[1]).getOperand() instanceof OWLObjectOneOf) {
                other=descriptions[0];
                nominal=(OWLObjectOneOf)((OWLObjectComplementOf)descriptions[1]).getOperand();
            }
            if (nominal!=null && (other instanceof OWLClass || (other instanceof OWLObjectComplementOf && ((OWLObjectComplementOf)other).getOperand() instanceof OWLClass))) {
                for (OWLIndividual individual : nominal.getIndividuals()) {
                    ioFacts.add(factory.getOWLClassAssertionAxiom(individual,other));
                }
                return true;
            }
        }
        return false;
    }

    /**
     * If exactly one of the classes in descriptions is an intersection, new inclusions are added that each contain one intersection operand and all the other descriptions.
     * 
     * @param descriptions
     *            an array of OWL class descriptions that represent a disjunction of classes that originate from a GCI
     * @param inclusions
     *            The set of GCIs from this ontology written a disjunctions
     * @return true if exactly one of the descriptions is an intersection and false otherwise
     */
    protected boolean distributeUnionOverAnd(OWLDescription[] descriptions,List<OWLDescription[]> inclusions) {
        int andIndex=-1;
        for (int index=0;index<descriptions.length;index++) {
            OWLDescription desc=descriptions[index];
            if (!isSimple(desc))
                if (desc instanceof OWLObjectIntersectionOf) {
                    if (andIndex==-1)
                        andIndex=index;
                    else
                        return false;
                }
                else
                    return false;
        }
        if (andIndex==-1)
            return false;
        OWLObjectIntersectionOf objectAnd=(OWLObjectIntersectionOf)descriptions[andIndex];
        for (OWLDescription desc : objectAnd.getOperands()) {
            OWLDescription[] newDescriptions=descriptions.clone();
            newDescriptions[andIndex]=desc;
            inclusions.add(newDescriptions);
        }
        return true;
    }

    protected OWLDescription getDefinitionFor(OWLDescription desc,boolean[] alreadyExists,boolean forcePositive) {
        OWLDescription definition=m_definitions.get(desc);
        if (definition==null || (forcePositive && !(definition instanceof OWLClass))) {
            definition=m_factory.getOWLClass(URI.create("internal:q#"+m_definitions.size()));
            if (!forcePositive && !desc.accept(PLVisitor.INSTANCE)) {
                definition=m_factory.getOWLObjectComplementOf(definition);
            }
            // TODO: it's a little ugly to switch the definition
            // to positive polarity if it would naturally be negative and
            // could make future normalization less efficient, but in practice
            // we only demand positive polarity for class definitions after
            // the main ontology has already been clausified, so it shouldn't
            // hurt us *too* much.
            m_definitions.put(desc,definition);
            alreadyExists[0]=false;
        }
        else
            alreadyExists[0]=true;
        return definition;
    }

    protected OWLDescription getDefinitionFor(OWLDescription desc,boolean[] alreadyExists) {
        return getDefinitionFor(desc,alreadyExists,false);
    }

    protected OWLClass getClassFor(OWLDescription desc,boolean[] alreadyExists) {
        return (OWLClass)getDefinitionFor(desc,alreadyExists,true);
    }

    protected OWLClass getDefinitionForNegativeNominal(OWLObjectOneOf nominal,boolean[] alreadyExists) {
        OWLClass definition=m_definitionsForNegativeNominals.get(nominal);
        if (definition==null) {
            definition=m_factory.getOWLClass(URI.create("internal:nnq#"+m_definitionsForNegativeNominals.size()));
            m_definitionsForNegativeNominals.put(nominal,definition);
            alreadyExists[0]=false;
        }
        else
            alreadyExists[0]=true;
        return definition;
    }

    protected class NormalizationVisitor implements Serializable,OWLDescriptionVisitorEx<OWLDescription> {
        private static final long serialVersionUID=-1826322500216576256L;
        protected final Collection<OWLDescription[]> m_newInclusions;
        protected final boolean[] m_alreadyExists;

        public NormalizationVisitor(Collection<OWLDescription[]> newInclusions) {
            m_newInclusions=newInclusions;
            m_alreadyExists=new boolean[1];
        }

        public OWLDescription visit(OWLDataAllRestriction desc) {
            return desc;
        }

        public OWLDescription visit(OWLDataSomeRestriction desc) {
            return desc;
        }

        public OWLDescription visit(OWLDataExactCardinalityRestriction desc) {
            OWLDataPropertyExpression dataProperty=desc.getProperty();
            OWLDataRange dataRange=desc.getFiller();
            OWLDescription definition=getDefinitionFor(desc,m_alreadyExists);
            if (!m_alreadyExists[0]) {
                m_newInclusions.add(new OWLDescription[] { definition.getComplementNNF(),m_factory.getOWLDataMaxCardinalityRestriction(dataProperty,desc.getCardinality(),dataRange) });
                m_newInclusions.add(new OWLDescription[] { definition.getComplementNNF(),m_factory.getOWLDataMinCardinalityRestriction(dataProperty,desc.getCardinality(),dataRange) });
            }
            return definition;
        }

        public OWLDescription visit(OWLDataMaxCardinalityRestriction desc) {
            return desc;
        }

        public OWLDescription visit(OWLDataMinCardinalityRestriction desc) {
            if (desc.getCardinality()<=0)
                return m_factory.getOWLThing();
            else
                return desc;
        }

        public OWLDescription visit(OWLDataValueRestriction desc) {
            OWLDataRange dataRange=m_factory.getOWLDataOneOf(desc.getValue());
            return m_factory.getOWLDataSomeRestriction(desc.getProperty(),dataRange);
        }

        public OWLDescription visit(OWLClass object) {
            return object;
        }

        public OWLDescription visit(OWLObjectAllRestriction object) {
            OWLDescription description=object.getFiller();
            if (isSimple(description) || description instanceof OWLObjectOneOf || (description instanceof OWLObjectComplementOf && ((OWLObjectComplementOf)description).getOperand() instanceof OWLObjectOneOf && ((OWLObjectOneOf)((OWLObjectComplementOf)description).getOperand()).getIndividuals().size()==1))
                // The ObjectOneof cases are optimizations.
                return object;
            else {
                OWLDescription definition=getDefinitionFor(description,m_alreadyExists);
                if (!m_alreadyExists[0])
                    m_newInclusions.add(new OWLDescription[] { definition.getComplementNNF(),description });
                return m_factory.getOWLObjectAllRestriction(object.getProperty().getSimplified(),definition);
            }
        }

        public OWLDescription visit(OWLObjectSomeRestriction object) {
            OWLDescription description=object.getFiller();
            if (isSimple(description) || description instanceof OWLObjectOneOf)
                // The ObjectOneof cases is an optimization.
                return object;
            else {
                OWLDescription definition=getDefinitionFor(description,m_alreadyExists);
                if (!m_alreadyExists[0])
                    m_newInclusions.add(new OWLDescription[] { definition.getComplementNNF(),description });
                return m_factory.getOWLObjectSomeRestriction(object.getProperty().getSimplified(),definition);
            }
        }

        public OWLDescription visit(OWLObjectExactCardinalityRestriction object) {
            OWLObjectPropertyExpression objectProperty=object.getProperty().getSimplified();
            OWLDescription filler=object.getFiller();
            OWLDescription definition=getDefinitionFor(object,m_alreadyExists);
            if (!m_alreadyExists[0]) {
                m_newInclusions.add(new OWLDescription[] { definition.getComplementNNF(),m_factory.getOWLObjectMaxCardinalityRestriction(objectProperty,object.getCardinality(),filler) });
                m_newInclusions.add(new OWLDescription[] { definition.getComplementNNF(),m_factory.getOWLObjectMinCardinalityRestriction(objectProperty,object.getCardinality(),filler) });
            }
            return definition;
        }

        public OWLDescription visit(OWLObjectMinCardinalityRestriction object) {
            OWLObjectPropertyExpression objectProperty=object.getProperty().getSimplified();
            OWLDescription filler=object.getFiller();
            if (object.getCardinality()<=0)
                return m_factory.getOWLThing();
            else if (isSimple(filler))
                return object;
            else if (object.getCardinality()==1 && filler instanceof OWLObjectOneOf) {
                // This is an optimization
                return m_factory.getOWLObjectSomeRestriction(objectProperty,filler);
            }
            else {
                OWLDescription definition=getDefinitionFor(filler,m_alreadyExists);
                if (!m_alreadyExists[0])
                    m_newInclusions.add(new OWLDescription[] { definition.getComplementNNF(),filler });
                return m_factory.getOWLObjectMinCardinalityRestriction(objectProperty,object.getCardinality(),definition);
            }
        }

        public OWLDescription visit(OWLObjectMaxCardinalityRestriction object) {
            OWLObjectPropertyExpression objectProperty=object.getProperty().getSimplified();
            OWLDescription description=object.getFiller();
            if (object.getCardinality()<=0) {
                return m_factory.getOWLObjectAllRestriction(objectProperty,description.getComplementNNF()).accept(this);
            }
            else if (isSimple(description))
                return object;
            else {
                OWLDescription complementDescription=description.getComplementNNF();
                OWLDescription definition=getDefinitionFor(complementDescription,m_alreadyExists);
                if (!m_alreadyExists[0])
                    m_newInclusions.add(new OWLDescription[] { definition.getComplementNNF(),complementDescription });
                return m_factory.getOWLObjectMaxCardinalityRestriction(objectProperty,object.getCardinality(),definition.getComplementNNF());
            }
        }

        public OWLDescription visit(OWLObjectComplementOf object) {
            if (object.getOperand() instanceof OWLObjectOneOf) {
                OWLObjectOneOf objectOneOf=(OWLObjectOneOf)object.getOperand();
                OWLClass definition=getDefinitionForNegativeNominal(objectOneOf,m_alreadyExists);
                if (!m_alreadyExists[0]) {
                    for (OWLIndividual individual : objectOneOf.getIndividuals())
                        addFact(m_factory.getOWLClassAssertionAxiom(individual,definition));
                }
                return m_factory.getOWLObjectComplementOf(definition);
            }
            else
                return object;
        }

        public OWLDescription visit(OWLObjectUnionOf object) {
            throw new RuntimeException("OR should be broken down at the outermost level");
        }

        public OWLDescription visit(OWLObjectIntersectionOf object) {
            OWLDescription definition=getDefinitionFor(object,m_alreadyExists);
            if (!m_alreadyExists[0]) {
                for (OWLDescription description : object.getOperands())
                    m_newInclusions.add(new OWLDescription[] { definition.getComplementNNF(),description });
            }
            return definition;
        }

        public OWLDescription visit(OWLObjectOneOf object) {
            return object;
        }

        public OWLDescription visit(OWLObjectValueRestriction object) {
            OWLObjectOneOf objectOneOf=m_factory.getOWLObjectOneOf(object.getValue());
            return m_factory.getOWLObjectSomeRestriction(object.getProperty().getSimplified(),objectOneOf);
        }

        public OWLDescription visit(OWLObjectSelfRestriction object) {
            return object;
        }
    }

    /**
     * checks the polarity
     */
    protected static class PLVisitor implements Serializable,OWLDescriptionVisitorEx<Boolean> {
        private static final long serialVersionUID=-4945870351766793640L;
        protected static final PLVisitor INSTANCE=new PLVisitor();

        public Boolean visit(OWLClass object) {
            if (object.isOWLThing())
                return Boolean.FALSE;
            else if (object.isOWLNothing())
                return Boolean.FALSE;
            else
                return Boolean.TRUE;
        }

        public Boolean visit(OWLObjectAllRestriction object) {
            return object.getFiller().accept(this);
        }

        public Boolean visit(OWLObjectSomeRestriction object) {
            return Boolean.TRUE;
        }

        public Boolean visit(OWLObjectMinCardinalityRestriction object) {
            return object.getCardinality()>0;
        }

        public Boolean visit(OWLObjectMaxCardinalityRestriction object) {
            return object.getCardinality()>0 ? Boolean.TRUE : object.getFiller().getComplementNNF().accept(this);
        }

        public Boolean visit(OWLObjectExactCardinalityRestriction object) {
            return object.getCardinality()>0 ? Boolean.TRUE : object.getFiller().getComplementNNF().accept(this);
        }

        public Boolean visit(OWLObjectComplementOf object) {
            return Boolean.FALSE;
        }

        public Boolean visit(OWLObjectUnionOf object) {
            for (OWLDescription desc : object.getOperands())
                if (desc.accept(this))
                    return Boolean.TRUE;
            return Boolean.FALSE;
        }

        public Boolean visit(OWLObjectIntersectionOf object) {
            for (OWLDescription desc : object.getOperands())
                if (desc.accept(this))
                    return Boolean.TRUE;
            return Boolean.FALSE;
        }

        public Boolean visit(OWLObjectOneOf object) {
            return Boolean.TRUE;
        }

        public Boolean visit(OWLObjectValueRestriction object) {
            return Boolean.TRUE;
        }

        public Boolean visit(OWLObjectSelfRestriction object) {
            return Boolean.TRUE;
        }

        public Boolean visit(OWLDataAllRestriction desc) {
            return Boolean.FALSE;
        }

        public Boolean visit(OWLDataSomeRestriction desc) {
            return Boolean.TRUE;
        }

        public Boolean visit(OWLDataExactCardinalityRestriction desc) {
            return Boolean.TRUE;
        }

        public Boolean visit(OWLDataMaxCardinalityRestriction desc) {
            return Boolean.TRUE;
        }

        public Boolean visit(OWLDataMinCardinalityRestriction desc) {
            return Boolean.TRUE;
        }

        public Boolean visit(OWLDataValueRestriction desc) {
            return Boolean.FALSE;
        }
    }

    protected class SimplificationVisitor implements Serializable,OWLDescriptionVisitorEx<OWLDescription> {
        private static final long serialVersionUID=-5816087209808020534L;

        public OWLDescription visit(OWLClass d) {
            return d;
        }

        public OWLDescription visit(OWLObjectAllRestriction d) {
            if (d.getFiller().isOWLThing())
                return m_factory.getOWLThing();
            return m_factory.getOWLObjectAllRestriction(d.getProperty().getSimplified(),d.getFiller().accept(this));
        }

        public OWLDescription visit(OWLObjectSomeRestriction d) {
            if (d.getFiller().isOWLNothing())
                return m_factory.getOWLNothing();
            return m_factory.getOWLObjectSomeRestriction(d.getProperty().getSimplified(),d.getFiller().accept(this));
        }

        public OWLDescription visit(OWLObjectMinCardinalityRestriction d) {
            if (d.getCardinality()<=0)
                return m_factory.getOWLThing();
            if (d.getFiller().isOWLNothing())
                return m_factory.getOWLNothing();
            return m_factory.getOWLObjectMinCardinalityRestriction(d.getProperty().getSimplified(),d.getCardinality(),d.getFiller().accept(this));
        }

        public OWLDescription visit(OWLObjectMaxCardinalityRestriction d) {
            if (d.getFiller().isOWLNothing())
                return m_factory.getOWLThing();
            if (d.getCardinality()<=0) {
                return m_factory.getOWLObjectAllRestriction(d.getProperty().getSimplified(),m_factory.getOWLObjectComplementOf(d.getFiller()).accept(this));
            }
            return m_factory.getOWLObjectMaxCardinalityRestriction(d.getProperty().getSimplified(),d.getCardinality(),d.getFiller().accept(this));
        }

        public OWLDescription visit(OWLObjectExactCardinalityRestriction d) {
            if (d.getCardinality()<0)
                return m_factory.getOWLNothing();
            if (d.getCardinality()==0)
                return m_factory.getOWLObjectAllRestriction(d.getProperty().getSimplified(),m_factory.getOWLObjectComplementOf(d.getFiller()).accept(this));
            if (d.getFiller().isOWLNothing())
                return m_factory.getOWLNothing();
            return m_factory.getOWLObjectExactCardinalityRestriction(d.getProperty().getSimplified(),d.getCardinality(),d.getFiller().accept(this));
        }

        public OWLDescription visit(OWLObjectComplementOf d) {
            OWLDescription s=d.getOperand().accept(this);
            if (s instanceof OWLObjectComplementOf) {
                return ((OWLObjectComplementOf)s).getOperand();
            }
            if (s.isOWLThing())
                return m_factory.getOWLNothing();
            if (s.isOWLNothing())
                return m_factory.getOWLThing();
            return m_factory.getOWLObjectComplementOf(s);
        }

        public OWLDescription visit(OWLObjectUnionOf d) {
            HashSet<OWLDescription> newDisjuncts=new HashSet<OWLDescription>();
            for (OWLDescription cur : d.getOperands()) {
                OWLDescription s=cur.accept(this);
                if (s.isOWLThing())
                    return s;
                if (s.isOWLNothing())
                    continue;
                if (s instanceof OWLObjectUnionOf) {
                    newDisjuncts.addAll(((OWLObjectUnionOf)s).getOperands());
                }
                else
                    newDisjuncts.add(s);
            }
            return m_factory.getOWLObjectUnionOf(newDisjuncts);
        }

        public OWLDescription visit(OWLObjectIntersectionOf d) {
            HashSet<OWLDescription> newConjuncts=new HashSet<OWLDescription>();
            for (OWLDescription cur : d.getOperands()) {
                OWLDescription s=cur.accept(this);
                if (s.isOWLThing())
                    continue;
                if (s.isOWLNothing())
                    return s;
                if (s instanceof OWLObjectIntersectionOf) {
                    newConjuncts.addAll(((OWLObjectIntersectionOf)s).getOperands());
                }
                else
                    newConjuncts.add(s);
            }
            return m_factory.getOWLObjectIntersectionOf(newConjuncts);
        }

        public OWLDescription visit(OWLObjectOneOf d) {
            return d;
        }

        public OWLDescription visit(OWLObjectValueRestriction d) {
            return d;
        }

        public OWLDescription visit(OWLObjectSelfRestriction d) {
            return d;
        }

        public OWLDescription visit(OWLDataAllRestriction d) {
            OWLDataRange range=d.getFiller();
            if (range instanceof OWLDataComplementOf && ((OWLDataComplementOf)range).getDataRange() instanceof OWLDataOneOf) {
                OWLDataComplementOf compl=(OWLDataComplementOf)range;
                OWLDataOneOf oneOf=(OWLDataOneOf)compl.getDataRange();
                if (oneOf.getValues().size()>1) {
                    Set<OWLDescription> conjuncts=new HashSet<OWLDescription>();
                    for (OWLConstant constant : oneOf.getValues()) {
                        conjuncts.add(m_factory.getOWLDataAllRestriction(d.getProperty(),m_factory.getOWLDataComplementOf(m_factory.getOWLDataOneOf(constant))));
                    }
                    return m_factory.getOWLObjectIntersectionOf(conjuncts);
                }
                else {
                    return d;
                }
            }
            return d;
        }

        public OWLDescription visit(OWLDataExactCardinalityRestriction d) {
            return d;
        }

        public OWLDescription visit(OWLDataMaxCardinalityRestriction d) {
            return d;
        }

        public OWLDescription visit(OWLDataMinCardinalityRestriction d) {
            return d;
        }

        public OWLDescription visit(OWLDataSomeRestriction d) {
            return d;
        }

        public OWLDescription visit(OWLDataValueRestriction d) {
            return d;
        }
    }
}
