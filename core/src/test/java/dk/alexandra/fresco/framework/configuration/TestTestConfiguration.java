package dk.alexandra.fresco.framework.configuration;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class TestTestConfiguration {

	private Map<Integer, NetworkConfiguration> confs;

	private static final int n = 3;
	
	@Before
	public void setUp() {
		confs = TestConfiguration.getNetworkConfigurations(n);
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
	

}
