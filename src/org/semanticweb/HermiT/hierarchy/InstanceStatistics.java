package org.semanticweb.HermiT.hierarchy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.HermiT.hierarchy.RoleElementManager.RoleElement;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.Individual;
import org.semanticweb.HermiT.model.Role;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;


public class InstanceStatistics {
    
    protected final InstanceManager m_instanceManager;
    protected final Reasoner m_reasoner;
    protected final AtomicConcept m_topConcept;
    protected final AtomicConcept m_bottomConcept;
    protected final RoleElement m_topRoleElement;
    protected final RoleElement m_bottomRoleElement;
    
    public InstanceStatistics(InstanceManager instanceManger, Reasoner reasoner) {
        m_instanceManager=instanceManger;
        m_reasoner=reasoner;
        m_topConcept=AtomicConcept.THING;
        m_bottomConcept=AtomicConcept.NOTHING;
        m_topRoleElement=instanceManger.m_roleElementManager.getRoleElement(AtomicRole.TOP_OBJECT_ROLE);
        m_bottomRoleElement=instanceManger.m_roleElementManager.getRoleElement(AtomicRole.BOTTOM_OBJECT_ROLE);
    }
    
    // *************************************    
    // methods for status of the instance manager
    // *************************************
    
    public boolean areClassesInitialised() {
        return m_instanceManager.areClassesInitialised();
    }
    public boolean arePropertiesInitialised() {
        return m_instanceManager.arePropertiesInitialised();
    }
    
    // *************************************    
    // methods for hierarchy statistics
    // *************************************
    
    public int getClassHierarchyDepth() {
        m_reasoner.classifyClasses();
        return m_instanceManager.m_currentConceptHierarchy.getDepth();
    }
    public int getObjectPropertyHierarchyDepth() {
        m_reasoner.classifyObjectProperties();
        return m_instanceManager.m_currentRoleHierarchy.getDepth();
    }
    
    // *************************************    
    // methods for class instance statistics
    // *************************************
    
