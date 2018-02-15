package dk.alexandra.fresco.fixedpoint.basic;

import java.math.BigDecimal;
import java.util.List;

public class TestUtils {

  static boolean isEqual(BigDecimal a, BigDecimal b) {
    if (a.compareTo(b) != 0) {
      System.out.println(a + " != " + b);
      return false;
    }
    return true;
  }
  
  static boolean isEqual(List<BigDecimal> a, List<BigDecimal> b) {
    if (a.size() != b.size()) {
      return false;
    }
    
    for (int i = 0; i < a.size(); i++) {
      if (!isEqual(a.get(i), b.get(i))) {
        return false;
      }
    }
    return true;
  }
  
}
