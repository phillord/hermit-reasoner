package org.semanticweb.HermiT.hierarchy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.Individual;
import org.semanticweb.HermiT.model.InverseRole;
import org.semanticweb.HermiT.model.Role;
import org.semanticweb.HermiT.tableau.ExtensionTable;
import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.HermiT.tableau.NodeType;
import org.semanticweb.HermiT.tableau.Tableau;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

public class PropertyRelationFinder {
    
    protected final Map<Individual,Node> m_nodesForIndividuals;
    
    public PropertyRelationFinder() {
        m_nodesForIndividuals=new HashMap<Individual,Node>();
    }
    
    public Map<Individual,Node> getNodesForIndividuals() {
       return m_nodesForIndividuals;
    }
    
    public OWLAxiom[] getAxiomsForReadingOffCompexProperties(Set<Role> complexObjectRoles, Set<Individual> individuals, OWLDataFactory factory) {
        List<OWLAxiom> additionalAxioms=new ArrayList<OWLAxiom>();
        for (Individual ind : individuals) {
            if (!ind.isAnonymous()) {
                String indIRI=ind.getIRI();
                m_nodesForIndividuals.put(ind,null);
//                OWLAxiom axiom=factory.getOWLClassAssertionAxiom(factory.getOWLThing(),factory.getOWLNamedIndividual(IRI.create(indIRI)));
//                additionalAxioms.add(axiom);
                if (!complexObjectRoles.isEmpty()) {
                    OWLClass classForIndividual=factory.getOWLClass(IRI.create("internal:individual-concept#"+indIRI));
                    OWLAxiom axiom=factory.getOWLClassAssertionAxiom(classForIndividual,factory.getOWLNamedIndividual(IRI.create(indIRI)));
                    additionalAxioms.add(axiom);
                    AtomicConcept conceptForRole;
                    OWLObjectPropertyExpression objectPropertyExpression;
                    for (Role objectRole : complexObjectRoles) {
                        if (objectRole instanceof AtomicRole) {
                            conceptForRole=AtomicConcept.create("internal:individual-concept#"+((AtomicRole)objectRole).getIRI()+"#"+indIRI);
                            objectPropertyExpression=factory.getOWLObjectProperty(IRI.create(((AtomicRole)objectRole).getIRI()));
                        }
                        else {
                            conceptForRole=AtomicConcept.create("internal:individual-concept#inv#"+((InverseRole)objectRole).getInverseOf().getIRI()+"#"+indIRI);
                            objectPropertyExpression=factory.getOWLObjectInverseOf(factory.getOWLObjectProperty(IRI.create(((InverseRole)objectRole).getInverseOf().getIRI())));
                        }
                        OWLClass classForRoleAndIndividual=factory.getOWLClass(IRI.create(conceptForRole.getIRI()));
                        // A_a implies forall r.A_a^r
                        axiom=factory.getOWLSubClassOfAxiom(classForIndividual,factory.getOWLObjectAllValuesFrom(objectPropertyExpression,classForRoleAndIndividual));
                        additionalAxioms.add(axiom);
                        // A_a^r implies forall r.A_a^r
                        axiom=factory.getOWLSubClassOfAxiom(classForRoleAndIndividual,factory.getOWLObjectAllValuesFrom(objectPropertyExpression,classForRoleAndIndividual));
                        additionalAxioms.add(axiom);
                    }
                }
            }
        }
        OWLAxiom[] additionalAxiomsArray=new OWLAxiom[additionalAxioms.size()];
        return additionalAxioms.toArray(additionalAxiomsArray);
    }
    
