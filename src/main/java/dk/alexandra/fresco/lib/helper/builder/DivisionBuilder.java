package dk.alexandra.fresco.lib.helper.builder;

import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.framework.value.SIntFactory;
import dk.alexandra.fresco.lib.math.integer.division.DivisionFactory;

public class DivisionBuilder extends AbstractProtocolBuilder {

    private DivisionFactory divisionFactory;
    private SIntFactory intFactory;

    public DivisionBuilder(DivisionFactory divisionFactory, SIntFactory intFactory) {
        this.divisionFactory = divisionFactory;
        this.intFactory = intFactory;
    }

    public SInt div(SInt p, int maxBitLengthP, SInt q, int maxBitLengthQ) {
        SInt out = intFactory.getSInt();
        append(divisionFactory.getDivisionProtocol(p, maxBitLengthP, q, maxBitLengthQ, out));
        return out;
    }

    @Override
    public void addProtocolProducer(ProtocolProducer pp) {
        append(pp);
    }
}
