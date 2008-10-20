package org.semanticweb.HermiT.tableau;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.semanticweb.HermiT.model.AtomicRole;
import org.semanticweb.HermiT.model.DLPredicate;
import org.semanticweb.HermiT.model.Inequality;
import org.semanticweb.HermiT.model.dataranges.CanonicalDataRange;
import org.semanticweb.HermiT.model.dataranges.DataConstant;
import org.semanticweb.HermiT.model.dataranges.DataRange;
import org.semanticweb.HermiT.model.dataranges.DatatypeRestrictionBoolean;
import org.semanticweb.HermiT.model.dataranges.DatatypeRestrictionDateTime;
import org.semanticweb.HermiT.model.dataranges.DatatypeRestrictionInteger;
import org.semanticweb.HermiT.model.dataranges.DatatypeRestrictionLiteral;
import org.semanticweb.HermiT.model.dataranges.DatatypeRestrictionOWLRealPlus;
import org.semanticweb.HermiT.model.dataranges.DatatypeRestrictionString;
import org.semanticweb.HermiT.model.dataranges.EnumeratedDataRange;
import org.semanticweb.HermiT.monitor.TableauMonitor;
import org.semanticweb.owl.vocab.XSDVocabulary;

public class DatatypeManager {
    protected final TableauMonitor tableauMonitor;
    protected final ExtensionManager extensionManager;
    protected final ExtensionTable.Retrieval pairsDeltaOld;
    protected final ExtensionTable.Retrieval triplesFirstBoundRetr;
    protected final ExtensionTable.Retrieval triplesSecondBoundRetr;
    protected final ExtensionTable.Retrieval pairsFirstBoundRetr;
    protected final ExtensionTable.Retrieval triplesZeroBoundRetr;
    protected final UnionDependencySet dependencySetTwoCauses;
    
    public DatatypeManager(Tableau tableau) {
        tableauMonitor = tableau.m_tableauMonitor;
        extensionManager = tableau.m_extensionManager;
        // retrieval object for all the datatype assertions in the changed part of the tuple table
        pairsDeltaOld = extensionManager.getBinaryExtensionTable().createRetrieval(new boolean[2],ExtensionTable.View.DELTA_OLD);
        // retrieval object to fetch the parent node of a data range node
        triplesFirstBoundRetr = extensionManager.getTernaryExtensionTable().createRetrieval(new boolean[] { false,true,false }, ExtensionTable.View.TOTAL);
        // retrieval object to fetch the parent node of a data range node
        triplesSecondBoundRetr = extensionManager.getTernaryExtensionTable().createRetrieval(new boolean[] { false,false,true }, ExtensionTable.View.TOTAL);
        // retrieval object for all datatype assertions that use the same variable as one from the above retrieval
        pairsFirstBoundRetr = extensionManager.getBinaryExtensionTable().createRetrieval(new boolean[] { false,true },ExtensionTable.View.TOTAL);
        // retrieval object for inequalities
        triplesZeroBoundRetr = extensionManager.getTernaryExtensionTable().createRetrieval(new boolean[] { true,false,false },ExtensionTable.View.TOTAL);
        dependencySetTwoCauses = new UnionDependencySet(2);
    }
    
    /**
     * Checks if in the last iteration a new data range assertion has been added 
     * and if so, whether the restrictions on this newly added node and its data 
     * range siblings are satisfiable given the asserted inequalities.  
     * @return true if the data range assertions are satisfiable and false 
     *         otherwise
     */
    public boolean checkDatatypeConstraints() {
        if (tableauMonitor != null) {
            tableauMonitor.datatypeCheckingStarted();
        }
        boolean datatypesSat = true;
        Set<DataRange> checkedDRs = new HashSet<DataRange>();
        Object[] pair = pairsDeltaOld.getTupleBuffer();
        pairsDeltaOld.open();
        while (datatypesSat && !pairsDeltaOld.afterLast()) {
            if (pair[0] instanceof DataRange && !checkedDRs.contains((DataRange) pair[0])) {
                // in the last saturation, we added a DataRange, so lets check 
                // whether this caused a clash
                Map<Node,Set<DataRange>> nodeToDRs = fetchRelevantAssertions((Node) pair[1]);
                datatypesSat = checkDatatypeAssertionFor(nodeToDRs);
                if (datatypesSat) {
                    // remember, which ranges we have already checked because 
                    // pairsDeltaOld can contain more than one data range 
                    // assertion and we check not just a found assertion, but 
                    // the all data range assertions for the node and its data 
                    // range siblings
                    for (Node node : nodeToDRs.keySet()) {
                        checkedDRs.addAll(nodeToDRs.get(node));
                    }
                }
            }
            pairsDeltaOld.next();
        }
        if (tableauMonitor != null) {
            tableauMonitor.datatypeCheckingFinished(datatypesSat);
        }
        return datatypesSat;
    }
    
