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
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.HermiT.graph.Graph;
import org.semanticweb.HermiT.hierarchy.DeterministicClassificationManager.GraphNode;
import org.semanticweb.HermiT.hierarchy.StandardClassificationManager.Relation;
import org.semanticweb.HermiT.model.Atom;
import org.semanticweb.HermiT.model.AtomicConcept;
import org.semanticweb.HermiT.model.DLClause;
import org.semanticweb.HermiT.model.DLOntology;
import org.semanticweb.HermiT.model.DLPredicate;
import org.semanticweb.HermiT.model.Individual;
import org.semanticweb.HermiT.tableau.ExtensionTable;
import org.semanticweb.HermiT.tableau.Node;
import org.semanticweb.HermiT.tableau.ReasoningTaskDescription;
import org.semanticweb.HermiT.tableau.Tableau;

public class QuasiOrderClassificationManager implements ClassificationManager<AtomicConcept> {
    protected final Reasoner m_reasoner;
    protected final Graph<AtomicConcept> m_knownSubsumptions=new Graph<AtomicConcept>();
    protected final Graph<AtomicConcept> m_possibleSubsumptions=new Graph<AtomicConcept>();

    public QuasiOrderClassificationManager(Reasoner reasoner) {
        m_reasoner=reasoner;
    }
    public boolean isSatisfiable(AtomicConcept element) {
        if (AtomicConcept.NOTHING.equals(element))
            return false;
        if (isUnsatisfiable(element))
            return false;
        Individual freshIndividual=Individual.createAnonymous("fresh-individual");
        return m_reasoner.getTableau().isSatisfiable(false,Collections.singleton(Atom.create(element,freshIndividual)),null,null,null,null,ReasoningTaskDescription.isConceptSatisfiable(element));
    }
    public boolean isSubsumedBy(AtomicConcept subelement,AtomicConcept superelement) {
        if (getKnownSubsumers(subelement).contains(superelement))
            return true;
        Individual freshIndividual=Individual.createAnonymous("fresh-individual");
        return !m_reasoner.getTableau().isSatisfiable(false,Collections.singleton(Atom.create(subelement,freshIndividual)),null,null,Collections.singleton(Atom.create(superelement,freshIndividual)),null,ReasoningTaskDescription.isConceptSubsumedBy(subelement,superelement));
    }
    public Hierarchy<AtomicConcept> classify(ProgressMonitor<AtomicConcept> progressMonitor,AtomicConcept topElement,AtomicConcept bottomElement,final Set<AtomicConcept> elements) {
        Individual freshIndividual=Individual.createAnonymous("fresh-individual");
        if (!m_reasoner.getTableau().isSatisfiable(false,Collections.singleton(Atom.create(topElement,freshIndividual)),null,null,Collections.singleton(Atom.create(bottomElement,freshIndividual)),null,ReasoningTaskDescription.isConceptSubsumedBy(topElement,bottomElement)))
            return Hierarchy.emptyHierarchy(elements,topElement,bottomElement);
        Relation<AtomicConcept> relation=new Relation<AtomicConcept>() {
            public boolean doesSubsume(AtomicConcept parent,AtomicConcept child) {
                Set<AtomicConcept> allKnownSubsumers=getKnownSubsumers(child);
                if (allKnownSubsumers.contains(parent))
                    return true;
                else if (!allKnownSubsumers.contains(parent) && !m_possibleSubsumptions.getSuccessors(child).contains(parent))
                    return false;
                Individual freshIndividual=Individual.createAnonymous("fresh-individual");
                Tableau tableau=m_reasoner.getTableau();
                boolean isSubsumedBy=!m_reasoner.getTableau().isSatisfiable(false,Collections.singleton(Atom.create(child,freshIndividual)),null,null,Collections.singleton(Atom.create(parent,freshIndividual)),null,ReasoningTaskDescription.isConceptSubsumedBy(child,parent));
                if (!isSubsumedBy)
                    prunePossibleSubsumers(tableau);
                return isSubsumedBy;
            }
        };
        return buildHierarchy(progressMonitor,relation,topElement,bottomElement,elements);
    }
    private Hierarchy<AtomicConcept> buildHierarchy(ProgressMonitor<AtomicConcept> progressMonitor,Relation<AtomicConcept> hierarchyRelation,AtomicConcept topElement,AtomicConcept bottomElement,Set<AtomicConcept> elements) {

        initializeKnownSubsumptions(elements,topElement,bottomElement);

        updateKnownSubsumptionsUsingToldSubsumers(m_reasoner.getDLOntology());

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
    private Hierarchy<AtomicConcept> buildSmallHierarchy(AtomicConcept topElement,AtomicConcept bottomElement,Set<AtomicConcept> unknownSubsumers) {
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
    private Set<AtomicConcept> updateSubsumptionsUsingLeafNodeStrategy(ProgressMonitor<AtomicConcept> progressMonitor, Set<AtomicConcept> elements,AtomicConcept topElement,AtomicConcept bottomElement) {

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
    private void getKnownSubsumersForConcept(AtomicConcept concept) {
        Individual freshIndividual=Individual.createAnonymous("fresh-individual");
        Tableau tableau=m_reasoner.getTableau();
        Map<Individual,Node> checkedNode=new HashMap<Individual,Node>();
        checkedNode.put(freshIndividual,null);
        if (tableau.isSatisfiable(false,Collections.singleton(Atom.create(concept,freshIndividual)),null,null,null,checkedNode,ReasoningTaskDescription.isConceptSatisfiable(concept))) {
            readKnownSubsumersFromRootNode(tableau,concept,checkedNode.get(freshIndividual));
            updatePossibleSubsumers(tableau);
        }
        else
            makeConceptUnsatisfiable(concept);
    }
    private void makeConceptUnsatisfiable(AtomicConcept concept) {
        m_knownSubsumptions.addEdge(concept,AtomicConcept.NOTHING);
    }
    private boolean isUnsatisfiable(AtomicConcept concept) {
        return m_knownSubsumptions.getSuccessors(concept).contains(AtomicConcept.NOTHING);
    }
    protected void readKnownSubsumersFromRootNode(Tableau tableau,AtomicConcept subconcept,Node checkedNode) {
        if (checkedNode.getCanonicalNodeDependencySet().isEmpty()) {
            checkedNode=checkedNode.getCanonicalNode();
            m_knownSubsumptions.addEdge(subconcept,AtomicConcept.THING);
            ExtensionTable.Retrieval retrieval=tableau.getExtensionManager().getBinaryExtensionTable().createRetrieval(new boolean[] { false,true },ExtensionTable.View.TOTAL);
            retrieval.getBindingsBuffer()[1]=checkedNode;
            retrieval.open();
            while (!retrieval.afterLast()) {
                Object concept=retrieval.getTupleBuffer()[0];
                if (concept instanceof AtomicConcept && retrieval.getDependencySet().isEmpty() && !Prefixes.isInternalIRI(((AtomicConcept)concept).getIRI()))
                    m_knownSubsumptions.addEdge(subconcept,(AtomicConcept)concept);
                retrieval.next();
            }
        }
    }
    protected void updatePossibleSubsumers(Tableau tableau) {
        ExtensionTable.Retrieval retrieval=tableau.getExtensionManager().getBinaryExtensionTable().createRetrieval(new boolean[] { false,false },ExtensionTable.View.TOTAL);
        retrieval.open();
        Object[] tupleBuffer=retrieval.getTupleBuffer();
        while (!retrieval.afterLast()) {
            Object conceptObject=tupleBuffer[0];
            if (conceptObject instanceof AtomicConcept) {
                AtomicConcept atomicConcept=(AtomicConcept)conceptObject;
                if (!Prefixes.isInternalIRI(atomicConcept.getIRI())) {
                    Node node=(Node)tupleBuffer[1];
                    if (node.isActive() && !node.isBlocked()) {
                        if (m_possibleSubsumptions.getSuccessors(atomicConcept).isEmpty())
                            readPossibleSubsumersFromNodeLabel(tableau,atomicConcept,node);
                        else
                            prunePossibleSubsumersOfConcept(tableau,atomicConcept,node);
                    }
                }
            }
            retrieval.next();
        }
    }
    private void prunePossibleSubsumers(Tableau tableau) {
        ExtensionTable.Retrieval retrieval=tableau.getExtensionManager().getBinaryExtensionTable().createRetrieval(new boolean[] { false,false },ExtensionTable.View.TOTAL);
        retrieval.open();
        Object[] tupleBuffer=retrieval.getTupleBuffer();
        while (!retrieval.afterLast()) {
            Object conceptObject=tupleBuffer[0];
            if (conceptObject instanceof AtomicConcept) {
                AtomicConcept atomicConcept=(AtomicConcept)conceptObject;
                if (!Prefixes.isInternalIRI(atomicConcept.getIRI())) {
                    Node node=(Node)tupleBuffer[1];
                    if (node.isActive() && !node.isBlocked())
                        prunePossibleSubsumersOfConcept(tableau,atomicConcept,node);
                }
            }
            retrieval.next();
        }
    }
    private void prunePossibleSubsumersOfConcept(Tableau tableau,AtomicConcept atomicConcept,Node node) {
        Set<AtomicConcept> possibleSubsumersOfConcept=new HashSet<AtomicConcept>(m_possibleSubsumptions.getSuccessors(atomicConcept));
        for (AtomicConcept atomicCon : possibleSubsumersOfConcept)
            if (!tableau.getExtensionManager().containsConceptAssertion(atomicCon,node))
                m_possibleSubsumptions.getSuccessors(atomicConcept).remove(atomicCon);
    }
    private void readPossibleSubsumersFromNodeLabel(Tableau tableau,AtomicConcept atomicConcept,Node node) {
        ExtensionTable.Retrieval retrieval=tableau.getExtensionManager().getBinaryExtensionTable().createRetrieval(new boolean[] { false,true },ExtensionTable.View.TOTAL);
        retrieval.getBindingsBuffer()[1]=node;
        retrieval.open();
        while (!retrieval.afterLast()) {
            Object concept=retrieval.getTupleBuffer()[0];
            if (concept instanceof AtomicConcept && !Prefixes.isInternalIRI(((AtomicConcept)concept).getIRI()))
                m_possibleSubsumptions.addEdge(atomicConcept,(AtomicConcept)concept);
            retrieval.next();
        }
    }
    private Hierarchy<AtomicConcept> buildTransitivelyReducedHierarchy(AtomicConcept topElement,AtomicConcept bottomElement,Graph<AtomicConcept> knownSubsumptions) {
        final Map<AtomicConcept,GraphNode<AtomicConcept>> allSubsumers=new HashMap<AtomicConcept,GraphNode<AtomicConcept>>();
        for (AtomicConcept element : knownSubsumptions.getElements())
            allSubsumers.put(element,new GraphNode<AtomicConcept>(element,knownSubsumptions.getSuccessors(element)));

        return DeterministicClassificationManager.buildHierarchy(topElement,bottomElement,allSubsumers);
    }
    private void updateKnownSubsumptionsUsingToldSubsumers(DLOntology dlOntology) {
        for (DLClause dlClause : dlOntology.getDLClauses())
            if (dlClause.getHeadLength()==1 && dlClause.getBodyLength()==1) {
                DLPredicate headPredicate=dlClause.getHeadAtom(0).getDLPredicate();
                DLPredicate bodyPredicate=dlClause.getBodyAtom(0).getDLPredicate();
                if (headPredicate instanceof AtomicConcept && bodyPredicate instanceof AtomicConcept) {
                    AtomicConcept headConcept=(AtomicConcept)headPredicate;
                    AtomicConcept bodyConcept=(AtomicConcept)bodyPredicate;
                    if (!Prefixes.isInternalIRI(headConcept.getIRI()) && !Prefixes.isInternalIRI(bodyConcept.getIRI()))
                        m_knownSubsumptions.addEdge(bodyConcept,headConcept);
                }
            }
    }
    private void checkUnknownSubsumersUsingEnhancedTraversal(Relation<AtomicConcept> hierarchyRelation,HierarchyNode<AtomicConcept> startNode,AtomicConcept pickedElement) {
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
                    m_knownSubsumptions.addEdge(pickedElement,element);
                    m_knownSubsumptions.addEdges(pickedElement,subordinateElement.getEquivalentElements());
                    if (visited.add(subordinateElement))
                        toProcess.add(subordinateElement);
                }
                visited.add(subordinateElement);
            }
        }
    }
    private boolean isEveryChildANonSubsumer(Set<HierarchyNode<AtomicConcept>> unknownSubsumerNodes,AtomicConcept pickedElement,int childNumberThreshold) {
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
            Tableau tableau=m_reasoner.getTableau();
            if (tableau.isSatisfiable(false,Collections.singleton(subconceptAssertion),null,null,superconceptAssertions,null,ReasoningTaskDescription.isConceptSubsumedByList(pickedElement,superconcepts))) {
                prunePossibleSubsumers(tableau);
                return true;
            }
        }
        return false;
    }
    private Set<AtomicConcept> getKnownSubsumers(AtomicConcept child) {
        return m_knownSubsumptions.getReachableSuccessors(child);
    }
    private void initializeKnownSubsumptions(Set<AtomicConcept> elements,AtomicConcept topElement,AtomicConcept bottomElement) {
        for (AtomicConcept element : elements) {
            m_knownSubsumptions.addEdge(element,element);
            m_knownSubsumptions.addEdge(element,topElement);
            m_knownSubsumptions.addEdge(bottomElement,element);
        }
        makeConceptUnsatisfiable(bottomElement);
    }
}
