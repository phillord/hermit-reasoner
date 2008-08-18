// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.owlapi.structural;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

import org.semanticweb.owl.model.*;
import org.semanticweb.HermiT.model.AbstractRole;
import org.semanticweb.HermiT.model.AtomicAbstractRole;
import org.semanticweb.HermiT.RoleBox;
import java.net.URI;

/**
 * This class implements the structural transformation from our new tableau paper. This transformation departs in the following way from the paper:
 * it keeps the concepts of the form \exists R.{ a_1, ..., a_n }, \forall R.{ a_1, ..., a_n }, and \forall R.\neg { a } intact.
 * These concepts are then clausified in a more efficient way.
 */
public class OwlNormalization {
    protected final Map<OWLDescription,OWLDescription> m_definitions;
    protected final Map<OWLObjectOneOf,OWLClass> m_definitionsForNegativeNominals;
    protected final Collection<OWLDescription[]> m_conceptInclusions;
    protected final Collection<OWLObjectPropertyExpression[]> m_normalObjectPropertyInclusions;
    protected final Collection<OWLObjectPropertyExpression[]> m_inverseObjectPropertyInclusions;
    protected final Collection<OWLDataPropertyExpression[]> m_normalDataPropertyInclusions;
    protected final Collection<OWLIndividualAxiom> m_facts;
    protected final OWLDataFactory m_factory;
    
