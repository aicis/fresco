package dk.alexandra.fresco.framework.util;

import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Test;

public class TestTiming {

  @Test
  public void testBasic() {
    Timing timing = new Timing();
    timing.start();
    try{
      Thread.sleep(200);
    } catch(Exception ignored) {
    }
    long stopped = timing.stop();
    Assert.assertTrue(timing.getTimeInNanos() > 200*1000000);
    Assert.assertTrue(timing.getTimeInNanos() < 210*1000000); // at most 5% off
    Assert.assertThat(stopped, Is.is(timing.getTimeInNanos()));
  }

/*
 * TODO  @Test
 
  public void testFormatting(){
    String format = Timing.formatNanosAsMilliSeconds(200);
    Assert.assertThat(format, Is.is("2.0E-4 ms"));
  }
 */ 
}

