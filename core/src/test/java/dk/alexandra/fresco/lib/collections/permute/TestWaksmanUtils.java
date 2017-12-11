package dk.alexandra.fresco.lib.collections.permute;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import dk.alexandra.fresco.lib.collections.Matrix;
import java.math.BigInteger;
import org.junit.Test;

public class TestWaksmanUtils {

  @Test
  public void testIs2Pow(){
    WaksmanUtils util = new WaksmanUtils();
    assertFalse(util.isPow2(-2));
    assertFalse(util.isPow2(14));
    assertFalse(util.isPow2(5));
    assertTrue(util.isPow2(4));
    assertTrue(util.isPow2(8));
    assertFalse(util.isPow2(-8));
  }

  
  @Test(expected = UnsupportedOperationException.class)
  public void testSetControlBits(){
    WaksmanUtils util = new WaksmanUtils();
   
    int[] controlBits = new int[]{2,3,0,1};
    Matrix<BigInteger> permutationMatrix = util.setControlBits(controlBits);
    assertThat(permutationMatrix.getHeight(), is(2));
    assertThat(permutationMatrix.getWidth(), is(controlBits.length-1));
        
    util.setControlBits(new int[]{2,1,0});
  }
  
}
