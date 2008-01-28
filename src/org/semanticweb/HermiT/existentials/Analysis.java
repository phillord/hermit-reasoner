package org.semanticweb.HermiT.existentials;

import java.io.File;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import org.semanticweb.kaon2.api.DefaultOntologyResolver;
import org.semanticweb.kaon2.api.KAON2Connection;
import org.semanticweb.kaon2.api.KAON2Manager;
import org.semanticweb.kaon2.api.Ontology;

import org.semanticweb.HermiT.blocking.*;
import org.semanticweb.HermiT.kaon2.structural.*;
import org.semanticweb.HermiT.model.*;
import org.semanticweb.HermiT.tableau.*;

public class Analysis {
    protected final DLOntology m_dlOntology;
    protected final Set<AtomicConcept> m_dontReuseConcepts;
    protected final Set<AtomicConcept> m_graphConcepts;
    protected final Set<AtomicAbstractRole> m_functionalRoles;
    protected final ObjectHierarchy<Concept> m_conceptHierarchy;
    protected final ObjectHierarchy<AbstractRole> m_roleHierarchy;
    protected final Map<AtomicConcept,Node> m_representativeNodes;
    protected final Map<Node,AtomicConcept> m_conceptForNode;
    protected final Map<NodePair,Node> m_arcCauses;
    protected final ExtensionTable m_extensionTable;
    
