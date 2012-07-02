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
package org.semanticweb.HermiT.tableau;

import java.io.Serializable;

public final class TupleIndex implements Serializable {
    private static final long serialVersionUID=-4284072092430590904L;

    protected static final float LOAD_FACTOR=0.7f;
    protected static final int BUCKET_OFFSET=1;

    protected final int[] m_indexingSequence;
    protected final TrieNodeManager m_trieNodeManager;
    protected int m_root;
    protected int[] m_buckets;
    protected int m_bucketsLengthMinusOne; // must be all ones in binary!
    protected int m_resizeThreshold;
    protected int m_numberOfNodes;

    public TupleIndex(int[] indexingSequence) {
        m_indexingSequence=indexingSequence;
        m_trieNodeManager=new TrieNodeManager();
        clear();
    }
    public int sizeInMemoy() {
        return m_buckets.length*4+m_trieNodeManager.size();
    }
    public int[] getIndexingSequence() {
        return m_indexingSequence;
    }
    public void clear() {
        m_trieNodeManager.clear();
        m_root=m_trieNodeManager.newTrieNode();
        m_trieNodeManager.initializeTrieNode(m_root,-1,-1,-1,-1,-1,null);
        m_buckets=new int[16];
        m_bucketsLengthMinusOne=m_buckets.length-1;
        m_resizeThreshold=(int)(m_buckets.length*LOAD_FACTOR);
        m_numberOfNodes=0;
    }
    public int addTuple(Object[] tuple,int potentialTupleIndex) {
        int trieNode=m_root;
        for (int position=0;position<m_indexingSequence.length;position++) {
            Object object=tuple[m_indexingSequence[position]];
            trieNode=getChildNodeAddIfNecessary(trieNode,object);
        }
        if (m_trieNodeManager.getTrieNodeComponent(trieNode,TRIE_NODE_TUPLE_INDEX)==-1) {
            m_trieNodeManager.setTrieNodeComponent(trieNode,TRIE_NODE_TUPLE_INDEX,potentialTupleIndex);
            return potentialTupleIndex;
        }
        else
            return m_trieNodeManager.getTrieNodeComponent(trieNode,TRIE_NODE_TUPLE_INDEX);
    }
    public int getTupleIndex(Object[] tuple) {
        int trieNode=m_root;
        for (int position=0;position<m_indexingSequence.length;position++) {
            Object object=tuple[m_indexingSequence[position]];
            trieNode=getChildNode(trieNode,object);
            if (trieNode==-1)
                return -1;
        }
        return m_trieNodeManager.getTrieNodeComponent(trieNode,TRIE_NODE_TUPLE_INDEX);
    }
    public int removeTuple(Object[] tuple) {
        int leafTrieNode=m_root;
        for (int position=0;position<m_indexingSequence.length;position++) {
            Object object=tuple[m_indexingSequence[position]];
            leafTrieNode=getChildNode(leafTrieNode,object);
            if (leafTrieNode==-1)
                return -1;
        }
        int tupleIndex=m_trieNodeManager.getTrieNodeComponent(leafTrieNode,TRIE_NODE_TUPLE_INDEX);
        int trieNode=m_trieNodeManager.getTrieNodeComponent(leafTrieNode,TRIE_NODE_PARENT);
        removeTrieNode(leafTrieNode);
        while (trieNode!=m_root && m_trieNodeManager.getTrieNodeComponent(trieNode,TRIE_NODE_FIRST_CHILD)==-1) {
            int parentTrieNode=m_trieNodeManager.getTrieNodeComponent(trieNode,TRIE_NODE_PARENT);
            removeTrieNode(trieNode);
            trieNode=parentTrieNode;
        }
        return tupleIndex;
    }
    protected void removeTrieNode(int trieNode) {
        Object object=m_trieNodeManager.getTrieNodeObject(trieNode);
        int parent=m_trieNodeManager.getTrieNodeComponent(trieNode,TRIE_NODE_PARENT);
        int bucketIndex=getIndexFor(object.hashCode()+parent,m_bucketsLengthMinusOne);
        int child=m_buckets[bucketIndex]-BUCKET_OFFSET;
        int previousChild=-1;
        while (child!=-1) {
            int nextChild=m_trieNodeManager.getTrieNodeComponent(child,TRIE_NODE_NEXT_ENTRY);
            if (child==trieNode) {
                m_numberOfNodes--;
                int previousSibling=m_trieNodeManager.getTrieNodeComponent(trieNode,TRIE_NODE_PREVIOUS_SIBLING);
                int nextSibling=m_trieNodeManager.getTrieNodeComponent(trieNode,TRIE_NODE_NEXT_SIBLING);
                if (previousSibling==-1)
                    m_trieNodeManager.setTrieNodeComponent(parent,TRIE_NODE_FIRST_CHILD,nextSibling);
                else
                    m_trieNodeManager.setTrieNodeComponent(previousSibling,TRIE_NODE_NEXT_SIBLING,nextSibling);
                if (nextSibling!=-1)
                    m_trieNodeManager.setTrieNodeComponent(nextSibling,TRIE_NODE_PREVIOUS_SIBLING,previousSibling);
                if (previousChild==-1)
                    m_buckets[bucketIndex]=nextChild+BUCKET_OFFSET;
                else
                    m_trieNodeManager.setTrieNodeComponent(previousChild,TRIE_NODE_NEXT_ENTRY,nextChild);
                m_trieNodeManager.deleteTrieNode(trieNode);
                return;
            }
            previousChild=child;
            child=nextChild;
        }
        throw new IllegalStateException("Internal error: should be able to remove the child node.");
    }
    protected int getChildNode(int parent,Object object) {
        int bucketIndex=getIndexFor(object.hashCode()+parent,m_bucketsLengthMinusOne);
        int child=m_buckets[bucketIndex]-BUCKET_OFFSET;
        while (child!=-1) {
            if (parent==m_trieNodeManager.getTrieNodeComponent(child,TRIE_NODE_PARENT) && object.equals(m_trieNodeManager.getTrieNodeObject(child)))
                return child;
            child=m_trieNodeManager.getTrieNodeComponent(child,TRIE_NODE_NEXT_ENTRY);
        }
        return -1;
    }
    protected int getChildNodeAddIfNecessary(int parent,Object object) {
        int hashCode=object.hashCode()+parent;
        int bucketIndex=getIndexFor(hashCode,m_bucketsLengthMinusOne);
        int child=m_buckets[bucketIndex]-BUCKET_OFFSET;
        while (child!=-1) {
            if (parent==m_trieNodeManager.getTrieNodeComponent(child,TRIE_NODE_PARENT) && object.equals(m_trieNodeManager.getTrieNodeObject(child)))
                return child;
            child=m_trieNodeManager.getTrieNodeComponent(child,TRIE_NODE_NEXT_ENTRY);
        }
        if (m_numberOfNodes>=m_resizeThreshold) {
            resizeBuckets();
            bucketIndex=getIndexFor(hashCode,m_bucketsLengthMinusOne);
        }
        child=m_trieNodeManager.newTrieNode();
        int nextSibling=m_trieNodeManager.getTrieNodeComponent(parent,TRIE_NODE_FIRST_CHILD);
        if (nextSibling!=-1)
            m_trieNodeManager.setTrieNodeComponent(nextSibling,TRIE_NODE_PREVIOUS_SIBLING,child);
        m_trieNodeManager.setTrieNodeComponent(parent,TRIE_NODE_FIRST_CHILD,child);
        m_trieNodeManager.initializeTrieNode(child,parent,-1,-1,nextSibling,m_buckets[bucketIndex]-BUCKET_OFFSET,object);
        m_buckets[bucketIndex]=child+BUCKET_OFFSET;
        m_numberOfNodes++;
        return child;
    }
    protected void resizeBuckets() {
    	if (m_buckets.length==0x40000000)
    	    m_resizeThreshold=Integer.MAX_VALUE;
    	else {
    		int[] newBuckets=new int[m_buckets.length*2];
            int newBucketsLengthMinusOne=newBuckets.length-1;
            for (int bucketIndex=m_bucketsLengthMinusOne;bucketIndex>=0;--bucketIndex) {
                int trieNode=m_buckets[bucketIndex]-BUCKET_OFFSET;
                while (trieNode!=-1) {
                    int nextTrieNode=m_trieNodeManager.getTrieNodeComponent(trieNode,TRIE_NODE_NEXT_ENTRY);
                    int hashCode=m_trieNodeManager.getTrieNodeObject(trieNode).hashCode()+m_trieNodeManager.getTrieNodeComponent(trieNode,TRIE_NODE_PARENT);
                    int newBucketIndex=getIndexFor(hashCode,newBucketsLengthMinusOne);
                    m_trieNodeManager.setTrieNodeComponent(trieNode,TRIE_NODE_NEXT_ENTRY,newBuckets[newBucketIndex]-BUCKET_OFFSET);
                    newBuckets[newBucketIndex]=trieNode+BUCKET_OFFSET;
                    trieNode=nextTrieNode;
                }
            }
            m_buckets=newBuckets;
            m_bucketsLengthMinusOne=newBucketsLengthMinusOne;
            m_resizeThreshold=(int)(m_buckets.length*LOAD_FACTOR);
    	}
    }
    protected static int getIndexFor(int hashCode,int tableLengthMinusOne) {
        hashCode+=~(hashCode << 9);
        hashCode^=(hashCode >>> 14);
        hashCode+=(hashCode << 4);
        hashCode^=(hashCode >>> 10);
        return hashCode & tableLengthMinusOne;
    }

