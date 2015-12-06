package com.lexicalscope.dafny.dafnyservergui;

import static org.junit.Assert.fail;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class TestServerOutputPreParser {
    private final String exampleFilename = "C:\\Users\\flami_000\\Documents\\BoundedProofs\\Arithmetic.dfy";
    private final String[] exampleSuccesful = new String []{
    "C:\\Users\\flami_000\\Documents\\BoundedProofs\\Arithmetic.dfy(3,24): Info: {:induction x, y}",
    ">>> Starting resolution   [0 s]",
    ">>> Starting resolution   [0.5470041 s]",
    ">>> Starting typechecking   [0.6251319 s]",
    ">>> Starting abstract interpretation   [0.9220175 s]",
    ">>> Starting implementation verification   [1.093904 s]",
    ">>> Starting implementation verification   [1.093904 s]",
    ">>> Starting live variable analysis   [1.2032817 s]",
    ">>> Starting live variable analysis   [1.2032817 s]",
    ">>> Finished implementation verification   [2.2970859 s]",
    "",
    "Verifying CheckWellformed$$_module.__default.ZeroIdentity ...",
    "  [2 proof obligations]  verified",
    ">>> Starting implementation verification   [2.3283375 s]",
    ">>> Starting live variable analysis   [2.3283375 s]",
    ">>> Finished implementation verification   [2.5314746 s]",
    ">>> Finished implementation verification   [2.5470981 s]",
    "",
    "Verifying Impl$$_module.__default.ZeroIdentity ...",
    "  [18 proof obligations]  verified",
    "",
    "Verifying CheckWellformed$$_module.__default.SuccSymmetry ...",
    "  [6 proof obligations]  verified",
    ">>> Starting implementation verification   [2.5470981 s]",
    ">>> Starting live variable analysis   [2.5470981 s]",
    ">>> Starting implementation verification   [2.5627245 s]",
    ">>> Starting live variable analysis   [2.5627245 s]",
    ">>> Finished implementation verification   [2.8908674 s]",
    ">>> Starting implementation verification   [2.8908674 s]",
    ">>> Starting live variable analysis   [2.8908674 s]",
    ">>> Finished implementation verification   [2.9377424 s]",
    "",
    "Verifying Impl$$_module.__default.SuccSymmetry ...",
    "  [16 proof obligations]  verified",
    "",
    "Verifying CheckWellformed$$_module.__default.SuccLemmaFour ...",
    "  [6 proof obligations]  verified",
    ">>> Starting implementation verification   [2.9377424 s]",
    ">>> Starting live variable analysis   [2.9377424 s]",
    ">>> Finished implementation verification   [3.2190066 s]",
    ">>> Starting implementation verification   [3.2190066 s]",
    ">>> Starting live variable analysis   [3.2346317 s]",
    ">>> Finished implementation verification   [3.2658837 s]",
    "",
    "Verifying Impl$$_module.__default.SuccLemmaFour ...",
    "  [35 proof obligations]  verified",
    "",
    "Verifying CheckWellformed$$_module.__default.AddCommutative ...",
    "  [4 proof obligations]  verified",
    ">>> Starting implementation verification   [3.2658837 s]",
    ">>> Starting live variable analysis   [3.2815096 s]",
    ">>> Finished implementation verification   [3.5002699 s]",
    ">>> Starting implementation verification   [3.5158955 s]",
    ">>> Starting live variable analysis   [3.5158955 s]",
    ">>> Finished implementation verification   [3.9846696 s]",
    ">>> Finished implementation verification   [3.9846696 s]",
    "",
    "Verifying Impl$$_module.__default.AddCommutative ...",
    "  [35 proof obligations]  verified",
    "",
    "Verifying CheckWellformed$$_module.__default.AddAssociative ...",
    "  [8 proof obligations]  verified",
    "",
    "Verifying Impl$$_module.__default.AddAssociative ...",
    "  [105 proof obligations]  verified",
    ">>> Starting implementation verification   [4.0002947 s]",
    ">>> Starting live variable analysis   [4.0002947 s]",
    ">>> Finished implementation verification   [4.3160249 s]",
    "",
    "Verifying CheckWellformed$$_module.__default._hAdd__FULL ...",
    "  [7 proof obligations]  verified",
    ">>> Starting implementation verification   [4.3160249 s]",
    ">>> Starting live variable analysis   [4.3160249 s]",
    ">>> Finished implementation verification   [4.519162 s]",
    "",
    "Verifying CheckWellformed$$_module.__default.reveal__Add ...",
    "  [4 proof obligations]  verified",
    "Verification completed successfully!",
    "[SUCCESS] [[DAFNY-SERVER: EOM]]"};

    @Rule public final JUnitRuleMockery context = new JUnitRuleMockery();

    private final ServerOutputPreParser parser = new ServerOutputPreParser(exampleFilename);
    @Mock PreprocessedServerOutputListener listener;

    @Before public void addListener() {
        parser.add(listener);
    }

    @Test public void canParseLogLine() {
        context.checking(new Expectations(){{
            oneOf(listener).log(
                    "C:\\Users\\flami_000\\Documents\\BoundedProofs\\Arithmetic.dfy",
                    3,
                    24,
                    "Info",
                    "{:induction x, y}");
        }});

        parser.outputLine("C:\\Users\\flami_000\\Documents\\BoundedProofs\\Arithmetic.dfy(3,24): Info: {:induction x, y}");
    }

    @Test public void parseTimeLine() {
        context.checking(new Expectations(){{
            oneOf(listener).time(
                    TimingBookend.Starting,
                    TimingEvent.Typechecking,
                    6251319);
        }});

        parser.outputLine(">>> Starting typechecking   [0.6251319 s]");
    }


    @Test public void parseVerficationStart() {
        context.checking(new Expectations(){{
            oneOf(listener).verifying(
                    VerificationType.CheckWellformed,
                    "default",
                    "ZeroIdentity");
        }});

        parser.outputLine("Verifying CheckWellformed$$_module.__default.ZeroIdentity ...");
    }

    @Test public void parseCached() {
        context.checking(new Expectations(){{
            oneOf(listener).cached(
                    VerificationType.CheckWellformed,
                    "default",
                    "reveal__Add");
        }});

        parser.outputLine("Retrieving cached verification result for implementation CheckWellformed$$_module.__default.reveal__Add...");
    }

    @Test public void parseVerficationCompleted() {
        context.checking(new Expectations(){{
            oneOf(listener).verficationCompleted();
        }});

        parser.outputLine("Verification completed successfully!");
    }


    @Test public void parseVerfied() {
        context.checking(new Expectations(){{
            oneOf(listener).verifed(35);
        }});

        parser.outputLine("  [35 proof obligations]  verified");
    }

    @Test public void parseBlankLine() {
        context.checking(new Expectations(){{
            oneOf(listener).blankLine();
        }});

        parser.outputLine("");
    }

    @Test public void parseEom() {
        context.checking(new Expectations(){{
            oneOf(listener).eom();
        }});

        parser.outputLine("[SUCCESS] [[DAFNY-SERVER: EOM]]");
    }

    @Test public void parseSuccessExample() {
        context.checking(new Expectations(){{
            ignoring(listener);
        }});
        parser.add(new PreprocessedServerOutputListener() {
            @Override public void unrecognised(final String line) {
                fail(line);
            }
        });
        for (final String line : exampleSuccesful) {
            parser.outputLine(line);
        }
    }

}
