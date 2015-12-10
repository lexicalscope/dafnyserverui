package com.lexicalscope.dafny.dafnyservergui.gui;

public interface ErrorListener {
    void errorAt(String file, String line, String column);
}