    public int[] getNumberOfInstances(OWLClass owlClass) {
        if (m_instanceManager.m_individuals.length==0)
            return new int[] { 0,0 };
        else {
            AtomicConcept concept=H(owlClass);
            return getNumberOfConceptInstances(concept);
        }
    }
    protected int[] getNumberOfConceptInstances(AtomicConcept concept) {
        int[] result=new int[2];
        HierarchyNode<AtomicConcept> node=m_instanceManager.m_currentConceptHierarchy.getNodeForElement(concept);
        if (node==null)
            return result; // unknown concept
        AtomicConcept representative=node.getRepresentative();
        if (representative.equals(m_bottomConcept))
            return result;
        if (representative.equals(m_topConcept)) {
            result[0]=m_instanceManager.m_individuals.length;
            return result;
        }
        return getNumberOfConceptInstances(node,result);
    }
    protected int[] getNumberOfConceptInstances(HierarchyNode<AtomicConcept> node,int[] result) {
        AtomicConceptElement representativeElement=m_instanceManager.m_conceptToElement.get(node.getRepresentative());
        if (representativeElement!=null) {
            result[0]+=representativeElement.getKnownInstances().size();
            result[1]+=representativeElement.getPossibleInstances().size();
        }
        for (HierarchyNode<AtomicConcept> child : node.getChildNodes())
            if (child!=m_instanceManager.m_currentConceptHierarchy.m_bottomNode)
                getNumberOfConceptInstances(child,result);
        return result;
    }
    public int[] getNumberOfTypes(OWLIndividual individual) {
        m_reasoner.classifyClasses();
        Individual ind=H(individual);
        return getNumberOfTypes(ind);
    }
    protected int[] getNumberOfTypes(Individual individual) {
        int[] result=new int[2];
        DFS<AtomicConcept> dfs=new DFS<AtomicConcept>(m_instanceManager.m_currentConceptHierarchy.m_bottomNode);
        DFSTypeCounter typeCounter=new DFSTypeCounter(individual);
        dfs.traverse(m_instanceManager.m_currentConceptHierarchy.m_topNode, typeCounter);
        result[0]=typeCounter.types;
        if (result[0]==0) result[0]=1; // individuals with only owl:Thing as type are not stored, but all individuals have at least 1 type (owl:Thing)
        return result;
    }
    public boolean[] isKnownOrPossibleInstance(OWLClass owlClass, OWLNamedIndividual individual) {
        AtomicConcept concept=H(owlClass);
        Individual ind=H(individual);
        return isKnownOrPossibleConceptInstance(concept, ind);
    }
    protected boolean[] isKnownOrPossibleConceptInstance(AtomicConcept concept, Individual individual) {
        HierarchyNode<AtomicConcept> node=m_instanceManager.m_currentConceptHierarchy.getNodeForElement(concept);
        if (node==null)
            return new boolean[] { false, false }; // unknown concept
        AtomicConcept representative=node.getRepresentative();
        if (representative.equals(m_topConcept))
            return new boolean[] { true, false };
        if (representative.equals(m_bottomConcept))
           return new boolean[] { false, false };
        return isKnownOrPossibleConceptInstance(node,individual);
    }
    protected boolean[] isKnownOrPossibleConceptInstance(HierarchyNode<AtomicConcept> node,Individual individual){
        AtomicConcept representative=node.getRepresentative();
        AtomicConceptElement element=m_instanceManager.m_conceptToElement.get(representative);
        if ((element!=null && element.isKnown(individual))) 
            return new boolean[] { true, false };
        if (element!=null && element.isPossible(individual)) 
            return new boolean[] { false, true };
        for (HierarchyNode<AtomicConcept> child : node.getChildNodes()){
            if (child!=m_instanceManager.m_currentConceptHierarchy.m_bottomNode) {
                boolean[] result=isKnownOrPossibleConceptInstance(child, individual);
                if (result[0] || result[1])
                    return result;
            }
        }
        return new boolean[] { false, false };
    }
    public void getKnownAndPossibleInstances(OWLClass owlClass, Set<OWLNamedIndividual> knownInstances, Set<OWLNamedIndividual> possibleInstances) {
        AtomicConcept concept=H(owlClass);
        Set<Individual> known=new HashSet<Individual>();
        Set<Individual> possible=new HashSet<Individual>();
        getKnownAndPossibleConceptInstances(concept, known, possible);
        convertToOWLNamedIndividuals(known, knownInstances);
        convertToOWLNamedIndividuals(possible, possibleInstances);
    }
    protected void getKnownAndPossibleConceptInstances(AtomicConcept concept, Set<Individual> knownInstances, Set<Individual> possibleInstances) {
        HierarchyNode<AtomicConcept> node=m_instanceManager.m_currentConceptHierarchy.getNodeForElement(concept);
        if (node==null) 
            return; // unknown concept
        AtomicConcept representative=node.getRepresentative();
        if (representative.equals(m_bottomConcept))
            return;
        if (representative.equals(m_topConcept)) {
            knownInstances.addAll(Arrays.asList(m_instanceManager.m_individuals));
        } else 
            getKnownAndPossibleConceptInstances(node, knownInstances, possibleInstances);
    }
    protected void getKnownAndPossibleConceptInstances(HierarchyNode<AtomicConcept> node, Set<Individual> knownInstances, Set<Individual> possibleInstances) {
        AtomicConceptElement representativeElement=m_instanceManager.m_conceptToElement.get(node.getRepresentative());
        if (representativeElement!=null) { 
            knownInstances.addAll(representativeElement.getKnownInstances());
            possibleInstances.addAll(representativeElement.getPossibleInstances());
        }
        for (HierarchyNode<AtomicConcept> child : node.getChildNodes())
            if (child!=m_instanceManager.m_currentConceptHierarchy.m_bottomNode) 
                getKnownAndPossibleConceptInstances(child,knownInstances,possibleInstances);
    }
    
