package com.lexicalscope.dafny.dafnyservergui;

import static java.lang.Integer.parseInt;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServerOutputPreParser implements RawServerOutputListener {
    private final List<PreprocessedServerOutputListener> listeners = new ArrayList<>();
    private final String filename;

    public ServerOutputPreParser(final String filename) {
        this.filename = filename;
    }

    @Override public void outputLine(final String line) {
        if(line.startsWith(filename)) {
            fire(parseLogLine(line));
        } else if (line.startsWith(">>>")) {
            fire(parseTimeLine(line.substring(">>> ".length())));
        } else if (line.startsWith("Verifying ")) {
            fire(parseVerifyingLine(line.substring("Verifying ".length())));
        } else if (line.startsWith("Retrieving cached verification result for implementation ")) {
            fire(parseCachedLine(line.substring("Retrieving cached verification result for implementation ".length())));
        } else if (line.startsWith("  [") && line.endsWith("  verified")) {
            fire(parseVerifiedLine(line.substring("  ".length(), line.length() - "  verified".length())));
        } else if (line.equals("Verification completed successfully!")) {
            fire(l -> l.verficationCompleted());
        } else if (line.equals("[SUCCESS] [[DAFNY-SERVER: EOM]]")) {
            fire(l -> l.eom());
        } else if (line.equals("")) {
            fire(l -> l.blankLine());
        } else {
            fire(l -> l.unrecognised(line));
        }
    }

    private Consumer<PreprocessedServerOutputListener> parseCachedLine(final String line) {
        return parseProcedureIdentity(line, (l,id) -> l.cached(id.verificationType, id.module, id.procedure));
    }

    private final Pattern verifiedLinePattern = Pattern.compile("\\[(\\d+) proof obligations\\]");
    private Consumer<PreprocessedServerOutputListener> parseVerifiedLine(final String line) {
        final Matcher matcher = verifiedLinePattern.matcher(line);
        if(matcher.matches()) {
            final int proofObligations = parseInt(matcher.group(1));
            return l -> l.verifed(proofObligations);
        }
        return l -> {};
    }

    private Consumer<PreprocessedServerOutputListener> parseVerifyingLine(final String line) {
        return parseProcedureIdentity(line, (l,id) -> l.verifying(id.verificationType, id.module, id.procedure));
    }

    private final class ProcedureId {VerificationType verificationType; String module; String procedure;}
    private final Pattern verifyingLinePattern = Pattern.compile("(.+?)\\$\\$_module.__([^\\.]+).(.+?) ?\\.{3}");
    private Consumer<PreprocessedServerOutputListener> parseProcedureIdentity(final String line, final BiConsumer<PreprocessedServerOutputListener, ProcedureId> listener) {
        final ProcedureId procedureId = new ProcedureId();

        final Matcher matcher = verifyingLinePattern.matcher(line);
        if(matcher.matches()) {
            final String verificationTypeString = matcher.group(1);
            procedureId.module = matcher.group(2);
            procedureId.procedure = matcher.group(3);

            switch(verificationTypeString) {
                case "CheckWellformed": procedureId.verificationType = VerificationType.CheckWellformed; break;
                case "Impl": procedureId.verificationType = VerificationType.Impl; break;
                default: procedureId.verificationType = VerificationType.Unknown; break;
            }
            return l -> listener.accept(l, procedureId);
        }
        return l -> {};
    }

    /*
     * bookend = sw1tch(bookendString,
     *              d3fault(TimingBookend.Unknown)
     *              kase("Starting", TimingBookend.Starting),
     *              kase("Finished", TimingBookend.Finished));
     */

    private final Pattern timeLinePattern = Pattern.compile("(\\w+) (.*?)   \\[(\\d+)\\.(\\d{7})? s\\]");
    private Consumer<PreprocessedServerOutputListener> parseTimeLine(final String line) {
        final Matcher matcher = timeLinePattern.matcher(line);
        if(matcher.matches()) {
            final String bookendString = matcher.group(1);
            final String eventString = matcher.group(2);
            final long seconds = Long.parseLong(matcher.group(3));
            final long hunderedNanoseconds = matcher.group(4) == null ? 0L : Long.parseLong(matcher.group(4));

            final TimingBookend bookend;
            switch(bookendString) {
                case "Starting": bookend = TimingBookend.Starting; break;
                case "Finished": bookend = TimingBookend.Finished; break;
                default: bookend = TimingBookend.Unknown; break;
            }

            final TimingEvent event;
            switch(eventString) {
                case "resolution": event = TimingEvent.Resolution; break;
                case "typechecking": event = TimingEvent.Typechecking; break;
                case "abstract interpretation": event = TimingEvent.AbstractInterpretation; break;
                case "implementation verification": event = TimingEvent.ImplementationVerification; break;
                case "live variable analysis": event = TimingEvent.LiveVariableAnalysis; break;
                default: event = TimingEvent.Unknown; break;
            }

            return l -> l.time(bookend, event, (seconds*1000*1000*10) + hunderedNanoseconds);
        }
        return l -> {};
    }

    private final Pattern logLinePattern = Pattern.compile("\\((\\d+),(\\d+)\\): ([^:]+): (.*)");
    private Consumer<PreprocessedServerOutputListener> parseLogLine(final String line) {
        final Matcher matcher = logLinePattern.matcher(line.substring(filename.length()));
        if(matcher.matches()) {
            final String lineNumber = matcher.group(1);
            final String columnNumber = matcher.group(2);
            final String level = matcher.group(3);
            final String message = matcher.group(4);
            return l -> l.log(filename, parseInt(lineNumber), parseInt(columnNumber), level, message);
        }
        return l -> {};
    }

    private void fire(final Consumer<PreprocessedServerOutputListener> event) {
        listeners.forEach(event);
    }

    public void add(final PreprocessedServerOutputListener preprocessedServerOutputListener) {
        listeners.add(preprocessedServerOutputListener);
    }
}
