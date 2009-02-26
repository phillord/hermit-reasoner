package org.semanticweb.HermiT.tableau;

public class TupleTableFullIndexTest extends AbstractReasonerInternalsTest {
    protected TupleTable m_tupleTable;
    protected TupleTableFullIndex m_index;

    public TupleTableFullIndexTest(String name) {
        super(name);
    }
    protected void setUp() {
        m_tupleTable=new TupleTable(2);
        m_index=new TupleTableFullIndex(m_tupleTable,2);
    }
    public void testIndex() {
        assertAdd(0,"a","b");
        assertAdd(1,"b","c");
        assertAdd(2,"c","d");
        assertAdd(0,"a","b");

        assertEquals(0,m_index.getTupleIndex(T("a","b")));
        assertEquals(1,m_index.getTupleIndex(T("b","c")));
        assertEquals(2,m_index.getTupleIndex(T("c","d")));

        m_index.removeTuple(1);
        assertEquals(0,m_index.getTupleIndex(T("a","b")));
        assertEquals(-1,m_index.getTupleIndex(T("b","c")));
        assertEquals(2,m_index.getTupleIndex(T("c","d")));
        
        assertAdd(3,"e","f");
        assertEquals(0,m_index.getTupleIndex(T("a","b")));
        assertEquals(-1,m_index.getTupleIndex(T("b","c")));
        assertEquals(2,m_index.getTupleIndex(T("c","d")));
        assertEquals(3,m_index.getTupleIndex(T("e","f")));
        
        assertAdd(4,"g","h");
        assertEquals(0,m_index.getTupleIndex(T("a","b")));
        assertEquals(-1,m_index.getTupleIndex(T("b","c")));
        assertEquals(2,m_index.getTupleIndex(T("c","d")));
        assertEquals(3,m_index.getTupleIndex(T("e","f")));
        assertEquals(4,m_index.getTupleIndex(T("g","h")));
    }
    public void testLotsOfData() {
        String[][] tuples=new String[40000][];
        for (int index=0;index<tuples.length;index++)
            tuples[index]=new String[] { "a"+index,"b"+index };
        
        for (int tupleIndex=0;tupleIndex<tuples.length;tupleIndex++)
            assertAdd(tupleIndex,tuples[tupleIndex]);
        
        for (int tupleIndex=0;tupleIndex<tuples.length;tupleIndex++)
            assertEquals(tupleIndex,m_index.getTupleIndex(tuples[tupleIndex]));
        
        assertEquals(-1,m_index.getTupleIndex(T("e","f")));
    }
    protected int add(String... tuple) {
        int tentativeTupleIndex=m_tupleTable.getFirstFreeTupleIndex();
        int result=m_index.addTuple(tuple,tentativeTupleIndex);
        if (result==tentativeTupleIndex)
            m_tupleTable.addTuple(tuple);
        return result;
    }
    protected void assertAdd(int tupleIndex,String... tuple) {
        assertEquals(tupleIndex,add(tuple));
    }
}