    // ****************************************    
    // methods for property instance statistics
    // ****************************************
    
    
    /**
     * @param property
     * @return 0: number of known instances 1: number of possible instances 2: number of individuals with at least one known or possible successor
     */
    /*
    public int[] getNumberOfInstances(OWLObjectProperty property) {
        if (m_instanceManager.m_individuals.length==0)
            return new int[] { 0,0 };
        else {
            AtomicRole role=H(property);
            return getNumberOfRoleInstances(role);
        }
    }
    protected int[] getNumberOfRoleInstances(AtomicRole role) {
        int[] result=new int[2];
        HierarchyNode<RoleElement> node=m_instanceManager.m_currentRoleHierarchy.getNodeForElement(m_instanceManager.m_roleElementManager.getRoleElement(role));
        if (node==null)
            return result; // unknown concept
        RoleElement representative=node.getRepresentative();
        if (representative.equals(m_topRoleElement)) {
            int inds=m_instanceManager.m_individuals.length;
            result[0]=inds*inds;
            return result;
        }
        if (representative.equals(m_bottomRoleElement)) {
            return result;
        }
        return getNumberOfRoleInstances(node,result);
    }
    protected int[] getNumberOfRoleInstances(HierarchyNode<RoleElement> node,int[] result) {
        RoleElement representative=node.getRepresentative();
        Map<Individual,Set<Individual>> map=representative.getKnownRelations();
        for (Individual individual : map.keySet())
              result[0]+=map.get(individual).size();
        map=representative.getPossibleRelations();
        for (Individual individual : map.keySet())
              result[1]+=map.get(individual).size();
        for (HierarchyNode<RoleElement> child : node.getChildNodes())
            if (child!=m_instanceManager.m_currentRoleHierarchy.m_bottomNode)
                getNumberOfRoleInstances(child,result);
        return result;
    }*/
    public RoleInstanceStatistics getRoleInstanceStatistics(OWLObjectProperty op) {
        if (m_instanceManager.m_individuals.length==0)
            return new RoleInstanceStatistics(0,0,0,0,0,0);
        else {
            AtomicRole role=H(op);
            return getRoleInstanceStatistics(role);
        }
    }
    protected RoleInstanceStatistics getRoleInstanceStatistics(AtomicRole role) {
        HierarchyNode<RoleElement> node=m_instanceManager.m_currentRoleHierarchy.getNodeForElement(m_instanceManager.m_roleElementManager.getRoleElement(role));
        if (node==null)
            return new RoleInstanceStatistics(0,0,0,0,0,0); // unknown role
        RoleElement representative=node.getRepresentative();
        if (representative.equals(m_topRoleElement)) {
            int numberOfIndividuals=m_instanceManager.m_individuals.length;
            return new RoleInstanceStatistics(numberOfIndividuals,numberOfIndividuals,0,0,numberOfIndividuals*numberOfIndividuals,0);
        }
        if (representative.equals(m_bottomRoleElement))
            return new RoleInstanceStatistics(0,0,0,0,0,0);
        RoleInstanceStatisticsVisitor statisticsVisitor=new RoleInstanceStatisticsVisitor(m_instanceManager.m_currentRoleHierarchy.m_bottomNode);
        @SuppressWarnings("unchecked")
        HierarchyNode<RoleElement>[] redirectBuffer=new HierarchyNode[2];
        Set<HierarchyNode<RoleElement>> visited=new HashSet<HierarchyNode<RoleElement>>();
        m_instanceManager.m_currentRoleHierarchy.traverseDepthFirst(statisticsVisitor,0,node,null,visited,redirectBuffer);
        return statisticsVisitor.getRoleInstanceStatistics();
    }
    final class RoleInstanceStatisticsVisitor implements Hierarchy.HierarchyNodeVisitor<RoleElement> {
        protected final HierarchyNode<RoleElement> m_bottomNode;
        protected final Set<Individual> m_distinctKnownPredecessors; // no duplicates
        protected final Set<Individual> m_distinctKnownSuccessors; // no duplicates
        protected final Set<Individual> m_distinctPossiblePredecessors; // no duplicates
        protected final Set<Individual> m_distinctPossibleSuccessors; // no duplicates
        protected int m_numberOfKnownSuccessors=0;  // incl. duplicates
        protected int m_numberOfPossibleSuccessors=0;  // incl. duplicates

