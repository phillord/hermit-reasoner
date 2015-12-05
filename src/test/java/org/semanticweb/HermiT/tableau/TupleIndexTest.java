package org.semanticweb.HermiT.tableau;

public class TupleIndexTest extends AbstractReasonerInternalsTest {
    protected TupleIndex m_tupleIndex;

    public TupleIndexTest(String name) {
        super(name);
    }
    protected void setUp() {
        m_tupleIndex=new TupleIndex(new int[] { 0,1,2 });
    }
    public void testIndex1() {
        assertRetrieval(S(),I());

        addTuple(1,S("a","b","c"));
        assertRetrieval(S("a"),I(1));
        assertRetrieval(S("a","b"),I(1));
        
        addTuple(2,S("a","b","d"));
        assertRetrieval(S("a"),I(1,2));
        assertRetrieval(S("a","b"),I(1,2));

        addTuple(3,S("a","b","c"));
        assertRetrieval(S("a"),I(2,1));
        assertRetrieval(S("a","b"),I(2,1));
        assertRetrieval(S("a","b","c"),I(1));

        addTuple(4,S("c","b","d"));
        assertRetrieval(S(),I(2,1,4));
        assertRetrieval(S("a"),I(2,1));
        assertRetrieval(S("a","b"),I(2,1));
        assertRetrieval(S("a","b","c"),I(1));
        assertRetrieval(S("f"),I());

        removeTuple(S("a","b","d"));
        assertRetrieval(S(),I(1,4));

        removeTuple(S("a","b","c"));
        assertRetrieval(S(),I(4));

        removeTuple(S("c","b","d"));
        assertRetrieval(S(),I());
    }
    public void testIndex2() {
        String[][] tuples=new String[10000][3];
        int[] tupleIndexes=new int[tuples.length];
        for (int index=0;index<tuples.length;index++) {
            tuples[index][0]=String.valueOf(index % 300);
            tuples[index][1]=String.valueOf(index % 3000);
            tuples[index][2]=String.valueOf(index);
            tupleIndexes[index]=index;
        }
        for (int index=0;index<tuples.length;index++)
            addTuple(index,tuples[index]);
        assertRetrieval(S(),tupleIndexes);
        for (int index=0;index<tuples.length;index++)
            assertEquals(index,removeTuple(tuples[index]));
        assertRetrieval(S(),I());
    }
    protected void addTuple(int tupleIndex,String[] strings) {
        m_tupleIndex.addTuple(strings,tupleIndex);
    }
    protected int removeTuple(String[] strings) {
        return m_tupleIndex.removeTuple(strings);
    }
    public void assertRetrieval(String[] selection,int[] expected) {
        int[] selectionIndices=new int[selection.length];
        for (int index=0;index<selectionIndices.length;index++)
            selectionIndices[index]=index;
        TupleIndex.TupleIndexRetrieval retrieval=new TupleIndex.TupleIndexRetrieval(m_tupleIndex,selection,selectionIndices);
        retrieval.open();
        boolean[] used=new boolean[expected.length];
        while (!retrieval.afterLast()) {
            int tupleIndex=retrieval.getCurrentTupleIndex();
            boolean found=false;
            for (int index=0;index<expected.length;index++)
                if (tupleIndex==expected[index] && !used[index]) {
                    used[index]=true;
                    found=true;
                    break;
                }
            if (!found)
                fail("The tuple index "+tupleIndex+" has not been found in the expedted set.");
            retrieval.next();
        }
        for (int index=0;index<used.length;index++)
            if (!used[index])
                fail("The tuple index "+expected[index]+" has not been found in the retrieval.");
    }
    
    protected static String[] S(String... strings) {
        return strings;
    }
    protected static int[] I(int... integers) {
        return integers;
    }
}
