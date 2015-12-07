package com.lexicalscope.dafny.dafnyservergui.gui;

import com.lexicalscope.dafny.dafnyserverui.VerificationType;

public class VerificationStatus {
    private final String module;
    private final String procedure;

    public VerificationStatus(final String module, final String procedure) {
        this.module = module;
        this.procedure = procedure;
    }

    public String procedureName() {
        return procedure;
    }

    public void verifying(final VerificationType verificationType) {

    }
}
