package dk.alexandra.fresco.framework.configuration;

import static org.junit.Assert.assertEquals;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;

public class TestNetworkUtils {

  private Map<Integer, NetworkConfiguration> confs;

  private static final int n = 3;

  @Before
  public void setUp() {
    confs = NetworkUtil.getNetworkConfigurations(n);
  }

  @Test
  public void testCorrectNumberOfPlayers() {
    assertEquals(n, confs.size());
  }

  @Test
  public void testMyIdIsCorrect() {
    assertEquals(1, confs.get(1).getMyId());
  }

  @Test
  public void testPortIsFree() throws IOException {
    int port = confs.get(1).getParty(1).getPort();
    ServerSocket s = new ServerSocket(port);
    s.close();
  }

  @Test
  public void testGetFreePortsGivesUniquePorts() {
    int numPorts = 100;
    int numIterations = 5;
    for (int i = 0; i < numIterations; i++) {
      Set<Integer> uniquePorts = new HashSet<>(NetworkUtil.getFreePorts(numPorts));
      assertEquals(numPorts, uniquePorts.size());
    }
  }

  @Test
  public void testGetFreePortsGivesFreePorts() throws IOException {
    int numPorts = 100;
    List<Integer> ports = NetworkUtil.getFreePorts(numPorts);
    for (Integer port : ports) {
      new ServerSocket(port).close();
    }
  }
}
