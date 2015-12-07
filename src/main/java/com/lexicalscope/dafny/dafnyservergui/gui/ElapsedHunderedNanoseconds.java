package com.lexicalscope.dafny.dafnyservergui.gui;

public class ElapsedHunderedNanoseconds {
    private final long timeHunderedNanoseconds;

    public ElapsedHunderedNanoseconds(final long timeHunderedNanoseconds) {
        this.timeHunderedNanoseconds = timeHunderedNanoseconds;
    }

    @Override public String toString() {
        final long fraction = timeHunderedNanoseconds % (1000*1000*10);
        final long seconds = timeHunderedNanoseconds / (1000*1000*10);
        return String.format("%04d.%07d", seconds, fraction);
    }
}
