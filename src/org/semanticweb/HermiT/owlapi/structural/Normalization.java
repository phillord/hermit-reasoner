package org.semanticweb.HermiT.owlapi.structural;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

import org.semanticweb.owl.model.*;
import org.semanticweb.owl.normalform.NegationalNormalFormConverter;
import java.net.URI;

// Note that the transitivity stuff has been temporarily removed (commented out).
// Please don't remove those comments---they are placeholders for a merge I plan to do very soon now...
// -rob 2008-06-20

/**
 * This class implements the structural transformation from our new tableau paper. This transformation departs in the following way from the paper:
 * it keeps the concepts of the form \exists R.{ a_1, ..., a_n }, \forall R.{ a_1, ..., a_n }, and \forall R.\neg { a } intact.
 * These concepts are then clausified in a more efficient way.
 */
public class Normalization {
    protected final Map<OWLDescription,OWLDescription> m_definitions;
    protected final Map<OWLObjectOneOf,OWLClass> m_definitionsForNegativeNominals;
    protected final Collection<OWLDescription[]> m_conceptInclusions;
    protected final Collection<OWLObjectPropertyExpression[]> m_normalObjectPropertyInclusions;
    protected final Collection<OWLObjectPropertyExpression[]> m_inverseObjectPropertyInclusions;
    protected final Collection<OWLDataPropertyExpression[]> m_normalDataPropertyInclusions;
    protected final Collection<OWLIndividualAxiom> m_facts;
	protected final OWLDataFactory m_factory;
	protected final NegationalNormalFormConverter m_nnf_converter;
	protected final PLVisitor m_PL;
    
    public Normalization(OWLDataFactory factory) {
        m_definitions=new HashMap<OWLDescription,OWLDescription>();
        m_definitionsForNegativeNominals=new HashMap<OWLObjectOneOf,OWLClass>();
        m_conceptInclusions=new ArrayList<OWLDescription[]>();
        m_normalObjectPropertyInclusions=new ArrayList<OWLObjectPropertyExpression[]>(); 
        m_inverseObjectPropertyInclusions=new ArrayList<OWLObjectPropertyExpression[]>();
        m_normalDataPropertyInclusions=new ArrayList<OWLDataPropertyExpression[]>();
        m_facts=new HashSet<OWLIndividualAxiom>();
		m_factory = factory;
		m_nnf_converter = new NegationalNormalFormConverter(factory);
		m_PL = new PLVisitor(this);
    }

	protected OWLDescription nnf(OWLDescription d) {
		return m_nnf_converter.convertToNormalForm(d);
	}
	protected OWLDescription cnnf(OWLDescription d) {
		return nnf(m_factory.getOWLObjectComplementOf(d));
	}
	
