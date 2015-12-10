package com.lexicalscope.dafny.dafnyservergui.gui;

import static java.lang.Math.min;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import com.lexicalscope.dafny.dafnyserverui.Arguments;
import com.lexicalscope.dafny.dafnyserverui.DafnyServer;
import com.lexicalscope.dafny.dafnyserverui.MessageToServer;

public class DafnyServerFrame extends JFrame {
    private final class SaveWindowStateListener extends WindowAdapter {
        private final Preferences prefs;

        private SaveWindowStateListener(final Preferences prefs) {
            this.prefs = prefs;
        }

        @Override public void windowClosing(final WindowEvent e) {
            prefs.putInt(WIDTH_KEY, getWidth());
            prefs.putInt(HEIGHT_KEY, getHeight());
            prefs.putInt(EXTENDED_STATE_KEY, getExtendedState());
            prefs.putInt(X_KEY, getX());
            prefs.putInt(Y_KEY, getY());
            try {
                prefs.flush();
            } catch (final BackingStoreException ex) {
                ex.printStackTrace();
            }
        }
    }

    private static final class SendVerifyMessageAction extends AbstractAction {
        private final JTextField fileField;
        private final DafnyServer server;
        private final JTextField timeoutField;
        private final JCheckBox traceTimesBox;
        private static final long serialVersionUID = -5699009837617872554L;

        {
            putValue(Action.NAME, "Verify!");
            putValue(Action.MNEMONIC_KEY, KeyEvent.VK_V);
            putValue(Action.DISPLAYED_MNEMONIC_INDEX_KEY, 0);
            putValue(Action.SHORT_DESCRIPTION, "Verify Now");
        }

        private SendVerifyMessageAction(final JTextField fileField, final DafnyServer server, final JTextField timeoutField,
                final JCheckBox traceTimesBox) {
            this.fileField = fileField;
            this.server = server;
            this.timeoutField = timeoutField;
            this.traceTimesBox = traceTimesBox;
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
    }

    private static final long serialVersionUID = 6704789357703188474L;
    private static final String Y_KEY = "y";
    private static final String X_KEY = "x";
    private static final String EXTENDED_STATE_KEY = "extendedState";
    private static final String HEIGHT_KEY = "height";
    private static final String WIDTH_KEY = "width";
    private final JTextPane dafnyOutputPane;

    public DafnyServerFrame(final DafnyServer server, final Arguments arguments, final VerificationModel verificationModel) {
        super("Dafny");
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new BorderLayout());

        final JCheckBox traceTimesBox = new JCheckBox();
        traceTimesBox.setSelected(true);
        traceTimesBox.setText("traceTimes");
        final JTextField timeoutField = new JTextField(2);
        timeoutField.setText("" + arguments.timeLimit());
        final JLabel timeoutLabel = new JLabel("timeout");
        timeoutLabel.setLabelFor(timeoutField);

        final JTextField fileField = new JTextField(40);
        fileField.setText(arguments.file());

        final JButton verify = new JButton(new SendVerifyMessageAction(fileField, server, timeoutField, traceTimesBox));

        final GridBagConstraints c = new GridBagConstraints();
        final JPanel settingsPane = new JPanel(new GridBagLayout());
        settingsPane.add(fileField);
        settingsPane.add(traceTimesBox);
        settingsPane.add(timeoutField);
        settingsPane.add(timeoutLabel);
        c.weightx = 1.0;
        c.anchor = GridBagConstraints.EAST;
        settingsPane.add(verify, c);

        dafnyOutputPane = new JTextPane();
        final NavigableHtmlPane errorTracePane = new NavigableHtmlPane();

        final VerificationTable verificationTable = new VerificationTable(verificationModel);
        final JScrollPane verificationResults = new JScrollPane(verificationTable);
        final JSplitPane resultsAndTrace = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        resultsAndTrace.setTopComponent(verificationResults);
        resultsAndTrace.setBottomComponent(errorTracePane);

        verificationTable.getSelectionModel().addListSelectionListener(e -> {
            final int selectedRow = verificationTable.getSelectedRow();
            if (selectedRow >= 0) {
                final int modelRow = verificationTable.convertRowIndexToModel(selectedRow);
                errorTracePane.replaceHtml(verificationModel.getTrace(modelRow));
            }
        });

        errorTracePane.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                if (e.getURL() != null) {
                    System.out.println(e.getURL());
                }
            }
        });

        final JTabbedPane tabs = new JTabbedPane();
        tabs.add("results", resultsAndTrace);
        tabs.setMnemonicAt(0, KeyEvent.VK_R);
        tabs.setDisplayedMnemonicIndexAt(0, 0);
        tabs.add("output", new JScrollPane(dafnyOutputPane));
        tabs.setMnemonicAt(1, KeyEvent.VK_O);
        tabs.setDisplayedMnemonicIndexAt(1, 0);

        this.getContentPane().add(tabs, BorderLayout.CENTER);
        this.getContentPane().add(settingsPane, BorderLayout.SOUTH);

        server.addOutputListener(line -> {SwingUtilities.invokeLater(() -> this.moreOutput(line));});

        final Preferences prefs = Preferences.userNodeForPackage(DafnyServerFrame.class);
        this.addWindowListener(new SaveWindowStateListener(prefs));
        this.setSize(prefs.getInt(WIDTH_KEY, screenSize.width), prefs.getInt(HEIGHT_KEY, min(400, screenSize.height)));
        this.setLocation(prefs.getInt(X_KEY, 0), prefs.getInt(Y_KEY, 0));
        this.setExtendedState(prefs.getInt(EXTENDED_STATE_KEY, Frame.NORMAL));
    }

    public static void show(final DafnyServer server, final Arguments arguments, final VerificationModel verificationModel) throws HeadlessException, InvocationTargetException, InterruptedException {
        SwingUtilities.invokeAndWait(() -> {
            new DafnyServerFrame(server, arguments, verificationModel).setVisible(true);
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
