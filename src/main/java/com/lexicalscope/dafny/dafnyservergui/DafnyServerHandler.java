package com.lexicalscope.dafny.dafnyservergui;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import com.zaxxer.nuprocess.NuAbstractProcessHandler;

public class DafnyServerHandler extends NuAbstractProcessHandler {
    private final byte[] message;

    public DafnyServerHandler(final byte[] message) {
        this.message = message;
    }

    @Override public void onStdout(final ByteBuffer buffer, final boolean closed) {
        if(!closed) {
            outputBytes(buffer);
        }
    }

    @Override public void onStderr(final ByteBuffer buffer, final boolean closed) {
        if(!closed) {
            outputBytes(buffer);
        }
    }

    @Override public boolean onStdinReady(final ByteBuffer buffer) {
        //buffer.compact();
        //buffer.clear();
        buffer.put(message);
        //buffer.put(message);
        buffer.flip();

        return false;
    }

    private final byte[] oldBytes = new byte[0];
    void outputBytes(final ByteBuffer buffer) {
        final byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        System.out.println("equal " + oldBytes.equals(bytes));
        try {
            System.out.append(new String(bytes, "UTF-8"));
        } catch (final UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
