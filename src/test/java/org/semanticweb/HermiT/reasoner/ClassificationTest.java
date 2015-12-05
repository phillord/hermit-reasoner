package org.semanticweb.HermiT.reasoner;

public class ClassificationTest extends AbstractReasonerTest {

    public ClassificationTest(String name) {
        super(name);
    }
    public void testWine() throws Exception {
        loadReasonerFromResource("res/wine.xml");
        assertHierarchies("res/wine.xml.txt");
    }
    public void testGalenIansFullUndoctored() throws Exception {
        loadReasonerFromResource("res/galen-ians-full-undoctored.xml");
        assertHierarchies("res/galen-ians-full-undoctored.xml.txt");
    }
    public void testPizza() throws Exception {
        loadReasonerFromResource("res/pizza.xml");
        assertHierarchies("res/pizza.xml.txt");
    }
    public void testPropreo() throws Exception {
        loadReasonerFromResource("res/propreo.xml");
        assertHierarchies("res/propreo.xml.txt");
    }
}
