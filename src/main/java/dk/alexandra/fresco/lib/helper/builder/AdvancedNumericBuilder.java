package dk.alexandra.fresco.lib.helper.builder;

import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.framework.value.SIntFactory;
import dk.alexandra.fresco.lib.math.integer.division.DivisionFactory;

public class AdvancedNumericBuilder extends AbstractProtocolBuilder {

    private DivisionFactory divisionFactory;
    private SIntFactory intFactory;

    public AdvancedNumericBuilder(DivisionFactory divisionFactory, SIntFactory intFactory) {
        this.divisionFactory = divisionFactory;
        this.intFactory = intFactory;
    }

    /**
     * Integer division of two shared integers.
     * @param maxNumeratorLength the maximum amount of bits in the numerator.
     * @param maxDenominatorLength the maximum amount of bits in the denominator.
     * @return <code>numerator / denominator</code>
     */
    public SInt div(SInt numerator, int maxNumeratorLength, SInt denominator, int maxDenominatorLength) {
        SInt out = intFactory.getSInt();
        append(divisionFactory.getDivisionProtocol(numerator, maxNumeratorLength, denominator, maxDenominatorLength, out));
        return out;
    }

    @Override
    public void addProtocolProducer(ProtocolProducer pp) {
        append(pp);
    }
}
