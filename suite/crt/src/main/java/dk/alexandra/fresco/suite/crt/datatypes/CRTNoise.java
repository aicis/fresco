package dk.alexandra.fresco.suite.crt.datatypes;

public class CRTNoise {
    private final CRTSInt noisePair;

    public CRTNoise(CRTSInt noisePair) {
        this.noisePair = noisePair;
    }

    public CRTSInt getNoisePair() {
        return noisePair;
    }
}
