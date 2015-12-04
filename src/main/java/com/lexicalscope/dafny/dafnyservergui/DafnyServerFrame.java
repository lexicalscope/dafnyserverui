package com.lexicalscope.dafny.dafnyservergui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class DafnyServerFrame {
    private final JTextPane dafnyOutputPane;

    public DafnyServerFrame(final JTextPane dafnyOutputPane) {
        this.dafnyOutputPane = dafnyOutputPane;
    }

    public static void show(final DafnyServer server, final Arguments arguments) throws HeadlessException, InvocationTargetException, InterruptedException {
        SwingUtilities.invokeAndWait(() -> {
            final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            final JFrame jFrame = new JFrame("Dafny");
            jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            jFrame.setLayout(new BorderLayout());

            final JPanel buttonPane = new JPanel(new GridBagLayout());
            buttonPane.add(new JButton(new AbstractAction() {
                @Override public void actionPerformed(final ActionEvent e) {
                    server.sendMessage(message);
                }
            }));

            final JTextPane dafnyOutputPane = new JTextPane();
            jFrame.getContentPane().add(new JScrollPane(dafnyOutputPane), BorderLayout.CENTER);
            jFrame.setSize(screenSize.width, 400);
            jFrame.setVisible(true);

            final DafnyServerFrame dafnyServerFrame = new DafnyServerFrame(dafnyOutputPane);

            server.addOutputListener(line -> {SwingUtilities.invokeLater(() -> dafnyServerFrame.moreOutput(line));});
        });
    }

    public void moreOutput(final String line)
    {
        final StyledDocument doc = dafnyOutputPane.getStyledDocument();

        final SimpleAttributeSet keyWord = new SimpleAttributeSet();
        StyleConstants.setForeground(keyWord, Color.RED);
        StyleConstants.setBackground(keyWord, Color.YELLOW);
        StyleConstants.setBold(keyWord, true);

        try {
            doc.insertString(doc.getLength(), line, null);
            doc.insertString(doc.getLength(), System.lineSeparator(), null);
        } catch (final BadLocationException e) {
            throw new RuntimeException(e);
        }
    }
}
