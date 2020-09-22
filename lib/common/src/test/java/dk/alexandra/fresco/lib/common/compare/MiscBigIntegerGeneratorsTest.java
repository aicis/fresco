package dk.alexandra.fresco.lib.common.compare;

import java.math.BigInteger;
import java.util.HashMap;
import org.junit.Assert;
import org.junit.Test;

public class MiscBigIntegerGeneratorsTest {

  @Test
  public void getPolyResultNotNull() {
    HashMap<Integer, BigInteger[]> mockMap = new HashMap<>();
    BigInteger[] mockEntry = new BigInteger[] {};
    mockMap.put(2, mockEntry);

    MiscBigIntegerGenerators generators =
        new MiscBigIntegerGenerators(BigInteger.valueOf(5), mockMap);

    Assert.assertArrayEquals(mockEntry, generators.getPoly(2));
  }
}
