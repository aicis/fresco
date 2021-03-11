package dk.alexandra.fresco.lib.helper;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.ProtocolProducer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

public class ParallelProtocolProducerTest {

  @Test
  public void iterate() {
    Iterator<ProtocolProducer> iterator = mock(Iterator.class);
    ProtocolCollection protocolCollection = mock(ProtocolCollection.class);
    ProtocolProducer protocolProducer = mock(ProtocolProducer.class);

    when(iterator.hasNext()).thenReturn(true, false);
    when(protocolCollection.hasFreeCapacity()).thenReturn(true);
    when(protocolProducer.hasNextProtocols()).thenReturn(false);
    when(iterator.next()).thenReturn(protocolProducer);

    List<ProtocolProducer> list = Arrays.asList(protocolProducer);
    ParallelProtocolProducer parallelProtocolProducer =
        new ParallelProtocolProducer(list);
    parallelProtocolProducer.iterate(iterator, protocolCollection);

    verify(iterator, times(1)).remove();
  }
}
