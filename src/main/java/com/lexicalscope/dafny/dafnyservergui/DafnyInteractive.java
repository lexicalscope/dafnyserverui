package com.lexicalscope.dafny.dafnyservergui;

import java.awt.HeadlessException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

import javax.swing.SwingUtilities;

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

        final VerificationModel verificationModel = new VerificationModel();
        final DafnyServer server = new DafnyServer(process.getInputStream(), process.getOutputStream());
        //server.addOutputListener(logToConsole());
        server.addOutputListener(logToQueue(serverOutputBuffer));

        final BufferedServerOutputParser parser = BufferedServerOutputParser.createServerOutputParser(serverOutputBuffer, arguments.file());
        parser.addEventListener(new ServerEventBroadcaster(forwardServerEventsOnEdt(verificationModel)));

        DafnyServerFrame.show(server, arguments, verificationModel);

        threadPool.submit(parser);
        threadPool.submit(server);
    }

    private static Consumer<Consumer<ServerEventListener>> forwardServerEventsOnEdt(final ServerEventListener l) {
        return forwarder -> SwingUtilities.invokeLater(() -> forwarder.accept(l));
    }

    private static ServerOutputListener logToQueue(final BlockingQueue<String> queue) {
        return line -> queue.add(line);
    }

    static ServerOutputListener logToConsole() {
        return message -> {System.out.println(message);};
    }
}
