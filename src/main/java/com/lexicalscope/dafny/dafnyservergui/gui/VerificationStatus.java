package com.lexicalscope.dafny.dafnyservergui.gui;

import com.lexicalscope.dafny.dafnyserverui.VerificationType;

public class VerificationStatus {
    public interface VerificationState {
        VerificationState verifyWellFormed();
        VerificationState verifyImpl();
        VerificationState verified(int proofObligations, long timeHunderedNanoseconds);
        VerificationState failed(int proofObligations, long timeHunderedNanoseconds);
        default VerificationState verifyUnknown() { return new ErrorState("unknown verification message"); }
        default Verified verified() { return Verified.Unknown; }
        default String message() { return ""; }
        VerificationState done();
        default long timeHunderedNanoseconds() { return 0; }
    }

    public static class ErrorState  implements VerificationState {
        private final String message;

        public ErrorState(final String message) {
            this.message = message;
        }

        @Override public VerificationState verifyWellFormed() {
            return this;
        }

        @Override public VerificationState verified(final int proofObligations, final long timeHunderedNanoseconds) {
            return this;
        }

        @Override public VerificationState failed(final int proofObligations, final long timeHunderedNanoseconds) {
            return this;
        }

        @Override public VerificationState verifyImpl() {
            return this;
        }

        @Override public VerificationState verifyUnknown() {
            return this;
        }

        @Override public VerificationState done() {
            return this;
        }

        @Override public Verified verified() {
            return Verified.ServerError;
        }

        @Override public String message() {
            return message;
        }
    }

    public static class InitialState implements VerificationState {
        @Override public VerificationState verifyWellFormed() {
            return new VerifyingWellFormed();
        }

        @Override public VerificationState verified(final int proofObligations, final long timeHunderedNanoseconds) {
            return new ErrorState("verified before verification started");
        }

        @Override public VerificationState failed(final int proofObligations, final long timeHunderedNanoseconds) {
            return new ErrorState("failed before verification started");
        }

        @Override public VerificationState verifyImpl() {
            return new ErrorState("verifying Impl before WellFormed");
        }

        @Override public VerificationState done() {
            return new ErrorState("verifying done before it started");
        }
    }

    public static class VerifyingWellFormed implements VerificationState {
        @Override public VerificationState verifyWellFormed() {
            return new ErrorState("already verifiying WellFormed");
        }

        @Override public VerificationState verified(final int proofObligations, final long timeHunderedNanoseconds) {
            return new VerifiedWellFormed(proofObligations, timeHunderedNanoseconds);
        }

        @Override public VerificationState failed(final int proofObligations, final long timeHunderedNanoseconds) {
            return new NotWellFormed(proofObligations, timeHunderedNanoseconds);
        }

        @Override public VerificationState verifyImpl() {
            return new ErrorState("verifying Impl started before verifying WellFormed ended");
        }

        @Override public VerificationState done() {
            return new ErrorState("verifying done durring WellFormed");
        }
    }

    public static class VerifiedWellFormed implements VerificationState {
        private final long timeHunderedNanoseconds;

        public VerifiedWellFormed(final int proofObligations, final long verificationTimeHunderedNanoseconds) {
            this.timeHunderedNanoseconds = verificationTimeHunderedNanoseconds;
        }

        @Override public VerificationState verifyWellFormed() {
            // some procedures don't seem to have an impl, particularly opaque ones
            return new VerifyingWellFormed();
        }

        @Override public VerificationState verified(final int proofObligations, final long timeHunderedNanoseconds) {
            return new ErrorState("already verified WellFormed, was expecing Impl verification");
        }

        @Override public VerificationState failed(final int proofObligations, final long timeHunderedNanoseconds) {
            return new ErrorState("already verified WellFormed, was expecing Impl verification");
        }

        @Override public VerificationState verifyImpl() {
            return new VerifyingImpl(timeHunderedNanoseconds);
        }

        @Override public Verified verified() {
            return Verified.WellFormed;
        }

        @Override public VerificationState done() {
            return this;
        }

        @Override public long timeHunderedNanoseconds() {
            return timeHunderedNanoseconds;
        }
    }

    private static final class NotWellFormed implements VerificationState {
        private final long timeHunderedNanoseconds;

        public NotWellFormed(final int proofObligations, final long timeHunderedNanoseconds) {
            this.timeHunderedNanoseconds = timeHunderedNanoseconds;
        }

        @Override public VerificationState verifyWellFormed() {
            return new VerifyingWellFormed();
        }

        @Override public VerificationState verifyImpl() {
            return new ErrorState("not WellFormed, so surprised to see Impl verification");
        }

        @Override public VerificationState verified(final int proofObligations, final long timeHunderedNanoseconds) {
            return new ErrorState("not WellFormed, so surprised to see verification result");
        }

        @Override public VerificationState failed(final int proofObligations, final long timeHunderedNanoseconds) {
            return new ErrorState("not WellFormed, so surprised to see verification result");
        }

