package org.semanticweb.HermiT.cli;
class StatusOutput {
    protected int level;
    public StatusOutput(int inLevel) {
        level=inLevel;
    }
    static public final int ALWAYS=0;
    static public final int STATUS=1;
    static public final int DETAIL=2;
    static public final int DEBUG=3;
    public void log(int inLevel,String message) {
        if (inLevel<=level)
            System.err.println(message);
    }
}