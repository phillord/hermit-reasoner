package org.semanticweb.HermiT.reasoner;

import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Configuration.TableauMonitorType;


public class ClassificationTest extends AbstractReasonerTest {

    public ClassificationTest(String name) {
        super(name);
    }
    protected Configuration getConfiguration() {
        Configuration c=super.getConfiguration();
        c.tableauMonitorType=TableauMonitorType.TIMING;
        return c;
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
//    public void testDolce() throws Exception {
//        loadReasonerFromResource("res/dolce_all.xml");
//        assertHierarchies("res/dolce_all.xml.txt");
//    }
}
