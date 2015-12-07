package com.lexicalscope.dafny.dafnyservergui.gui;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;

import org.junit.Test;

public class TestElapsedHunderedNanoseconds {
    @Test public void zeroIsRenderedTo7DecimalPlaces() {
        assertThat(new ElapsedHunderedNanoseconds(0), hasToString("0.0000000"));
    }

    @Test public void fractionIsRenderedTo7DecimalPlaces() {
        assertThat(new ElapsedHunderedNanoseconds(300), hasToString("0.0000300"));
    }

    @Test public void secondsAreRenderedTo7DecimalPlaces() {
        assertThat(new ElapsedHunderedNanoseconds(310000300), hasToString("31.0000300"));
    }
}
