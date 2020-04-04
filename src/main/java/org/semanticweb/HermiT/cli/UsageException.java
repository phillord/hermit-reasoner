package org.semanticweb.HermiT.cli;

class UsageException extends IllegalArgumentException {
    public UsageException(String inMessage) {
        super(inMessage);
    }
    public UsageException(String inMessage, Throwable t) {
        super(inMessage, t);
    }
}