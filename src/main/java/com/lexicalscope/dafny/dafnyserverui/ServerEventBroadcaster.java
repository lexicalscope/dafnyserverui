package com.lexicalscope.dafny.dafnyserverui;

import java.util.function.Consumer;

public final class ServerEventBroadcaster implements ServerEventListener {
    private final Consumer<Consumer<ServerEventListener>> broadcaster;

    public ServerEventBroadcaster(final String name, final Consumer<Consumer<ServerEventListener>> broadcaster) {
        this.broadcaster = broadcaster;
    }

    @Override public void log(final String fileName, final int lineNumber, final int columnNumber, final String level, final String message) {
        broadcaster.accept(l -> l.log(fileName, lineNumber, columnNumber, level, message));
    }

    @Override public void time(final TimingBookend bookend, final TimingEvent event, final long hunderedNanoseconds) {
        broadcaster.accept(l -> l.time(bookend, event, hunderedNanoseconds));
    }

    @Override public void verifying(final VerificationType verificationType, final String module, final String procedure) {
        broadcaster.accept(l -> l.verifying(verificationType, module, procedure));
    }

    @Override public void cached(final VerificationType verificationType, final String module, final String procedure) {
        broadcaster.accept(l -> l.cached(verificationType, module, procedure));
    }

    @Override public void verifed(final int proofObligations) {
        broadcaster.accept(l -> l.verifed(proofObligations));
    }

    @Override public void verficationCompleted() {
        broadcaster.accept(l -> l.verficationCompleted());
    }

    @Override public void unrecognised(final String line) {
        broadcaster.accept(l -> l.unrecognised(line));
    }

    @Override public void blankLine() {
        broadcaster.accept(l -> l.blankLine());
    }

    @Override public void eom() {
        broadcaster.accept(l -> l.eom());
    }
}