        public RoleInstanceStatisticsVisitor(HierarchyNode<RoleElement> bottomNode) {
            m_bottomNode=bottomNode;
            m_distinctKnownPredecessors=new HashSet<Individual>();
            m_distinctKnownSuccessors=new HashSet<Individual>();
            m_distinctPossiblePredecessors=new HashSet<Individual>();
            m_distinctPossibleSuccessors=new HashSet<Individual>();
        }
        public boolean redirect(HierarchyNode<RoleElement>[] nodes) {
            return true;
        }
        public void visit(int level,HierarchyNode<RoleElement> node,HierarchyNode<RoleElement> parentNode,boolean firstVisit) {
            if (firstVisit && !node.equals(m_bottomNode)) {
                RoleElement representative=node.getRepresentative();
                Map<Individual,Set<Individual>> map=representative.getKnownRelations();
                m_distinctKnownPredecessors.addAll(map.keySet());
                for (Individual individual : map.keySet()) {
                    m_numberOfKnownSuccessors+=map.get(individual).size();
                    m_distinctKnownSuccessors.addAll(map.get(individual));
                }
                map=representative.getPossibleRelations();
                m_distinctPossiblePredecessors.addAll(map.keySet());
                for (Individual individual : map.keySet()) {
                    m_numberOfPossibleSuccessors+=map.get(individual).size();
                    m_distinctPossibleSuccessors.addAll(map.get(individual));
                }
            }
        }
        public RoleInstanceStatistics getRoleInstanceStatistics() {
            return new RoleInstanceStatistics(m_distinctKnownPredecessors.size(), m_distinctKnownSuccessors.size(), m_distinctPossiblePredecessors.size(), m_distinctPossibleSuccessors.size(), m_numberOfKnownSuccessors, m_numberOfPossibleSuccessors);
        }
    }
    final class RoleInstanceStatistics {
        protected final int m_numberOfDistinctKnownPredecessors; // no duplicates
        protected final int m_numberOfDistinctKnownSuccessors; // no duplicates
        protected final int m_numberOfDistinctPossiblePredecessors; // no duplicates
        protected final int m_numberOfDistinctPossibleSuccessors; // no duplicates
        protected final int m_numberOfKnownSuccessors;  // incl. duplicates
        protected final int m_numberOfPossibleSuccessors;  // incl. duplicates

