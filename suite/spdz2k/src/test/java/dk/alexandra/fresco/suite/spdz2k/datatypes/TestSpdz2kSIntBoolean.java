package dk.alexandra.fresco.suite.spdz2k.datatypes;

import org.junit.Test;

public class TestSpdz2kSIntBoolean {

  @Test
  public void test() {
//    CompUInt128 alphaA = new CompUInt128(0L, 1); // 2k - 1
//    CompUInt128 alphaB = new CompUInt128(0L, 1); // 2k - 1
    CompUInt128 alpha = new CompUInt128(11111L, (int) (1L << 31), 1);

    CompUInt128 valueA = new CompUInt128Bit(0L, 1);
    CompUInt128 macA = valueA.toArithmeticRep().multiply(alpha);

    CompUInt128 valueB = new CompUInt128Bit(0L, 0);
    CompUInt128 macB = valueB.toArithmeticRep().multiply(alpha);

    CompUInt128 sum = valueA.add(valueB);
    CompUInt128 macSum = macA.add(macB);

    System.out.println("sum " + sum + " macSum " + macSum);
    System.out.println("alpha * sum (regular arithmetic) " + sum.toArithmeticRep()
        .multiply(alpha));
    System.out.println("alpha * sum (bool arithmetic) " + sum.multiply(alpha));
  }
}
