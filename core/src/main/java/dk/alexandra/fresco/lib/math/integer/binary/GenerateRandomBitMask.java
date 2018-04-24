package dk.alexandra.fresco.lib.math.integer.binary;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.RandomBitMask;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import java.util.ArrayList;
import java.util.List;

/**
 * See {@link dk.alexandra.fresco.framework.builder.numeric.AdvancedNumeric#randomBitMask(int)}.
 */
public class GenerateRandomBitMask implements Computation<RandomBitMask, ProtocolBuilderNumeric> {

  private final int numBits;

  public GenerateRandomBitMask(int numBits) {
    this.numBits = numBits;
  }

  @Override
  public DRes<RandomBitMask> buildComputation(ProtocolBuilderNumeric builder) {
    DRes<List<DRes<SInt>>> randomBits = builder.par(par -> {
      List<DRes<SInt>> innerRandomBits = new ArrayList<>(numBits);
      for (int i = 0; i < numBits; i++) {
        innerRandomBits.add(par.numeric().randomBit());
      }
      return () -> innerRandomBits;
    });
    // TODO should be its own computation, i.e., recombine(bits)
    List<DRes<OInt>> powersOfTwo = builder.getOIntArithmetic().getPowersOfTwo(numBits);
    DRes<SInt> recombined = builder.advancedNumeric()
        .innerProductWithPublicPart(() -> powersOfTwo, randomBits);
    return () -> new RandomBitMask(randomBits, recombined);
  }

}
