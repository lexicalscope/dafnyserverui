package com.lexicalscope.dafny.dafnyservergui;

import org.antlr.v4.runtime.RecognitionException;

import com.lexicalscope.dafny.dafnyserver.DafnyServerParser.VerificationArtifactContext;

public interface ParsingResultListener {
    void verificationArtifact(VerificationArtifactContext verificationArtifact);
    void syntaxError(RecognitionException e);
}
