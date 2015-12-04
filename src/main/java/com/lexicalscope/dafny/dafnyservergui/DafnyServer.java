package com.lexicalscope.dafny.dafnyservergui;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import com.google.common.io.ByteStreams;
import com.google.gson.Gson;

public class DafnyServer {
    private final Process process;
    private final OutputStream outputStream;
    private final InputStream inputStream;
    private final ExecutorService executor;

    public DafnyServer(final ExecutorService executor, final Process process, final Consumer<String> outputHandler) {
        this.executor = executor;
        this.process = process;
        this.outputStream = process.getOutputStream();
        this.inputStream = process.getInputStream();
    }

    public void sendMessage(final MessageToServer message)
    {
        byte[] messageBytes;
        try {
            messageBytes = messageToServer(message);
            synchronized (outputStream) {
                outputStream.write(messageBytes);
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void pipeOutput()
    {
        executor.submit(() -> {
            try {
                synchronized (inputStream) {
                     ByteStreams.copy(inputStream, System.out);
                }
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void shutdown() {
        try
        {
            executor.shutdownNow();
        }
        finally
        {
            try {
                inputStream.close();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
    }

    static byte[] messageToServer(final MessageToServer message) throws UnsupportedEncodingException {
        final String lineSeparator = System.lineSeparator();

        final String json = new Gson().toJson(message);
        final byte[] jsonBytes = json.getBytes("UTF-8");
        final byte[] encodedJson = Base64.getEncoder().encode(jsonBytes);

        final byte[] verify = ("verify" + lineSeparator).getBytes("UTF-8");
        final byte[] end = (lineSeparator + "[[DAFNY-CLIENT: EOM]]" + lineSeparator + lineSeparator).getBytes("UTF-8");

        final byte[] messageBytes = new byte[verify.length + encodedJson.length + end.length];
        System.arraycopy(verify, 0, messageBytes, 0, verify.length);
        System.arraycopy(encodedJson, 0, messageBytes, verify.length, encodedJson.length);
        System.arraycopy(end, 0, messageBytes, verify.length + encodedJson.length, end.length);
        return messageBytes;
    }
}
