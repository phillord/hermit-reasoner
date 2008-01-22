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

import org.semanticweb.HermiT.model.*;
import org.semanticweb.HermiT.tableau.*;
import org.semanticweb.HermiT.disjunction.*;

public class Analysis {
    protected final DLOntology m_dlOntology;
    protected final Set<AtomicConcept> m_dontReuseConcepts;
    
    public Analysis(DLOntology dlOntology) {
        m_dlOntology=dlOntology;
        m_dontReuseConcepts=new TreeSet<AtomicConcept>(DLOntology.AtomicConceptComparator.INSTANCE);
        Set<AtomicAbstractRole> functionalRoles=new HashSet<AtomicAbstractRole>();
        EntityHierarchy<Concept> conceptHierarchy=new EntityHierarchy<Concept>();
        EntityHierarchy<AbstractRole> roleHierarchy=new EntityHierarchy<AbstractRole>();
        buildApproximation(conceptHierarchy,roleHierarchy,functionalRoles);
        excludeLeafConcepts(conceptHierarchy);
        Map<NodePair,Node> arcCauses=new HashMap<NodePair,Node>();
        Map<AtomicConcept,Node> representativeNodes=new HashMap<AtomicConcept,Node>();
        Map<Node,AtomicConcept> conceptForNode=new HashMap<Node,AtomicConcept>();
        ExtensionTable extensionTable=buildExtensionTable(representativeNodes,conceptForNode,arcCauses);
        applyHierarchy(extensionTable,conceptHierarchy,roleHierarchy,functionalRoles,representativeNodes,conceptForNode,arcCauses);
        int tuplesToprocess=0;
        ExtensionTable.Retrieval retrieval1=extensionTable.createRetrieval(new boolean[] { false,false,false },ExtensionTable.View.TOTAL);
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
            ExtensionTable.Retrieval retrieval2=extensionTable.createRetrieval(new boolean[] { true,true,false },ExtensionTable.View.TOTAL);
            retrieval2.getBindingsBuffer()[0]=retrieval1.getTupleBuffer()[0];
            retrieval2.getBindingsBuffer()[1]=centralNode;
            retrieval2.open();
            while (!retrieval2.afterLast()) {
                Node leafNode2=(Node)retrieval2.getTupleBuffer()[2];
                if (leafNode1.getNodeID()<leafNode2.getNodeID()) {
                    Node cause1=arcCauses.get(new NodePair(centralNode,leafNode1));
                    if (cause1==centralNode)
                        dontReuseConceptDueToMerging(conceptForNode.get(leafNode1));
                    else if (cause1==leafNode1)
                        dontReuseConceptDueToMerging(conceptForNode.get(centralNode));
                    else
                        throw new IllegalStateException();
                    Node cause2=arcCauses.get(new NodePair(centralNode,leafNode2));
                    if (cause2==centralNode)
                        dontReuseConceptDueToMerging(conceptForNode.get(leafNode2));
                    else if (cause2==leafNode2)
                        dontReuseConceptDueToMerging(conceptForNode.get(centralNode));
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
    protected void applyHierarchy(ExtensionTable extensionTable,EntityHierarchy<Concept> conceptHierarchy,EntityHierarchy<AbstractRole> roleHierarchy,Set<AtomicAbstractRole> functionalRoles,Map<AtomicConcept,Node> representativeNodes,Map<Node,AtomicConcept> conceptForNode,Map<NodePair,Node> arcCauses) {
        Object[] buffer=new Object[3];
        Map<AbstractRole,Set<AbstractRole>> allSuperroles=roleHierarchy.getSuperentities();
        int done=0;
        for (Map.Entry<Node,AtomicConcept> entry : conceptForNode.entrySet()) {
            Node node=entry.getKey();
            Set<EntityInfo<Concept>> allSuperconcepts=conceptHierarchy.getEntityInfo(entry.getValue()).getAllSuperentities();
            for (EntityInfo<Concept> superconcept : allSuperconcepts)
                if (superconcept.m_entity instanceof AtLeastAbstractRoleConcept) {
                    AtLeastAbstractRoleConcept existentialConcept=(AtLeastAbstractRoleConcept)superconcept.m_entity;
                    AtomicConcept toConcept=(AtomicConcept)existentialConcept.getToConcept();
                    Node toNode=representativeNodes.get(toConcept);
                    if (toNode!=null) {
                        Set<AbstractRole> superroles=allSuperroles.get(existentialConcept.getOnAbstractRole());
                        if (superroles==null) {
                            superroles=new HashSet<AbstractRole>();
                            superroles.add(existentialConcept.getOnAbstractRole());
                            allSuperroles.put(existentialConcept.getOnAbstractRole(),superroles);
                        }
                        boolean change=false;
                        for (AbstractRole onAbstractRole : superroles) {
                            if (onAbstractRole instanceof AtomicAbstractRole) {
                                if (functionalRoles.contains(onAbstractRole)) {
                                    buffer[0]=onAbstractRole;
                                    buffer[1]=node;
                                    buffer[2]=toNode;
                                    if (extensionTable.addTuple(buffer,null))
                                        change=true;
                                }
                            }
                            else if (onAbstractRole instanceof InverseAbstractRole) {
                                AtomicAbstractRole inverseAbstractRole=((InverseAbstractRole)onAbstractRole).getInverseOf();
                                if (functionalRoles.contains(inverseAbstractRole)) {
                                    buffer[0]=inverseAbstractRole;
                                    buffer[1]=toNode;
                                    buffer[2]=node;
                                    if (extensionTable.addTuple(buffer,null))
                                        change=true;
                                }
                            }
                        }
                        if (change) {
                            arcCauses.put(new NodePair(node,toNode),node);
                            arcCauses.put(new NodePair(toNode,node),node);
                        }
                    }
                }
            done++;
            if ((done % 1000)==0)
                System.out.println(done+" nodes done");
        }
    }
    protected ExtensionTable buildExtensionTable(Map<AtomicConcept,Node> representativeNodes,Map<Node,AtomicConcept> conceptForNode,Map<NodePair,Node> arcCauses) {
        Set<DLClause> noDLClauses=Collections.emptySet();
        Set<Atom> noAtoms=Collections.emptySet();
        DLOntology emptyDLOntology=new DLOntology("nothing",noDLClauses,noAtoms,noAtoms,false,false,false,false);
        Tableau tableau=new Tableau(null,new CreationOrderStrategy(null),new MostRecentDisjunctionProcessingStrategy(),emptyDLOntology);
        ExtensionTable extensionTable=new ExtensionTableWithTupleIndexes(tableau,tableau.getExtensionManager(),3,false,new TupleIndex[] { new TupleIndex(new int[] { 0,1,2 }) });
        DependencySet emptySet=tableau.getDependencySetFactory().emptySet();
        for (AtomicConcept atomicConcept : m_dlOntology.getAllAtomicConcepts())
            if (!m_dontReuseConcepts.contains(atomicConcept)) {
                Node node=tableau.createNewRootNode(emptySet,0);
                representativeNodes.put(atomicConcept,node);
                conceptForNode.put(node,atomicConcept);
            }
        return extensionTable;
    }
    protected void buildApproximation(EntityHierarchy<Concept> conceptHierarchy,EntityHierarchy<AbstractRole> roleHierarchy,Set<AtomicAbstractRole> functionalRoles) {
        for (AtomicConcept atomicConcept : m_dlOntology.getAllAtomicConcepts()) {
            if (atomicConcept.getURI().startsWith("internal:"))
                dontReuseConcept(atomicConcept);
            else
                conceptHierarchy.getEntityInfo(atomicConcept);
        }
        for (DLClause dlClause : m_dlOntology.getDLClauses()) {
            if (isInverseRoleClause(dlClause)) {
                AtomicAbstractRole subrole=(AtomicAbstractRole)dlClause.getBodyAtom(0).getDLPredicate();
                AtomicAbstractRole superrole=(AtomicAbstractRole)dlClause.getHeadAtom(0,0).getDLPredicate();
                roleHierarchy.addSubsumption(subrole,superrole.getInverseRole());
                roleHierarchy.addSubsumption(subrole.getInverseRole(),superrole);
            }
            else if (isRoleHierarchyClause(dlClause)) {
                AtomicAbstractRole subrole=(AtomicAbstractRole)dlClause.getBodyAtom(0).getDLPredicate();
                AtomicAbstractRole superrole=(AtomicAbstractRole)dlClause.getHeadAtom(0,0).getDLPredicate();
                roleHierarchy.addSubsumption(subrole,superrole);
                roleHierarchy.addSubsumption(subrole.getInverseRole(),superrole.getInverseRole());
            }
            else if (isFunctionalityClause(dlClause)) {
                AtomicAbstractRole functionalRole=(AtomicAbstractRole)dlClause.getBodyAtom(0).getDLPredicate();
                functionalRoles.add(functionalRole);
            }
            else if (isConceptInclusionClause(dlClause)) {
                AtomicConcept bodyConcept=(AtomicConcept)dlClause.getBodyAtom(0).getDLPredicate();
                Concept headConcept=(Concept)dlClause.getHeadAtom(0,0).getDLPredicate();
                if (!m_dontReuseConcepts.contains(bodyConcept) && (!(headConcept instanceof AtomicConcept) || !m_dontReuseConcepts.contains(headConcept)))
                    conceptHierarchy.addSubsumption(bodyConcept,headConcept);
            }
        }
    }
    protected boolean isAtomicOrExistentialToAtomic(DLPredicate dlPredicate) {
        if (dlPredicate instanceof AtomicConcept)
            return true;
        else if (dlPredicate instanceof AtLeastAbstractRoleConcept) {
            AtLeastAbstractRoleConcept atLeastAbstractRoleConcept=(AtLeastAbstractRoleConcept)dlPredicate;
            return atLeastAbstractRoleConcept.getNumber()==1 && atLeastAbstractRoleConcept.getToConcept() instanceof AtomicConcept;
        }
        else
            return false;
    }
    protected boolean isConceptInclusionClause(DLClause dlClause) {
        if (dlClause.getBodyLength()==1 && dlClause.getHeadLength()==1 && dlClause.getHeadConjunctionLength(0)==1) {
            if (dlClause.getBodyAtom(0).getDLPredicate() instanceof AtomicConcept && dlClause.getHeadAtom(0,0).getDLPredicate() instanceof Concept) {
                Variable x=dlClause.getBodyAtom(0).getArgumentVariable(0);
                Variable headX=dlClause.getHeadAtom(0,0).getArgumentVariable(0);
                if (x!=null && x.equals(headX))
                    return true;
            }
        }
        return false;
    }
    protected boolean isRoleHierarchyClause(DLClause dlClause) {
        if (dlClause.getBodyLength()==1 && dlClause.getHeadLength()==1 && dlClause.getHeadConjunctionLength(0)==1) {
            if (dlClause.getBodyAtom(0).getDLPredicate() instanceof AtomicAbstractRole && dlClause.getHeadAtom(0,0).getDLPredicate() instanceof AtomicAbstractRole) {
                Variable x=dlClause.getBodyAtom(0).getArgumentVariable(0);
                Variable y=dlClause.getBodyAtom(0).getArgumentVariable(1);
                Variable headX=dlClause.getHeadAtom(0,0).getArgumentVariable(0);
                Variable headY=dlClause.getHeadAtom(0,0).getArgumentVariable(1);
                if (x!=null && y!=null && !x.equals(y) && x.equals(headX) && y.equals(headY))
                    return true;
            }
        }
        return false;
    }
    protected boolean isInverseRoleClause(DLClause dlClause) {
        if (dlClause.getBodyLength()==1 && dlClause.getHeadLength()==1 && dlClause.getHeadConjunctionLength(0)==1) {
            if (dlClause.getBodyAtom(0).getDLPredicate() instanceof AtomicAbstractRole && dlClause.getHeadAtom(0,0).getDLPredicate() instanceof AtomicAbstractRole) {
                Variable x=dlClause.getBodyAtom(0).getArgumentVariable(0);
                Variable y=dlClause.getBodyAtom(0).getArgumentVariable(1);
                Variable headX=dlClause.getHeadAtom(0,0).getArgumentVariable(0);
                Variable headY=dlClause.getHeadAtom(0,0).getArgumentVariable(1);
                if (x!=null && y!=null && !x.equals(y) && x.equals(headY) && y.equals(headX))
                    return true;
            }
        }
        return false;
    }
    protected boolean isFunctionalityClause(DLClause dlClause) {
        if (dlClause.getBodyLength()==2 && dlClause.getHeadLength()==1 && dlClause.getHeadConjunctionLength(0)==1) {
            DLPredicate atomicAbstractRole=dlClause.getBodyAtom(0).getDLPredicate();
            if (atomicAbstractRole instanceof AtomicAbstractRole) {
                if (dlClause.getBodyAtom(1).getDLPredicate().equals(atomicAbstractRole) && dlClause.getHeadAtom(0,0).getDLPredicate().equals(Equality.INSTANCE)) {
                    Variable x=dlClause.getBodyAtom(0).getArgumentVariable(0);
                    if (x!=null && x.equals(dlClause.getBodyAtom(1).getArgument(0))) {
                        Variable y1=dlClause.getBodyAtom(0).getArgumentVariable(1);
                        Variable y2=dlClause.getBodyAtom(1).getArgumentVariable(1);
                        Variable headY1=dlClause.getHeadAtom(0,0).getArgumentVariable(0);
                        Variable headY2=dlClause.getHeadAtom(0,0).getArgumentVariable(1);
                        if (y1!=null && y2!=null && !y1.equals(y2) && headY1!=null && headY2!=null && ((y1.equals(headY1) && y2.equals(headY2)) || (y1.equals(headY2) && y2.equals(headY1))))
                            return true;
                    }
                }
            }
        }
        return false;
    }
    protected void excludeLeafConcepts(EntityHierarchy<Concept> conceptHierarchy) {
        for (EntityInfo<Concept> conceptInfo : conceptHierarchy.m_entityInfos.values())
            if (conceptInfo.m_entity instanceof AtomicConcept) {
                for (EntityInfo<Concept> superconceptInfo : conceptInfo.m_superentityInfos)
                    if (superconceptInfo.m_entity instanceof AtomicConcept)
                        dontReuseConcept((AtomicConcept)superconceptInfo.m_entity);
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

    protected static class EntityHierarchy<T> {
        protected final Map<T,EntityInfo<T>> m_entityInfos;
        
        public EntityHierarchy() {
            m_entityInfos=new HashMap<T,EntityInfo<T>>();
        }
        public void addSubsumption(T subentity,T superentity) {
            getEntityInfo(subentity).m_superentityInfos.add(getEntityInfo(superentity));
        }
        protected EntityInfo<T> getEntityInfo(T entity) {
            EntityInfo<T> entityInfo=m_entityInfos.get(entity);
            if (entityInfo==null) {
                entityInfo=new EntityInfo<T>(entity);
                m_entityInfos.put(entity,entityInfo);
            }
            return entityInfo;
        }
        public Map<T,Set<T>> getSuperentities() {
            Map<T,Set<T>> result=new HashMap<T,Set<T>>();
            for (EntityInfo<T> entityInfo : m_entityInfos.values()) {
                Set<T> superentities=new HashSet<T>();
                result.put(entityInfo.m_entity,superentities);
                for (EntityInfo<T> superentityInfo : entityInfo.getAllSuperentities())
                    superentities.add(superentityInfo.m_entity);
            }
            return result;
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
        DLOntology dlOntology=DLOntology.load(new File("c:\\Temp\\galen-module1.ser"));
        Analysis analysis=new Analysis(dlOntology);
        analysis.save(new File("c:\\Temp\\reuse.txt"),new File("c:\\Temp\\dont-reuse.txt"));
    }
}
