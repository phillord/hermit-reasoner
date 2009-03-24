// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.graph;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

public class Graph<T> {
    protected final Map<T,Set<T>> m_successorsByNodes;

    public Graph() {
        m_successorsByNodes=new HashMap<T,Set<T>>();
    }
    public void addEdge(T from,T to) {
        Set<T> successors=m_successorsByNodes.get(from);
        if (successors==null) {
            successors=new HashSet<T>();
            m_successorsByNodes.put(from,successors);
        }
        successors.add(to);
    }
    public Set<T> getSuccessors(T node) {
        return m_successorsByNodes.get(node);
    }
    public void transitivelyClose() {
        List<T> toProcess=new ArrayList<T>();
        for (Set<T> reachable : m_successorsByNodes.values()) {
            toProcess.clear();
            toProcess.addAll(reachable);
            while (!toProcess.isEmpty()) {
                T elementOnPath=toProcess.remove(toProcess.size()-1);
                Set<T> elementOnPathSuccessors=m_successorsByNodes.get(elementOnPath);
                if (elementOnPathSuccessors!=null)
                    for (T elementOnPathSuccessor : elementOnPathSuccessors)
                        if (reachable.add(elementOnPathSuccessor))
                            toProcess.add(elementOnPathSuccessor);
            }
        }
    }
}
