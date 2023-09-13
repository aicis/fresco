package dk.alexandra.fresco.lib.common.compare;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.lib.common.math.AdvancedNumeric;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class RandomAdditiveMask implements
    Computation<AdvancedNumeric.RandomAdditiveMask, ProtocolBuilderNumeric> {

  private final int noOfBits;
  public RandomAdditiveMask(int noOfBits) {
    this.noOfBits = noOfBits;
  }

  @Override
  public DRes<AdvancedNumeric.RandomAdditiveMask> buildComputation(
          ProtocolBuilderNumeric builder) {
    return builder.par(par -> {
      Numeric numericBuilder = par.numeric();
      List<DRes<SInt>> bits = new ArrayList<>();
      for (int i = 0; i < noOfBits; i++) {
        DRes<SInt> randomBit = numericBuilder.randomBit();
        bits.add(randomBit);
      }
      return DRes.of(bits);
    }).par((par, bits) -> {
      MiscBigIntegerGenerators oIntGenerators = new MiscBigIntegerGenerators(par.getBasicNumericContext().getModulus());
      List<BigInteger> twoPows = oIntGenerators.getTwoPowersList(noOfBits);
      return Pair.lazy(bits, AdvancedNumeric.using(par).innerProductWithPublicPart(twoPows, bits));
    }).par((par, bitsAndValue) -> DRes.of(new AdvancedNumeric.RandomAdditiveMask(bitsAndValue.getFirst(), bitsAndValue.getSecond())));
  }
}