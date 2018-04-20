package dk.alexandra.fresco.lib.compare.lt;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.AdvancedNumeric;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.conditional.ConditionalSelect;
import java.math.BigInteger;
import java.util.List;

public class LessThanOrEquals implements Computation<SInt, ProtocolBuilderNumeric> {

  public LessThanOrEquals(int bitLength, int securityParameter, DRes<SInt> x, DRes<SInt> y) {
    this.bitLength = bitLength;
    this.securityParameter = securityParameter;
    this.x = x;
    this.y = y;
  }

  // params etc
  private final int bitLength;
  private final int securityParameter;
  private final DRes<SInt> x;
  private final DRes<SInt> y;

  @SuppressWarnings("unchecked")
  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    final BigInteger modulus = builder.getBasicNumericContext().getModulus();

    final int bitLengthBottom = bitLength / 2;
    final int bitLengthTop = bitLength - bitLengthBottom;

    final BigInteger twoToBitLength = BigInteger.ONE.shiftLeft(this.bitLength);
    final BigInteger twoToBitLengthBottom = BigInteger.ONE.shiftLeft(bitLengthBottom);
    final BigInteger twoToNegBitLength = twoToBitLength.modInverse(modulus);

    final BigInteger one = BigInteger.ONE;

    return builder.seq((seq) -> seq.advancedNumeric()
        .additiveMask(bitLength + securityParameter))
        .pairInPar((seq, mask) -> {
          List<DRes<SInt>> bits = mask.bits.subList(0, bitLength);
          List<DRes<SInt>> rBottomBits = bits.subList(0, bitLengthBottom);
          List<BigInteger> twoPowsBottom =
              seq.getBigIntegerHelper().getTwoPowersList(bitLengthBottom);
          return Pair.lazy(mask.random, seq.advancedNumeric().innerProductWithPublicPart(twoPowsBottom, rBottomBits));
        }, (seq, mask) -> {
          List<DRes<SInt>> rTopBits =
              mask.bits.subList(bitLengthBottom, bitLengthBottom + bitLengthTop);
          List<BigInteger> twoPowsTop = seq.getBigIntegerHelper().getTwoPowersList(bitLengthTop);
          AdvancedNumeric innerProduct = seq.advancedNumeric();

          return innerProduct.innerProductWithPublicPart(twoPowsTop, rTopBits);
        }).seq((seq, pair) -> {
          DRes<SInt> rTop = pair::getSecond;
          DRes<SInt> rBottom = pair.getFirst().getSecond();
          SInt r = pair.getFirst().getFirst();

          // construct r-values (rBar, rBottom, rTop)
          DRes<SInt> rBar;

          Numeric numeric = seq.numeric();

          DRes<SInt> tmp1 = numeric.mult(twoToBitLengthBottom, rTop);
          rBar = numeric.add(tmp1, rBottom);

          // Actual work: mask and reveal 2^bitLength+x-y
          // z = 2^bitLength + x -y
          DRes<SInt> diff = numeric.sub(y, x);
          DRes<SInt> z = numeric.add(twoToBitLength, diff);

          // mO = open(z + r)
          DRes<SInt> mS = numeric.add(z, () -> r);
          DRes<BigInteger> mO = seq.numeric().open(mS);

          return () -> new Object[] {mO, rBottom, rTop, rBar, z};
        }).seq((ProtocolBuilderNumeric seq, Object[] input) -> {
          BigInteger mO = ((DRes<BigInteger>) input[0]).out();
          DRes<SInt> rBottom = (DRes<SInt>) input[1];
          DRes<SInt> rTop = (DRes<SInt>) input[2];
          DRes<SInt> rBar = (DRes<SInt>) input[3];
          DRes<SInt> z = (DRes<SInt>) input[4];

          // extract mTop and mBot
          BigInteger mMod = mO.mod(BigInteger.ONE.shiftLeft(bitLength));
          BigInteger mBar = mMod;
          BigInteger mBot = mMod.mod(BigInteger.ONE.shiftLeft(bitLengthBottom));
          BigInteger mTop = mMod.shiftRight(bitLengthBottom);

          Numeric numeric = seq.numeric();
          // dif = mTop - rTop
          DRes<SInt> dif = numeric.sub(mTop, rTop);

          // eqResult <- execute eq.test
          DRes<SInt> eqResult = seq.comparison().compareZero(dif, bitLengthTop);
          return () -> new Object[] {eqResult, rBottom, rTop, mBot, mTop, mBar, rBar, z};
        }).seq((ProtocolBuilderNumeric seq, Object[] input) -> {
          DRes<SInt> eqResult = (DRes<SInt>) input[0];
          DRes<SInt> rBottom = (DRes<SInt>) input[1];
          DRes<SInt> rTop = (DRes<SInt>) input[2];
          BigInteger mBot = (BigInteger) input[3];
          BigInteger mTop = (BigInteger) input[4];
          BigInteger mBar = (BigInteger) input[5];
          DRes<SInt> rBar = (DRes<SInt>) input[6];
          DRes<SInt> z = (DRes<SInt>) input[7];
          // [eqResult]? BOT : TOP (for m and r) (store as mPrime,rPrime)

          // TODO rPrime and mPrime can be computed in parallel
          DRes<SInt> rPrime = seq.seq(new ConditionalSelect(eqResult, rBottom, rTop));

          Numeric numeric = seq.numeric();
          DRes<SInt> negEqResult = numeric.sub(one, eqResult);

          DRes<SInt> prod1 = numeric.mult(mBot, eqResult);
          DRes<SInt> prod2 = numeric.mult(mTop, negEqResult);

          DRes<SInt> mPrime = numeric.add(prod1, prod2);

          DRes<SInt> subComparisonResult;
          if (bitLength == 2) {
            // sub comparison is of length 1: mPrime >= rPrime:
            // NOT (rPrime AND NOT mPrime)
            DRes<SInt> mPrimeNegated = numeric.sub(one, mPrime);

            DRes<SInt> rPrimeStrictlyGTmPrime = numeric.mult(mPrimeNegated, rPrime);
            subComparisonResult = numeric.sub(one, rPrimeStrictlyGTmPrime);
          } else {
            // compare the half-length inputs
            int nextBitLength = (bitLength + 1) / 2;
            subComparisonResult =
                seq.seq(new LessThanOrEquals(nextBitLength, securityParameter, rPrime, mPrime));
          }
          return () -> new Object[] {subComparisonResult, mBar, rBar, z};
        }).seq((ProtocolBuilderNumeric seq, Object[] input) -> {
          DRes<SInt> subComparisonResult = (DRes<SInt>) input[0];
          BigInteger mBar = (BigInteger) input[1];
          DRes<SInt> rBar = (DRes<SInt>) input[2];
          DRes<SInt> z = (DRes<SInt>) input[3];

          Numeric numeric = seq.numeric();

          // u = 1 - subComparisonResult
          DRes<SInt> u = numeric.sub(one, subComparisonResult);

          // res = z - ((m mod 2^bitLength) - (r mod 2^bitlength) + u*2^bitLength)
          DRes<SInt> reducedWithError = numeric.sub(mBar, rBar);
          DRes<SInt> additiveError = numeric.mult(twoToBitLength, u);
          DRes<SInt> reducedNoError = numeric.add(additiveError, reducedWithError);
          DRes<SInt> resUnshifted = numeric.sub(z, reducedNoError);

          // res >> 2^bitLength
          return numeric.mult(twoToNegBitLength, resUnshifted);
        });
  }


}
