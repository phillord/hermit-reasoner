package org.semanticweb.HermiT.tableau;

import org.semanticweb.HermiT.model.DataRange;
import org.semanticweb.HermiT.monitor.TableauMonitor;

public class DatatypeManager {
    protected final TableauMonitor m_tableauMonitor;
    protected final ExtensionManager m_extensionManager;
    protected final ExtensionTable.Retrieval m_binaryExtensionTableDeltaOldRetrieval;
    protected final ExtensionTable.Retrieval m_binaryExtensionTable1BoundRetrieval;
    protected final UnionDependencySet m_binaryUnionDependencySet;

    public DatatypeManager(Tableau tableau) {
        m_tableauMonitor=tableau.m_tableauMonitor;
        m_extensionManager=tableau.m_extensionManager;
        m_binaryExtensionTableDeltaOldRetrieval=m_extensionManager.getBinaryExtensionTable().createRetrieval(new boolean[2],ExtensionTable.View.DELTA_OLD);
        m_binaryExtensionTable1BoundRetrieval=m_extensionManager.getBinaryExtensionTable().createRetrieval(new boolean[] { false,true },ExtensionTable.View.EXTENSION_THIS);
        m_binaryUnionDependencySet=new UnionDependencySet(2);
    }
    public void checkDatatypeConstraints() {
        Object[] tupleBuffer=m_binaryExtensionTableDeltaOldRetrieval.getTupleBuffer();
        m_binaryExtensionTableDeltaOldRetrieval.open();
        while (!m_binaryExtensionTableDeltaOldRetrieval.afterLast()) {
            if (tupleBuffer[0] instanceof DataRange)
                checkNewDatatypeAssertion((DataRange)tupleBuffer[0],(Node)tupleBuffer[1],m_binaryExtensionTableDeltaOldRetrieval.getDependencySet());
            m_binaryExtensionTableDeltaOldRetrieval.next();
        }
    }
    protected void checkNewDatatypeAssertion(DataRange dataRange,Node node,DependencySet dependencySet) {
        Object[] tupleBuffer=m_binaryExtensionTable1BoundRetrieval.getTupleBuffer();
        m_binaryExtensionTable1BoundRetrieval.getBindingsBuffer()[1]=node;
        m_binaryExtensionTable1BoundRetrieval.open();
        while (!m_binaryExtensionTable1BoundRetrieval.afterLast()) {
            if (!isCompatible(dataRange,(DataRange)tupleBuffer[0])) {
                if (m_tableauMonitor!=null)
                    m_tableauMonitor.clashDetected( new Object[][] { new Object[] { dataRange,node },new Object[] { tupleBuffer[0],node } });
                m_binaryUnionDependencySet.m_dependencySets[0]=dependencySet;
                m_binaryUnionDependencySet.m_dependencySets[1]=m_binaryExtensionTable1BoundRetrieval.getDependencySet();
                m_extensionManager.setClash(m_binaryUnionDependencySet);
            }
            m_binaryExtensionTable1BoundRetrieval.next();
        }
    }
    protected boolean isCompatible(DataRange dataRange1,DataRange dataRange2) {
        return true;
    }
}