    public Analysis(DLOntology dlOntology) {
        m_dlOntology=dlOntology;
        m_dontReuseConcepts=new TreeSet<AtomicConcept>(DLOntology.AtomicConceptComparator.INSTANCE);
        m_graphConcepts=new HashSet<AtomicConcept>();
        m_functionalRoles=new HashSet<AtomicAbstractRole>();
        m_conceptHierarchy=new ObjectHierarchy<Concept>();
        m_roleHierarchy=new ObjectHierarchy<AbstractRole>();
        analyzeDLOntology();
        m_representativeNodes=new HashMap<AtomicConcept,Node>();
        m_conceptForNode=new HashMap<Node,AtomicConcept>();
        m_arcCauses=new HashMap<NodePair,Node>();
        m_extensionTable=buildExtensionTable();
        applyHierarchy();
        int tuplesToprocess=0;
        ExtensionTable.Retrieval retrieval1=m_extensionTable.createRetrieval(new boolean[] { false,false,false },ExtensionTable.View.TOTAL);
        retrieval1.open();
        while (!retrieval1.afterLast()) {
            tuplesToprocess++;
            retrieval1.next();
        }
        System.out.println("Not reused concepts before merging analysis: "+m_dontReuseConcepts.size());
        System.out.println("Number of tuples to process: "+tuplesToprocess);
        retrieval1.open();
        int processedTuples=0;
        while (!retrieval1.afterLast()) {
            processedTuples++;
            Node centralNode=(Node)retrieval1.getTupleBuffer()[1];
            Node leafNode1=(Node)retrieval1.getTupleBuffer()[2];
            ExtensionTable.Retrieval retrieval2=m_extensionTable.createRetrieval(new boolean[] { true,true,false },ExtensionTable.View.TOTAL);
            retrieval2.getBindingsBuffer()[0]=retrieval1.getTupleBuffer()[0];
            retrieval2.getBindingsBuffer()[1]=centralNode;
            retrieval2.open();
            while (!retrieval2.afterLast()) {
                Node leafNode2=(Node)retrieval2.getTupleBuffer()[2];
                if (leafNode1.getNodeID()<leafNode2.getNodeID()) {
                    Node cause1=m_arcCauses.get(new NodePair(centralNode,leafNode1));
                    if (cause1==centralNode)
                        dontReuseConceptDueToMerging(m_conceptForNode.get(leafNode1));
                    else if (cause1==leafNode1)
                        dontReuseConceptDueToMerging(m_conceptForNode.get(centralNode));
                    else
                        throw new IllegalStateException();
                    Node cause2=m_arcCauses.get(new NodePair(centralNode,leafNode2));
                    if (cause2==centralNode)
                        dontReuseConceptDueToMerging(m_conceptForNode.get(leafNode2));
                    else if (cause2==leafNode2)
                        dontReuseConceptDueToMerging(m_conceptForNode.get(centralNode));
                    else
                        throw new IllegalStateException();
                }
                retrieval2.next();
            }
            retrieval1.next();
            if (processedTuples % 10000==0)
                System.out.println(processedTuples+" tuples processed in the outer loop ("+(processedTuples*100/tuplesToprocess)+"%)");
        }
        System.out.println("Total number of not reused concepts: "+m_dontReuseConcepts.size());
    }
    protected void dontReuseConceptDueToMerging(AtomicConcept atomicConcept) {
        if (m_dontReuseConcepts.add(atomicConcept))
            System.out.println(atomicConcept.getURI());
    }
    protected void dontReuseConcept(AtomicConcept atomicConcept) {
        m_dontReuseConcepts.add(atomicConcept);
    }
    protected void applyHierarchy() {
        Object[] buffer=new Object[3];
        int done=0;
        for (Map.Entry<Node,AtomicConcept> entry : m_conceptForNode.entrySet()) {
            Node node=entry.getKey();
            Set<Concept> allSuperconcepts=m_conceptHierarchy.getAllSuperobjects(entry.getValue());
            for (Concept superconcept : allSuperconcepts) {
                if (superconcept instanceof AtLeastAbstractRoleConcept) {
                    AtLeastAbstractRoleConcept existentialConcept=(AtLeastAbstractRoleConcept)superconcept;
                    AtomicConcept toConcept=(AtomicConcept)existentialConcept.getToConcept();
                    Node toNode=m_representativeNodes.get(toConcept);
                    Set<AbstractRole> superroles=m_roleHierarchy.getAllSuperobjects(existentialConcept.getOnAbstractRole());
                    superroles.add(existentialConcept.getOnAbstractRole());
                    boolean change=false;
                    for (AbstractRole onAbstractRole : superroles) {
                        if (onAbstractRole instanceof AtomicAbstractRole) {
                            if (m_functionalRoles.contains(onAbstractRole)) {
                                buffer[0]=onAbstractRole;
                                buffer[1]=node;
                                buffer[2]=toNode;
                                if (m_extensionTable.addTuple(buffer,null))
                                    change=true;
                            }
                        }
                        else if (onAbstractRole instanceof InverseAbstractRole) {
                            AtomicAbstractRole inverseAbstractRole=((InverseAbstractRole)onAbstractRole).getInverseOf();
                            if (m_functionalRoles.contains(inverseAbstractRole)) {
                                buffer[0]=inverseAbstractRole;
                                buffer[1]=toNode;
                                buffer[2]=node;
                                if (m_extensionTable.addTuple(buffer,null))
                                    change=true;
                            }
                        }
                    }
                    if (change) {
                        m_arcCauses.put(new NodePair(node,toNode),node);
                        m_arcCauses.put(new NodePair(toNode,node),node);
                    }
                }
            }
            done++;
            if ((done % 1000)==0)
                System.out.println(done+" nodes done");
        }
    }
    protected ExtensionTable buildExtensionTable() {
        Set<DLClause> noDLClauses=Collections.emptySet();
        Set<Atom> noAtoms=Collections.emptySet();
        DLOntology emptyDLOntology=new DLOntology("nothing",noDLClauses,noAtoms,noAtoms,false,false,false,false);
        Tableau tableau=new Tableau(null,new CreationOrderStrategy(new AnywhereBlocking(new PairWiseDirectBlockingChecker(),null)),emptyDLOntology);
        ExtensionTable extensionTable=new ExtensionTableWithTupleIndexes(tableau,tableau.getExtensionManager(),3,false,new TupleIndex[] { new TupleIndex(new int[] { 0,1,2 }) });
        DependencySet emptySet=tableau.getDependencySetFactory().emptySet();
        for (AtomicConcept atomicConcept : m_graphConcepts) {
            Node node=tableau.createNewRootNode(emptySet,0);
            m_representativeNodes.put(atomicConcept,node);
            m_conceptForNode.put(node,atomicConcept);
        }
        return extensionTable;
    }
    protected void analyzeDLOntology() {
        for (DLClause dlClause : m_dlOntology.getDLClauses()) {
            if (dlClause.isRoleInverseInclusion()) {
                AtomicAbstractRole subrole=(AtomicAbstractRole)dlClause.getBodyAtom(0).getDLPredicate();
                AtomicAbstractRole superrole=(AtomicAbstractRole)dlClause.getHeadAtom(0,0).getDLPredicate();
                m_roleHierarchy.addInclusion(subrole,superrole.getInverseRole());
                m_roleHierarchy.addInclusion(subrole.getInverseRole(),superrole);
            }
            else if (dlClause.isRoleInclusion()) {
                AtomicAbstractRole subrole=(AtomicAbstractRole)dlClause.getBodyAtom(0).getDLPredicate();
                AtomicAbstractRole superrole=(AtomicAbstractRole)dlClause.getHeadAtom(0,0).getDLPredicate();
                m_roleHierarchy.addInclusion(subrole,superrole);
                m_roleHierarchy.addInclusion(subrole.getInverseRole(),superrole.getInverseRole());
            }
            else if (dlClause.isFunctionalityAxiom()) {
                AtomicAbstractRole functionalRole=(AtomicAbstractRole)dlClause.getBodyAtom(0).getDLPredicate();
                m_functionalRoles.add(functionalRole);
            }
            else if (dlClause.isConceptInclusion()) {
                AtomicConcept bodyConcept=(AtomicConcept)dlClause.getBodyAtom(0).getDLPredicate();
                Concept headConcept=(Concept)dlClause.getHeadAtom(0,0).getDLPredicate();
                m_conceptHierarchy.addInclusion(bodyConcept,headConcept);
            }
            for (int i=0;i<dlClause.getHeadLength();i++)
                for (int j=0;j<dlClause.getHeadConjunctionLength(i);j++) {
                    DLPredicate dlPredicate=dlClause.getHeadAtom(i,j).getDLPredicate();
                    if (dlPredicate instanceof AtLeastAbstractRoleConcept) {
                        Concept toConcept=((AtLeastAbstractRoleConcept)dlPredicate).getToConcept();
                        if (toConcept instanceof AtomicConcept)
                            m_graphConcepts.add((AtomicConcept)toConcept);
                    }
                }
        }
    }
    public void save(File reuseConcepts,File dontReuseConcepts) throws IOException {
        PrintWriter dontReuseWriter=new PrintWriter(new FileWriter(dontReuseConcepts));
        try {
            for (AtomicConcept atomicConcept : m_dontReuseConcepts)
                dontReuseWriter.println(atomicConcept.getURI());
        }
        finally {
            dontReuseWriter.close();
        }
        PrintWriter reuseWriter=new PrintWriter(new FileWriter(reuseConcepts));
        try {
            for (AtomicConcept atomicConcept : m_dlOntology.getAllAtomicConcepts())
                if (!m_dontReuseConcepts.contains(atomicConcept))
                    reuseWriter.println(atomicConcept.getURI());
        }
        finally {
            reuseWriter.close();
        }
    }
    