    /**
     * Given a node that has a data range assertion in the extension table, 
     * look up its predecessor node in the tableau (unique for data range nodes) 
     * and collect all successors of the fond predecessor that have data range 
     * assertions. 
     * @param initialNode a node that has a data range assertion in the 
     *        extension table
     * @return A map that contains as keys the given node and its siblings from 
     *         the tableau that also have data range assertions. For each key n1 
     *         that mapping contains all data range assertion for n1 from the 
     *         extension table. 
     */
    protected Map<Node,Set<DataRange>> fetchRelevantAssertions(Node initialNode) {
        Map<Node,Set<DataRange>> nodeToDRs = new HashMap<Node,Set<DataRange>>();
        Set<DataRange> dataRanges;
        // find the predecessor of this node and collect all datatype 
        // successors that the predecessor has
        Object[] triple = triplesSecondBoundRetr.getTupleBuffer();
        triplesSecondBoundRetr.getBindingsBuffer()[2] = initialNode;
        triplesSecondBoundRetr.open();
        Node predecessor = (Node) triple[1];
        
        // collect all the data range successors for the parent node
        triple = triplesFirstBoundRetr.getTupleBuffer();
        triplesFirstBoundRetr.getBindingsBuffer()[1]=predecessor;
        triplesFirstBoundRetr.open();
        while (!triplesFirstBoundRetr.afterLast()) {
            DLPredicate predicate = (DLPredicate) triple[0];
            if (predicate instanceof AtomicRole 
                    && ((AtomicRole)predicate).isRestrictedToDatatypes()) {
                Node successor = (Node) triple[2];
                Object[] assertionsForSuccessor = pairsFirstBoundRetr.getTupleBuffer();
                pairsFirstBoundRetr.getBindingsBuffer()[1] = successor;
                pairsFirstBoundRetr.open();
                while (!pairsFirstBoundRetr.afterLast()) {
                    dataRanges = new HashSet<DataRange>();
                    dataRanges.add((DataRange) assertionsForSuccessor[0]);
                    if (nodeToDRs.containsKey(successor)) {
                        dataRanges.addAll(nodeToDRs.get(successor));
                    }
                    nodeToDRs.put(successor, dataRanges);
                    pairsFirstBoundRetr.next();
                }
            }
            triplesFirstBoundRetr.next();
        }
        return nodeToDRs;
    }
    
