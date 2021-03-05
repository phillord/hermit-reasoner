package org.semanticweb.HermiT.cli;
class StatusOutput {
    protected int level;
    public StatusOutput(int inLevel) {
        level=inLevel;
    }
    public static final int ALWAYS=0;
    public static final int STATUS=1;
    public static final int DETAIL=2;
    public static final int DEBUG=3;
    public void log(int inLevel,String message) {
        if (inLevel<=level)
            System.err.println(message);
    }
}