        @Override public VerificationState done() {
            return this;
        }

        @Override public Verified verified() {
            return Verified.Failure;
        }

        @Override public long timeHunderedNanoseconds() {
            return timeHunderedNanoseconds;
        }
    }


    public static class VerifyingImpl implements VerificationState {
        private final long timeSofarHunderedNanoseconds;

        public VerifyingImpl(final long timeSofarHunderedNanoseconds) {
            this.timeSofarHunderedNanoseconds = timeSofarHunderedNanoseconds;
        }

        @Override public VerificationState verifyWellFormed() {
            return new ErrorState("already verified WellFormed");
        }

        @Override public VerificationState verifyImpl() {
            return new ErrorState("already verifying Impl");
        }

        @Override public VerificationState verified(final int proofObligations, final long timeHunderedNanoseconds) {
            return new VerifiedImpl(proofObligations, timeSofarHunderedNanoseconds + timeHunderedNanoseconds);
        }

        @Override public VerificationState failed(final int proofObligations, final long timeHunderedNanoseconds) {
            return new NotVerifiedImpl(proofObligations, timeSofarHunderedNanoseconds + timeHunderedNanoseconds);
        }

        @Override public Verified verified() {
            return Verified.WellFormed;
        }

        @Override public VerificationState done() {
            return new ErrorState("verifying done whilst verifying Impl");
        }
    }

    private static final class NotVerifiedImpl implements VerificationState {
        private final long timeHunderedNanoseconds;

        public NotVerifiedImpl(final int proofObligations, final long timeHunderedNanoseconds) {
            this.timeHunderedNanoseconds = timeHunderedNanoseconds;
        }

        @Override public VerificationState verifyWellFormed() {
            return new VerifyingWellFormed();
        }

        @Override public VerificationState verifyImpl() {
            return new ErrorState("not verified, so surprised to see Impl verification");
        }

        @Override public VerificationState verified(final int proofObligations, final long timeHunderedNanoseconds) {
            return new ErrorState("not Verified, so surprised to see verification result");
        }

        @Override public VerificationState failed(final int proofObligations, final long timeHunderedNanoseconds) {
            return new ErrorState("not verified, so surprised to see verification result");
        }

        @Override public VerificationState done() {
            return this;
        }

        @Override public Verified verified() {
            return Verified.Failure;
        }

        @Override public long timeHunderedNanoseconds() {
            return timeHunderedNanoseconds;
        }
    }

    public static class VerifiedImpl implements VerificationState {
        private final long timeHunderedNanoseconds;

        public VerifiedImpl(final int proofObligations, final long timeHunderedNanoseconds) {
            this.timeHunderedNanoseconds = timeHunderedNanoseconds;
        }

        @Override public VerificationState verifyWellFormed() {
            return new VerifyingWellFormed();
        }

        @Override public VerificationState verified(final int proofObligations, final long timeHunderedNanoseconds) {
            return new ErrorState("already verified Impl");
        }

        @Override public VerificationState failed(final int proofObligations, final long timeHunderedNanoseconds) {
            return new ErrorState("already verified Impl");
        }

        @Override public VerificationState verifyImpl() {
            return new ErrorState("already verified Impl");
        }

        @Override public Verified verified() {
            return Verified.Successful;
        }

        @Override public VerificationState done() {
            return this;
        }

        @Override public long timeHunderedNanoseconds() {
            return timeHunderedNanoseconds;
        }
    }

    private final String module;
    private final String procedure;

    private VerificationState state = new InitialState();
    private final StringBuilder trace = new StringBuilder();

    public VerificationStatus(final String module, final String procedure) {
        this.module = module;
        this.procedure = procedure;
    }

    public String procedureName() {
        return procedure;
    }

    public void verifying(final VerificationType verificationType) {
        switch (verificationType) {
        case CheckWellformed:
            state = state.verifyWellFormed();
            break;
        case Impl:
            state = state.verifyImpl();
            break;
        case Unknown:
            state = state.verifyUnknown();
            break;
        }
    }

    public void done() {
        state = state.done();
    }

    public Verified verified() {
        return state.verified();
    }

    public void verified(final int proofObligations, final long timeHunderedNanoseconds) {
        state = state.verified(proofObligations, timeHunderedNanoseconds);
    }

    public void failed(final int proofObligations, final long timeHunderedNanoseconds) {
        state = state.failed(proofObligations, timeHunderedNanoseconds);
    }

    public String message() {
        return state.message();
    }

    public String trace() {
        return trace.toString();
    }

    public long timeHunderedNanoseconds() {
        return state.timeHunderedNanoseconds();
    }

    public void log(final String filename, final int lineNumber, final int columnNumber, final String level, final String message) {
        trace.append(String.format("<a href=\"fileloc:///%s(%d,%d)\">%1$s(%2$d,%3$d)</a>: %s: %s", filename, lineNumber, columnNumber, level, message));
    }

    public boolean isFailure() {
        return verified().equals(Verified.Failure);
    }
}
