package org.semanticweb.HermiT.tableau;

public class ReasonerTest extends AbstractOntologyTest {

    public ReasonerTest(String name) {
        super(name);
    }
    public void testSatisfiability1() throws Exception {
        addAxiom("[classMember C a]");
        addAxiom("[classMember [not C] a]");
        assertABoxSatisfiable(false);
    }
    public void testSatisfiability2() throws Exception {
        addAxiom("[subClassOf owl:Thing C]");
        addAxiom("[subClassOf owl:Thing [not C]]");
        assertABoxSatisfiable(false);
    }
    public void testSatisfiability3() throws Exception {
        addAxiom("[subClassOf Person [some hasParent Person]]");
        addAxiom("[subClassOf [some hasParent [some hasParent Person]] Grandchild]");
        addAxiom("[classMember Person peter]");
        addAxiom("[classMember [not Grandchild] peter]");
        assertABoxSatisfiable(false);
    }
    public void testSatisfiability4() throws Exception {
        addAxiom("[objectFunctional R]");
        addAxiom("[objectMember R a b]");
        addAxiom("[subClassOf owl:Thing [some R C]]");
        addAxiom("[classMember [not C] b]");
        assertABoxSatisfiable(false);
    }
    public void testChanges() throws Exception {
        addAxiom("[subClassOf owl:Thing C]");
        assertABoxSatisfiable(true);
        addAxiom("[subClassOf owl:Thing [not C]]");
        assertABoxSatisfiable(false);
        removeAxiom("[subClassOf owl:Thing C]");
        assertABoxSatisfiable(true);
    }
    public void testSubsumption1() throws Exception {
        addAxiom("[subClassOf Person Animal]");
        addAxiom("[subClassOf Student Person]");
        addAxiom("[subClassOf Dog Animal]");
        assertSubsumedBy("Student","Animal",true);
        assertSubsumedBy("Animal","Student",false);
        assertSubsumedBy("Student","Dog",false);
        assertSubsumedBy("Dog","Student",false);
    }
    public void testSubsumption2() throws Exception {
        addAxiom("[subObjectPropertyOf R S]");
        addAxiom("[equivalent A [some R C]]");
        addAxiom("[equivalent B [some S C]]");
        assertSubsumedBy("A","B",true);
        assertSubsumedBy("B","A",false);
    }
    public void testSubsumption3() throws Exception {
        addAxiom("[objectEquivalent R S]");
        addAxiom("[equivalent A [some R C]]");
        addAxiom("[equivalent B [some S C]]");
        assertSubsumedBy("A","B",true);
        assertSubsumedBy("B","A",true);
    }
    public void testHeinsohnTBox1() throws Exception {
        // Tests incoherency caused by disjoint concepts
        addAxiom("[disjoint c d]");
        addAxiom("[subClassOf e3 c]");
        addAxiom("[subClassOf f d]");
        addAxiom("[subClassOf c1 d1]");
        addAxiom("[disjoint c1 d1]");
        assertSatisfiable("[and c d]",false);
        assertSatisfiable("[and [all r [and c d]] [some r owl:Thing]]",false);
        assertSatisfiable("[and e3 f]",false);
        assertSatisfiable("c1",false);
    }
    public void testHeinsohnTBox2() throws Exception {
        // Tests incoherency caused by number restrictions
        addAxiom("[disjoint c d]");
        assertSatisfiable("[and [atLeast 2 r] [atMost 1 r]]",false);
        assertSatisfiable("[and [atMost 1 r] [some r c] [some r d]]",false);
    }
    public void testHeinsohnTBox3c() throws Exception {
        // Tests incoherency caused by the role hierarchy and number restrictions
        addAxiom("[disjoint c d]");
        addAxiom("[subClassOf a [or c d]]");
        addAxiom("[subObjectPropertyOf t1 tc]");
        addAxiom("[subObjectPropertyOf t1 td]");
        addAxiom("[subClassOf owl:Thing [all tc c]]");
        addAxiom("[subClassOf owl:Thing [all td d]]");
        addAxiom("[subObjectPropertyOf tc r]");
        addAxiom("[subObjectPropertyOf td s]");
        assertSatisfiable("[and [all t1 a] [atLeast 3 t1] [atMost 1 r] [atMost 1 s]]",false);
    }
    public void testHeinsohnTBox3cIrh() throws Exception {
        // Tests incoherency caused by number restrictions
        addAxiom("[disjoint c d]");
        addAxiom("[equivalent a [or c d]]");
        assertSatisfiable("[and [all tt a] [atLeast 3 tt] [atMost 1 tt c] [atMost 1 tt d]]",false);
    }
    public void testHeinsohnTBox3() throws Exception {
        // Tests incoherency caused by number restrictions and role hierarchy
        addAxiom("[disjoint c d e]");
        addAxiom("[subClassOf a [or c d]]");
        addAxiom("[subObjectPropertyOf r1 r]");
        addAxiom("[subObjectPropertyOf r2 r]");
        addAxiom("[subObjectPropertyOf r3 r]");
        addAxiom("[subObjectPropertyOf t1 tt]");
        addAxiom("[subObjectPropertyOf t2 tt]");
        addAxiom("[subObjectPropertyOf t3 tt]");
        assertSubsumedBy("[and [atLeast 1 r] [some r c] [some r d]]","[atLeast 2 r]",true);
        assertSubsumedBy("[and [atMost 2 r] [some r c] [some r d]]","[and [atMost 1 r c] [atMost 1 r d]]",true);
        assertSubsumedBy("[and [all r a] [atLeast 3 r] [atMost 1 r c]]","[atLeast 2 r d]",true);
        assertSubsumedBy("[and [all r a] [atLeast 3 r] [atMost 1 r c]]","[atLeast 2 r d]",true);
        assertSubsumedBy("[and [some r1 [and [atMost 1 tt] [some t1 c]]] [some r2 [and [atMost 1 tt] [some t2 d]]] [some r3 [and [atMost 1 tt] [some t3 e]]]]","[atLeast 2 r]",true);
    }
    public void testHeinsohnTBox3Modified() throws Exception {
        addAxiom("[disjoint c d]");
        addAxiom("[subClassOf a [atMost 2 r]]");
        addAxiom("[subClassOf a [some r c]]");
        addAxiom("[subClassOf a [some r d]]");
        addAxiom("[subClassOf owl:Thing [or [atLeast 2 r c] [atLeast 2 r d] b]]");
        assertSubsumedBy("a","b",true);
    }
    public void testHeinsohnTBox4() throws Exception {
        // Tests role restrictions
        addAxiom("[disjoint c d]");
        assertSubsumedBy("[and [all r d] [all r [or [not d] e]]]","[all r e]",true);
        assertSubsumedBy("[and [all r [or [not [atLeast 2 s]] c]] [all r d]]","[all r [atMost 1 s]]",true);
    }
    public void testHeinsohnTBox7() throws Exception {
        // Tests inverse roles
        addAxiom("[objectInverse r r-]");
        assertSubsumedBy("[and [all r [all r- a]] [some r owl:Thing]]","a",true);
    }
    public void testIanT1() throws Exception {
        addAxiom("[objectInverse r r-]");
        addAxiom("[subClassOf p1 [not [or p2 p3 p4 p5]]]");
        addAxiom("[subClassOf p2 [not [or p3 p4 p5]]]");
        addAxiom("[subClassOf p3 [not [or p4 p5]]]");
        addAxiom("[subClassOf p4 [not p5]]");
        assertSatisfiable("[and [some r p1] [some r p2] [some r p3] [atMost 2 r]]",false);
        assertSatisfiable("[some r- [and [some r p1] [atMost 1 r p1]]]",true);
        assertSatisfiable("[and p2 [some r- [and [some r p1] [atMost 1 r]]]]",false);
    }
    public void testIanT2() throws Exception {
        addAxiom("[subObjectPropertyOf r f1]");
        addAxiom("[subObjectPropertyOf r f2]");
        addAxiom("[subClassOf p1 [not p2]]");
        addAxiom("[objectFunctional f1]");
        addAxiom("[objectFunctional f2]");
        assertSatisfiable("[and [some f1 p1] [some f2 p2]]",true);
        assertSatisfiable("[and [some f1 p1] [some f2 p2] [some r owl:Thing]]",false);
    }
    public void testIanT3() throws Exception {
        addAxiom("[subClassOf p1 [not [or p2 p3 p4 p5]]]");
        addAxiom("[subClassOf p2 [not [or p3 p4 p5]]]");
        addAxiom("[subClassOf p3 [not [or p4 p5]]]");
        addAxiom("[subClassOf p4 [not p5]]");
        assertSatisfiable("[and [some r p1] [some r p2] [some r p3] [some r [and p1 p]] [some r [and p2 p]] [some r [and p3 p]] [atMost 3 r]]",true);
        assertSatisfiable("[and [some r p1] [some r p2] [some r p3] [some r p4] [some r [and p1 p]] [some r [and p2 p]] [some r [and p3 p]] [atMost 3 r]]",false);
        assertSatisfiable("[and [some r p1] [some r p2] [some r p3] [some r p4] [atMost 3 r]]",false);
        assertSatisfiable("[and [some r p1] [some r p2] [some r p3] [some r p4] [some r [and p1 p]] [some r [and p2 p]] [some r [and p3 p]] [some r [and p4 p]] [atMost 4 r]]",true);
        assertSatisfiable("[and [some r p1] [some r p2] [some r p3] [some r p4] [some r p5] [some r [and p1 p]] [some r [and p2 p]] [some r [and p3 p]] [some r [and p4 p]] [atMost 4 r]]",false);
        assertSatisfiable("[and [some r p1] [some r p2] [some r p3] [some r p4] [some r p5] [some r [and p1 p]] [some r [and p2 p]] [some r [and p3 p]] [some r [and p4 p]] [some r [and p5 p]] [atMost 5 r]]",true);
    }
    public void testIanT4() throws Exception {
        addAxiom("[objectTransitive p]");
        addAxiom("[objectInverse r r-]");
        addAxiom("[objectInverse p p-]");
        addAxiom("[objectInverse s s-]");
        addAxiom("[equivalent c [all r- [all p- [all s- [not a]]]]]");
        assertSatisfiable("[and a [some s [and [some r owl:Thing] [some p owl:Thing] [all r c] [all p [some r owl:Thing]] [all p [some p owl:Thing]] [all p [all r c]]]]]",false);
    }
    public void testIanT5() throws Exception {
        addAxiom("[objectInverse r r-]");
        addAxiom("[objectInverse f f-]");
        addAxiom("[objectTransitive r]");
        addAxiom("[subObjectPropertyOf f r]");
        addAxiom("[objectFunctional f]");
        assertSatisfiable("[and [not a] [some f- a] [all r- [some f- a]]]",true);
    }
    public void testIanT6() throws Exception {
        addAxiom("[objectInverse r r-]");
        addAxiom("[objectInverse f f-]");
        addAxiom("[objectTransitive r]");
        addAxiom("[subObjectPropertyOf f r]");
        addAxiom("[objectFunctional f]");
        addAxiom("[equivalent d [and c [some f [not c]]]]");
        assertSatisfiable("[and [not c] [some f- d] [all r- [some f- d]]]",false);
    }
    public void testIanT7() throws Exception {
        addAxiom("[objectInverse r r-]");
        addAxiom("[objectInverse f f-]");
        addAxiom("[objectTransitive r]");
        addAxiom("[objectFunctional f]");
        assertSatisfiable("[and p1 [some r [some r [and p1 [all r- [not p1]]]]]]",false);
        assertSatisfiable("[and p1 [some r [some r [and p1 [all r- [or [not p1] [all r p1]]]]]]]",true);
        assertSatisfiable("[some f [and p1 [all f- [some f [not p1]]]]]",false);
    }
    public void testIanT8() throws Exception {
        addAxiom("[objectInverse r r-]");
        assertSatisfiable("[and [some r [all r- [all r1 p]]] [some r [all r- [all r1 [not p]]]]]",true);
        assertSatisfiable("[and [some r1 owl:Thing] [some r [all r- [all r1 p]]] [some r [all r- [all r1 [not p]]]]]",false);
    }
    public void testIanT9() throws Exception {
        addAxiom("[objectInverse successor successor-]");
        addAxiom("[objectTransitive descendant]");
        addAxiom("[subObjectPropertyOf successor descendant]");
        addAxiom("[objectInverseFunctional successor]");
        addAxiom("[subClassOf root [not [some successor- owl:Thing]]]");
        addAxiom("[subClassOf Infinite-Tree-Node [and node [some successor Infinite-Tree-Node]]]");
        addAxiom("[subClassOf Infinite-Tree-Root [and Infinite-Tree-Node root]]");
        assertSatisfiable("Infinite-Tree-Root",true);
        assertSatisfiable("[and Infinite-Tree-Root [all descendant [some successor- root]]]",false);
    }
    public void testIanT10() throws Exception {
        addAxiom("[objectInverse s s-]");
        addAxiom("[objectInverse f f-]");
        addAxiom("[objectInverse f1 f1-]");
        addAxiom("[objectFunctional f]");
        addAxiom("[objectFunctional f1]");
        addAxiom("[subObjectPropertyOf s f]");
        addAxiom("[subObjectPropertyOf s f1]");
        assertSatisfiable("[and [not p] [some f [and [all s- p] [all f- [some s p]]]]]",false);
        assertSatisfiable("[and [all s [not p]] [some s [and p [some s- p]]]]",false);
        assertSatisfiable("[and [some f p] [some f1 [not p]]]",true);
        assertSatisfiable("[and [some f p] [some s owl:Thing] [some f1 [not p]]]",false);
        assertSatisfiable("[and [some f p] [some f1 [and [not p] [all f1- [some s owl:Thing]]]]]",false);
    }
    public void testIanT11() throws Exception {
        addAxiom("[objectInverse s s-]");
        addAxiom("[subObjectPropertyOf s r]");
        assertSatisfiable("[and [not p] [atMost 1 r] [some r [all s- p]] [some s p]]",false);
    }
    public void testIanT12() throws Exception {
        addAxiom("[objectInverse r r-]");
        assertSatisfiable("[and [some s [and [not p] [not q]]] [some r [and [atMost 1 r-] [some r- [all s p]]]]]",false);
    }
    public void testIanT13() throws Exception {
        addAxiom("[objectInverse s s-]");
        addAxiom("[equivalent a1 [some s [all s- [all r c]]]]");
        addAxiom("[equivalent a2 [some s [all s- [all r [not c]]]]]");
        addAxiom("[equivalent a3a [some s [all s- [or [some r d] [some s d]]]]]");
        addAxiom("[equivalent a3b [or [some r d] [some s d]]]");
        addAxiom("[equivalent a3c [or [some r d] d]]");
        addAxiom("[equivalent a3e [some r d]]");
        assertSatisfiable("[and a3a a2 a1]",true);
        assertSatisfiable("[and a3b a2 a1]",true);
        assertSatisfiable("[and a3c a2 a1]",true);
        assertSatisfiable("[and a3e a2 a1]",false);
        assertSatisfiable("[and a a2 a1]",true);
        assertSatisfiable("[and [and a3a a2 a1] [not [and a3b a2 a1]]]",false);
        assertSatisfiable("[and [not [and a3a a2 a1]] [and a3b a2 a1]]",false);
        assertSatisfiable("[and [and a3c a2 a1] [not [and a3c a2 a1]]]",false);
    }
    public void testIanFact1() throws Exception {
        addAxiom("[disjoint a b c]");
        assertSatisfiable("[or [and a b] [and a c] [and b c]]",false);
    }
    public void testIanFact2() throws Exception {
        addAxiom("[subClassOf c [all r c]]");
        addAxiom("[subClassOf [all r c] d]");
        assertSubsumedBy("c","d",true);
    }
    public void testIanFact3() throws Exception {
        addAxiom("[objectFunctional f1]");
        addAxiom("[objectFunctional f2]");
        addAxiom("[objectFunctional f3]");
        addAxiom("[subObjectPropertyOf f3 f1]");
        addAxiom("[subObjectPropertyOf f3 f2]");
        assertSatisfiable("[and [some f1 p1] [some f2 [not p1]] [some f3 p2]]",false);
    }
    public void testIanFact4() throws Exception {
        addAxiom("[objectFunctional rx]");
        addAxiom("[objectFunctional rx3]");
        addAxiom("[subObjectPropertyOf rx3 rx]");
        addAxiom("[subObjectPropertyOf rx3 rx1]");
        addAxiom("[objectFunctional rx4]");
        addAxiom("[subObjectPropertyOf rx4 rx]");
        addAxiom("[subObjectPropertyOf rx4 rx2]");
        addAxiom("[objectFunctional rx3a]");
        addAxiom("[subObjectPropertyOf rx3a rxa]");
        addAxiom("[subObjectPropertyOf rx3a rx1a]");
        addAxiom("[objectFunctional rx4a]");
        addAxiom("[subObjectPropertyOf rx4a rxa]");
        addAxiom("[subObjectPropertyOf rx4a rx2a]");
        assertSubsumedBy("[and [some rx3 c1] [some rx4 c2]]","[some rx3 [and c1 c2]]",true);
        assertSubsumedBy("[and [some rx3a c1] [some rx4a c2]]","[some rx3a [and c1 c2]]",false);
    }
    public void testIanBug1b() throws Exception {
        addAxiom("[equivalent c [and a [not b]]]");
        addAxiom("[subClassOf a [and d [not c]]]");
        assertSatisfiable("[and [not c] a [not b] d]",false);
    }
    public void testIanBug3() throws Exception {
        // slow, but works!
        assertSatisfiable("[and [some r a] [atLeast 3 r c] [atLeast 3 r d] [atLeast 2 r [and e [not [and c d]]]] [atMost 4 r] [atMost 2 r [and c d]]]",true);
    }
    public void testIanBug4() throws Exception {
        addAxiom("[objectInverse r r-]");
        addAxiom("[subObjectPropertyOf r r-]");
        addAxiom("[objectTransitive r]");
        assertSatisfiable("[and c [some r owl:Thing] [all r [not c]]]",false);
        assertSatisfiable("[and c [some r [some r c]] [all r [not c]]]",false);
    }
    public void testIanBug5() throws Exception {
        addAxiom("[objectTransitive r1]");
        addAxiom("[subObjectPropertyOf r2 r1]");
        addAxiom("[objectTransitive r2]");
        assertSatisfiable("[and [all r1 p] [some r2 [some r1 [not p]]]]",false);
    }
    public void testIanBug6() throws Exception {
        addAxiom("[subObjectPropertyOf S1 R]");
        addAxiom("[objectTransitive S1]");
        addAxiom("[subObjectPropertyOf S2 R]");
        addAxiom("[objectTransitive S2]");
        addAxiom("[subObjectPropertyOf P S1]");
        addAxiom("[subObjectPropertyOf P S2]");
        assertSatisfiable("[and [all R C] [some P [some S1 [not C]]]]",false);
        assertSatisfiable("[and [all R C] [some P [some S2 [not C]]]]",false);
    }
    public void testIanBug7() throws Exception {
        addAxiom("[subClassOf A [not B]]");
        assertSatisfiable("[and [some r A] [atMost 1 r A] [some r B] [atMost 1 r B]]",true);
    }
    public void testIanBug8() throws Exception {
        addAxiom("[subClassOf X [not Y]]");
        addAxiom("[subClassOf A [and [atLeast 1 r X] [atMost 1 r X]]]");
        addAxiom("[subClassOf A [and [atLeast 1 r Y] [atMost 1 r Y]]]");
        assertSatisfiable("A",true);
    }
    public void testIanMergeTest1() throws Exception {
        addAxiom("[objectInverse r r-]");
        addAxiom("[subClassOf c [some r [all r- [not d]]]]");
        assertSatisfiable("[and [some r c1] [some r [and c c2]] [some r [and c c3]] [some r [and c c4]] [some r [and c c5]] [some r [and c c6]] [some r [and c c7]] [some r [and c c8]] [some r [and c c9]] [some r [and c c10]] [some r [and c c11]] [some r [and c c12]] [some r [and c c13]] [some r [and c c14]] [some r [and c c15]] [some r [and c c16]] [some r [and c c17]] [some r c18] [atMost 1 r d]]",true);
    }
    public void testIanMergeTest2() throws Exception {
        addAxiom("[objectInverse r r-]");
        addAxiom("[subClassOf c [some r [all r- d]]]");
        assertSatisfiable("[and [some r c1] [some r [and c c2]] [some r [and c c3]] [some r [and c c4]] [some r [and c c5]] [some r [and c c6]] [some r [and c c7]] [some r [and c c8]] [some r [and c c9]] [some r [and c c10]] [some r [and c c11]] [some r [and c c12]] [some r [and c c13]] [some r [and c c14]] [some r [and c c15]] [some r [and c c16]] [some r [and c c17]] [some r c18] [atMost 1 r d]]",true);
    }
    public void testIanQNRTest() throws Exception {
        addAxiom("[subObjectPropertyOf son child]");
        addAxiom("[subObjectPropertyOf daughter child]");
        addAxiom("[equivalent A [and [atLeast 2 son male] [atLeast 2 daughter [not male]]]]");
        addAxiom("[equivalent B [and [atLeast 4 child]]]");
        assertSubsumedBy("A","B",true);
    }
    public void testIanRecursiveDefinitionTest1() throws Exception {
        addAxiom("[subClassOf A [and [some R0 B] [some R1 B] [some R2 B] [some R3 B] [some R4 B] [some R5 B] [some R6 B] [some R7 B] [some R8 B] [some R9 B]]]");
        addAxiom("[subClassOf B [and [some R0 A] [some R1 A] [some R2 A] [some R3 A] [some R4 A] [some R5 A] [some R6 A] [some R7 A] [some R8 A] [some R9 A]]]");
        assertSatisfiable("A",true);
    }
    public void testIanRecursiveDefinitionTest2() throws Exception {
        addAxiom("[subClassOf A [and [some R0 B] [some R1 B] [some R2 B] [some R3 B] [some R4 B] [some R5 B] [some R6 B] [some R7 B] [some R8 B] [some R9 B]]]");
        addAxiom("[subClassOf B [and [some R0 C] [some R1 C] [some R2 C] [some R3 C] [some R4 C] [some R5 C] [some R6 C] [some R7 C] [some R8 C] [some R9 C]]]");
        addAxiom("[subClassOf C [and [some R0 A] [some R1 A] [some R2 A] [some R3 A] [some R4 A] [some R5 A] [some R6 A] [some R7 A] [some R8 A] [some R9 A]]]");
        assertSatisfiable("A",true);
    }
    public void testIanRecursiveDefinitionTest3() throws Exception {
        addAxiom("[subClassOf A [and [some R0 B] [some R1 B] [some R2 B] [some R3 B] [some R4 B] [some R5 B] [some R6 B] [some R7 B] [some R8 B] [some R9 B]]]");
        addAxiom("[subClassOf B [and [some R0 C] [some R1 C] [some R2 C] [some R3 C] [some R4 C] [some R5 C] [some R6 C] [some R7 C] [some R8 C] [some R9 C]]]");
        addAxiom("[subClassOf C [and [some R0 D] [some R1 D] [some R2 D] [some R3 D] [some R4 D] [some R5 D] [some R6 D] [some R7 D] [some R8 D] [some R9 D]]]");
        addAxiom("[subClassOf D [and [some R0 A] [some R1 A] [some R2 A] [some R3 A] [some R4 A] [some R5 A] [some R6 A] [some R7 A] [some R8 A] [some R9 A]]]");
        assertSatisfiable("A",true);
    }
    public void testIanBackjumping1() throws Exception {
        addAxiom("[subClassOf C1 [and [or A0 B0] [or A1 B1] [or A2 B2] [or A3 B3] [or A4 B4] [or A5 B5] [or A6 B6] [or A7 B7] [or A8 B8] [or A9 B9] [or A10 B10] [or A11 B11] [or A12 B12] [or A13 B13] [or A14 B14] [or A15 B15] [or A16 B16] [or A17 B17] [or A18 B18] [or A19 B19] [or A20 B20] [or A21 B21] [or A22 B22] [or A23 B23] [or A24 B24] [or A25 B25] [or A26 B26] [or A27 B27] [or A28 B28] [or A29 B29] [or A30 B30] [or A31 B31]]]");
        addAxiom("[subClassOf C2 [and [or A B] [or A [not B]]]]");
        addAxiom("[subClassOf C3 [and [or [not A] B] [or [not A] [not B]]]]");
        addAxiom("[subClassOf C4 [some R C2]]");
        addAxiom("[subClassOf C5 [all R C3]]");
        addAxiom("[subClassOf test [and C1 C4 C5]]");
        assertSatisfiable("test",false);
    }
    public void testIanBackjumping2() throws Exception {
        addAxiom("[subClassOf C2 [and [or A B] [or A [not B]]]]");
        addAxiom("[subClassOf C3 [and [or [not A] B] [or [not A] [not B]]]]");
        addAxiom("[subClassOf C4 [some R [and C2 C8]]]");
        addAxiom("[subClassOf C5 [all R [and C3 C9]]]");
        addAxiom("[subClassOf C6 [some R [and C2 C10]]]");
        addAxiom("[subClassOf C7 [all R [and C3 C11]]]");
        addAxiom("[subClassOf test [and [or A0 B0] [or A1 B1] [or A2 B2] [or A3 B3] [or A4 B4] [or A5 B5] [or A6 B6] [or A7 B7] [or A8 B8] [or A9 B9] [or A10 B10] [or A11 B11] [or A12 B12] [or A13 B13] [or A14 B14] [or A15 B15] [or A16 B16] [or A17 B17] [or A18 B18] [or A19 B19] [or A20 B20] [or A21 B21] [or A22 B22] [or A23 B23] [or A24 B24] [or A25 B25] [or A26 B26] [or A27 B27] [or A28 B28] [or A29 B29] [or A30 B30] [or A31 B31]]]");
        assertSatisfiable("test",true);
    }
    public void testIanBackjumping3() throws Exception {
        addAxiom("[subClassOf C2 [and [or A B] [or A [not B]]]]");
        addAxiom("[subClassOf C3 [and [or [not A] B] [or [not A] [not B]]]]");
        addAxiom("[subClassOf C4 [some R [and C2 C8]]]");
        addAxiom("[subClassOf C5 [all R [and C3 C9]]]");
        addAxiom("[subClassOf C6 [some R [and C2 C10]]]");
        addAxiom("[subClassOf C7 [all R [and C3 C11]]]");
        addAxiom("[subClassOf test [and [or A0 B0] [or A1 B1] [or A2 B2] [or A3 B3] [or A4 B4] [or A5 B5] [or A6 B6] [or A7 B7] [or A8 B8] [or A9 B9] [or A10 B10] [or A11 B11] [or A12 B12] [or A13 B13] [or A14 B14] [or A15 B15] [or A16 B16] [or A17 B17] [or A18 B18] [or A19 B19] [or A20 B20] [or A21 B21] [or A22 B22] [or A23 B23] [or A24 B24] [or A25 B25] [or A26 B26] [or A27 B27] [or A28 B28] [or A29 B29] [or A30 B30] [or A31 B31] [or C4 C6] [or C5 C7]]]");
        assertSatisfiable("test",false);
    }
    public void testNominals1() throws Exception {
        addAxiom("[classMember A a]");
        addAxiom("[classMember A b]");
        addAxiom("[subClassOf A [some R A]]");
        addAxiom("[subClassOf A [some S [oneOf n]]]");
        assertABoxSatisfiable(true);
    }
    public void testNominals2() throws Exception {
        addAxiom("[classMember A a]");
        addAxiom("[subClassOf A [some R A]]");
        addAxiom("[subClassOf A [some S [oneOf n]]]");
        addAxiom("[classMember B b]");
        addAxiom("[subClassOf B [some R B]]");
        addAxiom("[subClassOf B [some S [oneOf n]]]");
        addAxiom("[disjoint A B]");
        addAxiom("[classMember [atMost 5 [inv S]] n]");
        assertABoxSatisfiable(true);
    }
    public void testNominals3() throws Exception {
        addAxiom("[subClassOf A [some R A]]");
        addAxiom("[subClassOf A [some S [oneOf n]]]");
        addAxiom("[classMember [some R A] a]");
        addAxiom("[subClassOf B [some R B]]");
        addAxiom("[subClassOf B [some S [oneOf n]]]");
        addAxiom("[classMember [some R B] b]");
        addAxiom("[classMember [atMost 1 [inv S]] n]");
        assertInstanceOf("[some [inv S] [and A B [some R [and A B]]]]","n",true);
    }
    public void testNominals4() throws Exception {
        addAxiom("[disjoint A B]");
        addAxiom("[subClassOf A [some R A]]");
        addAxiom("[subClassOf A [some S [oneOf n]]]");
        addAxiom("[classMember [some R A] a]");
        addAxiom("[subClassOf B [some R B]]");
        addAxiom("[subClassOf B [some S [oneOf n]]]");
        addAxiom("[classMember [some R B] b]");
        addAxiom("[classMember [atMost 2 [inv S]] n]");
        assertInstanceOf("[some [inv S] [and A [some R A]]]","n",true);
        assertInstanceOf("[some [inv S] [and B [some R B]]]","n",true);
    }
    public void testNominals5() throws Exception {
        addAxiom("[disjoint A B]");
        addAxiom("[subClassOf A [some R A]]");
        addAxiom("[subClassOf A [some S [oneOf n]]]");
        addAxiom("[classMember [some R A] a]");
        addAxiom("[subClassOf B [some R B]]");
        addAxiom("[subClassOf B [some S [oneOf n]]]");
        addAxiom("[classMember [some R B] b]");
        addAxiom("[classMember [atMost 2 [inv S]] n]");
        assertInstanceOf("[atLeast 2 [inv S] [or A B]]","n",true);
    }
    public void testNominals6() throws Exception {
        addAxiom("[disjoint A B]");
        addAxiom("[subClassOf A [some R A]]");
        addAxiom("[subClassOf A [some S [oneOf n]]]");
        addAxiom("[classMember [some R A] a]");
        addAxiom("[subClassOf B [some R B]]");
        addAxiom("[subClassOf B [some S [oneOf n]]]");
        addAxiom("[classMember [some R B] b]");
        addAxiom("[classMember [atMost 2 [inv S]] n]");
        assertInstanceOf("[atLeast 1 [inv S] [not A]]","n",true);
        assertInstanceOf("[atLeast 2 [inv S] [not A]]","n",false);
    }
    public void testDependencyDisjunctionMergingBug() throws Exception {
        loadResource("res/dependency-disjuntion-merging-bug.xml");
        assertSubsumedBy("http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#Anjou","http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#FullBodiedWine",false);
    }
    public void testWineNoDataProperties() throws Exception {
        loadResource("res/wine-no-data-properties.xml");
        assertSubsumptionHierarchy("res/wine-no-data-properties.xml.txt");
    }
    public void testGalenIansFullUndoctored() throws Exception {
        loadResource("res/galen-ians-full-undoctored.xml");
        assertSubsumptionHierarchy("res/galen-ians-full-undoctored.xml.txt");
    }
}
