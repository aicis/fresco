package dk.alexandra.fresco.lib.helper.builder;

import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.OInt;
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
        append(divisionFactory.getDivisionProtocol(
                numerator, maxNumeratorLength, denominator, maxDenominatorLength, out));
        return out;
    }

    /**
     * Integer division of two shared integers, with an adjustable precision.
     * @param maxNumeratorLength the maximum amount of bits in the numerator.
     * @param maxDenominatorLength the maximum amount of bits in the denominator.
     * @param precision the correct number of bits in the result
     * @return <code>numerator / denominator</code>
     */
    public SInt div(SInt numerator, int maxNumeratorLength, SInt denominator, int maxDenominatorLength, OInt precision) {
        SInt out = intFactory.getSInt();
        append(divisionFactory.getDivisionProtocol(
                numerator, maxNumeratorLength, denominator, maxDenominatorLength, out, precision));
        return out;
    }

    /**
     * Integer division of shared integer by known integer.
     * @param maxNumeratorLength the maximum amount of bits in the numerator.
     * @return <code>numerator / denominator</code>
     */
    public SInt div(SInt numerator, int maxNumeratorLength, OInt denominator) {
        SInt out = intFactory.getSInt();
        append(divisionFactory.getDivisionProtocol(numerator, maxNumeratorLength, denominator, out));
        return out;
    }

    /**
     * Integer division of shared integer by known integer. Returns quotient and remainder.
     * @param maxNumeratorLength the maximum amount of bits in the numerator.
     * @return
     *      An array of length 2,
     *      where first element is <code>(numerator / denominator)</code>,
     *      and the second element is <code>(numerator % denominator)</code>.
     */
    public SInt[] divWithRemainder(SInt numerator, int maxNumeratorLength, OInt denominator) {
        SInt quotient = intFactory.getSInt();
        SInt remainder = intFactory.getSInt();
        append(divisionFactory.getDivisionProtocol(
                numerator, maxNumeratorLength, denominator, quotient, remainder));
        return new SInt[]{ quotient, remainder };
    }

    /**
     * Returns the remainder of division of a shared integer by a known integer.
     * @param maxNumeratorLength the maximum amount of bits in the numerator.
     * @return <code>numerator % denominator</code>
     */
    public SInt mod(SInt numerator, int maxNumeratorLength, OInt denominator) {
        return divWithRemainder(numerator, maxNumeratorLength, denominator)[1];
    }

    @Override
    public void addProtocolProducer(ProtocolProducer pp) {
        append(pp);
    }
}
