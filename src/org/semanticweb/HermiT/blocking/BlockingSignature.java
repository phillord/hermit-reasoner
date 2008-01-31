package org.semanticweb.HermiT.blocking;

import org.semanticweb.HermiT.tableau.*;

public abstract class BlockingSignature {
    protected BlockingSignature m_nextEntry;
    
    public BlockingSignature() {
        m_nextEntry=null;
    }
    public final BlockingSignature getNextEntry() {
        return m_nextEntry;
    }
    public void setNextEntry(BlockingSignature nextEntry) {
        m_nextEntry=nextEntry;
    }
    public abstract boolean blocksNode(Node node);
    public abstract int hashCode();
    public abstract boolean equals(Object that);
}
