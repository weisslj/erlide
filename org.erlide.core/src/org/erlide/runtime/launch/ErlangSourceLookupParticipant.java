package org.erlide.runtime.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupParticipant;
import org.erlide.runtime.debug.ErlangStackFrame;

public class ErlangSourceLookupParticipant extends
        AbstractSourceLookupParticipant {

    public ErlangSourceLookupParticipant() {
        super();
    }

    public String getSourceName(final Object object) throws CoreException {
        if (!(object instanceof ErlangStackFrame)) {
            return null;
        }
        final ErlangStackFrame f = (ErlangStackFrame) object;
        System.out.println("SOURCE for " + f.getName() + ": " + f.getModule());
        return f.getModule() + ".erl";
    }
}