        public RoleInstanceStatistics(int numberOfDistinctKnownPredecessors, int numberOfDistinctKnownSuccessors, int numberOfDistinctPossiblePredecessors, int numberOfDistinctPossibleSuccessors, int numberOfKnownSuccessors, int numberOfPossibleSuccessors) {
            m_numberOfDistinctKnownPredecessors=numberOfDistinctKnownPredecessors;
            m_numberOfDistinctKnownSuccessors=numberOfDistinctKnownSuccessors;
            m_numberOfDistinctPossiblePredecessors=numberOfDistinctPossiblePredecessors;
            m_numberOfDistinctPossibleSuccessors=numberOfDistinctPossibleSuccessors;
            m_numberOfKnownSuccessors=numberOfKnownSuccessors;
            m_numberOfPossibleSuccessors=numberOfPossibleSuccessors;
        }
        public int getNumberOfKnownSuccessors() {
            return m_numberOfKnownSuccessors;
        }
        public int getNumberOfPossibleSuccessors() {
            return m_numberOfPossibleSuccessors;
        }
        public int getNumberOfDistinctKnownSuccessors() {
            return m_numberOfDistinctKnownSuccessors;
        }
        public int getNumberOfDistinctKnownPredecessors() {
            return m_numberOfDistinctKnownPredecessors;
        }
        public int getNumberOfDistinctPossibleSuccessors() {
            return m_numberOfDistinctPossibleSuccessors;
        }
        public int getNumberOfDistinctPossiblePredecessors() {
            return m_numberOfDistinctPossiblePredecessors;
        }
    }
    public int[] getNumberOfSuccessors(OWLObjectProperty property,OWLIndividual individual) {
        if (m_instanceManager.m_individuals.length==0)
            return new int[] { 0,0 };
        else {
            AtomicRole role=H(property);
            Individual ind=H(individual);
            return getNumberOfSuccessors(role,ind);
        }
    }
    protected int[] getNumberOfSuccessors(AtomicRole role, Individual individual) {
        int[] result=new int[2];
        HierarchyNode<RoleElement> node=m_instanceManager.m_currentRoleHierarchy.getNodeForElement(m_instanceManager.m_roleElementManager.getRoleElement(role));
        if (node==null)
            return result; // unknown role
        RoleElement representative=node.getRepresentative();
        if (representative.equals(m_topRoleElement)) {
            result[0]=m_instanceManager.m_individuals.length;
            return result;
        }
        if (representative.equals(m_bottomRoleElement)) {
            return result;
        }
        return getNumberOfSuccessors(node,individual,result);
    }
    protected int[] getNumberOfSuccessors(HierarchyNode<RoleElement> node, Individual individual, int[] result) {
        RoleElement representative=node.getRepresentative();
        Map<Individual,Set<Individual>> map=representative.getKnownRelations();
        if (map!=null && map.containsKey(individual))
            result[0]+=map.get(individual).size();
        map=representative.getPossibleRelations();
        if (map!=null && map.containsKey(individual))
            result[1]+=map.get(individual).size();
        for (HierarchyNode<RoleElement> child : node.getChildNodes())
            if (child!=m_instanceManager.m_currentRoleHierarchy.m_bottomNode)
                getNumberOfSuccessors(child,individual,result);
        return result;
    }
    public int[] getNumberOfPredecessors(OWLObjectProperty property,OWLIndividual individual) {
        if (m_instanceManager.m_individuals.length==0)
            return new int[] { 0,0 };
        else {
            AtomicRole role=H(property);
            Individual ind=H(individual);
            return getNumberOfPredecessors(role,ind);
        }
    }
    protected int[] getNumberOfPredecessors(AtomicRole role, Individual individual) {
        int[] result=new int[2];
        HierarchyNode<RoleElement> node=m_instanceManager.m_currentRoleHierarchy.getNodeForElement(m_instanceManager.m_roleElementManager.getRoleElement(role));
        if (node==null)
            return result; // unknown role
        RoleElement representative=node.getRepresentative();
        if (representative.equals(m_topRoleElement)) {
            result[0]=m_instanceManager.m_individuals.length;
            return result;
        }
        if (representative.equals(m_bottomRoleElement)) {
            return result;
        }
        return getNumberOfPredecessors(node,individual,result);
    }
    protected int[] getNumberOfPredecessors(HierarchyNode<RoleElement> node, Individual individual, int[] result) {
        RoleElement representative=node.getRepresentative();
        Map<Individual,Set<Individual>> map=representative.getKnownRelations();
        for (Individual ind : map.keySet())
            if (map.get(ind).contains(individual))
                result[0]++;
        map=representative.getPossibleRelations();
        for (Individual ind : map.keySet())
            if (map.get(ind).contains(individual))
                result[1]++;
        for (HierarchyNode<RoleElement> child : node.getChildNodes())
            if (child!=m_instanceManager.m_currentRoleHierarchy.m_bottomNode)
                getNumberOfPredecessors(child,individual,result);
        return result;
    }
    public boolean[] hasSuccessor(OWLObjectProperty op, OWLNamedIndividual individual1, OWLNamedIndividual individual2) {
        AtomicRole role=H(op);
        Individual ind1=H(individual1);
        Individual ind2=H(individual2);
        return hasSuccessor(role, ind1, ind2);
    }
    protected boolean[] hasSuccessor(AtomicRole role, Individual individual1, Individual individual2) {
        boolean[] result=new boolean[2];
        HierarchyNode<RoleElement> node=m_instanceManager.m_currentRoleHierarchy.getNodeForElement(m_instanceManager.m_roleElementManager.getRoleElement(role));
        if (node==null)
            return result; // unknown role
        RoleElement representative=node.getRepresentative();
        if (representative.equals(m_topRoleElement))
            return new boolean[] { true, false };
        if (representative.equals(m_bottomRoleElement))
            return result;
        return hasSuccessor(node,individual1,individual2);
    }
    protected boolean[] hasSuccessor(HierarchyNode<RoleElement> node, Individual individual1, Individual individual2) {
        RoleElement representative=node.getRepresentative();
        Map<Individual,Set<Individual>> known=representative.getKnownRelations();
        Map<Individual,Set<Individual>> possible=representative.getPossibleRelations();
        if (known!=null && known.containsKey(individual1) && known.get(individual1).contains(individual2))
            return new boolean[] { true, false };
        if (possible!=null && possible.containsKey(individual1) && possible.get(individual1).contains(individual2))
            return new boolean[] { false, true };
        for (HierarchyNode<RoleElement> child : node.getChildNodes())
            if (child!=m_instanceManager.m_currentRoleHierarchy.m_bottomNode) {
                boolean[] result=hasSuccessor(child,individual1,individual2);
                if (result[0] || result[1])
                    return result;
            }
        return new boolean[] { false, false };
    }
    public void getKnownAndPossibleInstances(OWLObjectProperty op, Map<OWLNamedIndividual,Set<OWLNamedIndividual>> knownInstances, Map<OWLNamedIndividual,Set<OWLNamedIndividual>> possibleInstances) {
        AtomicRole role=H(op);
        Map<Individual,Set<Individual>> known=new HashMap<Individual, Set<Individual>>();
        Map<Individual,Set<Individual>> possible=new HashMap<Individual, Set<Individual>>();
        getKnownAndPossibleRoleInstances(role, known, possible);
        for (Individual ind : known.keySet()) {
            Set<OWLNamedIndividual> successors=new HashSet<OWLNamedIndividual>();
            convertToOWLNamedIndividuals(known.get(ind), successors);
            knownInstances.put(convertToOWLNamedIndividual(ind), successors);
        }
        for (Individual ind : possible.keySet()) {
            Set<OWLNamedIndividual> successors=new HashSet<OWLNamedIndividual>();
            convertToOWLNamedIndividuals(possible.get(ind), successors);
            possibleInstances.put(convertToOWLNamedIndividual(ind), successors);
        }
    }
    protected void getKnownAndPossibleRoleInstances(AtomicRole role, Map<Individual,Set<Individual>> knownInstances, Map<Individual,Set<Individual>> possibleInstances) {
        HierarchyNode<RoleElement> node=m_instanceManager.m_currentRoleHierarchy.getNodeForElement(m_instanceManager.m_roleElementManager.getRoleElement(role));
        if (node==null) 
            return; // unknown conept
        RoleElement representative=node.getRepresentative();
        if (representative.equals(m_bottomRoleElement))
            return;
        if (representative.equals(m_topRoleElement)) {
            Set<Individual> indSet=new HashSet<Individual>(Arrays.asList(m_instanceManager.m_individuals));
            for (Individual ind : m_instanceManager.m_individuals)
                 knownInstances.put(ind, indSet);
        } else
            getKnownAndPossibleRoleInstances(node,knownInstances,possibleInstances);
    }
    protected void getKnownAndPossibleRoleInstances(HierarchyNode<RoleElement> node, Map<Individual,Set<Individual>> knownInstances, Map<Individual,Set<Individual>> possibleInstances) {
        RoleElement representative=node.getRepresentative();
        Map<Individual,Set<Individual>> known=representative.getKnownRelations();
        for (Individual ind : known.keySet()) {
            Set<Individual> successors=knownInstances.get(ind);
            if (successors==null) {
                successors=new HashSet<Individual>();
                knownInstances.put(ind, successors);   
            }
            successors.addAll(known.get(ind));
        }
        Map<Individual,Set<Individual>> possible=representative.getPossibleRelations();
        for (Individual ind : possible.keySet()) {
            Set<Individual> successors=possibleInstances.get(ind);
            if (successors==null) {
                successors=new HashSet<Individual>();
                possibleInstances.put(ind, successors);
            }
            successors.addAll(possible.get(ind));
        }
        for (HierarchyNode<RoleElement> child : node.getChildNodes())
            if (child!=m_instanceManager.m_currentRoleHierarchy.m_bottomNode) 
                getKnownAndPossibleRoleInstances(child,knownInstances,possibleInstances);
    }
    public void getKnownAndPossibleSuccessors(OWLObjectProperty op, OWLNamedIndividual individual, Set<OWLNamedIndividual> knownSuccessors, Set<OWLNamedIndividual> possibleSuccessors) {
        AtomicRole role=H(op);
        Individual ind=H(individual);
        Set<Individual> known=new HashSet<Individual>();
        Set<Individual> possible=new HashSet<Individual>();
        getKnownAndPossibleSuccessors(role, ind, known, possible);
        convertToOWLNamedIndividuals(known, knownSuccessors);
        convertToOWLNamedIndividuals(possible, possibleSuccessors);
    }
    protected void getKnownAndPossibleSuccessors(AtomicRole role, Individual individual, Set<Individual> knownSuccessors, Set<Individual> possibleSuccessors) {
        HierarchyNode<RoleElement> node=m_instanceManager.m_currentRoleHierarchy.getNodeForElement(m_instanceManager.m_roleElementManager.getRoleElement(role));
        if (node==null) 
            return; // unknown role
        RoleElement representative=node.getRepresentative();
        if (representative.equals(m_bottomRoleElement))
            return;
        if (representative.equals(m_topRoleElement))
            knownSuccessors.addAll(Arrays.asList(m_instanceManager.m_individuals));
        else 
            getKnownAndPossibleSuccessors(node,individual,knownSuccessors,possibleSuccessors);
    }
    protected void getKnownAndPossibleSuccessors(HierarchyNode<RoleElement> node, Individual individual, Set<Individual> knownSuccessors, Set<Individual> possibleSuccessors) {
        RoleElement representative=node.getRepresentative();
        Map<Individual,Set<Individual>> knownRelations=representative.getKnownRelations();
        if (knownRelations!=null && knownRelations.containsKey(individual))
            knownSuccessors.addAll(knownRelations.get(individual));
        Map<Individual,Set<Individual>> possibleRelations=representative.getPossibleRelations();
        if (possibleRelations!=null && possibleRelations.containsKey(individual))
            possibleSuccessors.addAll(possibleRelations.get(individual));
        for (HierarchyNode<RoleElement> child : node.getChildNodes())
            if (child!=m_instanceManager.m_currentRoleHierarchy.m_bottomNode) 
                getKnownAndPossibleSuccessors(child,individual,knownSuccessors,possibleSuccessors);
    }
    public void getKnownAndPossiblePredeccessors(OWLObjectProperty op, OWLNamedIndividual individual, Set<OWLNamedIndividual> knownPredecessors, Set<OWLNamedIndividual> possiblePredecessors) {
        AtomicRole role=H(op);
        Individual ind=H(individual);
        Set<Individual> known=new HashSet<Individual>();
        Set<Individual> possible=new HashSet<Individual>();
        getKnownAndPossiblePredeccessors(role, ind, known, possible);
        convertToOWLNamedIndividuals(known, knownPredecessors);
        convertToOWLNamedIndividuals(possible, possiblePredecessors);
    }
    protected void getKnownAndPossiblePredeccessors(AtomicRole role, Individual individual, Set<Individual> knownPredecessors, Set<Individual> possiblePredecessors) {
        HierarchyNode<RoleElement> node=m_instanceManager.m_currentRoleHierarchy.getNodeForElement(m_instanceManager.m_roleElementManager.getRoleElement(role));
        if (node==null) 
            return; // unknown role
        RoleElement representative=node.getRepresentative();
        if (representative.equals(m_bottomRoleElement))
            return;
        if (representative.equals(m_topRoleElement)) {
            knownPredecessors.addAll(Arrays.asList(m_instanceManager.m_individuals));
        }
        getKnownAndPossiblePredeccessors(node,individual,knownPredecessors,possiblePredecessors);
    }
    protected void getKnownAndPossiblePredeccessors(HierarchyNode<RoleElement> node, Individual individual, Set<Individual> knownPredecessors, Set<Individual> possiblePredecessors) {
        RoleElement representative=node.getRepresentative();
        Map<Individual,Set<Individual>> knownRelations=representative.getKnownRelations();
        for (Individual ind : knownRelations.keySet())        
            if (knownRelations.get(ind).contains(individual))
                knownPredecessors.add(ind);
        Map<Individual,Set<Individual>> possibleRelations=representative.getPossibleRelations();
        for (Individual ind : knownRelations.keySet()) 
            if (possibleRelations.get(ind).contains(individual))
                possiblePredecessors.add(ind);
        for (HierarchyNode<RoleElement> child : node.getChildNodes())
            if (child!=m_instanceManager.m_currentRoleHierarchy.m_bottomNode) 
                getKnownAndPossiblePredeccessors(child,individual,knownPredecessors,possiblePredecessors);
    }
    