    /**
     * Given a map with nodes and data range assertions for the nodes, check 
     * whether there is an assignment for all the data ranges that is consistent 
     * with the restrictions on the data ranges and the inequalities
     * @param nodeToDRs a mapping from nodes to their asserted data ranges
     * @return true if a consistent assignment exists, false otherwise
     */
    protected boolean checkDatatypeAssertionFor(Map<Node,Set<DataRange>> nodeToDRs) {
        // test for trivial unsatisfiability:
        // Is there a dataRange assertion that is bottom?
        for (Node node : nodeToDRs.keySet()) {
            for (DataRange dataRange : nodeToDRs.get(node)) {
                if (dataRange.isBottom()) {
                    if (tableauMonitor != null) {
                        tableauMonitor.clashDetected(new Object[][] {
                                new Object[] { dataRange, node } });
                        tableauMonitor.datatypeCheckingFinished(false);
                    }
                    extensionManager.setClash(
                            extensionManager.getAssertionDependencySet(dataRange, node));
                }
            }
        }
        
        // collect the inequalities and raise a clash when a self inequality is 
        // found 
        Map<Node,Set<Node>> inequalities = new HashMap<Node,Set<Node>>();
        Map<Node,Set<Node>> inequalitiesSym = new HashMap<Node,Set<Node>>();
        boolean foundSelfInequality = fetchInequalities(nodeToDRs.keySet(), 
                        inequalities, inequalitiesSym);
        if (foundSelfInequality) return false; // clash has been raised
        
        // conjoin all data ranges for each node into a canonical range 
        // leave the original ranges unchanged for backtracking
        Map<Node, CanonicalDataRange> nodeToCanonicalDR = buildCanonicalRanges(nodeToDRs);
        if (nodeToCanonicalDR == null) {
            // found a clash while joining the ranges, clash has been raised
            return false; 
        }
        
        // take out those that have more values than needed for the inequalities
        removeUnderRestrictedRanges(nodeToCanonicalDR, inequalities, inequalitiesSym);

        // Now we have a set of data ranges that have not enough trivial 
        // assignments, so we really have to check whether we can find a 
        // suitable assignments for the allowed values.
        // First we partition the nodes and their data ranges. 
        Set<Map<CanonicalDataRange, Node>> partitions = buildPartitions(
                nodeToCanonicalDR, inequalities, inequalitiesSym);

        for (Map<CanonicalDataRange, Node> partition : partitions) {
            boolean hasAssignment = hasAssignment(partition, 
                    inequalities,  
                    inequalitiesSym, 
                    nodeToDRs); 
            if (!hasAssignment) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Given a set of nodes for which there are data range assertions in the 
     * tableau, collect all inequalities for the nodes. If a self inequality is 
     * found (neq node node) a clash is raised and the method return true. 
     * @param nodes the set of nodes for which inequalities are collected
     * @param inequalities is a map that is initially assumed to be empty. 
     *        After running the method, it contains as keys all nodes n1 from 
     *        the given set nodes for which there is an inequality assertion of 
     *        the form neq n1 n2 in the extension table; the set of values 
     *        for each key n1 consist of all those nodes n2 for which neq n1 n2 
     *        is in the extension table. 
     * @param inequalitiesSym is a map that is initially assumed to be 
     *        empty. After running the method, it contains the symmetric 
     *        counterpart of inequalities. I.e., it contains as keys all 
     *        nodes n1 from the given set nodes for which there is an inequality 
     *        assertion of the form neq n2 n1 in the extension table; the set of 
     *        values for each key n1 consist of all those nodes n2 for which 
     *        neq n2 n1 is in the extension table. 
     * @return true if a tuple neq n1 n1 is found while fetching the 
     *         inequalities from the extension table and false otherwise
     */
    protected boolean fetchInequalities(Set<Node> nodes, 
            Map<Node, Set<Node>> inequalities, 
            Map<Node, Set<Node>> inequalitiesSym) {
        Object[] triple = triplesZeroBoundRetr.getTupleBuffer();
        triplesZeroBoundRetr.getBindingsBuffer()[0]=Inequality.INSTANCE;
        triplesZeroBoundRetr.open();
        while (!triplesZeroBoundRetr.afterLast()) {
            Node node1 = (Node) triple[1];
            Node node2 = (Node) triple[2];
            if (node1.equals(node2)) {
                if (tableauMonitor != null) {
                    tableauMonitor.clashDetected(new Object[][] {
                            new Object[] { Inequality.INSTANCE, node1, node2 } });
                    tableauMonitor.datatypeCheckingFinished(false);
                }
                extensionManager.setClash(triplesZeroBoundRetr.getDependencySet());
                return true;
            }
            if (nodes.contains(node1)) {
                // found an inequality between datatype nodes
                // save inequalities for node1
                Set<Node> inequalNodes = new HashSet<Node>();
                inequalNodes.add(node2);
                // add also all already known inequalities
                if (inequalities.containsKey(node1)) {
                    inequalNodes.addAll(inequalities.get(node1));
                }
                inequalities.put(node1, inequalNodes);
                
                // save inequalities for node2
                inequalNodes = new HashSet<Node>();
                inequalNodes.add(node1);
                if (inequalitiesSym.containsKey(node2)) {
                    // add also all already known inequalities
                    inequalNodes.addAll(inequalitiesSym.get(node2));
                }
                inequalitiesSym.put(node2, inequalNodes);
            }
            triplesZeroBoundRetr.next();
        }
        return false;
    }
    
    /**
     * Given a map from nodes to a set of data ranges, construct a map from 
     * nodes to data ranges such that each constructed data range captures all 
     * restrictions of the data ranges in the set for the node in the given map. 
     * If any constructed canonical data range is equal to bottom, a clash is 
     * raised and null is returned. The canonical ranges have either oneOf 
     * values or facets (possibly empty) and notOneOfs, which are constants 
     * unsuitable for assignment (possibly empty). 
     * @param nodeToDRs a map from nodes to sets of data ranges 
     *        n -> {dr_1, ..., dr_n}
     * @return a map from nodes to data ranges n -> dr such that dr captures the 
     *         restrictions of {dr_1, ..., dr_n} or null if conjoining the 
     *         ranges lead to a clash (constructed range is bottom)
     */
    protected Map<Node, CanonicalDataRange> buildCanonicalRanges(Map<Node, Set<DataRange>> nodeToDRs) {
        Map<Node, CanonicalDataRange> nodeToCanonicalDR = new HashMap<Node, CanonicalDataRange>();
        for (Node node : nodeToDRs.keySet()) {
            CanonicalDataRange canonicalRange;
            if (nodeToDRs.get(node).size() > 1) {
                canonicalRange = getCanonicalDataRange(nodeToDRs.get(node));
                if (canonicalRange == null || canonicalRange.isBottom()) {
                    // conjoining the restrictions led to a clash
                    int numberOfCauses = nodeToDRs.get(node).size();
                    UnionDependencySet ds = new UnionDependencySet(numberOfCauses);
                    Object[][] causes = new Object[numberOfCauses][2];
                    int i = 0;
                    for (DataRange range : nodeToDRs.get(node)) {
                        causes[i][0] = range;
                        causes[i][1] = node;
                        ds.m_dependencySets[i] = extensionManager.getAssertionDependencySet(range, node);
                        i++;
                    }
                    if (tableauMonitor != null) {
                        tableauMonitor.clashDetected(causes);
                        tableauMonitor.datatypeCheckingFinished(false);
                    }
                    extensionManager.setClash(ds);
                    return null;
                }
            } else {
                canonicalRange = (CanonicalDataRange) nodeToDRs.get(node).iterator().next();
            }
            nodeToCanonicalDR.put(node, canonicalRange);
        }
        return nodeToCanonicalDR;
    }
    
    /**
     * Given a set of data ranges, construct a data range that captures all the 
     * restrictions of the given ranges. 
     * @param ranges a set of data ranges
     * @return a data range that captures all restrictions from the data ranges 
     *         in the given set ranges or null this is not possible due to 
     *         different data types or contradiction oneOf values in the given 
     *         ranges
     */
    protected CanonicalDataRange getCanonicalDataRange(Set<DataRange> ranges) {
        // create a new instance of the type that the first positive data range has
        CanonicalDataRange canonicalDR = null;
        Set<URI> uris = null; 
        // set the URI to the one of the first positive data range, we then 
        // expect all ranges to have the same URI or it is a clash
        boolean onlyNegated = true;
        Set<DataConstant> constants = null;
        Set<DataConstant> forbiddenConstants = new HashSet<DataConstant>();
        for (DataRange range : ranges) {
            if (!range.isNegated()) {
                onlyNegated = false;
                if (range.getDatatypeURI() != null) {
                    // found a datatype restriction, which cannot have oneOfs
                    if (uris == null) {
                        uris = new HashSet<URI>();
                        //canonicalDR = range.getNewInstance();
                    } 
                    uris.add(range.getDatatypeURI());
                    //else {
//                        if (!range.getDatatypeURI().equals(canonicalDR.getDatatypeURI())) {
//                            // two non-negated ones with different URIs -> clash
//                            return null;
//                        }
//                    }
                } else {
                    // not a datatype restriction, so it is an enumerated range
                    if (constants == null) {
                        constants = range.getOneOf();
                    } else {
                        constants.retainAll(range.getOneOf());
                    }
                }
            } else {
                if (!range.getOneOf().isEmpty()) {
                    // range has notOneOfs
                    forbiddenConstants.addAll(range.getOneOf());
                }
            }
        }
        if (onlyNegated) {
            // only negated ranges -> trivially satisfiable
            return new DatatypeRestrictionLiteral(XSDVocabulary.BOOLEAN.getURI());
        }
        if (constants != null) {
            constants.removeAll(forbiddenConstants);
            if (constants.isEmpty()) {
                // we had oneOfs, but they were not compatible
                return null;
            }
        }
        if (uris == null) {
            // all ranges are enumerated ones
            if (constants == null) return null;
            canonicalDR = new EnumeratedDataRange();
            canonicalDR.setOneOf(constants);
            return canonicalDR;
        } else {
            // find which implementation to use
            if (DatatypeRestrictionOWLRealPlus.canHandleAll(uris)) {
                canonicalDR = new DatatypeRestrictionOWLRealPlus(URI.create(org.semanticweb.owl.vocab.Namespaces.OWL + "realPlus"));
                if (uris.contains(URI.create(org.semanticweb.owl.vocab.Namespaces.OWL + "real")) 
                        || uris.contains(URI.create(org.semanticweb.owl.vocab.Namespaces.XSD + "decimal"))) {
                    forbiddenConstants.addAll(DataConstant.numericSpecials);
                }
            } else if (DatatypeRestrictionInteger.canHandleAll(uris)) {
                canonicalDR = new DatatypeRestrictionInteger(URI.create(org.semanticweb.owl.vocab.Namespaces.XSD + "integer"));
            } else if (DatatypeRestrictionBoolean.canHandleAll(uris)) {
                canonicalDR = new DatatypeRestrictionInteger(URI.create(org.semanticweb.owl.vocab.Namespaces.XSD + "boolean"));
            } else if (DatatypeRestrictionDateTime.canHandleAll(uris)) {
                canonicalDR = new DatatypeRestrictionDateTime(URI.create(org.semanticweb.owl.vocab.Namespaces.XSD + "dateTime"));
            } else if (DatatypeRestrictionString.canHandleAll(uris)) {
                canonicalDR = new DatatypeRestrictionString(URI.create(org.semanticweb.owl.vocab.Namespaces.XSD + "string"));
            } else if (DatatypeRestrictionLiteral.canHandleAll(uris)) {
                canonicalDR = new DatatypeRestrictionLiteral(URI.create(org.semanticweb.owl.vocab.Namespaces.RDFS + "literal"));
            } else {
                return null;
            }
            // if there are oneOf restrictions, check whether there are values 
            // that suit the datatype restriction
            if (constants != null) {
                for (DataConstant constant : constants) {
                    if (!canonicalDR.datatypeAccepts(constant)) {
                        constants.remove(constant);
                    }
                }
                if (constants.isEmpty()) return null;
            }
        }
            
        // now we compute one canonical data range that captures the 
        // restrictions of all the data ranges for the node 

        // lets look at the facets
        for (DataRange range : ranges) {
            if (range.getDatatypeURI() != null) {
                canonicalDR.conjoinFacetsFrom(range);
            }
        }
        
        // if we have oneOfs, we make sure they conform to the facets
        if (constants != null) {
            for (DataConstant constant : constants) {
                if (!canonicalDR.facetsAccept(constant)) {
                    constants.remove(constant);
                }
            }
            if (constants.isEmpty()) {
                // no more suitable values left
                return null;
            }
            canonicalDR.setOneOf(constants);
            return canonicalDR;
        } else {
            canonicalDR.setNotOneOf(forbiddenConstants);
            return canonicalDR;
        }
    }
    
    /**
     * Remove those nodes and data ranges that have more than enough values to 
     * cover the inequalities. 
     * @param nodeToCanonicalDR a map from nodes to canonical data ranges
     * @param inequalities inequalities between nodes
     * @param inequalitiesSym symmetric counterpart for the above relation
     */
    protected void removeUnderRestrictedRanges(
            Map<Node, CanonicalDataRange> nodeToCanonicalDR, 
            Map<Node, Set<Node>> inequalities, 
            Map<Node, Set<Node>> inequalitiesSym) {
        boolean containedRemovable = true;
        Set<Node> removableNodes = new HashSet<Node>();
        while (containedRemovable) {
            containedRemovable = false;
            for (Node node : nodeToCanonicalDR.keySet()) {
                CanonicalDataRange canonicalRange = nodeToCanonicalDR.get(node);
                int numInequalNodes = 0;
                if (inequalities.get(node) != null) {
                    numInequalNodes = inequalities.get(node).size();
                }
                if (inequalitiesSym.get(node) != null) {
                    numInequalNodes += inequalitiesSym.get(node).size();
                }
                if (canonicalRange.hasMinCardinality(numInequalNodes + 1)) {
                    removableNodes.add(node);
                    containedRemovable = true;
                }
            }
            for (Node node  : removableNodes) {
                nodeToCanonicalDR.remove(node);
            }
            removableNodes.clear();
        }
    }
    
    /**
     * Partition the set of nodes and their canonical data ranges together with 
     * their inequalities into mutally disjoint non-empty subsets P1, ..., Pn 
     * such that no Pi and Pj with i neq j have variables in common. 
     * @param nodeToCanonicalDR a map from nodes to canonical data ranges
     * @param inequalities a map from nodes to the nodes for which an inequality 
     *        is known
     * @param inequalitiesSym the symmetric counterpart toinequalitiesSym 
     * @return a set of partitions such that the nodes and their data ranges in 
     *         each partition have inequalities between them 
     */
    protected Set<Map<CanonicalDataRange, Node>> buildPartitions(
            Map<Node, CanonicalDataRange> nodeToCanonicalDR, 
            Map<Node, Set<Node>> inequalities, 
            Map<Node, Set<Node>> inequalitiesSym) {
        Set<Map<CanonicalDataRange, Node>> partitions = new HashSet<Map<CanonicalDataRange, Node>>();
        Map<CanonicalDataRange, Node> partition = new HashMap<CanonicalDataRange, Node>();
        List<Node> remainingNodes = new ArrayList<Node>(nodeToCanonicalDR.keySet());
        List<Node> nodesForThisPartition = new ArrayList<Node>();
        while (!remainingNodes.isEmpty()) {
            Node node = remainingNodes.get(0);
            partition.put(nodeToCanonicalDR.get(node), node);
            remainingNodes.remove(node);
            // nodes that are inequal to this one should go into the same 
            // partition
            if (inequalities.containsKey(node)) {
                nodesForThisPartition.addAll(inequalities.get(node));
            }
            if (inequalitiesSym.containsKey(node)) {
                nodesForThisPartition.addAll(inequalitiesSym.get(node));
            }
            while (!nodesForThisPartition.isEmpty()) {
                Node currentNode = nodesForThisPartition.get(0);
                if (nodeToCanonicalDR.containsKey(currentNode)) {
                    partition.put(nodeToCanonicalDR.get(currentNode), currentNode);
                    remainingNodes.remove(currentNode);
                    // put all remaining nodes that are inequal to this one into 
                    // the list of nodes to be processed, so that they go into 
                    // the same partition
                    for (Node n : remainingNodes) {
                        if (inequalities.get(currentNode) != null 
                                && inequalities.get(currentNode).contains(n)) {
                            nodesForThisPartition.add(n);
                        }
                        if (inequalitiesSym.get(currentNode) != null 
                                && inequalitiesSym.get(currentNode).contains(n)) {
                            nodesForThisPartition.add(n);
                        }
                    }
                }
                nodesForThisPartition.remove(currentNode);
            }
            partitions.add(partition);
        }
        return partitions;
    }
    
    /**
     * Given a map from data ranges to nodes and inequalities between the nodes, 
     * check whether there is a suitable assignment of values for the nodes that 
     * complies with the given datatype restrictions and inequalities.  
     * @param partition A map from data ranges to nodes such that the 
     *        inequalities given in the next two parameters link the variables 
     *        in the partition. 
     * @param inequalities A map from nodes to sets of nodes for which there are 
     *        inequalities in the extension table. 
     * @param inequalitiesSym The symmetric counter part to the above map. 
     * @param originalNodeToDRs A map from nodes to data ranges such that for 
     *        each node and data range in the map, there is a corresponding 
     *        entry in the extension table.  
     * @return true if an assignment from values to nodes exists such that the 
     *         datatype restrictions and the inequalities are satisfied and 
     *         false otherwise.  
     */
    protected boolean hasAssignment(Map<CanonicalDataRange, Node> partition, 
            Map<Node, Set<Node>> inequalities, 
            Map<Node, Set<Node>> inequalitiesSym, 
            Map<Node, Set<DataRange>> originalNodeToDRs) {
        
        List<CanonicalDataRange> ranges = new ArrayList<CanonicalDataRange>(partition.keySet());
        Collections.sort(ranges, SetLengthComparator.INSTANCE);
        Map<Node, DataConstant> nodeToValue = new HashMap<Node, DataConstant>();
        Node currentNode;
        for (CanonicalDataRange currentRange : ranges) {
            currentNode = partition.get(currentRange);
            DataConstant assignment = currentRange.getSmallestAssignment();
            System.out.println("DataRange " + currentRange + " of node " + currentNode + " got assigned " + assignment);
            if (assignment == null) {
                // clash!
                // collect the inequalities and the ranges that made up the 
                // canonical data ranges in this partition and generate a 
                // clash
                int numberOfCauses = 0; 
                if (inequalities.containsKey(currentNode)) {
                    numberOfCauses += inequalities.get(currentNode).size(); 
                }
                if (inequalitiesSym.containsKey(currentNode)) {
                    numberOfCauses += inequalitiesSym.get(currentNode).size(); 
                }
                // although we are checking the canonical ranges, all the 
                // ranges that made up each canonical range contributes to 
                // the clash and we need their dependency sets
                for (Node node : partition.values()) {
                    numberOfCauses += originalNodeToDRs.get(node).size();
                }
                UnionDependencySet ds = new UnionDependencySet(numberOfCauses);
                Object[][] causes = new Object[numberOfCauses][];
                int i = 0;
                for (Node node : partition.values()) {
                    for (DataRange range : originalNodeToDRs.get(node)) {
                        causes[i] = new Object[] { range, node };
                        ds.m_dependencySets[i] = 
                                extensionManager.getAssertionDependencySet(range, node);
                        i++;
                    }
                }
                if (inequalities.get(currentNode) != null) {
                    for (Node inequalNode : inequalities.get(currentNode)) {
                        causes[i] = new Object[] { 
                                Inequality.INSTANCE, currentNode, inequalNode };
                        ds.m_dependencySets[i] = 
                                extensionManager.getAssertionDependencySet(
                                        Inequality.INSTANCE, 
                                        currentNode, 
                                        inequalNode);
                        i++;
                    }
                }
                if (inequalitiesSym.get(currentNode) != null) {
                    for (Node inequalNode : inequalitiesSym.get(currentNode)) {
                        causes[i] = new Object[] { 
                                Inequality.INSTANCE, inequalNode, currentNode };
                        ds.m_dependencySets[i] = 
                                extensionManager.getAssertionDependencySet(
                                        Inequality.INSTANCE, 
                                        inequalNode, 
                                        currentNode);
                        i++;
                    }
                }
                if (tableauMonitor != null) {
                    tableauMonitor.clashDetected(causes);
                    tableauMonitor.datatypeCheckingFinished(false);
                }
                extensionManager.setClash(ds);
                return false;
            }
            nodeToValue.put(currentNode, assignment);
            if (inequalities.get(currentNode) != null) {
                for (Node inequalNode : inequalities.get(currentNode)) {
                    for (Entry<CanonicalDataRange, Node> entry : partition.entrySet()) {
                        if (entry.getValue() == inequalNode) {
                            entry.getKey().notOneOf(assignment);
                        }
                    }
                }
            }
            if (inequalitiesSym.get(currentNode) != null) {
                for (Node inequalNode : inequalitiesSym.get(currentNode)) {
                    for (Entry<CanonicalDataRange, Node> entry : partition.entrySet()) {
                        if (entry.getValue() == inequalNode) {
                            entry.getKey().notOneOf(assignment);
                        }
                    }
                }
            }
        }
        System.out.println("-----------------------");
        return true;
    }
    
    /**
     * used to sort data ranges based on the number of possible assignments
     */
    protected static class SetLengthComparator implements Comparator<CanonicalDataRange> { 
        public static Comparator<CanonicalDataRange> INSTANCE = new SetLengthComparator();
        public int compare(CanonicalDataRange dr1, CanonicalDataRange dr2) {
            return dr1.getEnumerationSize().subtract(dr2.getEnumerationSize()).intValue(); 
        }
    }
}