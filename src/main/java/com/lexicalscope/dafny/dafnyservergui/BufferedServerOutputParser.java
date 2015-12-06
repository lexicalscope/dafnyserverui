package com.lexicalscope.dafny.dafnyservergui;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class BufferedServerOutputParser implements Runnable {
    private final BlockingQueue<String> serverOutputBuffer;
    private final ServerOutputParser parser;
    private final List<ServerEventListener>  serverEventListeners = new CopyOnWriteArrayList<>();

    public BufferedServerOutputParser(
            final BlockingQueue<String> serverOutputBuffer,
            final String filename) {
        this.serverOutputBuffer = serverOutputBuffer;
        this.parser = new ServerOutputParser(filename);
        parser.add(new ServerEventBroadcaster(this::fire));
    }

    @Override public void run() {
        while(!Thread.currentThread().isInterrupted()) {
            try {
                parser.outputLine(serverOutputBuffer.take());
            } catch (final InterruptedException e) {
                break;
            }
        }
    }

    public void addEventListener(final ServerEventListener serverEventListener) {
        serverEventListeners.add(serverEventListener);
    }

    private void fire(final Consumer<ServerEventListener> l) {
        serverEventListeners.forEach(l);
    }

    public static BufferedServerOutputParser createServerOutputParser(final BlockingQueue<String> serverOutputBuffer, final String filename) {
        return new BufferedServerOutputParser(serverOutputBuffer, filename);
    }
}
