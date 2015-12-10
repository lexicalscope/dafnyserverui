package com.lexicalscope.dafny.dafnyservergui.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

public class VerificationTable extends JTable {
    private static final long serialVersionUID = 1911560787499087582L;

    public VerificationTable(final VerificationModel verificationModel) {
        super(verificationModel);

        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setColumnSelectionAllowed(false);
        setRowSelectionAllowed(true);

        final int[] widths = new int[]{15,180,85};
        for (int i = 0; i < widths.length; i++) {
            final TableColumn column = getColumnModel().getColumn(i);
            column.setMinWidth(widths[i]);
            column.setMaxWidth(widths[i]);
            column.setPreferredWidth(widths[i]);
        }

        getActionMap().put("next-verification-failure", new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override public void actionPerformed(final ActionEvent e) {
                final int selectedModelRow = convertRowIndexToModel(getSelectedRow());
                verificationModel.getNextFailure(selectedModelRow, nextFailureModelRow -> {
                    final int nextFailureRow = convertRowIndexToView(nextFailureModelRow);
                    getSelectionModel().setSelectionInterval(nextFailureRow, nextFailureRow);
                });
            }
        });

        getActionMap().put("prev-verification-failure", new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override public void actionPerformed(final ActionEvent e) {
                final int selectedModelRow = convertRowIndexToModel(getSelectedRow());
                verificationModel.getPreviousFailure(selectedModelRow, nextFailureModelRow -> {
                    final int previousFailureRow = convertRowIndexToView(nextFailureModelRow);
                    getSelectionModel().setSelectionInterval(previousFailureRow, previousFailureRow);
                });
            }
        });

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK), "next-verification-failure");
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_MASK), "prev-verification-failure");
    }


    @Override public Component prepareRenderer(final TableCellRenderer renderer, final int row, final int column) {
        final Component c = super.prepareRenderer(renderer, row, column);

        final VerificationStatus type = ((VerificationModel)getModel()).getStatus(convertRowIndexToModel(row));
        switch (type.verified()) {
        case Successful:
            c.setBackground(Color.GREEN);
            break;

        case Failure:
            c.setBackground(Color.RED);
            break;

        case Unknown:
            c.setBackground(Color.YELLOW);
            break;

        case ServerError:
            c.setBackground(Color.PINK);
            break;

        case WellFormed:
            c.setBackground(new Color(0,0,182,155));
            break;
        }

        if ((isCellSelected(row, column))) {
            c.setBackground(getSelectionBackground());
        }

        return c;
    }
}
