package org.semanticweb.HermiT.cli;

class UsageException extends IllegalArgumentException {
    public UsageException(String inMessage) {
        super(inMessage);
    }
}