package com.lexicalscope.dafny.dafnyservergui;

public interface PreprocessedServerOutputListener {
    default void log(final String filename, final int lineNumber, final int columnNumber, final String level, final String message) {}
    default void time(final TimingBookend bookend, final TimingEvent event, final long hunderedNanoseconds) {}
    default void verifying(final VerificationType verificationType, final String module, final String procedure) {}
    default void cached(final VerificationType verificationType, final String module, final String procedure) {}
    default void verifed(final int proofObligations) {}
    default void verficationCompleted() {}
    default void unrecognised(final String line) {}
    default void blankLine() {}
    default void eom() {}
}
