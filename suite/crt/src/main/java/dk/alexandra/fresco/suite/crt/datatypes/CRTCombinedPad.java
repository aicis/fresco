package dk.alexandra.fresco.suite.crt.datatypes;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.value.SInt;

import java.util.List;

public class CRTCombinedPad {
    private final CRTSInt noisePair;
    private final DRes<SInt> rho;
    private final DRes<SInt> psi;

    public CRTCombinedPad(CRTSInt noisePair, DRes<SInt> rho, DRes<SInt> psi) {
        this.noisePair = noisePair;
        this.rho = rho;
        this.psi = psi;
    }

    public CRTSInt getNoisePair() {
        return noisePair;
    }

    public DRes<SInt> getRho() {
        return rho;
    }

    public DRes<SInt> getPsi() {
        return psi;
    }
}
