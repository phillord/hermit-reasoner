package org.semanticweb.HermiT.tableau;

public class DependencySetTest extends AbstractHermiTTest {
    protected DependencySetFactory m_factory;

    public DependencySetTest(String name) {
        super(name);
    }
    protected void setUp() {
        m_factory=new DependencySetFactory(); 
    }
    public void testDependencySet1() {
        DependencySet set=m_factory.emptySet();
        assertContains(set);
        assertTrue(set.isEmpty());
        set=m_factory.addBranchingPoint(set,32);
        assertFalse(set.isEmpty());
        assertContains(set,32);
        set=m_factory.addBranchingPoint(set,0);
        assertContains(set,0,32);
        DependencySet set2=m_factory.addBranchingPoint(m_factory.emptySet(),0);
        assertFalse(set.isSameAs(set2));
        assertTrue(set.isSameAs(m_factory.addBranchingPoint(set2,32)));
        set=m_factory.unionWith(set,m_factory.addBranchingPoint(m_factory.addBranchingPoint(set2,15),17));
        assertContains(set,0,15,17,32);
        assertSame(set,m_factory.unionWith(set,m_factory.emptySet()));
        assertSame(set,m_factory.addBranchingPoint(m_factory.unionWith(set,m_factory.emptySet()),17));
        assertSame(set,m_factory.removeBranchingPoint(set,13));
        set=m_factory.removeBranchingPoint(set,17);
        assertContains(set,0,15,32);
        set=m_factory.removeBranchingPoint(set,15);
        assertContains(set,0,32);
        set=m_factory.removeBranchingPoint(set,32);
        assertContains(set,0);
    }
    public void testDependencySet2() {
        DependencySet set1=m_factory.emptySet();
        set1=m_factory.addBranchingPoint(set1,10);
        set1=m_factory.addBranchingPoint(set1,3);
        set1=m_factory.addBranchingPoint(set1,1);
        set1=m_factory.addBranchingPoint(set1,14);
        assertContains(set1,1,3,10,14);
        
        DependencySet set2=m_factory.emptySet();
        set2=m_factory.addBranchingPoint(set2,15);
        set2=m_factory.addBranchingPoint(set2,10);
        set2=m_factory.addBranchingPoint(set2,1);
        set2=m_factory.addBranchingPoint(set2,17);
        assertContains(set2,1,10,15,17);
        
        DependencySet set3=m_factory.unionWith(set1,set2);
        assertContains(set3,1,3,10,14,15,17);
    }
    protected static void assertContains(DependencySet dependencySet,int... expectedMembers) {
        boolean[] used=new boolean[expectedMembers.length];
        int maxBranchingPoint=dependencySet.getMaximumBranchingPoint();
        for (int branchingPointLevel=0;branchingPointLevel<=maxBranchingPoint;branchingPointLevel++)
            if (dependencySet.containsBranchingPoint(branchingPointLevel)) {
                int index=getIndex(branchingPointLevel,expectedMembers);
                if (index==-1)
                    fail("Branching point "+branchingPointLevel+" is not in the expected set!");
                else
                    used[index]=true;
            }
        for (int index=0;index<used.length;index++)
            if (!used[index])
                fail("Branching point "+expectedMembers[index]+" is not in the set!");
    }
    protected static int getIndex(int branchingPoint,int[] expectedMembers) {
        for (int index=0;index<expectedMembers.length;index++)
            if (expectedMembers[index]==branchingPoint)
                return index;
        return -1;
    }
}
