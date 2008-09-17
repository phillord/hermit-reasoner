// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.tableau;

import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

public class ObjectHierarchy<T> implements Serializable {
    private static final long serialVersionUID=3262156492606272472L;

    protected final Map<T,Set<T>> m_directSuperobjects;
    protected final Map<T,Set<T>> m_directSubobjects;
    
    public ObjectHierarchy() {
        m_directSuperobjects=new HashMap<T,Set<T>>();
        m_directSubobjects=new HashMap<T,Set<T>>();
    }
    protected Set<T> getSet(T object,Map<T,Set<T>> map) {
        Set<T> result=map.get(object);
        if (result==null) {
            result=new HashSet<T>();
            map.put(object,result);
        }
        return result;
    }
    public void addInclusion(T subobject,T superobject) {
        getSet(subobject,m_directSuperobjects).add(superobject);
        getSet(superobject,m_directSubobjects).add(subobject);
    }
    public Set<T> getAllSuperobjects(T object) {
        return getAllObjectsInClosure(object,m_directSuperobjects);
    }
    public Set<T> getAllSubobjects(T object) {
        return getAllObjectsInClosure(object,m_directSubobjects);
    }
    public Set<T> getAllObjects() {
        Set<T> objects=new HashSet<T>();
        objects.addAll(m_directSubobjects.keySet());
        objects.addAll(m_directSuperobjects.keySet());
        return objects;
    }
    protected Set<T> getAllObjectsInClosure(T object,Map<T,Set<T>> map) {
        Set<T> result=new HashSet<T>();
        List<T> unprocessed=new ArrayList<T>();
        unprocessed.add(object);
        while (!unprocessed.isEmpty()) {
            T unprocessedObject=unprocessed.remove(unprocessed.size()-1);
            if (result.add(unprocessedObject)) {
                Set<T> relatedObjects=map.get(unprocessedObject);
                if (relatedObjects!=null)
                    unprocessed.addAll(relatedObjects);
            }
        }
        return result;
    }
}
