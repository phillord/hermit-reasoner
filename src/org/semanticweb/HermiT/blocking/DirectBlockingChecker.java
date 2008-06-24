// Copyright 2008 by Oxford University; see license.txt for details
package org.semanticweb.HermiT.blocking;

import org.semanticweb.HermiT.tableau.*;

public interface DirectBlockingChecker {
    boolean isBlockedBy(Node blocker,Node blocked);
    int blockingHashCode(Node node);
    boolean canBeBlocker(Node node);
    boolean canBeBlocked(Node node);
    BlockingSignature getBlockingSignatureFor(Node node);
}
