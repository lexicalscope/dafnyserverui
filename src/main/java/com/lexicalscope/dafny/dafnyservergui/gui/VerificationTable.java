package com.lexicalscope.dafny.dafnyservergui.gui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class VerificationTable extends JTable {
    private static final long serialVersionUID = 1911560787499087582L;

    public VerificationTable(final VerificationModel verificationModel) {
        super(verificationModel);
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
