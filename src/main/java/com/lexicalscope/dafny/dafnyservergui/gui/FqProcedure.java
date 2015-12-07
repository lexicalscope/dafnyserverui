package com.lexicalscope.dafny.dafnyservergui.gui;

public class FqProcedure {
    private final String module;
    private final String procedure;

    public FqProcedure(final String module, final String procedure) {
        this.module = module;
        this.procedure = procedure;
    }

    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((module == null) ? 0 : module.hashCode());
        result = prime * result + ((procedure == null) ? 0 : procedure.hashCode());
        return result;
    }

    @Override public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FqProcedure other = (FqProcedure) obj;
        if (module == null) {
            if (other.module != null) {
                return false;
            }
        } else if (!module.equals(other.module)) {
            return false;
        }
        if (procedure == null) {
            if (other.procedure != null) {
                return false;
            }
        } else if (!procedure.equals(other.procedure)) {
            return false;
        }
        return true;
    }

}
