package com.lexicalscope.dafny.dafnyservergui.gui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

public class VerificationTable extends JTable {
    private static final long serialVersionUID = 1911560787499087582L;

    public VerificationTable(final VerificationModel verificationModel) {
        super(verificationModel);


        final int[] widths = new int[]{180,85};
        for (int i = 0; i < widths.length; i++) {
            final TableColumn column = getColumnModel().getColumn(i);
            column.setMinWidth(widths[i]);
            column.setMaxWidth(widths[i]);
            column.setPreferredWidth(widths[i]);
        }
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

        return c;
    }

}
