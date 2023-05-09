package dk.alexandra.fresco.suite.crt.datatypes;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.value.SInt;

public class CRTCombinedPad extends CRTNoise {
    private final DRes<SInt> rho;
    private final DRes<SInt> psi;

    public CRTCombinedPad(CRTSInt noisePair, DRes<SInt> rho, DRes<SInt> psi) {
        super(noisePair);
        this.rho = rho;
        this.psi = psi;
    }


    public DRes<SInt> getRho() {
        return rho;
    }

    public DRes<SInt> getPsi() {
        return psi;
    }
}
