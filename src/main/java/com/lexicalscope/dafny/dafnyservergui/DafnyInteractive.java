package com.lexicalscope.dafny.dafnyservergui;

import java.awt.HeadlessException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.antlr.v4.runtime.RecognitionException;

import com.lexicalscope.dafny.dafnyserver.DafnyServerBaseVisitor;
import com.lexicalscope.dafny.dafnyserver.DafnyServerParser.VerificationArtifactContext;
import com.lexicalscope.dafny.dafnyserver.DafnyServerParser.VerificationCompletedContext;
import com.lexicalscope.jewel.cli.CliFactory;

public class DafnyInteractive
{
    public static void main(final String[] args) throws IOException, InterruptedException, HeadlessException, InvocationTargetException
    {
        final Arguments arguments = CliFactory.parseArguments(Arguments.class, args);

        final ProcessBuilder pb = new ProcessBuilder(arguments.server());
        pb.redirectErrorStream(true);
        final ExecutorService threadPool = Executors.newCachedThreadPool();
        final Process process = pb.start();

        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override public void run() {
                try
                {
                    threadPool.shutdownNow();
                }
                finally
                {
                    process.destroy();
                }
            }
        });

        final LinkedBlockingQueue<String> serverOutputBuffer = new LinkedBlockingQueue<String>();

        final DafnyServer server = new DafnyServer(process.getInputStream(), process.getOutputStream());
        //server.addOutputListener(logToConsole());
        server.addOutputListener(logToQueue(serverOutputBuffer));

        final AntlrServerOutputParser parser = AntlrServerOutputParser.createServerOutputParser(serverOutputBuffer);
        parser.addParsingListener(logParsingToConsole());

        DafnyServerFrame.show(server, arguments);

        threadPool.submit(parser);
        threadPool.submit(server);
    }

    private static ParsingResultListener logParsingToConsole() {
        return new ParsingResultListener() {
            @Override public void verificationArtifact(final VerificationArtifactContext verificationArtifact) {
                new DafnyServerBaseVisitor<Void>(){
                    @Override
                    public Void visitVerificationCompleted(final VerificationCompletedContext ctx) {
                        System.out.println("verification completed");
                        return null;
                    };

                    @Override
                    public Void visitLogLine(final com.lexicalscope.dafny.dafnyserver.DafnyServerParser.LogLineContext ctx) {
                        System.out.println("log line");
                        return null;
                    };
                }.visit(verificationArtifact);
            }

            @Override public void syntaxError(final RecognitionException e) {
                System.out.println(e);
            }
        };
    }

    private static RawServerOutputListener logToQueue(final BlockingQueue<String> queue) {
        return line -> queue.add(line);
    }

    static RawServerOutputListener logToConsole() {
        return message -> {System.out.println(message);};
    }
}
