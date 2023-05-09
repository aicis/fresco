package dk.alexandra.fresco.suite.crt.datatypes;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.value.SInt;

public class CRTPadPair {
    private final SInt rhoPad;
    private final SInt psiPad;

    public CRTPadPair(SInt rhoPad, SInt psiPad) {
        this.rhoPad = rhoPad;
        this.psiPad = psiPad;
    }

    public SInt getRho() {
        return rhoPad;
    }

    public SInt getPsi() {
        return psiPad;
    }
}
