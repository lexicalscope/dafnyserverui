package com.lexicalscope.dafny.dafnyservergui.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import com.lexicalscope.dafny.dafnyserverui.Arguments;
import com.lexicalscope.dafny.dafnyserverui.DafnyServer;
import com.lexicalscope.dafny.dafnyserverui.MessageToServer;

public class DafnyServerFrame {
    private final JTextPane dafnyOutputPane;

    public DafnyServerFrame(final JTextPane dafnyOutputPane) {
        this.dafnyOutputPane = dafnyOutputPane;
    }

    public static void show(final DafnyServer server, final Arguments arguments, final VerificationModel verificationModel) throws HeadlessException, InvocationTargetException, InterruptedException {
        SwingUtilities.invokeAndWait(() -> {
            constructAndShowUi(server, arguments, verificationModel);
        });
    }

    static void constructAndShowUi(final DafnyServer server, final Arguments arguments, final VerificationModel verificationModel) {
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        final JFrame jFrame = new JFrame("Dafny");
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setLayout(new BorderLayout());

        final JCheckBox traceTimesBox = new JCheckBox();
        traceTimesBox.setSelected(true);
        traceTimesBox.setText("traceTimes");
        final JTextField timeoutField = new JTextField(2);
        timeoutField.setText("" + arguments.timeLimit());
        final JLabel timeoutLabel = new JLabel("timeout");
        timeoutLabel.setLabelFor(timeoutField);

        final JTextField fileField = new JTextField(40);
        fileField.setText(arguments.file());

        final JButton verify = new JButton(new AbstractAction() {
            private static final long serialVersionUID = -5699009837617872554L;
            {
                putValue(Action.NAME, "Go!");
                putValue(Action.MNEMONIC_KEY, KeyEvent.VK_G);
                putValue(Action.DISPLAYED_MNEMONIC_INDEX_KEY, 0);
                putValue(Action.SHORT_DESCRIPTION, "Verify Now");
            }
            @Override public void actionPerformed(final ActionEvent e) {
                final MessageToServer messageToServer = new MessageToServer();
                messageToServer.source = fileField.getText();
                messageToServer.filename = fileField.getText();
                messageToServer.sourceIsFile = true;
                final List<String> args = new ArrayList<>();
                if(traceTimesBox.isSelected()) {
                    args.add("/traceTimes");
                }
                args.add("/timeLimit:" + timeoutField.getText());

                messageToServer.args = args.toArray(new String[args.size()]);
                server.sendMessage(messageToServer);
            }
        });

        final GridBagConstraints c = new GridBagConstraints();
        final JPanel settingsPane = new JPanel(new GridBagLayout());
        settingsPane.add(fileField);
        settingsPane.add(traceTimesBox);
        settingsPane.add(timeoutField);
        settingsPane.add(timeoutLabel);
        c.weightx = 1.0;
        c.anchor = GridBagConstraints.EAST;
        settingsPane.add(verify, c);

        final JTextPane dafnyOutputPane = new JTextPane();

        final JTabbedPane tabs = new JTabbedPane();
        tabs.add("results", new JScrollPane(new VerificationTable(verificationModel)));
        tabs.setMnemonicAt(0, KeyEvent.VK_R);
        tabs.setDisplayedMnemonicIndexAt(0, 0);
        tabs.add("output", new JScrollPane(dafnyOutputPane));
        tabs.setMnemonicAt(1, KeyEvent.VK_O);
        tabs.setDisplayedMnemonicIndexAt(1, 0);

        jFrame.getContentPane().add(tabs, BorderLayout.CENTER);
        jFrame.getContentPane().add(settingsPane, BorderLayout.SOUTH);
        jFrame.setSize(screenSize.width, 400);
        jFrame.setVisible(true);

        final DafnyServerFrame dafnyServerFrame = new DafnyServerFrame(dafnyOutputPane);

        server.addOutputListener(line -> {SwingUtilities.invokeLater(() -> dafnyServerFrame.moreOutput(line));});
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
