package com.lexicalscope.dafny.dafnyservergui.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import com.lexicalscope.dafny.dafnyserverui.ServerEventListener;
import com.lexicalscope.dafny.dafnyserverui.VerificationType;

public class VerificationModel extends AbstractTableModel implements ServerEventListener {
    private static final long serialVersionUID = 4193841155859578559L;

    private final List<VerificationStatus> rows = new ArrayList<VerificationStatus>();
    private final Map<FqProcedure, Integer> rowMap = new HashMap<>();

    private VerificationStatus currentRow;

    @Override public int getRowCount() {
        return rows.size();
    }

    @Override public int getColumnCount() {
        return 1;
    }

    @Override public Object getValueAt(final int rowIndex, final int columnIndex) {
        final VerificationStatus status = rows.get(rowIndex);
        switch (columnIndex) {
        case 0:
            return status.procedureName();
        default:
            return null;
        }
    }

    @Override
    public void verifying(final VerificationType verificationType, final String module, final String procedure) {
        final FqProcedure key = new FqProcedure(module, procedure);
        final boolean inserted = !rowMap.containsKey(key);
        if(inserted) {
            rowMap.put(key, rows.size());
            rows.add(new VerificationStatus(module, procedure));
        }
        final int index = rowMap.get(key);
        currentRow = rows.get(index);
        currentRow.verifying(verificationType);

        if(inserted) {
            fireTableRowsInserted(index, index);
        } else {
            fireTableRowsUpdated(index, index);
        }
    }
}
