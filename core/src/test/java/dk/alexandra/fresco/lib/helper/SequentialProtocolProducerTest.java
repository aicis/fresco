package dk.alexandra.fresco.lib.helper;

import dk.alexandra.fresco.suite.dummy.bool.DummyBooleanNotProtocol;
import org.hamcrest.core.Is;
import org.hamcrest.core.StringContains;
import org.junit.Assert;
import org.junit.Test;

public class SequentialProtocolProducerTest {

  @Test
  public void testToString() throws Exception {
    SequentialProtocolProducer sequentialProtocolProducer = new SequentialProtocolProducer();
    sequentialProtocolProducer
        .append(new SingleProtocolProducer<>(new DummyBooleanNotProtocol(null)));
    String toString = sequentialProtocolProducer.toString();
    Assert.assertThat(toString, StringContains.containsString("SequentialProtocolProducer"));
    Assert.assertThat(toString, StringContains.containsString("SingleProtocolProducer"));
    Assert.assertThat(toString, StringContains.containsString("DummyBooleanNotProtocol"));
    Assert.assertThat(sequentialProtocolProducer.toString(), Is.is(toString));
  }

}