    public OwlNormalization(OWLDataFactory factory) {
        m_definitions=new HashMap<OWLDescription,OWLDescription>();
        m_definitionsForNegativeNominals=new HashMap<OWLObjectOneOf,OWLClass>();
        m_conceptInclusions=new ArrayList<OWLDescription[]>();
        m_normalObjectPropertyInclusions=new ArrayList<OWLObjectPropertyExpression[]>(); 
        m_inverseObjectPropertyInclusions=new ArrayList<OWLObjectPropertyExpression[]>();
        m_normalDataPropertyInclusions=new ArrayList<OWLDataPropertyExpression[]>();
        m_facts=new HashSet<OWLIndividualAxiom>();
        m_factory = factory;
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
    protected OWLDescription simplify(OWLDescription d) {
        return d.accept(new SimplificationVisitor(m_factory));
    }
    protected boolean isSimple(OWLDescription description) {
        return description instanceof OWLClass ||
                (description instanceof OWLObjectComplementOf &&
                ((OWLObjectComplementOf)description).getOperand() instanceof OWLClass);
    }

    public void processOntology(OWLOntology OWLOntology) throws OWLException {
        RoleManager roleManager=new RoleManager();
        List<OWLDescription[]> inclusions=new ArrayList<OWLDescription[]>();
        for (OWLAxiom untyped_axiom : OWLOntology.getAxioms()) {
            if (untyped_axiom instanceof OWLInverseObjectPropertiesAxiom) {
                OWLInverseObjectPropertiesAxiom axiom = (OWLInverseObjectPropertiesAxiom)(untyped_axiom);
                OWLObjectPropertyExpression first=axiom.getFirstProperty().getSimplified();
                OWLObjectPropertyExpression second=axiom.getSecondProperty().getSimplified();
                roleManager.addInclusion(first,second.getInverseProperty().getSimplified());
                roleManager.addInclusion(second,first.getInverseProperty().getSimplified());
                m_inverseObjectPropertyInclusions.add(new OWLObjectPropertyExpression[] { first,second  });
                m_inverseObjectPropertyInclusions.add(new OWLObjectPropertyExpression[] { second,first  });
			} else if (untyped_axiom instanceof OWLObjectSubPropertyAxiom) {
                OWLObjectSubPropertyAxiom axiom = (OWLObjectSubPropertyAxiom)(untyped_axiom);
                OWLObjectPropertyExpression subObjectProperty=axiom.getSubProperty().getSimplified();
                OWLObjectPropertyExpression superObjectProperty=axiom.getSuperProperty().getSimplified();
                roleManager.addInclusion(subObjectProperty,superObjectProperty);
                m_normalObjectPropertyInclusions.add(new OWLObjectPropertyExpression[] { subObjectProperty,superObjectProperty });
			} else if (untyped_axiom instanceof OWLEquivalentObjectPropertiesAxiom) {
                OWLEquivalentObjectPropertiesAxiom axiom = (OWLEquivalentObjectPropertiesAxiom)(untyped_axiom);
                OWLObjectPropertyExpression[] objectProperties=new OWLObjectPropertyExpression[axiom.getProperties().size()];
                axiom.getProperties().toArray(objectProperties);
                for (int i=0;i<objectProperties.length;i++)
                    objectProperties[i]=objectProperties[i].getSimplified();
                for (int i=0;i<objectProperties.length-1;i++) {
                    roleManager.addInclusion(objectProperties[i],objectProperties[i+1]);
                    roleManager.addInclusion(objectProperties[i+1],objectProperties[i]);
                    m_normalObjectPropertyInclusions.add(new OWLObjectPropertyExpression[] { objectProperties[i],objectProperties[i+1] });
                    m_normalObjectPropertyInclusions.add(new OWLObjectPropertyExpression[] { objectProperties[i+1],objectProperties[i] });
                }
			} else if (untyped_axiom instanceof OWLDataSubPropertyAxiom) {
                OWLDataSubPropertyAxiom axiom = (OWLDataSubPropertyAxiom)(untyped_axiom);
                OWLDataPropertyExpression subDataProperty=axiom.getSubProperty();
                OWLDataPropertyExpression superDataProperty=axiom.getSuperProperty();
                m_normalDataPropertyInclusions.add(new OWLDataPropertyExpression[] { subDataProperty,superDataProperty });
			} else if (untyped_axiom instanceof OWLEquivalentDataPropertiesAxiom) {
                OWLEquivalentDataPropertiesAxiom axiom = (OWLEquivalentDataPropertiesAxiom)(untyped_axiom);
                OWLDataPropertyExpression[] dataProperties=new OWLDataPropertyExpression[axiom.getProperties().size()];
                axiom.getProperties().toArray(dataProperties);
                for (int i=0;i<dataProperties.length-1;i++) {
                    m_normalDataPropertyInclusions.add(new OWLDataPropertyExpression[] { dataProperties[i],dataProperties[i+1] });
                    m_normalDataPropertyInclusions.add(new OWLDataPropertyExpression[] { dataProperties[i+1],dataProperties[i] });
                }
			} else if (untyped_axiom instanceof OWLSubClassAxiom) {
                OWLSubClassAxiom axiom = (OWLSubClassAxiom)(untyped_axiom);
                inclusions.add(new OWLDescription[] { axiom.getSubClass().getComplementNNF(),axiom.getSuperClass().getNNF() });
			} else if (untyped_axiom instanceof OWLEquivalentClassesAxiom) {
                OWLEquivalentClassesAxiom axiom = (OWLEquivalentClassesAxiom)(untyped_axiom);
                OWLDescription[] descriptions=new OWLDescription[axiom.getDescriptions().size()];
                axiom.getDescriptions().toArray(descriptions);
                for (int i=0;i<descriptions.length-1;i++) {
                    inclusions.add(new OWLDescription[] { descriptions[i].getComplementNNF(),descriptions[i+1].getNNF() });
                    inclusions.add(new OWLDescription[] { descriptions[i+1].getComplementNNF(),descriptions[i].getNNF() });
                }
			} else if (untyped_axiom instanceof OWLDisjointClassesAxiom) {
                OWLDisjointClassesAxiom axiom = (OWLDisjointClassesAxiom)(untyped_axiom);
                OWLDescription[] descriptions=new OWLDescription[axiom.getDescriptions().size()];
                axiom.getDescriptions().toArray(descriptions);
                for (int i=0;i<descriptions.length;i++)
                    descriptions[i]=descriptions[i].getComplementNNF();
                for (int i=0;i<descriptions.length;i++)
                    for (int j=i+1;j<descriptions.length;j++)
                        inclusions.add(new OWLDescription[] { descriptions[i],descriptions[j] });
			} else if (untyped_axiom instanceof OWLFunctionalObjectPropertyAxiom) {
                OWLFunctionalObjectPropertyAxiom axiom = (OWLFunctionalObjectPropertyAxiom)(untyped_axiom);
                inclusions.add(new OWLDescription[] { m_factory.getOWLObjectMaxCardinalityRestriction(axiom.getProperty().getSimplified(),1) });
			} else if (untyped_axiom instanceof OWLInverseFunctionalObjectPropertyAxiom) {
                OWLInverseFunctionalObjectPropertyAxiom axiom = (OWLInverseFunctionalObjectPropertyAxiom)(untyped_axiom);
                inclusions.add(new OWLDescription[] { m_factory.getOWLObjectMaxCardinalityRestriction(axiom.getProperty().getSimplified().getInverseProperty(),1) });
			} else if (untyped_axiom instanceof OWLSymmetricObjectPropertyAxiom) {
                OWLSymmetricObjectPropertyAxiom axiom = (OWLSymmetricObjectPropertyAxiom)(untyped_axiom);
                OWLObjectPropertyExpression objectProperty=axiom.getProperty().getSimplified();
                m_inverseObjectPropertyInclusions.add(new OWLObjectPropertyExpression[] { objectProperty,objectProperty });
                roleManager.addInclusion(objectProperty,objectProperty.getInverseProperty().getSimplified());
			} else if (untyped_axiom instanceof OWLTransitiveObjectPropertyAxiom) {
                OWLTransitiveObjectPropertyAxiom axiom = (OWLTransitiveObjectPropertyAxiom)(untyped_axiom);
                roleManager.makeTransitive(axiom.getProperty().getSimplified());
			} else if (untyped_axiom instanceof OWLFunctionalDataPropertyAxiom) {
                OWLFunctionalDataPropertyAxiom axiom = (OWLFunctionalDataPropertyAxiom)(untyped_axiom);
                inclusions.add(new OWLDescription[] { m_factory.getOWLDataMaxCardinalityRestriction(axiom.getProperty(),1) });
			} else if (untyped_axiom instanceof OWLObjectPropertyDomainAxiom) {
                OWLObjectPropertyDomainAxiom axiom = (OWLObjectPropertyDomainAxiom)(untyped_axiom);
                OWLObjectAllRestriction allPropertyNohting=m_factory.getOWLObjectAllRestriction(axiom.getProperty().getSimplified(),m_factory.getOWLNothing());
                inclusions.add(new OWLDescription[] { axiom.getDomain(),allPropertyNohting });
			} else if (untyped_axiom instanceof OWLObjectPropertyRangeAxiom) {
                OWLObjectPropertyRangeAxiom axiom = (OWLObjectPropertyRangeAxiom)(untyped_axiom);
                OWLObjectAllRestriction allPropertyRange=m_factory.getOWLObjectAllRestriction(axiom.getProperty().getSimplified(),axiom.getRange().getNNF());
                inclusions.add(new OWLDescription[] { allPropertyRange });
			} else if (untyped_axiom instanceof OWLDataPropertyDomainAxiom) {
                OWLDataPropertyDomainAxiom axiom = (OWLDataPropertyDomainAxiom)(untyped_axiom);
                OWLDataRange dataNothing = m_factory.getOWLDataComplementOf(m_factory.getOWLDataType(URI.create("http://www.w3.org/2000/01/rdf-schema#Literal")));
                OWLDataAllRestriction allPropertyNohting=m_factory.getOWLDataAllRestriction(axiom.getProperty(),dataNothing);
                inclusions.add(new OWLDescription[] { axiom.getDomain(),allPropertyNohting });
			} else if (untyped_axiom instanceof OWLDataPropertyRangeAxiom) {
                OWLDataPropertyRangeAxiom axiom = (OWLDataPropertyRangeAxiom)(untyped_axiom);
                OWLDataAllRestriction allPropertyRange=m_factory.getOWLDataAllRestriction(axiom.getProperty(),axiom.getRange());
                inclusions.add(new OWLDescription[] { allPropertyRange });
			} else if (untyped_axiom instanceof OWLAntiSymmetricObjectPropertyAxiom) {
                OWLAntiSymmetricObjectPropertyAxiom axiom = (OWLAntiSymmetricObjectPropertyAxiom)(untyped_axiom);
			    throw new RuntimeException("OWL 2.0 role axioms are not yet supported."); // until Birte changes this...
			} else if (untyped_axiom instanceof OWLDisjointDataPropertiesAxiom) {
                OWLDisjointDataPropertiesAxiom axiom = (OWLDisjointDataPropertiesAxiom)(untyped_axiom);
			    throw new RuntimeException("OWL 2.0 role axioms are not yet supported."); // until Birte changes this...
			} else if (untyped_axiom instanceof OWLDisjointObjectPropertiesAxiom) {
                OWLDisjointObjectPropertiesAxiom axiom = (OWLDisjointObjectPropertiesAxiom)(untyped_axiom);
			    throw new RuntimeException("OWL 2.0 role axioms are not yet supported."); // until Birte changes this...
			} else if (untyped_axiom instanceof OWLIrreflexiveObjectPropertyAxiom) {
                OWLIrreflexiveObjectPropertyAxiom axiom = (OWLIrreflexiveObjectPropertyAxiom)(untyped_axiom);
			    throw new RuntimeException("OWL 2.0 role axioms are not yet supported."); // until Birte changes this...
			} else if (untyped_axiom instanceof OWLReflexiveObjectPropertyAxiom) {
                OWLReflexiveObjectPropertyAxiom axiom = (OWLReflexiveObjectPropertyAxiom)(untyped_axiom);
			    throw new RuntimeException("OWL 2.0 role axioms are not yet supported."); // until Birte changes this...
			} else if (untyped_axiom instanceof OWLClassAssertionAxiom) {
                OWLClassAssertionAxiom axiom = (OWLClassAssertionAxiom)(untyped_axiom);
                OWLDescription desc=simplify(axiom.getDescription().getNNF());
                if (!isSimple(desc)) {
                    boolean[] alreadyExists=new boolean[1];
                    OWLDescription definition=getDefinitionFor(desc,alreadyExists);
                    if (!alreadyExists[0])
                        inclusions.add(new OWLDescription[] { definition.getComplementNNF(),desc });
                    desc=definition;
                }
                if (desc==axiom.getDescription())
                    m_facts.add(axiom);
                else
                    m_facts.add(m_factory.getOWLClassAssertionAxiom(axiom.getIndividual(),desc));
			} else if (untyped_axiom instanceof OWLObjectPropertyAssertionAxiom) {
                OWLObjectPropertyAssertionAxiom axiom = (OWLObjectPropertyAssertionAxiom)(untyped_axiom);
                m_facts.add(m_factory.getOWLObjectPropertyAssertionAxiom(axiom.getSubject(),axiom.getProperty().getSimplified(),axiom.getObject()));
			} else if (untyped_axiom instanceof OWLSameIndividualsAxiom) {
                OWLSameIndividualsAxiom axiom = (OWLSameIndividualsAxiom)(untyped_axiom);
                m_facts.add(axiom);
			} else if (untyped_axiom instanceof OWLDifferentIndividualsAxiom) {
                OWLDifferentIndividualsAxiom axiom = (OWLDifferentIndividualsAxiom)(untyped_axiom);
                m_facts.add(axiom);
            } else if (untyped_axiom instanceof OWLAxiomAnnotationAxiom ||
                       untyped_axiom instanceof OWLEntityAnnotationAxiom ||
                       untyped_axiom instanceof OWLOntologyAnnotationAxiom ||
                       untyped_axiom instanceof OWLDeclarationAxiom ||
                       untyped_axiom instanceof OWLImportsDeclaration) {
                // do nothing; these axiom types have no effect on reasoning
            } else if (untyped_axiom instanceof OWLDataPropertyAssertionAxiom) {
                // In a way we should throw an exception here, but we've been allowing it through
                // for testing purposes, so just issue a warning right now.
                // TODO: Either actually implement datatypes, or throw an exception here!
                System.err.println("ignoring data assertion...");
            } else {
                throw new RuntimeException("Unsupported axiom type:" + untyped_axiom.getAxiomType().toString());
            }
        }
        OWLDescriptionVisitorEx<OWLDescription> normalizer=new NormalizationVisitor(inclusions, this);
        nomalizeInclusions(inclusions,normalizer);
        roleManager.rewriteConceptInclusions(m_conceptInclusions, m_factory);
    }
    protected void nomalizeInclusions(List<OWLDescription[]> inclusions,OWLDescriptionVisitorEx<OWLDescription> normalizer) throws OWLException {
        while (!inclusions.isEmpty()) {
            OWLDescription simplifiedDescription=simplify(m_factory.getOWLObjectUnionOf(inclusions.remove(inclusions.size()-1)));
            if (!simplifiedDescription.isOWLThing()) {
                if (simplifiedDescription instanceof OWLObjectUnionOf) {
                    OWLObjectUnionOf objectOr=(OWLObjectUnionOf)simplifiedDescription;
                    OWLDescription[] descriptions=new OWLDescription[objectOr.getOperands().size()];
                    objectOr.getOperands().toArray(descriptions);
                    if (!distributeUnionOverAnd(descriptions,inclusions) && !optimizedNegativeOneOfTranslation(descriptions,inclusions)) {
                        for (int index=0;index<descriptions.length;index++)
                            descriptions[index]=(OWLDescription)descriptions[index].accept(normalizer);
                        m_conceptInclusions.add(descriptions);
                    }
                }
                else if (simplifiedDescription instanceof OWLObjectIntersectionOf) {
                    OWLObjectIntersectionOf objectAnd=(OWLObjectIntersectionOf)simplifiedDescription;
                    for (OWLDescription conjunct : objectAnd.getOperands())
                        inclusions.add(new OWLDescription[] { conjunct });
                }
                else {
                    OWLDescription normalized=simplifiedDescription.accept(normalizer);
                    m_conceptInclusions.add(new OWLDescription[] { normalized });
                }
            }
        }
        
    }
    protected boolean optimizedNegativeOneOfTranslation(OWLDescription[] descriptions,List<OWLDescription[]> inclusions) {
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
                for (OWLIndividual individual : nominal.getIndividuals())
                    m_facts.add(m_factory.getOWLClassAssertionAxiom(individual,other));
                return true;
            }
        }
        return false;
    }
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
    protected OWLDescription getDefinitionFor(OWLDescription desc,boolean[] alreadyExists) {
        OWLDescription definition=m_definitions.get(desc);
        if (definition==null) {
            definition=m_factory.getOWLClass(URI.create("internal:q#"+m_definitions.size()));
            if (!desc.accept(PLVisitor.INSTANCE))
                definition=m_factory.getOWLObjectComplementOf(definition);
            m_definitions.put(desc,definition);
            alreadyExists[0]=false;
        }
        else
            alreadyExists[0]=true;
        return definition;
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
    protected class NormalizationVisitor implements OWLDescriptionVisitorEx<OWLDescription> {
        protected final Collection<OWLDescription[]> m_newInclusions;
        protected final boolean[] m_alreadyExists;
		OwlNormalization m_n;
    
        public NormalizationVisitor(Collection<OWLDescription[]> newInclusions, OwlNormalization n) {
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
                    m_newInclusions.add(new OWLDescription[] { definition.getComplementNNF(),description });
                return m_factory.getOWLObjectAllRestriction(object.getProperty().getSimplified(),definition);
            }
        }
        public OWLDescription visit(OWLObjectSomeRestriction object) {
            OWLDescription description=object.getFiller();
            if (isSimple(description) || description instanceof OWLObjectOneOf) // The ObjectOneOf case is an optimization.
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
            OWLDescription description=object.getFiller();
            OWLDescription definition=getDefinitionFor(object,m_alreadyExists);
            if (!m_alreadyExists[0]) {
                m_newInclusions.add(new OWLDescription[] { definition.getComplementNNF(),m_n.m_factory.getOWLObjectExactCardinalityRestriction(objectProperty,object.getCardinality(),description) });
            }
            return definition;
        }
        public OWLDescription visit(OWLObjectMinCardinalityRestriction object) {
            OWLObjectPropertyExpression objectProperty=object.getProperty().getSimplified();
            OWLDescription description=object.getFiller();
            if (object.getCardinality()<=0) return m_n.m_factory.getOWLThing();
            else if (isSimple(description)) return object;
            else if (object.getCardinality()==1 && description instanceof OWLObjectOneOf) {  // This is an optimization
                return m_n.m_factory.getOWLObjectSomeRestriction(objectProperty,description);
            } else {
                OWLDescription definition=getDefinitionFor(description,m_alreadyExists);
                if (!m_alreadyExists[0])
                    m_newInclusions.add(new OWLDescription[] { definition.getComplementNNF(),description });
                return m_n.m_factory.getOWLObjectMinCardinalityRestriction(objectProperty,object.getCardinality(),definition);
            }
        }
        public OWLDescription visit(OWLObjectMaxCardinalityRestriction object) {
            OWLObjectPropertyExpression objectProperty=object.getProperty().getSimplified();
            OWLDescription description=object.getFiller();
            if (object.getCardinality()<=0) {
                return m_n.m_factory.getOWLObjectAllRestriction(objectProperty,description.getComplementNNF()).accept(this);
            } else if (isSimple(description)) return object;
            else {
                OWLDescription complementDescription=description.getComplementNNF();
                OWLDescription definition=getDefinitionFor(complementDescription,m_alreadyExists);
                if (!m_alreadyExists[0])
                    m_newInclusions.add(new OWLDescription[] { definition.getComplementNNF(),complementDescription });
                return m_n.m_factory.getOWLObjectMaxCardinalityRestriction(objectProperty,object.getCardinality(),definition.getComplementNNF());
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
                    m_newInclusions.add(new OWLDescription[] { definition.getComplementNNF(),description });
            }
            return definition;
        }
        public OWLDescription visit(OWLObjectOneOf object) {
            return object;
        }
        public OWLDescription visit(OWLObjectValueRestriction object) {
            OWLObjectOneOf objectOneOf=m_n.m_factory.getOWLObjectOneOf(object.getValue());
            return m_n.m_factory.getOWLObjectSomeRestriction(object.getProperty().getSimplified(),objectOneOf);
        }
        public OWLDescription visit(OWLObjectSelfRestriction object) {
			throw new RuntimeException("Reflexivity is not supported yet.");
        }
    }
    protected static class PLVisitor implements OWLDescriptionVisitorEx<Boolean> {
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
			return object.getCardinality() > 0;
		}
		public Boolean visit(OWLObjectMaxCardinalityRestriction object) {
			return object.getCardinality() > 0 ? Boolean.TRUE : object.getFiller().getComplementNNF().accept(this);
		}
		public Boolean visit(OWLObjectExactCardinalityRestriction object) {
			return object.getCardinality() > 0 ? Boolean.TRUE : object.getFiller().getComplementNNF().accept(this);
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

    protected class RoleManager {
        protected final Map<OWLObjectPropertyExpression,Set<OWLObjectPropertyExpression>> m_subObjectProperties;
        protected final Set<OWLObjectPropertyExpression> m_transitiveObjectProperties;
        protected final Map<OWLObjectAllRestriction,OWLDescription> m_replacedDescriptions;
    
        public RoleManager() {
            m_subObjectProperties=new HashMap<OWLObjectPropertyExpression,Set<OWLObjectPropertyExpression>>();
            m_transitiveObjectProperties=new HashSet<OWLObjectPropertyExpression>();
            m_replacedDescriptions=new HashMap<OWLObjectAllRestriction,OWLDescription>();
        }
        public void addInclusion(OWLObjectPropertyExpression subObjectProperty,OWLObjectPropertyExpression superObjectProperty) {
            addInclusionEx(subObjectProperty.getSimplified(),superObjectProperty.getSimplified());
            addInclusionEx(subObjectProperty.getInverseProperty().getSimplified(),superObjectProperty.getInverseProperty().getSimplified());
        }
        public void makeTransitive(OWLObjectPropertyExpression objectProperty) {
            m_transitiveObjectProperties.add(objectProperty.getSimplified());
            m_transitiveObjectProperties.add(objectProperty.getInverseProperty().getSimplified());
        }
        public void rewriteConceptInclusions(Collection<OWLDescription[]> inclusions, OWLDataFactory factory) {
            transitivelyClose();
            for (OWLDescription[] inclusion : inclusions) {
                for (int index=0; index<inclusion.length; index++) {
                    inclusion[index] = replaceDescriptionIfNecessary(inclusion[index], factory);
                }
            }
            for (Map.Entry<OWLObjectAllRestriction,OWLDescription> replacement : m_replacedDescriptions.entrySet()) {
                m_conceptInclusions.add(new OWLDescription[] { replacement.getValue().getComplementNNF(),replacement.getKey() });
                OWLObjectPropertyExpression objectProperty=replacement.getKey().getProperty();
                for (OWLObjectPropertyExpression transitiveSubObjectProperty : getTransitiveSubObjectProperties(objectProperty)) {
                    OWLObjectAllRestriction consequentAll=factory.getOWLObjectAllRestriction(transitiveSubObjectProperty,replacement.getKey().getFiller());
                    OWLDescription consequentReplacement=m_replacedDescriptions.get(consequentAll);
                    assert consequentReplacement!=null;
                    OWLObjectAllRestriction forallConsequentReplacement=factory.getOWLObjectAllRestriction(transitiveSubObjectProperty,consequentReplacement);
                    m_conceptInclusions.add(new OWLDescription[] { replacement.getValue().getComplementNNF(),forallConsequentReplacement });
                }
            }
            m_replacedDescriptions.clear();
        }
        protected void addInclusionEx(OWLObjectPropertyExpression subObjectProperty,OWLObjectPropertyExpression superObjectProperty) {
            Set<OWLObjectPropertyExpression> subObjectProperties=m_subObjectProperties.get(superObjectProperty);
            if (subObjectProperties==null) {
                subObjectProperties=new HashSet<OWLObjectPropertyExpression>();
                m_subObjectProperties.put(superObjectProperty,subObjectProperties);
            }
            subObjectProperties.add(subObjectProperty);
        }
        public OWLDescription replaceDescriptionIfNecessary(OWLDescription desc, OWLDataFactory factory) {
            if (desc instanceof OWLObjectAllRestriction) {
                OWLObjectAllRestriction objectAll=(OWLObjectAllRestriction)desc;
                OWLObjectPropertyExpression objectProperty=objectAll.getProperty();
                Set<OWLObjectPropertyExpression> transitiveSubObjectProperties=getTransitiveSubObjectProperties(objectProperty);
                if (!transitiveSubObjectProperties.isEmpty()) {
                    OWLDescription replacement=getReplacementFor(objectAll, factory);
                    for (OWLObjectPropertyExpression transitiveSubObjectProperty : transitiveSubObjectProperties) {
                        OWLObjectAllRestriction subObjectAll=factory.getOWLObjectAllRestriction(transitiveSubObjectProperty,objectAll.getFiller());
                        getReplacementFor(subObjectAll, factory);
                    }
                    return replacement;
                }
            }
            return desc;
        }
        protected OWLDescription getReplacementFor(OWLObjectAllRestriction objectAll, OWLDataFactory factory) {
            OWLDescription replacement=m_replacedDescriptions.get(objectAll);
            if (replacement==null) {
                replacement=factory.getOWLClass(URI.create("internal:all#"+m_replacedDescriptions.size()));
                if (objectAll.getFiller() instanceof OWLObjectComplementOf) replacement = replacement.getComplementNNF();
                m_replacedDescriptions.put(objectAll,replacement);
            }
            return replacement;
        }
        protected void transitivelyClose() {
            boolean changed=true;
            List<OWLObjectPropertyExpression> temporary=new ArrayList<OWLObjectPropertyExpression>();
            while (changed) {
                changed=false;
                for (Map.Entry<OWLObjectPropertyExpression,Set<OWLObjectPropertyExpression>> entry : m_subObjectProperties.entrySet()) {
                    temporary.clear();
                    temporary.addAll(entry.getValue());
                    for (int i=temporary.size()-1;i>=0;--i) {
                        Set<OWLObjectPropertyExpression> subObjectProperties=m_subObjectProperties.get(temporary.get(i));
                        if (subObjectProperties!=null)
                            if (entry.getValue().addAll(subObjectProperties))
                                changed=true;
                    }
                }
            }
        }
        protected Set<OWLObjectPropertyExpression> getTransitiveSubObjectProperties(OWLObjectPropertyExpression objectProperty) {
            Set<OWLObjectPropertyExpression> result=new HashSet<OWLObjectPropertyExpression>();
            if (m_transitiveObjectProperties.contains(objectProperty))
                result.add(objectProperty);
            Set<OWLObjectPropertyExpression> subObjectProperties=m_subObjectProperties.get(objectProperty);
            if (subObjectProperties != null)
                for (OWLObjectPropertyExpression subObjectProperty : subObjectProperties)
                    if (m_transitiveObjectProperties.contains(subObjectProperty))
                        result.add(subObjectProperty);
            return result;
        }
    }
    protected static class SimplificationVisitor implements OWLDescriptionVisitorEx<OWLDescription> {
		protected final OWLDataFactory factory;
		
		public SimplificationVisitor(OWLDataFactory f) { factory = f; }
    
        public OWLDescription visit(OWLClass d) { return d; }
        public OWLDescription visit(OWLObjectAllRestriction d) {
            if (d.getFiller().isOWLThing()) return factory.getOWLThing();
            return factory.getOWLObjectAllRestriction(d.getProperty().getSimplified(), d.getFiller().accept(this));
        }
        public OWLDescription visit(OWLObjectSomeRestriction d) {
            if (d.getFiller().isOWLNothing()) return factory.getOWLNothing();
            return factory.getOWLObjectSomeRestriction(d.getProperty().getSimplified(), d.getFiller().accept(this));
        }
        public OWLDescription visit(OWLObjectMinCardinalityRestriction d) {
            if (d.getCardinality() <= 0) return factory.getOWLThing();
            if (d.getFiller().isOWLNothing()) return factory.getOWLNothing();
            return factory.getOWLObjectMinCardinalityRestriction(d.getProperty().getSimplified(), d.getCardinality(), d.getFiller().accept(this));
		}
		public OWLDescription visit(OWLObjectMaxCardinalityRestriction d) {
            if (d.getFiller().isOWLNothing()) return factory.getOWLThing();
            if (d.getCardinality() <= 0) {
                return factory.getOWLObjectAllRestriction(d.getProperty().getSimplified(), factory.getOWLObjectComplementOf(d.getFiller()).accept(this));
            }
            return factory.getOWLObjectMaxCardinalityRestriction(d.getProperty().getSimplified(), d.getCardinality(), d.getFiller().accept(this));
		}
		public OWLDescription visit(OWLObjectExactCardinalityRestriction d) {
            if (d.getCardinality() < 0) return factory.getOWLNothing();
            if (d.getCardinality() == 0) return factory.getOWLObjectAllRestriction(d.getProperty().getSimplified(), factory.getOWLObjectComplementOf(d.getFiller()).accept(this));
            if (d.getFiller().isOWLNothing()) return factory.getOWLNothing();
            return factory.getOWLObjectExactCardinalityRestriction(d.getProperty().getSimplified(), d.getCardinality(), d.getFiller().accept(this));
		}
        public OWLDescription visit(OWLObjectComplementOf d) {
	        OWLDescription s = d.getOperand().accept(this);
	        if (s instanceof OWLObjectComplementOf) {
	            return ((OWLObjectComplementOf)s).getOperand();
            }
            if (s.isOWLThing()) return factory.getOWLNothing();
            if (s.isOWLNothing()) return factory.getOWLThing();
            return factory.getOWLObjectComplementOf(s);
        }
        public OWLDescription visit(OWLObjectUnionOf d) {
            java.util.HashSet<OWLDescription> newDisjuncts = new java.util.HashSet<OWLDescription>();
    	    for (OWLDescription cur : d.getOperands()) {
    	        OWLDescription s = cur.accept(this);
    	        if (s.isOWLThing()) return s;
    	        if (s.isOWLNothing()) continue;
    	        if (s instanceof OWLObjectUnionOf) {
    	            newDisjuncts.addAll(((OWLObjectUnionOf)s).getOperands());
    	        } else newDisjuncts.add(s);
    	    }
    	    return factory.getOWLObjectUnionOf(newDisjuncts);
        }
        public OWLDescription visit(OWLObjectIntersectionOf d) {
            java.util.HashSet<OWLDescription> newConjuncts = new java.util.HashSet<OWLDescription>();
    	    for (OWLDescription cur : d.getOperands()) {
    	        OWLDescription s = cur.accept(this);
    	        if (s.isOWLThing()) continue;
    	        if (s.isOWLNothing()) return s;
    	        if (s instanceof OWLObjectIntersectionOf) {
    	            newConjuncts.addAll(((OWLObjectIntersectionOf)s).getOperands());
    	        } else newConjuncts.add(s);
    	    }
    	    return factory.getOWLObjectIntersectionOf(newConjuncts);
        }
        public OWLDescription visit(OWLObjectOneOf d) { return d; }
        public OWLDescription visit(OWLObjectValueRestriction d) { return d; }
        public OWLDescription visit(OWLObjectSelfRestriction d) { return d; }
		public OWLDescription visit(OWLDataAllRestriction d) { return d; }
		public OWLDescription visit(OWLDataExactCardinalityRestriction d) { return d; }
		public OWLDescription visit(OWLDataMaxCardinalityRestriction d) { return d; }
		public OWLDescription visit(OWLDataMinCardinalityRestriction d) { return d; }
		public OWLDescription visit(OWLDataSomeRestriction d) { return d; }
		public OWLDescription visit(OWLDataValueRestriction d) { return d; }
    }
}