    // ****************************************    
    // methods for sameAs
    // ****************************************

    public int[] getNumberOfSameIndividuals(OWLIndividual individual) {
        if (m_instanceManager.m_individuals.length==0)
            return new int[] { 0,0 };
        else {
            Individual ind=H(individual);
            return getNumberOfSameIndividuals(ind);
        }
    }
    protected int[] getNumberOfSameIndividuals(Individual individual) {
        int[] result=new int[2];
        Set<Individual> equivalenceClass=m_instanceManager.m_individualToEquivalenceClass.get(individual);
        if (equivalenceClass!=null) {
            result[0]=equivalenceClass.size();
            Set<Set<Individual>> possiblySameEquivalenceClasses=m_instanceManager.m_individualToPossibleEquivalenceClass.get(equivalenceClass);
            if (possiblySameEquivalenceClasses!=null)
                for (Set<Individual> possiblySameAs : possiblySameEquivalenceClasses)
                    result[1]+=possiblySameAs.size();
        }
        return result;
    }
    
    // helper classes
    
    final class DFS<T> {
        Set<HierarchyNode<T>> visited=new HashSet<HierarchyNode<T>>();
        protected final HierarchyNode<T> m_bottomNode;

        public DFS(HierarchyNode<T> bottomNode) {
            m_bottomNode=bottomNode;
        }
        public void traverse(HierarchyNode<T> startNode, DFSVisitor<T> visitor) {
            traverse(0, startNode, visitor);
        }
        protected void traverse(int level, HierarchyNode<T> node, DFSVisitor<T> visitor) {
            boolean firstVisit=visited.add(node);
            if (firstVisit) {
                visitor.previsit(level, node);
                boolean continueVisiting=visitor.visit(level,node);
                if (continueVisiting)
                    for (HierarchyNode<T> childNode : node.m_childNodes)
                        traverse(level+1,childNode,visitor);
                visitor.postvisit(level,node);
            }
        }
    }
    protected static interface DFSVisitor<E> {
        public void previsit(int level,HierarchyNode<E> node);
        public boolean visit(int level,HierarchyNode<E> node);
        public void postvisit(int level,HierarchyNode<E> node);
    }
    protected final class DFSTypeCounter implements DFSVisitor<AtomicConcept> {
        protected final Individual m_individual;
        protected int types=0;
        protected int possibleTypes=0;
        protected List<Integer> typesStack=new ArrayList<Integer>();