    public Collection<OWLDescription[]> getConceptInclusions() {
        return m_conceptInclusions;
    }
    public Collection<OWLObjectPropertyExpression[]> getNormalObjectPropertyInclusions() {
        return m_normalObjectPropertyInclusions;
    }
    public Collection<OWLObjectPropertyExpression[]> getInverseObjectPropertyInclusions() {
        return m_inverseObjectPropertyInclusions;
    }
    public Collection<OWLDataPropertyExpression[]> getNormalDataPropertyInclusios() {
        return m_normalDataPropertyInclusions;
    }
    public Collection<OWLIndividualAxiom> getFacts() {
        return m_facts;
    }
    public void processOntology(OWLOntology ontology) throws OWLException {
    //     TransitivityManager transitivityManager=new TransitivityManager();
        for (OWLInverseObjectPropertiesAxiom axiom : ontology.getAxioms(AxiomType.INVERSE_OBJECT_PROPERTIES)) {
            OWLObjectPropertyExpression first=axiom.getFirstProperty();
            OWLObjectPropertyExpression second=axiom.getSecondProperty();
            // transitivityManager.addInclusion(first,second.getInverseObjectProperty().getSimplified());
            // transitivityManager.addInclusion(second,first.getInverseObjectProperty().getSimplified());
            m_inverseObjectPropertyInclusions.add(new OWLObjectPropertyExpression[] { first,second  });
            m_inverseObjectPropertyInclusions.add(new OWLObjectPropertyExpression[] { second,first  });
        }
        for (OWLObjectSubPropertyAxiom axiom : ontology.getAxioms(AxiomType.SUB_OBJECT_PROPERTY)) {
            OWLObjectPropertyExpression subObjectProperty=axiom.getSubProperty();
            OWLObjectPropertyExpression superObjectProperty=axiom.getSuperProperty();
            // transitivityManager.addInclusion(subObjectProperty,superObjectProperty);
            m_normalObjectPropertyInclusions.add(new OWLObjectPropertyExpression[] { subObjectProperty,superObjectProperty });
        }
        for (OWLEquivalentObjectPropertiesAxiom axiom : ontology.getAxioms(AxiomType.EQUIVALENT_OBJECT_PROPERTIES)) {
            OWLObjectPropertyExpression[] objectProperties=new OWLObjectPropertyExpression[axiom.getProperties().size()];
            axiom.getProperties().toArray(objectProperties);
            // for (int i=0;i<objectProperties.length;i++)
            //     objectProperties[i]=objectProperties[i].getSimplified();
            for (int i=0;i<objectProperties.length-1;i++) {
                // transitivityManager.addInclusion(objectProperties[i],objectProperties[i+1]);
                // transitivityManager.addInclusion(objectProperties[i+1],objectProperties[i]);
                m_normalObjectPropertyInclusions.add(new OWLObjectPropertyExpression[] { objectProperties[i],objectProperties[i+1] });
                m_normalObjectPropertyInclusions.add(new OWLObjectPropertyExpression[] { objectProperties[i+1],objectProperties[i] });
            }
        }
        for (OWLDataSubPropertyAxiom axiom : ontology.getAxioms(AxiomType.SUB_DATA_PROPERTY)) {
            OWLDataPropertyExpression subDataProperty=axiom.getSubProperty();
            OWLDataPropertyExpression superDataProperty=axiom.getSuperProperty();
            m_normalDataPropertyInclusions.add(new OWLDataPropertyExpression[] { subDataProperty,superDataProperty });
        }
        for (OWLEquivalentDataPropertiesAxiom axiom : ontology.getAxioms(AxiomType.EQUIVALENT_DATA_PROPERTIES)) {
            OWLDataPropertyExpression[] dataProperties=new OWLDataPropertyExpression[axiom.getProperties().size()];
            axiom.getProperties().toArray(dataProperties);
            for (int i=0;i<dataProperties.length-1;i++) {
                m_normalDataPropertyInclusions.add(new OWLDataPropertyExpression[] { dataProperties[i],dataProperties[i+1] });
                m_normalDataPropertyInclusions.add(new OWLDataPropertyExpression[] { dataProperties[i+1],dataProperties[i] });
            }
        }
        List<OWLDescription[]> inclusions=new ArrayList<OWLDescription[]>();
        for (OWLSubClassAxiom axiom : ontology.getAxioms(AxiomType.SUBCLASS)) {
            inclusions.add(new OWLDescription[] { cnnf(axiom.getSubClass()),nnf(axiom.getSuperClass()) });
        }
        for (OWLEquivalentClassesAxiom axiom : ontology.getAxioms(AxiomType.EQUIVALENT_CLASSES)) {
            OWLDescription[] descriptions=new OWLDescription[axiom.getDescriptions().size()];
            axiom.getDescriptions().toArray(descriptions);
            for (int i=0;i<descriptions.length-1;i++) {
                inclusions.add(new OWLDescription[] { cnnf(descriptions[i]),nnf(descriptions[i+1]) });
                inclusions.add(new OWLDescription[] { cnnf(descriptions[i+1]),nnf(descriptions[i]) });
            }
        }
        for (OWLDisjointClassesAxiom axiom : ontology.getAxioms(AxiomType.DISJOINT_CLASSES)) {
            OWLDescription[] descriptions=new OWLDescription[axiom.getDescriptions().size()];
            axiom.getDescriptions().toArray(descriptions);
            for (int i=0;i<descriptions.length;i++)
                descriptions[i]=cnnf(descriptions[i]);
            for (int i=0;i<descriptions.length;i++)
                for (int j=i+1;j<descriptions.length;j++)
                    inclusions.add(new OWLDescription[] { descriptions[i],descriptions[j] });
        }
		for (OWLFunctionalObjectPropertyAxiom axiom : ontology.getAxioms(AxiomType.FUNCTIONAL_OBJECT_PROPERTY)) {
        	inclusions.add(new OWLDescription[] { m_factory.getOWLObjectMaxCardinalityRestriction(axiom.getProperty(), 1) });
		}
		for (OWLInverseFunctionalObjectPropertyAxiom axiom : ontology.getAxioms(AxiomType.INVERSE_FUNCTIONAL_OBJECT_PROPERTY)) {
        	inclusions.add(new OWLDescription[] { m_factory.getOWLObjectMaxCardinalityRestriction(m_factory.getOWLObjectPropertyInverse(axiom.getProperty()), 1) });
		}
		for (OWLSymmetricObjectPropertyAxiom axiom : ontology.getAxioms(AxiomType.SYMMETRIC_OBJECT_PROPERTY)) {
            OWLObjectPropertyExpression objectProperty=axiom.getProperty();
            m_inverseObjectPropertyInclusions.add(new OWLObjectPropertyExpression[] { objectProperty,objectProperty });
            // transitivityManager.addInclusion(objectProperty,objectProperty.getInverseObjectProperty().getSimplified());
        }
		for (OWLTransitiveObjectPropertyAxiom axiom : ontology.getAxioms(AxiomType.TRANSITIVE_OBJECT_PROPERTY)) {
            // transitivityManager.makeTransitive(axiom.getObjectProperty().getSimplified());
		}
		for (OWLFunctionalDataPropertyAxiom axiom : ontology.getAxioms(AxiomType.FUNCTIONAL_DATA_PROPERTY)) {
        	inclusions.add(new OWLDescription[] { m_factory.getOWLDataMaxCardinalityRestriction(axiom.getProperty(), 1) });
		}
		for (OWLObjectPropertyDomainAxiom axiom : ontology.getAxioms(AxiomType.OBJECT_PROPERTY_DOMAIN)) {
            OWLObjectAllRestriction allPropertyNothing=m_factory.getOWLObjectAllRestriction(axiom.getProperty(),m_factory.getOWLNothing());
            inclusions.add(new OWLDescription[] { axiom.getDomain(),allPropertyNothing });
        }
		for (OWLObjectPropertyRangeAxiom axiom : ontology.getAxioms(AxiomType.OBJECT_PROPERTY_RANGE)) {
            OWLObjectAllRestriction allPropertyRange=m_factory.getOWLObjectAllRestriction(axiom.getProperty(),nnf(axiom.getRange()));
            inclusions.add(new OWLDescription[] { allPropertyRange });
        }
		for (OWLDataPropertyDomainAxiom axiom : ontology.getAxioms(AxiomType.DATA_PROPERTY_DOMAIN)) {
			// Not sure whether this is the best way to get an unsatisfiable data range using the OWL API:
			OWLDataRange dataNothing = m_factory.getOWLDataComplementOf(m_factory.getOWLDataType(URI.create("http://www.w3.org/2000/01/rdf-schema#Literal")));
            OWLDataAllRestriction allPropertyNothing=m_factory.getOWLDataAllRestriction(axiom.getProperty(),dataNothing);
            inclusions.add(new OWLDescription[] { axiom.getDomain(),allPropertyNothing });
        }
		for (OWLDataPropertyRangeAxiom axiom : ontology.getAxioms(AxiomType.DATA_PROPERTY_RANGE)) {
            OWLDataAllRestriction allPropertyRange=m_factory.getOWLDataAllRestriction(axiom.getProperty(),axiom.getRange());
            inclusions.add(new OWLDescription[] { allPropertyRange });
        }
        OWLDescriptionVisitorEx<OWLDescription> normalizer=new NormalizationVisitor(inclusions, this);
        boolean[] alreadyExists=new boolean[1];
		for (OWLClassAssertionAxiom axiom : ontology.getAxioms(AxiomType.CLASS_ASSERTION)) {
            OWLDescription description=nnf(axiom.getDescription());
            if (!isSimple(description)) {
                OWLDescription definition=getDefinitionFor(description,alreadyExists);
                if (!alreadyExists[0])
                    inclusions.add(new OWLDescription[] { cnnf(definition),description });
                description=definition;
            }
            if (description==axiom.getDescription()) m_facts.add(axiom);
            else m_facts.add(m_factory.getOWLClassAssertionAxiom(axiom.getIndividual(),description));
        }
		for (OWLObjectPropertyAssertionAxiom axiom : ontology.getAxioms(AxiomType.OBJECT_PROPERTY_ASSERTION)) {
            m_facts.add(m_factory.getOWLObjectPropertyAssertionAxiom(axiom.getSubject(),axiom.getProperty(),axiom.getObject()));
        }
		for (OWLSameIndividualsAxiom axiom : ontology.getAxioms(AxiomType.SAME_INDIVIDUAL)) m_facts.add(axiom);
		for (OWLDifferentIndividualsAxiom axiom : ontology.getAxioms(AxiomType.DIFFERENT_INDIVIDUALS)) m_facts.add(axiom);
        normalizeInclusions(inclusions,normalizer);
    //     // process transitivity
    //     transitivityManager.transitivelyClose();
    //     for (Description[] inclusion : m_conceptInclusions) {
    //         for (int index=0;index<inclusion.length;index++)
    //             inclusion[index]=transitivityManager.replaceDescriptionIfNecessary(inclusion[index]);
    //     }
    //     transitivityManager.generateTransitivityAxioms();
    }
    protected void normalizeInclusions(List<OWLDescription[]> inclusions,OWLDescriptionVisitorEx<OWLDescription> normalizer) throws OWLException {
        while (!inclusions.isEmpty()) {
            OWLDescription[] curInclusion = inclusions.remove(inclusions.size()-1);
            java.util.HashSet<OWLDescription> setInclusion = new java.util.HashSet<OWLDescription>();
            for (int i=0; i < curInclusion.length; ++i) setInclusion.add(curInclusion[i]);
            OWLDescription simplifiedDescription=m_factory.getOWLObjectUnionOf(setInclusion);
            if (!simplifiedDescription.isOWLThing()) {
                if (simplifiedDescription instanceof OWLObjectUnionOf) {
                    OWLObjectUnionOf objectOr=(OWLObjectUnionOf)simplifiedDescription;
                    OWLDescription[] descriptions=new OWLDescription[objectOr.getOperands().size()];
                    objectOr.getOperands().toArray(descriptions);
                    if (!distributeUnionOverAnd(descriptions,inclusions) && !optimizedNegativeOneOfTranslation(descriptions,inclusions)) {
                        for (int index=0;index<descriptions.length;index++) {
                            descriptions[index]=descriptions[index].accept(normalizer);
                        }
                        m_conceptInclusions.add(descriptions);
                    }
                }
                else if (simplifiedDescription instanceof OWLObjectIntersectionOf) {
                    OWLObjectIntersectionOf objectAnd=(OWLObjectIntersectionOf)simplifiedDescription;
                    for (OWLDescription conjunct : objectAnd.getOperands())
                        inclusions.add(new OWLDescription[] { conjunct });
                }
                else {
                    OWLDescription normalized=(OWLDescription)simplifiedDescription.accept(normalizer);
                    m_conceptInclusions.add(new OWLDescription[] { normalized });
                }
            }
        }
        
    }
    protected boolean optimizedNegativeOneOfTranslation(OWLDescription[] descriptions,List<OWLDescription[]> inclusions) {
        if (descriptions.length==2) {
            OWLObjectOneOf objectOneOf=null;
            OWLDescription other=null;
            if (descriptions[0] instanceof OWLObjectComplementOf && ((OWLObjectComplementOf)descriptions[0]).getOperand() instanceof OWLObjectOneOf) {
                objectOneOf=(OWLObjectOneOf)((OWLObjectComplementOf)descriptions[0]).getOperand();
                other=descriptions[1];
            }
            else if (descriptions[1] instanceof OWLObjectComplementOf && ((OWLObjectComplementOf)descriptions[1]).getOperand() instanceof OWLObjectOneOf) {
                other=descriptions[0];
                objectOneOf=(OWLObjectOneOf)((OWLObjectComplementOf)descriptions[1]).getOperand();
            }
            if (objectOneOf!=null && (other instanceof OWLClass || (other instanceof OWLObjectComplementOf && ((OWLObjectComplementOf)other).getOperand() instanceof OWLClass))) {
                for (OWLIndividual individual : objectOneOf.getIndividuals())
                    m_facts.add(m_factory.getOWLClassAssertionAxiom(individual,other));
                return true;
            }
        }
        return false;
    }
    protected boolean distributeUnionOverAnd(OWLDescription[] descriptions,List<OWLDescription[]> inclusions) {
        int andIndex=-1;
        for (int index=0;index<descriptions.length;index++) {
            OWLDescription description=descriptions[index];
            if (!isSimple(description))
                if (description instanceof OWLObjectIntersectionOf) {
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
        for (OWLDescription description : objectAnd.getOperands()) {
            OWLDescription[] newDescriptions=descriptions.clone();
            newDescriptions[andIndex]=description;
            inclusions.add(newDescriptions);
        }
        return true;
    }
    protected OWLDescription getDefinitionFor(OWLDescription description,boolean[] alreadyExists) {
        OWLDescription definition=m_definitions.get(description);
        if (definition==null) {
            definition=m_factory.getOWLClass(URI.create("internal:q#"+m_definitions.size()));
            if (!(Boolean)description.accept(m_PL))
                definition=m_factory.getOWLObjectComplementOf(definition);
            m_definitions.put(description,definition);
            alreadyExists[0]=false;
        }
        else
            alreadyExists[0]=true;
        return definition;
    }
    protected OWLClass getDefinitionForNegativeNominal(OWLObjectOneOf objectOneOf,boolean[] alreadyExists) {
        OWLClass definition=m_definitionsForNegativeNominals.get(objectOneOf);
        if (definition==null) {
            definition=m_factory.getOWLClass(URI.create("internal:nnq#"+m_definitionsForNegativeNominals.size()));
            m_definitionsForNegativeNominals.put(objectOneOf,definition);
            alreadyExists[0]=false;
        }
        else
            alreadyExists[0]=true;
        return definition;
    }
    protected boolean isSimple(OWLDescription description) {
        return description instanceof OWLClass || (description instanceof OWLObjectComplementOf && ((OWLObjectComplementOf)description).getOperand() instanceof OWLClass);
    }
    
    protected class NormalizationVisitor implements OWLDescriptionVisitorEx<OWLDescription> {
        protected final Collection<OWLDescription[]> m_newInclusions;
        protected final boolean[] m_alreadyExists;
		Normalization m_n;
    
        public NormalizationVisitor(Collection<OWLDescription[]> newInclusions, Normalization n) {
            m_newInclusions=newInclusions;
            m_alreadyExists=new boolean[1];
			m_n = n;
        }

		public OWLDescription visit(OWLDataAllRestriction desc) {
			throw new RuntimeException("Datatypes are not supported yet.");
		}
		public OWLDescription visit(OWLDataExactCardinalityRestriction desc) {
			throw new RuntimeException("Datatypes are not supported yet.");
		}
		public OWLDescription visit(OWLDataMaxCardinalityRestriction desc) {
			throw new RuntimeException("Datatypes are not supported yet.");
		}
		public OWLDescription visit(OWLDataMinCardinalityRestriction desc) {
			throw new RuntimeException("Datatypes are not supported yet.");
		}
		public OWLDescription visit(OWLDataSomeRestriction desc) {
			throw new RuntimeException("Datatypes are not supported yet.");
		}
		public OWLDescription visit(OWLDataValueRestriction desc) {
			throw new RuntimeException("Datatypes are not supported yet.");
		}

        public OWLDescription visit(OWLClass object) {
            return object;
        }

        public OWLDescription visit(OWLObjectAllRestriction object) {
            OWLDescription description=object.getFiller();
            if ( isSimple(description) ||
                 description instanceof OWLObjectOneOf ||
                (description instanceof OWLObjectComplementOf &&
                 ((OWLObjectComplementOf)description).getOperand() instanceof OWLObjectOneOf &&
                 ((OWLObjectOneOf)((OWLObjectComplementOf)description).getOperand()).getIndividuals().size()==1
                )) // The ObjectOneof cases are optimizations.
                return object;
            else {
                OWLDescription definition=getDefinitionFor(description,m_alreadyExists);
                if (!m_alreadyExists[0])
                    m_newInclusions.add(new OWLDescription[] { cnnf(definition),description });
                return m_factory.getOWLObjectAllRestriction(object.getProperty(),definition);
            }
        }
        public OWLDescription visit(OWLObjectSomeRestriction object) {
            OWLDescription description=object.getFiller();
            if (isSimple(description) || description instanceof OWLObjectOneOf) // The ObjectOneOf case is an optimization.
                return object;
            else {
                OWLDescription definition=getDefinitionFor(description,m_alreadyExists);
                if (!m_alreadyExists[0])
                    m_newInclusions.add(new OWLDescription[] { cnnf(definition),description });
                return m_factory.getOWLObjectSomeRestriction(object.getProperty(),definition);
            }
        }
        public OWLDescription visit(OWLObjectExactCardinalityRestriction object) {
            OWLObjectPropertyExpression objectProperty=object.getProperty();
            OWLDescription description=object.getFiller();
            OWLDescription definition=getDefinitionFor(object,m_alreadyExists);
            if (!m_alreadyExists[0]) {
                m_newInclusions.add(new OWLDescription[] { m_n.cnnf(definition),m_n.m_factory.getOWLObjectExactCardinalityRestriction(objectProperty,object.getCardinality(),description) });
            }
            return definition;
        }
        public OWLDescription visit(OWLObjectMinCardinalityRestriction object) {
            OWLObjectPropertyExpression objectProperty=object.getProperty();
            OWLDescription description=object.getFiller();
            if (object.getCardinality()<=0) return m_n.m_factory.getOWLThing();
            else if (isSimple(description)) return object;
            else if (object.getCardinality()==1 && description instanceof OWLObjectOneOf) {  // This is an optimization
                return m_n.m_factory.getOWLObjectSomeRestriction(objectProperty,description);
            } else {
                OWLDescription definition=getDefinitionFor(description,m_alreadyExists);
                if (!m_alreadyExists[0])
                    m_newInclusions.add(new OWLDescription[] { m_n.cnnf(definition),description });
                return m_n.m_factory.getOWLObjectMinCardinalityRestriction(objectProperty,object.getCardinality(),definition);
            }
        }
        public OWLDescription visit(OWLObjectMaxCardinalityRestriction object) {
            OWLObjectPropertyExpression objectProperty=object.getProperty();
            OWLDescription description=object.getFiller();
            if (object.getCardinality()<=0) {
                return m_n.m_factory.getOWLObjectAllRestriction(objectProperty,m_n.cnnf(description)).accept(this);
            } else if (isSimple(description)) return object;
            else {
                OWLDescription complementDescription=m_n.cnnf(description);
                OWLDescription definition=getDefinitionFor(complementDescription,m_alreadyExists);
                if (!m_alreadyExists[0])
                    m_newInclusions.add(new OWLDescription[] { m_n.cnnf(definition),complementDescription });
                return m_n.m_factory.getOWLObjectMaxCardinalityRestriction(objectProperty,object.getCardinality(),m_n.cnnf(definition));
            }
        }
        public OWLDescription visit(OWLObjectComplementOf object) {
            if (object.getOperand() instanceof OWLObjectOneOf) {
                OWLObjectOneOf objectOneOf=(OWLObjectOneOf)object.getOperand();
                OWLClass definition=getDefinitionForNegativeNominal(objectOneOf,m_alreadyExists);
                if (!m_alreadyExists[0]) {
                    for (OWLIndividual individual : objectOneOf.getIndividuals())
                        m_facts.add(m_n.m_factory.getOWLClassAssertionAxiom(individual,definition));
                }
                return m_n.m_factory.getOWLObjectComplementOf(definition);
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
                    m_newInclusions.add(new OWLDescription[] { m_n.cnnf(definition),description });
            }
            return definition;
        }
        public OWLDescription visit(OWLObjectOneOf object) {
            return object;
        }
        public OWLDescription visit(OWLObjectValueRestriction object) {
            OWLObjectOneOf objectOneOf=m_n.m_factory.getOWLObjectOneOf(object.getValue());
            return m_n.m_factory.getOWLObjectSomeRestriction(object.getProperty(),objectOneOf);
        }
        public OWLDescription visit(OWLObjectSelfRestriction object) {
			throw new RuntimeException("Reflexivity is not supported yet.");
        }
    }
    
    protected static class PLVisitor implements OWLDescriptionVisitorEx<Boolean> {
		protected final Normalization m_n;
		
		public PLVisitor(Normalization n) { m_n = n; }
    
        public Boolean visit(OWLClass object) {
            if (object.isOWLThing()) return Boolean.FALSE;
            else if (object.isOWLNothing()) return Boolean.FALSE;
            else return Boolean.TRUE;
        }
        public Boolean visit(OWLObjectAllRestriction object) {
            return object.getFiller().accept(this);
        }
        public Boolean visit(OWLObjectSomeRestriction object) {
  			return Boolean.TRUE;
        }
        public Boolean visit(OWLObjectMinCardinalityRestriction object) {
			return object.getCardinality() > 0;
		}
		public Boolean visit(OWLObjectMaxCardinalityRestriction object) {
			return object.getCardinality() > 0 ? Boolean.TRUE : m_n.cnnf(object.getFiller()).accept(this);
		}
		public Boolean visit(OWLObjectExactCardinalityRestriction object) {
			return object.getCardinality() > 0 ? Boolean.TRUE : m_n.cnnf(object.getFiller()).accept(this);
		}
        public Boolean visit(OWLObjectComplementOf object) {
            return Boolean.FALSE;
        }
        public Boolean visit(OWLObjectUnionOf object) {
            for (OWLDescription description : object.getOperands())
                if (description.accept(this)) return Boolean.TRUE;
            return Boolean.FALSE;
        }
        public Boolean visit(OWLObjectIntersectionOf object) {
            for (OWLDescription description : object.getOperands())
                if (description.accept(this)) return Boolean.TRUE;
            return Boolean.FALSE;
        }
        public Boolean visit(OWLObjectOneOf object) {
            return Boolean.TRUE;
        }
        public Boolean visit(OWLObjectValueRestriction object) {
            return Boolean.TRUE;
        }
        public Boolean visit(OWLObjectSelfRestriction object) {
			throw new RuntimeException("Reflexivity is not supported yet.");
        }
		public Boolean visit(OWLDataAllRestriction desc) {
			throw new RuntimeException("Datatypes are not supported yet.");
		}
		public Boolean visit(OWLDataExactCardinalityRestriction desc) {
			throw new RuntimeException("Datatypes are not supported yet.");
		}
		public Boolean visit(OWLDataMaxCardinalityRestriction desc) {
			throw new RuntimeException("Datatypes are not supported yet.");
		}
		public Boolean visit(OWLDataMinCardinalityRestriction desc) {
			throw new RuntimeException("Datatypes are not supported yet.");
		}
		public Boolean visit(OWLDataSomeRestriction desc) {
			throw new RuntimeException("Datatypes are not supported yet.");
		}
		public Boolean visit(OWLDataValueRestriction desc) {
			throw new RuntimeException("Datatypes are not supported yet.");
		}
    }

    // protected class TransitivityManager {
    //     protected final Map<ObjectPropertyExpression,Set<ObjectPropertyExpression>> m_subObjectProperties;
    //     protected final Set<ObjectPropertyExpression> m_transitiveObjectProperties;
    //     protected final Map<ObjectAll,Description> m_replacedDescriptions;
    // 
    //     public TransitivityManager() {
    //         m_subObjectProperties=new HashMap<ObjectPropertyExpression,Set<ObjectPropertyExpression>>();
    //         m_transitiveObjectProperties=new HashSet<ObjectPropertyExpression>();
    //         m_replacedDescriptions=new HashMap<ObjectAll,Description>();
    //     }
    //     public void addInclusion(ObjectPropertyExpression subObjectProperty,ObjectPropertyExpression superObjectProperty) {
    //         addInclusionEx(subObjectProperty.getSimplified(),superObjectProperty.getSimplified());
    //         addInclusionEx(subObjectProperty.getInverseObjectProperty().getSimplified(),superObjectProperty.getInverseObjectProperty().getSimplified());
    //     }
    //     public void makeTransitive(ObjectPropertyExpression objectProperty) {
    //         m_transitiveObjectProperties.add(objectProperty.getSimplified());
    //         m_transitiveObjectProperties.add(objectProperty.getInverseObjectProperty().getSimplified());
    //     }
    //     protected void addInclusionEx(ObjectPropertyExpression subObjectProperty,ObjectPropertyExpression superObjectProperty) {
    //         Set<ObjectPropertyExpression> subObjectProperties=m_subObjectProperties.get(superObjectProperty);
    //         if (subObjectProperties==null) {
    //             subObjectProperties=new HashSet<ObjectPropertyExpression>();
    //             m_subObjectProperties.put(superObjectProperty,subObjectProperties);
    //         }
    //         subObjectProperties.add(subObjectProperty);
    //     }
    //     public Description replaceDescriptionIfNecessary(Description description) {
    //         if (description instanceof ObjectAll) {
    //             ObjectAll objectAll=(ObjectAll)description;
    //             ObjectPropertyExpression objectProperty=((ObjectAll)description).getObjectProperty();
    //             Set<ObjectPropertyExpression> transitiveSubObjectProperties=getTransitiveSubObjectProperties(objectProperty);
    //             if (!transitiveSubObjectProperties.isEmpty()) {
    //                 Description replacement=getReplacementFor(objectAll);
    //                 for (ObjectPropertyExpression transitiveSubObjectProperty : transitiveSubObjectProperties) {
    //                     ObjectAll subObjectAll=KAON2Manager.factory().objectAll(transitiveSubObjectProperty,objectAll.getDescription());
    //                     getReplacementFor(subObjectAll);
    //                 }
    //                 return replacement;
    //             }
    //         }
    //         return description;
    //     }
    //     protected Description getReplacementFor(ObjectAll objectAll) {
    //         Description replacement=m_replacedDescriptions.get(objectAll);
    //         if (replacement==null) {
    //             replacement=KAON2Manager.factory().owlClass("internal:all#"+m_replacedDescriptions.size());
    //             if (objectAll.getDescription() instanceof ObjectNot)
    //                 replacement=replacement.getComplementNNF();
    //             m_replacedDescriptions.put(objectAll,replacement);
    //         }
    //         return replacement;
    //     }
    //     public void generateTransitivityAxioms() {
    //         for (Map.Entry<ObjectAll,Description> replacement : m_replacedDescriptions.entrySet()) {
    //             m_conceptInclusions.add(new Description[] { replacement.getValue().getComplementNNF(),replacement.getKey() });
    //             ObjectPropertyExpression objectProperty=replacement.getKey().getObjectProperty();
    //             for (ObjectPropertyExpression transitiveSubObjectProperty : getTransitiveSubObjectProperties(objectProperty)) {
    //                 ObjectAll consequentAll=KAON2Manager.factory().objectAll(transitiveSubObjectProperty,replacement.getKey().getDescription());
    //                 Description consequentReplacement=m_replacedDescriptions.get(consequentAll);
    //                 assert consequentReplacement!=null;
    //                 ObjectAll forallConsequentReplacement=KAON2Manager.factory().objectAll(transitiveSubObjectProperty,consequentReplacement);
    //                 m_conceptInclusions.add(new Description[] { replacement.getValue().getComplementNNF(),forallConsequentReplacement });
    //             }
    //         }
    //         m_replacedDescriptions.clear();
    //     }
    //     protected void transitivelyClose() {
    //         boolean changed=true;
    //         List<ObjectPropertyExpression> temporary=new ArrayList<ObjectPropertyExpression>();
    //         while (changed) {
    //             changed=false;
    //             for (Map.Entry<ObjectPropertyExpression,Set<ObjectPropertyExpression>> entry : m_subObjectProperties.entrySet()) {
    //                 temporary.clear();
    //                 temporary.addAll(entry.getValue());
    //                 for (int i=temporary.size()-1;i>=0;--i) {
    //                     Set<ObjectPropertyExpression> subObjectProperties=m_subObjectProperties.get(temporary.get(i));
    //                     if (subObjectProperties!=null)
    //                         if (entry.getValue().addAll(subObjectProperties))
    //                             changed=true;
    //                 }
    //             }
    //         }
    //     }
    //     protected Set<ObjectPropertyExpression> getTransitiveSubObjectProperties(ObjectPropertyExpression objectProperty) {
    //         Set<ObjectPropertyExpression> result=new HashSet<ObjectPropertyExpression>();
    //         if (m_transitiveObjectProperties.contains(objectProperty))
    //             result.add(objectProperty);
    //         Set<ObjectPropertyExpression> subObjectProperties=m_subObjectProperties.get(objectProperty);
    //         if (subObjectProperties!=null)
    //             for (ObjectPropertyExpression subObjectProperty : subObjectProperties)
    //                 if (m_transitiveObjectProperties.contains(subObjectProperty))
    //                     result.add(subObjectProperty);
    //         return result;
    //     }
    // }
}
