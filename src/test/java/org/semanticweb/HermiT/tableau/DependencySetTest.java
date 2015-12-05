package org.semanticweb.HermiT.tableau;

import junit.framework.TestCase;

public class DependencySetTest extends TestCase {
    protected DependencySetFactory m_factory;

    public DependencySetTest(String name) {
        super(name);
    }

    protected void setUp() {
        m_factory=new DependencySetFactory();
    }

    public void testDependencySet1() {
        PermanentDependencySet set=m_factory.emptySet();
        assertDSEquals(set);
        assertTrue(set.isEmpty());
        set=m_factory.addBranchingPoint(set,32);
        assertFalse(set.isEmpty());
        assertDSEquals(set,32);
        set=m_factory.addBranchingPoint(set,0);
        assertDSEquals(set,0,32);
        PermanentDependencySet set2=m_factory.addBranchingPoint(m_factory.emptySet(),0);
        assertTrue(set!=set2);
        assertSame(set,m_factory.addBranchingPoint(set2,32));
        set=m_factory.unionWith(set,m_factory.addBranchingPoint(m_factory.addBranchingPoint(set2,15),17));
        assertDSEquals(set,0,15,17,32);
        assertSame(set,m_factory.unionWith(set,m_factory.emptySet()));
        assertSame(set,m_factory.addBranchingPoint(m_factory.unionWith(set,m_factory.emptySet()),17));
        assertSame(set,m_factory.removeBranchingPoint(set,13));
        set=m_factory.removeBranchingPoint(set,17);
        assertDSEquals(set,0,15,32);
        set=m_factory.removeBranchingPoint(set,15);
        assertDSEquals(set,0,32);
        set=m_factory.removeBranchingPoint(set,32);
        assertDSEquals(set,0);
    }

    public void testDependencySet2() {
        PermanentDependencySet set1=m_factory.emptySet();
        set1=m_factory.addBranchingPoint(set1,10);
        set1=m_factory.addBranchingPoint(set1,3);
        set1=m_factory.addBranchingPoint(set1,1);
        set1=m_factory.addBranchingPoint(set1,14);
        assertDSEquals(set1,1,3,10,14);

        PermanentDependencySet set2=m_factory.emptySet();
        set2=m_factory.addBranchingPoint(set2,15);
        set2=m_factory.addBranchingPoint(set2,10);
        set2=m_factory.addBranchingPoint(set2,1);
        set2=m_factory.addBranchingPoint(set2,17);
        assertDSEquals(set2,1,10,15,17);

        PermanentDependencySet set3=m_factory.unionWith(set1,set2);
        assertDSEquals(set3,1,3,10,14,15,17);
    }

    public void testDependencySet3() {
        PermanentDependencySet set1=m_factory.emptySet();
        set1=m_factory.addBranchingPoint(set1,10);
        set1=m_factory.addBranchingPoint(set1,3);
        set1=m_factory.addBranchingPoint(set1,1);
        assertDSEquals(set1,1,3,10);

        PermanentDependencySet set2=m_factory.emptySet();
        set2=m_factory.addBranchingPoint(set2,14);
        set2=m_factory.addBranchingPoint(set2,3);
        set2=m_factory.addBranchingPoint(set2,1);
        set2=m_factory.addBranchingPoint(set2,17);
        assertDSEquals(set2,1,3,14,17);

        PermanentDependencySet set3=m_factory.emptySet();
        set3=m_factory.addBranchingPoint(set3,14);
        set3=m_factory.addBranchingPoint(set3,3);
        set3=m_factory.addBranchingPoint(set3,2);
        set3=m_factory.addBranchingPoint(set3,18);
        assertDSEquals(set3,2,3,14,18);

        UnionDependencySet union=new UnionDependencySet(3);
        union.m_dependencySets[0]=set1;
        union.m_dependencySets[1]=set2;
        union.m_dependencySets[2]=set3;
        PermanentDependencySet set4=m_factory.getPermanent(union);
        assertDSEquals(set4,1,2,3,10,14,17,18);
    }

    protected static void assertDSEquals(PermanentDependencySet dependencySet,int... expectedSortedMembers) {
        PermanentDependencySet checkSet=dependencySet;
        for (int index=expectedSortedMembers.length-1;index>=0;--index) {
            int expectedBranchingPoint=expectedSortedMembers[index];
            if (expectedBranchingPoint!=checkSet.m_branchingPoint) {
                StringBuffer buffer=new StringBuffer();
                buffer.append("Dependency sets are different: expected { ");
                for (int i=expectedSortedMembers.length-1;i>=0;--i) {
                    buffer.append(expectedSortedMembers[i]);
                    if (i>0)
                        buffer.append(',');
                }
                buffer.append(" } but got { ");
                while (dependencySet.m_branchingPoint!=-1) {
                    buffer.append(dependencySet.m_branchingPoint);
                    if (dependencySet.m_rest.m_branchingPoint!=-1)
                        buffer.append(',');
                    dependencySet=dependencySet.m_rest;
                }
                buffer.append(" }.");
                fail(buffer.toString());
            }
            checkSet=checkSet.m_rest;
        }
    }

    protected static int getIndex(int branchingPoint,int[] expectedMembers) {
        for (int index=0;index<expectedMembers.length;index++)
            if (expectedMembers[index]==branchingPoint)
                return index;
        return -1;
    }
}
