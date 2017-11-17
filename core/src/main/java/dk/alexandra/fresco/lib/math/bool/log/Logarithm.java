package dk.alexandra.fresco.lib.math.bool.log;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.value.SBool;
import java.util.ArrayList;
import java.util.List;

/**
 * This class implements logarithm base 2 for binary protocols. It is currently up to the
 * application programmer to check if the input is 0. It is well-defined to input 0 and will return
 * 0, but this is not correct as log_2(0) = NaN It uses a method consisting of 3 steps: - Prefix OR:
 * Starting from most significant bit of the input X, OR with the next bit. This causes a bit vector
 * of the form Y=[0,0,...,1,1,...,1].
 * <p>
 * - XOR sum: The resulting bit Zi in the bit vector Z is given as: Y(i+1) XOR Yi This gives a
 * result of the form: Z=[0,...,0,1,0,...,0]. This is the result of the function
 * 2^{floor(log(X))+1}.
 * </p>
 * <p>
 * - Finally, we get hold of only floor(log(X))+1 by having the result Ai become: forall j: XOR (Zj
 * AND i'th bit of j) This means fx if Z = [0,1,0], then A0 becomes = (Z0 AND 0'th bit of 0) XOR (Z1
 * AND 0'th bit of 1) XOR (Z2 AND 0'th bit of 2) = 0 XOR 1 XOR 0 = 1 Whereas A1 = (Z0 AND 1'th bit
 * of 0) XOR (Z1 AND 1'th bit of 1) XOR (Z2 AND 1'th bit of 2) = 0 XOR 0 XOR 0 = 0 and A2 is also 0,
 * which gives the correct result of A = [0,0,1].
 * </p>
 */
public class Logarithm implements
    Computation<List<DRes<SBool>>, ProtocolBuilderBinary> {

  private List<DRes<SBool>> number;

  /**
   * Note that on an input of 0, this implementation yields 0, which is incorrect.
   * The application is itself responsible for checking that we do indeed not input 0.
   *
   * @param number The number which we want to calculate log base 2 on.
   */
  public Logarithm(List<DRes<SBool>> number) {
    this.number = number;
  }


  @Override
  public DRes<List<DRes<SBool>>> buildComputation(ProtocolBuilderBinary builder) {
    return builder.seq(seq -> {
      List<DRes<SBool>> ors = new ArrayList<>();
      ors.add(number.get(0));
      for (int i = 1; i < number.size(); i++) {
        ors.add(seq.advancedBinary().or(number.get(i), ors.get(i - 1)));
      }
      return () -> ors;
    }).seq((seq, ors) -> {
      List<DRes<SBool>> xors = new ArrayList<>();
      xors.add(seq.binary().xor(ors.get(0), seq.binary().known(false)));

      for (int i = 1; i < number.size(); i++) {
        xors.add(seq.binary().xor(ors.get(i - 1), ors.get(i)));
      }
      xors.add(seq.binary().known(false));
      return () -> xors;
    }).seq((seq, xors) -> {
      List<DRes<SBool>> res = new ArrayList<>();
      for (int j = 0; j < log2(number.size()) + 1; j++) {
        res.add(seq.binary().known(false));
      }
      for (int j = 0; j < log2(number.size()) + 1; j++) {
        for (int i = 0; i < xors.size(); i++) {
          boolean ithBit = ithBit(xors.size() - 1 - i, res.size() - 1 - j); //j'th bit of i
          DRes<SBool> tmp = seq.binary().and(xors.get(i), seq.binary().known(ithBit));
          res.add(j, seq.binary().xor(tmp, res.remove(j)));
        }
      }
      return () -> res;
    });
  }

  /**
   * Computes the floor(log_2(x)).
   */
  private int log2(int n) {
    return 31 - Integer.numberOfLeadingZeros(n);
  }


  private boolean ithBit(int no, int i) {
    return ((no >> i) & 0x01) == 1;
  }

}