    protected static class EntityInfo<T> {
        public final T m_entity;
        public final Set<EntityInfo<T>> m_superentityInfos;
        
        public EntityInfo(T entity) {
            m_entity=entity;
            m_superentityInfos=new HashSet<EntityInfo<T>>();
        }
        public Set<EntityInfo<T>> getAllSuperentities() {
            Set<EntityInfo<T>> superentities=new HashSet<EntityInfo<T>>();
            List<EntityInfo<T>> unprocessed=new ArrayList<EntityInfo<T>>();
            unprocessed.add(this);
            while (!unprocessed.isEmpty()) {
                EntityInfo<T> entityInfo=unprocessed.remove(unprocessed.size()-1);
                superentities.add(entityInfo);
                for (EntityInfo<T> superentityInfo : entityInfo.m_superentityInfos)
                    if (!superentities.contains(superentityInfo))
                        unprocessed.add(superentityInfo);
            }
            return superentities;
        }
        public String toString() {
            return m_entity.toString();
        }
    }

    protected static class NodePair {
        protected final Node m_first;
        protected final Node m_second;
        
        public NodePair(Node first,Node second) {
            m_first=first;
            m_second=second;
        }
        public int hashCode() {
            return m_first.hashCode()+m_second.hashCode();
        }
        public boolean equals(Object that) {
            if (this==that)
                return true;
            if (!(that instanceof NodePair))
                return false;
            NodePair thatPair=(NodePair)that;
            return m_first==thatPair.m_first && m_second==thatPair.m_second;
        }
    }
    
    public static void main(String[] args) throws Exception {
//        String physicalURI="file:/C:/Work/ontologies/GALEN/galen-ians-full-undoctored.owl";
        String physicalURI="file:/C:/Work/ontologies/GALEN/galen-module1.owl";
        
        DLOntology dlOntology=loadDLOntology(physicalURI);
        Analysis analysis=new Analysis(dlOntology);
        analysis.save(new File("c:\\Temp\\reuse.txt"),new File("c:\\Temp\\dont-reuse.txt"));
    }
    protected static DLOntology loadDLOntology(String physicalURI) throws Exception {
        DefaultOntologyResolver resolver=new DefaultOntologyResolver();
        String ontologyURI=resolver.registerOntology(physicalURI);
        KAON2Connection connection=KAON2Manager.newConnection();
        connection.setOntologyResolver(resolver);
        Ontology ontology=connection.openOntology(ontologyURI,new HashMap<String,Object>());
        Clausification clausification=new Clausification();
        Set<DescriptionGraph> noDescriptionGraphs=Collections.emptySet();
        return clausification.clausify(false,ontology,true,noDescriptionGraphs);
    }
}
