package com.lexicalscope.dafny.dafnyservergui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import com.google.common.base.Splitter;
import com.google.gson.Gson;

public class DafnyServer implements Runnable {
    private final Object inputLock = new Object();
    private final Object outputLock = new Object();
    private final Object listenerLock = new Object();

    private final OutputStream outputStream;
    private BufferedReader reader;
    private final List<ServerOutputListener> outputHandlers = new ArrayList<>();

    public DafnyServer(final InputStream inputStream, final OutputStream outputStream) {
        this.outputStream = outputStream;
        try {
            this.reader = new BufferedReader(new InputStreamReader(inputStream, "US-ASCII"));
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public void addOutputListener(final ServerOutputListener outputListener)
    {
        synchronized (listenerLock) {
            outputHandlers.add(outputListener);
        }
    }

    private void broadcast(final String line) {
        List<ServerOutputListener> copy;
        synchronized (listenerLock) {
            copy = new ArrayList<>(outputHandlers);
        }
        copy.forEach(listener -> listener.outputLine(line));
    }

    public void sendMessage(final MessageToServer message)
    {
        byte[] messageBytes;
        try {
            messageBytes = messageToServer(message);
            synchronized (outputLock) {
                System.out.println(new String(messageBytes, "US-ASCII"));
                outputStream.write(messageBytes);
                outputStream.flush();
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run()
    {
        try {
            while(!Thread.currentThread().isInterrupted())
            {
                String line;
                synchronized (inputLock) {
                    line = reader.readLine();
                }
                if(line != null) {
                    broadcast(line);
                }
            }
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    static byte[] messageToServer(final MessageToServer message) throws UnsupportedEncodingException {
        final String lineSeparator = System.lineSeparator();

        final String json = new Gson().toJson(message);
        final byte[] jsonBytes = json.toString().getBytes("US-ASCII");
        final byte[] encodedJsonBytes = Base64.getEncoder().encode(jsonBytes);
        final String encodedJson = new String(encodedJsonBytes, "US-ASCII");

        final String brokenJson = wrapJsonAt76Chars(lineSeparator, encodedJson);

        final byte[] verify = ("verify" + lineSeparator).getBytes("US-ASCII");
        final byte[] brokenJsonBytes = brokenJson.getBytes("US-ASCII");
        final byte[] end = (lineSeparator + "[[DAFNY-CLIENT: EOM]]" + lineSeparator).getBytes("US-ASCII");

        return concatMessage(verify, brokenJsonBytes, end);
    }

    static String wrapJsonAt76Chars(final String lineSeparator, final String encodedJson) {
        final Iterable<String> splitJson = Splitter.fixedLength(76).split(encodedJson);
        final StringBuilder brokenJson = new StringBuilder();
        String separator = "";
        for (final String string : splitJson) {
            brokenJson.append(separator).append(string);
            separator = lineSeparator;
        }
        return brokenJson.toString();
    }

    static byte[] concatMessage(
            final byte[] verify,
            final byte[] brokenJsonBytes,
            final byte[] end) {
        final byte[] messageBytes = new byte[verify.length + brokenJsonBytes.length + end.length];
        System.arraycopy(verify, 0, messageBytes, 0, verify.length);
        System.arraycopy(brokenJsonBytes, 0, messageBytes, verify.length, brokenJsonBytes.length);
        System.arraycopy(end, 0, messageBytes, verify.length + brokenJsonBytes.length, end.length);
        return messageBytes;
    }
}
