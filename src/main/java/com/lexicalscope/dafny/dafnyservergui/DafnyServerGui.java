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



        final ProcessBuilder pb = new ProcessBuilder(arguments.server());
        pb.redirectErrorStream(true);
        final ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(1);
        final Process process = pb.start();

        final DafnyServer server = new DafnyServer(newFixedThreadPool, process);
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override public void run() {
                server.close();
            }
        });

        server.addOutputListener(message -> {System.out.println(message);});
        DafnyServerFrame.show(server, arguments);

        server.pipeOutput();
    }
}