    protected static final int TRIE_NODE_PARENT=0;
    protected static final int TRIE_NODE_FIRST_CHILD=1;
    protected static final int TRIE_NODE_TUPLE_INDEX=1;
    protected static final int TRIE_NODE_PREVIOUS_SIBLING=2;
    protected static final int TRIE_NODE_NEXT_SIBLING=3;
    protected static final int TRIE_NODE_NEXT_ENTRY=4;
    protected static final int TRIE_NODE_SIZE=5;
    protected static final int TRIE_NODE_PAGE_SIZE=1024;

    protected static final class TrieNodeManager implements Serializable {
        private static final long serialVersionUID=-1978070096232682717L;

        protected int[][] m_indexPages;
        protected Object[][] m_objectPages;
        protected int m_firstFreeTrieNode;
        protected int m_numberOfPages;

        public TrieNodeManager() {
           clear();
        }
        public int size() {
            int size=m_indexPages.length*4+m_objectPages.length*4;
            for (int i=m_indexPages.length-1;i>=0;--i)
                if (m_indexPages[i]!=null)
                    size+=m_indexPages[i].length*4;
            for (int i=m_objectPages.length-1;i>=0;--i)
                if (m_objectPages[i]!=null)
                    size+=m_objectPages[i].length*4;
            return size;
        }
        public void clear() {
            m_indexPages=new int[10][];
            m_indexPages[0]=new int[TRIE_NODE_SIZE*TRIE_NODE_PAGE_SIZE];
            m_objectPages=new Object[10][];
            m_objectPages[0]=new Object[TRIE_NODE_PAGE_SIZE];
            m_numberOfPages=1;
            m_firstFreeTrieNode=0;
            setTrieNodeComponent(m_firstFreeTrieNode,TRIE_NODE_NEXT_SIBLING,-1);
        }
        public int getTrieNodeComponent(int trieNode,int component) {
            return m_indexPages[trieNode / TRIE_NODE_PAGE_SIZE][(trieNode % TRIE_NODE_PAGE_SIZE)*TRIE_NODE_SIZE+component];
        }
        public void setTrieNodeComponent(int trieNode,int component,int value) {
            m_indexPages[trieNode / TRIE_NODE_PAGE_SIZE][(trieNode % TRIE_NODE_PAGE_SIZE)*TRIE_NODE_SIZE+component]=value;
        }
        public Object getTrieNodeObject(int trieNode) {
            return m_objectPages[trieNode / TRIE_NODE_PAGE_SIZE][trieNode % TRIE_NODE_PAGE_SIZE];
        }
        public void setTrieNodeObject(int trieNode,Object object) {
            m_objectPages[trieNode / TRIE_NODE_PAGE_SIZE][trieNode % TRIE_NODE_PAGE_SIZE]=object;
        }
        public void initializeTrieNode(int trieNode,int parent,int firstChild,int previousSibling,int nextSibling,int nextEntry,Object object) {
            int pageIndex=trieNode / TRIE_NODE_PAGE_SIZE;
            int indexInPage=trieNode % TRIE_NODE_PAGE_SIZE;
            int[] indexPage=m_indexPages[pageIndex];
            int start=indexInPage*TRIE_NODE_SIZE;
            indexPage[start+TRIE_NODE_PARENT]=parent;
            indexPage[start+TRIE_NODE_FIRST_CHILD]=firstChild;
            indexPage[start+TRIE_NODE_PREVIOUS_SIBLING]=previousSibling;
            indexPage[start+TRIE_NODE_NEXT_SIBLING]=nextSibling;
            indexPage[start+TRIE_NODE_NEXT_ENTRY]=nextEntry;
            m_objectPages[pageIndex][indexInPage]=object;
        }
        public int newTrieNode() {
            int newTrieNode=m_firstFreeTrieNode;
            int nextFreeTrieNode=getTrieNodeComponent(m_firstFreeTrieNode,TRIE_NODE_NEXT_SIBLING);
            if (nextFreeTrieNode!=-1)
                m_firstFreeTrieNode=nextFreeTrieNode;
            else {
                m_firstFreeTrieNode++;
                if (m_firstFreeTrieNode<0)
                    throw new OutOfMemoryError("The space of nodes in TupleIndex was exhausted: the ontology is just too large.");
                int pageIndex=m_firstFreeTrieNode / TRIE_NODE_PAGE_SIZE;
                if (pageIndex>=m_numberOfPages) {
                    if (pageIndex>=m_indexPages.length) {
                        int[][] newIndexPages=new int[m_indexPages.length*3/2][];
                        System.arraycopy(m_indexPages,0,newIndexPages,0,m_indexPages.length);
                        m_indexPages=newIndexPages;
                        Object[][] newObjectPages=new Object[m_objectPages.length*3/2][];
                        System.arraycopy(m_objectPages,0,newObjectPages,0,m_objectPages.length);
                        m_objectPages=newObjectPages;
                    }
                    m_indexPages[pageIndex]=new int[TRIE_NODE_SIZE*TRIE_NODE_PAGE_SIZE];
                    m_objectPages[pageIndex]=new Object[TRIE_NODE_PAGE_SIZE];
                    m_numberOfPages++;
                }
                setTrieNodeComponent(m_firstFreeTrieNode,TRIE_NODE_NEXT_SIBLING,-1);
            }
            return newTrieNode;
        }
        public void deleteTrieNode(int trieNode) {
            setTrieNodeComponent(trieNode,TRIE_NODE_NEXT_SIBLING,m_firstFreeTrieNode);
            setTrieNodeObject(trieNode,null);
            m_firstFreeTrieNode=trieNode;
        }
    }

