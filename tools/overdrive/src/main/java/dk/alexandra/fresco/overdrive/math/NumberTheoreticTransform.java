package dk.alexandra.fresco.overdrive.math;

import java.math.BigInteger;
import java.util.List;

public interface NumberTheoreticTransform {

  public List<BigInteger> nnt(List<BigInteger> coefficients);

  public List<BigInteger> nntInverse(List<BigInteger> evaluations);

}
