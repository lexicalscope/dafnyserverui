package com.lexicalscope.dafny.dafnyserverui;

public interface ServerEvent {
    void fire(ServerEventListener l);
}
