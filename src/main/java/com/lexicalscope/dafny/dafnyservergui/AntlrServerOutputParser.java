package com.lexicalscope.dafny.dafnyservergui;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;

import com.lexicalscope.dafny.dafnyserver.DafnyServerLexer;
import com.lexicalscope.dafny.dafnyserver.DafnyServerParser;
import com.lexicalscope.dafny.dafnyserver.DafnyServerParser.VerificationArtifactContext;

public class AntlrServerOutputParser implements Runnable {
    private final Object listenerLock = new Object();
    private final List<ParsingResultListener> listeners = new ArrayList<>();
    private final BlockingQueue<String> queue;
    private String unparsedInput = "";

    private AntlrServerOutputParser(final BlockingQueue<String> queue) {
        this.queue = queue;
    }

    @Override public void run() {
        try {
            tryToParse();
        } catch (final InterruptedException e) {
            // if we are interrupted we stop
        }
    }

    private void tryToParse() throws InterruptedException {
        while (!Thread.currentThread().isInterrupted())
        {
            more(queue.take());
            try {
                final DafnyServerLexer lexer = new DafnyServerLexer(new ANTLRInputStream(unparsedInput));
                final CommonTokenStream tokens = new CommonTokenStream(lexer);
                final DafnyServerParser parser = new DafnyServerParser(tokens);
                parser.removeErrorListeners();

                final VerificationArtifactContext verificationArtifact = parser.verificationArtifact();
                clearInput();
                fireParseResult(verificationArtifact);
            } catch (final RecognitionException e) {
                if (e.getOffendingToken().getType() == Token.EOF)
                {
                    // we just ran out of input, try again when more input arrives
                }
                else
                {
                    // the input was garbage, throw it away and hope we can resynchronise at some point
                    clearInput();
                    fireSyntaxError(e);
                }
            }
        }
    }

    private void fireParseResult(final VerificationArtifactContext verificationArtifact) {
        listeners().forEach(l -> l.verificationArtifact(verificationArtifact));
    }

    private void fireSyntaxError(final RecognitionException e) {
        listeners().forEach(l -> l.syntaxError(e));
    }

    private List<ParsingResultListener> listeners() {
        synchronized (listenerLock) {
            return new ArrayList<>(listeners);
        }
    }

    public void addParsingListener(final ParsingResultListener listener) {
        synchronized (listenerLock) {
            listeners.add(listener);
        }
    }

    private void clearInput() {
        unparsedInput = "";
    }

    private void more(final String line) {
        unparsedInput += line + System.lineSeparator();
    }

    public static AntlrServerOutputParser createServerOutputParser(final BlockingQueue<String> serverOutputBuffer) {
        return new AntlrServerOutputParser(serverOutputBuffer);
    }
}
