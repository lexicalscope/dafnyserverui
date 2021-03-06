package com.lexicalscope.dafny.dafnyservergui.gui;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;

import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.KeyStroke;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

public final class NavigableHtmlPane extends JEditorPane {
    private static final long serialVersionUID = 7497832090531860760L;

    {
        setEditable(false);
        setFocusable(true);
        setContentType("text/html");

        final InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, InputEvent.CTRL_MASK), "next-link-action");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, InputEvent.CTRL_MASK), "previous-link-action");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_MASK), "activate-link-action");
    }

    public void replaceHtml(final String html) {
        final HTMLDocument doc = (HTMLDocument) getDocument();
        final HTMLEditorKit editorKit = (HTMLEditorKit) getEditorKit();

        try {
            doc.remove(0, doc.getLength());
            editorKit.insertHTML(doc, 0, html, 0, 0, null);
        } catch (BadLocationException | IOException e) {
            throw new RuntimeException(e);
        }
        setCaretPosition(0);
    }
}