        public DFSTypeCounter(Individual individual) {
            m_individual=individual;
        }
        public void previsit(int level,HierarchyNode<AtomicConcept> node) {

        }
        public boolean visit(int level,HierarchyNode<AtomicConcept> node) {
            AtomicConceptElement element=m_instanceManager.m_conceptToElement.get(node.getRepresentative());
            int accumulatedTypes=typesStack.size()>0?typesStack.get(typesStack.size()-1):0;
            accumulatedTypes+=node.getEquivalentElements().size();
            typesStack.add(accumulatedTypes);
            if (element!=null) {
                if (element.getKnownInstances().contains(m_individual)) {
                    types+=accumulatedTypes;
                    typesStack.clear();
                    return false; // stop search on this branch
                }
                if (element.getPossibleInstances().contains(m_individual)) {
                    possibleTypes+=accumulatedTypes;
                    typesStack.clear();
                    return false; // stop search on this branch
                }
            }
            return true;
        }
        public void postvisit(int level,HierarchyNode<AtomicConcept> node) {
            if (!typesStack.isEmpty())
                typesStack.remove(typesStack.size()-1);
        }
    }
    
    // Methods for conversion between OWL API and HermiT's API

    protected static AtomicConcept H(OWLClass owlClass) {
        return AtomicConcept.create(owlClass.getIRI().toString());
    }
    protected static AtomicRole H(OWLObjectProperty objectProperty) {
        return AtomicRole.create(objectProperty.getIRI().toString());
    }
    protected static Role H(OWLObjectPropertyExpression objectPropertyExpression) {
        objectPropertyExpression=objectPropertyExpression.getSimplified();
        if (objectPropertyExpression instanceof OWLObjectProperty)
            return H((OWLObjectProperty)objectPropertyExpression);
        else
            return H(objectPropertyExpression.getNamedProperty()).getInverse();
    }
    protected static Individual H(OWLNamedIndividual namedIndividual) {
        return Individual.create(namedIndividual.getIRI().toString());
    }
    protected static Individual H(OWLAnonymousIndividual anonymousIndividual) {
        return Individual.createAnonymous(anonymousIndividual.getID().toString());
    }
    protected static Individual H(OWLIndividual individual) {
        if (individual.isAnonymous())
            return H((OWLAnonymousIndividual)individual);
        else
            return H((OWLNamedIndividual)individual);
    }
    
    protected OWLNamedIndividual convertToOWLNamedIndividual(Individual ind) {
        assert !ind.isAnonymous();
        OWLDataFactory factory=m_reasoner.getDataFactory();
        return factory.getOWLNamedIndividual(IRI.create(ind.getIRI()));
    }
    protected OWLAnonymousIndividual convertToOWLAnonymousIndividual(Individual ind) {
        assert ind.isAnonymous();
        OWLDataFactory factory=m_reasoner.getDataFactory();
        return factory.getOWLAnonymousIndividual(ind.getIRI());
    }
    protected OWLIndividual convertToOWLIndividual(Individual ind) {
        if (ind.isAnonymous())
            return convertToOWLAnonymousIndividual(ind);
        else
            return convertToOWLNamedIndividual(ind);
    }
    protected Set<OWLNamedIndividual> convertToOWLNamedIndividuals(Set<Individual> inds, Set<OWLNamedIndividual> namedInds) {
        for (Individual ind : inds) {
            if (!ind.isAnonymous())
                namedInds.add(convertToOWLNamedIndividual(ind));
        }
        return namedInds;
    }
}
