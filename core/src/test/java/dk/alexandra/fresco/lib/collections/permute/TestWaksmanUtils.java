package dk.alexandra.fresco.lib.collections.permute;

import dk.alexandra.fresco.lib.collections.Matrix;
import java.math.BigInteger;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Test;

public class TestWaksmanUtils {

  @Test
  public void testIs2Pow(){
    WaksmanUtils util = new WaksmanUtils();
    Assert.assertFalse(util.isPow2(-2));
    Assert.assertFalse(util.isPow2(14));
    Assert.assertFalse(util.isPow2(5));
    Assert.assertTrue(util.isPow2(4));
    Assert.assertTrue(util.isPow2(8));
    Assert.assertFalse(util.isPow2(-8));
  }

  
  @Test(expected = UnsupportedOperationException.class)
  public void testSetControlBits(){
    WaksmanUtils util = new WaksmanUtils();
   
    int[] controlBits = new int[]{2,3,0,1};
    Matrix<BigInteger> permutationMatrix = util.setControlBits(controlBits);
    Assert.assertThat(permutationMatrix.getHeight(), Is.is(2));
    Assert.assertThat(permutationMatrix.getWidth(), Is.is(controlBits.length-1));
        
    util.setControlBits(new int[]{2,1,0});
  }
  
}
