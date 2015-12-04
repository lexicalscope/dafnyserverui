package com.lexicalscope.dafny.dafnyservergui;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.common.io.ByteStreams;
import com.lexicalscope.jewel.cli.CliFactory;

public class DafnyServerGui
{
    public static void main(final String[] args) throws IOException, InterruptedException
    {
        final Arguments arguments = CliFactory.parseArguments(Arguments.class, args);

        final MessageToServer messageToServer = new MessageToServer();
        messageToServer.source = arguments.file();
        messageToServer.filename = arguments.file();
        messageToServer.sourceIsFile = true;
        messageToServer.args = new String[]{"/traceTimes"};

        final ProcessBuilder pb = new ProcessBuilder(arguments.server());
        pb.redirectErrorStream(true);
        final ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(1);
        final Process process = pb.start();

        final InputStream inputStream = process.getInputStream();
        newFixedThreadPool.submit(() -> {
            while(!Thread.currentThread().isInterrupted()) {
                try {
                    ByteStreams.copy(inputStream, System.out);
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        });


        process.getOutputStream().write(message);
        process.getOutputStream().flush();

        Thread.sleep(3000);

        process.getOutputStream().write(message);
        process.getOutputStream().flush();

        process.waitFor(30, TimeUnit.SECONDS);
        newFixedThreadPool.shutdownNow();
    }
}