    public static class TupleIndexRetrieval implements Serializable {
        private static final long serialVersionUID=3052986474027614595L;

        protected final TupleIndex m_tupleIndex;
        protected final Object[] m_bindingsBuffer;
        protected final int[] m_selectionIndices;
        protected final int m_selectionIndicesLength;
        protected final int m_indexingSequenceLength;
        protected int m_currentTrieNode;

        public TupleIndexRetrieval(TupleIndex tupleIndex,Object[] bindingsBuffer,int[] selectionIndices) {
            m_tupleIndex=tupleIndex;
            m_bindingsBuffer=bindingsBuffer;
            m_selectionIndices=selectionIndices;
            m_selectionIndicesLength=m_selectionIndices.length;
            m_indexingSequenceLength=tupleIndex.m_indexingSequence.length;
        }
        public void open() {
            m_currentTrieNode=m_tupleIndex.m_root;
            for (int position=0;position<m_selectionIndicesLength;position++) {
                Object object=m_bindingsBuffer[m_selectionIndices[position]];
                m_currentTrieNode=m_tupleIndex.getChildNode(m_currentTrieNode,object);
                if (m_currentTrieNode==-1)
                    return;
            }
            if (m_selectionIndicesLength==0 && m_tupleIndex.m_trieNodeManager.getTrieNodeComponent(m_tupleIndex.m_root,TRIE_NODE_FIRST_CHILD)==-1)
                m_currentTrieNode=-1;
            else {
                for (int index=m_selectionIndicesLength;index<m_indexingSequenceLength;index++)
                    m_currentTrieNode=m_tupleIndex.m_trieNodeManager.getTrieNodeComponent(m_currentTrieNode,TRIE_NODE_FIRST_CHILD);
            }
        }
        public boolean afterLast() {
            return m_currentTrieNode==-1;
        }
        public int getCurrentTupleIndex() {
            return m_tupleIndex.m_trieNodeManager.getTrieNodeComponent(m_currentTrieNode,TRIE_NODE_TUPLE_INDEX);
        }
        public void next() {
            int trieNodeDepth=m_indexingSequenceLength;
            while (trieNodeDepth!=m_selectionIndicesLength && m_tupleIndex.m_trieNodeManager.getTrieNodeComponent(m_currentTrieNode,TRIE_NODE_NEXT_SIBLING)==-1) {
                m_currentTrieNode=m_tupleIndex.m_trieNodeManager.getTrieNodeComponent(m_currentTrieNode,TRIE_NODE_PARENT);
                trieNodeDepth--;
            }
            if (trieNodeDepth==m_selectionIndicesLength)
                m_currentTrieNode=-1;
            else {
                m_currentTrieNode=m_tupleIndex.m_trieNodeManager.getTrieNodeComponent(m_currentTrieNode,TRIE_NODE_NEXT_SIBLING);
                for (int index=trieNodeDepth;index<m_indexingSequenceLength;index++)
                    m_currentTrieNode=m_tupleIndex.m_trieNodeManager.getTrieNodeComponent(m_currentTrieNode,TRIE_NODE_FIRST_CHILD);
            }
        }
    }
}
