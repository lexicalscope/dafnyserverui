package com.lexicalscope.dafny.dafnyservergui;

import java.awt.HeadlessException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.lexicalscope.jewel.cli.CliFactory;

public class DafnyServerGui
{
    public static void main(final String[] args) throws IOException, InterruptedException, HeadlessException, InvocationTargetException
    {
        final Arguments arguments = CliFactory.parseArguments(Arguments.class, args);

        final MessageToServer messageToServer = new MessageToServer();
        messageToServer.source = arguments.file();
        messageToServer.filename = arguments.file();
        messageToServer.sourceIsFile = true;
        messageToServer.args = new String[]{};//"/traceTimes"};

        final ProcessBuilder pb = new ProcessBuilder(arguments.server());
        pb.redirectErrorStream(true);
        final ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(1);
        final Process process = pb.start();


        final DafnyServer server = new DafnyServer(newFixedThreadPool, process);
        server.addOutputListener(message -> {System.out.println(message);});

        DafnyServerFrame.show(server);
            server.sendMessage(messageToServer);

            server.pipeOutput();

            Thread.sleep(15000);

            server.sendMessage(messageToServer);

            Thread.sleep(5000);


        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override public void run() {
                server.close();
            }
        });
    }
}
