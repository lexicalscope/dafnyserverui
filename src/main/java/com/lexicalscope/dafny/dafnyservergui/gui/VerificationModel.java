package com.lexicalscope.dafny.dafnyservergui.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.table.AbstractTableModel;

import com.lexicalscope.dafny.dafnyserverui.ServerEventListener;
import com.lexicalscope.dafny.dafnyserverui.TimingBookend;
import com.lexicalscope.dafny.dafnyserverui.TimingEvent;
import com.lexicalscope.dafny.dafnyserverui.VerificationType;

public class VerificationModel extends AbstractTableModel implements ServerEventListener {
    private static final long serialVersionUID = 4193841155859578559L;

    private final List<VerificationStatus> rows = new ArrayList<VerificationStatus>();
    private final Map<FqProcedure, Integer> rowMap = new HashMap<>();

    private VerificationStatus currentRow;

    private Integer currentIndex;

    @Override public int getRowCount() {
        return rows.size();
    }

    @Override public int getColumnCount() {
        return 4;
    }

    @Override public String getColumnName(final int columnIndex) {
        switch (columnIndex) {
        case 0:
            return "";
        case 1:
            return "Name";
        case 2:
            return "Seconds";
        case 3:
            return "Message";
        default:
            return null;
        }
    }

    @Override public Object getValueAt(final int rowIndex, final int columnIndex) {
        final VerificationStatus status = rows.get(rowIndex);
        switch (columnIndex) {
        case 0:
            return new Object(){ @Override
                public String toString() {
                    final Verified verified = status.verified();
                    switch (verified) {
                        case Failure:
                            return "F";
                        case ServerError:
                            return "E";
                        case Successful:
                            return "S";
                        case Unknown:
                            return "U";
                        case WellFormed:
                            return "W";
                    }
                    return "";
                };
            };
        case 1:
            return status.procedureName();
        case 2:
            return new ElapsedHunderedNanoseconds(status.timeHunderedNanoseconds());
        case 3:
            return status.message();
        default:
            return null;
        }
    }

    @Override
    public void verifying(final VerificationType verificationType, final String module, final String procedure) {
        if(currentRow != null) {
            currentRow.done();
        }
        final FqProcedure key = new FqProcedure(module, procedure);
        final boolean inserted = !rowMap.containsKey(key);
        if(inserted) {
            rowMap.put(key, rows.size());
            rows.add(new VerificationStatus(module, procedure));
        }
        currentIndex = rowMap.get(key);
        currentRow = rows.get(currentIndex);
        currentRow.verifying(verificationType);


        if(inserted) {
            fireTableRowsInserted(currentIndex, currentIndex);
        } else {
            fireTableRowsUpdated(currentIndex, currentIndex);
        }
    }

    @Override public void verifed(final int proofObligations) {
        currentRow.verified(proofObligations, timeHunderedNanoseconds());
        fireTableRowsUpdated(currentIndex, currentIndex);
    }

    @Override public void failed(final int proofObligations) {
        currentRow.failed(proofObligations, timeHunderedNanoseconds());
        fireTableRowsUpdated(currentIndex, currentIndex);
    }

    @Override public void log(final String filename, final int lineNumber, final int columnNumber, final String level, final String message) {
        // todo - what are these log lines that come before the first row?
        if(currentRow != null)
        {
            currentRow.log(filename, lineNumber, columnNumber, level, message);
        }
    }

    long timeHunderedNanoseconds() {
        final long result = timingState.timeHunderedNanoseconds();
        timingState = new InitialTimingState();
        return result;
    }

    private interface TimingState {
        TimingState starting(long hunderedNanoseconds);
        TimingState finished(long hunderedNanoseconds);
        long timeHunderedNanoseconds();
    }

    private class InitialTimingState implements TimingState {
        @Override public TimingState starting(final long hunderedNanoseconds) {
            return new StartedTimingState(hunderedNanoseconds);
        }

        @Override public TimingState finished(final long hunderedNanoseconds) {
            // sometimes we get a finished by itself, not sure what these correspond to
            return this;
        }

        @Override public long timeHunderedNanoseconds() {
            return 0;
        }
    }

    private class StartedTimingState implements TimingState {
        private final long startedAtHunderedNanoseconds;

        public StartedTimingState(final long startedAtHunderedNanoseconds) {
            this.startedAtHunderedNanoseconds = startedAtHunderedNanoseconds;
        }

        @Override public TimingState starting(final long hunderedNanoseconds) {
            return this;
        }

        @Override public TimingState finished(final long finishedAtHunderedNanoseconds) {
            return new FinishedTimingState(startedAtHunderedNanoseconds, finishedAtHunderedNanoseconds);
        }

        @Override public long timeHunderedNanoseconds() {
            return 0;
        }
    }

    private class FinishedTimingState implements TimingState {
        private final long startedAtHunderedNanoseconds;
        private final long finishedAtHunderedNanoseconds;

        public FinishedTimingState(final long startedAtHunderedNanoseconds, final long finishedAtHunderedNanoseconds) {
            this.startedAtHunderedNanoseconds = startedAtHunderedNanoseconds;
            this.finishedAtHunderedNanoseconds = finishedAtHunderedNanoseconds;
        }

        @Override public TimingState starting(final long hunderedNanoseconds) {
            return new StartedTimingState(hunderedNanoseconds);
        }

        @Override public TimingState finished(final long hunderedNanoseconds) {
            // sometimes we get more than one finished message
            return new FinishedTimingState(startedAtHunderedNanoseconds, hunderedNanoseconds);
        }

        @Override public long timeHunderedNanoseconds() {
            return finishedAtHunderedNanoseconds - startedAtHunderedNanoseconds;
        }
    }

    private TimingState timingState = new InitialTimingState();
    @Override public void time(final TimingBookend bookend, final TimingEvent event, final long hunderedNanoseconds) {
        if(bookend == TimingBookend.Starting && event == TimingEvent.ImplementationVerification) {
            timingState = timingState.starting(hunderedNanoseconds);
        } else if(bookend == TimingBookend.Finished && event == TimingEvent.ImplementationVerification) {
            timingState = timingState.finished(hunderedNanoseconds);
        }
    }

    public VerificationStatus getStatus(final int index) {
        return rows.get(index);
    }

    public String getTrace(final int index) {
        return getStatus(index).trace();
    }

    public void getNextFailure(final int afterRow, final Consumer<Integer> cont) {
        final int startAt = (Math.max(afterRow+1,0) % rows.size());

        for (int i = startAt; i != afterRow; i=(i+1) % rows.size()) {
            if(rows.get(i).isFailure()) {
                cont.accept(i);
                break;
            }
        }
    }

    public void getPreviousFailure(final int beforeRow, final Consumer<Integer> cont) {
        final int startAt = (Math.max(beforeRow-1,0) % rows.size());

        for (int i = startAt; i != beforeRow; i=(i+(rows.size()-1)) % rows.size()) {
            if(rows.get(i).isFailure()) {
                cont.accept(i);
                break;
            }
        }
    }
}
