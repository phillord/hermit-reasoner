/* Copyright 2008, 2009, 2010 by the Oxford University Computing Laboratory

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
package org.semanticweb.HermiT.hierarchy;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import org.semanticweb.HermiT.Prefixes;
import org.semanticweb.HermiT.graph.Graph;
import org.semanticweb.HermiT.hierarchy.DeterministicClassificationManager.GraphNode;
import org.semanticweb.HermiT.hierarchy.StandardClassificationManager.Relation;
import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLPredicate;
import org.semanticweb.HermiT.model.Individual;
import org.semanticweb.HermiT.tableau.ExtensionTable;
import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.HermiT.tableau.ReasoningTaskDescription;
import org.semanticweb.HermiT.tableau.Tableau;

public class QuasiOrderClassificationManager implements ClassificationManager<AtomicConcept> {
    protected final Tableau m_tableau;
    protected final Graph<AtomicConcept> m_knownSubsumptions=new Graph<AtomicConcept>();
    protected final Graph<AtomicConcept> m_possibleSubsumptions=new Graph<AtomicConcept>();

    public QuasiOrderClassificationManager(Tableau tableau) {
        m_tableau=tableau;
    }
    public boolean isSatisfiable(AtomicConcept element) {
        if (AtomicConcept.NOTHING.equals(element))
            return false;
        if (isUnsatisfiable(element))
            return false;
        Individual freshIndividual=Individual.createAnonymous("fresh-individual");
        return m_tableau.isSatisfiable(false,Collections.singleton(Atom.create(element,freshIndividual)),null,null,null,null,getSatTestDescription(element));
    }
    public boolean isSubsumedBy(AtomicConcept subelement,AtomicConcept superelement) {
        if (getKnownSubsumers(subelement).contains(superelement))
            return true;
        Individual freshIndividual=Individual.createAnonymous("fresh-individual");
        return !m_tableau.isSatisfiable(false,Collections.singleton(Atom.create(subelement,freshIndividual)),null,null,Collections.singleton(Atom.create(superelement,freshIndividual)),null,getSubsumptionTestDescription(subelement, superelement));
    }
    public Hierarchy<AtomicConcept> classify(ProgressMonitor<AtomicConcept> progressMonitor,AtomicConcept topElement,AtomicConcept bottomElement,final Set<AtomicConcept> elements) {
        Individual freshIndividual=Individual.createAnonymous("fresh-individual");
        if (!m_tableau.isSatisfiable(false,Collections.singleton(Atom.create(topElement,freshIndividual)),null,null,Collections.singleton(Atom.create(bottomElement,freshIndividual)),null,getSubsumptionTestDescription(topElement, bottomElement)))
            return Hierarchy.emptyHierarchy(elements,topElement,bottomElement);
        Relation<AtomicConcept> relation=new Relation<AtomicConcept>() {
            public boolean doesSubsume(AtomicConcept parent,AtomicConcept child) {
                Set<AtomicConcept> allKnownSubsumers=getKnownSubsumers(child);
                if (allKnownSubsumers.contains(parent))
                    return true;
                else if (!allKnownSubsumers.contains(parent) && !m_possibleSubsumptions.getSuccessors(child).contains(parent))
                    return false;
                Individual freshIndividual=Individual.createAnonymous("fresh-individual");
                boolean isSubsumedBy=!m_tableau.isSatisfiable(false,Collections.singleton(Atom.create(child,freshIndividual)),null,null,Collections.singleton(Atom.create(parent,freshIndividual)),null,getSubsumptionTestDescription(child, parent));
                if (!isSubsumedBy)
                    prunePossibleSubsumers();
                return isSubsumedBy;
            }
        };
        return buildHierarchy(progressMonitor,relation,topElement,bottomElement,elements);
    }
    protected Hierarchy<AtomicConcept> buildHierarchy(ProgressMonitor<AtomicConcept> progressMonitor,Relation<AtomicConcept> hierarchyRelation,AtomicConcept topElement,AtomicConcept bottomElement,Set<AtomicConcept> elements) {

        initializeKnownSubsumptions(elements,topElement,bottomElement);

        updateKnownSubsumptionsUsingToldSubsumers();

        Set<AtomicConcept> processedConcepts = updateSubsumptionsUsingLeafNodeStrategy(progressMonitor,elements,topElement,bottomElement);

        // Unlike Rob's paper our set of possible subsumptions P would only keep unknown possible subsumptions and not known subsumptions as well.
        Set<AtomicConcept> unclassifiedElements=new HashSet<AtomicConcept>();
        for (AtomicConcept element : elements) {
            if (!isUnsatisfiable(element)) {
                m_possibleSubsumptions.getSuccessors(element).removeAll(getKnownSubsumers(element));
                if (!m_possibleSubsumptions.getSuccessors(element).isEmpty()) {
                    unclassifiedElements.add(element);
                    continue;
                }
            }
            if( !processedConcepts.contains( element ) )
            	progressMonitor.elementClassified(element);
        }

        Set<AtomicConcept> classifiedElements=new HashSet<AtomicConcept>();
        while (!unclassifiedElements.isEmpty()) {

            AtomicConcept unclassifiedElement=null;
            for (AtomicConcept element : unclassifiedElements) {
                m_possibleSubsumptions.getSuccessors(element).removeAll(getKnownSubsumers(element));
                if (!m_possibleSubsumptions.getSuccessors(element).isEmpty()) {
                    unclassifiedElement=element;
                    break;
                }
                classifiedElements.add(element);
                if( !processedConcepts.contains( element ) )
                	progressMonitor.elementClassified(element);
            }
            unclassifiedElements.removeAll(classifiedElements);
            if (unclassifiedElements.isEmpty())
                break;

            Hierarchy<AtomicConcept> smallHierarchy=buildSmallHierarchy(topElement,bottomElement,m_possibleSubsumptions.getSuccessors(unclassifiedElement));

            checkUnknownSubsumersUsingEnhancedTraversal(hierarchyRelation,smallHierarchy.getTopNode(),unclassifiedElement);

            m_possibleSubsumptions.getSuccessors(unclassifiedElement).clear();
        }
        return buildTransitivelyReducedHierarchy(topElement,bottomElement,m_knownSubsumptions);
    }
    protected Hierarchy<AtomicConcept> buildSmallHierarchy(AtomicConcept topElement,AtomicConcept bottomElement,Set<AtomicConcept> unknownSubsumers) {
        Graph<AtomicConcept> smallKnownSubsumptions=new Graph<AtomicConcept>();
        for (AtomicConcept unknownSubsumer0 : unknownSubsumers) {
            smallKnownSubsumptions.addEdge(bottomElement,unknownSubsumer0);
            smallKnownSubsumptions.addEdge(unknownSubsumer0,topElement);
            Set<AtomicConcept> knownSubsumersOfElement=getKnownSubsumers(unknownSubsumer0);
            for (AtomicConcept unknownSubsumer1 : unknownSubsumers)
                if (knownSubsumersOfElement.contains(unknownSubsumer1))
                    smallKnownSubsumptions.addEdge(unknownSubsumer0,unknownSubsumer1);
        }
        return buildTransitivelyReducedHierarchy(topElement,bottomElement,smallKnownSubsumptions);
    }
    protected Set<AtomicConcept> updateSubsumptionsUsingLeafNodeStrategy(ProgressMonitor<AtomicConcept> progressMonitor, Set<AtomicConcept> elements,AtomicConcept topElement,AtomicConcept bottomElement) {

        Hierarchy<AtomicConcept> hierarchy=buildTransitivelyReducedHierarchy(topElement,bottomElement,m_knownSubsumptions);

        Set<HierarchyNode<AtomicConcept>> leafNodes=hierarchy.getBottomNode().getParentNodes();
        Set<AtomicConcept> processedConcepts = new HashSet<AtomicConcept>( );
        for (HierarchyNode<AtomicConcept> leafNode : leafNodes) {

            AtomicConcept leafNodeElement=leafNode.getRepresentative();
        	processedConcepts.add( leafNodeElement );
        	progressMonitor.elementClassified( leafNodeElement );
            if (!m_possibleSubsumptions.getSuccessors(leafNodeElement).isEmpty() || isUnsatisfiable(leafNodeElement))
                continue;

            getKnownSubsumersForConcept(leafNodeElement);
            // If the leaf was unsatisfable go up to find satisfiable parents. Unsatisfiable parent information can be propagated downwards.
            if (isUnsatisfiable(leafNodeElement)) {
                Stack<HierarchyNode<AtomicConcept>> parentsToProcess=new Stack<HierarchyNode<AtomicConcept>>();
                parentsToProcess.addAll(leafNode.getParentNodes());
                HierarchyNode<AtomicConcept> lastUnsatNode=leafNode;
                while (!parentsToProcess.isEmpty()) {
                    HierarchyNode<AtomicConcept> parentNode=parentsToProcess.pop();
                    AtomicConcept parentElement=parentNode.getRepresentative();
                    if (isUnsatisfiable(parentElement))
                        continue;
                    getKnownSubsumersForConcept(parentElement);
                    if (!isUnsatisfiable(parentElement)) {
                        for (HierarchyNode<AtomicConcept> newUnsatNode : lastUnsatNode.getDescendantNodes()) {
                            makeConceptUnsatisfiable(newUnsatNode.getRepresentative());
                            parentsToProcess.remove(newUnsatNode);
                        }
                    }
                    else {
                        parentsToProcess.addAll(parentNode.getParentNodes());
                        lastUnsatNode=parentNode;
                    }
                }
            }
        }
        return processedConcepts;
    }
    protected void getKnownSubsumersForConcept(AtomicConcept concept) {
        Individual freshIndividual=Individual.createAnonymous("fresh-individual");
        Map<Individual,Node> checkedNode=new HashMap<Individual,Node>();
        checkedNode.put(freshIndividual,null);
        if (m_tableau.isSatisfiable(false,Collections.singleton(Atom.create(concept,freshIndividual)),null,null,null,checkedNode,getSatTestDescription(concept))) {
            readKnownSubsumersFromRootNode(concept,checkedNode.get(freshIndividual));
            updatePossibleSubsumers();
        }
        else
            makeConceptUnsatisfiable(concept);
    }
    protected void makeConceptUnsatisfiable(AtomicConcept concept) {
        addKnownSubsumption(concept,AtomicConcept.NOTHING);
    }
    protected boolean isUnsatisfiable(AtomicConcept concept) {
        return m_knownSubsumptions.getSuccessors(concept).contains(AtomicConcept.NOTHING);
    }
    protected void readKnownSubsumersFromRootNode(AtomicConcept subconcept,Node checkedNode) {
        if (checkedNode.getCanonicalNodeDependencySet().isEmpty()) {
            checkedNode=checkedNode.getCanonicalNode();
            addKnownSubsumption(subconcept,AtomicConcept.THING);
            ExtensionTable.Retrieval retrieval=m_tableau.getExtensionManager().getBinaryExtensionTable().createRetrieval(new boolean[] { false,true },ExtensionTable.View.TOTAL);
            retrieval.getBindingsBuffer()[1]=checkedNode;
            retrieval.open();
            while (!retrieval.afterLast()) {
                Object concept=retrieval.getTupleBuffer()[0];
                if (concept instanceof AtomicConcept && retrieval.getDependencySet().isEmpty() && isRelevantConcept((AtomicConcept)concept))
                    addKnownSubsumption(subconcept,(AtomicConcept)concept);
                retrieval.next();
            }
        }
    }
    protected void updatePossibleSubsumers() {
        ExtensionTable.Retrieval retrieval=m_tableau.getExtensionManager().getBinaryExtensionTable().createRetrieval(new boolean[] { false,false },ExtensionTable.View.TOTAL);
        retrieval.open();
        Object[] tupleBuffer=retrieval.getTupleBuffer();
        while (!retrieval.afterLast()) {
            Object conceptObject=tupleBuffer[0];
            if (conceptObject instanceof AtomicConcept) {
                AtomicConcept atomicConcept=(AtomicConcept)conceptObject;
                if (isRelevantConcept(atomicConcept)) {
                    Node node=(Node)tupleBuffer[1];
                    if (node.isActive() && !node.isBlocked()) {
                        if (m_possibleSubsumptions.getSuccessors(atomicConcept).isEmpty())
                            readPossibleSubsumersFromNodeLabel(atomicConcept,node);
                        else
                            prunePossibleSubsumersOfConcept(atomicConcept,node);
                    }
                }
            }
            retrieval.next();
        }
    }
    protected void prunePossibleSubsumers() {
        ExtensionTable.Retrieval retrieval=m_tableau.getExtensionManager().getBinaryExtensionTable().createRetrieval(new boolean[] { false,false },ExtensionTable.View.TOTAL);
        retrieval.open();
        Object[] tupleBuffer=retrieval.getTupleBuffer();
        while (!retrieval.afterLast()) {
            Object conceptObject=tupleBuffer[0];
            if (conceptObject instanceof AtomicConcept) {
                AtomicConcept atomicConcept=(AtomicConcept)conceptObject;
                if (isRelevantConcept(atomicConcept)) {
                    Node node=(Node)tupleBuffer[1];
                    if (node.isActive() && !node.isBlocked())
                        prunePossibleSubsumersOfConcept(atomicConcept,node);
                }
            }
            retrieval.next();
        }
    }
    protected void prunePossibleSubsumersOfConcept(AtomicConcept atomicConcept,Node node) {
        Set<AtomicConcept> possibleSubsumersOfConcept=new HashSet<AtomicConcept>(m_possibleSubsumptions.getSuccessors(atomicConcept));
        for (AtomicConcept atomicCon : possibleSubsumersOfConcept)
            if (!m_tableau.getExtensionManager().containsConceptAssertion(atomicCon,node))
                m_possibleSubsumptions.getSuccessors(atomicConcept).remove(atomicCon);
    }
    protected void readPossibleSubsumersFromNodeLabel(AtomicConcept atomicConcept,Node node) {
        ExtensionTable.Retrieval retrieval=m_tableau.getExtensionManager().getBinaryExtensionTable().createRetrieval(new boolean[] { false,true },ExtensionTable.View.TOTAL);
        retrieval.getBindingsBuffer()[1]=node;
        retrieval.open();
        while (!retrieval.afterLast()) {
            Object concept=retrieval.getTupleBuffer()[0];
            if (concept instanceof AtomicConcept && isRelevantConcept((AtomicConcept)concept))
                addPossibleSubsumption(atomicConcept,(AtomicConcept)concept);
            retrieval.next();
        }
    }
    protected Hierarchy<AtomicConcept> buildTransitivelyReducedHierarchy(AtomicConcept topElement,AtomicConcept bottomElement,Graph<AtomicConcept> knownSubsumptions) {
        final Map<AtomicConcept,GraphNode<AtomicConcept>> allSubsumers=new HashMap<AtomicConcept,GraphNode<AtomicConcept>>();
        for (AtomicConcept element : knownSubsumptions.getElements())
            allSubsumers.put(element,new GraphNode<AtomicConcept>(element,knownSubsumptions.getSuccessors(element)));

        return DeterministicClassificationManager.buildHierarchy(topElement,bottomElement,allSubsumers);
    }
    protected void updateKnownSubsumptionsUsingToldSubsumers() {
        updateKnownSubsumptionsUsingToldSubsumers(m_tableau.getPermanentDLOntology().getDLClauses());
    }
    protected void updateKnownSubsumptionsUsingToldSubsumers(Set<DLClause> dlClauses) {
        for (DLClause dlClause : dlClauses) {
            if (dlClause.getHeadLength()==1 && dlClause.getBodyLength()==1) {
                DLPredicate headPredicate=dlClause.getHeadAtom(0).getDLPredicate();
                DLPredicate bodyPredicate=dlClause.getBodyAtom(0).getDLPredicate();
                if (headPredicate instanceof AtomicConcept && bodyPredicate instanceof AtomicConcept) {
                    AtomicConcept headConcept=(AtomicConcept)headPredicate;
                    AtomicConcept bodyConcept=(AtomicConcept)bodyPredicate;
                    if (isRelevantConcept(headConcept) && isRelevantConcept(bodyConcept))
                        addKnownSubsumption(bodyConcept,headConcept);
                }
            }
        }
    }
    protected void checkUnknownSubsumersUsingEnhancedTraversal(Relation<AtomicConcept> hierarchyRelation,HierarchyNode<AtomicConcept> startNode,AtomicConcept pickedElement) {
        Set<HierarchyNode<AtomicConcept>> startSearch=Collections.singleton(startNode);
        Set<HierarchyNode<AtomicConcept>> visited=new HashSet<HierarchyNode<AtomicConcept>>(startSearch);
        Queue<HierarchyNode<AtomicConcept>> toProcess=new LinkedList<HierarchyNode<AtomicConcept>>(startSearch);

        if (isEveryChildANonSubsumer(startNode.getChildNodes(),pickedElement,2))
            return;
        while (!toProcess.isEmpty()) {
            HierarchyNode<AtomicConcept> current=toProcess.remove();
            Set<HierarchyNode<AtomicConcept>> subordinateElements=current.getChildNodes();

            for (HierarchyNode<AtomicConcept> subordinateElement : subordinateElements) {
                AtomicConcept element=subordinateElement.getRepresentative();
                if (visited.contains(subordinateElement))
                    continue;
                if (hierarchyRelation.doesSubsume(element,pickedElement)) {
                    addKnownSubsumption(pickedElement,element);
                    addKnownSubsumptions(pickedElement,subordinateElement.getEquivalentElements());
                    if (visited.add(subordinateElement))
                        toProcess.add(subordinateElement);
                }
                visited.add(subordinateElement);
            }
        }
    }
    protected boolean isEveryChildANonSubsumer(Set<HierarchyNode<AtomicConcept>> unknownSubsumerNodes,AtomicConcept pickedElement,int childNumberThreshold) {
        if (unknownSubsumerNodes.size()>childNumberThreshold) {
            Individual freshIndividual=Individual.createAnonymous("fresh-individual");
            Atom subconceptAssertion=Atom.create(pickedElement,freshIndividual);
            Set<Atom> superconceptAssertions=new HashSet<Atom>();
            for (HierarchyNode<AtomicConcept> unknownSupNode : unknownSubsumerNodes)
                superconceptAssertions.add(Atom.create(unknownSupNode.getRepresentative(),freshIndividual));
            Object[] superconcepts=new Object[superconceptAssertions.size()];
            int index=0;
            for (Atom atom : superconceptAssertions)
                superconcepts[index++]=atom.getDLPredicate();
            if (m_tableau.isSatisfiable(false,Collections.singleton(subconceptAssertion),null,null,superconceptAssertions,null,getSubsumedByListTestDescription(pickedElement,superconcepts))) {
                prunePossibleSubsumers();
                return true;
            }
        }
        return false;
    }
    protected Set<AtomicConcept> getKnownSubsumers(AtomicConcept child) {
        return m_knownSubsumptions.getReachableSuccessors(child);
    }
    protected void initializeKnownSubsumptions(Set<AtomicConcept> elements,AtomicConcept topElement,AtomicConcept bottomElement) {
        for (AtomicConcept element : elements) {
            addKnownSubsumption(element,element);
            addKnownSubsumption(element,topElement);
            addKnownSubsumption(bottomElement,element);
        }
        makeConceptUnsatisfiable(bottomElement);
    }
    protected void addKnownSubsumption(AtomicConcept subConcept, AtomicConcept superConcept) {
        m_knownSubsumptions.addEdge(subConcept,superConcept);
    }
    protected void addKnownSubsumptions(AtomicConcept subConcept, Set<AtomicConcept> superConcepts) {
        m_knownSubsumptions.addEdges(subConcept,superConcepts);
    }
    protected void addPossibleSubsumption(AtomicConcept subConcept, AtomicConcept superConcept) {
        m_possibleSubsumptions.addEdge(subConcept,superConcept);
    }
    protected boolean isRelevantConcept(AtomicConcept atomicConcept) {
        return !Prefixes.isInternalIRI(atomicConcept.getIRI());
    }
    protected ReasoningTaskDescription getSatTestDescription(AtomicConcept atomicConcept) {
        return ReasoningTaskDescription.isConceptSatisfiable(atomicConcept);
    }
    protected ReasoningTaskDescription getSubsumptionTestDescription(AtomicConcept subConcept, AtomicConcept superConcept) {
        return ReasoningTaskDescription.isConceptSubsumedBy(subConcept,superConcept);
    }
    protected ReasoningTaskDescription getSubsumedByListTestDescription(AtomicConcept subConcept, Object[] superconcepts) {
        return ReasoningTaskDescription.isConceptSubsumedByList(subConcept,superconcepts);
    }
}
