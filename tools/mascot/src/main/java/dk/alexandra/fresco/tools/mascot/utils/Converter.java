package dk.alexandra.fresco.tools.mascot.utils;

import java.math.BigInteger;
import java.util.BitSet;
import java.util.List;

public class Converter {

  public BitSet toBits(BigInteger value) {
    byte[] asBytes = value.toByteArray();
    return BitSet.valueOf(asBytes);
  }
  
  public BigInteger recombine(List<BigInteger> values, BigInteger prime) {
    // TODO: super-inefficient, optimize!
//    System.out.println(x);
    BigInteger res = BigInteger.ZERO;
    BigInteger two = BigInteger.valueOf(2);
    int power = 0;
    for (BigInteger value : values) {
      BigInteger temp = two.modPow(BigInteger.valueOf(power), prime).multiply(value).mod(prime);
      res = res.add(temp).mod(prime);
      power++;
    }
    return res;
  }
}