    public void readOfPossibleAndKnowRelations(Tableau tableau, Set<Role> complexObjectRoles, Set<AtomicRole> objectRoles, Set<Individual> individuals, Map<AtomicRole,Map<Individual,Set<Individual>>> knownObjectPropertyRelations, Map<AtomicRole,Map<Individual,Set<Individual>>> possibleObjectPropertyRelations) {
        Map<Node,Individual> individualsForNodes=new HashMap<Node,Individual>();
        for (Entry<Individual, Node> indToNode : m_nodesForIndividuals.entrySet()) {
            individualsForNodes.put(indToNode.getValue().getCanonicalNode(), indToNode.getKey());
        }
        for (Individual ind : individualsForNodes.values()) {
            if (!ind.isAnonymous()) {
                ExtensionTable.Retrieval retrieval=tableau.getExtensionManager().getTernaryExtensionTable().createRetrieval(new boolean[] { false,true,false },ExtensionTable.View.TOTAL);
                retrieval.getBindingsBuffer()[1]=m_nodesForIndividuals.get(ind);
                retrieval.open();
                Object[] tupleBuffer=retrieval.getTupleBuffer();
                while (!retrieval.afterLast()) {
                    Object roleObject=tupleBuffer[0];
                    if (roleObject instanceof AtomicRole) {
                        AtomicRole atomicrole=(AtomicRole)roleObject;
                        Node node2=(Node)tupleBuffer[2];
                        if (node2.isActive() && node2.getNodeType()==NodeType.NAMED_NODE && individualsForNodes.containsKey(node2)) {
                            Individual successor=individualsForNodes.get(node2);
                            Map<AtomicRole,Map<Individual,Set<Individual>>> relevantRelations;
                            if (retrieval.getDependencySet().isEmpty())
                                relevantRelations=knownObjectPropertyRelations;
                            else
                                relevantRelations=possibleObjectPropertyRelations;
                            Map<Individual,Set<Individual>> relationsForRole=relevantRelations.get(atomicrole);
                            if (relationsForRole==null) {
                                relationsForRole=new HashMap<Individual, Set<Individual>>();
                                relevantRelations.put(atomicrole, relationsForRole);
                            }
                            Set<Individual> successors=relationsForRole.get(ind);
                            if (successors==null) {
                                successors=new HashSet<Individual>();
                                relationsForRole.put(ind, successors);
                            }
                            successors.add(successor);
                        }
                    }
                    retrieval.next();
                }
                // add more possible relations for complex properties
                String indIRI=ind.getIRI();
                AtomicConcept conceptForRole;
                for (Role objectRole : complexObjectRoles) {
                    if (objectRole instanceof AtomicRole) {
                        conceptForRole=AtomicConcept.create("internal:individual-concept#"+((AtomicRole)objectRole).getIRI()+"#"+indIRI);
                    } else {
                        conceptForRole=AtomicConcept.create("internal:individual-concept#inv#"+((InverseRole)objectRole).getInverseOf().getIRI()+"#"+indIRI);
                    }
                    retrieval=tableau.getExtensionManager().getBinaryExtensionTable().createRetrieval(new boolean[] { true,false },ExtensionTable.View.TOTAL);
                    retrieval.getBindingsBuffer()[0]=conceptForRole;
                    retrieval.open();
                    tupleBuffer=retrieval.getTupleBuffer();
                    while (!retrieval.afterLast()) {
                        Node node=(Node)tupleBuffer[1];
                        if (node.isActive() && node.getNodeType()==NodeType.NAMED_NODE && individualsForNodes.containsKey(node)) {
                            AtomicRole atomicrole;
                            Individual first=ind;
                            Individual second=individualsForNodes.get(node);
                            if (objectRole instanceof AtomicRole) {
                                atomicrole=(AtomicRole)objectRole;
                            } else {
                                atomicrole=((InverseRole)objectRole).getInverseOf();
                                Individual tmp=second;
                                second=first;
                                first=tmp;
                            }
                            Map<AtomicRole,Map<Individual,Set<Individual>>> relevantRelations;
                            if (retrieval.getDependencySet().isEmpty())
                                relevantRelations=knownObjectPropertyRelations;
                            else {
                                relevantRelations=possibleObjectPropertyRelations;
                                if (knownObjectPropertyRelations.containsKey(atomicrole)&&knownObjectPropertyRelations.get(atomicrole).containsKey(first)&&knownObjectPropertyRelations.get(atomicrole).get(first).contains(second))
                                    break;
                            }
                            Map<Individual,Set<Individual>> relationsForRole=relevantRelations.get(atomicrole);
                            if (relationsForRole==null) {
                                relationsForRole=new HashMap<Individual, Set<Individual>>();
                                relevantRelations.put(atomicrole, relationsForRole);
                            }
                            Set<Individual> successors=relationsForRole.get(first);
                            if (successors==null) {
                                successors=new HashSet<Individual>();
                                relationsForRole.put(first, successors);
                            }
                            successors.add(second);
                        }
                        retrieval.next();
                    }
                }
            }
        }
    }

}
